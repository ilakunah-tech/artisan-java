package org.artisan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Core roast profile data (curve and key metadata).
 * Subset of Python ProfileData TypedDict used for curves and events.
 */
public class ProfileData {

    private List<Double> timex = new ArrayList<>();
    /** ET curve (°C). */
    private List<Double> temp1 = new ArrayList<>();
    /** BT curve (°C). */
    private List<Double> temp2 = new ArrayList<>();
    /** ΔET / RoR(ET) (°C/min), optional. */
    private List<Double> delta1 = new ArrayList<>();
    /** ΔBT / RoR(BT) (°C/min), optional. */
    private List<Double> delta2 = new ArrayList<>();
    private List<Integer> specialevents = new ArrayList<>();
    private List<Integer> specialeventstype = new ArrayList<>();
    private List<Double> specialeventsvalue = new ArrayList<>();
    /** Event indices: 0=CHARGE, 1=DRY_END, 2=FC_START, 3=FC_END, 4=SCs, 5=SCe, 6=DROP, 7=COOL. -1 or 0 means not set. */
    private List<Integer> timeindex = new ArrayList<>();
    private String title;
    private Double samplingInterval;
    private ComputedProfileInformation computed;

    public List<Double> getTimex() { return timex; }
    public void setTimex(List<Double> timex) { this.timex = timex != null ? timex : new ArrayList<>(); }
    public List<Double> getTemp1() { return temp1; }
    public void setTemp1(List<Double> temp1) { this.temp1 = temp1 != null ? temp1 : new ArrayList<>(); }
    public List<Double> getTemp2() { return temp2; }
    public void setTemp2(List<Double> temp2) { this.temp2 = temp2 != null ? temp2 : new ArrayList<>(); }
    public List<Double> getDelta1() { return delta1; }
    public void setDelta1(List<Double> delta1) { this.delta1 = delta1 != null ? delta1 : new ArrayList<>(); }
    public List<Double> getDelta2() { return delta2; }
    public void setDelta2(List<Double> delta2) { this.delta2 = delta2 != null ? delta2 : new ArrayList<>(); }
    public List<Integer> getSpecialevents() { return specialevents; }
    public void setSpecialevents(List<Integer> specialevents) { this.specialevents = specialevents != null ? specialevents : new ArrayList<>(); }
    public List<Integer> getSpecialeventstype() { return specialeventstype; }
    public void setSpecialeventstype(List<Integer> specialeventstype) { this.specialeventstype = specialeventstype != null ? specialeventstype : new ArrayList<>(); }
    public List<Double> getSpecialeventsvalue() { return specialeventsvalue; }
    public void setSpecialeventsvalue(List<Double> specialeventsvalue) { this.specialeventsvalue = specialeventsvalue != null ? specialeventsvalue : new ArrayList<>(); }
    public List<Integer> getTimeindex() { return timeindex; }
    public void setTimeindex(List<Integer> timeindex) { this.timeindex = timeindex != null ? timeindex : new ArrayList<>(); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Double getSamplingInterval() { return samplingInterval; }
    public void setSamplingInterval(Double samplingInterval) { this.samplingInterval = samplingInterval; }
    public ComputedProfileInformation getComputed() { return computed; }
    public void setComputed(ComputedProfileInformation computed) { this.computed = computed; }

    /** Returns the number of data points (length of timex). */
    public int size() {
        return timex.size();
    }
}
