package org.artisan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Canvas data model for roast profile: time, temperatures, and RoR (delta) arrays.
 * Holds event indices for CHARGE, DRY_END, FC_START, FC_END, DROP.
 * Ported from Python artisanlib.canvas (timex, temp1, temp2, delta1, delta2, timeindex).
 * temp1 = ET, temp2 = BT; delta1 = RoR ET, delta2 = RoR BT.
 */
public final class CanvasData {

    /** Index into timex/temp/delta arrays for CHARGE. -1 if not set. */
    private int chargeIndex = -1;
    /** Index for DRY END. */
    private int dryEndIndex = -1;
    /** Index for FC START. */
    private int fcStartIndex = -1;
    /** Index for FC END. */
    private int fcEndIndex = -1;
    /** Index for SC START (Second Crack). */
    private int scStartIndex = -1;
    /** Index for SC END. */
    private int scEndIndex = -1;
    /** Index for DROP. */
    private int dropIndex = -1;

    private final List<Double> timex = new ArrayList<>();
    private final List<Double> temp1 = new ArrayList<>();
    private final List<Double> temp2 = new ArrayList<>();
    private final List<Double> delta1 = new ArrayList<>();
    private final List<Double> delta2 = new ArrayList<>();

    private final List<Double> extraTimex1 = new ArrayList<>();
    private final List<Double> extraTemp1 = new ArrayList<>();
    private final List<Double> extraTimex2 = new ArrayList<>();
    private final List<Double> extraTemp2 = new ArrayList<>();
    private final List<Double> extraTimex3 = new ArrayList<>();
    private final List<Double> extraTemp3 = new ArrayList<>();
    private final List<Double> extraTimex4 = new ArrayList<>();
    private final List<Double> extraTemp4 = new ArrayList<>();

    /**
     * Appends one data point and computes deltas externally (caller uses RorCalculator).
     * Time in seconds; BT = bean temp, ET = env temp.
     *
     * @param timeSec time in seconds (x-axis)
     * @param bt      bean temperature (temp2)
     * @param et      environment temperature (temp1)
     */
    public void addDataPoint(double timeSec, double bt, double et) {
        timex.add(timeSec);
        temp2.add(bt);
        temp1.add(et);
        // Delta (RoR) is computed by RorCalculator and set via setDelta1/setDelta2
    }

    /**
     * Sets RoR (delta) arrays. Call after batch add or when recomputing from RorCalculator.
     * Lengths must match timex size; otherwise only the minimum length is used.
     */
    public void setDelta1(List<Double> ror1) {
        if (ror1 == null) return;
        delta1.clear();
        int n = Math.min(timex.size(), ror1.size());
        for (int i = 0; i < n; i++) {
            delta1.add(ror1.get(i));
        }
        while (delta1.size() < timex.size()) {
            delta1.add(0.0);
        }
    }

    public void setDelta2(List<Double> ror2) {
        if (ror2 == null) return;
        delta2.clear();
        int n = Math.min(timex.size(), ror2.size());
        for (int i = 0; i < n; i++) {
            delta2.add(ror2.get(i));
        }
        while (delta2.size() < timex.size()) {
            delta2.add(0.0);
        }
    }

    /** Clears all arrays and resets event indices to -1 / 0. */
    public void clear() {
        timex.clear();
        temp1.clear();
        temp2.clear();
        delta1.clear();
        delta2.clear();
        extraTimex1.clear(); extraTemp1.clear();
        extraTimex2.clear(); extraTemp2.clear();
        extraTimex3.clear(); extraTemp3.clear();
        extraTimex4.clear(); extraTemp4.clear();
        chargeIndex = -1;
        dryEndIndex = 0;
        fcStartIndex = 0;
        fcEndIndex = 0;
        scStartIndex = -1;
        scEndIndex = -1;
        dropIndex = 0;
    }

    public List<Double> getTimex()  { return Collections.unmodifiableList(timex); }
    public List<Double> getTemp1()  { return Collections.unmodifiableList(temp1); }
    public List<Double> getTemp2()  { return Collections.unmodifiableList(temp2); }
    public List<Double> getDelta1() { return Collections.unmodifiableList(delta1); }
    public List<Double> getDelta2() { return Collections.unmodifiableList(delta2); }

    /** Appends one RoR(ET) sample without a full list copy. */
    public void addDelta1(double ror) { delta1.add(ror); }
    /** Appends one RoR(BT) sample without a full list copy. */
    public void addDelta2(double ror) { delta2.add(ror); }

    public int getChargeIndex() { return chargeIndex; }
    public void setChargeIndex(int chargeIndex) { this.chargeIndex = chargeIndex; }

    public int getDryEndIndex() { return dryEndIndex; }
    public void setDryEndIndex(int dryEndIndex) { this.dryEndIndex = dryEndIndex; }

    public int getFcStartIndex() { return fcStartIndex; }
    public void setFcStartIndex(int fcStartIndex) { this.fcStartIndex = fcStartIndex; }

    public int getFcEndIndex() { return fcEndIndex; }
    public void setFcEndIndex(int fcEndIndex) { this.fcEndIndex = fcEndIndex; }

    public int getScStartIndex() { return scStartIndex; }
    public void setScStartIndex(int scStartIndex) { this.scStartIndex = scStartIndex; }

    public int getScEndIndex() { return scEndIndex; }
    public void setScEndIndex(int scEndIndex) { this.scEndIndex = scEndIndex; }

    public int getDropIndex() { return dropIndex; }
    public void setDropIndex(int dropIndex) { this.dropIndex = dropIndex; }

    public List<Double> getExtraTimex1() { return Collections.unmodifiableList(extraTimex1); }
    public List<Double> getExtraTemp1()  { return Collections.unmodifiableList(extraTemp1); }
    public List<Double> getExtraTimex2() { return Collections.unmodifiableList(extraTimex2); }
    public List<Double> getExtraTemp2()  { return Collections.unmodifiableList(extraTemp2); }
    public List<Double> getExtraTimex3() { return Collections.unmodifiableList(extraTimex3); }
    public List<Double> getExtraTemp3()  { return Collections.unmodifiableList(extraTemp3); }
    public List<Double> getExtraTimex4() { return Collections.unmodifiableList(extraTimex4); }
    public List<Double> getExtraTemp4()  { return Collections.unmodifiableList(extraTemp4); }

    public void addExtraDataPoint(int channel, double timeSec, double temp) {
        switch (channel) {
            case 1: extraTimex1.add(timeSec); extraTemp1.add(temp); break;
            case 2: extraTimex2.add(timeSec); extraTemp2.add(temp); break;
            case 3: extraTimex3.add(timeSec); extraTemp3.add(temp); break;
            case 4: extraTimex4.add(timeSec); extraTemp4.add(temp); break;
            default: break;
        }
    }
}
