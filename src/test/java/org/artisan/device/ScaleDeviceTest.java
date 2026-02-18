package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link ScaleDevice} (base scale DevicePort, no serial port).
 */
class ScaleDeviceTest {

    @Test
    void isConnectedReturnsFalse() {
        ScaleDevice device = new ScaleDevice();
        assertFalse(device.isConnected());
    }

    @Test
    void connectAndDisconnectAreNoOp() {
        ScaleDevice device = new ScaleDevice();
        assertDoesNotThrow(device::connect);
        assertFalse(device.isConnected());
        assertDoesNotThrow(device::disconnect);
        assertFalse(device.isConnected());
    }

    @Test
    void readTemperaturesReturnsEmptyArray() {
        ScaleDevice device = new ScaleDevice();
        double[] temps = device.readTemperatures();
        assertNotNull(temps);
        assertEquals(0, temps.length);
    }
}
