package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.artisan.controller.DisplaySettings;

import java.util.function.Consumer;

/**
 * Curve legend / toggles: list of curves with visibility checkboxes and color indicators.
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
        setSpacing(6);
        setPadding(new Insets(8));

        btCheck = new CheckBox("BT");
        btCheck.setSelected(displaySettings != null && displaySettings.isVisibleBT());
        etCheck = new CheckBox("ET");
        etCheck.setSelected(displaySettings != null && displaySettings.isVisibleET());
        deltaBTCheck = new CheckBox("RoR (ΔBT)");
        deltaBTCheck.setSelected(displaySettings != null && displaySettings.isVisibleDeltaBT());
        deltaETCheck = new CheckBox("ΔET");
        deltaETCheck.setSelected(displaySettings != null && displaySettings.isVisibleDeltaET());

        Consumer<Boolean> sync = v -> {
            if (onVisibilityChanged != null) onVisibilityChanged.run();
        };
        btCheck.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleBT(btCheck.isSelected()); sync.accept(true); });
        etCheck.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleET(etCheck.isSelected()); sync.accept(true); });
        deltaBTCheck.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleDeltaBT(deltaBTCheck.isSelected()); sync.accept(true); });
        deltaETCheck.setOnAction(e -> { if (displaySettings != null) displaySettings.setVisibleDeltaET(deltaETCheck.isSelected()); sync.accept(true); });

        getChildren().addAll(btCheck, etCheck, deltaBTCheck, deltaETCheck);
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
}
