package com.bdznh.fhztelemetry.util;

import android.content.Context;
import com.bdznh.fhztelemetry.R;

public class Constant {

    public static final int DEFAULT_PORT = 9998;

    // Handler message types
    public static final int MSG_REFRESH_UI = 1;
    public static final int MSG_PAUSE_RECV = 2;
    public static final int MSG_START_RECV = 3;
    public static final int MSG_AUTO_FULLSCREEN = 4;

    public static final long AUTO_FULLSCREEN_DELAY_MS = 10_000L;

    // -----------------------------------------------------------------------
    // Car Class mapping (0=D, 1=C, 2=B, 3=A, 4=S1, 5=S2, 6=X)
    // -----------------------------------------------------------------------
    public static String mapIntToCarClass(int carClass, Context context) {
        switch (carClass) {
            case 0: return "D";
            case 1: return "C";
            case 2: return "B";
            case 3: return "A";
            case 4: return "S1";
            case 5: return "S2";
            case 6: return "X";
            default: return "D";
        }
    }

    // -----------------------------------------------------------------------
    // Drivetrain Type mapping (0=FWD, 1=RWD, 2=AWD)
    // -----------------------------------------------------------------------
    public static String mapIntToDrivingType(int type, Context context) {
        switch (type) {
            case 0: return context.getResources().getString(R.string.drive_train_type_0);
            case 1: return context.getResources().getString(R.string.drive_train_type_1);
            case 2: return context.getResources().getString(R.string.drive_train_type_2);
            default: return String.valueOf(type);
        }
    }

    // -----------------------------------------------------------------------
    // Car Category mapping (Forza Horizon car category codes)
    // -----------------------------------------------------------------------
    public static String mapIntToCarCategory(int category, Context context) {
        switch (category) {
            case 11: return context.getResources().getString(R.string.car_category_11);
            case 12: return context.getResources().getString(R.string.car_category_12);
            case 13: return context.getResources().getString(R.string.car_category_13);
            case 14: return context.getResources().getString(R.string.car_category_14);
            case 16: return context.getResources().getString(R.string.car_category_16);
            case 17: return context.getResources().getString(R.string.car_category_17);
            case 18: return context.getResources().getString(R.string.car_category_18);
            case 19: return context.getResources().getString(R.string.car_category_19);
            case 20: return context.getResources().getString(R.string.car_category_20);
            case 21: return context.getResources().getString(R.string.car_category_21);
            case 22: return context.getResources().getString(R.string.car_category_22);
            case 23: return context.getResources().getString(R.string.car_category_23);
            case 24: return context.getResources().getString(R.string.car_category_24);
            case 25: return context.getResources().getString(R.string.car_category_25);
            case 26: return context.getResources().getString(R.string.car_category_26);
            case 28: return context.getResources().getString(R.string.car_category_28);
            case 29: return context.getResources().getString(R.string.car_category_29);
            case 30: return context.getResources().getString(R.string.car_category_30);
            case 31: return context.getResources().getString(R.string.car_category_31);
            case 32: return context.getResources().getString(R.string.car_category_32);
            case 33: return context.getResources().getString(R.string.car_category_33);
            case 34: return context.getResources().getString(R.string.car_category_34);
            case 35: return context.getResources().getString(R.string.car_category_35);
            case 36: return context.getResources().getString(R.string.car_category_36);
            case 37: return context.getResources().getString(R.string.car_category_37);
            case 38: return context.getResources().getString(R.string.car_category_38);
            case 39: return context.getResources().getString(R.string.car_category_39);
            case 40: return context.getResources().getString(R.string.car_category_40);
            case 41: return context.getResources().getString(R.string.car_category_41);
            case 42: return context.getResources().getString(R.string.car_category_42);
            case 43: return context.getResources().getString(R.string.car_category_43);
            case 44: return context.getResources().getString(R.string.car_category_44);
            case 45: return context.getResources().getString(R.string.car_category_45);
            case 46: return context.getResources().getString(R.string.car_category_46);
            case 47: return context.getResources().getString(R.string.car_category_47);
            case 48: return context.getResources().getString(R.string.car_category_48);
            case 49: return context.getResources().getString(R.string.car_category_49);
            default: return String.valueOf(category);
        }
    }
}
