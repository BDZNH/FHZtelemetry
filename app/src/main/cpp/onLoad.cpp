//
// Created by Dell3060 on 2022/11/25.
//

#include "jni.h"
#define LOG_TAG "ForzaHorizon"
#include "Log.h"

int register_com_example_ForzaHorizonDataOut(JNIEnv* env);

extern "C" jint JNI_OnLoad(JavaVM* vm, void* /* reserved */)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("GetEnv failed!");
        return result;
    }
    ALOG_ASSERT(env, "Could not retrieve the env!");
    register_com_example_ForzaHorizonDataOut(env);
    return JNI_VERSION_1_4;
}