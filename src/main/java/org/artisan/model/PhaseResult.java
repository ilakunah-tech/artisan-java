package org.artisan.model;

import java.util.Objects;

/**
 * Result of phase computation: times (seconds) and percentages for DRYING, MAILLARD, DEVELOPMENT.
 * Aligns with Python comparator getPhasesData and atypes.ComputedProfileInformation phase fields.
 */
public final class PhaseResult {

    private final double totalTimeSec;
    private final double dryingTimeSec;
    private final double maillardTimeSec;
    private final double developmentTimeSec;
    private final double dryingPercent;
    private final double maillardPercent;
    private final double developmentPercent;
    /** True if profile is invalid (e.g. DROP before FC START). */
    private final boolean invalid;

    public PhaseResult(
            double totalTimeSec,
            double dryingTimeSec,
            double maillardTimeSec,
            double developmentTimeSec,
            double dryingPercent,
            double maillardPercent,
            double developmentPercent,
            boolean invalid) {
        this.totalTimeSec = totalTimeSec;
        this.dryingTimeSec = dryingTimeSec;
        this.maillardTimeSec = maillardTimeSec;
        this.developmentTimeSec = developmentTimeSec;
        this.dryingPercent = dryingPercent;
        this.maillardPercent = maillardPercent;
        this.developmentPercent = developmentPercent;
        this.invalid = invalid;
    }

    public double getTotalTimeSec() { return totalTimeSec; }
    public double getDryingTimeSec() { return dryingTimeSec; }
    public double getMaillardTimeSec() { return maillardTimeSec; }
    public double getDevelopmentTimeSec() { return developmentTimeSec; }
    public double getDryingPercent() { return dryingPercent; }
    public double getMaillardPercent() { return maillardPercent; }
    public double getDevelopmentPercent() { return developmentPercent; }
    public boolean isInvalid() { return invalid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhaseResult that = (PhaseResult) o;
        return Double.compare(that.totalTimeSec, totalTimeSec) == 0
                && Double.compare(that.dryingTimeSec, dryingTimeSec) == 0
                && Double.compare(that.maillardTimeSec, maillardTimeSec) == 0
                && Double.compare(that.developmentTimeSec, developmentTimeSec) == 0
                && Double.compare(that.dryingPercent, dryingPercent) == 0
                && Double.compare(that.maillardPercent, maillardPercent) == 0
                && Double.compare(that.developmentPercent, developmentPercent) == 0
                && invalid == that.invalid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalTimeSec, dryingTimeSec, maillardTimeSec, developmentTimeSec,
                dryingPercent, maillardPercent, developmentPercent, invalid);
    }
}
