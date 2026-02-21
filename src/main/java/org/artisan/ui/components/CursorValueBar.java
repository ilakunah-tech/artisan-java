package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Thin HBox above the main chart showing BT and Time at cursor position.
 * Hidden when mouse leaves chart area.
 */
public final class CursorValueBar extends HBox {

    private static final String PLACEHOLDER = "---.--";
    private static final String TIME_PLACEHOLDER = "--:--";

    private final Label btLabel;
    private final Label timeLabel;

    public CursorValueBar() {
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(4, 12, 4, 12));
        setMinHeight(28);
        setPrefHeight(28);
        setMaxHeight(28);
        getStyleClass().add("cursor-value-bar");
        setVisible(false);

        btLabel = new Label("BT: " + PLACEHOLDER + " °C");
        timeLabel = new Label("Time: " + TIME_PLACEHOLDER);

        getChildren().addAll(btLabel, new Label(" | "), timeLabel);
    }

    /** Update display with cursor position values. Called on chart mouse-move. */
    public void update(double timeSec, double bt) {
        boolean valid = Double.isFinite(bt);
        btLabel.setText("BT: " + (valid ? String.format("%.1f", bt) : PLACEHOLDER) + " °C");
        int min = (int) (timeSec / 60);
        int sec = (int) (timeSec % 60);
        timeLabel.setText("Time: " + (timeSec >= 0 ? String.format("%02d:%02d", min, sec) : TIME_PLACEHOLDER));
    }

    /** Reset to placeholder values. */
    public void clear() {
        btLabel.setText("BT: " + PLACEHOLDER + " °C");
        timeLabel.setText("Time: " + TIME_PLACEHOLDER);
    }
}
