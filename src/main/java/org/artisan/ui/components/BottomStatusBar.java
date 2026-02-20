package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Bottom status strip: phase, timer, connection status, controls toggle, end button, warnings.
 */
public final class BottomStatusBar extends HBox {

    private final Label phaseLabel;
    private final Label timerLabel;
    private final Label connectionLabel;
    private final ToggleButton controlsToggle;
    private final Button endButton;
    private final Label warningLabel;

    public BottomStatusBar() {
        getStyleClass().add("ri5-status-strip");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(16);
        setPadding(new Insets(6, 12, 6, 12));
        setMinHeight(38);
        setPrefHeight(38);
        setMaxHeight(38);

        phaseLabel = new Label("—");
        timerLabel = new Label("0:00");
        connectionLabel = new Label("Disconnected");
        controlsToggle = new ToggleButton("Controls");
        controlsToggle.setTooltip(new Tooltip("Show/Hide Controls panel (C)"));
        controlsToggle.getStyleClass().add("ri5-controls-toggle");
        endButton = new Button("End Roast");
        endButton.setTooltip(new Tooltip("Stop recording and end roast"));
        endButton.getStyleClass().add("ri5-end-button");
        warningLabel = new Label("");
        warningLabel.getStyleClass().add("ri5-warning");
        warningLabel.setManaged(false);
        warningLabel.setVisible(false);
        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(phaseLabel, timerLabel, connectionLabel, spacer, controlsToggle, endButton, warningLabel);
    }

    public void setPhase(String phase) {
        phaseLabel.setText(phase != null ? phase : "—");
    }

    public void setElapsedSeconds(double sec) {
        int total = (int) Math.round(sec);
        int m = total / 60;
        int s = total % 60;
        timerLabel.setText(String.format("%d:%02d", m, s));
    }

    public void setConnectionStatus(String status) {
        connectionLabel.setText(status != null ? status : "Disconnected");
    }

    public void setWarning(String warning) {
        warningLabel.setText(warning != null ? warning : "");
        boolean show = warning != null && !warning.isEmpty();
        warningLabel.setManaged(show);
        warningLabel.setVisible(show);
    }

    /** Call when Controls panel visibility is toggled (e.g. by shortcut C). */
    public void setControlsVisible(boolean visible) {
        controlsToggle.setSelected(visible);
    }

    public ToggleButton getControlsToggle() {
        return controlsToggle;
    }

    public Button getEndButton() {
        return endButton;
    }

    /** Set callback for End Roast button. */
    public void setOnEndRoast(Runnable runnable) {
        endButton.setOnAction(e -> {
            if (runnable != null) runnable.run();
        });
    }

    /** Set callback for Controls toggle (Show/Hide). Called when user clicks the toggle. */
    public void setOnControlsToggle(Runnable runnable) {
        controlsToggle.setOnAction(e -> {
            if (runnable != null) runnable.run();
        });
    }

    public Label getPhaseLabel() { return phaseLabel; }
    public Label getTimerLabel() { return timerLabel; }
    public Label getConnectionLabel() { return connectionLabel; }
    public Label getWarningLabel() { return warningLabel; }
}
