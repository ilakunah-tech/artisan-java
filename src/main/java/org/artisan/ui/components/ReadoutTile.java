package org.artisan.ui.components;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.artisan.ui.state.UIPreferences;

/**
 * Single readout tile: label + large numeric value. Size mode S/M/L.
 */
public final class ReadoutTile extends VBox {

    private final Label label;
    private final Label valueLabel;
    private final String format;

    public ReadoutTile(String labelText, javafx.beans.value.ObservableValue<Number> valueProperty,
                       String format) {
        this.format = format != null ? format : "%.1f";
        getStyleClass().add("ri5-readout-tile");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(8, 12, 8, 12));
        setSpacing(2);

        label = new Label(labelText != null ? labelText : "");
        label.getStyleClass().add("label");

        valueLabel = new Label("—");
        valueLabel.getStyleClass().add("value");
        if (valueProperty != null) {
            ChangeListener<Number> listener = (a, b, n) -> {
                if (n == null || !Double.isFinite(n.doubleValue())) valueLabel.setText("—");
                else valueLabel.setText(String.format(this.format, n.doubleValue()));
            };
            valueProperty.addListener(listener);
            if (valueProperty.getValue() != null && Double.isFinite(valueProperty.getValue().doubleValue()))
                valueLabel.setText(String.format(this.format, valueProperty.getValue().doubleValue()));
        }

        getChildren().addAll(label, valueLabel);
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
