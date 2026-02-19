package org.artisan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Ramp/Soak program: list of segments with linear ramp then flat soak.
 * Persisted under pid.ramp_soak.* as JSON.
 */
public final class RampSoakProgram {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "pid.ramp_soak.";
    private static final String KEY_SEGMENTS = "segments";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final java.lang.reflect.Type LIST_TYPE = new TypeToken<List<RampSoakSegment>>() {}.getType();

    private final List<RampSoakSegment> segments = new ArrayList<>();
    private int currentSegmentIndex = 0;
    private double segmentElapsedSeconds = 0.0;

    public List<RampSoakSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /** Replace segments (e.g. from dialog). */
    public void setSegments(List<RampSoakSegment> newSegments) {
        segments.clear();
        if (newSegments != null) {
            segments.addAll(newSegments);
        }
    }

    public int getCurrentSegmentIndex() {
        return currentSegmentIndex;
    }

    public double getSegmentElapsedSeconds() {
        return segmentElapsedSeconds;
    }

    /**
     * Current setpoint for given elapsed roast time.
     * Linear interpolation during ramp, then flat at targetTemp during soak.
     * If no segments or after last segment: returns last targetTemp or 0.
     */
    public double getCurrentSetpoint(double elapsedRoastSeconds) {
        if (segments.isEmpty()) {
            return 0.0;
        }
        double t = 0.0;
        for (int i = 0; i < segments.size(); i++) {
            RampSoakSegment seg = segments.get(i);
            double segStart = t;
            double rampEnd = segStart + seg.rampSeconds();
            double soakEnd = rampEnd + seg.soakSeconds();
            if (elapsedRoastSeconds < segStart) {
                return i == 0 ? 0.0 : segments.get(i - 1).targetTemp();
            }
            if (elapsedRoastSeconds <= rampEnd) {
                if (seg.rampSeconds() <= 0) {
                    return seg.targetTemp();
                }
                double prevTemp = i == 0 ? 0.0 : segments.get(i - 1).targetTemp();
                double frac = (elapsedRoastSeconds - segStart) / seg.rampSeconds();
                return prevTemp + frac * (seg.targetTemp() - prevTemp);
            }
            if (elapsedRoastSeconds <= soakEnd) {
                return seg.targetTemp();
            }
            t = soakEnd;
        }
        return segments.get(segments.size() - 1).targetTemp();
    }

    /** Total duration of all segments (ramp + soak) in seconds. */
    public double getTotalDurationSeconds() {
        double total = 0.0;
        for (RampSoakSegment seg : segments) {
            total += seg.rampSeconds() + seg.soakSeconds();
        }
        return total;
    }

    /** True when elapsed roast time has passed the end of the last segment. */
    public boolean isFinished(double elapsedRoastSeconds) {
        return segments.isEmpty() || elapsedRoastSeconds >= getTotalDurationSeconds();
    }

    /** Reset segment index and elapsed (e.g. when starting a new roast). */
    public void reset() {
        currentSegmentIndex = 0;
        segmentElapsedSeconds = 0.0;
    }

    /** Load segments from Preferences (JSON string). */
    public void load() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        String json = p.get(PREFIX + KEY_SEGMENTS, "[]");
        try {
            List<RampSoakSegment> list = GSON.fromJson(json, LIST_TYPE);
            segments.clear();
            if (list != null) {
                segments.addAll(list);
            }
        } catch (Exception e) {
            segments.clear();
        }
        reset();
    }

    /** Save segments to Preferences as JSON. */
    public void save() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.put(PREFIX + KEY_SEGMENTS, GSON.toJson(segments));
    }
}
