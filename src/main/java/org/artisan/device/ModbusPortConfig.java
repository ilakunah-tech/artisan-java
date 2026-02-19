package org.artisan.device;

import java.util.prefs.Preferences;

/**
 * Modbus port configuration (TCP or RTU over serial). Persisted under Preferences "modbus.*".
 */
public final class ModbusPortConfig {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "modbus.";

    public static final int DEFAULT_PORT = 502;
    public static final int DEFAULT_SLAVE_ID = 1;
    public static final int DEFAULT_BT_REGISTER = 1;
    public static final int DEFAULT_ET_REGISTER = 2;
    public static final double DEFAULT_SCALE = 0.1;

    private String host = "";
    private int port = DEFAULT_PORT;
    private boolean useTcp = true;
    private int slaveId = DEFAULT_SLAVE_ID;
    private int btRegister = DEFAULT_BT_REGISTER;
    private int etRegister = DEFAULT_ET_REGISTER;
    private double scale = DEFAULT_SCALE;

    /** TCP host or serial port name depending on useTcp. */
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host != null ? host : "";
    }

    /** TCP port (when useTcp is true). */
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /** true = TCP, false = RTU over serial. */
    public boolean isUseTcp() {
        return useTcp;
    }

    public void setUseTcp(boolean useTcp) {
        this.useTcp = useTcp;
    }

    public int getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(int slaveId) {
        this.slaveId = slaveId;
    }

    /** Holding register for Bean Temperature. */
    public int getBtRegister() {
        return btRegister;
    }

    public void setBtRegister(int btRegister) {
        this.btRegister = btRegister;
    }

    /** Holding register for Environmental Temperature. */
    public int getEtRegister() {
        return etRegister;
    }

    public void setEtRegister(int etRegister) {
        this.etRegister = etRegister;
    }

    /** Multiplier for register values (e.g. 0.1 for tenths of degree). */
    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = Math.max(0.001, Math.min(10.0, scale));
    }

    public static void loadFromPreferences(ModbusPortConfig target) {
        if (target == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        target.setHost(p.get(PREFIX + "host", ""));
        target.setPort(p.getInt(PREFIX + "port", DEFAULT_PORT));
        target.setUseTcp(p.getBoolean(PREFIX + "useTcp", true));
        target.setSlaveId(p.getInt(PREFIX + "slaveId", DEFAULT_SLAVE_ID));
        target.setBtRegister(p.getInt(PREFIX + "btRegister", DEFAULT_BT_REGISTER));
        target.setEtRegister(p.getInt(PREFIX + "etRegister", DEFAULT_ET_REGISTER));
        target.setScale(p.getDouble(PREFIX + "scale", DEFAULT_SCALE));
    }

    public static void saveToPreferences(ModbusPortConfig config) {
        if (config == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.put(PREFIX + "host", config.getHost());
        p.putInt(PREFIX + "port", config.getPort());
        p.putBoolean(PREFIX + "useTcp", config.isUseTcp());
        p.putInt(PREFIX + "slaveId", config.getSlaveId());
        p.putInt(PREFIX + "btRegister", config.getBtRegister());
        p.putInt(PREFIX + "etRegister", config.getEtRegister());
        p.putDouble(PREFIX + "scale", config.getScale());
    }
}
