package org.artisan.device;

import com.fazecast.jSerialComm.SerialPort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Device discovery and factory: scan serial ports, list device types, create DevicePort instances.
 */
public final class DeviceManager {

  private static final List<String> DEVICE_TYPES = Collections.unmodifiableList(Arrays.asList(
      "Hottop",
      "Aillio R1 Bullet",
      "Kaleido",
      "Modbus TCP",
      "Modbus RTU",
      "Generic Serial (FUJI PXR)",
      "Simulator"
  ));

  private DeviceManager() {}

  /**
   * Returns system port names (e.g. "COM3", "/dev/ttyUSB0") from jSerialComm.
   */
  public static List<String> scanSerialPorts() {
    SerialPort[] ports = SerialPort.getCommPorts();
    if (ports == null) return new ArrayList<>();
    List<String> names = new ArrayList<>(ports.length);
    for (SerialPort p : ports) {
      String name = p.getSystemPortName();
      if (name != null && !name.isEmpty()) {
        names.add(name);
      }
    }
    return names;
  }

  /**
   * Returns available device type display names for the device type combo.
   */
  public static List<String> getAvailableDeviceTypes() {
    return new ArrayList<>(DEVICE_TYPES);
  }

  /**
   * Creates a DevicePort for the given type, port, and baud rate.
   * For "Simulator", port and baudRate are ignored.
   *
   * @param type     one of getAvailableDeviceTypes()
   * @param port     system port name (e.g. "COM3"); can be empty for Simulator
   * @param baudRate baud rate (e.g. 9600)
   * @return device instance, or StubDevice if type is unknown
   */
  public static DevicePort createDevice(String type, String port, int baudRate) {
    if (type == null) return new StubDevice();
    switch (type) {
      case "Simulator":
        return new SimulatorDevice(); // implemented in Step 2
      case "Hottop":
        return port != null && !port.isEmpty()
            ? new HottopDevice(port)
            : new StubDevice();
      case "Aillio R1 Bullet":
        return new AillioR1Device();
      case "Kaleido":
        return new KaleidoDevice();
      case "Modbus TCP":
      case "Modbus RTU":
      case "Generic Serial (FUJI PXR)":
        return new StubDevice();
      default:
        return new StubDevice();
    }
  }
}
