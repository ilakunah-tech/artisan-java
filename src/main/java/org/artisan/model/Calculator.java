package org.artisan.model;

import java.util.List;

/**
 * Roast calculations: Development Time Ratio (DTR) and Area Under Curve (AUC).
 * Formulas aligned with Artisan Python main.py and calculator logic.
 */
public final class Calculator {

    private Calculator() {}

    /**
     * Development Time Ratio: 100 * (DROP_time - FCs_time) / DROP_time.
     * See main.py:12999
     *
     * @param dropTimeSec total roast time to DROP (seconds)
     * @param fcsTimeSec  time to FC START (seconds)
     * @return DTR as percentage (0..100), or 0 if dropTimeSec &lt;= 0
     */
    public static double developmentTimeRatio(double dropTimeSec, double fcsTimeSec) {
        if (dropTimeSec <= 0) {
            return 0.0;
        }
        return 100.0 * (dropTimeSec - fcsTimeSec) / dropTimeSec;
    }

    /**
     * Area Under Curve (AUC) above base temperature, in Celsius·minutes.
     * Trapezoidal: for each segment, area = max(0, (T[i]+T[i-1])/2 - base) * dt; sum then /60.
     * See main.py calcAUC:24187-24207, profileAUC:24268-24276
     *
     * @param timex      time in seconds
     * @param temp2      bean temp (BT) in Celsius
     * @param baseTempC  reference temperature (C) – area above this
     * @param startIdx   first index (inclusive)
     * @param endIdx     last index (inclusive)
     * @return AUC in C·min, or 0 if invalid range
     */
    public static double areaUnderCurve(
            List<Double> timex,
            List<Double> temp2,
            double baseTempC,
            int startIdx,
            int endIdx) {
        if (timex == null || temp2 == null || startIdx < 0 || endIdx >= timex.size()
                || endIdx >= temp2.size() || startIdx >= endIdx) {
            return 0.0;
        }
        double sumCsec = 0.0;
        for (int i = startIdx + 1; i <= endIdx; i++) {
            double dt = timex.get(i) - timex.get(i - 1);
            if (dt <= 0) continue;
            double t1 = clampTemp(temp2.get(i));
            double t2 = clampTemp(temp2.get(i - 1));
            double ta = (Math.max(0, t1) + Math.max(0, t2)) / 2.0;
            sumCsec += Math.max(0.0, ta - baseTempC) * dt;
        }
        return sumCsec / 60.0;
    }

    /**
     * AUC from CHARGE to DROP using profile timeindex. Base temp in Celsius.
     * See main.py profileAUC (ts called with default start/end).
     */
    public static double areaUnderCurve(ProfileData profile, double baseTempC) {
        if (profile == null) return 0.0;
        List<Double> timex = profile.getTimex();
        List<Double> temp2 = profile.getTemp2();
        List<Integer> timeindex = profile.getTimeindex();
        if (timex == null || timex.isEmpty() || temp2 == null || timeindex == null) return 0.0;
        int startIdx = timeindex.size() > 0 && timeindex.get(0) != null && timeindex.get(0) >= 0
                ? timeindex.get(0) : 0;
        int endIdx = timeindex.size() > 6 && timeindex.get(6) != null && timeindex.get(6) > 0
                ? timeindex.get(6) : timex.size() - 1;
        if (endIdx >= timex.size()) endIdx = timex.size() - 1;
        return areaUnderCurve(timex, temp2, baseTempC, startIdx, endIdx);
    }

    /** Clamp invalid temps (Python: &gt;500 -> 0). See main.py calcAUC:24191-24195 */
    private static double clampTemp(double t) {
        if (!Double.isFinite(t) || t > 500) return 0;
        return t;
    }
}
