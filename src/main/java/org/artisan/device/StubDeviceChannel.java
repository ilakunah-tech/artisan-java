package org.artisan.device;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stub DeviceChannel for physical devices not yet implemented. Logs and returns NaN.
 */
public final class StubDeviceChannel implements DeviceChannel {

    private static final Logger LOG = Logger.getLogger(StubDeviceChannel.class.getName());

    private final String deviceName;
    private final String portName;
    private volatile boolean open;

    public StubDeviceChannel(String deviceName, String portName) {
        this.deviceName = deviceName != null ? deviceName : "Device";
        this.portName = portName != null ? portName : "";
    }

    @Override
    public void open() throws DeviceException {
        LOG.log(Level.INFO, "{0}: opening on {1}", new Object[] { deviceName, portName.isEmpty() ? "<no port>" : portName });
        open = true;
    }

    @Override
    public void close() {
        open = false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public SampleResult read() throws DeviceException {
        LOG.log(Level.WARNING, "{0}: read not yet implemented", deviceName);
        return SampleResult.unavailable();
    }

    @Override
    public String getDescription() {
        return deviceName + " (stub)";
    }
}
