package org.artisan.model;

public class ModulationAction {
    public enum ActionType {
        GAS, AIR, DRUM, CHARGE, TP, DRY_END, FC_START, FC_END, DROP
    }

    private ActionType type;
    private double triggerTimeSec;
    private int targetValue;
    private String label;
    private String timeStr;

    public ModulationAction(ActionType type, double triggerTimeSec,
                            int targetValue, String label, String timeStr) {
        this.type = type;
        this.triggerTimeSec = triggerTimeSec;
        this.targetValue = targetValue;
        this.label = label;
        this.timeStr = timeStr;
    }

    public ActionType getType() { return type; }
    public double getTriggerTimeSec() { return triggerTimeSec; }
    public int getTargetValue() { return targetValue; }
    public String getLabel() { return label; }
    public String getTimeStr() { return timeStr; }
}
