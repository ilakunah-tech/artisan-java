package org.artisan.util;

/**
 * Live digital filter using second-order sections (SOS).
 * More numerically stable than direct IIR. Equivalent to scipy.sosfilt(sos, xs) sample-by-sample.
 * Ported from Python artisanlib.filters.LiveSosFilter.
 */
public final class LiveSosFilter extends LiveFilter {

    /** SOS matrix: n_sections x 6 [b0, b1, b2, a0, a1, a2]. */
    private final double[][] sos;
    /** State for transposed direct form II: n_sections x 2. */
    private final double[][] state;

    /**
     * @param sos second-order sections, shape [n_sections][6] with [b0, b1, b2, a0, a1, a2] per row
     */
    public LiveSosFilter(double[][] sos) {
        this.sos = new double[sos.length][6];
        for (int i = 0; i < sos.length; i++) {
            System.arraycopy(sos[i], 0, this.sos[i], 0, Math.min(6, sos[i].length));
        }
        this.state = new double[sos.length][2];
    }

    @Override
    protected double processImpl(double x) {
        double y = 0.0;
        for (int s = 0; s < sos.length; s++) {
            double b0 = sos[s][0], b1 = sos[s][1], b2 = sos[s][2];
            double a1 = sos[s][4], a2 = sos[s][5];
            y = b0 * x + state[s][0];
            state[s][0] = b1 * x - a1 * y + state[s][1];
            state[s][1] = b2 * x - a2 * y;
            x = y;
        }
        return y;
    }
}
