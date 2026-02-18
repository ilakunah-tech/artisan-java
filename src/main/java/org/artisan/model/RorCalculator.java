package org.artisan.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.artisan.util.LiveSosFilter;

/**
 * Computes Rate of Rise (RoR) from time and temperature arrays.
 * RoR = (delta T / delta t) * 60 → degrees per minute.
 * Uses a sliding window of (smoothingWindow + 1) points; optional LiveSosFilter for smoothing.
 * Ported from Python artisanlib.canvas sample_processing RoR logic.
 */
public final class RorCalculator {

    /**
     * Computes RoR for each point. For indices &lt; smoothingWindow returns 0.0.
     * Formula: RoR[i] = (temps[i] - temps[i - smoothingWindow]) / (timex[i] - timex[i - smoothingWindow]) * 60.
     *
     * @param timex           time in seconds; not null
     * @param temps           temperature values; not null; same length as timex
     * @param smoothingWindow number of samples in the delta span (≥ 1); uses (smoothingWindow + 1) points per RoR
     * @return list of RoR values (degrees/min), same length as timex; first smoothingWindow entries are 0.0
     */
    public List<Double> computeRoR(List<Double> timex, List<Double> temps, int smoothingWindow) {
        Objects.requireNonNull(timex, "timex");
        Objects.requireNonNull(temps, "temps");
        int n = Math.min(timex.size(), temps.size());
        if (n == 0) {
            return new ArrayList<>();
        }
        int window = Math.max(1, smoothingWindow);
        List<Double> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (i < window) {
                result.add(0.0);
                continue;
            }
            double t0 = timex.get(i - window);
            double t1 = timex.get(i);
            double temp0 = temps.get(i - window);
            double temp1 = temps.get(i);
            double dtSec = t1 - t0;
            if (dtSec <= 0 || !Double.isFinite(temp0) || !Double.isFinite(temp1)) {
                result.add(0.0);
                continue;
            }
            double ror = (temp1 - temp0) / dtSec * 60.0;
            result.add(ror);
        }
        return result;
    }

    /**
     * Computes RoR after smoothing the temperature series with the given SOS filter.
     * Filter is applied sample-by-sample; then RoR is computed on the smoothed series.
     *
     * @param timex           time in seconds; not null
     * @param temps           raw temperature values; not null
     * @param smoothingWindow number of samples in the delta span (≥ 1)
     * @param filter          optional; if non-null, temps are smoothed with this filter before RoR
     * @return list of RoR values, same length as timex
     */
    public List<Double> computeRoR(List<Double> timex, List<Double> temps, int smoothingWindow, LiveSosFilter filter) {
        if (filter == null) {
            return computeRoR(timex, temps, smoothingWindow);
        }
        Objects.requireNonNull(timex, "timex");
        Objects.requireNonNull(temps, "temps");
        int n = Math.min(timex.size(), temps.size());
        if (n == 0) {
            return new ArrayList<>();
        }
        List<Double> smoothed = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            smoothed.add(filter.process(temps.get(i)));
        }
        return computeRoR(timex, smoothed, smoothingWindow);
    }
}
