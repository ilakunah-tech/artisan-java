package org.artisan.device;

/**
 * BlueDOT BLE temperature probe device.
 * Parity stub for Python artisanlib/bluedot.py (class BlueDOT extends ClientBLE).
 * Full BLE GATT characteristic parsing not yet ported; returns 0.0/0.0 until implemented.
 */
public class BlueDotDevice implements DevicePort {

    private boolean connected = false;

    public BlueDotDevice() {}

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
