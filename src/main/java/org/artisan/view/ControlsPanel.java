package org.artisan.view;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.artisan.controller.AppController;
import org.artisan.model.EventType;
import org.artisan.ui.vm.RoastViewModel;

/**
 * Event chips (pill buttons) and sliders (Gas, Air, Drum).
 * "Show controls" toggle lives in card header; this panel exposes it.
 * When RoastViewModel provided, sliders bind bidirectionally.
 */
public final class ControlsPanel extends VBox {

    private final AppController appController;
    private final Slider gasSlider;
    private final Slider airSlider;
    private final Slider drumSlider;
    private final VBox sliderContent;
    private final ToggleButton showControlsToggle;

    public ControlsPanel(AppController appController) {
        this(appController, null);
    }

    public ControlsPanel(AppController appController, RoastViewModel vm) {
        this.appController = appController;
        setSpacing(12);
        setPadding(new Insets(0));

        FlowPane eventChips = new FlowPane(6, 6);
        eventChips.getStyleClass().add("ri5-event-chips");
        eventChips.setPrefWrapLength(260);
        eventChips.getChildren().addAll(
            eventChip("Charge", EventType.CHARGE, "CHARGE (1)"),
            eventChip("TP", EventType.TP, "Turning Point"),
            eventChip("Dry End", EventType.DRY_END, "Dry End (2)"),
            eventChip("FC Start", EventType.FC_START, "FC Start (3)"),
            eventChip("FC End", EventType.FC_END, "FC End (4)"),
            eventChip("SC Start", EventType.SC_START, "SC Start"),
            eventChip("SC End", EventType.SC_END, "SC End"),
            eventChip("Drop", EventType.DROP, "DROP (5)")
        );

        showControlsToggle = new ToggleButton("Controls");
        showControlsToggle.setSelected(true);
        showControlsToggle.getStyleClass().addAll("ri5-controls-toggle-compact");
        showControlsToggle.setTooltip(new Tooltip("Show/hide sliders (C)"));

        gasSlider = slider("Gas", "Gas");
        airSlider = slider("Air", "Air");
        drumSlider = slider("Drum", "Drum");
        if (vm != null) {
            Bindings.bindBidirectional(gasSlider.valueProperty(), vm.gasValueProperty());
            Bindings.bindBidirectional(airSlider.valueProperty(), vm.airValueProperty());
            Bindings.bindBidirectional(drumSlider.valueProperty(), vm.drumValueProperty());
        }
        GridPane sliderGrid = new GridPane();
        sliderGrid.setHgap(12);
        sliderGrid.setVgap(10);
        sliderGrid.add(sliderGroupWithButtons("Gas", gasSlider, "Gas"), 0, 0);
        sliderGrid.add(sliderGroupWithButtons("Air", airSlider, "Air"), 1, 0);
        sliderGrid.add(sliderGroupWithButtons("Drum", drumSlider, "Drum"), 0, 1);
        GridPane.setHgrow(sliderGrid.getChildren().get(0), Priority.ALWAYS);
        GridPane.setHgrow(sliderGrid.getChildren().get(1), Priority.ALWAYS);
        GridPane.setHgrow(sliderGrid.getChildren().get(2), Priority.ALWAYS);

        sliderContent = new VBox(8, sliderGrid);
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

    private Button eventChip(String label, EventType type, String tooltipText) {
        Button b = new Button(label);
        b.getStyleClass().add("ri5-event-chip");
        b.setTooltip(new Tooltip(tooltipText));
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
        s.setBlockIncrement(5);
        s.setPrefWidth(160);
        s.setMinWidth(120);
        s.setMaxWidth(Double.MAX_VALUE);
        s.valueProperty().addListener((a, b, c) -> {
            if (appController != null) appController.setControlOutput(key, s.getValue());
        });
        return s;
    }

    private VBox sliderGroupWithButtons(String labelText, Slider slider, String key) {
        Label label = new Label(labelText);
        TextField tf = new TextField();
        tf.setPrefColumnCount(4);
        tf.setTextFormatter(new javafx.scene.control.TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) return c;
            try {
                double v = Double.parseDouble(c.getControlNewText());
                if (v >= 0 && v <= 100) return c;
            } catch (NumberFormatException ignored) {}
            return null;
        }));
        slider.valueProperty().addListener((a, ov, nv) -> {
            if (!tf.isFocused()) tf.setText(String.format("%.0f", nv.doubleValue()));
        });
        Runnable commitTf = () -> {
            try {
                double v = Double.parseDouble(tf.getText());
                slider.setValue(Math.max(0, Math.min(100, v)));
            } catch (NumberFormatException ignored) {}
        };
        tf.setOnAction(e -> commitTf.run());
        tf.focusedProperty().addListener((a, wasFocused, nowFocused) -> {
            if (wasFocused && !nowFocused) commitTf.run();
        });
        Button minusBtn = new Button("−");
        minusBtn.getStyleClass().add("ri5-slider-step-btn");
        minusBtn.setOnAction(e -> slider.setValue(Math.max(0, slider.getValue() - 1)));
        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("ri5-slider-step-btn");
        plusBtn.setOnAction(e -> slider.setValue(Math.min(100, slider.getValue() + 1)));
        HBox row = new HBox(4, label, tf, minusBtn, slider, plusBtn);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(slider, Priority.ALWAYS);
        tf.setText(String.format("%.0f", slider.getValue()));
        VBox box = new VBox(4, row);
        box.getStyleClass().add("ri5-slider-group");
        return box;
    }
}
