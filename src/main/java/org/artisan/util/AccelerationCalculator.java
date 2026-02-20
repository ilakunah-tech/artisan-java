package org.artisan.util;

import java.util.List;

/**
 * Computes the second derivative of temperature: acceleration = d(RoR)/dt.
 * This is the "Roast Speedometer" — positive values mean RoR is increasing,
 * negative means it's decreasing (the curve is flattening or crashing).
 * Result in °C/min² (or °F/min²).
 */
public final class AccelerationCalculator {

    private AccelerationCalculator() {}

    /**
     * Computes acceleration from time and RoR arrays.
     * @param timex time values in seconds
     * @param ror   rate of rise values (°/min)
     * @return acceleration array (°/min²), same length, endpoints extrapolated
     */
    public static double[] compute(List<Double> timex, List<Double> ror) {
        if (timex == null || ror == null) return new double[0];
        int n = Math.min(timex.size(), ror.size());
        if (n < 2) return new double[n];
        double[] acc = new double[n];
        for (int i = 1; i < n - 1; i++) {
            double dt0 = timex.get(i) - timex.get(i - 1);
            double dt1 = timex.get(i + 1) - timex.get(i);
            double dtAvg = (dt0 + dt1) / 2.0;
            if (dtAvg <= 0 || !Double.isFinite(ror.get(i + 1)) || !Double.isFinite(ror.get(i - 1))) {
                acc[i] = 0;
            } else {
                acc[i] = (ror.get(i + 1) - ror.get(i - 1)) / (dtAvg / 60.0) / 2.0;
            }
        }
        if (n >= 2) {
            acc[0] = acc[1];
            acc[n - 1] = acc[n - 2];
        }
        return acc;
    }

    /**
     * Computes acceleration from primitive arrays.
     */
    public static double[] compute(double[] timex, double[] ror) {
        if (timex == null || ror == null) return new double[0];
        int n = Math.min(timex.length, ror.length);
        if (n < 2) return new double[n];
        double[] acc = new double[n];
        for (int i = 1; i < n - 1; i++) {
            double dt0 = timex[i] - timex[i - 1];
            double dt1 = timex[i + 1] - timex[i];
            double dtAvg = (dt0 + dt1) / 2.0;
            if (dtAvg <= 0 || !Double.isFinite(ror[i + 1]) || !Double.isFinite(ror[i - 1])) {
                acc[i] = 0;
            } else {
                acc[i] = (ror[i + 1] - ror[i - 1]) / (dtAvg / 60.0) / 2.0;
            }
        }
        if (n >= 2) {
            acc[0] = acc[1];
            acc[n - 1] = acc[n - 2];
        }
        return acc;
    }
}
