package org.artisan.device;

// Ported from scale.py — original Python lines: 75–148

/**
 * Base device abstraction for scales (weight). Implements {@link DevicePort} with no-op
 * connect/disconnect and no temperature data (scales do not provide ET/BT).
 * Subclasses (e.g. AcaiaScale) add actual connection and weight handling.
 */
public class ScaleDevice implements DevicePort {

    @Override
    public void connect() {
        // Base scale: no-op (subclasses override for BT/Serial)
    }

    @Override
    public void disconnect() {
        // Base scale: no-op
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public double[] readTemperatures() {
        // Scales do not provide temperatures
        return new double[0];
    }
}
