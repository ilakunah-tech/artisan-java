package org.artisan.model;

/**
 * Single alarm definition: condition, threshold, action, guard, and triggered state.
 * IF-THEN style matching Python Artisan. Condition evaluation is done in AlarmEngine.
 */
public class Alarm {

    private boolean enabled;
    private String description;
    private AlarmCondition condition;
    private double threshold;
    private AlarmAction action;
    private String actionParam;
    private boolean triggerOnce;
    private int guardAlarmIndex;
    private boolean triggered;

    public Alarm(boolean enabled, String description, AlarmCondition condition, double threshold,
                 AlarmAction action, String actionParam, boolean triggerOnce, int guardAlarmIndex, boolean triggered) {
        this.enabled = enabled;
        this.description = description != null ? description : "";
        this.condition = condition != null ? condition : AlarmCondition.BT_RISES_ABOVE;
        this.threshold = threshold;
        this.action = action != null ? action : AlarmAction.POPUP_MESSAGE;
        this.actionParam = actionParam != null ? actionParam : "";
        this.triggerOnce = triggerOnce;
        this.guardAlarmIndex = guardAlarmIndex;
        this.triggered = triggered;
    }

    /** Copy constructor for duplicating an alarm (triggered state reset). */
    public Alarm(Alarm other) {
        this(other.enabled, other.description, other.condition, other.threshold,
                other.action, other.actionParam, other.triggerOnce, other.guardAlarmIndex, false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public AlarmCondition getCondition() {
        return condition;
    }

    public void setCondition(AlarmCondition condition) {
        this.condition = condition != null ? condition : AlarmCondition.BT_RISES_ABOVE;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public AlarmAction getAction() {
        return action;
    }

    public void setAction(AlarmAction action) {
        this.action = action != null ? action : AlarmAction.POPUP_MESSAGE;
    }

    public String getActionParam() {
        return actionParam;
    }

    public void setActionParam(String actionParam) {
        this.actionParam = actionParam != null ? actionParam : "";
    }

    public boolean isTriggerOnce() {
        return triggerOnce;
    }

    public void setTriggerOnce(boolean triggerOnce) {
        this.triggerOnce = triggerOnce;
    }

    public int getGuardAlarmIndex() {
        return guardAlarmIndex;
    }

    public void setGuardAlarmIndex(int guardAlarmIndex) {
        this.guardAlarmIndex = guardAlarmIndex;
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
}
