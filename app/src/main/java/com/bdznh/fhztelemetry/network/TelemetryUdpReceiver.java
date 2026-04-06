package com.bdznh.fhztelemetry.network;

import android.util.Log;
import com.bdznh.fhztelemetry.callback.TelemetryCallback;
import com.bdznh.fhztelemetry.data.model.ForzaHorizonData;
import com.bdznh.fhztelemetry.data.parser.ForzaHorizonDataParser;
import com.bdznh.fhztelemetry.data.parser.ForzaHorizonDataParser.DataFrame;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TelemetryUdpReceiver {
    private static final String TAG = "TelemetryUdp";

    private TelemetryCallback callback;

    // Reused data model — never replaced, fields updated in-place on each packet
    private final ForzaHorizonData dataModel;
    // Reused internal frame — allocated ONCE, fields updated on each packet
    private final DataFrame dataFrame;
    private final ForzaHorizonDataParser parser;

    private DatagramSocket socket;
    private Thread receiveThread;
    private volatile boolean running = false;
    private volatile boolean paused = false;

    public TelemetryUdpReceiver(TelemetryCallback callback) {
        this.callback = callback;
        this.dataModel = new ForzaHorizonData();
        this.dataFrame = new DataFrame();
        this.parser = new ForzaHorizonDataParser();
    }

    public long start(int port) {
        if (running) {
            Log.e(TAG, "already running, don't start again");
            return -1;
        }
        running = true;
        paused = false;

        receiveThread = new Thread(() -> {
            Log.d(TAG, "UDP receiver thread started, port=" + port);
            DatagramSocket sock = null;
            try {
                sock = new DatagramSocket(port);
                socket = sock;
                synchronized (this) {
                    notifyAll();
                }

                byte[] buffer = new byte[ForzaHorizonDataParser.DATA_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, ForzaHorizonDataParser.DATA_SIZE);
                ByteBuffer bb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);

                int onMenu = 0;

                while (running) {
                    try {
                        packet.setLength(ForzaHorizonDataParser.DATA_SIZE);
                        sock.receive(packet);
                    } catch (Exception e) {
                        if (running) {
                            Log.e(TAG, "recvfrom error: " + e);
                        }
                        break;
                    }

                    int readLen = packet.getLength();
                    if (readLen != ForzaHorizonDataParser.DATA_SIZE) {
                        Log.w(TAG, "expect " + ForzaHorizonDataParser.DATA_SIZE + " but received " + readLen);
                        continue;
                    }

                    bb.rewind();
                    parser.parse(bb, dataFrame);

                    // Race state change detection
                    if (onMenu != dataFrame.IsRaceOn) {
                        onMenu = dataFrame.IsRaceOn;
                        if (onMenu == 1) {
                            notifyRaceStarted();
                        } else {
                            notifyRaceEnded();
                        }
                    }

                    if (!paused && dataFrame.IsRaceOn == 1) {
                        // Copy parsed frame fields into the shared dataModel reference
                        // No new objects created — same ForzaHorizonData instance reused every frame
                        parser.fillModel(dataFrame, dataModel);
                        notifyDataUpdate();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "UDP receiver error: " + e);
            } finally {
                if (sock != null && !sock.isClosed()) {
                    sock.close();
                }
                socket = null;
                Log.d(TAG, "UDP receiver thread ended");
            }
        }, "ForzaUDPReceiver");

        receiveThread.start();

        synchronized (this) {
            try {
                wait(3000);
            } catch (InterruptedException ignored) {
            }
        }

        if (socket != null && socket.isBound()) {
            Log.d(TAG, "start port " + port + " success");
            return 0;
        } else {
            Log.e(TAG, "start port " + port + " failed");
            running = false;
            if (receiveThread != null) {
                receiveThread.interrupt();
            }
            return -1;
        }
    }

    public void pause() {
        if (running) {
            paused = !paused;
            Log.d(TAG, (paused ? "Pause" : "Resume"));
        } else {
            Log.e(TAG, "pause called but not started");
        }
    }

    public long stop() {
        Log.d(TAG, "stop");
        if (running) {
            paused = true;
            running = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (receiveThread != null) {
                try {
                    receiveThread.join(1000);
                } catch (InterruptedException ignored) {
                }
                receiveThread = null;
            }
        }
        return 0;
    }

    public void release() {
        if (running) {
            stop();
        }
        callback = null;
        Log.d(TAG, "release");
    }

    private void notifyDataUpdate() {
        // dataModel is a shared reference — the receiver and UI must both respect
        // that it is mutated in-place. The UI copies values it cares about in refreshUI().
        if (callback != null) {
            callback.onTelemetryDataUpdate(dataModel);
        }
    }

    private void notifyRaceStarted() {
        if (callback != null) {
            callback.onRaceStarted();
        }
    }

    private void notifyRaceEnded() {
        if (callback != null) {
            callback.onRaceEnded();
        }
    }
}
