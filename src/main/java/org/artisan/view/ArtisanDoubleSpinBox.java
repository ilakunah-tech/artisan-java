package org.artisan.view;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.ScrollEvent;


/**
 * Double spin box with min/max/step and validation; value is clamped to range.
 * Analog of MyQDoubleSpinBox / QDoubleSpinBox with C locale in widgets.py.
 */
public class ArtisanDoubleSpinBox extends Spinner<Double> {

    private final double min;
    private final double max;
    private final double step;

    public ArtisanDoubleSpinBox(double min, double max, double initial, double step) {
        super();
        this.min = min;
        this.max = max;
        this.step = step;
        double value = clamp(initial);
        setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, value, step));
        getStyleClass().add("artisan-double-spin-box");
        setFocusTraversable(true);
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if (!isFocused()) {
                e.consume();
            }
        });
    }

    public ArtisanDoubleSpinBox(double min, double max, double initial) {
        this(min, max, initial, 1.0);
    }

    public ArtisanDoubleSpinBox(double min, double max) {
        this(min, max, min, 1.0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value) {
        return clamp(value, min, max);
    }

    /**
     * Current value, guaranteed in [min, max].
     */
    public double getDoubleValue() {
        return clamp(getValue());
    }

    /**
     * Set value; clamped to range.
     */
    @SuppressWarnings("unchecked")
    public void setDoubleValue(double value) {
        ((SpinnerValueFactory.DoubleSpinnerValueFactory) getValueFactory()).setValue(clamp(value));
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }
}
