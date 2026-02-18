package org.artisan.view;

import javafx.scene.control.ComboBox;
import javafx.scene.input.ScrollEvent;

/**
 * Extended ComboBox: scroll (wheel) only changes value when focused.
 * Analog of MyQComboBox in widgets.py.
 */
public class ArtisanComboBox<T> extends ComboBox<T> {

    public ArtisanComboBox() {
        getStyleClass().add("artisan-combo-box");
        setFocusTraversable(true);
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if (!isFocused()) {
                e.consume();
            }
        });
    }

    /**
     * Returns the currently selected item, or null if none or editable text is used.
     */
    public T getSelectedValue() {
        return getValue();
    }

    /**
     * Sets the selected item by value.
     */
    public void setSelectedValue(T value) {
        setValue(value);
    }
}
