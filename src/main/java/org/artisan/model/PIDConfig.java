package org.artisan.model;

import java.util.prefs.Preferences;

/**
 * PID configuration persisted under Preferences "pid.*".
 */
public final class PIDConfig {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "pid.";

    public static final boolean DEFAULT_ENABLED = false;
    public static final double DEFAULT_KP = 5.0;
    public static final double DEFAULT_KI = 0.01;
    public static final double DEFAULT_KD = 1.0;
    public static final double DEFAULT_OUTPUT_MIN = 0.0;
    public static final double DEFAULT_OUTPUT_MAX = 100.0;
    public static final double DEFAULT_SETPOINT = 150.0;
    public static final PIDMode DEFAULT_MODE = PIDMode.MANUAL;

    private boolean enabled = DEFAULT_ENABLED;
    private double kp = DEFAULT_KP;
    private double ki = DEFAULT_KI;
    private double kd = DEFAULT_KD;
    private double outputMin = DEFAULT_OUTPUT_MIN;
    private double outputMax = DEFAULT_OUTPUT_MAX;
    private double setpoint = DEFAULT_SETPOINT;
    private PIDMode mode = DEFAULT_MODE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getKp() {
        return kp;
    }

    public void setKp(double kp) {
        this.kp = kp;
    }

    public double getKi() {
        return ki;
    }

    public void setKi(double ki) {
        this.ki = ki;
    }

    public double getKd() {
        return kd;
    }

    public void setKd(double kd) {
        this.kd = kd;
    }

    public double getOutputMin() {
        return outputMin;
    }

    public void setOutputMin(double outputMin) {
        this.outputMin = outputMin;
    }

    public double getOutputMax() {
        return outputMax;
    }

    public void setOutputMax(double outputMax) {
        this.outputMax = outputMax;
    }

    public double getSetpoint() {
        return setpoint;
    }

    public void setSetpoint(double setpoint) {
        this.setpoint = setpoint;
    }

    public PIDMode getMode() {
        return mode;
    }

    public void setMode(PIDMode mode) {
        this.mode = mode != null ? mode : DEFAULT_MODE;
    }

    /** Load from Preferences into this instance. */
    public void load() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        enabled = p.getBoolean(PREFIX + "enabled", DEFAULT_ENABLED);
        kp = p.getDouble(PREFIX + "kp", DEFAULT_KP);
        ki = p.getDouble(PREFIX + "ki", DEFAULT_KI);
        kd = p.getDouble(PREFIX + "kd", DEFAULT_KD);
        outputMin = p.getDouble(PREFIX + "outputMin", DEFAULT_OUTPUT_MIN);
        outputMax = p.getDouble(PREFIX + "outputMax", DEFAULT_OUTPUT_MAX);
        setpoint = p.getDouble(PREFIX + "setpoint", DEFAULT_SETPOINT);
        String modeStr = p.get(PREFIX + "mode", DEFAULT_MODE.name());
        try {
            mode = PIDMode.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            mode = DEFAULT_MODE;
        }
    }

    /** Save this instance to Preferences. */
    public void save() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.putBoolean(PREFIX + "enabled", enabled);
        p.putDouble(PREFIX + "kp", kp);
        p.putDouble(PREFIX + "ki", ki);
        p.putDouble(PREFIX + "kd", kd);
        p.putDouble(PREFIX + "outputMin", outputMin);
        p.putDouble(PREFIX + "outputMax", outputMax);
        p.putDouble(PREFIX + "setpoint", setpoint);
        p.put(PREFIX + "mode", mode.name());
    }

    /** Reset to default values (in memory only). */
    public void defaults() {
        enabled = DEFAULT_ENABLED;
        kp = DEFAULT_KP;
        ki = DEFAULT_KI;
        kd = DEFAULT_KD;
        outputMin = DEFAULT_OUTPUT_MIN;
        outputMax = DEFAULT_OUTPUT_MAX;
        setpoint = DEFAULT_SETPOINT;
        mode = DEFAULT_MODE;
    }
}
