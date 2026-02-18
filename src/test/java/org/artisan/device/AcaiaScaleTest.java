package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link AcaiaScale}. Acaia is BLE-based (no SerialPort); tests verify DevicePort contract.
 */
class AcaiaScaleTest {

    @Test
    void isConnectedReturnsFalseWhenNotConnected() {
        AcaiaScale scale = new AcaiaScale("AA:BB:CC", "LUNAR-1");
        assertFalse(scale.isConnected());
    }

    @Test
    void connectAndDisconnectAreSafe() {
        AcaiaScale scale = new AcaiaScale("", "");
        assertDoesNotThrow(scale::connect);
        assertFalse(scale.isConnected());
        assertDoesNotThrow(scale::disconnect);
    }

    @Test
    void readTemperaturesReturnsEmptyArray() {
        AcaiaScale scale = new AcaiaScale("id", "name");
        double[] temps = scale.readTemperatures();
        assertNotNull(temps);
        assertEquals(0, temps.length);
    }

    @Test
    void getIdentAndGetNameReturnConstructorValues() {
        AcaiaScale scale = new AcaiaScale("ADDR", "PEARL-1");
        assertEquals("ADDR", scale.getIdent());
        assertEquals("PEARL-1", scale.getName());
    }

    @Test
    void nullIdentAndNameBecomeEmpty() {
        AcaiaScale scale = new AcaiaScale(null, null);
        assertEquals("", scale.getIdent());
        assertEquals("", scale.getName());
    }
}
