package org.artisan.ui.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.artisan.controller.AppController;
import org.artisan.ui.vm.RoastViewModel;

/**
 * Right-side panel showing timer, temperature readouts, reference box,
 * modulation timeline, and control sliders.
 */
public final class RightReadoutPanel extends VBox {

    private final ReferenceBox refBox;
    private final ModulationTimeline modulationTimeline;
    private AppController appController;

    public RightReadoutPanel(RoastViewModel vm) {
        getStyleClass().add("right-readout-panel");
        setPrefWidth(220);
        setMinWidth(200);
        setMaxWidth(240);
        setSpacing(6);

        // 1. TIMER — split into three labels with blinking colon
        Label minutesLabel = new Label("00");
        minutesLabel.getStyleClass().add("timer-label");
        Label colonLabel = new Label(":");
        colonLabel.getStyleClass().add("timer-label");
        Label secondsLabel = new Label("00.00");
        secondsLabel.getStyleClass().add("timer-label");

        minutesLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            int mins = (int)(vm.getElapsedSec() / 60);
            return String.format("%02d", mins);
        }, vm.elapsedSecProperty()));

        secondsLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            double sec = vm.getElapsedSec();
            int secs = (int)(sec % 60);
            int hundredths = (int)((sec % 1) * 100);
            return String.format("%02d.%02d", secs, hundredths);
        }, vm.elapsedSecProperty()));

        Timeline colonBlink = new Timeline(
            new KeyFrame(Duration.ZERO,       e2 -> colonLabel.setOpacity(1.0)),
            new KeyFrame(Duration.millis(500), e2 -> colonLabel.setOpacity(0.15)),
            new KeyFrame(Duration.millis(1000), e2 -> colonLabel.setOpacity(1.0))
        );
        colonBlink.setCycleCount(Animation.INDEFINITE);

        vm.samplingActiveProperty().addListener((obs, wasActive, isActive) -> {
            if (isActive) {
                colonBlink.play();
            } else {
                colonBlink.stop();
                colonLabel.setOpacity(1.0);
            }
        });

        HBox timerBox = new HBox(0, minutesLabel, colonLabel, secondsLabel);
        timerBox.setAlignment(Pos.CENTER);

        // 2. READOUT ROWS
        VBox readoutsBox = new VBox(6);

        Label btLabel = new Label("BT:");
        btLabel.getStyleClass().add("readout-label-bt");
        Label btValue = new Label("000.0 \u00b0C");
        btValue.getStyleClass().add("readout-value-bt");
        btValue.textProperty().bind(Bindings.createStringBinding(() -> {
            double v = vm.getBt();
            return Double.isFinite(v) ? String.format("%.1f \u00b0C", v) : "000.0 \u00b0C";
        }, vm.btProperty()));
        HBox btRow = new HBox(4, btLabel, btValue);
        btRow.getStyleClass().add("readout-row");
        btRow.setAlignment(Pos.CENTER_LEFT);

        Label etLabel = new Label("ET:");
        etLabel.getStyleClass().add("readout-label-et");
        Label etValue = new Label("000.0 \u00b0C");
        etValue.getStyleClass().add("readout-value-et");
        etValue.textProperty().bind(Bindings.createStringBinding(() -> {
            double v = vm.getEt();
            return Double.isFinite(v) ? String.format("%.1f \u00b0C", v) : "000.0 \u00b0C";
        }, vm.etProperty()));
        HBox etRow = new HBox(4, etLabel, etValue);
        etRow.getStyleClass().add("readout-row");
        etRow.setAlignment(Pos.CENTER_LEFT);

        Label dbtLabel = new Label("\u0394BT:");
        dbtLabel.getStyleClass().add("readout-label-dbt");
        Label dbtValue = new Label("0.0 \u00b0C/MIN");
        dbtValue.getStyleClass().add("readout-value-dbt");
        dbtValue.textProperty().bind(Bindings.createStringBinding(() -> {
            double v = vm.getRorBT();
            return Double.isFinite(v) ? String.format("%.1f \u00b0C/MIN", v) : "0.0 \u00b0C/MIN";
        }, vm.rorBTProperty()));
        HBox dbtRow = new HBox(4, dbtLabel, dbtValue);
        dbtRow.getStyleClass().add("readout-row");
        dbtRow.setAlignment(Pos.CENTER_LEFT);

        Label detLabel = new Label("\u0394ET:");
        detLabel.getStyleClass().add("readout-label-det");
        Label detValue = new Label("0.0 \u00b0C/MIN");
        detValue.getStyleClass().add("readout-value-det");
        detValue.textProperty().bind(Bindings.createStringBinding(() -> {
            double v = vm.getRorET();
            return Double.isFinite(v) ? String.format("%.1f \u00b0C/MIN", v) : "0.0 \u00b0C/MIN";
        }, vm.rorETProperty()));
        HBox detRow = new HBox(4, detLabel, detValue);
        detRow.getStyleClass().add("readout-row");
        detRow.setAlignment(Pos.CENTER_LEFT);

        readoutsBox.getChildren().addAll(btRow, etRow, dbtRow, detRow);

        // 3. REFERENCE BOX
        refBox = new ReferenceBox();
        refBox.setMaxWidth(Double.MAX_VALUE);

        // 4. MODULATION TIMELINE
        modulationTimeline = new ModulationTimeline();
        modulationTimeline.setMaxWidth(Double.MAX_VALUE);

        // 5. CONTROLS SECTION
        Label controlsHeader = new Label("Controls");
        controlsHeader.setStyle("-fx-font-size:12px; -fx-font-weight:bold;");

        ControlSlider burnControl = new ControlSlider("Burn", Color.web("#E53935"));
        ControlSlider airControl = new ControlSlider("Air", Color.web("#87CEEB"));
        burnControl.bindBidirectional(vm.gasPercentProperty());
        airControl.bindBidirectional(vm.airPercentProperty());
        burnControl.getSlider().valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging && appController != null) {
                appController.setControlOutput("Gas", (int) burnControl.getValue());
            }
        });
        airControl.getSlider().valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging && appController != null) {
                appController.setControlOutput("Air", (int) airControl.getValue());
            }
        });

        HBox slidersRow = new HBox(6, burnControl, airControl);
        slidersRow.setAlignment(Pos.TOP_CENTER);
        slidersRow.setPrefHeight(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(burnControl, Priority.ALWAYS);
        HBox.setHgrow(airControl, Priority.ALWAYS);
        VBox.setVgrow(slidersRow, Priority.ALWAYS);

        VBox slidersBox = new VBox(slidersRow);
        slidersBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(slidersBox, Priority.ALWAYS);

        VBox controlsSection = new VBox(4, controlsHeader, slidersBox);

        // 6. SPACER
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
            timerBox,
            readoutsBox,
            refBox,
            modulationTimeline,
            controlsSection,
            spacer
        );
    }

    public ReferenceBox getRefBox() {
        return refBox;
    }

    public ModulationTimeline getModulationTimeline() {
        return modulationTimeline;
    }

    public void setAppController(AppController ac) {
        this.appController = ac;
    }
}
