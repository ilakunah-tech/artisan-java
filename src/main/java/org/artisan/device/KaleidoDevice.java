package org.artisan.device;

// Ported from kaleido.py — original Python lines: 50–174

/**
 * Kaleido roaster device (websocket/serial). getBTET() returns BT, ET, sid from state; this stub
 * implements {@link DevicePort} for future websocket/serial support.
 */
public class KaleidoDevice implements DevicePort {

    private boolean connected;

    public KaleidoDevice() {
        this.connected = false;
    }

    @Override
    public void connect() {
        // Websocket/serial connect not implemented yet
        connected = false;
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public double[] readTemperatures() {
        // BT, ET from getBTET(); no connection yet
        return new double[0];
    }
}
