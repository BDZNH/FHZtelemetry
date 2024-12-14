#include <jni.h>
#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

#define LOG_TAG "ForzaHorizon"
#include "Log.h"
#include <cstdio>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>

#include <sys/system_properties.h>
#ifndef PROPERTY_VALUE_MAX
#define PROPERTY_VALUE_MAX 82
#endif

#ifndef FULL_JNI_CLASS_NAME
#define FULL_JNI_CLASS_NAME "com/bdznh/fhztelemetry/ForzaHorizonDataOut"
#endif

#include <thread>
#include <condition_variable>
#include <mutex>
#include "ForzaDataFormat.h"
#include "Trace.h"

#define USE_TCP_AS_SERVER

static struct {
    jclass clazz;
    jmethodID onReadForzaHorizonData;
    jmethodID onStartForzaHorizonData;
    jmethodID onPauseForzaHorizonData;
} gServiceClassInfo;

class ScopedAttach {
public:
    ScopedAttach(JavaVM *vm, JNIEnv **env) : vm_(vm) {
        vm_->AttachCurrentThread(env, NULL);
    }

    ~ScopedAttach() {
        vm_->DetachCurrentThread();
    }

private:
    JavaVM *vm_;
};

bool gRunning = false;
bool gPause = false;
std::mutex gMutex;
std::condition_variable gWaiter;
JavaVM* gJvm = nullptr;
jobject gServiceObj;
std::thread* gReceiveThread = nullptr;
int gSocketFD = 0;
void startReceiveForzaData(int port){
    ALOGD("startReceiveForzaData start at %d",port);
    if(!gRunning){
        ALOGE("startReceiveForzaData not running");
        return;
    }
    gSocketFD = socket(AF_INET,SOCK_DGRAM,IPPROTO_UDP);
    if(gSocketFD == -1){
        ALOGE("get socket failed: %s", strerror(errno));
        gSocketFD = 0;
        gRunning = false;
        {
            std::lock_guard<std::mutex> lock(gMutex);
            gWaiter.notify_one();
        }
        return;
    }
    struct sockaddr_in localAddr{},remoteAddr{};
    memset(&localAddr,0,sizeof(localAddr));
    memset(&remoteAddr,0,sizeof(remoteAddr));

    localAddr.sin_family=AF_INET;
    localAddr.sin_port=htons(port);
    localAddr.sin_addr.s_addr = htons(INADDR_ANY);

    socklen_t addrlen = sizeof (localAddr);
    if(bind(gSocketFD, (struct sockaddr*)&localAddr, addrlen)==-1){
        ALOGE("bind failed: %s", strerror(errno));
        close(gSocketFD);
        gSocketFD = 0;
        gRunning = false;
        {
            std::lock_guard<std::mutex> lock(gMutex);
            gWaiter.notify_one();
        }
        return ;
    }
    {
        std::lock_guard<std::mutex> lock(gMutex);
        gWaiter.notify_one();
    }
    JNIEnv* env;
    ScopedAttach attach(gJvm,&env);
    ForzaHorizonDataOutFormat forzadata = {0};
    int sizeForza = sizeof (struct ForzaHorizonDataOutFormat);
    int onMenu = 0;
    u8 tmpdata[1024];
    while(gRunning){
        memset(tmpdata,0,1024);
        addrlen = sizeof(remoteAddr);
        ssize_t readLen = 0;
        {
            ScopedTrace recvTrace("recvfrom");
            readLen = recvfrom(gSocketFD,tmpdata,1024,0,(sockaddr*)&remoteAddr,&addrlen);
        }
        if(readLen > 0){
            if(readLen == sizeForza){
                memcpy(&forzadata,tmpdata,sizeForza);
                if(onMenu != forzadata.IsRaceOn){
                    onMenu = forzadata.IsRaceOn;
                    if(onMenu == 1){
                        ScopedTrace recvTrace("onStartForzaHorizonData");
                        env->CallVoidMethod(gServiceObj,gServiceClassInfo.onStartForzaHorizonData);
                    }else{
                        ScopedTrace recvTrace("onPauseForzaHorizonData");
                        env->CallVoidMethod(gServiceObj,gServiceClassInfo.onPauseForzaHorizonData);
                    }
                }
                if(!gPause && forzadata.IsRaceOn == 1){
                    ScopedTrace recvTrace("onReadForzaHorizonData");
                    env->CallVoidMethod(gServiceObj,gServiceClassInfo.onReadForzaHorizonData,
                                        forzadata.CarOrdinal,forzadata.CarClass,forzadata.CarPerformanceIndex,forzadata.CarCategory,
                                        s32(forzadata.Steer),s32(forzadata.Accel&0xFF),s32(forzadata.Brake&0xFF),forzadata.Gear,forzadata.DrivetrainType,
                                        s32(forzadata.Clutch&0x0FF),s32(forzadata.HandBrake&0xFF),s32(forzadata.NormalizedDrivingLine&0xFF),s32(forzadata.NormalizedAIBrakeDifference&0xFF),
                                        forzadata.Speed,forzadata.EngineIdleRpm,forzadata.EngineMaxRpm,forzadata.CurrentEngineRpm,
                                        forzadata.Power,forzadata.Torque);
                }
            }else{
                ALOGW("respect %d but received %zd",sizeForza,readLen);
            }
        }else{
            ALOGE("received failed %s", strerror(errno));
            break;
        }
    }
    if(gSocketFD != 0){
        shutdown(gSocketFD,SHUT_RDWR);
        close(gSocketFD);
        gSocketFD = 0;
        gRunning = false;
    }
    ALOGD("startReceiveForzaData end");
}

/*******************************************************************************/
// jni start

static jlong nativeStart(JNIEnv* env, jclass /* clazz */,jobject serviceObj,jint port){
    if(gRunning){
        ALOGE("don't start again");
        return -1;
    }
    std::unique_lock<std::mutex> lock(gMutex);
    if(gJvm == nullptr){
        env->GetJavaVM(&gJvm);
    }
    if(gServiceObj == nullptr){
        gServiceObj = env->NewGlobalRef(serviceObj);
    }
    ALOGD("native Start %d",(int)port);
    gRunning = true;
    gPause = false;
    gReceiveThread = new std::thread(startReceiveForzaData,(int)port);
    gWaiter.wait(lock);
    if(gSocketFD >0 && gRunning){
        ALOGD("native Start %d successed",(int)port);
        return 0;
    }else{
        ALOGE("start failed");
        return -1;
    }
}

static void nativePause(JNIEnv* env, jclass /* clazz */){
    if(gRunning){
        gPause = !gPause;
        ALOGD("native %s",(gPause)?"Pause":"Resume");
    }else{
        ALOGE("nativePause have not started");
    }
}

static jlong nativeStop(JNIEnv* env, jclass /* clazz */){
    ALOGD("native Stop");
    if(gRunning){
        gPause = true;
        gRunning = false;
        if(gSocketFD != 0){
            shutdown(gSocketFD,SHUT_RDWR);
            close(gSocketFD);
            gSocketFD = 0;
        }
        gReceiveThread->join();
    }
    return 0;
}

static void nativeRelease(JNIEnv* env, jclass clazz){
    if(gRunning){
        nativeStop(env,clazz);
        env->DeleteGlobalRef(gServiceObj);
        gServiceObj = nullptr;
    }
    ALOGD("native Release");
}

static const JNINativeMethod gKonkaFCTAudioRecordMethods[] = {
        {"nativeStart","(L" FULL_JNI_CLASS_NAME ";I)J",(void*) nativeStart},
        {"nativePause","()V",(void*)nativePause},
        {"nativeStop","()J",(void*)nativeStop},
        {"nativeRelease","()V",(void*)nativeRelease},
};

#define FIND_CLASS(var, className) \
        var = env->FindClass(className); \
        LOG_FATAL_IF(! (var), "Unable to find class " className);

#define GET_METHOD_ID(var, clazz, methodName, methodDescriptor) \
        var = env->GetMethodID(clazz, methodName, methodDescriptor); \
        LOG_FATAL_IF(! (var), "Unable to find method " methodName);

#define GET_STATIC_METHOD_ID(var, clazz, methodName, methodDescriptor) \
        var = env->GetStaticMethodID(clazz, methodName, methodDescriptor); \
        LOG_FATAL_IF(! (var), "Unable to find static method " methodName);

#define GET_FIELD_ID(var, clazz, fieldName, fieldDescriptor) \
        var = env->GetFieldID(clazz, fieldName, fieldDescriptor); \
        LOG_FATAL_IF(! (var), "Unable to find field " fieldName);

int jniRegisterNativeMethods(JNIEnv* env, const char* className,
                             const JNINativeMethod* gMethods, int numMethods)
{
    JNIEnv* e = env;

    ALOGV("Registering %s's %d native methods...", className, numMethods);
    jclass clazz = env->FindClass(className);
    if(clazz == nullptr){
        char* tmp;
        const char* msg;
        if (asprintf(&tmp,
                     "Native registration unable to find class '%s'; aborting...",
                     className) == -1) {
            // Allocation failed, print default warning.
            msg = "Native registration unable to find class; aborting...";
        } else {
            msg = tmp;
        }
        e->FatalError(msg);
    }

    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        char* tmp;
        const char* msg;
        if (asprintf(&tmp, "RegisterNatives failed for '%s'; aborting...", className) == -1) {
            // Allocation failed, print default warning.
            msg = "RegisterNatives failed; aborting...";
        } else {
            msg = tmp;
        }
        e->FatalError(msg);
    }

    return 0;
}

int register_com_example_ForzaHorizonDataOut(JNIEnv* env){
    int res = jniRegisterNativeMethods(env, FULL_JNI_CLASS_NAME,
                                       gKonkaFCTAudioRecordMethods, NELEM(gKonkaFCTAudioRecordMethods));
    (void) res;  // Faked use when LOG_NDEBUG.
    LOG_FATAL_IF(res < 0, "Unable to register native methods.");

    jclass clazz;
    FIND_CLASS(clazz, FULL_JNI_CLASS_NAME);
    gServiceClassInfo.clazz = reinterpret_cast<jclass>(env->NewGlobalRef(clazz));

    GET_METHOD_ID(gServiceClassInfo.onReadForzaHorizonData, clazz,
                  "onReadForzaHorizonData", "(IIIIIIIIIIIIIFFFFFF)V");

    GET_METHOD_ID(gServiceClassInfo.onStartForzaHorizonData,clazz,
                  "onStartForzaHorizonData","()V");

    GET_METHOD_ID(gServiceClassInfo.onPauseForzaHorizonData,clazz,
                  "onPauseForzaHorizonData","()V");
    return 0;
}

// JNI end
/*******************************************************************************/