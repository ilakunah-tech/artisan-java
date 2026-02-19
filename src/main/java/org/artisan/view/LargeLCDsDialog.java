package org.artisan.view;

import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.controller.AppController.SampleListener;
import org.artisan.model.Phases;
import org.artisan.model.PhaseResult;
import org.artisan.model.ProfileData;

/**
 * View » Large LCDs: non-modal Stage with 2×3 grid of LCDPanels (BT, ET, RoR BT, RoR ET, Time, Development Time).
 * Registers as SampleListener on open, unregisters on close. Window position/size in Preferences: lcds.*
 */
public final class LargeLCDsDialog {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "lcds.";
    private static final String DASH = "–––";

    private final Stage stage;
    private final AppController appController;
    private final LCDPanel btPanel;
    private final LCDPanel etPanel;
    private final LCDPanel rorBtPanel;
    private final LCDPanel rorEtPanel;
    private final LCDPanel timePanel;
    private final LCDPanel devTimePanel;
    private final SampleListener sampleListener = this::onSample;

    public LargeLCDsDialog(Window owner, AppController appController) {
        this.appController = appController;
        stage = new Stage();
        stage.setTitle("Large LCDs");
        stage.initOwner(owner);
        stage.initModality(javafx.stage.Modality.NONE);

        btPanel = new LCDPanel("BT", "°C");
        btPanel.setColor(LCDPanel.COLOR_BT);
        etPanel = new LCDPanel("ET", "°C");
        etPanel.setColor(LCDPanel.COLOR_ET);
        rorBtPanel = new LCDPanel("RoR BT", "°C/min");
        rorBtPanel.setColor(LCDPanel.COLOR_ROR);
        rorEtPanel = new LCDPanel("RoR ET", "°C/min");
        rorEtPanel.setColor(LCDPanel.COLOR_ROR);
        timePanel = new LCDPanel("Time", "");
        timePanel.setColor(LCDPanel.COLOR_DEFAULT);
        devTimePanel = new LCDPanel("Development Time", "");
        devTimePanel.setColor(LCDPanel.COLOR_DEFAULT);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(12));
        grid.add(btPanel, 0, 0);
        grid.add(etPanel, 1, 0);
        grid.add(rorBtPanel, 0, 1);
        grid.add(rorEtPanel, 1, 1);
        grid.add(timePanel, 0, 2);
        grid.add(devTimePanel, 1, 2);

        ToolBar toolbar = new ToolBar();
        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("Close");
        closeBtn.setOnAction(e -> stage.hide());
        toolbar.getItems().add(closeBtn);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(grid);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 280, 280);
        stage.setScene(scene);

        loadPreferences();
        stage.setOnCloseRequest(e -> {
            if (this.appController != null) appController.removeSampleListener(sampleListener);
            savePreferences();
        });
    }

    private void onSample(double bt, double et, double rorBT, double rorET, double timeSec) {
        Platform.runLater(() -> {
            btPanel.setValue(bt);
            etPanel.setValue(et);
            rorBtPanel.setValue(rorBT);
            rorEtPanel.setValue(rorET);
            timePanel.setValueText(formatMmSs(timeSec));
            double devSec = 0.0;
            ProfileData profile = appController != null ? appController.getCurrentProfileData() : null;
            if (profile != null) {
                PhaseResult phase = Phases.compute(profile);
                if (!phase.isInvalid()) devSec = phase.getDevelopmentTimeSec();
            }
            devTimePanel.setValueText(formatMmSs(devSec));
        });
    }

    private static String formatMmSs(double totalSeconds) {
        if (!Double.isFinite(totalSeconds) || totalSeconds < 0) return DASH;
        int total = (int) Math.round(totalSeconds);
        int m = total / 60;
        int s = total % 60;
        return String.format("%d:%02d", m, s);
    }

    public void show() {
        if (appController != null) appController.addSampleListener(sampleListener);
        stage.show();
        stage.toFront();
    }

    public Stage getStage() {
        return stage;
    }

    private void loadPreferences() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            double x = p.getDouble(PREFIX + "x", Double.NaN);
            double y = p.getDouble(PREFIX + "y", Double.NaN);
            double w = p.getDouble(PREFIX + "width", Double.NaN);
            double h = p.getDouble(PREFIX + "height", Double.NaN);
            if (Double.isFinite(x)) stage.setX(x);
            if (Double.isFinite(y)) stage.setY(y);
            if (Double.isFinite(w) && w > 100) stage.setWidth(w);
            if (Double.isFinite(h) && h > 100) stage.setHeight(h);
        } catch (Exception ignored) {}
    }

    private void savePreferences() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            p.putDouble(PREFIX + "x", stage.getX());
            p.putDouble(PREFIX + "y", stage.getY());
            p.putDouble(PREFIX + "width", stage.getWidth());
            p.putDouble(PREFIX + "height", stage.getHeight());
        } catch (Exception ignored) {}
    }
}
