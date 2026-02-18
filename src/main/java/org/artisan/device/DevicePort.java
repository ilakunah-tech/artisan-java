package org.artisan.device;

/**
 * Abstraction for a device communication port (e.g. serial/COM).
 * Concrete implementations handle specific hardware (Hottop, Aillio, Phidgets, etc.).
 */
public interface DevicePort {

    /**
     * Opens the port. May perform reconnect attempts on failure.
     *
     * @throws CommException if the port cannot be opened
     */
    void connect();

    /**
     * Closes the port. Idempotent: safe to call when already disconnected.
     */
    void disconnect();

    /**
     * Returns whether the port is currently open and available for I/O.
     */
    boolean isConnected();

    /**
     * Reads temperature values from the device (e.g. ET, BT). Order and count are device-specific.
     * When not connected, returns an empty array or throws depending on implementation.
     *
     * @return array of temperatures (e.g. Celsius); never null
     * @throws CommException if connected but read fails
     */
    double[] readTemperatures();
}
