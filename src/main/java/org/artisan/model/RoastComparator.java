package org.artisan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Session-only holder for multiple roast profiles (e.g. from .alog files).
 * Supports time-offset alignment for comparison. No persistence.
 */
public final class RoastComparator {

    private final List<ProfileData> profiles = new ArrayList<>();
    private final List<String> filenames = new ArrayList<>();

    public RoastComparator() {}

    /**
     * Adds a profile (e.g. loaded via Roastlog.load). Display name in list is taken from filename.
     *
     * @param profile  profile data (not null)
     * @param filename display name (e.g. file name); may be null then "Profile N" is used
     */
    public void addProfile(ProfileData profile, String filename) {
        if (profile == null) return;
        profiles.add(profile);
        filenames.add(filename != null && !filename.isBlank() ? filename : "Profile " + (profiles.size()));
    }

    /** Removes the profile at the given index. No-op if index out of range. */
    public void removeProfile(int index) {
        if (index < 0 || index >= profiles.size()) return;
        profiles.remove(index);
        filenames.remove(index);
    }

    /** Returns an unmodifiable view of the loaded profiles. */
    public List<ProfileData> getProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    /** Returns the display filename for the profile at index, or null if out of range. */
    public String getFilename(int index) {
        if (index < 0 || index >= filenames.size()) return null;
        return filenames.get(index);
    }

    /**
     * Returns BT (temp2) values for the profile at index with time axis shifted by timeOffsetSeconds.
     * The effective time for point i is timex.get(i) + timeOffsetSeconds.
     * Caller can use getProfile(index).getTimex() plus offset for X axis.
     *
     * @param index              profile index
     * @param timeOffsetSeconds  offset in seconds (positive = later)
     * @return array of BT values, or empty array if index invalid or no data
     */
    public double[] getAlignedBT(int index, double timeOffsetSeconds) {
        if (index < 0 || index >= profiles.size()) return new double[0];
        ProfileData p = profiles.get(index);
        List<Double> temp2 = p.getTemp2();
        if (temp2 == null || temp2.isEmpty()) return new double[0];
        double[] out = new double[temp2.size()];
        for (int i = 0; i < temp2.size(); i++) {
            Double v = temp2.get(i);
            out[i] = v != null ? v : 0.0;
        }
        return out;
    }

    /**
     * Returns time axis for the profile at index with offset applied.
     * Effective time at i is timex.get(i) + timeOffsetSeconds.
     */
    public double[] getAlignedTime(int index, double timeOffsetSeconds) {
        if (index < 0 || index >= profiles.size()) return new double[0];
        ProfileData p = profiles.get(index);
        List<Double> timex = p.getTimex();
        if (timex == null || timex.isEmpty()) return new double[0];
        double[] out = new double[timex.size()];
        for (int i = 0; i < timex.size(); i++) {
            Double t = timex.get(i);
            out[i] = (t != null ? t : 0.0) + timeOffsetSeconds;
        }
        return out;
    }

    /**
     * Returns ET (temp1) values for the profile at index.
     */
    public double[] getAlignedET(int index, double timeOffsetSeconds) {
        if (index < 0 || index >= profiles.size()) return new double[0];
        ProfileData p = profiles.get(index);
        List<Double> temp1 = p.getTemp1();
        if (temp1 == null || temp1.isEmpty()) return new double[0];
        double[] out = new double[temp1.size()];
        for (int i = 0; i < temp1.size(); i++) {
            Double v = temp1.get(i);
            out[i] = v != null ? v : 0.0;
        }
        return out;
    }

    /**
     * Returns RoR of BT for the profile at index, computed with the given smoothing window.
     */
    public double[] getAlignedRoRBT(int index, double timeOffsetSeconds, int smoothWindow) {
        if (index < 0 || index >= profiles.size()) return new double[0];
        ProfileData p = profiles.get(index);
        List<Double> timex = p.getTimex();
        List<Double> temp2 = p.getTemp2();
        if (timex == null || temp2 == null || timex.isEmpty()) return new double[0];
        RorCalculator calc = new RorCalculator();
        List<Double> ror = calc.computeRoRSmoothed(timex, temp2, smoothWindow);
        RorCalculator.clampRoR(ror, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
        double[] out = new double[ror.size()];
        for (int i = 0; i < ror.size(); i++) out[i] = ror.get(i);
        return out;
    }

    /**
     * Returns event timeindex list for the profile at the given index.
     */
    public List<Integer> getEventTimeindex(int index) {
        if (index < 0 || index >= profiles.size()) return List.of();
        List<Integer> ti = profiles.get(index).getTimeindex();
        return ti != null ? ti : List.of();
    }

    public void clear() {
        profiles.clear();
        filenames.clear();
    }
}
