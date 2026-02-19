package org.artisan.device;

import java.util.prefs.Preferences;

/**
 * Serial port configuration. Persisted under Preferences "serial.*".
 */
public final class SerialPortConfig {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "serial.";

    public static final int DEFAULT_BAUD_RATE = 115200;
    public static final int DEFAULT_DATA_BITS = 8;
    public static final int DEFAULT_STOP_BITS = 1;
    public static final int DEFAULT_PARITY = 0;
    public static final int DEFAULT_READ_TIMEOUT_MS = 1000;
    public static final String DEFAULT_LINE_ENDING = "\r\n";

    private String portName = "";
    private int baudRate = DEFAULT_BAUD_RATE;
    private int dataBits = DEFAULT_DATA_BITS;
    private int stopBits = DEFAULT_STOP_BITS;
    private int parity = DEFAULT_PARITY;
    private int readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
    private String lineEnding = DEFAULT_LINE_ENDING;

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName != null ? portName : "";
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = Math.max(5, Math.min(8, dataBits));
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = Math.max(100, Math.min(5000, readTimeoutMs));
    }

    public String getLineEnding() {
        return lineEnding;
    }

    public void setLineEnding(String lineEnding) {
        this.lineEnding = lineEnding != null ? lineEnding : DEFAULT_LINE_ENDING;
    }

    public static void loadFromPreferences(SerialPortConfig target) {
        if (target == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        target.setPortName(p.get(PREFIX + "portName", ""));
        target.setBaudRate(p.getInt(PREFIX + "baudRate", DEFAULT_BAUD_RATE));
        target.setDataBits(p.getInt(PREFIX + "dataBits", DEFAULT_DATA_BITS));
        target.setStopBits(p.getInt(PREFIX + "stopBits", DEFAULT_STOP_BITS));
        target.setParity(p.getInt(PREFIX + "parity", DEFAULT_PARITY));
        target.setReadTimeoutMs(p.getInt(PREFIX + "readTimeoutMs", DEFAULT_READ_TIMEOUT_MS));
        target.setLineEnding(p.get(PREFIX + "lineEnding", DEFAULT_LINE_ENDING));
    }

    public static void saveToPreferences(SerialPortConfig config) {
        if (config == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.put(PREFIX + "portName", config.getPortName());
        p.putInt(PREFIX + "baudRate", config.getBaudRate());
        p.putInt(PREFIX + "dataBits", config.getDataBits());
        p.putInt(PREFIX + "stopBits", config.getStopBits());
        p.putInt(PREFIX + "parity", config.getParity());
        p.putInt(PREFIX + "readTimeoutMs", config.getReadTimeoutMs());
        p.put(PREFIX + "lineEnding", config.getLineEnding());
    }
}
