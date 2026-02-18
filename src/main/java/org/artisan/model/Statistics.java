package org.artisan.model;

import org.apache.commons.math3.stat.StatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Roast statistics: mean BT/ET, RoR min/max/mean, and comparison (delta) between two profiles.
 * Uses ProfileData from org.artisan.model. Aligns with Python main.computedProfileInformation
 * and statistics display (mean over roast segment, RoR from curve).
 */
public final class Statistics {

    private static final int IDX_CHARGE = 0;
    private static final int IDX_DROP = 6;

    private Statistics() {}

    /**
     * Computes roast statistics for the segment from CHARGE to DROP (or full curve if events missing).
     */
    public static RoastStats compute(ProfileData profile) {
        if (profile == null) {
            return emptyStats();
        }
        List<Double> timex = profile.getTimex();
        List<Double> temp1 = profile.getTemp1();
        List<Double> temp2 = profile.getTemp2();
        List<Integer> timeindex = profile.getTimeindex();
        if (timex == null || timex.isEmpty() || temp2 == null || temp2.size() < timex.size()) {
            return emptyStats();
        }

        int startIdx = startIndex(timeindex, timex.size());
        int endIdx = endIndex(timeindex, timex.size());
        if (startIdx >= endIdx) {
            return emptyStats();
        }

        double startTime = timex.get(startIdx);
        double endTime = timex.get(endIdx);
        double totalTimeSec = endTime - startTime;
        if (totalTimeSec <= 0) {
            return emptyStats();
        }

        double sumBt = 0;
        double sumEt = 0;
        int countBt = 0;
        int countEt = 0;
        List<Double> rorPerMin = new ArrayList<>();

        for (int i = startIdx; i < endIdx; i++) {
            double t = timex.get(i);
            double tNext = timex.get(i + 1);
            double dt = tNext - t;
            if (dt <= 0) continue;

            if (i < temp2.size()) {
                double bt = temp2.get(i);
                if (validTemp(bt)) {
                    sumBt += bt;
                    countBt++;
                }
                if (i + 1 < temp2.size()) {
                    double btNext = temp2.get(i + 1);
                    if (validTemp(bt) && validTemp(btNext)) {
                        double ror = (btNext - bt) / dt * 60.0;
                        rorPerMin.add(ror);
                    }
                }
            }
            if (temp1 != null && i < temp1.size()) {
                double et = temp1.get(i);
                if (validTemp(et)) {
                    sumEt += et;
                    countEt++;
                }
            }
        }
        if (endIdx < temp2.size() && validTemp(temp2.get(endIdx))) {
            sumBt += temp2.get(endIdx);
            countBt++;
        }
        if (temp1 != null && endIdx < temp1.size() && validTemp(temp1.get(endIdx))) {
            sumEt += temp1.get(endIdx);
            countEt++;
        }

        double meanBt = countBt > 0 ? sumBt / countBt : 0;
        double meanEt = countEt > 0 ? sumEt / countEt : 0;
        double rorMin = 0;
        double rorMax = 0;
        double rorMean = 0;
        if (!rorPerMin.isEmpty()) {
            double[] arr = rorPerMin.stream().mapToDouble(Double::doubleValue).toArray();
            rorMin = StatUtils.min(arr);
            rorMax = StatUtils.max(arr);
            rorMean = StatUtils.mean(arr);
        }

        return new RoastStats(meanBt, meanEt, rorMin, rorMax, rorMean, totalTimeSec, false);
    }

    /**
     * Returns delta (a - b) between two roast stats for comparison.
     */
    public static RoastStats delta(RoastStats a, RoastStats b) {
        if (a == null || a.isEmpty()) return b != null && !b.isEmpty() ? b : emptyStats();
        if (b == null || b.isEmpty()) return a;
        return new RoastStats(
                a.getMeanBt() - b.getMeanBt(),
                a.getMeanEt() - b.getMeanEt(),
                a.getRorMin() - b.getRorMin(),
                a.getRorMax() - b.getRorMax(),
                a.getRorMean() - b.getRorMean(),
                a.getTotalTimeSec() - b.getTotalTimeSec(),
                false);
    }

    private static RoastStats emptyStats() {
        return new RoastStats(0, 0, 0, 0, 0, 0, true);
    }

    private static int startIndex(List<Integer> timeindex, int n) {
        if (timeindex == null || timeindex.size() <= IDX_CHARGE) return 0;
        Integer v = timeindex.get(IDX_CHARGE);
        if (v != null && v >= 0 && v < n) return v;
        return 0;
    }

    private static int endIndex(List<Integer> timeindex, int n) {
        if (timeindex == null || timeindex.size() <= IDX_DROP) return n - 1;
        Integer v = timeindex.get(IDX_DROP);
        if (v != null && v > 0 && v < n) return v;
        return n - 1;
    }

    private static boolean validTemp(double t) {
        return Double.isFinite(t) && t >= -100 && t < 500;
    }
}
