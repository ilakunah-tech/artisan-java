package org.artisan.ui.components;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Transparent overlay for the main chart showing BT and ET live values at the right edge.
 * Updates on sample. Mouse-transparent so chart interactions pass through.
 */
public final class LiveValueOverlay extends VBox {

    private final Label btLabel;
    private final Label etLabel;

    public LiveValueOverlay(DoubleProperty btProperty, DoubleProperty etProperty) {
        setMouseTransparent(true);
        setAlignment(Pos.TOP_RIGHT);
        setPadding(new Insets(8));
        setSpacing(4);

        btLabel = new Label();
        btLabel.getStyleClass().add("live-value-bt");
        btLabel.setStyle("-fx-text-fill: #FF6B35; -fx-font-size: 12px; -fx-font-weight: bold;");

        etLabel = new Label();
        etLabel.getStyleClass().add("live-value-et");
        etLabel.setStyle("-fx-text-fill: #00D4AA; -fx-font-size: 12px; -fx-font-weight: bold;");

        getChildren().addAll(btLabel, etLabel);

        btProperty.addListener((o, ov, nv) -> updateBt(nv == null ? Double.NaN : nv.doubleValue()));
        etProperty.addListener((o, ov, nv) -> updateEt(nv == null ? Double.NaN : nv.doubleValue()));
        updateBt(btProperty.get());
        updateEt(etProperty.get());
    }

    private void updateBt(double v) {
        btLabel.setText(Double.isFinite(v) ? String.format("BT: %.1f °C", v) : "BT: —");
    }

    private void updateEt(double v) {
        etLabel.setText(Double.isFinite(v) ? String.format("ET: %.1f °C", v) : "ET: —");
    }
}
