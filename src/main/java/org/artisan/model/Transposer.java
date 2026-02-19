package org.artisan.model;

import org.artisan.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Transposes a roast profile by applying time and/or temperature offsets.
 * Ported from Artisan Python transposer logic (shift-only semantics).
 * Always returns a new ProfileData; never mutates the original.
 */
public final class Transposer {

    private Transposer() {}

    /**
     * Returns a new profile with the time axis shifted by the given offset (seconds).
     * Null profile returns null. Empty profile returns an empty copy.
     *
     * @param profile      source profile (not modified)
     * @param timeOffsetSec offset in seconds (positive = later, negative = earlier)
     * @return new ProfileData with shifted timex, or null if profile is null
     */
    public static ProfileData transposeTime(ProfileData profile, double timeOffsetSec) {
        if (profile == null) {
            return null;
        }
        ProfileData out = copySkeleton(profile);
        List<Double> timex = profile.getTimex();
        List<Double> newTimex = new ArrayList<>(timex.size());
        for (Double t : timex) {
            newTimex.add(t == null ? timeOffsetSec : t + timeOffsetSec);
        }
        out.setTimex(newTimex);
        return out;
    }

    /**
     * Returns a new profile with temperatures (temp1 and temp2) shifted by the given offset.
     * Invalid readings (-1 or not proper temp) are left unchanged.
     *
     * @param profile     source profile (not modified)
     * @param tempOffset  offset to add to each valid temperature
     * @return new ProfileData with shifted temps, or null if profile is null
     */
    public static ProfileData transposeTemp(ProfileData profile, double tempOffset) {
        if (profile == null) {
            return null;
        }
        ProfileData out = copySkeleton(profile);
        out.setTemp1(shiftTempList(profile.getTemp1(), tempOffset));
        out.setTemp2(shiftTempList(profile.getTemp2(), tempOffset));
        return out;
    }

    /**
     * Returns a new profile with both time and temperature offsets applied.
     * Equivalent to transposeTime then transposeTemp on the result.
     *
     * @param profile      source profile (not modified)
     * @param timeOffsetSec time offset in seconds
     * @param tempOffset   temperature offset
     * @return new ProfileData, or null if profile is null
     */
    public static ProfileData transpose(ProfileData profile, double timeOffsetSec, double tempOffset) {
        if (profile == null) {
            return null;
        }
        ProfileData afterTime = transposeTime(profile, timeOffsetSec);
        return transposeTemp(afterTime, tempOffset);
    }

    /**
     * Applies time shift, temperature shift, and temperature scale to the profile.
     * Result: time' = time + timeShift, temp' = (temp + tempShift) * tempScale.
     * Invalid readings (-1 or non-finite) are left unchanged.
     *
     * @param profile source profile (not modified)
     * @param params  time shift (s), temp shift (°C), temp scale (e.g. 0.5–2.0)
     * @return new ProfileData, or null if profile or params is null
     */
    public static ProfileData apply(ProfileData profile, TransposerParams params) {
        if (profile == null || params == null) return null;
        ProfileData afterTime = transposeTime(profile, params.getTimeShift());
        ProfileData out = copySkeleton(afterTime);
        out.setTemp1(shiftAndScaleTempList(afterTime.getTemp1(), params.getTempShift(), params.getTempScale()));
        out.setTemp2(shiftAndScaleTempList(afterTime.getTemp2(), params.getTempShift(), params.getTempScale()));
        return out;
    }

    private static List<Double> shiftAndScaleTempList(List<Double> list, double shift, double scale) {
        if (list == null) return new ArrayList<>();
        List<Double> out = new ArrayList<>(list.size());
        for (Double t : list) {
            if (t == null || t == -1 || !Util.isProperTemp(t)) {
                out.add(t == null ? null : t);
            } else {
                out.add((Util.toFloat(t) + shift) * scale);
            }
        }
        return out;
    }

    private static List<Double> shiftTempList(List<Double> list, double offset) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<Double> out = new ArrayList<>(list.size());
        for (Double t : list) {
            if (t == null || t == -1 || !Util.isProperTemp(t)) {
                out.add(t == null ? null : t);
            } else {
                out.add(Util.toFloat(t) + offset);
            }
        }
        return out;
    }

    private static ProfileData copySkeleton(ProfileData profile) {
        ProfileData out = new ProfileData();
        out.setTimex(copyDoubleList(profile.getTimex()));
        out.setTemp1(copyDoubleList(profile.getTemp1()));
        out.setTemp2(copyDoubleList(profile.getTemp2()));
        out.setSpecialevents(copyIntList(profile.getSpecialevents()));
        out.setSpecialeventstype(copyIntList(profile.getSpecialeventstype()));
        out.setSpecialeventsvalue(copyDoubleList(profile.getSpecialeventsvalue()));
        out.setTimeindex(copyIntList(profile.getTimeindex()));
        out.setTitle(profile.getTitle());
        out.setSamplingInterval(profile.getSamplingInterval());
        out.setComputed(profile.getComputed());
        return out;
    }

    private static List<Double> copyDoubleList(List<Double> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list);
    }

    private static List<Integer> copyIntList(List<Integer> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list);
    }
}
