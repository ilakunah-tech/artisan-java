package org.artisan.device;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulates a coffee roast for UI testing without hardware.
 * Phase 1 (0–90s): BT rises from start at ~3°C/s → ~200°C
 * Phase 2 (90–270s): BT from ~200°C at ~1°C/s → ~380°C
 * Phase 3 (270–390s): BT at ~0.8°C/s → FC range. ET = BT + offset + noise.
 * Each read() advances internal clock by one tick (sampling interval).
 */
public final class RoastSimulatorChannel implements DeviceChannel {

    public static final double DEFAULT_SAMPLING_INTERVAL = 1.0;

    private final SimulatorConfig config;
    private final AtomicLong tick = new AtomicLong(0);
    private final AtomicBoolean open = new AtomicBoolean(false);

    public RoastSimulatorChannel(SimulatorConfig config) {
        this.config = config != null ? config : new SimulatorConfig();
    }

    @Override
    public void open() throws DeviceException {
        tick.set(0);
        open.set(true);
    }

    @Override
    public void close() {
        open.set(false);
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    public SampleResult read() throws DeviceException {
        if (!isOpen()) {
            throw new DeviceException("Simulator is not open");
        }
        long sec = tick.getAndIncrement();
        double t = sec * config.getSpeedMultiplier();
        double bt = computeBt(t);
        bt = bt + noise();
        double et = bt + config.getEtOffset() + noise();
        return new SampleResult(bt, et, System.currentTimeMillis());
    }

    private double computeBt(double t) {
        double bt;
        if (t <= 90) {
            bt = config.getBtStartTemp() + t * 3.0;
        } else if (t <= 270) {
            bt = config.getBtStartTemp() + 90 * 3.0 + (t - 90) * 1.0;
        } else {
            bt = config.getBtStartTemp() + 90 * 3.0 + 180 * 1.0 + (t - 270) * 0.8;
        }
        return bt;
    }

    private double noise() {
        double a = config.getNoiseAmplitude();
        if (a <= 0) return 0;
        return (Math.random() * 2 - 1) * a;
    }

    @Override
    public String getDescription() {
        return "Roast Simulator";
    }
}
