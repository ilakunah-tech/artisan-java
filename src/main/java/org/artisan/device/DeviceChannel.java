package org.artisan.device;

/**
 * Abstraction for a device communication channel (Serial, MODBUS, BLE).
 * Used by CommController for the sampling loop.
 */
public interface DeviceChannel {

    /**
     * Opens the channel. Must be called before read().
     *
     * @throws DeviceException if the channel cannot be opened
     */
    void open() throws DeviceException;

    /**
     * Closes the channel. Idempotent.
     */
    void close();

    /**
     * Returns whether the channel is currently open.
     */
    boolean isOpen();

    /**
     * Reads one sample (BT, ET). Blocking until data is available or timeout.
     *
     * @return sample with bt, et, timestampMs (use NaN for unavailable)
     * @throws DeviceException on read or parse error
     */
    SampleResult read() throws DeviceException;

    /**
     * Short description for status bar (e.g. "Serial COM3", "Modbus 192.168.1.1:502").
     */
    String getDescription();
}
