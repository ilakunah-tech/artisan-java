package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link AillioR1Device}. Device uses USB (no SerialPort).
 */
class AillioR1DeviceTest {

    @Test
    void isConnectedReturnsFalse() {
        AillioR1Device device = new AillioR1Device();
        assertFalse(device.isConnected());
    }

    @Test
    void connectAndDisconnectAreSafe() {
        AillioR1Device device = new AillioR1Device();
        assertDoesNotThrow(device::connect);
        assertFalse(device.isConnected());
        assertDoesNotThrow(device::disconnect);
    }

    @Test
    void readTemperaturesReturnsEmptyArray() {
        AillioR1Device device = new AillioR1Device();
        double[] temps = device.readTemperatures();
        assertNotNull(temps);
        assertEquals(0, temps.length);
    }
}
