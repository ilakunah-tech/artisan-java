package org.artisan.util;

/**
 * IIR filter design for PID (1st order Butterworth lowpass via bilinear transform).
 * Used by PID.derivativeFilter() and PID.outputFilter().
 * Equivalent to scipy.signal.iirfilter(1, Wn, fs=fs, btype='low', ftype='butter', output='sos').
 */
public final class IirFilterDesign {

    private IirFilterDesign() {}

    /**
     * First-order Butterworth lowpass SOS for given sampling rate and cutoff (Hz).
     * Wn is clamped to (0, fs/2 - 0.001). Bilinear transform with prewarping.
     *
     * @param fs sampling rate in Hz
     * @param wn cutoff frequency in Hz
     * @return one SOS section [b0, b1, b2, a0, a1, a2] for LiveSosFilter
     */
    public static double[][] butter1LowpassSos(double fs, double wn) {
        double wnClamped = Math.max(0.0, Math.min(wn, fs / 2.0 - 0.001));
        if (wnClamped <= 0.0) {
            return new double[][] {{1.0, 0.0, 0.0, 1.0, 0.0, 0.0}};
        }
        double k = Math.tan(Math.PI * wnClamped / fs);
        double denom = 1.0 + k;
        double b0 = 1.0 / denom;
        double b1 = b0;
        double a1 = (1.0 - k) / denom;
        return new double[][] {{b0, b1, 0.0, 1.0, a1, 0.0}};
    }
}
