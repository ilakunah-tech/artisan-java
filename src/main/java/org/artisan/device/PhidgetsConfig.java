package org.artisan.device;

import java.util.prefs.Preferences;

/**
 * Configuration for Phidgets temperature sensors.
 * Persisted under Preferences "phidgets.*".
 */
public final class PhidgetsConfig {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "phidgets.";

    public static final double DEFAULT_SAMPLING_INTERVAL_MS = 250.0;

    private String serialNumber = "";
    private int hubPort = 0;
    private double samplingIntervalMs = DEFAULT_SAMPLING_INTERVAL_MS;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber != null ? serialNumber : "";
    }

    public int getHubPort() {
        return hubPort;
    }

    public void setHubPort(int hubPort) {
        this.hubPort = hubPort;
    }

    public double getSamplingIntervalMs() {
        return samplingIntervalMs;
    }

    public void setSamplingIntervalMs(double samplingIntervalMs) {
        this.samplingIntervalMs = Math.max(50, Math.min(5000, samplingIntervalMs));
    }

    public static void loadFromPreferences(PhidgetsConfig target) {
        if (target == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        target.setSerialNumber(p.get(PREFIX + "serialNumber", ""));
        target.setHubPort(p.getInt(PREFIX + "hubPort", 0));
        target.setSamplingIntervalMs(p.getDouble(PREFIX + "samplingIntervalMs", DEFAULT_SAMPLING_INTERVAL_MS));
    }

    public void save() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.put(PREFIX + "serialNumber", serialNumber);
        p.putInt(PREFIX + "hubPort", hubPort);
        p.putDouble(PREFIX + "samplingIntervalMs", samplingIntervalMs);
    }
}
