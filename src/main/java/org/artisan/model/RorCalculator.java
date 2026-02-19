package org.artisan.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.artisan.util.CurveSmoothing;
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

    /** Default min RoR for clamping (degrees/min), same as Python Artisan. */
    public static final double DEFAULT_MIN_ROR = -20.0;
    /** Default max RoR for clamping (degrees/min), same as Python Artisan. */
    public static final double DEFAULT_MAX_ROR = 50.0;

    /**
     * Computes RoR after smoothing the temperature series with Savitzky-Golay (CurveSmoothing).
     * Consistent with Python Artisan: smooth temperatures first, then differentiate.
     *
     * @param timex           time in seconds; not null
     * @param temps           raw temperature values; not null
     * @param smoothingWindow odd window size for CurveSmoothing (1–99); also used as delta span for RoR
     * @return list of RoR values (degrees/min), same length as timex
     */
    public List<Double> computeRoRSmoothed(List<Double> timex, List<Double> temps, int smoothingWindow) {
        Objects.requireNonNull(timex, "timex");
        Objects.requireNonNull(temps, "temps");
        List<Double> smoothed = CurveSmoothing.smooth(temps, smoothingWindow);
        return computeRoR(timex, smoothed, smoothingWindow);
    }

    /**
     * Clamps RoR values to [minRoR, maxRoR], in place.
     * Values outside the range are replaced with the nearest boundary.
     *
     * @param ror    list of RoR values to clamp (modified in place); not null
     * @param minRoR minimum RoR (degrees/min), e.g. {@link #DEFAULT_MIN_ROR}
     * @param maxRoR maximum RoR (degrees/min), e.g. {@link #DEFAULT_MAX_ROR}
     */
    public static void clampRoR(List<Double> ror, double minRoR, double maxRoR) {
        Objects.requireNonNull(ror, "ror");
        for (int i = 0; i < ror.size(); i++) {
            double v = ror.get(i);
            if (Double.isFinite(v)) {
                if (v < minRoR) ror.set(i, minRoR);
                else if (v > maxRoR) ror.set(i, maxRoR);
            }
        }
    }

    /**
     * Finds the index of the minimum BT (bean temperature) in the window [chargeIdx, endIdx].
     * Turning Point = minimum BT after CHARGE and before DRY_END (or FC_START if DRY_END not set).
     *
     * @param temp2     BT curve (bean temperature); not null
     * @param chargeIdx first index (inclusive)
     * @param endIdx    last index (inclusive); must be &gt;= chargeIdx
     * @return index of minimum BT in that range, or chargeIdx if flat or invalid
     */
    public static int findTurningPoint(List<Double> temp2, int chargeIdx, int endIdx) {
        Objects.requireNonNull(temp2, "temp2");
        if (chargeIdx < 0 || endIdx < chargeIdx) return chargeIdx >= 0 ? chargeIdx : 0;
        if (temp2.isEmpty() || chargeIdx >= temp2.size()) return chargeIdx;
        int end = Math.min(endIdx, temp2.size() - 1);
        int minIdx = chargeIdx;
        double minVal = temp2.get(chargeIdx) != null ? temp2.get(chargeIdx) : Double.POSITIVE_INFINITY;
        for (int i = chargeIdx + 1; i <= end; i++) {
            Double v = temp2.get(i);
            if (v != null && Double.isFinite(v) && v < minVal) {
                minVal = v;
                minIdx = i;
            }
        }
        return minIdx;
    }
}
