package org.artisan.device;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * DeviceManager: scanSerialPorts returns list (may be empty, not null);
 * createDevice("Simulator", "", 9600) returns SimulatorDevice instance.
 */
class DeviceManagerTest {

  @Test
  void scanSerialPortsReturnsListNotNull() {
    List<String> ports = DeviceManager.scanSerialPorts();
    assertNotNull(ports);
  }

  @Test
  void createDeviceSimulatorReturnsSimulatorDevice() {
    DevicePort device = DeviceManager.createDevice("Simulator", "", 9600);
    assertNotNull(device);
    assertTrue(device instanceof SimulatorDevice);
  }

  @Test
  void createDeviceSimulatorSameInstanceType() {
    DevicePort d1 = DeviceManager.createDevice("Simulator", null, 9600);
    DevicePort d2 = DeviceManager.createDevice("Simulator", "COM1", 115200);
    assertTrue(d1 instanceof SimulatorDevice);
    assertTrue(d2 instanceof SimulatorDevice);
  }

  @Test
  void createDeviceBescaTcpReturnsBescaDevice() {
    DevicePort device = DeviceManager.createDevice("Besca (Modbus TCP)", "192.168.1.1", 502);
    assertNotNull(device);
    assertTrue(device instanceof BescaDevice);
  }

  @Test
  void createDeviceDiedrichRtuReturnsDiedrichDevice() {
    DevicePort device = DeviceManager.createDevice("Diedrich (Modbus RTU)", "COM3", 9600);
    assertNotNull(device);
    assertTrue(device instanceof DiedrichDevice);
  }
}
