package org.artisan.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.artisan.model.ReferenceProfile;

/**
 * Compact box showing reference profile key stats.
 */
public final class ReferenceBox extends VBox {

    public ReferenceBox() {
        getStyleClass().add("reference-box");
        setMaxHeight(80);
        setMaxWidth(Double.MAX_VALUE);
        setReferenceProfile(null);
    }

    public void setReferenceProfile(ReferenceProfile rp) {
        getChildren().clear();
        if (rp == null) {
            Label empty = new Label("No reference selected");
            empty.getStyleClass().add("reference-box-empty");
            getChildren().add(empty);
            return;
        }

        Label name = new Label(rp.getName());
        name.getStyleClass().add("reference-box-title");

        Label line1 = new Label(String.format("CHG %.0f\u00b0  TP %.0f\u00b0  DROP %.0f\u00b0",
            rp.getChargeTempC(), rp.getTpTempC(), rp.getDropTempC()));
        line1.getStyleClass().add("reference-box-label");

        Label line2 = new Label(String.format("DE %s  FC %s  Total %s",
            formatTime(rp.getDryEndTimeSec()),
            formatTime(rp.getFcStartTimeSec()),
            formatTime(rp.getDropTimeSec())));
        line2.getStyleClass().add("reference-box-label");

        Label line3 = new Label(String.format("DRY %.0f%%  MAL %.0f%%  DT %.0f%%",
            rp.getDryPct(), rp.getMaillardPct(), rp.getDtPct()));
        line3.getStyleClass().add("reference-box-label");

        getChildren().addAll(name, line1, line2, line3);
    }

    private String formatTime(double sec) {
        return String.format("%d:%02d", (int)(sec / 60), (int)(sec % 60));
    }
}
