package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link KaleidoDevice}. Device uses websocket/serial (no SerialPort mock in this stub).
 */
class KaleidoDeviceTest {

    @Test
    void isConnectedReturnsFalse() {
        KaleidoDevice device = new KaleidoDevice();
        assertFalse(device.isConnected());
    }

    @Test
    void connectAndDisconnectAreSafe() {
        KaleidoDevice device = new KaleidoDevice();
        assertDoesNotThrow(device::connect);
        assertFalse(device.isConnected());
        assertDoesNotThrow(device::disconnect);
    }

    @Test
    void readTemperaturesReturnsEmptyArray() {
        KaleidoDevice device = new KaleidoDevice();
        double[] temps = device.readTemperatures();
        assertNotNull(temps);
        assertEquals(0, temps.length);
    }
}
