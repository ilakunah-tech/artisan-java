package org.artisan.util;

/**
 * Live IIR filter using difference equations (numerator b, denominator a).
 * Equivalent to scipy lfilter(b, a, xs) when applied sample-by-sample.
 * Ported from Python artisanlib.filters.LiveLFilter.
 */
public final class LiveLFilter extends LiveFilter {

    private final double[] b;
    private final double[] a;
    private final double[] xs;
    private final double[] ys;
    private int xsHead; // newest at index 0 (logically)
    private int ysHead;

    /**
     * @param b numerator coefficients (from filter design)
     * @param a denominator coefficients (a[0] must be non-zero)
     */
    public LiveLFilter(double[] b, double[] a) {
        this.b = b.clone();
        this.a = a.clone();
        this.xs = new double[b.length];
        this.ys = new double[Math.max(0, a.length - 1)];
        this.xsHead = 0;
        this.ysHead = 0;
    }

    @Override
    protected double processImpl(double x) {
        xs[xsHead] = x;
        xsHead = (xsHead + 1) % xs.length;
        // Logical order: newest first (index 0 = just written at xsHead-1)
        double y = 0.0;
        for (int i = 0; i < b.length; i++) {
            int idx = (xsHead - 1 - i + xs.length * 2) % xs.length;
            y += b[i] * xs[idx];
        }
        for (int i = 0; i < ys.length; i++) {
            int idx = (ysHead - 1 - i + ys.length * 2) % ys.length;
            y -= a[i + 1] * ys[idx];
        }
        y /= a[0];
        if (ys.length > 0) {
            ys[ysHead] = y;
            ysHead = (ysHead + 1) % ys.length;
        }
        return y;
    }
}
