package com.bdznh.fhztelemetry.callback;

import com.bdznh.fhztelemetry.data.model.ForzaHorizonData;

/**
 * Callback interface for telemetry data updates from Forza Horizon UDP stream.
 */
public interface TelemetryCallback {

    /**
     * Called when a new telemetry data frame is available.
     * @param data the parsed Forza Horizon telemetry data
     */
    void onTelemetryDataUpdate(ForzaHorizonData data);

    /**
     * Called when the race starts (car enters gameplay).
     */
    void onRaceStarted();

    /**
     * Called when the race ends or car returns to menu.
     */
    void onRaceEnded();
}
