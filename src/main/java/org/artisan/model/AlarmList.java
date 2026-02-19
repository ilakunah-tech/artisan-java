package org.artisan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * List of alarms. Evaluation is done by AlarmEngine; this class holds add/remove/reset.
 */
public class AlarmList {

    private final List<Alarm> alarms = new ArrayList<>();

    public void add(Alarm alarm) {
        if (alarm != null) {
            alarms.add(alarm);
        }
    }

    public void remove(int index) {
        if (index >= 0 && index < alarms.size()) {
            alarms.remove(index);
        }
    }

    public Alarm get(int index) {
        return alarms.get(index);
    }

    public int size() {
        return alarms.size();
    }

    public void clear() {
        alarms.clear();
    }

    /**
     * Returns a copy of the alarm list (for persistence and iteration).
     */
    public List<Alarm> getAlarms() {
        return new ArrayList<>(alarms);
    }

    /**
     * Replaces all alarms with the given list (e.g. after loading from file).
     */
    public void setAlarms(List<Alarm> list) {
        alarms.clear();
        if (list != null) {
            for (Alarm a : list) {
                if (a != null) {
                    alarms.add(a);
                }
            }
        }
    }

    /**
     * Re-arms all alarms so they can fire again.
     */
    public void resetAll() {
        for (Alarm a : alarms) {
            a.reset();
        }
    }
}
