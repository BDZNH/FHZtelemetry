package com.bdznh.fhztelemetry.util;

import android.os.Trace;

/**
 * Helper wrapper for systrace debugging.
 */
public final class TraceHelper {

    private TraceHelper() {
        // utility class
    }

    public static void beginSection(String sectionName) {
        Trace.beginSection(sectionName);
    }

    public static void endSection() {
        Trace.endSection();
    }
}
