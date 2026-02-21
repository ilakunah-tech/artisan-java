package org.artisan.device;

/**
 * LeBrew RoastSeeNEXT device — BLE communication.
 * Parity stub for Python artisanlib/lebrew.py (class Lebrew_RoastSeeNEXT_BLE / Lebrew_RoastSeeNEXT).
 * Full BLE protocol parsing not yet ported; returns 0.0/0.0 until implemented.
 */
public class LeBrewDevice implements DevicePort {

    private boolean connected = false;

    public LeBrewDevice() {}

    @Override
    public void connect() {
        // BLE connection not yet implemented on Java desktop; see BlePort stub
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
        return new double[]{0.0, 0.0};
    }
}
