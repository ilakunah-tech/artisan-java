package org.artisan.device;

/**
 * DeviceChannel that always returns NaN (no device selected).
 */
public final class NullDeviceChannel implements DeviceChannel {

    @Override
    public void open() throws DeviceException {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public SampleResult read() throws DeviceException {
        return SampleResult.unavailable();
    }

    @Override
    public String getDescription() {
        return "No device";
    }
}
