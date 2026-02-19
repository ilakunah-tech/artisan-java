package org.artisan.model;

/**
 * One segment of a ramp/soak program: ramp time, soak time, target temperature.
 */
public record RampSoakSegment(
    int rampSeconds,
    int soakSeconds,
    double targetTemp
) {
    public RampSoakSegment {
        rampSeconds = Math.max(0, rampSeconds);
        soakSeconds = Math.max(0, soakSeconds);
    }
}
