package org.artisan.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.artisan.model.EventEntry;
import org.artisan.model.EventType;
import org.artisan.model.ProfileData;

/**
 * Replays recorded custom events from a background profile during live recording.
 * Fires when currentTimeSec >= (eventTimeSec + offsetSeconds). Fired events are not fired again until reset (e.g. on CHARGE).
 */
public final class EventReplay {

    private static final String PREF_NODE = "org.artisan";
    private static final String KEY_ENABLED = "replay.enabled";
    private static final String KEY_OFFSET = "replay.offsetSeconds";
    private static final String KEY_REPLAY_BACKGROUND = "replay.replayBackground";

    private boolean enabled;
    private double offsetSeconds;
    private boolean replayBackground;
    private final Set<Integer> firedIndices = new HashSet<>();

    public EventReplay() {
        load();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getOffsetSeconds() {
        return offsetSeconds;
    }

    public void setOffsetSeconds(double offsetSeconds) {
        this.offsetSeconds = offsetSeconds;
    }

    public boolean isReplayBackground() {
        return replayBackground;
    }

    public void setReplayBackground(boolean replayBackground) {
        this.replayBackground = replayBackground;
    }

    /**
     * Resets fired state so events can fire again. Call when recording starts (e.g. on CHARGE).
     */
    public void reset() {
        firedIndices.clear();
    }

    /**
     * Walks background profile's custom events; for each whose (timeSec + offsetSeconds) <= currentTimeSec
     * and not yet fired, calls onFire and marks as fired.
     *
     * @param currentTimeSec elapsed time in current roast (seconds)
     * @param background     background profile data (or null to skip)
     * @param onFire         callback for each event to fire
     */
    public void checkReplay(double currentTimeSec, ProfileData background, Consumer<EventEntry> onFire) {
        if (!enabled || onFire == null) return;
        if (!replayBackground || background == null) return;
        List<Integer> se = background.getSpecialevents();
        List<Integer> set = background.getSpecialeventstype();
        List<Double> sev = background.getSpecialeventsvalue();
        List<Double> timex = background.getTimex();
        List<Double> temp2 = background.getTemp2();
        if (se == null || set == null || sev == null || timex == null || temp2 == null) return;
        int n = Math.min(se.size(), Math.min(set.size(), Math.min(sev.size(), timex.size())));
        for (int i = 0; i < n; i++) {
            int typeOrd = set.get(i);
            EventType type = typeOrd >= 0 && typeOrd < EventType.values().length
                    ? EventType.values()[typeOrd] : EventType.CUSTOM;
            if (type != EventType.CUSTOM) continue;
            int idx = se.get(i);
            if (idx < 0 || idx >= timex.size()) continue;
            double timeSec = timex.get(idx);
            double fireTime = timeSec + offsetSeconds;
            if (fireTime > currentTimeSec) continue;
            if (firedIndices.contains(i)) continue;
            double temp = idx < temp2.size() ? temp2.get(idx) : 0.0;
            double value = i < sev.size() ? sev.get(i) : 0.0;
            String label = "Replay";
            EventEntry entry = new EventEntry(idx, temp, label, type, value);
            firedIndices.add(i);
            onFire.accept(entry);
        }
    }

    public void save() {
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node(PREF_NODE);
            prefs.putBoolean(KEY_ENABLED, enabled);
            prefs.putDouble(KEY_OFFSET, offsetSeconds);
            prefs.putBoolean(KEY_REPLAY_BACKGROUND, replayBackground);
        } catch (Exception ignored) {}
    }

    public void load() {
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node(PREF_NODE);
            enabled = prefs.getBoolean(KEY_ENABLED, false);
            offsetSeconds = prefs.getDouble(KEY_OFFSET, 0.0);
            replayBackground = prefs.getBoolean(KEY_REPLAY_BACKGROUND, true);
        } catch (Exception ignored) {
            enabled = false;
            offsetSeconds = 0.0;
            replayBackground = true;
        }
    }
}
