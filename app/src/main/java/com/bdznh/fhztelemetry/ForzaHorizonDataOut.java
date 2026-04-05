package com.bdznh.fhztelemetry;

import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ForzaHorizonDataOut {
    private static final String TAG = "ForzaHorizon";
    private static final int DATA_SIZE = 324;

    private OnDataOutCallback cb = null;
    ForzaHorizonData mForzaData;
    private DatagramSocket mSocket = null;
    private Thread mReceiveThread = null;
    private volatile boolean mRunning = false;
    private volatile boolean mPaused = false;

    // Cached subscription to avoid re-subscribing on pause/resume
    private int mSubscribedPort = 0;

    public ForzaHorizonDataOut(OnDataOutCallback callback) {
        cb = callback;
        mForzaData = new ForzaHorizonData();
        cb.onDataOut(mForzaData);
    }

    private void onReadForzaHorizonData(int CarId, int CarClass, int CarPerformanceIndex, int Category,
                                        int Steer, int Accel, int Brake, int Gears, int DrivetrainType,
                                        int Clutch, int HandBrake, int DrivingLine, int AiBrakeDifference,
                                        float Speed, float idlerpm, float maxrpm, float currentrpm,
                                        float Power, float Torque) {
        if (cb != null) {
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
            mForzaData.Idlerpm = idlerpm;
            mForzaData.Maxrpm = maxrpm;
            mForzaData.Currentrpm = currentrpm;
            mForzaData.Power = Power;
            mForzaData.Torque = Torque;
            cb.onDataOut(mForzaData);
        }
    }

    private void onStartForzaHorizonData() {
        if (cb != null) {
            cb.onStartForzaDataOut();
        }
    }

    private void onPauseForzaHorizonData() {
        if (cb != null) {
            cb.onPauseForzaDataOut();
        }
    }

    public long start(int port) {
        if (mRunning) {
            Log.e(TAG, "already running, don't start again");
            return -1;
        }
        mSubscribedPort = port;
        mRunning = true;
        mPaused = false;

        mReceiveThread = new Thread(() -> {
            Log.d(TAG, "UDP receiver thread started, port=" + port);
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(port);
                mSocket = socket;
                // Notify caller that socket is bound
                synchronized (this) {
                    notifyAll();
                }

                byte[] buffer = new byte[DATA_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, DATA_SIZE);
                ByteBuffer bb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);

                int onMenu = 0;
                ForzaHorizonDataOutFormat data = new ForzaHorizonDataOutFormat();

                while (mRunning) {
                    try {
                        packet.setLength(DATA_SIZE);
                        socket.receive(packet);
                    } catch (Exception e) {
                        if (mRunning) {
                            Log.e(TAG, "recvfrom error: " + e);
                        }
                        break;
                    }

                    int readLen = packet.getLength();
                    if (readLen != DATA_SIZE) {
                        Log.w(TAG, "expect " + DATA_SIZE + " but received " + readLen);
                        continue;
                    }

                    bb.rewind();
                    parseForzaData(bb, data);

                    if (onMenu != data.IsRaceOn) {
                        onMenu = data.IsRaceOn;
                        if (onMenu == 1) {
                            onStartForzaHorizonData();
                        } else {
                            onPauseForzaHorizonData();
                        }
                    }

                    if (!mPaused && data.IsRaceOn == 1) {
                        onReadForzaHorizonData(
                                data.CarOrdinal, data.CarClass, data.CarPerformanceIndex, data.CarCategory,
                                data.Steer, data.Accel & 0xFF, data.Brake & 0xFF, data.Gear, data.DrivetrainType,
                                data.Clutch & 0xFF, data.HandBrake & 0xFF, data.NormalizedDrivingLine & 0xFF, data.NormalizedAIBrakeDifference & 0xFF,
                                data.Speed, data.EngineIdleRpm, data.EngineMaxRpm, data.CurrentEngineRpm,
                                data.Power, data.Torque);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "UDP receiver error: " + e);
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                mSocket = null;
                Log.d(TAG, "UDP receiver thread ended");
            }
        }, "ForzaUDPReceiver");

        mReceiveThread.start();

        // Wait for socket to be bound (or error)
        synchronized (this) {
            try {
                wait(3000);
            } catch (InterruptedException ignored) {
            }
        }

        if (mSocket != null && mSocket.isBound()) {
            Log.d(TAG, "native Start port " + port + " success");
            return 0;
        } else {
            Log.e(TAG, "native Start port " + port + " failed");
            mRunning = false;
            if (mReceiveThread != null) {
                mReceiveThread.interrupt();
            }
            return -1;
        }
    }

    public void pause() {
        if (mRunning) {
            mPaused = !mPaused;
            Log.d(TAG, "native " + (mPaused ? "Pause" : "Resume"));
        } else {
            Log.e(TAG, "nativePause have not started");
        }
    }

    public long stop() {
        Log.d(TAG, "native Stop");
        if (mRunning) {
            mPaused = true;
            mRunning = false;
            if (mSocket != null && !mSocket.isClosed()) {
                mSocket.close();
            }
            if (mReceiveThread != null) {
                try {
                    mReceiveThread.join(1000);
                } catch (InterruptedException ignored) {
                }
                mReceiveThread = null;
            }
        }
        return 0;
    }

    public void release() {
        if (mRunning) {
            stop();
        }
        cb = null;
        Log.d(TAG, "native Release");
    }

    // -----------------------------------------------------------------------
    // Data parsing — matches ForzaHorizonDataOutFormat (324 bytes, LITTLE_ENDIAN)
    // -----------------------------------------------------------------------
    private static class ForzaHorizonDataOutFormat {
        int IsRaceOn;
        int TimestampMS;
        float EngineMaxRpm;
        float EngineIdleRpm;
        float CurrentEngineRpm;
        float AccelerationX;
        float AccelerationY;
        float AccelerationZ;
        float VelocityX;
        float VelocityY;
        float VelocityZ;
        float AngularVelocityX;
        float AngularVelocityY;
        float AngularVelocityZ;
        float Yaw;
        float Pitch;
        float Roll;
        float NormalizedSuspensionTravelFrontLeft;
        float NormalizedSuspensionTravelFrontRight;
        float NormalizedSuspensionTravelRearLeft;
        float NormalizedSuspensionTravelRearRight;
        float TireSlipRatioFrontLeft;
        float TireSlipRatioFrontRight;
        float TireSlipRatioRearLeft;
        float TireSlipRatioRearRight;
        float WheelRotationSpeedFrontLeft;
        float WheelRotationSpeedFrontRight;
        float WheelRotationSpeedRearLeft;
        float WheelRotationSpeedRearRight;
        int WheelOnRumbleStripFrontLeft;
        int WheelOnRumbleStripFrontRight;
        int WheelOnRumbleStripRearLeft;
        int WheelOnRumbleStripRearRight;
        float WheelInPuddleDepthFrontLeft;
        float WheelInPuddleDepthFrontRight;
        float WheelInPuddleDepthRearLeft;
        float WheelInPuddleDepthRearRight;
        float SurfaceRumbleFrontLeft;
        float SurfaceRumbleFrontRight;
        float SurfaceRumbleRearLeft;
        float SurfaceRumbleRearRight;
        float TireSlipAngleFrontLeft;
        float TireSlipAngleFrontRight;
        float TireSlipAngleRearLeft;
        float TireSlipAngleRearRight;
        float TireCombinedSlipFrontLeft;
        float TireCombinedSlipFrontRight;
        float TireCombinedSlipRearLeft;
        float TireCombinedSlipRearRight;
        float SuspensionTravelMetersFrontLeft;
        float SuspensionTravelMetersFrontRight;
        float SuspensionTravelMetersRearLeft;
        float SuspensionTravelMetersRearRight;
        int CarOrdinal;
        int CarClass;
        int CarPerformanceIndex;
        int DrivetrainType;
        int NumCylinders;
        int CarCategory;
        // byte[8] HorizonPlaceholder (skip)
        float PositionX;
        float PositionY;
        float PositionZ;
        float Speed;
        float Power;
        float Torque;
        float TireTempFrontLeft;
        float TireTempFrontRight;
        float TireTempRearLeft;
        float TireTempRearRight;
        float Boost;
        float Fuel;
        float DistanceTraveled;
        float BestLap;
        float LastLap;
        float CurrentLap;
        float CurrentRaceTime;
        int LapNumber;        // uint16_t
        int RacePosition;    // uint8_t
        int Accel;           // uint8_t
        int Brake;           // uint8_t
        int Clutch;          // uint8_t
        int HandBrake;       // uint8_t
        int Gear;            // int8_t
        int Steer;           // int8_t
        int NormalizedDrivingLine;       // int8_t
        int NormalizedAIBrakeDifference;  // int8_t
    }

    private void parseForzaData(ByteBuffer bb, ForzaHorizonDataOutFormat d) {
        d.IsRaceOn = bb.getInt();
        d.TimestampMS = bb.getInt();
        d.EngineMaxRpm = bb.getFloat();
        d.EngineIdleRpm = bb.getFloat();
        d.CurrentEngineRpm = bb.getFloat();
        d.AccelerationX = bb.getFloat();
        d.AccelerationY = bb.getFloat();
        d.AccelerationZ = bb.getFloat();
        d.VelocityX = bb.getFloat();
        d.VelocityY = bb.getFloat();
        d.VelocityZ = bb.getFloat();
        d.AngularVelocityX = bb.getFloat();
        d.AngularVelocityY = bb.getFloat();
        d.AngularVelocityZ = bb.getFloat();
        d.Yaw = bb.getFloat();
        d.Pitch = bb.getFloat();
        d.Roll = bb.getFloat();
        d.NormalizedSuspensionTravelFrontLeft = bb.getFloat();
        d.NormalizedSuspensionTravelFrontRight = bb.getFloat();
        d.NormalizedSuspensionTravelRearLeft = bb.getFloat();
        d.NormalizedSuspensionTravelRearRight = bb.getFloat();
        d.TireSlipRatioFrontLeft = bb.getFloat();
        d.TireSlipRatioFrontRight = bb.getFloat();
        d.TireSlipRatioRearLeft = bb.getFloat();
        d.TireSlipRatioRearRight = bb.getFloat();
        d.WheelRotationSpeedFrontLeft = bb.getFloat();
        d.WheelRotationSpeedFrontRight = bb.getFloat();
        d.WheelRotationSpeedRearLeft = bb.getFloat();
        d.WheelRotationSpeedRearRight = bb.getFloat();
        d.WheelOnRumbleStripFrontLeft = bb.getInt();
        d.WheelOnRumbleStripFrontRight = bb.getInt();
        d.WheelOnRumbleStripRearLeft = bb.getInt();
        d.WheelOnRumbleStripRearRight = bb.getInt();
        d.WheelInPuddleDepthFrontLeft = bb.getFloat();
        d.WheelInPuddleDepthFrontRight = bb.getFloat();
        d.WheelInPuddleDepthRearLeft = bb.getFloat();
        d.WheelInPuddleDepthRearRight = bb.getFloat();
        d.SurfaceRumbleFrontLeft = bb.getFloat();
        d.SurfaceRumbleFrontRight = bb.getFloat();
        d.SurfaceRumbleRearLeft = bb.getFloat();
        d.SurfaceRumbleRearRight = bb.getFloat();
        d.TireSlipAngleFrontLeft = bb.getFloat();
        d.TireSlipAngleFrontRight = bb.getFloat();
        d.TireSlipAngleRearLeft = bb.getFloat();
        d.TireSlipAngleRearRight = bb.getFloat();
        d.TireCombinedSlipFrontLeft = bb.getFloat();
        d.TireCombinedSlipFrontRight = bb.getFloat();
        d.TireCombinedSlipRearLeft = bb.getFloat();
        d.TireCombinedSlipRearRight = bb.getFloat();
        d.SuspensionTravelMetersFrontLeft = bb.getFloat();
        d.SuspensionTravelMetersFrontRight = bb.getFloat();
        d.SuspensionTravelMetersRearLeft = bb.getFloat();
        d.SuspensionTravelMetersRearRight = bb.getFloat();
        d.CarOrdinal = bb.getInt();
        d.CarClass = bb.getInt();
        d.CarPerformanceIndex = bb.getInt();
        d.DrivetrainType = bb.getInt();
        d.NumCylinders = bb.getInt();
        d.CarCategory = bb.getInt();
        // 8 bytes HorizonPlaceholder
        bb.position(bb.position() + 8);
        d.PositionX = bb.getFloat();
        d.PositionY = bb.getFloat();
        d.PositionZ = bb.getFloat();
        d.Speed = bb.getFloat();
        d.Power = bb.getFloat();
        d.Torque = bb.getFloat();
        d.TireTempFrontLeft = bb.getFloat();
        d.TireTempFrontRight = bb.getFloat();
        d.TireTempRearLeft = bb.getFloat();
        d.TireTempRearRight = bb.getFloat();
        d.Boost = bb.getFloat();
        d.Fuel = bb.getFloat();
        d.DistanceTraveled = bb.getFloat();
        d.BestLap = bb.getFloat();
        d.LastLap = bb.getFloat();
        d.CurrentLap = bb.getFloat();
        d.CurrentRaceTime = bb.getFloat();
        d.LapNumber = bb.getShort() & 0xFFFF;        // uint16_t
        d.RacePosition = bb.get() & 0xFF;             // uint8_t
        d.Accel = bb.get() & 0xFF;                   // uint8_t
        d.Brake = bb.get() & 0xFF;                   // uint8_t
        d.Clutch = bb.get() & 0xFF;                  // uint8_t
        d.HandBrake = bb.get() & 0xFF;                // uint8_t
        d.Gear = bb.get();                           // int8_t
        d.Steer = bb.get();                           // int8_t
        d.NormalizedDrivingLine = bb.get();           // int8_t
        d.NormalizedAIBrakeDifference = bb.get();     // int8_t
    }

    public interface OnDataOutCallback {
        void onDataOut(ForzaHorizonData data);
        void onStartForzaDataOut();
        void onPauseForzaDataOut();
    }
}
