package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.artisan.controller.DisplaySettings;

import java.util.function.Consumer;

/**
 * Curve legend: each row has a colored line indicator, name, and toggle.
 * Replaces the default checkbox list with a legend-list style.
 */
public final class CurveLegendPanel extends VBox {

    private final CheckBox btCheck;
    private final CheckBox etCheck;
    private final CheckBox deltaBTCheck;
    private final CheckBox deltaETCheck;
    private DisplaySettings displaySettings;
    private Runnable onVisibilityChanged;

    public CurveLegendPanel(DisplaySettings displaySettings) {
        this.displaySettings = displaySettings;
        getStyleClass().add("curve-legend-panel");
        setSpacing(8);
        setPadding(new Insets(0));

        btCheck = createLegendRow("BT", getColorHex(displaySettings, "bt"), displaySettings != null && displaySettings.isVisibleBT());
        etCheck = createLegendRow("ET", getColorHex(displaySettings, "et"), displaySettings != null && displaySettings.isVisibleET());
        deltaBTCheck = createLegendRow("RoR (ΔBT)", getColorHex(displaySettings, "deltabt"), displaySettings != null && displaySettings.isVisibleDeltaBT());
        deltaETCheck = createLegendRow("ΔET", getColorHex(displaySettings, "deltaet"), displaySettings != null && displaySettings.isVisibleDeltaET());

        Consumer<Boolean> sync = v -> {
            if (onVisibilityChanged != null) onVisibilityChanged.run();
        };
        btCheck.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleBT(btCheck.isSelected()); sync.accept(true); });
        etCheck.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleET(etCheck.isSelected()); sync.accept(true); });
        deltaBTCheck.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleDeltaBT(deltaBTCheck.isSelected()); sync.accept(true); });
        deltaETCheck.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleDeltaET(deltaETCheck.isSelected()); sync.accept(true); });

        String btHex = getColorHex(this.displaySettings, "bt");
        String etHex = getColorHex(this.displaySettings, "et");
        String deltabtHex = getColorHex(this.displaySettings, "deltabt");
        String deltaetHex = getColorHex(this.displaySettings, "deltaet");
        getChildren().addAll(
            legendRow(btCheck, btHex),
            legendRow(etCheck, etHex),
            legendRow(deltaBTCheck, deltabtHex),
            legendRow(deltaETCheck, deltaetHex)
        );
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

    private CheckBox createLegendRow(String label, String colorHex, boolean selected) {
        CheckBox cb = new CheckBox(label);
        cb.setSelected(selected);
        cb.getStyleClass().add("curve-legend-toggle");
        return cb;
    }

    private HBox legendRow(CheckBox check, String colorHex) {
        Region line = new Region();
        line.setMinSize(24, 3);
        line.setPrefSize(24, 3);
        line.setMaxHeight(3);
        line.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 2;");
        line.getStyleClass().add("curve-legend-line");

        HBox row = new HBox(10);
        row.getStyleClass().add("curve-legend-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(line, check);
        HBox.setHgrow(check, Priority.ALWAYS);
        return row;
    }

    public void setDisplaySettings(DisplaySettings displaySettings) {
        this.displaySettings = displaySettings;
        if (displaySettings != null) {
            btCheck.setSelected(displaySettings.isVisibleBT());
            etCheck.setSelected(displaySettings.isVisibleET());
            deltaBTCheck.setSelected(displaySettings.isVisibleDeltaBT());
            deltaETCheck.setSelected(displaySettings.isVisibleDeltaET());
        }
    }

    public void setOnVisibilityChanged(Runnable onVisibilityChanged) {
        this.onVisibilityChanged = onVisibilityChanged;
    }

    /** Updates the line indicator colors when palette changes. */
    public void refreshColors(DisplaySettings ds) {
        if (ds == null) return;
        this.displaySettings = ds;
        java.util.List<javafx.scene.Node> children = getChildren();
        if (children.size() >= 4) {
            updateRowColor((HBox) children.get(0), ds.getPaletteCurveBT());
            updateRowColor((HBox) children.get(1), ds.getPaletteCurveET());
            updateRowColor((HBox) children.get(2), ds.getPaletteCurveDeltaBT());
            updateRowColor((HBox) children.get(3), ds.getPaletteCurveDeltaET());
        }
    }

    private void updateRowColor(HBox row, String colorHex) {
        if (row.getChildren().isEmpty()) return;
        javafx.scene.Node first = row.getChildren().get(0);
        if (first instanceof Region) {
            ((Region) first).setStyle("-fx-background-color: " + (colorHex != null ? colorHex : "#6B7280") + "; -fx-background-radius: 2;");
        }
    }
}
