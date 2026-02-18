package org.artisan.device;

import org.artisan.controller.AppSettings;

/**
 * Optional parameters for Modbus devices (Besca, Diedrich): TCP port, slave ID, register addresses, scale.
 * Used by DeviceManager when creating devices; null means use defaults or load from AppSettings.
 */
public final class DeviceOptions {

    private final Integer tcpPort;
    private final Integer slaveId;
    private final Integer btRegister;
    private final Integer etRegister;
    private final Double scaleFactor;

    public DeviceOptions(Integer tcpPort, Integer slaveId, Integer btRegister, Integer etRegister, Double scaleFactor) {
        this.tcpPort = tcpPort;
        this.slaveId = slaveId;
        this.btRegister = btRegister;
        this.etRegister = etRegister;
        this.scaleFactor = scaleFactor;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public Integer getSlaveId() {
        return slaveId;
    }

    public Integer getBtRegister() {
        return btRegister;
    }

    public Integer getEtRegister() {
        return etRegister;
    }

    public Double getScaleFactor() {
        return scaleFactor;
    }

    /** Build from AppSettings for createDevice when no explicit options given. */
    public static DeviceOptions from(AppSettings s) {
        if (s == null) return null;
        return new DeviceOptions(
            s.getDeviceTcpPort(),
            s.getModbusSlaveId(),
            s.getModbusBtRegister(),
            s.getModbusEtRegister(),
            s.getModbusScaleFactor()
        );
    }
}
