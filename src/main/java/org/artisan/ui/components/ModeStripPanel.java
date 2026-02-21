package org.artisan.ui.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Mode strip: Smart predictions toggle, Reference dropdown.
 * Cropster RI5-like compact strip for Live Roast right rail.
 */
public final class ModeStripPanel extends VBox {

    private static final String[] REF_OPTIONS = { "(None)", "Last roast", "Reference 1", "Reference 2" };

    private final BooleanProperty smartPredictions = new SimpleBooleanProperty(true);
    private final ComboBox<String> refCombo;

    public ModeStripPanel() {
        getStyleClass().add("ri5-mode-strip");
        setSpacing(8);
        setPadding(new Insets(8));

        CheckBox smartPredCheck = new CheckBox("Smart predictions");
        smartPredCheck.setSelected(true);
        smartPredCheck.selectedProperty().bindBidirectional(smartPredictions);
        smartPredCheck.setTooltip(new javafx.scene.control.Tooltip("Use smart predictions (UI state only)"));

        Label refLabel = new Label("Reference:");
        refLabel.getStyleClass().add("ri5-mode-strip-label");
        refCombo = new ComboBox<>();
        refCombo.getItems().addAll(REF_OPTIONS);
        refCombo.getSelectionModel().select(0);
        refCombo.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(smartPredCheck, refLabel, refCombo);
    }

    public BooleanProperty smartPredictionsProperty() {
        return smartPredictions;
    }

    public boolean isSmartPredictions() {
        return smartPredictions.get();
    }

    public void setSmartPredictions(boolean value) {
        smartPredictions.set(value);
    }

    public ComboBox<String> getRefCombo() {
        return refCombo;
    }

    public String getSelectedReference() {
        return refCombo.getSelectionModel().getSelectedItem();
    }
}
