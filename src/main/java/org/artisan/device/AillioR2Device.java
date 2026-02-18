package org.artisan.device;

// Ported from aillio_r2.py — original Python lines: 61–250

/**
 * Aillio Bullet R2 roaster device (USB). Uses libusb in Python; this stub implements
 * {@link DevicePort} for future USB support. Temperatures from A0 frame (bean_probe_temp, etc.).
 */
public class AillioR2Device implements DevicePort {

    private boolean connected;

    public AillioR2Device() {
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
        // BT, DT etc. from USB frames; no USB yet
        return new double[0];
    }
}
