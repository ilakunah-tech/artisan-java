package org.artisan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Roast phase computation: boundaries and percentages for DRYING, MAILLARD, DEVELOPMENT.
 * Key events (from EventType): CHARGE, DRY_END, FC_START, DROP.
 * Logic aligned with Python comparator.getPhasesData() and atypes.ComputedProfileInformation.
 * Supports manual mode (BT thresholds) and auto-adjusted (events) via PhasesConfig.
 */
public final class Phases {

    /** timeindex slot: CHARGE */
    private static final int IDX_CHARGE = 0;
    /** timeindex slot: DRY_END */
    private static final int IDX_DRY_END = 1;
    /** timeindex slot: FC_START */
    private static final int IDX_FC_START = 2;
    /** timeindex slot: DROP */
    private static final int IDX_DROP = 6;
    /** Required timeindex size for getEffectiveTimeindex */
    private static final int TIMEINDEX_SIZE = 8;

    private Phases() {}

    /**
     * Computes phase boundaries and percentages from profile time series and event indices.
     *
     * @param timex    time in seconds (same length as temp2)
     * @param temp2    bean temp (e.g. BT) – used only for bounds checks
     * @param timeindex event indices: [0]=CHARGE, [1]=DRY_END, [2]=FC_START, [6]=DROP; -1 or 0 means not set
     * @return phase result; invalid if DROP &lt; FC_START or data too short
     */
    public static PhaseResult compute(
            List<Double> timex,
            List<Double> temp2,
            List<Integer> timeindex) {
        if (timex == null || timex.isEmpty() || timeindex == null || timeindex.size() <= IDX_DROP) {
            return new PhaseResult(0, 0, 0, 0, 0, 0, 0, true);
        }

        int chargeIdx = getIdx(timeindex, IDX_CHARGE);
        int dryEndIdx = getIdx(timeindex, IDX_DRY_END);
        int fcStartIdx = getIdx(timeindex, IDX_FC_START);
        int dropIdx = getIdx(timeindex, IDX_DROP);

        int startIdx = chargeIdx >= 0 ? chargeIdx : 0;
        double startTime = timex.get(startIdx);
        int n = timex.size();

        double totalTimeSec;
        if (dropIdx > 0 && dropIdx < n) {
            totalTimeSec = timex.get(dropIdx) - startTime;
        } else {
            totalTimeSec = timex.get(n - 1) - startTime;
        }
        if (totalTimeSec <= 0) {
            return new PhaseResult(0, 0, 0, 0, 0, 0, 0, true);
        }

        // Invalid: DROP before FC START (drop index valid but earlier than FC)
        if (dropIdx > 0 && fcStartIdx > 0 && dropIdx < fcStartIdx) {
            return new PhaseResult(totalTimeSec, 0, 0, 0, 0, 0, 0, true);
        }

        double drySec = 0;
        if (dryEndIdx > 0 && dryEndIdx < n) {
            drySec = timex.get(dryEndIdx) - startTime;
            if (drySec < 0) drySec = 0;
        }

        double fcsSec = 0;
        if (fcStartIdx > 0 && fcStartIdx < n) {
            fcsSec = timex.get(fcStartIdx) - startTime;
            if (fcsSec < 0) fcsSec = 0;
        }

        double developmentSec = (fcsSec > 0 && dropIdx > 0 && dropIdx < n)
                ? timex.get(dropIdx) - startTime - fcsSec
                : (totalTimeSec - fcsSec);
        if (developmentSec < 0) developmentSec = 0;

        double maillardSec = totalTimeSec - drySec - developmentSec;
        if (maillardSec < 0) maillardSec = 0;

        double dryingPct = (totalTimeSec > 0) ? (drySec / totalTimeSec) * 100.0 : 0;
        double maillardPct = (totalTimeSec > 0) ? (maillardSec / totalTimeSec) * 100.0 : 0;
        double developmentPct = (totalTimeSec > 0) ? (developmentSec / totalTimeSec) * 100.0 : 0;

        return new PhaseResult(
                totalTimeSec,
                drySec,
                maillardSec,
                developmentSec,
                dryingPct,
                maillardPct,
                developmentPct,
                false);
    }

    /**
     * Computes phases from ProfileData (uses timex, temp2, timeindex).
     */
    public static PhaseResult compute(ProfileData profile) {
        if (profile == null) {
            return new PhaseResult(0, 0, 0, 0, 0, 0, 0, true);
        }
        return compute(profile.getTimex(), profile.getTemp2(), profile.getTimeindex());
    }

    /**
     * Computes phases using config: when autoAdjustedLimits is true and DRY/FCs events exist,
     * uses profile timeindex; otherwise derives DRY/FCs boundaries from BT thresholds (manual mode).
     */
    public static PhaseResult compute(ProfileData profile, PhasesConfig config) {
        if (profile == null) {
            return new PhaseResult(0, 0, 0, 0, 0, 0, 0, true);
        }
        List<Integer> effective = getEffectiveTimeindex(
                profile.getTimex(), profile.getTemp2(), profile.getTimeindex(), config);
        return compute(profile.getTimex(), profile.getTemp2(), effective);
    }

    /**
     * Returns effective event indices for phase boundaries. When config is null or autoAdjustedLimits
     * is true and both DRY and FCs are set in timeindex, returns a copy of timeindex. Otherwise
     * derives dryEnd and fcStart indices from first crossing of dryEndTempC and fcsTempC in temp2.
     * Ensures dryEnd &lt; fcStart; invalid order leaves indices unset (0).
     */
    public static List<Integer> getEffectiveTimeindex(
            List<Double> timex,
            List<Double> temp2,
            List<Integer> timeindex,
            PhasesConfig config) {
        List<Integer> out = new ArrayList<>();
        int n = timeindex != null ? timeindex.size() : 0;
        for (int i = 0; i < TIMEINDEX_SIZE; i++) {
            out.add(i < n && timeindex.get(i) != null ? timeindex.get(i) : (i == IDX_CHARGE ? 0 : 0));
        }
        if (timex == null || timex.isEmpty() || temp2 == null || temp2.size() < timex.size()) {
            return out;
        }
        if (config == null) {
            return out;
        }
        int chargeIdx = getIdx(out, IDX_CHARGE);
        int dropIdx = getIdx(out, IDX_DROP);
        int startIdx = chargeIdx >= 0 ? chargeIdx : 0;
        int endIdx = dropIdx > 0 && dropIdx < timex.size() ? dropIdx : timex.size() - 1;
        if (endIdx < startIdx) endIdx = startIdx;

        boolean useManual = !config.isAutoAdjustedLimits()
                || getIdx(out, IDX_DRY_END) <= 0
                || getIdx(out, IDX_FC_START) <= 0;

        if (useManual) {
            double dryTemp = config.getDryEndTempC();
            double fcsTemp = config.getFcsTempC();
            if (dryTemp > fcsTemp) {
                double t = dryTemp;
                dryTemp = fcsTemp;
                fcsTemp = t;
            }
            int dryEndIdx = findFirstIndexAtOrAbove(temp2, dryTemp, startIdx, endIdx);
            int fcStartIdx = findFirstIndexAtOrAbove(temp2, fcsTemp, dryEndIdx > 0 ? dryEndIdx : startIdx, endIdx);
            if (dryEndIdx > 0) out.set(IDX_DRY_END, dryEndIdx);
            if (fcStartIdx > 0 && (dryEndIdx <= 0 || fcStartIdx > dryEndIdx)) {
                out.set(IDX_FC_START, fcStartIdx);
            }
        }
        return out;
    }

    /**
     * Builds timeindex list (size 8) from charge, dryEnd, fcStart, drop indices.
     * Use -1 for charge when not set (stored as 0 for "start at 0"); 0 or -1 for others means unset.
     */
    public static List<Integer> timeindexFromIndices(int chargeIdx, int dryEndIdx, int fcStartIdx, int dropIdx) {
        List<Integer> ti = new ArrayList<>(TIMEINDEX_SIZE);
        ti.add(chargeIdx >= 0 ? chargeIdx : 0);
        ti.add(dryEndIdx > 0 ? dryEndIdx : 0);
        ti.add(fcStartIdx > 0 ? fcStartIdx : 0);
        ti.add(0);
        ti.add(0);
        ti.add(0);
        ti.add(dropIdx > 0 ? dropIdx : 0);
        ti.add(0);
        return ti;
    }

    private static int findFirstIndexAtOrAbove(List<Double> temp2, double threshold, int from, int to) {
        for (int i = from; i <= to && i < temp2.size(); i++) {
            Double v = temp2.get(i);
            if (v != null && v >= threshold) return i;
        }
        return -1;
    }

    private static int getIdx(List<Integer> timeindex, int slot) {
        if (slot >= timeindex.size()) return -1;
        Integer v = timeindex.get(slot);
        if (v == null) return -1;
        if (slot == IDX_CHARGE) return v >= 0 ? v : -1;
        return v > 0 ? v : -1;
    }
}
