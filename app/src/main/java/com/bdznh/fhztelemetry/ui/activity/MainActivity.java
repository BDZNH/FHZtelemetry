package com.bdznh.fhztelemetry.ui.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bdznh.fhztelemetry.R;
import com.bdznh.fhztelemetry.callback.TelemetryCallback;
import com.bdznh.fhztelemetry.data.model.ForzaHorizonData;
import com.bdznh.fhztelemetry.databinding.ActivityMainBinding;
import com.bdznh.fhztelemetry.network.TelemetryUdpReceiver;
import com.bdznh.fhztelemetry.ui.widget.ForzaHorizonDashboard;
import com.bdznh.fhztelemetry.util.Constant;
import com.bdznh.fhztelemetry.util.TraceHelper;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TelemetryCallback {

    private TelemetryUdpReceiver forza = null;
    private ActivityMainBinding mBinding;

    int mCarID;
    int mCarClass;
    int mCarPerformanceIndex;
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
    int mCurrentPort = Constant.DEFAULT_PORT;

    private SharedPreferences mPrefs;

    LocalHandler mLocalHandler;

    boolean isVisible = false;

    boolean isStarted = false;
    boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());

        // Load saved port
        mPrefs = getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE);
        mCurrentPort = mPrefs.getInt(Constant.PREF_UDP_PORT, Constant.DEFAULT_PORT);

        initViewState();
        setSystemUIVisibility(false);
        isVisible = false;
        mLocalHandler = new LocalHandler(getMainLooper(), this);
        forza = new TelemetryUdpReceiver(this);
        mBinding.forzaDashboard.update(100f, 9999, 2300, 888, 3, 180, 120, -100);

        // Check for auto-start parameter or action
        boolean shouldAutoStart = false;
        if (getIntent().hasExtra(Constant.EXTRA_AUTO_START)) {
            int autoStart = getIntent().getIntExtra(Constant.EXTRA_AUTO_START, 0);
            shouldAutoStart = (autoStart == 1);
        } else if (getIntent().getAction() != null
                && getIntent().getAction().equals("com.bdznh.fhztelemetry.ACTION_AUTO_START")) {
            shouldAutoStart = true;
        }

        if (shouldAutoStart && !isStarted) {
            long ret = forza.start(mCurrentPort);
            if (ret == 0) {
                isStarted = true;
                mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.pause_green));
                mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop_white));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Check for auto-start parameter
        boolean shouldAutoStart = false;
        if (intent.hasExtra(Constant.EXTRA_AUTO_START)) {
            int autoStart = intent.getIntExtra(Constant.EXTRA_AUTO_START, 0);
            shouldAutoStart = (autoStart == 1);
        } else if (intent.getAction() != null
                && intent.getAction().equals("com.bdznh.fhztelemetry.ACTION_AUTO_START")) {
            shouldAutoStart = true;
        }

        if (shouldAutoStart && !isStarted) {
            long ret = forza.start(mCurrentPort);
            if (ret == 0) {
                isStarted = true;
                mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.pause_green));
                mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop_white));
            }
        }
    }

    void initViewState() {
        mBinding.startpause.setOnClickListener(v -> {
            if (isStarted) {
                isPaused = !isPaused;
                forza.pause();
                if (isPaused) {
                    mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.start_green));
                } else {
                    mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.pause_green));
                }
            } else {
                long ret = forza.start(mCurrentPort);
                if (ret == 0) {
                    isStarted = true;
                    mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.pause_green));
                    mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop_white));
                }
            }
        });
        mBinding.stop.setOnClickListener(v -> {
            if (isStarted) {
                long ret = forza.stop();
                if (ret == 0) {
                    isStarted = false;
                    mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop_gray));
                    mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.start_white));
                }
            }
        });
        mBinding.settings.setOnClickListener(v -> showPortSettingsDialog());
        mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop_gray));
        mBinding.settings.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_settings));
        mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.start_white));
        mBinding.carInfo.setOnClickListener(v -> {
            if (mBinding.carInfo.getVisibility() == View.VISIBLE) {
                mBinding.carInfo.setVisibility(View.GONE);
            }
        });
        mBinding.forzaDashboard.setOnClickListener(v -> {
            isVisible = !isVisible;
            setSystemUIVisibility(isVisible);
            if (!isVisible) {
                mLocalHandler.removeMessages(Constant.MSG_AUTO_FULLSCREEN);
                mLocalHandler.sendEmptyMessageDelayed(Constant.MSG_AUTO_FULLSCREEN, Constant.AUTO_FULLSCREEN_DELAY_MS);
            } else {
                mLocalHandler.removeMessages(Constant.MSG_AUTO_FULLSCREEN);
            }
        });
    }

    private void showPortSettingsDialog() {
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_port_settings, null);
        EditText portInput = dialogView.findViewById(R.id.port_input);
        portInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        portInput.setText(String.valueOf(mCurrentPort));
        portInput.selectAll();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.udp_port_settings_title);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String portStr = portInput.getText().toString().trim();
            if (!isValidPort(portStr)) {
                portInput.setError(getString(R.string.port_invalid));
                return;
            }
            int newPort = Integer.parseInt(portStr);
            if (newPort < 1024 || newPort > 65535) {
                portInput.setError(getString(R.string.port_out_of_range));
                return;
            }
            if (newPort != mCurrentPort) {
                mCurrentPort = newPort;
                // Save to SharedPreferences
                mPrefs.edit().putInt(Constant.PREF_UDP_PORT, mCurrentPort).apply();
                if (isStarted) {
                    forza.stop();
                    isStarted = false;
                    long ret = forza.start(mCurrentPort);
                    if (ret == 0) {
                        isStarted = true;
                        mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop_white));
                        mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.pause_green));
                    } else {
                        Toast.makeText(this, "Failed to restart with new port", Toast.LENGTH_SHORT).show();
                        mBinding.stop.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.stop_gray));
                        mBinding.startpause.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.start_white));
                    }
                }
            }
            dialog.dismiss();
        });
    }

    private boolean isValidPort(String portStr) {
        if (portStr == null || portStr.isEmpty()) {
            return false;
        }
        try {
            int port = Integer.parseInt(portStr);
            return port > 0 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setSystemUIVisibility(boolean visible) {
        if (visible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            if (mBinding.carInfo.getVisibility() != View.VISIBLE) {
                mBinding.carInfo.setVisibility(View.VISIBLE);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
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
        if (isStarted) {
            forza.stop();
        }
        forza.release();
        forza = null;
        super.onDestroy();
    }

    @Override
    public void onTelemetryDataUpdate(ForzaHorizonData data) {
        if (mForzaData == null) {
            mForzaData = data;
        }
    }

    @Override
    public void onRaceStarted() {
        onListen = true;
        mLocalHandler.sendEmptyMessage(Constant.MSG_START_RECV);
        mLocalHandler.sendEmptyMessage(Constant.MSG_REFRESH_UI);
    }

    @Override
    public void onRaceEnded() {
        onListen = false;
        mLocalHandler.removeMessages(Constant.MSG_REFRESH_UI);
        mLocalHandler.sendEmptyMessage(Constant.MSG_PAUSE_RECV);
    }

    @SuppressLint("DefaultLocale")
    public void refreshUI() {
        if (mForzaData == null) {
            return;
        }
        if (mBinding.carInfo.getVisibility() == View.VISIBLE) {
            if (mCarClass != mForzaData.CarClass) {
                mCarClass = mForzaData.CarClass;
                mBinding.carclass.setText(Constant.mapIntToCarClass(mCarClass, this));
            }
            if (mCarID != mForzaData.CarId) {
                mCarID = mForzaData.CarId;
                mBinding.carid.setText(String.valueOf(mCarID));
            }
            if (mCarPerformanceIndex != mForzaData.CarPerformanceIndex) {
                mCarPerformanceIndex = mForzaData.CarPerformanceIndex;
                mBinding.carPerform.setText(String.valueOf(mCarPerformanceIndex));
            }
            if (mCategory != mForzaData.CarCategory) {
                mCategory = mForzaData.CarCategory;
                mBinding.carCategory.setText(Constant.mapIntToCarCategory(mCategory, this));
            }
            if (mPower != mForzaData.Power) {
                mPower = mForzaData.Power;
                if (mPower < 0.0) {
                    mPower = 0.0f;
                }
                mBinding.carPower.setText(String.format(Locale.CHINA,
                        getResources().getString(R.string.power_suffix), (mPower / 1000f)));
            }
            if (mTorque != mForzaData.Torque) {
                mTorque = mForzaData.Torque;
                mBinding.carTorque.setText(String.format(Locale.CHINA,
                        getResources().getString(R.string.torque_suffix), (int) mTorque));
            }
            if (mDriveTrainType != mForzaData.DrivetrainType) {
                mDriveTrainType = mForzaData.DrivetrainType;
                mBinding.carDriveTrainType.setText(Constant.mapIntToDrivingType(mDriveTrainType, this));
            }
            if (mClutch != mForzaData.Clutch) {
                mClutch = mForzaData.Clutch;
                if (mClutch > 0) {
                    mBinding.carClutch.setText(String.format(Locale.CHINA, "%.1f%%", mClutch * 100f / 255));
                    mBinding.carClutch.setTextColor(ContextCompat.getColor(this, R.color.turn_on));
                } else {
                    mBinding.carClutch.setText(String.format(Locale.CHINA, "%.1f%%", mClutch * 100f / 255));
                    mBinding.carClutch.setTextColor(ContextCompat.getColor(this, R.color.turn_off));
                }
            }
            if (mHandBrake != mForzaData.HandBrake) {
                mHandBrake = mForzaData.HandBrake;
                if (mHandBrake > 0) {
                    mBinding.carHandbrake.setText(String.format(Locale.CHINA, "%.1f%%", mHandBrake * 100f / 255));
                    mBinding.carHandbrake.setTextColor(ContextCompat.getColor(this, R.color.turn_on));
                } else {
                    mBinding.carHandbrake.setText(String.format(Locale.CHINA, "%.1f%%", mHandBrake * 100f / 255));
                    mBinding.carHandbrake.setTextColor(ContextCompat.getColor(this, R.color.turn_off));
                }
            }
            if (mBrake != mForzaData.Brake) {
                mBrake = mForzaData.Brake;
                if (mBrake > 0) {
                    mBinding.carBrake.setText(String.format(Locale.CHINA, "%.1f%%", mBrake * 100f / 255));
                    mBinding.carBrake.setTextColor(ContextCompat.getColor(this, R.color.turn_on));
                } else {
                    mBinding.carBrake.setText(String.format(Locale.CHINA, "%.1f%%", mBrake * 100f / 255));
                    mBinding.carBrake.setTextColor(ContextCompat.getColor(this, R.color.turn_off));
                }
            }
            if (mAccel != mForzaData.Accel) {
                mAccel = mForzaData.Accel;
                if (mAccel > 0) {
                    mBinding.carAccel.setText(String.format(Locale.CHINA, "%.1f%%", mAccel * 100f / 255));
                    mBinding.carAccel.setTextColor(ContextCompat.getColor(this, R.color.turn_on));
                } else {
                    mBinding.carAccel.setText(String.format(Locale.CHINA, "%.1f%%", mAccel * 100f / 255));
                    mBinding.carAccel.setTextColor(ContextCompat.getColor(this, R.color.turn_off));
                }
            }
        }

        mBinding.forzaDashboard.update(mForzaData.Speed * 3.6f,
                (int) mForzaData.Maxrpm,
                (int) mForzaData.Currentrpm,
                (int) mForzaData.Idlerpm,
                mForzaData.Gears,
                mForzaData.Accel,
                mForzaData.Brake,
                mForzaData.Steer);
        if (onListen) {
            mLocalHandler.sendEmptyMessageDelayed(Constant.MSG_REFRESH_UI, 16);
        }
    }

    private void updateCoverViewStat() {
        if (onListen) {
            mBinding.viewCover.setBackgroundColor(0x00000000);
        } else {
            mBinding.viewCover.setBackgroundColor(ContextCompat.getColor(this, R.color.half_color_dark));
        }
    }

    private static class LocalHandler extends Handler {
        private final WeakReference<MainActivity> activity;

        public LocalHandler(Looper looper, MainActivity activity) {
            super(looper);
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_REFRESH_UI:
                    if (activity.get() != null) {
                        TraceHelper.beginSection("refreshui");
                        activity.get().refreshUI();
                        TraceHelper.endSection();
                    }
                    break;
                case Constant.MSG_START_RECV:
                case Constant.MSG_PAUSE_RECV:
                    if (activity.get() != null) {
                        activity.get().updateCoverViewStat();
                    }
                    break;
                case Constant.MSG_AUTO_FULLSCREEN:
                    if (activity.get() != null) {
                        if (!activity.get().isVisible) {
                            activity.get().isVisible = true;
                            activity.get().setSystemUIVisibility(true);
                        }
                    }
                    break;
            }
        }
    }
}
