package org.artisan.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.artisan.controller.AppController;
import org.artisan.model.EventType;

/**
 * Inline panel: event buttons and sliders (Gas, Air, Drum).
 * "Show controls" toggle collapses/expands the sliders area.
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
        setSpacing(12);
        setPadding(new Insets(0));

        HBox eventRow = new HBox(8);
        eventRow.getChildren().addAll(
            button("CHARGE", EventType.CHARGE),
            button("TP", EventType.TP),
            button("DRY END", EventType.DRY_END),
            button("FC START", EventType.FC_START),
            button("FC END", EventType.FC_END),
            button("SC START", EventType.SC_START),
            button("SC END", EventType.SC_END),
            button("DROP", EventType.DROP)
        );

        showControlsToggle = new ToggleButton("Show controls");
        showControlsToggle.setSelected(true);
        showControlsToggle.getStyleClass().add("ri5-controls-toggle-inline");

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

        getChildren().addAll(eventRow, showControlsToggle, sliderContent);
    }

    /** Sets whether the sliders area is visible. Call to sync with LayoutState. */
    public void setSlidersVisible(boolean visible) {
        showControlsToggle.setSelected(visible);
        sliderContent.setManaged(visible);
        sliderContent.setVisible(visible);
    }

    private Button button(String text, EventType type) {
        Button b = new Button(text);
        b.getStyleClass().add("event-button");
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
