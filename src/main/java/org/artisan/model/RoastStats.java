package org.artisan.model;

import java.util.Objects;

/**
 * Roast statistics for one profile: mean BT/ET, RoR min/max/mean.
 * Aligns with Python ComputedProfileInformation and statistics display.
 */
public final class RoastStats {

    private final double meanBt;
    private final double meanEt;
    private final double rorMin;
    private final double rorMax;
    private final double rorMean;
    private final double totalTimeSec;
    private final boolean empty;

    public RoastStats(double meanBt, double meanEt, double rorMin, double rorMax, double rorMean,
                      double totalTimeSec, boolean empty) {
        this.meanBt = meanBt;
        this.meanEt = meanEt;
        this.rorMin = rorMin;
        this.rorMax = rorMax;
        this.rorMean = rorMean;
        this.totalTimeSec = totalTimeSec;
        this.empty = empty;
    }

    public double getMeanBt() { return meanBt; }
    public double getMeanEt() { return meanEt; }
    public double getRorMin() { return rorMin; }
    public double getRorMax() { return rorMax; }
    public double getRorMean() { return rorMean; }
    public double getTotalTimeSec() { return totalTimeSec; }
    public boolean isEmpty() { return empty; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoastStats that = (RoastStats) o;
        return Double.compare(that.meanBt, meanBt) == 0
                && Double.compare(that.meanEt, meanEt) == 0
                && Double.compare(that.rorMin, rorMin) == 0
                && Double.compare(that.rorMax, rorMax) == 0
                && Double.compare(that.rorMean, rorMean) == 0
                && Double.compare(that.totalTimeSec, totalTimeSec) == 0
                && empty == that.empty;
    }

    @Override
    public int hashCode() {
        return Objects.hash(meanBt, meanEt, rorMin, rorMax, rorMean, totalTimeSec, empty);
    }
}
