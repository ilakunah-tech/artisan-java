package org.artisan.device;

// Ported from aillio_r1.py — original Python lines: 60–338

/**
 * Aillio Bullet R1 roaster device (USB). Uses libusb in Python; this stub implements
 * {@link DevicePort} for future USB support. BT/DT come from status packets (state[0:4], state[8:12]).
 */
public class AillioR1Device implements DevicePort {

    private boolean connected;

    public AillioR1Device() {
        this.connected = false;
    }

    @Override
    public void connect() {
        // USB connect not implemented yet
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
        // BT, DT (and exitt) from USB status; no USB yet
        return new double[0];
    }
}
