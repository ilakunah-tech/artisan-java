package org.artisan.view;

import javafx.scene.paint.Color;

/**
 * Level for toast notifications: INFO, WARNING, ERROR.
 * Each level has an associated background color for NotificationSystem.
 */
public enum NotificationLevel {
    INFO("#2196F3"),
    WARNING("#FF9800"),
    ERROR("#F44336");

    private final String hexColor;

    NotificationLevel(String hexColor) {
        this.hexColor = hexColor != null ? hexColor : "#888888";
    }

    /** Returns the JavaFX Color for this level (non-null). */
    public Color getColor() {
        try {
            return Color.web(hexColor);
        } catch (Exception e) {
            return Color.GRAY;
        }
    }

    public String getHexColor() {
        return hexColor;
    }
}
