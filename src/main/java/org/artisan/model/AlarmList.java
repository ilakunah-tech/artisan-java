package org.artisan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * List of alarms with evaluation and reset.
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
     * Re-arms all alarms so they can fire again.
     */
    public void resetAll() {
        for (Alarm a : alarms) {
            a.reset();
        }
    }

    /**
     * Evaluates all alarms for the current cycle and returns only those that fired this cycle.
     * Guard condition is checked: an alarm with guardAlarmIndex >= 0 only evaluates if
     * the alarm at that index has already triggered. Triggered alarms are marked and included once.
     *
     * @param currentTemp   current temperature
     * @param currentTimeSec current time in seconds
     * @param events        event list (for conditions that use events)
     * @return list of alarms that fired this cycle (newly triggered)
     */
    public List<Alarm> evaluateAll(double currentTemp, double currentTimeSec, List<EventEntry> events) {
        List<EventEntry> ev = events != null ? events : new ArrayList<>();
        List<Alarm> fired = new ArrayList<>();
        for (int i = 0; i < alarms.size(); i++) {
            Alarm a = alarms.get(i);
            if (!a.isActive() || a.isTriggered()) {
                continue;
            }
            int guard = a.getGuardAlarmIndex();
            if (guard >= 0 && guard < alarms.size() && !alarms.get(guard).isTriggered()) {
                continue; // guard alarm must have fired first
            }
            if (a.evaluate(currentTemp, currentTimeSec, ev)) {
                a.markTriggered();
                fired.add(a);
            }
        }
        return fired;
    }
}
