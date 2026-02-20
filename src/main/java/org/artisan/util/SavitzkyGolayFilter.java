package org.artisan.util;

import java.util.List;

/**
 * Savitzky-Golay polynomial smoothing filter. Computes convolution coefficients
 * via least-squares polynomial fit for the given window size and polynomial order.
 * Provides three presets: RECOMMENDED, NOISE_SMOOTHING, and SENSITIVE.
 */
public final class SavitzkyGolayFilter {

    public enum Preset {
        /** Order 2, window 7 — balanced smoothing. */
        RECOMMENDED(7, 2),
        /** Order 2, window 15 — heavy noise smoothing. */
        NOISE_SMOOTHING(15, 2),
        /** Order 2, window 3 — minimal smoothing, maximum sensitivity. */
        SENSITIVE(3, 2);

        public final int windowSize;
        public final int polyOrder;
        Preset(int windowSize, int polyOrder) {
            this.windowSize = windowSize;
            this.polyOrder = polyOrder;
        }
    }

    private SavitzkyGolayFilter() {}

    /**
     * Applies Savitzky-Golay filter to the data using the given preset.
     * @param data   input array (not null, may contain NaN which are passed through)
     * @param preset smoothing preset
     * @return filtered array of same length
     */
    public static double[] filter(double[] data, Preset preset) {
        return filter(data, preset.windowSize, preset.polyOrder);
    }

    /**
     * Applies Savitzky-Golay filter to the data.
     * @param data       input array
     * @param windowSize must be odd and >= 3
     * @param polyOrder  polynomial order (typically 2 or 3), must be < windowSize
     * @return filtered array of same length
     */
    public static double[] filter(double[] data, int windowSize, int polyOrder) {
        if (data == null || data.length == 0) return new double[0];
        int n = data.length;
        int w = Math.max(3, windowSize);
        if ((w & 1) == 0) w++;
        if (w > n) w = (n % 2 == 0) ? n - 1 : n;
        if (w < 3) return data.clone();
        int order = Math.min(polyOrder, w - 1);
        int half = w / 2;

        double[] coeffs = computeCoefficients(w, order);
        double[] result = new double[n];

        for (int i = 0; i < n; i++) {
            if (i < half || i >= n - half) {
                int localW = Math.min(2 * Math.min(i, n - 1 - i) + 1, w);
                if (localW < 3) {
                    result[i] = data[i];
                    continue;
                }
                double[] localCoeffs = computeCoefficients(localW, Math.min(order, localW - 1));
                int localHalf = localW / 2;
                double sum = 0;
                for (int j = -localHalf; j <= localHalf; j++) {
                    sum += localCoeffs[j + localHalf] * data[i + j];
                }
                result[i] = sum;
            } else {
                double sum = 0;
                for (int j = -half; j <= half; j++) {
                    double v = data[i + j];
                    if (!Double.isFinite(v)) { sum = Double.NaN; break; }
                    sum += coeffs[j + half] * v;
                }
                result[i] = sum;
            }
        }
        return result;
    }

    /**
     * Applies SG filter to a List, returning a List.
     */
    public static List<Double> filterList(List<Double> data, Preset preset) {
        if (data == null || data.isEmpty()) return List.of();
        double[] arr = new double[data.size()];
        for (int i = 0; i < arr.length; i++) {
            Double v = data.get(i);
            arr[i] = (v != null && Double.isFinite(v)) ? v : Double.NaN;
        }
        double[] result = filter(arr, preset);
        java.util.ArrayList<Double> out = new java.util.ArrayList<>(result.length);
        for (double v : result) out.add(v);
        return out;
    }

    /**
     * Computes the SG convolution coefficients for a given window and polynomial order.
     * Uses the pseudo-inverse of the Vandermonde matrix: coeffs = (J^T J)^{-1} J^T, row 0.
     */
    static double[] computeCoefficients(int windowSize, int polyOrder) {
        int half = windowSize / 2;
        int m = polyOrder + 1;

        double[][] J = new double[windowSize][m];
        for (int i = 0; i < windowSize; i++) {
            double x = i - half;
            double xp = 1.0;
            for (int j = 0; j < m; j++) {
                J[i][j] = xp;
                xp *= x;
            }
        }

        double[][] JtJ = new double[m][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                double sum = 0;
                for (int k = 0; k < windowSize; k++) {
                    sum += J[k][i] * J[k][j];
                }
                JtJ[i][j] = sum;
            }
        }

        double[][] inv = invertMatrix(JtJ, m);

        double[] coeffs = new double[windowSize];
        for (int i = 0; i < windowSize; i++) {
            double sum = 0;
            for (int j = 0; j < m; j++) {
                sum += inv[0][j] * J[i][j];
            }
            coeffs[i] = sum;
        }
        return coeffs;
    }

    private static double[][] invertMatrix(double[][] matrix, int n) {
        double[][] aug = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, aug[i], 0, n);
            aug[i][n + i] = 1.0;
        }
        for (int col = 0; col < n; col++) {
            int maxRow = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(aug[row][col]) > Math.abs(aug[maxRow][col])) maxRow = row;
            }
            double[] tmp = aug[col]; aug[col] = aug[maxRow]; aug[maxRow] = tmp;
            double pivot = aug[col][col];
            if (Math.abs(pivot) < 1e-15) continue;
            for (int j = 0; j < 2 * n; j++) aug[col][j] /= pivot;
            for (int row = 0; row < n; row++) {
                if (row == col) continue;
                double factor = aug[row][col];
                for (int j = 0; j < 2 * n; j++) aug[row][j] -= factor * aug[col][j];
            }
        }
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(aug[i], n, result[i], 0, n);
        }
        return result;
    }
}
