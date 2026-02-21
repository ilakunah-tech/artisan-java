package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.artisan.ui.vm.RoastViewModel;

/**
 * HBox (48px) for replay mode: slider to scrub reference roast, time label, Load/Exit buttons.
 * Visible only when vm.replayMode is true.
 */
public final class ReplayPanel extends HBox {

    private final Label timeLabel;
    private final Slider scrubber;
    private final Stage ownerStage;

    public ReplayPanel(RoastViewModel vm, Stage ownerStage) {
        this.ownerStage = ownerStage;
        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(8, 12, 8, 12));
        setMinHeight(48);
        setPrefHeight(48);
        setMaxHeight(48);
        getStyleClass().add("replay-panel");

        managedProperty().bind(vm.replayModeProperty());
        visibleProperty().bind(vm.replayModeProperty());

        getChildren().add(new Label("Replay:"));

        scrubber = new Slider(0, 1, 0);
        scrubber.setBlockIncrement(0.01);
        scrubber.setMajorTickUnit(0.25);
        scrubber.setShowTickMarks(false);
        scrubber.setPrefWidth(200);
        HBox.setHgrow(scrubber, javafx.scene.layout.Priority.SOMETIMES);

        timeLabel = new Label("0:00");
        timeLabel.setMinWidth(50);

        scrubber.valueProperty().addListener((o, ov, nv) -> {
            double v = nv != null ? nv.doubleValue() : 0;
            int totalSec = (int) (v * 900);
            timeLabel.setText(String.format("%d:%02d", totalSec / 60, totalSec % 60));
        });

        Button loadBtn = new Button("Load Reference...");
        loadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Reference Roast");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Artisan log (*.alog)", "*.alog"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
            var f = fc.showOpenDialog(ownerStage);
            if (f != null) {
            }
        });

        Button exitBtn = new Button("Exit Replay");
        exitBtn.setOnAction(e -> vm.setReplayMode(false));

        getChildren().addAll(scrubber, timeLabel, loadBtn, exitBtn);
    }
}
