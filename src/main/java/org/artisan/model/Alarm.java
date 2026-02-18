package org.artisan.model;

import java.util.List;

/**
 * Single alarm definition: condition, threshold, action, guard, and triggered state.
 */
public class Alarm {

    private boolean active;
    private final AlarmCondition condition;
    private final double threshold;
    private final AlarmAction action;
    private final String actionValue;
    private final int guardAlarmIndex;
    private boolean triggered;

    public Alarm(boolean active, AlarmCondition condition, double threshold,
                 AlarmAction action, String actionValue, int guardAlarmIndex, boolean triggered) {
        this.active = active;
        this.condition = condition != null ? condition : AlarmCondition.ABOVE_TEMP;
        this.threshold = threshold;
        this.action = action != null ? action : AlarmAction.POP_UP;
        this.actionValue = actionValue != null ? actionValue : "";
        this.guardAlarmIndex = guardAlarmIndex;
        this.triggered = triggered;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public AlarmCondition getCondition() {
        return condition;
    }

    public double getThreshold() {
        return threshold;
    }

    public AlarmAction getAction() {
        return action;
    }

    public String getActionValue() {
        return actionValue;
    }

    public int getGuardAlarmIndex() {
        return guardAlarmIndex;
    }

    public boolean isTriggered() {
        return triggered;
    }

    /**
     * Marks this alarm as having fired (so it does not fire again until reset).
     */
    public void markTriggered() {
        this.triggered = true;
    }

    /**
     * Re-arms the alarm so it can fire again.
     */
    public void reset() {
        this.triggered = false;
    }

    /**
     * Returns true if the alarm should fire this cycle (active, not yet triggered,
     * and condition met). Guard is NOT checked here — AlarmList checks guard before calling.
     *
     * @param currentTemp   current temperature (used for ABOVE_TEMP / BELOW_TEMP)
     * @param currentTimeSec current time in seconds (used for AT_TIME / AFTER_EVENT)
     * @param events        event list (for AFTER_EVENT; may use threshold as event time)
     * @return true if condition is satisfied and alarm should fire
     */
    public boolean evaluate(double currentTemp, double currentTimeSec, List<EventEntry> events) {
        if (!active || triggered) {
            return false;
        }
        switch (condition) {
            case ABOVE_TEMP:
                return currentTemp >= threshold;
            case BELOW_TEMP:
                return currentTemp <= threshold;
            case AT_TIME:
            case AFTER_EVENT:
                return currentTimeSec >= threshold;
            default:
                return false;
        }
    }
}
