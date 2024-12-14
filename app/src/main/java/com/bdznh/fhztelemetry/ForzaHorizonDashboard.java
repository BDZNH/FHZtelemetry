package com.bdznh.fhztelemetry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bdznh.fhztelemetry.R;

import java.util.Locale;

public class ForzaHorizonDashboard extends View {
    private final String TAG = "ForzaHorizon";
    boolean onMeasured = false;

    int mSpeed = 0;
    Rect mRectFSpeed;
    float mSpeedTextSize = sp2px(120);
    int mSpeedTextColor = Color.WHITE;
    LocalPorint mSpeedTextPoint = new LocalPorint();
    LocalPorint mSpeedPoint = new LocalPorint();

    Context mContext;

    int mBackGroundWidth = 100;

    Paint mPaint;
    LocalPorint mCenter = new LocalPorint();
    LocalPorint mLeftTop = new LocalPorint();
    LocalPorint mRightBottom = new LocalPorint();

    //引擎转速指针的起始和截止坐标
    LocalPorint mIndicatorStart = new LocalPorint();
    LocalPorint mIndicatorEnd = new LocalPorint();

    float mRadius = 10000f;

    final float mSwipAngle = 255;
    final float mMiddleAngle = -90;
    final float mStartAngle = mMiddleAngle-(mSwipAngle/2);


    //展示数据 引擎
    int mEngineMaxRPM = 5000;
    int mEngineCurrentRPM = 0;
    int mEngineIdleRPM = 0;
    float mEngineArcRadius = 1000f;
    float mEngineArgRadiusStrokeSize = 5f;
    LocalPorint mEngineStartPoint = new LocalPorint();
    LocalPorint mEngineStopPoint = new LocalPorint();
    int mEngineCircleColor = 0x80FFFFFF;
    Rect mRectEngineMark;

    //挡位
    int mGears = 0;
    float mGearsTextSize = sp2px(60);
    int mGearsIdleTextColor = Color.WHITE;
    int mGearsSteerTextColor = 0xFF18ee88;
    int mGearsMaxTextColor = 0xFFea1f54;
    float mGearsCircleRadius = 70f;
    float mGearsCircleStrokeSize = 5f;
    Rect mRectGears = new Rect();
    String mGearsTextString;
    LocalPorint mGearsPoint = new LocalPorint();
    Paint mGearsTextPaint;

    //油门
    float mAccelsStrokeSize = 20f;
    float mAccelsCircleRadius = 300f;
    int mAccelsColor = 0xFF3ddc84;
    int mAccels = 0;

    //刹车
    float mBrakeStrokeSize = 20f;
    float mBrakeCircleRadius = 275f;
    int mBrakeColor = 0xFFea1f54;
    int mBrake = 0;

    //方向盘
    int mSteer = 0;
    int mRealSteer = 0;
    int mSpaceSteer=0;
    long mLastUpdateSteerTime=0;
    float mMaxSteer = 127;
    long mMaxAdjustTime = 500;//ms
    float mMaxSteerRotateAngle = 90;
    float mSteerExternalCircleRaius = 170f;
    float mSteerExternalCircleStrokeSize = 10f;
    float mSteerInternalCircleRadius = 100f;
    float mSteerInternalCircleStrokeSize = 10f;
    float mSteerLinkLinkeSize = 5f;
    int mSteerColor = 0xFFFFFFFF;
    LocalPorint mSteerLine1StartPoint = new LocalPorint();
    LocalPorint mSteerLine1StopPoint = new LocalPorint();
    LocalPorint mSteerLine2StartPoint = new LocalPorint();
    LocalPorint mSteerLine2StopPoint = new LocalPorint();
    LocalPorint mSteerLine3StartPoint = new LocalPorint();
    LocalPorint mSteerLine3StopPoint = new LocalPorint();

    Typeface mFontTypeFace;
    public ForzaHorizonDashboard(Context context) {
        super(context,null);
    }

    public ForzaHorizonDashboard(Context context, AttributeSet attrs){
        this(context,attrs,0);
    }

    public ForzaHorizonDashboard(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        this(context,attrs,defStyleAttr,0);
    }

    public ForzaHorizonDashboard(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context,attrs,defStyleAttr,defStyleRes);
        mContext = context;
        mFontTypeFace = Typeface.createFromAsset(mContext.getAssets(),"fonts/DS-DIGI.TTF");
        mPaint = new Paint();
        mGearsTextPaint = new Paint();
        mRectFSpeed = new Rect();
        mRectEngineMark = new Rect();
        initAttrs(context,attrs);
    }

    private void initAttrs(Context context,AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.ForzaHorizonDashboard);
        mEngineArcRadius = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_engine_arc_radius,mRadius);
        Log.d(TAG,"mRadius " + mRadius + " mEngineArcRadius " + mEngineArcRadius);
        mEngineArgRadiusStrokeSize = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_engine_arc_size,mEngineArgRadiusStrokeSize);
        mEngineCircleColor = typedArray.getColor(R.styleable.ForzaHorizonDashboard_engine_arc_color,mEngineCircleColor);

        mSpeedTextSize = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_speed_text_size,mSpeedTextSize);
        mSpeedTextColor = typedArray.getColor(R.styleable.ForzaHorizonDashboard_speed_text_color,mSpeedTextColor);

        mGearsIdleTextColor = typedArray.getColor(R.styleable.ForzaHorizonDashboard_idle_gears_text_color,mGearsIdleTextColor);
        mGearsSteerTextColor = typedArray.getColor(R.styleable.ForzaHorizonDashboard_steer_gears_text_color,mGearsSteerTextColor);
        mGearsMaxTextColor = typedArray.getColor(R.styleable.ForzaHorizonDashboard_max_gears_text_color,Color.RED);
        mGearsCircleRadius = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_gears_circle_radius,mGearsCircleRadius);
        mGearsCircleStrokeSize = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_gears_circle_stroke_size,mGearsCircleStrokeSize);
        mGearsTextSize = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_gears_text_size,mGearsTextSize);

        mAccelsStrokeSize = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_accels_size,mAccelsStrokeSize);
        mAccelsCircleRadius = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_accels_radius,mAccelsCircleRadius);
        mAccelsColor = typedArray.getColor(R.styleable.ForzaHorizonDashboard_accels_color,mAccelsColor);

        mBrakeStrokeSize = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_brake_size,mBrakeStrokeSize);
        mBrakeCircleRadius = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_brake_radius,mBrakeCircleRadius);
        mBrakeColor = typedArray.getColor(R.styleable.ForzaHorizonDashboard_brake_color,mBrakeColor);

        mSteerExternalCircleRaius = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_steer_external_circle_radius,mSteerExternalCircleRaius);;
        mSteerExternalCircleStrokeSize = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_steer_external_stroke_size,mSteerExternalCircleStrokeSize);;
        mSteerInternalCircleRadius = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_steer_internal_circle_raidus,mSteerInternalCircleRadius);;
        mSteerInternalCircleStrokeSize = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_steer_internal_stroke_size,mSteerInternalCircleStrokeSize);;
        mSteerLinkLinkeSize = typedArray.getDimension(R.styleable.ForzaHorizonDashboard_steer_link_line_size,mSteerLinkLinkeSize);;
        mSteerColor = typedArray.getColor(R.styleable.ForzaHorizonDashboard_steer_color,mSteerColor);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        onMeasured  = true;
        Log.d(TAG,"onMeasure called");
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthMode == MeasureSpec.AT_MOST){
            Log.e(TAG,"LayoutA onMeasure() called,widthMode = MeasureSpec.AT_MOST,widthSize = " + widthSize + " ,heightSize = " + heightSize);
        }

        if(widthMode == MeasureSpec.EXACTLY){
            Log.e(TAG,"LayoutA onMeasure() called,widthMode = MeasureSpec.EXACTLY,widthSize = " + widthSize + " ,heightSize = " + heightSize);
        }

        if(widthMode == MeasureSpec.UNSPECIFIED){
            Log.e(TAG,"LayoutA onMeasure() called,widthMode = MeasureSpec.UNSPECIFIED,widthSize = " + widthSize + " ,heightSize = " + heightSize);
        }

        if(heightMode == MeasureSpec.AT_MOST){
            Log.e(TAG,"LayoutA onMeasure() called,heightMode = MeasureSpec.AT_MOST,widthSize = " + widthSize + " ,heightSize = " + heightSize);
        }

        if(heightMode == MeasureSpec.EXACTLY){
            Log.e(TAG,"LayoutA onMeasure() called,heightMode = MeasureSpec.EXACTLY,widthSize = " + widthSize + " ,heightSize = " + heightSize);
        }

        if(heightMode == MeasureSpec.UNSPECIFIED){
            Log.e(TAG,"LayoutA onMeasure() called,heightMode = MeasureSpec.UNSPECIFIED,widthSize = " + widthSize + " ,heightSize = " + heightSize);
        }

        mBackGroundWidth = Math.min(widthSize,heightSize);
        mRadius = mBackGroundWidth/2f - 100;
        mCenter.X = widthSize/2f;
        mCenter.Y = heightSize/2f;
        Log.d(TAG,"center " + mCenter);
        mLeftTop.X = mCenter.X - mRadius;
        mLeftTop.Y = mCenter.Y - mRadius;
        mRightBottom.X = mCenter.X + mRadius;
        mRightBottom.Y = mCenter.Y + mRadius;
        Log.d(TAG,"mRadius " + mRadius + " mEngineArcRadius " + mEngineArcRadius);
        mEngineArcRadius = Math.min(mRadius,mEngineArcRadius);
        mRadius = mEngineArcRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int sections=mapEngineSectionCount(mEngineMaxRPM);
        mEngineMaxRPM = 1000*sections;
        mPaint.setTypeface(Typeface.MONOSPACE);
        //最外侧的引擎转速圆弧
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mEngineArgRadiusStrokeSize);
        mPaint.setColor(mEngineCircleColor);
        drawArc(canvas,mPaint,mCenter,mEngineArcRadius,mStartAngle,mStartAngle+(mSwipAngle/sections)*(sections-1));
        mPaint.setColor(mGearsMaxTextColor);
        drawArc(canvas,mPaint,mCenter,mEngineArcRadius,mStartAngle+(mSwipAngle/sections)*(sections-1),mStartAngle+mSwipAngle);

        //引擎转速刻度
        if(mEngineMaxRPM > 0){

            float angleStep = mSwipAngle/sections;
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mEngineCircleColor);
            mPaint.setTextSize(40);
            for(int i=0;i<=sections;i++){
                if(i>=sections-1){
                    mPaint.setColor(mGearsMaxTextColor);
                }
                mEngineStartPoint.X = mCenter.X+(mRadius/10f)*9.5f;
                mEngineStartPoint.Y= mCenter.Y;
                mEngineStopPoint.X=mCenter.X+mRadius;
                mEngineStopPoint.Y=mCenter.Y;
                float angle = mStartAngle+angleStep*i;
                rotatePoint(mEngineStartPoint,mCenter,angle);
                rotatePoint(mEngineStopPoint,mCenter,angle);
                canvas.drawLine(mEngineStartPoint.X,mEngineStartPoint.Y,
                        mEngineStopPoint.X,mEngineStopPoint.Y,
                        mPaint);
                String text = String.format(Locale.CHINA,"%d",i);
                mPaint.getTextBounds(text,0,text.length(),mRectEngineMark);
                float yOffset = 0;
                float xOffset = 0;
                if(angle<=-180){
                    mPaint.setTextAlign(Paint.Align.LEFT);
                    yOffset = 0+mRectEngineMark.height()/2f;
//                    xOffset = 0+mRectEngineMark.
                }else if(angle <= -135){
                    yOffset = 0+mRectEngineMark.height()/2f;
                }else if(angle < -90){
                    mPaint.setTextAlign(Paint.Align.LEFT);
                    yOffset = mRectEngineMark.height();
                }else if(angle == -90){
                    mPaint.setTextAlign(Paint.Align.LEFT);
                    xOffset=0-mRectEngineMark.width()/2f;
                    yOffset = mRectEngineMark.height()*1.2f;
                }else if(angle <= -45){
                    yOffset = mRectEngineMark.height();
                    xOffset = -mRectEngineMark.width();
                }else if(angle <= 0){
                    mPaint.setTextAlign(Paint.Align.RIGHT);
                    yOffset = 0+mRectEngineMark.height()/2f;
                }else{
                    mPaint.setTextAlign(Paint.Align.RIGHT);
                    yOffset = 0+mRectEngineMark.height()/2f;
                }
                canvas.drawText(text,mEngineStartPoint.X+xOffset,mEngineStartPoint.Y+yOffset,mPaint);
            }
            mPaint.setTextAlign(Paint.Align.CENTER);
        }

        //画速度
        mSpeed = Math.min(mSpeed, 999);
        mSpeed = Math.max(mSpeed, 0);

        mPaint.setTypeface(mFontTypeFace);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(mSpeedTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(mSpeedTextColor);
        if(mSpeedTextPoint.X == 0f || mSpeedTextPoint.Y == 0f || onMeasured){
            String speed = "8";
            mPaint.getTextBounds(speed, 0, speed.length(), mRectFSpeed);
            mSpeedPoint.X=mCenter.X+mRadius;
            mSpeedPoint.Y=mCenter.Y;
            rotatePoint(mSpeedPoint,mCenter,mStartAngle+mSwipAngle);
            mSpeedTextPoint.X = mCenter.X;
            mSpeedTextPoint.Y = mRectFSpeed.height()+mSpeedPoint.Y;
        }
        float fontspace = 25;
        //画3个0衬底
        mPaint.setColor(0x20FFFFFF);
        canvas.drawText("8", mSpeedTextPoint.X-mRectFSpeed.width()-fontspace,mSpeedTextPoint.Y ,mPaint);
        canvas.drawText("8", mSpeedTextPoint.X,mSpeedTextPoint.Y ,mPaint);
        canvas.drawText("8", mSpeedTextPoint.X+mRectFSpeed.width()+fontspace,mSpeedTextPoint.Y ,mPaint);
        mPaint.setColor(0xFFFFFFFF);
        //百位速度
        int hundredSpeed = (mSpeed/100)%10;
        //十位速度
        int decileSpeed = (mSpeed/10)%10;
        //个位速度
        int digitsSpeed = mSpeed%10;
        int drawspeed = 0;
        if(hundredSpeed > 0){
            drawspeed = hundredSpeed;
            if(drawspeed == 1){
                canvas.drawText(String.format(Locale.CHINA,"%d",drawspeed),
                        mSpeedTextPoint.X-mRectFSpeed.width()*0.56f-fontspace,mSpeedTextPoint.Y ,mPaint);
            } else {
                canvas.drawText(String.format(Locale.CHINA,"%d",drawspeed),
                        mSpeedTextPoint.X-mRectFSpeed.width()-fontspace,mSpeedTextPoint.Y ,mPaint);
            }
        }
        if(decileSpeed > 0) {
            drawspeed=decileSpeed;
            if(drawspeed == 1){
                canvas.drawText(String.format(Locale.CHINA,"%d",drawspeed),
                        mSpeedTextPoint.X+mRectFSpeed.width()*0.44f,mSpeedTextPoint.Y ,mPaint);
            } else {
                canvas.drawText(String.format(Locale.CHINA,"%d",drawspeed),
                        mSpeedTextPoint.X,mSpeedTextPoint.Y ,mPaint);
            }
        }else if(hundredSpeed != 0){
            canvas.drawText("0",mSpeedTextPoint.X,mSpeedTextPoint.Y ,mPaint);
        }
        if(digitsSpeed > 0){
            drawspeed=digitsSpeed;
            if(drawspeed == 1){
                canvas.drawText(String.format(Locale.CHINA,"%d",drawspeed),
                        mSpeedTextPoint.X+mRectFSpeed.width()*1.44f+fontspace,mSpeedTextPoint.Y ,mPaint);
            }else{
                canvas.drawText(String.format(Locale.CHINA,"%d",drawspeed),
                        mSpeedTextPoint.X+mRectFSpeed.width()+fontspace,mSpeedTextPoint.Y ,mPaint);
            }
        }else if(hundredSpeed != 0 || decileSpeed != 0){
            canvas.drawText("0",mSpeedTextPoint.X+mRectFSpeed.width()+fontspace,mSpeedTextPoint.Y ,mPaint);
        }

        //画挡位
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mGearsCircleStrokeSize);
        if(mEngineCurrentRPM<=mEngineIdleRPM || mGears == 0){
            mPaint.setColor(mGearsIdleTextColor);
            mGearsTextPaint.setColor(mGearsIdleTextColor);
        }else if(mEngineCurrentRPM>=(mEngineMaxRPM*1f/sections)*(sections-1)){
            mPaint.setColor(mGearsMaxTextColor);
        }else{
            mPaint.setColor(mGearsSteerTextColor);
        }
        canvas.drawArc(mCenter.X-mGearsCircleRadius,mCenter.Y-mGearsCircleRadius,
                mCenter.X+mGearsCircleRadius,mCenter.Y+mGearsCircleRadius,
                0f,360f,false,mPaint);
        mGearsTextString  =String.format(Locale.CHINA,"%d",mGears);
        if(mGears == 0){
            mGearsTextString="R";
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(mGearsTextSize);
        if(mGearsPoint.X == 0f || mGearsPoint.Y == 0f || onMeasured){
            mPaint.getTextBounds(mGearsTextString,0,mGearsTextString.length(),mRectGears);
            mGearsPoint.X = mCenter.X;
            mGearsPoint.Y = mCenter.Y+(mRectGears.height()/2f);
        }
        canvas.drawText(mGearsTextString,mGearsPoint.X ,mGearsPoint.Y,mPaint);

        mPaint.setTypeface(Typeface.MONOSPACE);
        //画指针
        float rotateAngle = mStartAngle+(mEngineCurrentRPM*1f / mEngineMaxRPM)*mSwipAngle;
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mIndicatorStart.X = mCenter.X + (mRadius/10f)*7;
        mIndicatorStart.Y = mCenter.Y;
        mIndicatorEnd.X = mCenter.X+mRadius;
        mIndicatorEnd.Y = mCenter.Y;
        rotatePoint(mIndicatorStart,mCenter,rotateAngle);
        rotatePoint(mIndicatorEnd,mCenter,rotateAngle);
        canvas.drawLine(mIndicatorStart.X,mIndicatorStart.Y,mIndicatorEnd.X,mIndicatorEnd.Y,mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0x20FFFFFF);
        mPaint.setStrokeWidth((mRadius/10f)*3);
        drawArc(canvas,mPaint,mCenter,(mRadius/10f)*8.5f,mStartAngle,rotateAngle);

        if(mAccels > 0){
            float accelsAngle = (mAccels*1f/255)*mSwipAngle;
            mPaint.setColor(mAccelsColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mAccelsStrokeSize);
            canvas.drawArc(mCenter.X-mAccelsCircleRadius,mCenter.Y-mAccelsCircleRadius,
                    mCenter.X+mAccelsCircleRadius,mCenter.Y+mAccelsCircleRadius,
                    mStartAngle+accelsAngle,0-accelsAngle,false,mPaint);
        }

        //刹车
        if(mBrake > 0){
            float brakeAngle = (mStartAngle+mSwipAngle)-(mBrake*1f/255)*mSwipAngle;
            mPaint.setColor(mBrakeColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mAccelsStrokeSize);
            drawArc(canvas,mPaint,mCenter,mBrakeCircleRadius,mStartAngle+mSwipAngle,brakeAngle);
        }

        //画方向盘
        mPaint.setColor(mSteerColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mSteerExternalCircleStrokeSize);
        drawArc(canvas,mPaint,mCenter,mSteerExternalCircleRaius,0,360);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mSteerInternalCircleStrokeSize);
        drawArc(canvas,mPaint,mCenter,mSteerInternalCircleRadius,0,360);
        //左上角的连接线
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mSteerLinkLinkeSize);
        mSteerLine1StartPoint.X = mCenter.X+mSteerInternalCircleRadius;
        mSteerLine1StartPoint.Y = mCenter.Y;
        mSteerLine1StopPoint.X = mCenter.X+mSteerExternalCircleRaius;
        mSteerLine1StopPoint.Y = mCenter.Y;
        int space=0;
        //计算真正的转向角度
        if(mSteer > mRealSteer){
            space  = mSteer-mRealSteer;
            if(space < mMaxSteer/25){
                mRealSteer = mSteer;
            }else{
                mRealSteer += space/6;
            }
//            mRealSteer += (mMaxSteer/20f);
//            if(mRealSteer > mSteer){
//                mRealSteer = mSteer;
//            }
        }else{
            space = mRealSteer - mSteer;
            if(space < mMaxSteer/25){
                mRealSteer= mSteer;
            }else{
                mRealSteer-=space/6;
            }
//            mRealSteer -= (mMaxSteer/20f);
//            if(mRealSteer < mSteer){
//                mRealSteer = mSteer;
//            }
        }
        float steerRotateAngle = (mRealSteer*1f/mMaxSteer)*mMaxSteerRotateAngle;
        rotatePoint(mSteerLine1StartPoint,mCenter,-170f+steerRotateAngle);
        rotatePoint(mSteerLine1StopPoint,mCenter,-170f+steerRotateAngle);
        canvas.drawLine(mSteerLine1StartPoint.X,mSteerLine1StartPoint.Y,mSteerLine1StopPoint.X,mSteerLine1StopPoint.Y,mPaint);

        mSteerLine2StartPoint.X = mCenter.X+mSteerInternalCircleRadius;
        mSteerLine2StartPoint.Y = mCenter.Y;
        mSteerLine2StopPoint.X = mCenter.X+mSteerExternalCircleRaius;
        mSteerLine2StopPoint.Y = mCenter.Y;
        rotatePoint(mSteerLine2StartPoint,mCenter,-10f+steerRotateAngle);
        rotatePoint(mSteerLine2StopPoint,mCenter,-10f+steerRotateAngle);
        canvas.drawLine(mSteerLine2StartPoint.X,mSteerLine2StartPoint.Y,mSteerLine2StopPoint.X,mSteerLine2StopPoint.Y,mPaint);

        mSteerLine3StartPoint.X = mCenter.X+mSteerInternalCircleRadius;
        mSteerLine3StartPoint.Y = mCenter.Y;
        mSteerLine3StopPoint.X = mCenter.X+mSteerExternalCircleRaius;
        mSteerLine3StopPoint.Y = mCenter.Y;
        rotatePoint(mSteerLine3StartPoint,mCenter,90f+steerRotateAngle);
        rotatePoint(mSteerLine3StopPoint,mCenter,90f+steerRotateAngle);
        canvas.drawLine(mSteerLine3StartPoint.X,mSteerLine3StartPoint.Y,mSteerLine3StopPoint.X,mSteerLine3StopPoint.Y,mPaint);

        onMeasured  = false;
    }

    private void drawArc(Canvas canvas,Paint paint,final LocalPorint center,float radius,float startAngle,float stopAngle){
        canvas.drawArc(center.X-radius,center.Y-radius,
                center.X+radius,center.Y+radius,
                stopAngle,startAngle-stopAngle,
                false,paint);
    }

    public void update(float speed,int maxRpm,int currentRpm,int idlerpm,int gears,int accels,int brake,int steer){
        mSpeed = (int)speed;
        if(maxRpm>0){
            mEngineMaxRPM = (int)maxRpm;
        }
        mEngineCurrentRPM = (int)currentRpm;
        mEngineIdleRPM = idlerpm;
        mGears = gears;
        mAccels = accels;
        mBrake = Math.min(255,brake);
        mBrake = Math.max(0,brake);
        mSteer = Math.min(127,steer);
        mSteer = Math.max(-127,steer);
        invalidate();
    }

    private void rotatePoint(LocalPorint point,final LocalPorint center,float angle){
        double arcAngle = Math.toRadians(angle);
        LocalPorint result = new LocalPorint();
        result.X = (float)((point.X - center.X)*Math.cos(arcAngle)-(point.Y - center.Y)*Math.sin(arcAngle))+ center.X;
        result.Y = (float)((point.X - center.X)*Math.sin(arcAngle)-(point.Y - center.Y)*Math.cos(arcAngle))+ center.Y ;
        point.X = result.X;
        point.Y = result.Y;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int mapEngineSectionCount(int maxRpm){
        if(maxRpm<1000){
            return mapEngineSectionCount(maxRpm*10);
        }
        if(maxRpm%1000 >0){
            return (maxRpm+1000)/1000;
        }else{
            return maxRpm/1000;
        }
    }
}

class LocalPorint{
    public float X = 0f;
    public float Y = 0f;

    @NonNull
    public String toString(){
        return String.format(Locale.CHINA,"(%.2f,%.2f)",X,Y);
    }
}