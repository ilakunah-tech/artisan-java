package org.artisan.model;

import org.artisan.device.SimulatorConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * High-level simulator for Tools » Simulator: generates synthetic ProfileData
 * from charge/drop temps, total time, RoR peak, and config (ET offset, noise).
 */
public final class RoastSimulator {

    private double chargeTemp = 200.0;
    private double dropTemp = 210.0;
    private double totalTimeSeconds = 600.0;
    private double rorPeak = 15.0;      // °C/min
    private double rorPeakTime = 90.0;   // seconds
    private SimulatorConfig config = new SimulatorConfig();

    public double getChargeTemp() { return chargeTemp; }
    public void setChargeTemp(double chargeTemp) { this.chargeTemp = chargeTemp; }
    public double getDropTemp() { return dropTemp; }
    public void setDropTemp(double dropTemp) { this.dropTemp = dropTemp; }
    public double getTotalTimeSeconds() { return totalTimeSeconds; }
    public void setTotalTimeSeconds(double totalTimeSeconds) { this.totalTimeSeconds = Math.max(1, totalTimeSeconds); }
    public double getRorPeak() { return rorPeak; }
    public void setRorPeak(double rorPeak) { this.rorPeak = rorPeak; }
    public double getRorPeakTime() { return rorPeakTime; }
    public void setRorPeakTime(double rorPeakTime) { this.rorPeakTime = Math.max(0, rorPeakTime); }
    public SimulatorConfig getConfig() { return config; }
    public void setConfig(SimulatorConfig config) { this.config = config != null ? config : new SimulatorConfig(); }

    /**
     * Generates a synthetic ProfileData: BT rises from chargeTemp toward dropTemp with
     * RoR peaking at rorPeakTime, then declining. ET = BT + etOffset + noise.
     * Sampling interval from config.speedMultiplier (default 1 s).
     */
    public ProfileData generate() {
        double interval = config.getSpeedMultiplier() <= 0 ? 1.0 : config.getSpeedMultiplier();
        int n = Math.max(1, (int) Math.round(totalTimeSeconds / interval));
        double etOffset = config.getEtOffset();
        double noiseAmplitude = config.getNoiseAmplitude();

        List<Double> timex = new ArrayList<>(n);
        List<Double> temp2 = new ArrayList<>(n);
        List<Double> temp1 = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            double t = i * interval;
            if (t > totalTimeSeconds) break;
            timex.add(t);
            double bt = btAt(t);
            double noise = noiseAmplitude > 0 ? (ThreadLocalRandom.current().nextDouble() * 2 - 1) * noiseAmplitude : 0;
            temp2.add(bt + noise);
            temp1.add(bt + etOffset + (noiseAmplitude > 0 ? (ThreadLocalRandom.current().nextDouble() * 2 - 1) * noiseAmplitude : 0));
        }

        ProfileData p = new ProfileData();
        p.setTimex(timex);
        p.setTemp2(temp2);
        p.setTemp1(temp1);
        p.setSamplingInterval(interval);
        p.setTitle("Simulated");
        return p;
    }

    /** BT at time t: 0..rorPeakTime quadratic rise, then decline toward dropTemp (cubic-style). */
    private double btAt(double t) {
        if (t <= 0) return chargeTemp;
        if (t >= totalTimeSeconds) return dropTemp;
        if (t <= rorPeakTime) {
            // Rise from chargeTemp with RoR building toward peak at rorPeakTime
            // RoR at 0 ≈ 0, at rorPeakTime = rorPeak. Use quadratic: BT = chargeTemp + a*t^2, dBT/dt = 2at -> at rorPeakTime 2a*rorPeakTime = rorPeak/60 -> a = rorPeak/(120*rorPeakTime)
            double a = rorPeakTime > 0 ? (rorPeak / 60.0) / (2.0 * rorPeakTime) : 0;
            return chargeTemp + a * t * t;
        }
        // From rorPeakTime to totalTimeSeconds: decline RoR, end at dropTemp
        if (totalTimeSeconds <= rorPeakTime) return dropTemp;
        double btAtPeak = btAt(rorPeakTime);
        double frac = (t - rorPeakTime) / (totalTimeSeconds - rorPeakTime);
        if (frac >= 1) return dropTemp;
        if (frac <= 0) return btAtPeak;
        // Cubic Hermite-like: smooth transition
        double s = frac * frac * (3 - 2 * frac);
        return btAtPeak + (dropTemp - btAtPeak) * s;
    }
}
