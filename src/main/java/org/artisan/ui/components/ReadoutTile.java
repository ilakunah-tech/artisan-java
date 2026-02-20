package org.artisan.ui.components;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.artisan.ui.state.UIPreferences;

import java.util.function.DoubleFunction;

/**
 * Single readout tile: label (small) + value (big/bold) + unit (muted).
 * Cropster RI5: 1 decimal, consistent formatting, value prominent.
 */
public final class ReadoutTile extends VBox {

    private final Label label;
    private final Label valueLabel;
    private final Label unitLabel;
    private final String format;
    private final String unit;
    private final DoubleFunction<String> customFormatter;

    public ReadoutTile(String labelText, javafx.beans.value.ObservableValue<Number> valueProperty,
                       String format) {
        this(labelText, valueProperty, format, null, null, null);
    }

    public ReadoutTile(String labelText, javafx.beans.value.ObservableValue<Number> valueProperty,
                       String format, String accentClass) {
        this(labelText, valueProperty, format, accentClass, null, null);
    }

    public ReadoutTile(String labelText, javafx.beans.value.ObservableValue<Number> valueProperty,
                       String format, String accentClass, String unit) {
        this(labelText, valueProperty, format, accentClass, unit, null);
    }

    public ReadoutTile(String labelText, javafx.beans.value.ObservableValue<Number> valueProperty,
                       String format, String accentClass, String unit, DoubleFunction<String> customFormatter) {
        this.format = format != null ? format : "%.1f";
        this.unit = unit != null ? unit : "";
        this.customFormatter = customFormatter;
        getStyleClass().add("ri5-readout-tile");
        if (accentClass != null && !accentClass.isEmpty()) {
            getStyleClass().add("accent-" + accentClass);
        }
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(10, 12, 10, 14));
        setSpacing(2);

        label = new Label(labelText != null ? labelText : "");
        label.getStyleClass().add("label");

        HBox valueRow = new HBox(4);
        valueRow.setAlignment(Pos.CENTER_LEFT);
        valueLabel = new Label("—");
        valueLabel.getStyleClass().add("value");
        unitLabel = new Label(this.unit);
        unitLabel.getStyleClass().add("unit");
        valueRow.getChildren().addAll(valueLabel, unitLabel);
        HBox.setHgrow(valueLabel, Priority.NEVER);

        if (valueProperty != null) {
            ChangeListener<Number> listener = (a, b, n) -> {
                if (n == null || !Double.isFinite(n.doubleValue())) {
                    valueLabel.setText("—");
                } else {
                    double v = n.doubleValue();
                    valueLabel.setText(customFormatter != null ? customFormatter.apply(v) : String.format(this.format, v));
                }
            };
            valueProperty.addListener(listener);
            if (valueProperty.getValue() != null && Double.isFinite(valueProperty.getValue().doubleValue())) {
                double v = valueProperty.getValue().doubleValue();
                valueLabel.setText(customFormatter != null ? customFormatter.apply(v) : String.format(this.format, v));
            }
        }

        getChildren().addAll(label, valueRow);
    }

    public void setReadoutSize(UIPreferences.ReadoutSize size) {
        getStyleClass().removeAll("ri5-readout-size-s", "ri5-readout-size-m", "ri5-readout-size-l");
        if (size != null) {
            switch (size) {
                case S: getStyleClass().add("ri5-readout-size-s"); break;
                case M: getStyleClass().add("ri5-readout-size-m"); break;
                case L: getStyleClass().add("ri5-readout-size-l"); break;
                default: getStyleClass().add("ri5-readout-size-m");
            }
        } else {
            getStyleClass().add("ri5-readout-size-m");
        }
    }
}
