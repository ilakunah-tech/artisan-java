package org.artisan.view;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.ScrollEvent;

/**
 * Integer spin box with min/max validation; value is always clamped to range.
 * Analog of QSpinBox + QIntValidator in dialogs.py / widgets.
 */
public class ArtisanSpinBox extends Spinner<Integer> {

    private final int min;
    private final int max;

    public ArtisanSpinBox(int min, int max, int initial) {
        super();
        this.min = min;
        this.max = max;
        int value = clamp(initial);
        setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value));
        getStyleClass().add("artisan-spin-box");
        setFocusTraversable(true);
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if (!isFocused()) {
                e.consume();
            }
        });
    }

    public ArtisanSpinBox(int min, int max) {
        this(min, max, min);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clamp(int value) {
        return clamp(value, min, max);
    }

    /**
     * Current value, guaranteed in [min, max].
     */
    public int getIntValue() {
        return clamp(getValue());
    }

    /**
     * Set value; clamped to range.
     */
    @SuppressWarnings("unchecked")
    public void setIntValue(int value) {
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) getValueFactory()).setValue(clamp(value));
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
