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

  private String lastDevicePort = "";
  private String deviceType = "Simulator";
  private int baudRate = 9600;
  private int samplingRateMs = 2000;
  private AxisConfig.TemperatureUnit tempUnit = AxisConfig.TemperatureUnit.CELSIUS;
  private boolean darkTheme = true;

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
  }
}
