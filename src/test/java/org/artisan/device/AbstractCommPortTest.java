package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AbstractCommPort} (and {@link DevicePort} contract).
 * Uses a concrete subclass and invalid/non-existent port names; no real hardware.
 */
class AbstractCommPortTest {

    /** Concrete implementation for tests; returns empty temperatures. */
    private static final class TestableCommPort extends AbstractCommPort {
        TestableCommPort(String portName) {
            super(portName);
        }

        @Override
        protected double[] readTemperaturesImpl() {
            return new double[0];
        }
    }

    @Test
    void connectWhenPortUnavailableThrowsCommException() {
        DevicePort port = new TestableCommPort("COM99999");
        assertThrows(CommException.class, port::connect);
        assertFalse(port.isConnected());
    }

    @Test
    void disconnectWithoutConnectDoesNotThrow() {
        DevicePort port = new TestableCommPort("COM1");
        assertDoesNotThrow(port::disconnect);
        assertFalse(port.isConnected());
    }

    @Test
    void readTemperaturesOnClosedPortReturnsEmptyArray() {
        DevicePort port = new TestableCommPort("COM1");
        double[] temps = port.readTemperatures();
        assertTrue(temps != null && temps.length == 0);
        assertFalse(port.isConnected());
    }

    @Test
    void isConnectedReturnsCorrectState() {
        TestableCommPort port = new TestableCommPort("COM99999");
        assertFalse(port.isConnected());
        assertThrows(CommException.class, port::connect);
        assertFalse(port.isConnected());
        port.disconnect();
        assertFalse(port.isConnected());
    }

    @Test
    void disconnectAfterFailedConnectDoesNotThrow() {
        TestableCommPort port = new TestableCommPort("COM99999");
        try {
            port.connect();
        } catch (CommException ignored) {
            // expected
        }
        assertDoesNotThrow(port::disconnect);
        assertFalse(port.isConnected());
    }
}
