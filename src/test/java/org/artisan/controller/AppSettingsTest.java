package org.artisan.controller;

import org.artisan.model.AxisConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppSettingsTest {

  @Test
  void saveThenLoadRoundTripPreservesAllFields() {
    AppSettings saved = new AppSettings();
    saved.setLastDevicePort("COM3");
    saved.setSamplingRateMs(1000);
    saved.setTempUnit(AxisConfig.TemperatureUnit.FAHRENHEIT);
    saved.setDarkTheme(false);
    saved.save();

    AppSettings loaded = AppSettings.load();
    assertEquals("COM3", loaded.getLastDevicePort());
    assertEquals(1000, loaded.getSamplingRateMs());
    assertEquals(AxisConfig.TemperatureUnit.FAHRENHEIT, loaded.getTempUnit());
    assertFalse(loaded.isDarkTheme());
  }

  @Test
  void loadReturnsValidSettings() {
    AppSettings loaded = AppSettings.load();
    assertEquals(loaded.getLastDevicePort() != null, true);
    assertTrue(loaded.getSamplingRateMs() >= 100);
    assertTrue(loaded.getTempUnit() != null);
  }
}
