package org.artisan.model;

import java.util.List;

/**
 * Roast phase computation: boundaries and percentages for DRYING, MAILLARD, DEVELOPMENT.
 * Key events (from EventType): CHARGE, DRY_END, FC_START, DROP.
 * Logic aligned with Python comparator.getPhasesData() and atypes.ComputedProfileInformation.
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

    private static int getIdx(List<Integer> timeindex, int slot) {
        if (slot >= timeindex.size()) return -1;
        Integer v = timeindex.get(slot);
        if (v == null) return -1;
        if (slot == IDX_CHARGE) return v >= 0 ? v : -1;
        return v > 0 ? v : -1;
    }
}
