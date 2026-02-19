package org.artisan.controller;

import org.artisan.device.DeviceChannel;
import org.artisan.device.DeviceType;
import org.artisan.device.ModbusPortConfig;
import org.artisan.device.NullDeviceChannel;
import org.artisan.device.RoastSimulatorChannel;
import org.artisan.device.SerialDeviceChannel;
import org.artisan.device.ModbusDeviceChannel;
import org.artisan.device.SerialPortConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for controller.DeviceManager (createChannel, listAvailable).
 */
class DeviceManagerTest {

    @Test
    void createChannel_NONE_returnsNullChannel() {
        DeviceChannel ch = DeviceManager.createChannel(DeviceType.NONE, new SerialPortConfig(), new ModbusPortConfig());
        assertNotNull(ch);
        assertTrue(ch instanceof NullDeviceChannel);
    }

    @Test
    void createChannel_SIMULATOR_returnsSimulatorChannel() {
        DeviceChannel ch = DeviceManager.createChannel(DeviceType.SIMULATOR, null, null);
        assertNotNull(ch);
        assertTrue(ch instanceof RoastSimulatorChannel);
    }

    @Test
    void createChannel_GENERIC_SERIAL_returnsSerialChannel() {
        SerialPortConfig serial = new SerialPortConfig();
        serial.setPortName("COM1");
        DeviceChannel ch = DeviceManager.createChannel(DeviceType.GENERIC_SERIAL, serial, null);
        assertNotNull(ch);
        assertTrue(ch instanceof SerialDeviceChannel);
    }

    @Test
    void createChannel_MODBUS_TCP_returnsModbusChannel() {
        ModbusPortConfig modbus = new ModbusPortConfig();
        modbus.setHost("192.168.1.1");
        modbus.setUseTcp(true);
        DeviceChannel ch = DeviceManager.createChannel(DeviceType.MODBUS_TCP, null, modbus);
        assertNotNull(ch);
        assertTrue(ch instanceof ModbusDeviceChannel);
    }

    @Test
    void listAvailable_excludesNone() {
        List<DeviceType> list = DeviceManager.listAvailable();
        assertNotNull(list);
        assertTrue(list.stream().noneMatch(t -> t == DeviceType.NONE));
    }
}
