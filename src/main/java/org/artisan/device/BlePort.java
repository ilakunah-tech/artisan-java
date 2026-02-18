package org.artisan.device;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * BLE (Bluetooth Low Energy) port stub.
 * <p>
 * TODO: Java standard library does not provide a cross-platform BLE API on desktop (JSR 82 / javax.bluetooth
 * is optional and often not available). A full implementation would require either:
 * <ul>
 *   <li>A platform-specific library (e.g. BlueCove on desktop, or Android BLE APIs on Android), or</li>
 *   <li>A third-party library that bundles native BLE support for the target platform.</li>
 * </ul>
 * This stub preserves the API shape of Python artisanlib.ble_port for future implementation.
 * </p>
 */
public class BlePort {

    /** Placeholder for a BLE client handle when a real BLE stack is available. */
    public interface BleClientHandle {
        boolean isConnected();
    }

    /**
     * Scans for devices matching the given descriptions and connects to the first match.
     *
     * @param deviceDescriptions map from service UUID to set of device names (null = any name)
     * @param blacklist          set of device addresses to ignore
     * @param caseSensitive      whether name matching is case-sensitive
     * @param scanTimeoutSeconds scan timeout in seconds
     * @param connectTimeoutSeconds connect timeout in seconds
     * @param address            optional specific device address to connect to
     * @return null (stub: BLE not available on this platform)
     */
    public ScanResult scanAndConnect(
            Map<String, Set<String>> deviceDescriptions,
            Set<String> blacklist,
            boolean caseSensitive,
            double scanTimeoutSeconds,
            double connectTimeoutSeconds,
            String address) {
        // TODO: Implement when BLE API is available on target platform
        return null;
    }

    /**
     * Disconnects the given BLE client. No-op in stub.
     */
    public void disconnect(BleClientHandle client) {
        // TODO: Implement when BLE API is available
    }

    /**
     * Writes data to a GATT characteristic. Stub throws.
     */
    public void write(BleClientHandle client, String writeUuid, byte[] message, boolean response, int chunkSize) {
        // TODO: Implement when BLE API is available
        throw new UnsupportedOperationException("BLE not available on this platform");
    }

    /**
     * Reads from a GATT characteristic. Stub returns null.
     */
    public byte[] read(BleClientHandle client, String readUuid) {
        // TODO: Implement when BLE API is available
        return null;
    }

    /**
     * Starts notifications on a characteristic. No-op in stub.
     */
    public void startNotify(BleClientHandle client, String uuid, Callable<Void> onData) {
        // TODO: Implement when BLE API is available
    }

    /**
     * Stops notifications on a characteristic. No-op in stub.
     */
    public void stopNotify(BleClientHandle client, String uuid) {
        // TODO: Implement when BLE API is available
    }

    /**
     * Stops an ongoing scan. No-op in stub.
     */
    public void terminateScan() {
        // TODO: Implement when BLE API is available
    }

    /**
     * Releases resources. No-op in stub.
     */
    public void close() {
        // TODO: Implement when BLE API is available
    }

    /** Result of a successful scan-and-connect. */
    public static final class ScanResult {
        private final BleClientHandle client;
        private final String serviceUuid;
        private final String deviceName;

        public ScanResult(BleClientHandle client, String serviceUuid, String deviceName) {
            this.client = client;
            this.serviceUuid = serviceUuid;
            this.deviceName = deviceName;
        }

        public BleClientHandle getClient() { return client; }
        public String getServiceUuid() { return serviceUuid; }
        public String getDeviceName() { return deviceName; }
    }
}
