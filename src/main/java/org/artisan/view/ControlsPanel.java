package org.artisan.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.artisan.controller.AppController;
import org.artisan.model.EventType;

/**
 * Event chips (pill buttons) and sliders (Gas, Air, Drum).
 * "Show controls" toggle lives in card header; this panel exposes it.
 */
public final class ControlsPanel extends VBox {

    private final AppController appController;
    private final Slider gasSlider;
    private final Slider airSlider;
    private final Slider drumSlider;
    private final VBox sliderContent;
    private final ToggleButton showControlsToggle;

    public ControlsPanel(AppController appController) {
        this.appController = appController;
        setSpacing(10);
        setPadding(new Insets(0));

        FlowPane eventChips = new FlowPane(6, 6);
        eventChips.getStyleClass().add("ri5-event-chips");
        eventChips.getChildren().addAll(
            eventChip("Charge", EventType.CHARGE),
            eventChip("TP", EventType.TP),
            eventChip("Dry End", EventType.DRY_END),
            eventChip("FC Start", EventType.FC_START),
            eventChip("FC End", EventType.FC_END),
            eventChip("SC Start", EventType.SC_START),
            eventChip("SC End", EventType.SC_END),
            eventChip("Drop", EventType.DROP)
        );

        showControlsToggle = new ToggleButton("Controls");
        showControlsToggle.setSelected(true);
        showControlsToggle.getStyleClass().addAll("ri5-controls-toggle-compact");
        showControlsToggle.setTooltip(new Tooltip("Show/hide sliders (C)"));

        gasSlider = slider("Gas", "Gas");
        airSlider = slider("Air", "Air");
        drumSlider = slider("Drum", "Drum");
        HBox sliderRow = new HBox(16);
        sliderRow.getChildren().addAll(
            new VBox(4, new Label("Gas"), gasSlider),
            new VBox(4, new Label("Air"), airSlider),
            new VBox(4, new Label("Drum"), drumSlider)
        );

        sliderContent = new VBox(8, sliderRow);
        showControlsToggle.setOnAction(e -> {
            boolean show = showControlsToggle.isSelected();
            sliderContent.setManaged(show);
            sliderContent.setVisible(show);
        });

        getChildren().addAll(eventChips, sliderContent);
    }

    public ToggleButton getShowControlsToggle() {
        return showControlsToggle;
    }

    /** Sets whether the sliders area is visible. Call to sync with LayoutState. */
    public void setSlidersVisible(boolean visible) {
        showControlsToggle.setSelected(visible);
        sliderContent.setManaged(visible);
        sliderContent.setVisible(visible);
    }

    private Button eventChip(String label, EventType type) {
        Button b = new Button(label);
        b.getStyleClass().add("ri5-event-chip");
        b.setOnAction(e -> {
            if (appController != null) appController.markEvent(type);
        });
        return b;
    }

    private Slider slider(String name, String key) {
        Slider s = new Slider(0, 100, 0);
        s.getStyleClass().addAll("controls-slider", "ri5-slider");
        s.setShowTickMarks(true);
        s.setShowTickLabels(true);
        s.setMajorTickUnit(25);
        s.setMinorTickCount(5);
        s.setPrefWidth(120);
        s.valueProperty().addListener((a, b, c) -> {
            if (appController != null) appController.setControlOutput(key, s.getValue());
        });
        return s;
    }
}
