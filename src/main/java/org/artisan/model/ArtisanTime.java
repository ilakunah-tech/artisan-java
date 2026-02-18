package org.artisan.model;

/**
 * High-resolution elapsed time (ported from Python artisanlib.time.ArtisanTime).
 * Uses System.nanoTime(); base 1000 gives elapsed() in milliseconds, elapsedMilli() in seconds.
 */
public class ArtisanTime {

    private long clockNanos;
    private double base = 1000.0;

    public ArtisanTime() {
        start();
    }

    public void setBase(double b) {
        this.base = b;
    }

    public double getBase() {
        return base;
    }

    public void start() {
        this.clockNanos = System.nanoTime();
    }

    /** Adds a period (in seconds) to the clock; next elapsed() will be reduced by that amount. */
    public void addClock(double periodSeconds) {
        this.clockNanos += (long) (periodSeconds * 1_000_000_000);
    }

    /** Elapsed time in base units: (now - clock) * base. With base=1000, returns milliseconds. */
    public double elapsed() {
        long now = System.nanoTime();
        double deltaSeconds = (now - clockNanos) / 1_000_000_000.0;
        return deltaSeconds * base;
    }

    /** Elapsed in base/1000 (with base=1000, returns seconds). */
    public double elapsedMilli() {
        return elapsed() / 1000.0;
    }
}
