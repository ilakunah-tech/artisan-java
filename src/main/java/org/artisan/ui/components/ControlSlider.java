package org.artisan.ui.components;

import javafx.beans.property.Property;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Vertical control slider with numeric tick buttons and a value readout.
 */
public final class ControlSlider extends HBox {

    private static final double SLIDER_HEIGHT = 280;
    private static final double SLIDER_WIDTH = 20;

    private final Slider slider;

    public ControlSlider(String label, Color trackColor) {
        setAlignment(Pos.CENTER);
        setSpacing(4);

        VBox leftTicks = buildTickColumn(
            new int[] {100, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0},
            Pos.TOP_RIGHT,
            SLIDER_HEIGHT / 11
        );
        leftTicks.setPrefHeight(SLIDER_HEIGHT);

        VBox rightTicks = buildTickColumn(
            new int[] {95, 85, 75, 65, 55, 45, 35, 25, 15, 5},
            Pos.TOP_LEFT,
            SLIDER_HEIGHT / 10
        );
        rightTicks.setPrefHeight(SLIDER_HEIGHT);

        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280; -fx-alignment: CENTER;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        slider = new Slider(0, 100, 50);
        slider.setOrientation(Orientation.VERTICAL);
        slider.setMinHeight(SLIDER_HEIGHT);
        slider.setPrefHeight(SLIDER_HEIGHT);
        slider.setPrefWidth(SLIDER_WIDTH);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.setSnapToTicks(false);
        if ("Burn".equalsIgnoreCase(label)) {
            slider.getStyleClass().add("burn-slider");
        } else if ("Air".equalsIgnoreCase(label)) {
            slider.getStyleClass().add("air-slider");
        }

        Label valueLabel = new Label();
        valueLabel.setStyle(String.format(
            "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: %s;",
            toRgb(trackColor)
        ));
        valueLabel.setAlignment(Pos.CENTER);
        valueLabel.setMaxWidth(Double.MAX_VALUE);
        valueLabel.setText(String.valueOf((int) Math.round(slider.getValue())));
        slider.valueProperty().addListener((obs, oldVal, newVal) ->
            valueLabel.setText(String.valueOf((int) Math.round(newVal.doubleValue()))));

        VBox sliderBox = new VBox(2, titleLabel, slider, valueLabel);
        sliderBox.setAlignment(Pos.CENTER);
        sliderBox.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(sliderBox, Priority.ALWAYS);
        VBox.setVgrow(slider, Priority.ALWAYS);

        getChildren().addAll(leftTicks, sliderBox, rightTicks);
    }

    private VBox buildTickColumn(int[] values, Pos alignment, double itemHeight) {
        VBox box = new VBox(0);
        box.setAlignment(alignment);
        for (int value : values) {
            Button btn = new Button(String.valueOf(value));
            btn.getStyleClass().add("tick-btn");
            btn.setPrefWidth(34);
            btn.setPrefHeight(itemHeight);
            btn.setOnAction(e -> slider.setValue(value));
            box.getChildren().add(btn);
        }
        return box;
    }

    private static String toRgb(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }

    public Slider getSlider() {
        return slider;
    }

    public void bindBidirectional(Property<Number> prop) {
        slider.valueProperty().bindBidirectional(prop);
    }

    public double getValue() {
        return slider.getValue();
    }
}
