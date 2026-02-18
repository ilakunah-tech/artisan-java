package org.artisan.model;

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

    public AxisConfig() {
        this(-30, 600, 120, 0, 275, 50, TemperatureUnit.CELSIUS);
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
}
