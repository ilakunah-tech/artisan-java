package org.artisan.device;

import java.util.prefs.Preferences;

/**
 * Roast simulator configuration. Persisted under Preferences "simulator.*".
 */
public final class SimulatorConfig {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "simulator.";

    public static final double DEFAULT_BT_START_TEMP = 25.0;
    public static final double DEFAULT_ET_OFFSET = 10.0;
    public static final double DEFAULT_NOISE_AMPLITUDE = 0.5;
    public static final double DEFAULT_SPEED_MULTIPLIER = 1.0;

    private double btStartTemp = DEFAULT_BT_START_TEMP;
    private double etOffset = DEFAULT_ET_OFFSET;
    private double noiseAmplitude = DEFAULT_NOISE_AMPLITUDE;
    private double speedMultiplier = DEFAULT_SPEED_MULTIPLIER;

    public double getBtStartTemp() {
        return btStartTemp;
    }

    public void setBtStartTemp(double btStartTemp) {
        this.btStartTemp = btStartTemp;
    }

    public double getEtOffset() {
        return etOffset;
    }

    public void setEtOffset(double etOffset) {
        this.etOffset = etOffset;
    }

    public double getNoiseAmplitude() {
        return noiseAmplitude;
    }

    public void setNoiseAmplitude(double noiseAmplitude) {
        this.noiseAmplitude = Math.max(0, Math.min(5.0, noiseAmplitude));
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = Math.max(0.1, Math.min(10.0, speedMultiplier));
    }

    public static void loadFromPreferences(SimulatorConfig target) {
        if (target == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        target.setBtStartTemp(p.getDouble(PREFIX + "btStartTemp", DEFAULT_BT_START_TEMP));
        target.setEtOffset(p.getDouble(PREFIX + "etOffset", DEFAULT_ET_OFFSET));
        target.setNoiseAmplitude(p.getDouble(PREFIX + "noiseAmplitude", DEFAULT_NOISE_AMPLITUDE));
        target.setSpeedMultiplier(p.getDouble(PREFIX + "speedMultiplier", DEFAULT_SPEED_MULTIPLIER));
    }

    public static void saveToPreferences(SimulatorConfig config) {
        if (config == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.putDouble(PREFIX + "btStartTemp", config.getBtStartTemp());
        p.putDouble(PREFIX + "etOffset", config.getEtOffset());
        p.putDouble(PREFIX + "noiseAmplitude", config.getNoiseAmplitude());
        p.putDouble(PREFIX + "speedMultiplier", config.getSpeedMultiplier());
    }
}
