package org.artisan.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Reusable JavaFX component: a styled Label-based LCD display.
 * Color-coded by type: BT, ET, RoR, or default.
 */
public final class LCDPanel extends VBox {

    private static final String STYLE_CLASS = "lcd-panel";
    private static final String BG_COLOR = "#1a1a1a";
    private static final String FONT_MONO = "monospace";
    private static final int FONT_SIZE = 24;
    private static final int PREF_WIDTH = 120;
    private static final int PREF_HEIGHT = 60;

    /** Color for BT (bean temp). */
    public static final String COLOR_BT = "#FF6600";
    /** Color for ET (env temp). */
    public static final String COLOR_ET = "#FF0000";
    /** Color for RoR. */
    public static final String COLOR_ROR = "#00CC00";
    /** Default text color. */
    public static final String COLOR_DEFAULT = "#CCCCCC";

    private final Label valueLabel;
    private final String unit;
    private String textColor = COLOR_DEFAULT;

    /**
     * Creates an LCD panel with label and unit. Color is default until set via
     * {@link #setColor(String)} (e.g. COLOR_BT, COLOR_ET, COLOR_ROR).
     *
     * @param labelText title shown above the value (e.g. "BT", "ET")
     * @param unit      unit suffix (e.g. "°C", "°C/min"); may be empty
     */
    public LCDPanel(String labelText, String unit) {
        this.unit = unit != null ? unit : "";
        Label titleLabel = new Label(labelText != null ? labelText : "");
        titleLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
        valueLabel = new Label(ValueFormatter.format(Double.NaN, 1));
        valueLabel.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-family: %s; -fx-font-size: %dpx; -fx-padding: 4px;",
            BG_COLOR, COLOR_DEFAULT, FONT_MONO, FONT_SIZE));
        getChildren().addAll(titleLabel, valueLabel);
        setAlignment(Pos.CENTER);
        setSpacing(2);
        setPadding(new Insets(4));
        setPrefSize(PREF_WIDTH, PREF_HEIGHT);
        getStyleClass().add(STYLE_CLASS);
        setStyle("-fx-background-color: " + BG_COLOR + ";");
    }

    /**
     * Sets the display color (e.g. COLOR_BT, COLOR_ET, COLOR_ROR, COLOR_DEFAULT).
     */
    public void setColor(String color) {
        if (color != null && !color.isEmpty()) {
            this.textColor = color;
            valueLabel.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-family: %s; -fx-font-size: %dpx; -fx-padding: 4px;",
                BG_COLOR, textColor, FONT_MONO, FONT_SIZE));
        }
    }

    /**
     * Updates the display with a numeric value (formatted to 1 decimal place).
     * Uses "–––" for NaN/Infinite.
     */
    public void setValue(double v) {
        valueLabel.setText(ValueFormatter.format(v, 1) + (unit.isEmpty() ? "" : " " + unit));
    }

    /**
     * Sets the display to arbitrary text (e.g. "–––").
     */
    public void setValueText(String text) {
        valueLabel.setText(text != null ? text : "–––");
    }

    public Label getValueLabel() {
        return valueLabel;
    }
}
