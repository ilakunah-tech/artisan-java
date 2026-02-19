package org.artisan.model;

import java.util.prefs.Preferences;

/**
 * Sampling configuration: interval, oversampling, spike filter.
 * Persisted under Preferences "sampling.*". Used by AppController to drive Sampling timer.
 */
public final class SamplingConfig {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "sampling.";

    public static final double DEFAULT_INTERVAL_SECONDS = 1.0;
    public static final double MIN_INTERVAL = 0.5;
    public static final double MAX_INTERVAL = 30.0;
    public static final int DEFAULT_OVERSAMPLING = 1;
    public static final int MIN_OVERSAMPLING = 1;
    public static final int MAX_OVERSAMPLING = 10;
    public static final double DEFAULT_SPIKE_THRESHOLD = 25.0; // °C/s

    private double intervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private int oversampling = DEFAULT_OVERSAMPLING;
    private boolean filterSpikes = false;
    private double spikeThreshold = DEFAULT_SPIKE_THRESHOLD;

    public double getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(double intervalSeconds) {
        this.intervalSeconds = Math.max(MIN_INTERVAL, Math.min(MAX_INTERVAL, intervalSeconds));
    }

    public int getOversampling() {
        return oversampling;
    }

    public void setOversampling(int oversampling) {
        this.oversampling = Math.max(MIN_OVERSAMPLING, Math.min(MAX_OVERSAMPLING, oversampling));
    }

    public boolean isFilterSpikes() {
        return filterSpikes;
    }

    public void setFilterSpikes(boolean filterSpikes) {
        this.filterSpikes = filterSpikes;
    }

    public double getSpikeThreshold() {
        return spikeThreshold;
    }

    public void setSpikeThreshold(double spikeThreshold) {
        this.spikeThreshold = Math.max(0.1, Math.min(200, spikeThreshold));
    }

    /** Interval in milliseconds for the Sampling timer. */
    public int getIntervalMs() {
        return (int) Math.round(intervalSeconds * 1000);
    }

    public static void loadFromPreferences(SamplingConfig target) {
        if (target == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        target.setIntervalSeconds(p.getDouble(PREFIX + "intervalSeconds", DEFAULT_INTERVAL_SECONDS));
        target.setOversampling(p.getInt(PREFIX + "oversampling", DEFAULT_OVERSAMPLING));
        target.setFilterSpikes(p.getBoolean(PREFIX + "filterSpikes", false));
        target.setSpikeThreshold(p.getDouble(PREFIX + "spikeThreshold", DEFAULT_SPIKE_THRESHOLD));
    }

    public static void saveToPreferences(SamplingConfig config) {
        if (config == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.putDouble(PREFIX + "intervalSeconds", config.getIntervalSeconds());
        p.putInt(PREFIX + "oversampling", config.getOversampling());
        p.putBoolean(PREFIX + "filterSpikes", config.isFilterSpikes());
        p.putDouble(PREFIX + "spikeThreshold", config.getSpikeThreshold());
    }
}
