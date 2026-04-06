package com.bdznh.fhztelemetry.ui.widget;

import androidx.annotation.NonNull;
import java.util.Locale;

/**
 * Simple 2D point with float coordinates.
 * Used internally by ForzaHorizonDashboard for coordinate calculations.
 */
class LocalPorint {
    public float X = 0f;
    public float Y = 0f;

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.CHINA, "(%.2f,%.2f)", X, Y);
    }
}
