package org.artisan.device;

/**
 * One temperature sample from a device channel (BT, ET, timestamp).
 * Use Double.NaN for unavailable values.
 */
public record SampleResult(double bt, double et, long timestampMs) {

    /**
     * Creates a result with current time as timestamp.
     */
    public static SampleResult now(double bt, double et) {
        return new SampleResult(bt, et, System.currentTimeMillis());
    }

    /**
     * Creates a result with all NaN and current time (e.g. BLE stub).
     */
    public static SampleResult unavailable() {
        return new SampleResult(Double.NaN, Double.NaN, System.currentTimeMillis());
    }
}
