package org.artisan.device;

import com.fazecast.jSerialComm.SerialPort;

import org.artisan.controller.AppSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Device discovery and factory: scan serial ports, list device types, create DevicePort instances.
 */
public final class DeviceManager {

  public static final String BESCA_TCP = "Besca (Modbus TCP)";
  public static final String BESCA_RTU = "Besca (Modbus RTU/USB)";
  public static final String DIEDRICH_RTU = "Diedrich (Modbus RTU)";

  private static final List<String> DEVICE_TYPES = Collections.unmodifiableList(Arrays.asList(
      "Hottop",
      "Aillio R1 Bullet",
      "Kaleido",
      BESCA_TCP,
      BESCA_RTU,
      DIEDRICH_RTU,
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
   * Uses AppSettings for Modbus options (TCP port, slave ID, registers, scale) when options is null.
   *
   * @param type     one of getAvailableDeviceTypes()
   * @param port     system port name or host (e.g. "COM3", "192.168.1.1"); can be empty for Simulator
   * @param baudRate baud rate (e.g. 9600) or TCP port (e.g. 502) for Besca TCP
   * @param options  optional Modbus settings; null to use AppSettings
   * @return device instance, or StubDevice if type is unknown
   */
  public static DevicePort createDevice(String type, String port, int baudRate, DeviceOptions options) {
    if (type == null) return new StubDevice();
    DeviceOptions opts = options != null ? options : DeviceOptions.from(AppSettings.load());
    switch (type) {
      case "Simulator":
        return new SimulatorDevice();
      case "Hottop":
        return port != null && !port.isEmpty()
            ? new HottopDevice(port)
            : new StubDevice();
      case "Aillio R1 Bullet":
        return new AillioR1Device();
      case "Kaleido":
        return new KaleidoDevice();
      case BESCA_TCP: {
        BescaDevice d = BescaDevice.tcpMode(port != null && !port.isEmpty() ? port : "192.168.1.1");
        if (opts != null) {
          if (opts.getTcpPort() != null) d = d.withTcpPort(opts.getTcpPort());
          if (opts.getSlaveId() != null) d = d.withSlaveId(opts.getSlaveId());
          if (opts.getBtRegister() != null) d = d.withBtRegister(opts.getBtRegister());
          if (opts.getEtRegister() != null) d = d.withEtRegister(opts.getEtRegister());
          if (opts.getScaleFactor() != null) d = d.withScaleFactor(opts.getScaleFactor());
        }
        return d;
      }
      case BESCA_RTU: {
        BescaDevice d = BescaDevice.rtuMode(port != null ? port : "");
        if (opts != null) {
          if (opts.getSlaveId() != null) d = d.withSlaveId(opts.getSlaveId());
          if (opts.getBtRegister() != null) d = d.withBtRegister(opts.getBtRegister());
          if (opts.getEtRegister() != null) d = d.withEtRegister(opts.getEtRegister());
          if (opts.getScaleFactor() != null) d = d.withScaleFactor(opts.getScaleFactor());
          if (baudRate > 0) d = d.withBaudRate(baudRate);
        }
        return d;
      }
      case DIEDRICH_RTU: {
        DiedrichDevice d = DiedrichDevice.rtuMode(port != null ? port : "");
        if (opts != null) {
          if (opts.getSlaveId() != null) d = d.withSlaveId(opts.getSlaveId());
          if (opts.getBtRegister() != null) d = d.withBtRegister(opts.getBtRegister());
          if (opts.getEtRegister() != null) d = d.withEtRegister(opts.getEtRegister());
          if (opts.getScaleFactor() != null) d = d.withScaleFactor(opts.getScaleFactor());
          if (baudRate > 0) d = d.withBaudRate(baudRate);
        } else if (baudRate > 0) {
          d = d.withBaudRate(baudRate);
        }
        return d;
      }
      case "Modbus TCP":
      case "Modbus RTU":
      case "Generic Serial (FUJI PXR)":
        return new StubDevice();
      default:
        return new StubDevice();
    }
  }

  /**
   * Creates a DevicePort using saved AppSettings for optional Modbus parameters.
   */
  public static DevicePort createDevice(String type, String port, int baudRate) {
    return createDevice(type, port, baudRate, null);
  }
}
