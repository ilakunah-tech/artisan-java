package org.artisan.device;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stub implementation of DeviceChannel for Phidgets temperature sensors.
 * connect(): logs INFO "Phidgets: connecting to serial=" + serialNumber.
 * readSample(): returns synthetic BT/ET (200–230°C range) for testing.
 * Full implementation requires phidget22java library; stub only.
 */
public final class PhidgetsDeviceChannel implements DeviceChannel {

    private static final Logger LOG = Logger.getLogger(PhidgetsDeviceChannel.class.getName());

    private final PhidgetsConfig config;
    private boolean open;

    public PhidgetsDeviceChannel(PhidgetsConfig config) {
        this.config = config != null ? config : new PhidgetsConfig();
    }

    @Override
    public void open() throws DeviceException {
        LOG.log(Level.INFO, "Phidgets: connecting to serial={0}", config.getSerialNumber());
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
        if (!open) {
            throw new DeviceException("Phidgets channel not open");
        }
        double bt = 200.0 + Math.random() * 30;
        double et = 220.0 + Math.random() * 30;
        return SampleResult.now(bt, et);
    }

    @Override
    public String getDescription() {
        return "Phidgets " + (config.getSerialNumber().isEmpty() ? "stub" : config.getSerialNumber());
    }
}
