package org.artisan.model;

import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Model for a single programmable event button (toolbar and config).
 * Persisted in ~/.artisan/eventbuttons.json.
 */
public class EventButtonConfig {

    private static final int MAX_LABEL_LEN = 10;

    private String label = "";
    private String description = "";
    private EventType type = EventType.CUSTOM;
    private double value = 0.0;
    private Color color = Color.GRAY;
    private boolean visible = true;
    private String action = "";
    private String actionParam = "";

    public EventButtonConfig() {}

    public EventButtonConfig(String label, String description, EventType type, double value,
                             Color color, boolean visible, String action, String actionParam) {
        this.label = truncateLabel(label != null ? label : "");
        this.description = description != null ? description : "";
        this.type = type != null ? type : EventType.CUSTOM;
        this.value = clampValue(value);
        this.color = color != null ? color : Color.GRAY;
        this.visible = visible;
        this.action = action != null ? action : "";
        this.actionParam = actionParam != null ? actionParam : "";
    }

    public EventButtonConfig(EventButtonConfig other) {
        if (other == null) return;
        this.label = other.label;
        this.description = other.description;
        this.type = other.type;
        this.value = other.value;
        this.color = other.color;
        this.visible = other.visible;
        this.action = other.action;
        this.actionParam = other.actionParam;
    }

    private static String truncateLabel(String s) {
        if (s == null) return "";
        return s.length() > MAX_LABEL_LEN ? s.substring(0, MAX_LABEL_LEN) : s;
    }

    private static double clampValue(double v) {
        if (!Double.isFinite(v)) return 0.0;
        if (v < 0.0) return 0.0;
        if (v > 100.0) return 100.0;
        return v;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = truncateLabel(label != null ? label : ""); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type != null ? type : EventType.CUSTOM; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = clampValue(value); }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color != null ? color : Color.GRAY; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action != null ? action : ""; }

    public String getActionParam() { return actionParam; }
    public void setActionParam(String actionParam) { this.actionParam = actionParam != null ? actionParam : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventButtonConfig that = (EventButtonConfig) o;
        return Double.compare(that.value, value) == 0
                && visible == that.visible
                && Objects.equals(label, that.label)
                && Objects.equals(description, that.description)
                && type == that.type
                && Objects.equals(color, that.color)
                && Objects.equals(action, that.action)
                && Objects.equals(actionParam, that.actionParam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, description, type, value, color, visible, action, actionParam);
    }
}
