package org.artisan.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.artisan.device.AillioR1Channel;
import org.artisan.device.AillioR1Config;
import org.artisan.device.DeviceChannel;
import org.artisan.device.DeviceType;
import org.artisan.device.ModbusDeviceChannel;
import org.artisan.device.ModbusPortConfig;
import org.artisan.device.NullDeviceChannel;
import org.artisan.device.RoastSimulatorChannel;
import org.artisan.device.SerialDeviceChannel;
import org.artisan.device.SerialPortConfig;
import org.artisan.device.SimulatorConfig;
import org.artisan.device.StubDeviceChannel;

/**
 * Factory and registry for DeviceChannel by DeviceType. Creates the appropriate
 * channel implementation and sets it on CommController.
 */
public final class DeviceManager {

    private DeviceManager() {}

    /**
     * Creates a DeviceChannel for the given type using the provided configs.
     * NONE / unknown → NullDeviceChannel; GENERIC_SERIAL → SerialDeviceChannel;
     * SIMULATOR → RoastSimulatorChannel; MODBUS_* → ModbusDeviceChannel;
     * AILLIO_R1 → AillioR1Channel; other physical devices → stub channels.
     */
    public static DeviceChannel createChannel(DeviceType type,
                                             SerialPortConfig serialCfg,
                                             ModbusPortConfig modbusCfg) {
        if (type == null) {
            return new NullDeviceChannel();
        }
        switch (type) {
            case NONE:
                return new NullDeviceChannel();
            case GENERIC_SERIAL:
                return new SerialDeviceChannel(serialCfg != null ? serialCfg : new SerialPortConfig());
            case SIMULATOR: {
                SimulatorConfig sim = new SimulatorConfig();
                SimulatorConfig.loadFromPreferences(sim);
                return new RoastSimulatorChannel(sim);
            }
            case MODBUS_TCP: {
                ModbusPortConfig m = modbusCfg != null ? modbusCfg : new ModbusPortConfig();
                ModbusPortConfig copy = new ModbusPortConfig();
                copy.setHost(m.getHost());
                copy.setPort(m.getPort());
                copy.setUseTcp(true);
                copy.setSlaveId(m.getSlaveId());
                copy.setBtRegister(m.getBtRegister());
                copy.setEtRegister(m.getEtRegister());
                copy.setScale(m.getScale());
                return new ModbusDeviceChannel(copy);
            }
            case MODBUS_RTU: {
                ModbusPortConfig m = modbusCfg != null ? modbusCfg : new ModbusPortConfig();
                ModbusPortConfig copy = new ModbusPortConfig();
                copy.setHost(m.getHost());
                copy.setPort(m.getPort());
                copy.setUseTcp(false);
                copy.setSlaveId(m.getSlaveId());
                copy.setBtRegister(m.getBtRegister());
                copy.setEtRegister(m.getEtRegister());
                copy.setScale(m.getScale());
                return new ModbusDeviceChannel(copy);
            }
            case AILLIO_R1: {
                AillioR1Config r1 = new AillioR1Config();
                AillioR1Config.loadFromPreferences(r1);
                return new AillioR1Channel(r1);
            }
            case AILLIO_R2:
                return new StubDeviceChannel("Aillio Bullet R2", serialCfg != null ? serialCfg.getPortName() : "");
            case HOTTOP_KN8828B:
                return new StubDeviceChannel("Hottop KN-8828B", serialCfg != null ? serialCfg.getPortName() : "");
            case IKAWA:
                return new StubDeviceChannel("Ikawa", serialCfg != null ? serialCfg.getPortName() : "");
            case KALEIDO_M1:
                return new StubDeviceChannel("Kaleido M1", serialCfg != null ? serialCfg.getPortName() : "");
            case GIESEN:
                return new StubDeviceChannel("Giesen", serialCfg != null ? serialCfg.getPortName() : "");
            case LORING:
                return new StubDeviceChannel("Loring", serialCfg != null ? serialCfg.getPortName() : "");
            case SANTOKER:
                return new StubDeviceChannel("Santoker", serialCfg != null ? serialCfg.getPortName() : "");
            case STRONGHOLD_S7X:
                return new StubDeviceChannel("Stronghold S7X", serialCfg != null ? serialCfg.getPortName() : "");
            case ROEST:
                return new StubDeviceChannel("Roest", serialCfg != null ? serialCfg.getPortName() : "");
            case ACAIA_LUNAR:
                return new StubDeviceChannel("Acacia Lunar", serialCfg != null ? serialCfg.getPortName() : "");
            default:
                return new NullDeviceChannel();
        }
    }

    /**
     * Returns all device types except NONE (for UI dropdown).
     */
    public static List<DeviceType> listAvailable() {
        return Arrays.stream(DeviceType.values())
                .filter(t -> t != DeviceType.NONE)
                .collect(Collectors.toList());
    }
}
