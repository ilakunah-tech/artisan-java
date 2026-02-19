package org.artisan.device;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stub DeviceChannel for BLE. Full BLE requires a native stack; this allows
 * the Ports dialog and config to be wired now. open() logs a warning and
 * isOpen() returns false; read() returns NaN, NaN.
 */
public final class BleDeviceChannel implements DeviceChannel {

    private static final Logger LOG = Logger.getLogger(BleDeviceChannel.class.getName());

    private final BlePortConfig config;
    private boolean opened;

    public BleDeviceChannel(BlePortConfig config) {
        this.config = config != null ? config : new BlePortConfig();
        this.opened = false;
    }

    @Override
    public void open() throws DeviceException {
        LOG.log(Level.WARNING, "BLE not yet supported on this platform");
        opened = true;
    }

    @Override
    public void close() {
        opened = false;
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
        String addr = config.getDeviceAddress();
        return addr != null && !addr.isEmpty() ? "BLE " + addr : "BLE (not supported)";
    }
}
