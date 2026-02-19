package org.artisan.view;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import org.artisan.controller.AppController.DisplayState;

/**
 * Bottom status bar: [ BT ] [ ET ] [ Time ] [ State ].
 * Updated via SampleListener and setState(). State colors: SAMPLING=green, IDLE=gray, ERROR=red.
 */
public final class StatusBar extends HBox {

    private static final String DASH = "—";
    private static final String ROR_DASH = "–––";
    private static final double PADDING = 4.0;
    private static final int SEPARATOR_PADDING = 8;

    private final Label btLabel;
    private final Label etLabel;
    private final Label rorLabel;
    private final Label timeLabel;
    private final Label stateLabel;

    public StatusBar() {
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(PADDING));
        setSpacing(SEPARATOR_PADDING);

        btLabel = new Label(formatLcd("BT", DASH));
        etLabel = new Label(formatLcd("ET", DASH));
        rorLabel = new Label(formatLcd("RoR", ROR_DASH));
        timeLabel = new Label(formatLcd("Time", DASH));
        stateLabel = new Label(formatLcd("State", DASH));
        stateLabel.setTextFill(Color.GRAY);

        getChildren().addAll(
            btLabel,
            new Separator(Orientation.VERTICAL),
            etLabel,
            new Separator(Orientation.VERTICAL),
            rorLabel,
            new Separator(Orientation.VERTICAL),
            timeLabel,
            new Separator(Orientation.VERTICAL),
            stateLabel
        );
    }

    private static String formatLcd(String name, String value) {
        return "  [ " + name + ": " + value + " ]  ";
    }

    /**
     * Updates BT, ET, RoR (BT), Time from a sample. Call from SampleListener (FX thread).
     */
    public void updateSample(double bt, double et, double rorBT, double rorET, double timeSec) {
        String btStr = formatTemp(bt);
        String etStr = formatTemp(et);
        String rorStr = formatRor(rorBT);
        String timeStr = formatTime(timeSec);
        btLabel.setText(formatLcd("BT", btStr));
        etLabel.setText(formatLcd("ET", etStr));
        rorLabel.setText(formatLcd("RoR", rorStr));
        timeLabel.setText(formatLcd("Time", timeStr));
    }

    /**
     * Updates the state label and its color. Call from FX thread.
     */
    public void setState(DisplayState state) {
        String text = state != null ? state.name() : DASH;
        stateLabel.setText(formatLcd("State", text));
        if (state == DisplayState.SAMPLING) {
            stateLabel.setTextFill(Color.GREEN);
        } else if (state == DisplayState.ERROR) {
            stateLabel.setTextFill(Color.RED);
        } else {
            stateLabel.setTextFill(Color.GRAY);
        }
    }

    private static String formatTemp(double c) {
        if (!Double.isFinite(c)) return DASH;
        return String.format("%.1f °C", c);
    }

    private static String formatTime(double totalSeconds) {
        if (!Double.isFinite(totalSeconds) || totalSeconds < 0) return DASH;
        int total = (int) Math.round(totalSeconds);
        int m = total / 60;
        int s = total % 60;
        return String.format("%d:%02d", m, s);
    }

    private static String formatRor(double ror) {
        if (!Double.isFinite(ror)) return ROR_DASH;
        return String.format("%+.1f", ror);
    }
}
