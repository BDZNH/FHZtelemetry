package com.bdznh.fhztelemetry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.bdznh.fhztelemetry.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ForzaHorizonDataOut.OnDataOutCallback {
    static String TAG = "ForzaHorizon";

    private final static int MSG_REFRESH_UI = 1;
    private final static int MSG_PAUSE_RECV = 2;
    private final static int MSG_START_RECV = 3;
    ForzaHorizonDataOut forza =null;
    ActivityMainBinding mBinding;

    int mCarID;
    int mCarClass;
    int mCarPereformanceIndex;
    int mCategory;
    int mDriveTrainType;
    int mClutch;
    int mHandBrake;

    int mAccel;
    int mBrake;
    float mPower;
    float mTorque;

    ForzaHorizonData mForzaData;
    boolean onListen = false;

    LocalHandler mLocalHandler;

    boolean isVisiable = false;

    boolean isStarted = false;
    boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        initViewState();
        setSystemUIVisibility(false);
        isVisiable = false;
        mLocalHandler = new LocalHandler(this);
        forza = new ForzaHorizonDataOut(this);
        mBinding.forzaDashboard.update(100f,9999,2300,888,3,180,120,-100);
    }

    void initViewState(){
        mBinding.startpause.setOnClickListener((v)->{
            if(isStarted){
                isPaused = !isPaused;
                forza.pause();
                if(isPaused){
                    mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.start_green));
                }else{
                    mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.pause_green));
                }
            }else{
                long ret = forza.start(9998);
                if(ret == 0){
                    isStarted = true;
                    mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.pause_green));
                    mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.stop_white));
                }
            }
        });
        mBinding.stop.setOnClickListener((v)->{
            if(isStarted){
                long ret = forza.stop();
                if(ret == 0){
                    isStarted = false;
                    mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.stop_gray));
                    mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.start_white));
                }
            }
        });
        mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.stop_gray));
        mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.start_white));
        mBinding.carInfo.setOnClickListener((v)->{
            if(mBinding.carInfo.getVisibility() == View.VISIBLE){
                mBinding.carInfo.setVisibility(View.GONE);
            }
        });
        mBinding.forzaDashboard.setOnClickListener((v)->{
            isVisiable = !isVisiable;
            setSystemUIVisibility(isVisiable);
        });
    }

    private void setSystemUIVisibility(boolean visiable){
        if(visiable){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            if(mBinding.carInfo.getVisibility() != View.VISIBLE){
                mBinding.carInfo.setVisibility(View.VISIBLE);
            }
        }else{
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        if(isStarted){
            forza.stop();
        }
        forza.release();
        forza = null;
        super.onDestroy();
    }

    @Override
    public void onDataOut(ForzaHorizonData data) {
        if(mForzaData == null){
            mForzaData = data;
        }
    }

    @Override
    public void onStartForzaDataOut(){
        onListen  = true;
        mLocalHandler.sendEmptyMessage(MSG_START_RECV);
        mLocalHandler.sendEmptyMessage(MSG_REFRESH_UI);
    }

    @Override
    public void onPauseForzaDataOut(){
        onListen  = false;
        mLocalHandler.removeMessages(MSG_REFRESH_UI);
        mLocalHandler.sendEmptyMessage(MSG_PAUSE_RECV);
    }

    @SuppressLint("DefaultLocale")
    public void  refreshUI(){
        if(mForzaData == null){
            return;
        }
        if(mBinding.carInfo.getVisibility() == View.VISIBLE){
            if(mCarClass != mForzaData.CarClass){
                mCarClass = mForzaData.CarClass;
                mBinding.carclass.setText(mapIntToCarClass(mCarClass));
            }
            if(mCarID != mForzaData.CarId){
                mCarID = mForzaData.CarId;
                mBinding.carid.setText(String.valueOf(mCarID));
            }
            if(mCarPereformanceIndex != mForzaData.CarPerformanceIndex){
                mCarPereformanceIndex = mForzaData.CarPerformanceIndex;
                mBinding.carPerform.setText(String.valueOf(mCarPereformanceIndex));
            }
            if(mCategory != mForzaData.CarCategory){
                mCategory = mForzaData.CarCategory;
                mBinding.carCategory.setText(mapIntToCarCategory(mCategory));
            }
            if(mPower != mForzaData.Power){
                mPower= mForzaData.Power;
                if(mPower < 0.0) {
                    mPower = 0.0f;
                }
                mBinding.carPower.setText(String.format(Locale.CHINA,
                        getResources().getString(R.string.power_suffix),(mPower/1000f)));
            }
            if(mTorque != mForzaData.Torque){
                mTorque = mForzaData.Torque;
                mBinding.carTorque.setText(String.format(Locale.CHINA,
                        getResources().getString(R.string.torque_suffix),(int)mTorque));
            }
            if(mDriveTrainType != mForzaData.DrivetrainType){
                mDriveTrainType = mForzaData.DrivetrainType;
                mBinding.carDriveTrainType.setText(mapIntToDrivingType(mDriveTrainType));
            }
            if(mClutch != mForzaData.Clutch){
                mClutch = mForzaData.Clutch;
                if(mClutch >0){
                    mBinding.carClutch.setText(String.format(Locale.CHINA,"%.1f%%",mClutch*100f/255));
                    mBinding.carClutch.setTextColor(ContextCompat.getColor(this,R.color.turn_on));
                }else{
                    mBinding.carClutch.setText(String.format(Locale.CHINA,"%.1f%%",mClutch*100f/255));
                    mBinding.carClutch.setTextColor(ContextCompat.getColor(this,R.color.turn_off));
                }
            }
            if(mHandBrake != mForzaData.HandBrake){
                mHandBrake = mForzaData.HandBrake;
                if(mHandBrake >0){
                    mBinding.carHandbrake.setText(String.format(Locale.CHINA,"%.1f%%",mHandBrake*100f/255));
                    mBinding.carHandbrake.setTextColor(ContextCompat.getColor(this,R.color.turn_on));
                }else{
                    mBinding.carHandbrake.setText(String.format(Locale.CHINA,"%.1f%%",mHandBrake*100f/255));
                    mBinding.carHandbrake.setTextColor(ContextCompat.getColor(this,R.color.turn_off));
                }
            }
            if(mBrake != mForzaData.Brake){
                mBrake = mForzaData.Brake;
                if(mBrake >0){
                    mBinding.carBrake.setText(String.format(Locale.CHINA,"%.1f%%",mBrake*100f/255));
                    mBinding.carBrake.setTextColor(ContextCompat.getColor(this,R.color.turn_on));
                }else{
                    mBinding.carBrake.setText(String.format(Locale.CHINA,"%.1f%%",mBrake*100f/255));
                    mBinding.carBrake.setTextColor(ContextCompat.getColor(this,R.color.turn_off));
                }
            }
            if(mAccel != mForzaData.Accel){
                mAccel = mForzaData.Accel;
                if(mAccel >0){
                    mBinding.carAccel.setText(String.format(Locale.CHINA,"%.1f%%",mAccel*100f/255));
                    mBinding.carAccel.setTextColor(ContextCompat.getColor(this,R.color.turn_on));
                }else{
                    mBinding.carAccel.setText(String.format(Locale.CHINA,"%.1f%%",mAccel*100f/255));
                    mBinding.carAccel.setTextColor(ContextCompat.getColor(this,R.color.turn_off));
                }
            }
        }

        mBinding.forzaDashboard.update(mForzaData.Speed*3.6f,
                (int)mForzaData.Maxrpm,
                (int)mForzaData.Currentrpm,
                (int)mForzaData.Idlerpm,
                mForzaData.Gears,
                mForzaData.Accel,
                mForzaData.Brake,
                mForzaData.Steer);
        if(onListen){
            mLocalHandler.sendEmptyMessageDelayed(MSG_REFRESH_UI,16);
        }
    }

    private void updateCoverViewStat(){
        if(onListen){
            mBinding.viewCover.setBackgroundColor(0x00000000);
        } else {
            mBinding.viewCover.setBackgroundColor(ContextCompat.getColor(this,R.color.half_color_dark));
        }
    }

    String mapIntToCarClass(int carclass){
        switch (carclass){
            case 0:
                return "D";
            case 1:
                return "C";
            case 2:
                return "B";
            case 3:
                return "A";
            case 4:
                return "S1";
            case 5:
                return  "S2";
            case 6:
                return "X";
            default:
                return "D";
        }
    }

    String mapIntToCarCategory(int category){
        switch (category){
            case 11: return getResources().getString(R.string.car_category_11);
            case 12: return getResources().getString(R.string.car_category_12);
            case 13: return getResources().getString(R.string.car_category_13);
            case 14: return getResources().getString(R.string.car_category_14);
            case 16: return getResources().getString(R.string.car_category_16);
            case 17: return getResources().getString(R.string.car_category_17);
            case 18: return getResources().getString(R.string.car_category_18);
            case 19: return getResources().getString(R.string.car_category_19);
            case 20: return getResources().getString(R.string.car_category_20);
            case 21: return getResources().getString(R.string.car_category_21);
            case 22: return getResources().getString(R.string.car_category_22);
            case 23: return getResources().getString(R.string.car_category_23);
            case 24: return getResources().getString(R.string.car_category_24);
            case 25: return getResources().getString(R.string.car_category_25);
            case 26: return getResources().getString(R.string.car_category_26);
            case 28: return getResources().getString(R.string.car_category_28);
            case 29: return getResources().getString(R.string.car_category_29);
            case 30: return getResources().getString(R.string.car_category_30);
            case 31: return getResources().getString(R.string.car_category_31);
            case 32: return getResources().getString(R.string.car_category_32);
            case 33: return getResources().getString(R.string.car_category_33);
            case 34: return getResources().getString(R.string.car_category_34);
            case 35: return getResources().getString(R.string.car_category_35);
            case 36: return getResources().getString(R.string.car_category_36);
            case 37: return getResources().getString(R.string.car_category_37);
            case 38: return getResources().getString(R.string.car_category_38);
            case 39: return getResources().getString(R.string.car_category_39);
            case 40: return getResources().getString(R.string.car_category_40);
            case 41: return getResources().getString(R.string.car_category_41);
            case 42: return getResources().getString(R.string.car_category_42);
            case 43: return getResources().getString(R.string.car_category_43);
            case 44: return getResources().getString(R.string.car_category_44);
            case 45: return getResources().getString(R.string.car_category_45);
            case 46: return getResources().getString(R.string.car_category_46);
            case 47: return getResources().getString(R.string.car_category_47);
            case 48: return getResources().getString(R.string.car_category_48);
            case 49: return getResources().getString(R.string.car_category_49);
            default: return String.valueOf(category);
        }
    }

    String mapIntToDrivingType(int type){
        switch (type){
            case 0: return getResources().getString(R.string.drive_train_type_0);
            case 1: return getResources().getString(R.string.drive_train_type_1);
            case 2: return getResources().getString(R.string.drive_train_type_2);
            default: return String.valueOf(type);
        }
    }

    private static class LocalHandler extends Handler{
        WeakReference<MainActivity> activity;
        public LocalHandler(MainActivity activity){
            this.activity = new WeakReference<>(activity);
        }
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_REFRESH_UI:
                    if(activity.get() != null){
                        android.os.Trace.beginSection("refreshui");
                        activity.get().refreshUI();
                        android.os.Trace.endSection();
                    }
                    break;
                case MSG_START_RECV:
                case MSG_PAUSE_RECV:
                    if(activity.get() != null){
                        activity.get().updateCoverViewStat();
                    }
                    break;
            }
        }
    }
}