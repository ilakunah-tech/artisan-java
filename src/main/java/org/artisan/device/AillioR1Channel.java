package org.artisan.device;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DeviceChannel for Aillio Bullet R1 via USB HID (VID=0x0483, PID=0x5741).
 * Sends READ_DATA_REQUEST (0x30), parses BT/ET from 64-byte response.
 */
public final class AillioR1Channel implements DeviceChannel {

    private static final Logger LOG = Logger.getLogger(AillioR1Channel.class.getName());
    private static final byte READ_DATA_REQUEST = 0x30;

    private final AillioR1Config config;
    private HidServices hidServices;
    private HidDevice hidDevice;

    public AillioR1Channel(AillioR1Config config) {
        this.config = config != null ? config : new AillioR1Config();
    }

    @Override
    public void open() throws DeviceException {
        if (hidDevice != null && hidDevice.isOpen()) {
            return;
        }
        close();
        HidServicesSpecification spec = new HidServicesSpecification();
        spec.setAutoStart(false);
        hidServices = HidManager.getHidServices(spec);
        hidServices.start();
        List<HidDevice> devices = hidServices.getAttachedHidDevices();
        int vid = config.getVid();
        int pid = config.getPid();
        for (HidDevice d : devices) {
            if (d.getVendorId() == vid && d.getProductId() == pid) {
                try {
                    d.open();
                    this.hidDevice = d;
                    return;
                } catch (Exception e) {
                    throw new DeviceException("Failed to open Aillio R1 HID: " + e.getMessage(), e);
                }
            }
        }
        throw new DeviceException("Aillio R1 not found (VID=0x" + Integer.toHexString(vid) + " PID=0x" + Integer.toHexString(pid) + ")");
    }

    @Override
    public void close() {
        if (hidDevice != null) {
            try {
                if (hidDevice.isOpen()) {
                    hidDevice.close();
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error closing Aillio R1: {0}", e.getMessage());
            }
            hidDevice = null;
        }
        if (hidServices != null) {
            try {
                hidServices.shutdown();
            } catch (Exception ignored) {
            }
            hidServices = null;
        }
    }

    @Override
    public boolean isOpen() {
        return hidDevice != null && hidDevice.isOpen();
    }

    @Override
    public SampleResult read() throws DeviceException {
        if (!isOpen()) {
            throw new DeviceException("Aillio R1 is not open");
        }
        byte[] cmd = new byte[] { READ_DATA_REQUEST, 0x01, 0x00, 0x00 };
        int written = hidDevice.write(cmd, cmd.length, (byte) 0);
        if (written < 0) {
            throw new DeviceException("Aillio R1 write failed");
        }
        byte[] buf = new byte[64];
        int n = hidDevice.read(buf, 500);
        if (n < 12) {
            throw new DeviceException("Aillio R1 read too short: " + n + " bytes");
        }
        int btRaw = (buf[9] & 0xFF) | ((buf[10] & 0xFF) << 8);
        int etRaw = (buf[11] & 0xFF) | ((buf[12] & 0xFF) << 8);
        double bt = btRaw / 10.0;
        double et = etRaw / 10.0;
        return SampleResult.now(bt, et);
    }

    @Override
    public String getDescription() {
        return "Aillio Bullet R1";
    }
}
