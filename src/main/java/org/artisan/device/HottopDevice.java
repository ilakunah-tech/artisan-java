package org.artisan.device;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;

// Ported from hottop.py — original Python lines: 34–184

/**
 * Hottop 2k+ roaster device (serial). Message format: 36 bytes, header 0xA5 0x96.
 * ET = (bytes[22]<<8|bytes[23])/10.0, BT = (bytes[24]<<8|bytes[25])/10.0.
 * Timeout 500ms; on timeout returns last known values. Reconnect after 3 read failures.
 */
public class HottopDevice extends AbstractCommPort {

    /** Message header (2 bytes). */
    private static final byte[] HEADER = {(byte) 0xA5, (byte) 0x96};

    private static final int MESSAGE_LENGTH = 36;
    private static final int READ_TIMEOUT_MS = 500;
    private static final int FAILURES_BEFORE_RECONNECT = 3;
    private static final int RECONNECT_DELAY_MS = 2000;

    private volatile double lastET = 0.0;
    private volatile double lastBT = 0.0;
    private int consecutiveFailures = 0;

    public HottopDevice(String portName) {
        super(portName);
    }

    @Override
    protected void configurePort(SerialPort port) {
        port.setBaudRate(BAUD_RATE);
        port.setNumDataBits(DATA_BITS);
        port.setNumStopBits(STOP_BITS);
        port.setParity(SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, READ_TIMEOUT_MS, READ_TIMEOUT_MS);
    }

    @Override
    protected double[] readTemperaturesImpl() {
        SerialPort port = getSerialPort();
        if (port == null) {
            return new double[]{lastET, lastBT};
        }
        byte[] message = readMessage(port);
        if (message == null || message.length != MESSAGE_LENGTH || message[0] != HEADER[0] || message[1] != HEADER[1]) {
            consecutiveFailures++;
            if (consecutiveFailures >= FAILURES_BEFORE_RECONNECT) {
                try {
                    disconnect();
                    Thread.sleep(RECONNECT_DELAY_MS);
                    connect();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                consecutiveFailures = 0;
            }
            return new double[]{lastET, lastBT};
        }
        consecutiveFailures = 0;
        double et = ((message[22] & 0xFF) << 8 | (message[23] & 0xFF)) / 10.0;
        double bt = ((message[24] & 0xFF) << 8 | (message[25] & 0xFF)) / 10.0;
        lastET = et;
        lastBT = bt;
        return new double[]{et, bt};
    }

    /**
     * Reads one 36-byte message, syncing on header 0xA5 0x96.
     */
    private byte[] readMessage(SerialPort port) {
        try (InputStream in = port.getInputStream()) {
            // Sync to first header byte
            while (true) {
                int b = in.read();
                if (b < 0) return null;
                if (b == (HEADER[0] & 0xFF)) break;
            }
            int second = in.read();
            if (second < 0 || second != (HEADER[1] & 0xFF)) return null;
            byte[] msg = new byte[MESSAGE_LENGTH];
            msg[0] = HEADER[0];
            msg[1] = HEADER[1];
            int need = MESSAGE_LENGTH - 2;
            int off = 2;
            while (need > 0) {
                int n = in.read(msg, off, need);
                if (n <= 0) return null;
                off += n;
                need -= n;
            }
            return msg;
        } catch (IOException e) {
            throw new CommException("Hottop read failed", e);
        }
    }
}
