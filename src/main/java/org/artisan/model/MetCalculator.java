package org.artisan.model;

import java.util.List;

/**
 * Computes MET (Maximum ET) between CHARGE and DROP from profile data.
 */
public final class MetCalculator {

    private MetCalculator() {}

    /** Index of CHARGE in timeindex (ProfileData / CanvasData). */
    private static final int IDX_CHARGE = 0;
    /** Index of DROP in timeindex. */
    private static final int IDX_DROP = 6;

    /**
     * Returns the maximum ET (temp1) value between CHARGE and DROP (inclusive).
     * Uses timeindex for CHARGE and DROP; if not set or invalid, returns NaN.
     *
     * @param pd profile data with timex, temp1, timeindex
     * @return max of temp1[chargeIdx..dropIdx], or NaN if not enough data
     */
    public static double compute(ProfileData pd) {
        if (pd == null) return Double.NaN;
        List<Integer> ti = pd.getTimeindex();
        List<Double> temp1 = pd.getTemp1();
        if (ti == null || ti.size() <= IDX_DROP || temp1 == null || temp1.isEmpty()) return Double.NaN;
        int chargeIdx = ti.get(IDX_CHARGE) != null ? ti.get(IDX_CHARGE) : -1;
        int dropIdx = ti.get(IDX_DROP) != null ? ti.get(IDX_DROP) : -1;
        return compute(temp1, chargeIdx, dropIdx);
    }

    /**
     * Returns the maximum ET (temp1) value between chargeIdx and dropIdx (inclusive).
     *
     * @param temp1     ET curve
     * @param chargeIdx first index (inclusive)
     * @param dropIdx   last index (inclusive)
     * @return max of temp1[chargeIdx..dropIdx], or NaN if not enough data
     */
    public static double compute(List<Double> temp1, int chargeIdx, int dropIdx) {
        if (temp1 == null || temp1.isEmpty()) return Double.NaN;
        if (chargeIdx < 0 || dropIdx < chargeIdx) return Double.NaN;
        if (dropIdx >= temp1.size()) return Double.NaN;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = chargeIdx; i <= dropIdx && i < temp1.size(); i++) {
            Double v = temp1.get(i);
            if (v != null && Double.isFinite(v) && v > max) max = v;
        }
        return max == Double.NEGATIVE_INFINITY ? Double.NaN : max;
    }
}
