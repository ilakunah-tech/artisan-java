package org.artisan.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Curve smoothing for BT, ET, and delta series. Odd window size 1–99.
 * Window 1 = no smoothing. Uses symmetric moving average (SG-like with polynomial order 0).
 */
public final class CurveSmoothing {

    private CurveSmoothing() {}

    /**
     * Smooths the series with a symmetric window. Window must be odd (1 = no smoothing).
     * At boundaries, uses available points (partial window).
     *
     * @param y      input values (not null)
     * @param window odd window size, 1–99 (1 = no smoothing)
     * @return new list of same size; values at edges use smaller effective window
     */
    public static List<Double> smooth(List<Double> y, int window) {
        if (y == null || y.isEmpty()) return new ArrayList<>();
        int n = y.size();
        int w = Math.max(1, Math.min(99, window));
        if ((w & 1) == 0) w++;
        if (w == 1) return new ArrayList<>(y);
        int half = w / 2;
        List<Double> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int start = Math.max(0, i - half);
            int end = Math.min(n, i + half + 1);
            double sum = 0;
            int count = 0;
            for (int j = start; j < end; j++) {
                Double v = y.get(j);
                if (v != null && Double.isFinite(v)) {
                    sum += v;
                    count++;
                }
            }
            out.add(count > 0 ? sum / count : (i < y.size() ? y.get(i) : 0.0));
        }
        return out;
    }
}
