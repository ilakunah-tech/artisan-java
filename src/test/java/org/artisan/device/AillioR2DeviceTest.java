package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link AillioR2Device}. Device uses USB (no SerialPort).
 */
class AillioR2DeviceTest {

    @Test
    void isConnectedReturnsFalse() {
        AillioR2Device device = new AillioR2Device();
        assertFalse(device.isConnected());
    }

    @Test
    void connectAndDisconnectAreSafe() {
        AillioR2Device device = new AillioR2Device();
        assertDoesNotThrow(device::connect);
        assertFalse(device.isConnected());
        assertDoesNotThrow(device::disconnect);
    }

    @Test
    void readTemperaturesReturnsEmptyArray() {
        AillioR2Device device = new AillioR2Device();
        double[] temps = device.readTemperatures();
        assertNotNull(temps);
        assertEquals(0, temps.length);
    }
}
