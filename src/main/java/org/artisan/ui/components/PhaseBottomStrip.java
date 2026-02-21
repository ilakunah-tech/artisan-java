package org.artisan.ui.components;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.artisan.model.PhaseResult;

/**
 * Horizontal phase strip shown at the bottom of the main layout.
 * Shows DRY / MAILLARD / DEVELOPMENT proportional bands.
 */
public final class PhaseBottomStrip extends HBox {

    private final SimpleDoubleProperty dryFraction      = new SimpleDoubleProperty(0.55);
    private final SimpleDoubleProperty maillardFraction = new SimpleDoubleProperty(0.25);
    private final SimpleDoubleProperty devFraction      = new SimpleDoubleProperty(0.20);

    private final StackPane dryBlock;
    private final StackPane maillardBlock;
    private final StackPane devBlock;

    public PhaseBottomStrip() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(0);
        setPrefHeight(34);
        setMinHeight(34);
        setMaxHeight(34);

        Label dryLabel      = new Label();
        Label maillardLabel = new Label();
        Label devLabel      = new Label();

        dryLabel.getStyleClass().add("phase-strip-label");
        maillardLabel.getStyleClass().add("phase-strip-label");
        devLabel.getStyleClass().add("phase-strip-label");

        dryLabel.setMaxWidth(Double.MAX_VALUE);
        maillardLabel.setMaxWidth(Double.MAX_VALUE);
        devLabel.setMaxWidth(Double.MAX_VALUE);
        dryLabel.setAlignment(Pos.CENTER);
        maillardLabel.setAlignment(Pos.CENTER);
        devLabel.setAlignment(Pos.CENTER);

        dryLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> String.format("DRY %.0f%%", dryFraction.get() * 100), dryFraction));
        maillardLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> String.format("MAILLARD %.0f%%", maillardFraction.get() * 100), maillardFraction));
        devLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> String.format("DT %.0f%%", devFraction.get() * 100), devFraction));

        dryBlock      = new StackPane(dryLabel);
        maillardBlock = new StackPane(maillardLabel);
        devBlock      = new StackPane(devLabel);

        dryBlock.getStyleClass().add("phase-strip-dry");
        maillardBlock.getStyleClass().add("phase-strip-maillard");
        devBlock.getStyleClass().add("phase-strip-dev");

        dryBlock.setAlignment(Pos.CENTER);
        maillardBlock.setAlignment(Pos.CENTER);
        devBlock.setAlignment(Pos.CENTER);

        dryBlock.prefWidthProperty().bind(widthProperty().multiply(dryFraction));
        maillardBlock.prefWidthProperty().bind(widthProperty().multiply(maillardFraction));
        devBlock.prefWidthProperty().bind(widthProperty().multiply(devFraction));

        dryBlock.setMinHeight(34);
        maillardBlock.setMinHeight(34);
        devBlock.setMinHeight(34);

        getChildren().addAll(dryBlock, maillardBlock, devBlock);
    }

    public void updatePhase(PhaseResult result) {
        Platform.runLater(() -> {
            if (result == null || result.isInvalid()) return;
            double total = result.getDryingTimeSec()
                         + result.getMaillardTimeSec()
                         + result.getDevelopmentTimeSec();
            if (total <= 0) return;
            dryFraction.set(result.getDryingTimeSec() / total);
            maillardFraction.set(result.getMaillardTimeSec() / total);
            devFraction.set(result.getDevelopmentTimeSec() / total);
        });
    }
}
