package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ModbusPort} with mock (no real Modbus device).
 */
class ModbusPortTest {

    @Test
    void disconnectWithoutConnectDoesNotThrow() {
        ModbusPort port = new ModbusPort("127.0.0.1", 502);
        port.disconnect();
        assertFalse(port.isConnected());
    }

    @Test
    void isConnectedFalseWhenNotConnected() {
        ModbusPort port = new ModbusPort("127.0.0.1", 502);
        assertFalse(port.isConnected());
    }

    @Test
    void connectToInvalidHostThrowsCommException() {
        ModbusPort port = new ModbusPort("192.0.2.1", 502, 500);
        assertThrows(CommException.class, port::connect);
        assertFalse(port.isConnected());
    }

    @Test
    void disconnectAfterFailedConnectDoesNotThrow() {
        ModbusPort port = new ModbusPort("192.0.2.1", 502);
        try {
            port.connect();
        } catch (CommException ignored) {
        }
        port.disconnect();
        assertFalse(port.isConnected());
    }

    @Test
    void addressToRegisterHolding() {
        assertTrue(ModbusPort.addressToRegister(40001, 3) == 0);
        assertTrue(ModbusPort.addressToRegister(40002, 6) == 1);
    }

    @Test
    void addressToRegisterInput() {
        assertTrue(ModbusPort.addressToRegister(30001, 4) == 0);
    }

    @Test
    void readHoldingRegistersWhenNotConnectedReturnsNull() {
        ModbusPort port = new ModbusPort("127.0.0.1", 502);
        int[] regs = port.readHoldingRegisters(1, 0, 1);
        assertTrue(regs == null);
    }

    @Test
    void writeSingleRegisterWhenNotConnectedThrows() {
        ModbusPort port = new ModbusPort("127.0.0.1", 502);
        assertThrows(CommException.class, () -> port.writeSingleRegister(1, 0, 100));
    }
}
