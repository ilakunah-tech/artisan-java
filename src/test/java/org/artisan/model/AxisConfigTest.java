package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AxisConfigTest {

    @Test
    void defaultConfigHasValidRanges() {
        AxisConfig c = new AxisConfig();
        assertEquals(-30, c.getTimeMinSec());
        assertEquals(600, c.getTimeMaxSec());
        assertEquals(120, c.getTimeTickStepSec());
        assertEquals(0, c.getTempMin());
        assertEquals(275, c.getTempMax());
        assertEquals(50, c.getTempTickStep());
        assertEquals(AxisConfig.TemperatureUnit.CELSIUS, c.getUnit());
    }

    @Test
    void celsiusToFahrenheitConversion() {
        AxisConfig c = new AxisConfig(0, 600, 60, 0, 100, 25, AxisConfig.TemperatureUnit.CELSIUS);
        AxisConfig f = c.toFahrenheit();
        assertEquals(AxisConfig.TemperatureUnit.FAHRENHEIT, f.getUnit());
        assertEquals(0, f.getTimeMinSec());
        assertEquals(600, f.getTimeMaxSec());
        assertEquals(32.0, f.getTempMin(), 1e-6);
        assertEquals(212.0, f.getTempMax(), 1e-6);
        assertEquals(77.0, f.getTempTickStep(), 1e-6);
    }

    @Test
    void fahrenheitToCelsiusConversion() {
        AxisConfig f = new AxisConfig(0, 600, 60, 32, 212, 18, AxisConfig.TemperatureUnit.FAHRENHEIT);
        AxisConfig c = f.toCelsius();
        assertEquals(AxisConfig.TemperatureUnit.CELSIUS, c.getUnit());
        assertEquals(0, c.getTempMin(), 1e-6);
        assertEquals(100, c.getTempMax(), 1e-6);
    }

    @Test
    void toFahrenheitWhenAlreadyFahrenheitReturnsCopy() {
        AxisConfig f = new AxisConfig(0, 100, 60, 100, 300, 50, AxisConfig.TemperatureUnit.FAHRENHEIT);
        AxisConfig f2 = f.toFahrenheit();
        assertEquals(AxisConfig.TemperatureUnit.FAHRENHEIT, f2.getUnit());
        assertEquals(100, f2.getTempMin());
        assertEquals(300, f2.getTempMax());
    }

    @Test
    void toCelsiusWhenAlreadyCelsiusReturnsCopy() {
        AxisConfig c = new AxisConfig(0, 100, 60, 50, 200, 25, AxisConfig.TemperatureUnit.CELSIUS);
        AxisConfig c2 = c.toCelsius();
        assertEquals(AxisConfig.TemperatureUnit.CELSIUS, c2.getUnit());
        assertEquals(50, c2.getTempMin());
        assertEquals(200, c2.getTempMax());
    }

    @Test
    void constructorThrowsWhenTimeMinGreaterThanTimeMax() {
        assertThrows(IllegalArgumentException.class, () ->
                new AxisConfig(100, 50, 60, 0, 275, 50, AxisConfig.TemperatureUnit.CELSIUS));
    }

    @Test
    void constructorThrowsWhenTempMinGreaterThanTempMax() {
        assertThrows(IllegalArgumentException.class, () ->
                new AxisConfig(0, 600, 60, 300, 100, 50, AxisConfig.TemperatureUnit.CELSIUS));
    }

    @Test
    void setTimeMinThrowsWhenGreaterThanTimeMax() {
        AxisConfig c = new AxisConfig();
        assertThrows(IllegalArgumentException.class, () -> c.setTimeMinSec(700));
    }

    @Test
    void setTimeMaxThrowsWhenLessThanTimeMin() {
        AxisConfig c = new AxisConfig();
        assertThrows(IllegalArgumentException.class, () -> c.setTimeMaxSec(-40));
    }

    @Test
    void setTempMinThrowsWhenGreaterThanTempMax() {
        AxisConfig c = new AxisConfig();
        assertThrows(IllegalArgumentException.class, () -> c.setTempMin(300));
    }

    @Test
    void setTempMaxThrowsWhenLessThanTempMin() {
        AxisConfig c = new AxisConfig(0, 600, 60, 50, 250, 50, AxisConfig.TemperatureUnit.CELSIUS);
        assertThrows(IllegalArgumentException.class, () -> c.setTempMax(25));
    }
}
