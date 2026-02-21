package org.artisan.controller;

import java.util.prefs.Preferences;

import org.artisan.model.AxisConfig;

/**
 * Simple settings DTO persisted via java.util.prefs.Preferences.
 * Ported from Python QSettings (last device, sampling rate, theme).
 */
public final class AppSettings {

  private static final String NODE = "org/artisan/artisan-java";
  private static final String KEY_LAST_DEVICE = "lastDevicePort";
  private static final String KEY_DEVICE_TYPE = "deviceType";
  private static final String KEY_BAUD_RATE = "baudRate";
  private static final String KEY_SAMPLING_RATE_MS = "samplingRateMs";
  private static final String KEY_TEMP_UNIT = "tempUnit";
  private static final String KEY_DARK_THEME = "darkTheme";
  private static final String KEY_DEVICE_TCP_PORT = "deviceTcpPort";
  private static final String KEY_MODBUS_SLAVE_ID = "modbusSlaveId";
  private static final String KEY_MODBUS_BT_REGISTER = "modbusBtRegister";
  private static final String KEY_MODBUS_ET_REGISTER = "modbusEtRegister";
  private static final String KEY_MODBUS_SCALE_FACTOR = "modbusScaleFactor";
  private static final String KEY_TCP_HOST = "tcpHost";
  private static final String KEY_AXIS_AUTO_SCALE_FLOOR = "axis.autoScaleFloor";
  private static final String KEY_AXIS_AUTO_SCALE_Y = "axis.autoScaleY";
  private static final String KEY_AXIS_TEMP_MIN = "axis.tempMin";
  private static final String KEY_AXIS_TEMP_MAX = "axis.tempMax";
  private static final String KEY_AXIS_ROR_MIN = "axis.rorMin";
  private static final String KEY_AXIS_ROR_MAX = "axis.rorMax";
  private static final String KEY_ROAST_AUTO_CHARGE_DROP = "roast.autoChargeDrop";
  private static final String KEY_ROAST_AUTO_CHARGE_SUSTAIN = "roast.autoChargeSustain";
  private static final String KEY_ROAST_PRE_ROAST_TIMEOUT = "roast.preRoastTimeout";

  private String lastDevicePort = "";
  private String deviceType = "Simulator";
  private int baudRate = 9600;
  private int samplingRateMs = 2000;
  private AxisConfig.TemperatureUnit tempUnit = AxisConfig.TemperatureUnit.CELSIUS;
  private boolean darkTheme = true;
  private int deviceTcpPort = 502;
  private int modbusSlaveId = 1;
  private int modbusBtRegister = 1;
  private int modbusEtRegister = 2;
  private double modbusScaleFactor = 10.0;
  private String tcpHost = "localhost";
  private double axisAutoScaleFloor = 50.0;
  private boolean axisAutoScaleY = true;
  private double axisTempMin = 0.0;
  private double axisTempMax = 275.0;
  private double axisRorMin = -20.0;
  private double axisRorMax = 50.0;
  private double autoChargeDrop = 5.0;
  private double autoChargeSustain = 20.0;
  private double preRoastTimeout = 300.0;

  public String getLastDevicePort() {
    return lastDevicePort;
  }

  public void setLastDevicePort(String lastDevicePort) {
    this.lastDevicePort = lastDevicePort != null ? lastDevicePort : "";
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType != null ? deviceType : "Simulator";
  }

  public int getBaudRate() {
    return baudRate;
  }

  public void setBaudRate(int baudRate) {
    this.baudRate = Math.max(300, Math.min(115200, baudRate));
  }

  public int getSamplingRateMs() {
    return samplingRateMs;
  }

  public void setSamplingRateMs(int samplingRateMs) {
    this.samplingRateMs = Math.max(100, samplingRateMs);
  }

  public AxisConfig.TemperatureUnit getTempUnit() {
    return tempUnit;
  }

  public void setTempUnit(AxisConfig.TemperatureUnit tempUnit) {
    this.tempUnit = tempUnit != null ? tempUnit : AxisConfig.TemperatureUnit.CELSIUS;
  }

  public boolean isDarkTheme() {
    return darkTheme;
  }

  public void setDarkTheme(boolean darkTheme) {
    this.darkTheme = darkTheme;
  }

  public int getDeviceTcpPort() {
    return deviceTcpPort;
  }

  public void setDeviceTcpPort(int deviceTcpPort) {
    this.deviceTcpPort = Math.max(1, Math.min(65535, deviceTcpPort));
  }

  public int getModbusSlaveId() {
    return modbusSlaveId;
  }

  public void setModbusSlaveId(int modbusSlaveId) {
    this.modbusSlaveId = Math.max(1, Math.min(247, modbusSlaveId));
  }

  public int getModbusBtRegister() {
    return modbusBtRegister;
  }

  public void setModbusBtRegister(int modbusBtRegister) {
    this.modbusBtRegister = Math.max(0, modbusBtRegister);
  }

  public int getModbusEtRegister() {
    return modbusEtRegister;
  }

  public void setModbusEtRegister(int modbusEtRegister) {
    this.modbusEtRegister = Math.max(0, modbusEtRegister);
  }

  public double getModbusScaleFactor() {
    return modbusScaleFactor;
  }

  public void setModbusScaleFactor(double modbusScaleFactor) {
    this.modbusScaleFactor = modbusScaleFactor > 0 ? modbusScaleFactor : 10.0;
  }

  public String getTcpHost() {
    return tcpHost;
  }

  public void setTcpHost(String tcpHost) {
    this.tcpHost = tcpHost != null ? tcpHost : "localhost";
  }

  public double getAxisAutoScaleFloor() {
    return axisAutoScaleFloor;
  }

  public void setAxisAutoScaleFloor(double axisAutoScaleFloor) {
    this.axisAutoScaleFloor = axisAutoScaleFloor;
  }

  public boolean isAxisAutoScaleY() {
    return axisAutoScaleY;
  }

  public void setAxisAutoScaleY(boolean axisAutoScaleY) {
    this.axisAutoScaleY = axisAutoScaleY;
  }

  public double getAxisTempMin() {
    return axisTempMin;
  }

  public void setAxisTempMin(double axisTempMin) {
    this.axisTempMin = axisTempMin;
  }

  public double getAxisTempMax() {
    return axisTempMax;
  }

  public void setAxisTempMax(double axisTempMax) {
    this.axisTempMax = axisTempMax;
  }

  public double getAxisRorMin() {
    return axisRorMin;
  }

  public void setAxisRorMin(double axisRorMin) {
    this.axisRorMin = axisRorMin;
  }

  public double getAxisRorMax() {
    return axisRorMax;
  }

  public void setAxisRorMax(double axisRorMax) {
    this.axisRorMax = axisRorMax;
  }

  public double getAutoChargeDrop() {
    return autoChargeDrop;
  }

  public void setAutoChargeDrop(double autoChargeDrop) {
    this.autoChargeDrop = autoChargeDrop;
  }

  public double getAutoChargeSustain() {
    return autoChargeSustain;
  }

  public void setAutoChargeSustain(double autoChargeSustain) {
    this.autoChargeSustain = autoChargeSustain;
  }

  public double getPreRoastTimeout() {
    return preRoastTimeout;
  }

  public void setPreRoastTimeout(double preRoastTimeout) {
    this.preRoastTimeout = preRoastTimeout;
  }

  /** Loads settings from Preferences. */
  public static AppSettings load() {
    AppSettings s = new AppSettings();
    Preferences prefs = Preferences.userRoot().node(NODE);
    s.lastDevicePort = prefs.get(KEY_LAST_DEVICE, "");
    s.deviceType = prefs.get(KEY_DEVICE_TYPE, "Simulator");
    s.baudRate = prefs.getInt(KEY_BAUD_RATE, 9600);
    s.samplingRateMs = prefs.getInt(KEY_SAMPLING_RATE_MS, 2000);
    String unit = prefs.get(KEY_TEMP_UNIT, "CELSIUS");
    try {
      s.tempUnit = AxisConfig.TemperatureUnit.valueOf(unit);
    } catch (Exception e) {
      s.tempUnit = AxisConfig.TemperatureUnit.CELSIUS;
    }
    s.darkTheme = prefs.getBoolean(KEY_DARK_THEME, true);
    s.deviceTcpPort = prefs.getInt(KEY_DEVICE_TCP_PORT, 502);
    s.modbusSlaveId = prefs.getInt(KEY_MODBUS_SLAVE_ID, 1);
    s.modbusBtRegister = prefs.getInt(KEY_MODBUS_BT_REGISTER, 1);
    s.modbusEtRegister = prefs.getInt(KEY_MODBUS_ET_REGISTER, 2);
    s.modbusScaleFactor = prefs.getDouble(KEY_MODBUS_SCALE_FACTOR, 10.0);
    s.tcpHost = prefs.get(KEY_TCP_HOST, "localhost");
    s.axisAutoScaleFloor = prefs.getDouble(KEY_AXIS_AUTO_SCALE_FLOOR, 50.0);
    s.axisAutoScaleY = prefs.getBoolean(KEY_AXIS_AUTO_SCALE_Y, true);
    s.axisTempMin = prefs.getDouble(KEY_AXIS_TEMP_MIN, 0.0);
    s.axisTempMax = prefs.getDouble(KEY_AXIS_TEMP_MAX, 275.0);
    s.axisRorMin = prefs.getDouble(KEY_AXIS_ROR_MIN, -20.0);
    s.axisRorMax = prefs.getDouble(KEY_AXIS_ROR_MAX, 50.0);
    s.autoChargeDrop = prefs.getDouble(KEY_ROAST_AUTO_CHARGE_DROP, 5.0);
    s.autoChargeSustain = prefs.getDouble(KEY_ROAST_AUTO_CHARGE_SUSTAIN, 20.0);
    s.preRoastTimeout = prefs.getDouble(KEY_ROAST_PRE_ROAST_TIMEOUT, 300.0);
    return s;
  }

  /** Saves this instance to Preferences. */
  public void save() {
    Preferences prefs = Preferences.userRoot().node(NODE);
    prefs.put(KEY_LAST_DEVICE, lastDevicePort);
    prefs.put(KEY_DEVICE_TYPE, deviceType);
    prefs.putInt(KEY_BAUD_RATE, baudRate);
    prefs.putInt(KEY_SAMPLING_RATE_MS, samplingRateMs);
    prefs.put(KEY_TEMP_UNIT, tempUnit.name());
    prefs.putBoolean(KEY_DARK_THEME, darkTheme);
    prefs.putInt(KEY_DEVICE_TCP_PORT, deviceTcpPort);
    prefs.putInt(KEY_MODBUS_SLAVE_ID, modbusSlaveId);
    prefs.putInt(KEY_MODBUS_BT_REGISTER, modbusBtRegister);
    prefs.putInt(KEY_MODBUS_ET_REGISTER, modbusEtRegister);
    prefs.putDouble(KEY_MODBUS_SCALE_FACTOR, modbusScaleFactor);
    prefs.put(KEY_TCP_HOST, tcpHost);
    prefs.putDouble(KEY_AXIS_AUTO_SCALE_FLOOR, axisAutoScaleFloor);
    prefs.putBoolean(KEY_AXIS_AUTO_SCALE_Y, axisAutoScaleY);
    prefs.putDouble(KEY_AXIS_TEMP_MIN, axisTempMin);
    prefs.putDouble(KEY_AXIS_TEMP_MAX, axisTempMax);
    prefs.putDouble(KEY_AXIS_ROR_MIN, axisRorMin);
    prefs.putDouble(KEY_AXIS_ROR_MAX, axisRorMax);
    prefs.putDouble(KEY_ROAST_AUTO_CHARGE_DROP, autoChargeDrop);
    prefs.putDouble(KEY_ROAST_AUTO_CHARGE_SUSTAIN, autoChargeSustain);
    prefs.putDouble(KEY_ROAST_PRE_ROAST_TIMEOUT, preRoastTimeout);
  }
}
