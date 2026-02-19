package org.artisan.controller;

import org.artisan.model.PID;
import org.artisan.model.PIDConfig;
import org.artisan.model.PIDMode;
import org.artisan.model.RampSoakProgram;

/**
 * PID control: wraps PIDConfig, RampSoakProgram, and org.artisan.model.PID.
 * tick(currentTemp, elapsedRoastSeconds) returns duty output clamped to config range.
 */
public final class PIDControl {

    private final PIDConfig config;
    private final RampSoakProgram rampSoakProgram;
    private final PID pid;
    private volatile boolean running;
    private volatile double lastSetpoint;

    public PIDControl() {
        this.config = new PIDConfig();
        this.rampSoakProgram = new RampSoakProgram();
        this.pid = new PID();
        this.running = false;
        this.lastSetpoint = PIDConfig.DEFAULT_SETPOINT;
    }

    public PIDConfig getConfig() {
        return config;
    }

    public RampSoakProgram getRampSoakProgram() {
        return rampSoakProgram;
    }

    public PID getPid() {
        return pid;
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        applyConfigToPid();
        pid.on();
        running = true;
        rampSoakProgram.reset();
    }

    public void stop() {
        pid.off();
        running = false;
    }

    /**
     * One control tick. Returns duty output in [outputMin, outputMax].
     * If not running, returns 0.
     */
    public double tick(double currentTemp, double elapsedRoastSeconds) {
        if (!running) {
            return 0.0;
        }
        double setpoint;
        if (config.getMode() == PIDMode.RAMP_SOAK) {
            setpoint = rampSoakProgram.getCurrentSetpoint(elapsedRoastSeconds);
        } else {
            setpoint = config.getSetpoint();
        }
        lastSetpoint = setpoint;
        pid.setTarget(setpoint, false);
        pid.update(currentTemp);
        Double duty = pid.getDuty();
        if (duty == null) {
            return 0.0;
        }
        double min = config.getOutputMin();
        double max = config.getOutputMax();
        return Math.max(min, Math.min(max, duty));
    }

    /** Current active setpoint (last used in tick). */
    public double getSetpoint() {
        return lastSetpoint;
    }

    public void loadConfig() {
        config.load();
        rampSoakProgram.load();
        applyConfigToPid();
    }

    public void saveConfig() {
        config.save();
        rampSoakProgram.save();
    }

    private void applyConfigToPid() {
        pid.setPID(config.getKp(), config.getKi(), config.getKd());
        int outMin = (int) Math.round(Math.max(0, config.getOutputMin()));
        int outMax = (int) Math.round(Math.min(100, config.getOutputMax()));
        if (outMax < outMin) outMax = outMin;
        pid.setLimits(outMin, outMax);
        pid.setDutyMin(outMin);
        pid.setDutyMax(outMax);
        lastSetpoint = config.getSetpoint();
        pid.setTarget(lastSetpoint, true);
    }
}
