package org.artisan.device;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Mugma roaster device — TCP communication via AsyncCommPort.
 * Parity stub for Python artisanlib/mugma.py (class Mugma extends AsyncComm).
 * Full protocol parsing is not yet ported; returns 0.0/0.0 until implemented.
 */
public class MugmaDevice extends AsyncCommPort implements DevicePort {

    private static final String DEFAULT_HOST = "192.168.10.10";
    private static final int    DEFAULT_PORT  = 8088;

    private final AtomicReference<double[]> lastReadings = new AtomicReference<>(new double[]{0.0, 0.0});

    private volatile boolean connected = false;

    public MugmaDevice() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public MugmaDevice(String host, int port) {
        super(host, port);
        setConnectedHandler(() -> connected = true);
        setDisconnectedHandler(() -> {
            connected = false;
            resetReadings();
        });
    }

    @Override
    protected void resetReadings() {
        lastReadings.set(new double[]{0.0, 0.0});
    }

    @Override
    public void connect() {
        start(5.0);
    }

    @Override
    public void disconnect() {
        stop();
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public double[] readTemperatures() {
        return lastReadings.get().clone();
    }
}
