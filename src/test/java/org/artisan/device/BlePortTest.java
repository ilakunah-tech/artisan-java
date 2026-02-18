package org.artisan.device;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link BlePort} stub (no BLE hardware).
 */
class BlePortTest {

    @Test
    void scanAndConnectReturnsNull() {
        BlePort port = new BlePort();
        BlePort.ScanResult result = port.scanAndConnect(
                Map.of("00001234-0000-1000-8000-00805f9b34fb", Set.of("Device")),
                Collections.emptySet(),
                true,
                1.0,
                1.0,
                null);
        assertNull(result);
    }

    @Test
    void readReturnsNull() {
        BlePort port = new BlePort();
        byte[] data = port.read(null, "00001234-0000-1000-8000-00805f9b34fb");
        assertNull(data);
    }

    @Test
    void writeThrowsUnsupportedOperationException() {
        BlePort port = new BlePort();
        assertThrows(UnsupportedOperationException.class, () ->
                port.write(null, "uuid", new byte[]{1, 2, 3}, false, 20));
    }

    @Test
    void disconnectDoesNotThrow() {
        BlePort port = new BlePort();
        port.disconnect(null);
    }

    @Test
    void closeDoesNotThrow() {
        BlePort port = new BlePort();
        port.close();
    }
}
