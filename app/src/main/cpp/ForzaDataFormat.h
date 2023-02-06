//
// Created by Dell3060 on 2022/11/25.
//

#ifndef MY_APPLICATION_FORZADATAFORMAT_H
#define MY_APPLICATION_FORZADATAFORMAT_H
#include <stdint.h>

typedef float f32;
typedef int32_t s32;
typedef uint32_t u32;
typedef uint16_t u16;
typedef uint8_t u8;
typedef int8_t s8;

struct ForzaHorizonDataOutFormat{
    s32 IsRaceOn; // = 1 when race is on. = 0 when in menus/race stopped …
    u32 TimestampMS; //Can overflow to 0 eventually
    f32 EngineMaxRpm;  //最大引擎转速
    f32 EngineIdleRpm;  //发动机怠速转速
    f32 CurrentEngineRpm; //当前引擎转速
    f32 AccelerationX; //加速度In the car's local space; X = right, Y = up, Z = forward
    f32 AccelerationY;
    f32 AccelerationZ;
    f32 VelocityX; //速度In the car's local space; X = right, Y = up, Z = forward
    f32 VelocityY;
    f32 VelocityZ;
    f32 AngularVelocityX; //角速度In the car's local space; X = pitch, Y = yaw, Z = roll
    f32 AngularVelocityY;
    f32 AngularVelocityZ;
    f32 Yaw;  //偏航角
    f32 Pitch;  //俯仰角
    f32 Roll;  //翻滚角
    f32 NormalizedSuspensionTravelFrontLeft; // 悬架行程：Suspension travel normalized: 0.0f = max stretch; 1.0 = max compression
    f32 NormalizedSuspensionTravelFrontRight;
    f32 NormalizedSuspensionTravelRearLeft;
    f32 NormalizedSuspensionTravelRearRight;
    f32 TireSlipRatioFrontLeft; // 轮胎滑移率Tire normalized slip ratio, = 0 means 100% grip and |ratio| > 1.0 means loss of grip.
    f32 TireSlipRatioFrontRight;
    f32 TireSlipRatioRearLeft;
    f32 TireSlipRatioRearRight;
    f32 WheelRotationSpeedFrontLeft; // 车轮转速Wheel rotation speed radians/sec.
    f32 WheelRotationSpeedFrontRight;
    f32 WheelRotationSpeedRearLeft;
    f32 WheelRotationSpeedRearRight;
    s32 WheelOnRumbleStripFrontLeft; // = 1 when wheel is on rumble strip, = 0 when off.
    s32 WheelOnRumbleStripFrontRight;
    s32 WheelOnRumbleStripRearLeft;
    s32 WheelOnRumbleStripRearRight;
    f32 WheelInPuddleDepthFrontLeft; // = from 0 to 1, where 1 is the deepest puddle
    f32 WheelInPuddleDepthFrontRight;
    f32 WheelInPuddleDepthRearLeft;
    f32 WheelInPuddleDepthRearRight;
    f32 SurfaceRumbleFrontLeft; // Non-dimensional surface rumble values passed to controller force feedback
    f32 SurfaceRumbleFrontRight;
    f32 SurfaceRumbleRearLeft;
    f32 SurfaceRumbleRearRight;
    f32 TireSlipAngleFrontLeft; // Tire normalized slip angle, = 0 means 100% grip and |angle| > 1.0 means loss of grip.
    f32 TireSlipAngleFrontRight;
    f32 TireSlipAngleRearLeft;
    f32 TireSlipAngleRearRight;
    f32 TireCombinedSlipFrontLeft; // Tire normalized combined slip, = 0 means 100% grip and |slip| > 1.0 means loss of grip.
    f32 TireCombinedSlipFrontRight;
    f32 TireCombinedSlipRearLeft;
    f32 TireCombinedSlipRearRight;
    f32 SuspensionTravelMetersFrontLeft; // 实际悬架行程(米)Actual suspension travel in meters
    f32 SuspensionTravelMetersFrontRight;
    f32 SuspensionTravelMetersRearLeft;
    f32 SuspensionTravelMetersRearRight;
    s32 CarOrdinal; //Unique ID of the car make/model 车辆代号
    s32 CarClass; //Between 0 (D -- worst cars) and 7 (X class -- best cars) inclusive
    s32 CarPerformanceIndex; //Between 100 (slowest car) and 999 (fastest car) inclusive
    s32 DrivetrainType; //Corresponds to EDrivetrainType; 0 = FWD(全时4驱), 1 = RWD（落锤式弯沉仪）, 2 = AWD（后轮驱动）
    s32 NumCylinders; //气缸数Number of cylinders in the engine
    s32 CarCategory; //
    u8 HorizonPlaceholder[8]; // unknown FH4 values
    f32 PositionX; //车辆位置
    f32 PositionY;
    f32 PositionZ;
    f32 Speed; // 速度 m/s meters per second
    f32 Power; // 功率 瓦特watts
    f32 Torque; // 扭矩 newton meter
    f32 TireTempFrontLeft; //胎温
    f32 TireTempFrontRight;
    f32 TireTempRearLeft;
    f32 TireTempRearRight;
    f32 Boost;
    f32 Fuel; //油门
    f32 DistanceTraveled; //比赛中的行驶距离
    f32 BestLap; //最佳圈速
    f32 LastLap; //上一圈速
    f32 CurrentLap; //当前圈速
    f32 CurrentRaceTime; //当前比赛时间
    u16 LapNumber; //圈数
    u8 RacePosition; //比赛排名
    u8 Accel; //加速
    u8 Brake; //刹车
    u8 Clutch; //离合 0没有踩离合，FF 踩下离合
    u8 HandBrake; //手刹 0 没有拉手刹，FF 拉下手刹
    u8 Gear; //挡位
    s8 Steer; //驾驶
    s8 NormalizedDrivingLine; //驾驶辅助线
    s8 NormalizedAIBrakeDifference; //AI制动差
};
#endif //MY_APPLICATION_FORZADATAFORMAT_H
