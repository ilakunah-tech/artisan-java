package org.artisan.view;

import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Slider with optional tick labels (min/max or custom).
 * Analog of QSlider usage in widgets.py (SliderUnclickable + labels).
 */
public class ArtisanSlider extends VBox {

    private final Slider slider;
    private final double min;
    private final double max;

    public ArtisanSlider(double min, double max, double value) {
        super(2);
        this.min = min;
        this.max = max;
        slider = new Slider(min, max, clamp(value, min, max));
        slider.getStyleClass().add("artisan-slider");
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit((max - min) / 5.0);
        slider.setMinorTickCount(1);
        getChildren().add(slider);
    }

    public ArtisanSlider(double min, double max) {
        this(min, max, min);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public Slider getSlider() {
        return slider;
    }

    public double getValue() {
        return clamp(slider.getValue(), min, max);
    }

    public void setValue(double value) {
        slider.setValue(clamp(value, min, max));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    /**
     * Add a label row below the slider (e.g. "min — max").
     */
    public void setLabel(String label) {
        Text t = new Text(label);
        t.getStyleClass().add("artisan-slider-label");
        getChildren().add(t);
    }
}
