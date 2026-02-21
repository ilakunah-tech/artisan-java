package org.artisan.ui.model;

import java.util.Objects;

/**
 * Rule for smart customizable alerts.
 * Evaluated against current curve values; fires toast and/or audible notification when threshold is met.
 */
public final class AlertRule {

    public enum NotificationType { VISUAL, AUDIBLE, BOTH }

    private String id;
    private String curveName;       // BT, ET, RoR, gas, air
    private String operator;        // ">", "<", "="
    private double threshold;
    private int countdownSeconds;   // if > 0: alert fires N seconds before event
    private NotificationType type;
    private String message;

    public AlertRule() {
        this.id = "";
        this.curveName = "BT";
        this.operator = ">";
        this.threshold = 0;
        this.countdownSeconds = 0;
        this.type = NotificationType.VISUAL;
        this.message = "";
    }

    public AlertRule(String id, String curveName, String operator, double threshold,
                     int countdownSeconds, NotificationType type, String message) {
        this.id = id != null ? id : "";
        this.curveName = curveName != null ? curveName : "BT";
        this.operator = operator != null ? operator : ">";
        this.threshold = threshold;
        this.countdownSeconds = countdownSeconds;
        this.type = type != null ? type : NotificationType.VISUAL;
        this.message = message != null ? message : "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id != null ? id : ""; }

    public String getCurveName() { return curveName; }
    public void setCurveName(String curveName) { this.curveName = curveName != null ? curveName : "BT"; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator != null ? operator : ">"; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public int getCountdownSeconds() { return countdownSeconds; }
    public void setCountdownSeconds(int countdownSeconds) { this.countdownSeconds = countdownSeconds; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type != null ? type : NotificationType.VISUAL; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message != null ? message : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertRule that = (AlertRule) o;
        return Double.compare(that.threshold, threshold) == 0
                && countdownSeconds == that.countdownSeconds
                && Objects.equals(id, that.id)
                && Objects.equals(curveName, that.curveName)
                && Objects.equals(operator, that.operator)
                && type == that.type
                && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, curveName, operator, threshold, countdownSeconds, type, message);
    }
}
