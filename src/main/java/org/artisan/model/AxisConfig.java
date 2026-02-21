package org.artisan.model;

import java.util.prefs.Preferences;

/**
 * Chart axis configuration (time X, temperature Y).
 * Mirrors axis settings from Python artisanlib (axis dialog / canvas defaults).
 */
public final class AxisConfig {

    public enum TemperatureUnit {
        CELSIUS,
        FAHRENHEIT
    }

    /** Time axis: min (seconds). */
    private double timeMinSec;
    /** Time axis: max (seconds). */
    private double timeMaxSec;
    /** Time axis: major tick step (seconds), e.g. 120 = 2 min. */
    private double timeTickStepSec;
    /** Temperature axis: min (in current unit). */
    private double tempMin;
    /** Temperature axis: max (in current unit). */
    private double tempMax;
    /** Temperature axis: major tick step (in current unit). */
    private double tempTickStep;
    /** Current temperature unit. */
    private TemperatureUnit unit;
    /** Auto-fit temperature axis from data. */
    private boolean autoScaleY = true;
    /** Auto-fit lower bound floor (°C) for temp axis. */
    private double tempAutoScaleFloor = 50.0;
    /** Auto-fit RoR axis from data. */
    private boolean autoScaleY2 = false;
    /** RoR (right) axis min (degrees/min). */
    private double rorMin = DEFAULT_MIN_ROR;
    /** RoR (right) axis max (degrees/min). */
    private double rorMax = DEFAULT_MAX_ROR;

    /** Default min RoR for axis (degrees/min). */
    public static final double DEFAULT_MIN_ROR = -5.0;
    /** Default max RoR for axis (degrees/min). */
    public static final double DEFAULT_MAX_ROR = 30.0;

    public AxisConfig() {
        this(0, 900, 60, 0, 300, 25, TemperatureUnit.CELSIUS);
    }

    /**
     * @param timeMinSec   X axis min (seconds)
     * @param timeMaxSec   X axis max (seconds)
     * @param timeTickStep X axis tick step (seconds)
     * @param tempMin      Y axis min (in given unit)
     * @param tempMax      Y axis max (in given unit)
     * @param tempTickStep Y axis tick step (in given unit)
     * @param unit         temperature unit
     * @throws IllegalArgumentException if timeMin > timeMax or tempMin > tempMax
     */
    public AxisConfig(double timeMinSec, double timeMaxSec, double timeTickStepSec,
                      double tempMin, double tempMax, double tempTickStep,
                      TemperatureUnit unit) {
        if (timeMinSec > timeMaxSec) {
            throw new IllegalArgumentException("timeMin > timeMax: " + timeMinSec + " > " + timeMaxSec);
        }
        if (tempMin > tempMax) {
            throw new IllegalArgumentException("tempMin > tempMax: " + tempMin + " > " + tempMax);
        }
        this.timeMinSec = timeMinSec;
        this.timeMaxSec = timeMaxSec;
        this.timeTickStepSec = timeTickStepSec;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.tempTickStep = tempTickStep;
        this.unit = unit != null ? unit : TemperatureUnit.CELSIUS;
    }

    public double getTimeMinSec() { return timeMinSec; }
    public void setTimeMinSec(double timeMinSec) {
        if (timeMinSec > this.timeMaxSec) {
            throw new IllegalArgumentException("timeMin > timeMax: " + timeMinSec + " > " + timeMaxSec);
        }
        this.timeMinSec = timeMinSec;
    }

    public double getTimeMaxSec() { return timeMaxSec; }
    public void setTimeMaxSec(double timeMaxSec) {
        if (this.timeMinSec > timeMaxSec) {
            throw new IllegalArgumentException("timeMin > timeMax: " + timeMinSec + " > " + timeMaxSec);
        }
        this.timeMaxSec = timeMaxSec;
    }

    public double getTimeTickStepSec() { return timeTickStepSec; }
    public void setTimeTickStepSec(double timeTickStepSec) { this.timeTickStepSec = timeTickStepSec; }

    public double getTempMin() { return tempMin; }
    public void setTempMin(double tempMin) {
        if (tempMin > this.tempMax) {
            throw new IllegalArgumentException("tempMin > tempMax: " + tempMin + " > " + tempMax);
        }
        this.tempMin = tempMin;
    }

    public double getTempMax() { return tempMax; }
    public void setTempMax(double tempMax) {
        if (this.tempMin > tempMax) {
            throw new IllegalArgumentException("tempMin > tempMax: " + tempMin + " > " + tempMax);
        }
        this.tempMax = tempMax;
    }

    public double getTempTickStep() { return tempTickStep; }
    public void setTempTickStep(double tempTickStep) { this.tempTickStep = tempTickStep; }

    public TemperatureUnit getUnit() { return unit; }
    public void setUnit(TemperatureUnit unit) { this.unit = unit != null ? unit : TemperatureUnit.CELSIUS; }

    public boolean isAutoScaleY() { return autoScaleY; }
    public void setAutoScaleY(boolean autoScaleY) { this.autoScaleY = autoScaleY; }

    public double getTempAutoScaleFloor() { return tempAutoScaleFloor; }
    public void setTempAutoScaleFloor(double v) { this.tempAutoScaleFloor = v; }

    public boolean isAutoScaleY2() { return autoScaleY2; }
    public void setAutoScaleY2(boolean autoScaleY2) { this.autoScaleY2 = autoScaleY2; }

    public double getRorMin() { return rorMin; }
    public void setRorMin(double rorMin) { this.rorMin = rorMin; }

    public double getRorMax() { return rorMax; }
    public void setRorMax(double rorMax) { this.rorMax = rorMax; }

    /** Returns "°C" or "°F" for display. */
    public String getTempUnitString() {
        return unit == TemperatureUnit.FAHRENHEIT ? "°F" : "°C";
    }

    /** Converts temperature from Celsius to Fahrenheit. */
    public static double celsiusToFahrenheit(double c) {
        return c * 9.0 / 5.0 + 32.0;
    }

    /** Converts temperature from Fahrenheit to Celsius. */
    public static double fahrenheitToCelsius(double f) {
        return (f - 32.0) * 5.0 / 9.0;
    }

    /**
     * Returns a new AxisConfig with the same limits and steps converted to Fahrenheit.
     * If already in Fahrenheit, returns a copy. Time axis unchanged.
     */
    public AxisConfig toFahrenheit() {
        if (unit == TemperatureUnit.FAHRENHEIT) {
            return new AxisConfig(timeMinSec, timeMaxSec, timeTickStepSec,
                    tempMin, tempMax, tempTickStep, TemperatureUnit.FAHRENHEIT);
        }
        return new AxisConfig(timeMinSec, timeMaxSec, timeTickStepSec,
                celsiusToFahrenheit(tempMin), celsiusToFahrenheit(tempMax),
                celsiusToFahrenheit(tempTickStep), TemperatureUnit.FAHRENHEIT);
    }

    /**
     * Returns a new AxisConfig with the same limits and steps converted to Celsius.
     * If already in Celsius, returns a copy. Time axis unchanged.
     */
    public AxisConfig toCelsius() {
        if (unit == TemperatureUnit.CELSIUS) {
            return new AxisConfig(timeMinSec, timeMaxSec, timeTickStepSec,
                    tempMin, tempMax, tempTickStep, TemperatureUnit.CELSIUS);
        }
        return new AxisConfig(timeMinSec, timeMaxSec, timeTickStepSec,
                fahrenheitToCelsius(tempMin), fahrenheitToCelsius(tempMax),
                fahrenheitToCelsius(tempTickStep), TemperatureUnit.CELSIUS);
    }

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "axis.";

    /** Loads axis settings from Preferences into the given config. */
    public static void loadFromPreferences(AxisConfig target) {
        if (target == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        target.setTimeMinSec(p.getDouble(PREFIX + "timeMinSec", 0));
        target.setTimeMaxSec(p.getDouble(PREFIX + "timeMaxSec", 900));
        target.setTimeTickStepSec(p.getDouble(PREFIX + "timeTickStepSec", 60));
        target.setTempMin(p.getDouble(PREFIX + "tempMin", 0));
        target.setTempMax(p.getDouble(PREFIX + "tempMax", 300));
        target.setTempTickStep(p.getDouble(PREFIX + "tempTickStep", 25));
        target.setAutoScaleY(p.getBoolean(PREFIX + "autoScaleY", true));
        target.setAutoScaleY2(p.getBoolean(PREFIX + "autoScaleY2", false));
        target.setTempAutoScaleFloor(p.getDouble(PREFIX + "tempAutoScaleFloor", 50.0));
        target.setRorMin(p.getDouble(PREFIX + "rorMin", DEFAULT_MIN_ROR));
        target.setRorMax(p.getDouble(PREFIX + "rorMax", DEFAULT_MAX_ROR));
        target.setUnit(p.getBoolean(PREFIX + "unitFahrenheit", false) ? TemperatureUnit.FAHRENHEIT : TemperatureUnit.CELSIUS);
    }

    /** Saves the given config to Preferences. */
    public static void saveToPreferences(AxisConfig config) {
        if (config == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.putDouble(PREFIX + "timeMinSec", config.getTimeMinSec());
        p.putDouble(PREFIX + "timeMaxSec", config.getTimeMaxSec());
        p.putDouble(PREFIX + "timeTickStepSec", config.getTimeTickStepSec());
        p.putDouble(PREFIX + "tempMin", config.getTempMin());
        p.putDouble(PREFIX + "tempMax", config.getTempMax());
        p.putDouble(PREFIX + "tempTickStep", config.getTempTickStep());
        p.putBoolean(PREFIX + "autoScaleY", config.isAutoScaleY());
        p.putBoolean(PREFIX + "autoScaleY2", config.isAutoScaleY2());
        p.putDouble(PREFIX + "tempAutoScaleFloor", config.getTempAutoScaleFloor());
        p.putDouble(PREFIX + "rorMin", config.getRorMin());
        p.putDouble(PREFIX + "rorMax", config.getRorMax());
        p.putBoolean(PREFIX + "unitFahrenheit", config.getUnit() == TemperatureUnit.FAHRENHEIT);
    }
}
