package com.bdznh.fhztelemetry.data.parser;

import com.bdznh.fhztelemetry.data.model.ForzaHorizonData;
import java.nio.ByteBuffer;

/**
 * Parser for Forza Horizon UDP telemetry data (324 bytes, LITTLE_ENDIAN).
 *
 * The internal frame buffer ({@link DataFrame}) is a reusable singleton allocated once
 * per TelemetryUdpReceiver — NOT per packet. This eliminates ~60 object allocations/second
 * at 60fps and prevents GC pressure on the hot path.
 */
public class ForzaHorizonDataParser {

    public static final int DATA_SIZE = 324;

    /**
     * Internal 324-byte frame structure — allocated ONCE and reused for every packet.
     * Marked public so TelemetryUdpReceiver (a different package) can reuse the same instance.
     */
    public static final class DataFrame {
        // Fields accessed by TelemetryUdpReceiver (different package) — must be public
        public int IsRaceOn;
        // All other fields — package-private, used only by ForzaHorizonDataParser
        int TimestampMS;
        public float EngineMaxRpm;
        public float EngineIdleRpm;
        public float CurrentEngineRpm;
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
        public int CarOrdinal;
        public int CarClass;
        public int CarPerformanceIndex;
        public int DrivetrainType;
        int NumCylinders;
        public int CarCategory;
        float PositionX;
        float PositionY;
        float PositionZ;
        public float Speed;
        public float Power;
        public float Torque;
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
        int LapNumber;
        int RacePosition;
        public int Accel;
        public int Brake;
        public int Clutch;
        public int HandBrake;
        public int Gear;
        public int Steer;
        public int NormalizedDrivingLine;
        public int NormalizedAIBrakeDifference;
    }

    /**
     * Parse raw UDP bytes into the provided DataFrame (MUST be reused, never null).
     * @param buffer ByteBuffer positioned at start, LITTLE_ENDIAN order
     * @param frame  reusable DataFrame instance — caller allocates once and reuses
     */
    public void parse(ByteBuffer buffer, DataFrame frame) {
        frame.IsRaceOn          = buffer.getInt();
        frame.TimestampMS       = buffer.getInt();
        frame.EngineMaxRpm     = buffer.getFloat();
        frame.EngineIdleRpm    = buffer.getFloat();
        frame.CurrentEngineRpm = buffer.getFloat();
        frame.AccelerationX     = buffer.getFloat();
        frame.AccelerationY     = buffer.getFloat();
        frame.AccelerationZ     = buffer.getFloat();
        frame.VelocityX        = buffer.getFloat();
        frame.VelocityY        = buffer.getFloat();
        frame.VelocityZ        = buffer.getFloat();
        frame.AngularVelocityX = buffer.getFloat();
        frame.AngularVelocityY = buffer.getFloat();
        frame.AngularVelocityZ = buffer.getFloat();
        frame.Yaw               = buffer.getFloat();
        frame.Pitch             = buffer.getFloat();
        frame.Roll              = buffer.getFloat();
        frame.NormalizedSuspensionTravelFrontLeft  = buffer.getFloat();
        frame.NormalizedSuspensionTravelFrontRight = buffer.getFloat();
        frame.NormalizedSuspensionTravelRearLeft   = buffer.getFloat();
        frame.NormalizedSuspensionTravelRearRight  = buffer.getFloat();
        frame.TireSlipRatioFrontLeft  = buffer.getFloat();
        frame.TireSlipRatioFrontRight  = buffer.getFloat();
        frame.TireSlipRatioRearLeft    = buffer.getFloat();
        frame.TireSlipRatioRearRight   = buffer.getFloat();
        frame.WheelRotationSpeedFrontLeft  = buffer.getFloat();
        frame.WheelRotationSpeedFrontRight  = buffer.getFloat();
        frame.WheelRotationSpeedRearLeft    = buffer.getFloat();
        frame.WheelRotationSpeedRearRight   = buffer.getFloat();
        frame.WheelOnRumbleStripFrontLeft  = buffer.getInt();
        frame.WheelOnRumbleStripFrontRight  = buffer.getInt();
        frame.WheelOnRumbleStripRearLeft    = buffer.getInt();
        frame.WheelOnRumbleStripRearRight   = buffer.getInt();
        frame.WheelInPuddleDepthFrontLeft  = buffer.getFloat();
        frame.WheelInPuddleDepthFrontRight = buffer.getFloat();
        frame.WheelInPuddleDepthRearLeft   = buffer.getFloat();
        frame.WheelInPuddleDepthRearRight  = buffer.getFloat();
        frame.SurfaceRumbleFrontLeft  = buffer.getFloat();
        frame.SurfaceRumbleFrontRight = buffer.getFloat();
        frame.SurfaceRumbleRearLeft   = buffer.getFloat();
        frame.SurfaceRumbleRearRight  = buffer.getFloat();
        frame.TireSlipAngleFrontLeft  = buffer.getFloat();
        frame.TireSlipAngleFrontRight = buffer.getFloat();
        frame.TireSlipAngleRearLeft    = buffer.getFloat();
        frame.TireSlipAngleRearRight   = buffer.getFloat();
        frame.TireCombinedSlipFrontLeft  = buffer.getFloat();
        frame.TireCombinedSlipFrontRight = buffer.getFloat();
        frame.TireCombinedSlipRearLeft    = buffer.getFloat();
        frame.TireCombinedSlipRearRight   = buffer.getFloat();
        frame.SuspensionTravelMetersFrontLeft  = buffer.getFloat();
        frame.SuspensionTravelMetersFrontRight = buffer.getFloat();
        frame.SuspensionTravelMetersRearLeft   = buffer.getFloat();
        frame.SuspensionTravelMetersRearRight  = buffer.getFloat();
        frame.CarOrdinal       = buffer.getInt();
        frame.CarClass         = buffer.getInt();
        frame.CarPerformanceIndex = buffer.getInt();
        frame.DrivetrainType   = buffer.getInt();
        frame.NumCylinders     = buffer.getInt();
        frame.CarCategory      = buffer.getInt();
        // HorizonPlaceholder[8]
        buffer.position(buffer.position() + 8);
        frame.PositionX = buffer.getFloat();
        frame.PositionY = buffer.getFloat();
        frame.PositionZ = buffer.getFloat();
        frame.Speed     = buffer.getFloat();
        frame.Power     = buffer.getFloat();
        frame.Torque    = buffer.getFloat();
        frame.TireTempFrontLeft  = buffer.getFloat();
        frame.TireTempFrontRight = buffer.getFloat();
        frame.TireTempRearLeft    = buffer.getFloat();
        frame.TireTempRearRight   = buffer.getFloat();
        frame.Boost      = buffer.getFloat();
        frame.Fuel       = buffer.getFloat();
        frame.DistanceTraveled = buffer.getFloat();
        frame.BestLap    = buffer.getFloat();
        frame.LastLap    = buffer.getFloat();
        frame.CurrentLap = buffer.getFloat();
        frame.CurrentRaceTime = buffer.getFloat();
        frame.LapNumber       = buffer.getShort() & 0xFFFF;
        frame.RacePosition    = buffer.get() & 0xFF;
        frame.Accel           = buffer.get() & 0xFF;
        frame.Brake           = buffer.get() & 0xFF;
        frame.Clutch          = buffer.get() & 0xFF;
        frame.HandBrake       = buffer.get() & 0xFF;
        frame.Gear            = buffer.get();
        frame.Steer           = buffer.get();
        frame.NormalizedDrivingLine       = buffer.get();
        frame.NormalizedAIBrakeDifference  = buffer.get();
    }

    /**
     * Copy parsed frame data into the public data model (reused, no allocation).
     * @param frame parsed internal frame
     * @param out   output ForzaHorizonData (caller reuses a single instance)
     */
    public void fillModel(DataFrame frame, ForzaHorizonData out) {
        out.CarId              = frame.CarOrdinal;
        out.CarClass           = frame.CarClass;
        out.CarPerformanceIndex = frame.CarPerformanceIndex;
        out.CarCategory        = frame.CarCategory;
        out.Steer              = frame.Steer;
        out.Accel              = frame.Accel;
        out.Brake              = frame.Brake;
        out.Gears              = frame.Gear;
        out.DrivetrainType     = frame.DrivetrainType;
        out.Clutch             = frame.Clutch;
        out.HandBrake          = frame.HandBrake;
        out.DrivingLine        = frame.NormalizedDrivingLine;
        out.ABS                = frame.NormalizedAIBrakeDifference;
        out.Speed              = frame.Speed;
        out.Idlerpm            = frame.EngineIdleRpm;
        out.Maxrpm             = frame.EngineMaxRpm;
        out.Currentrpm         = frame.CurrentEngineRpm;
        out.Power              = frame.Power;
        out.Torque             = frame.Torque;
    }
}
