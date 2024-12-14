package com.bdznh.fhztelemetry;

public class ForzaHorizonDataOut {
    private OnDataOutCallback cb = null;
    ForzaHorizonData mForzaData;
    //native call back
    private void onReadForzaHorizonData(int CarId,int CarClass, int CarPerformanceIndex,int Category,int Steer,
                                        int Accel,int Brake,int Gears,int DrivetrainType,int Clutch, int HandBrake,int DrivingLine,int AiBrakeDifference,
                                        float Speed, float idlerpm,float maxrpm,float currentrpm,
                                        float Power,float Torque){
        if(cb != null){
            mForzaData.CarId = CarId;
            mForzaData.CarClass = CarClass;
            mForzaData.CarPerformanceIndex = CarPerformanceIndex;
            mForzaData.CarCategory = Category;
            mForzaData.Steer = Steer;
            mForzaData.Accel = Accel;
            mForzaData.Brake = Brake;
            mForzaData.Gears = Gears;
            mForzaData.DrivetrainType = DrivetrainType;
            mForzaData.Clutch = Clutch;
            mForzaData.HandBrake = HandBrake;
            mForzaData.DrivingLine = DrivingLine;
            mForzaData.ABS = AiBrakeDifference;
            mForzaData.Speed = Speed;
            mForzaData.Idlerpm= idlerpm;
            mForzaData.Maxrpm = maxrpm;
            mForzaData.Currentrpm = currentrpm;
            mForzaData.Power = Power;
            mForzaData.Torque = Torque;
            cb.onDataOut(mForzaData);
        }
    }

    private void onStartForzaHorizonData(){
        if(cb!=null){
            cb.onStartForzaDataOut();
        }
    }

    private void onPauseForzaHorizonData(){
        if(cb!=null){
            cb.onPauseForzaDataOut();
        }
    }

    public ForzaHorizonDataOut(OnDataOutCallback callback){
        cb=callback;
        mForzaData = new ForzaHorizonData();
        cb.onDataOut(mForzaData);
    }

    public interface OnDataOutCallback{
        void onDataOut(ForzaHorizonData data);
        void onStartForzaDataOut();
        void onPauseForzaDataOut();
    }

    public long start(int port){
        return nativeStart(this,port);
    }

    public void pause(){
        nativePause();
    }

    public long stop(){
        return nativeStop();
    }

    public void release(){
        nativeRelease();
        cb=null;
    }

    private native long nativeStart( ForzaHorizonDataOut instance,int port);
    private native void nativePause();
    private native long nativeStop();
    private native void nativeRelease();
    static {
        System.loadLibrary("forzadata");
    }
}
