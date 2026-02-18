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
  private static final String KEY_SAMPLING_RATE_MS = "samplingRateMs";
  private static final String KEY_TEMP_UNIT = "tempUnit";
  private static final String KEY_DARK_THEME = "darkTheme";

  private String lastDevicePort = "";
  private int samplingRateMs = 2000;
  private AxisConfig.TemperatureUnit tempUnit = AxisConfig.TemperatureUnit.CELSIUS;
  private boolean darkTheme = true;

  public String getLastDevicePort() {
    return lastDevicePort;
  }

  public void setLastDevicePort(String lastDevicePort) {
    this.lastDevicePort = lastDevicePort != null ? lastDevicePort : "";
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
    prefs.putInt(KEY_SAMPLING_RATE_MS, samplingRateMs);
    prefs.put(KEY_TEMP_UNIT, tempUnit.name());
    prefs.putBoolean(KEY_DARK_THEME, darkTheme);
  }
}
