package org.artisan.device;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
/**
 * DeviceChannel implementation using jSerialComm. Reads one line per sample;
 * parses "BT,ET" or "BT" CSV format.
 */
public final class SerialDeviceChannel implements DeviceChannel {

    private final SerialPortConfig config;
    private SerialPort port;

    public SerialDeviceChannel(SerialPortConfig config) {
        this.config = config != null ? config : new SerialPortConfig();
        this.port = null;
    }

    @Override
    public void open() throws DeviceException {
        if (port != null && port.isOpen()) {
            return;
        }
        String name = config.getPortName();
        if (name == null || name.isBlank()) {
            throw new DeviceException("Serial port name is empty");
        }
        SerialPort p = SerialPort.getCommPort(name);
        p.setBaudRate(config.getBaudRate());
        p.setNumDataBits(config.getDataBits());
        p.setNumStopBits(config.getStopBits() == 2 ? SerialPort.TWO_STOP_BITS : SerialPort.ONE_STOP_BIT);
        int par = config.getParity();
        if (par == 1) {
            p.setParity(SerialPort.ODD_PARITY);
        } else if (par == 2) {
            p.setParity(SerialPort.EVEN_PARITY);
        } else {
            p.setParity(SerialPort.NO_PARITY);
        }
        int to = Math.max(1, config.getReadTimeoutMs());
        p.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, to, 0);
        if (!p.openPort()) {
            throw new DeviceException("Failed to open serial port: " + name + " (error " + p.getLastErrorCode() + ")");
        }
        this.port = p;
    }

    @Override
    public void close() {
        if (port == null) return;
        try {
            if (port.isOpen()) {
                port.closePort();
            }
        } finally {
            port = null;
        }
    }

    @Override
    public boolean isOpen() {
        return port != null && port.isOpen();
    }

    @Override
    public SampleResult read() throws DeviceException {
        if (!isOpen()) {
            throw new DeviceException("Serial port is not open");
        }
        String line = readLine();
        if (line == null || line.isBlank()) {
            throw new DeviceException("No data received (timeout or empty line)");
        }
        double bt;
        double et = Double.NaN;
        String[] parts = line.trim().split("\\s*,\\s*");
        if (parts.length >= 2) {
            try {
                bt = Double.parseDouble(parts[0].trim());
                et = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException e) {
                throw new DeviceException("Parse error: expected BT,ET numbers: " + line, e);
            }
        } else if (parts.length == 1) {
            try {
                bt = Double.parseDouble(parts[0].trim());
            } catch (NumberFormatException e) {
                throw new DeviceException("Parse error: expected BT number: " + line, e);
            }
        } else {
            throw new DeviceException("Parse error: expected BT or BT,ET: " + line);
        }
        return SampleResult.now(bt, et);
    }

    @Override
    public String getDescription() {
        String name = config.getPortName();
        return name != null && !name.isEmpty() ? "Serial " + name : "Serial";
    }

    private String readLine() throws DeviceException {
        InputStream in = port.getInputStream();
        StringBuilder sb = new StringBuilder();
        byte[] one = new byte[1];
        try {
            while (true) {
                int n = in.read(one);
                if (n <= 0) {
                    if (sb.length() > 0) return sb.toString();
                    return null;
                }
                char c = (char) (one[0] & 0xFF);
                if (c == '\r' || c == '\n') {
                    break;
                }
                sb.append(c);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new DeviceException("Serial read failed", e);
        }
    }
}
