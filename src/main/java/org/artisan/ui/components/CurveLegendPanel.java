package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.artisan.controller.DisplaySettings;

import java.util.function.Consumer;

/**
 * Curve legend: each row has a colored line indicator, label, and toggle switch.
 * Cropster RI5 style: toggle switches instead of checkboxes; hover/selection focus dims other rows.
 */
public final class CurveLegendPanel extends VBox {

    private final ToggleButton btToggle;
    private final ToggleButton etToggle;
    private final ToggleButton deltaBTToggle;
    private final ToggleButton deltaETToggle;
    private final HBox btRow;
    private final HBox etRow;
    private final HBox deltaBTRow;
    private final HBox deltaETRow;
    private DisplaySettings displaySettings;
    private Runnable onVisibilityChanged;

    public CurveLegendPanel(DisplaySettings displaySettings) {
        this.displaySettings = displaySettings;
        getStyleClass().add("curve-legend-panel");
        setSpacing(6);
        setPadding(new Insets(0));

        btRow = legendRow("BT", getColorHex(displaySettings, "bt"), displaySettings != null && displaySettings.isVisibleBT());
        etRow = legendRow("ET", getColorHex(displaySettings, "et"), displaySettings != null && displaySettings.isVisibleET());
        deltaBTRow = legendRow("RoR (ΔBT)", getColorHex(displaySettings, "deltabt"), displaySettings != null && displaySettings.isVisibleDeltaBT());
        deltaETRow = legendRow("ΔET", getColorHex(displaySettings, "deltaet"), displaySettings != null && displaySettings.isVisibleDeltaET());

        btToggle = (ToggleButton) btRow.getChildren().get(2);
        etToggle = (ToggleButton) etRow.getChildren().get(2);
        deltaBTToggle = (ToggleButton) deltaBTRow.getChildren().get(2);
        deltaETToggle = (ToggleButton) deltaETRow.getChildren().get(2);

        Consumer<Boolean> sync = v -> { if (onVisibilityChanged != null) onVisibilityChanged.run(); };
        btToggle.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleBT(btToggle.isSelected()); sync.accept(true); });
        etToggle.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleET(etToggle.isSelected()); sync.accept(true); });
        deltaBTToggle.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleDeltaBT(deltaBTToggle.isSelected()); sync.accept(true); });
        deltaETToggle.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleDeltaET(deltaETToggle.isSelected()); sync.accept(true); });

        setupFocusDimming(btRow, etRow, deltaBTRow, deltaETRow);

        getChildren().addAll(btRow, etRow, deltaBTRow, deltaETRow);
    }

    private static String getColorHex(DisplaySettings ds, String key) {
        if (ds == null) return "#6B7280";
        switch (key) {
            case "bt": return ds.getPaletteCurveBT();
            case "et": return ds.getPaletteCurveET();
            case "deltabt": return ds.getPaletteCurveDeltaBT();
            case "deltaet": return ds.getPaletteCurveDeltaET();
            default: return "#6B7280";
        }
    }

    private HBox legendRow(String label, String colorHex, boolean selected) {
        Region line = new Region();
        line.setMinSize(20, 3);
        line.setPrefSize(20, 3);
        line.setMaxHeight(3);
        line.setStyle("-fx-background-color: " + (colorHex != null ? colorHex : "#6B7280") + "; -fx-background-radius: 2;");
        line.getStyleClass().add("curve-legend-line");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("curve-legend-label");

        ToggleButton toggle = new ToggleButton();
        toggle.setSelected(selected);
        toggle.getStyleClass().addAll("curve-legend-switch", "curve-legend-toggle");
        toggle.setMinWidth(36);
        toggle.setPrefWidth(36);
        toggle.setMaxWidth(36);
        toggle.setMinHeight(20);
        toggle.setPrefHeight(20);

        HBox row = new HBox(8);
        row.getStyleClass().add("curve-legend-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(line, lbl, toggle);
        HBox.setHgrow(lbl, Priority.ALWAYS);
        return row;
    }

    private void setupFocusDimming(HBox... rows) {
        for (HBox row : rows) {
            row.setOnMouseEntered(e -> {
                for (HBox r : rows) r.getStyleClass().remove("curve-legend-row-focused");
                for (HBox r : rows) if (r != row) r.getStyleClass().add("curve-legend-row-dimmed");
            });
            row.setOnMouseExited(e -> {
                for (HBox r : rows) {
                    r.getStyleClass().remove("curve-legend-row-focused");
                    r.getStyleClass().remove("curve-legend-row-dimmed");
                }
            });
        }
    }

    public void setDisplaySettings(DisplaySettings displaySettings) {
        this.displaySettings = displaySettings;
        if (displaySettings != null) {
            btToggle.setSelected(displaySettings.isVisibleBT());
            etToggle.setSelected(displaySettings.isVisibleET());
            deltaBTToggle.setSelected(displaySettings.isVisibleDeltaBT());
            deltaETToggle.setSelected(displaySettings.isVisibleDeltaET());
        }
    }

    public void setOnVisibilityChanged(Runnable onVisibilityChanged) {
        this.onVisibilityChanged = onVisibilityChanged;
    }

    /** Updates the line indicator colors when palette changes. */
    public void refreshColors(DisplaySettings ds) {
        if (ds == null) return;
        this.displaySettings = ds;
        updateRowColor(btRow, ds.getPaletteCurveBT());
        updateRowColor(etRow, ds.getPaletteCurveET());
        updateRowColor(deltaBTRow, ds.getPaletteCurveDeltaBT());
        updateRowColor(deltaETRow, ds.getPaletteCurveDeltaET());
    }

    private void updateRowColor(HBox row, String colorHex) {
        if (row.getChildren().isEmpty()) return;
        javafx.scene.Node first = row.getChildren().get(0);
        if (first instanceof Region) {
            ((Region) first).setStyle("-fx-background-color: " + (colorHex != null ? colorHex : "#6B7280") + "; -fx-background-radius: 2;");
        }
    }
}
