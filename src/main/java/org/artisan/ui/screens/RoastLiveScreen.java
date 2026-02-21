package org.artisan.ui.screens;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.artisan.controller.AppController;
import org.artisan.controller.RoastStateMachine;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.EventEntry;
import org.artisan.model.EventType;
import org.artisan.model.ReferenceProfile;
import org.artisan.ui.components.EventLogPanel;
import org.artisan.ui.components.ModulationTimeline;
import org.artisan.ui.components.PhaseStripOverlay;
import org.artisan.ui.components.ShortcutHelpDialog;
import org.artisan.ui.vm.BbpController;
import org.artisan.ui.vm.RoastViewModel;
import org.artisan.ui.state.ChartAppearance;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;
import org.artisan.view.RoastChartController;
import org.artisan.model.PhaseResult;

import java.util.List;

/**
 * Roast (Live) screen: chart fills the center. All dock panels and top bar removed.
 * Layout is now managed by AppShell (BorderPane with LeftIconRail, RightReadoutPanel, PhaseBottomStrip).
 */
public final class RoastLiveScreen {

    private final AnchorPane root;
    private final RoastViewModel viewModel;
    private final AppController appController;
    private final RoastChartController chartController;
    private final UIPreferences uiPreferences;
    private final PreferencesStore preferencesStore;
    private final Stage primaryStage;
    private final EventLogPanel eventLogPanel;
    private final BbpController bbpController;
    private final Button startButton;
    private final PhaseStripOverlay phaseStrip;
    private javafx.animation.AnimationTimer statusTimer;
    private Popup popup;
    private ModulationTimeline modulationRef;
    private Runnable onStart;
    private RoastStateMachine roastStateMachine;

    public RoastLiveScreen(Stage primaryStage, AppController appController,
                           RoastChartController chartController, DisplaySettings displaySettings,
                           UIPreferences uiPreferences, PreferencesStore preferencesStore) {
        this.primaryStage = primaryStage;
        this.appController = appController;
        this.chartController = chartController;
        this.uiPreferences = uiPreferences;
        this.preferencesStore = preferencesStore;
        this.viewModel = new RoastViewModel();

        Node chartView = chartController != null ? chartController.getView() : new Pane();
        if (chartView instanceof Region) {
            ((Region) chartView).setMinSize(300, 0);
        }

        Label roastNameLabel = new Label("#Name Roast");
        roastNameLabel.getStyleClass().add("roast-name-label");

        startButton = new Button("Start");
        startButton.getStyleClass().add("btn-start");
        startButton.setOnAction(e -> { if (onStart != null) onStart.run(); });
        startButton.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> !viewModel.isSamplingActive() || viewModel.isPreRoastActive(),
            viewModel.samplingActiveProperty(), viewModel.preRoastActiveProperty()));
        startButton.managedProperty().bind(startButton.visibleProperty());
        startButton.textProperty().bind(Bindings.createStringBinding(
            () -> viewModel.isPreRoastActive() ? "Charge" : "Start",
            viewModel.preRoastActiveProperty()));

        HBox startWrapper = new HBox(startButton);
        startWrapper.setAlignment(Pos.CENTER);

        chartView.getStyleClass().add("chart-area");

        // Build root first — PhaseStripOverlay needs root as its parentPane
        root = new AnchorPane(chartView, startWrapper, roastNameLabel);
        AnchorPane.setTopAnchor(chartView, 0.0);
        AnchorPane.setBottomAnchor(chartView, 0.0);
        AnchorPane.setLeftAnchor(chartView, 0.0);
        AnchorPane.setRightAnchor(chartView, 0.0);

        AnchorPane.setBottomAnchor(startWrapper, 60.0);
        AnchorPane.setLeftAnchor(startWrapper, 0.0);
        AnchorPane.setRightAnchor(startWrapper, 0.0);

        AnchorPane.setTopAnchor(roastNameLabel, 10.0);
        AnchorPane.setRightAnchor(roastNameLabel, 14.0);

        // Canvas-based phase strip: positions itself over the plot area via
        // coordinate transforms. Adds itself to root in the constructor.
        phaseStrip = new PhaseStripOverlay(
            chartController != null ? chartController.getChart() : null,
            root);

        startPulseAnimation(startButton);

        bbpController = new BbpController(viewModel);
        eventLogPanel = new EventLogPanel();

        if (appController != null) {
            appController.addPhaseListener(result -> javafx.application.Platform.runLater(() -> {
                double elapsed = viewModel.getElapsedSec();
                String phaseName = phaseNameFromResult(result, elapsed);
                viewModel.setPhaseName(phaseName);
                viewModel.setDevTimeSec(result != null && !result.isInvalid()
                    ? result.getDevelopmentTimeSec() : Double.NaN);
            }));

            appController.addSampleListener((bt, et, rorBT, rorET, timeSec) ->
                javafx.application.Platform.runLater(() -> {
                    viewModel.setBt(bt);
                    viewModel.setEt(et);
                    viewModel.setRorBT(rorBT);
                    viewModel.setRorET(rorET);
                    double elapsedTimeSec = computeElapsedSec(timeSec);
                    viewModel.setElapsedSec(elapsedTimeSec);
                    viewModel.setSamplingActive(true);
                }));

            viewModel.setConnectionStatus(
                appController.getCommController() != null
                    && appController.getCommController().getActiveChannel() != null
                    ? appController.getCommController().getActiveChannel().getDescription()
                    : "Disconnected");

            if (chartController != null) {
                eventLogPanel.setOnEventSelected(entry -> {
                    var timex = appController.getSession().getCanvasData().getTimex();
                    int idx = entry.getTimeIndex();
                    if (timex != null && idx >= 0 && idx < timex.size()) {
                        double timeSec = timex.get(idx);
                        chartController.centerChartOnTime(timeSec);
                        chartController.setHighlightTimeSec(timeSec);
                    }
                });
                chartController.setOnEventBarClicked(entry -> {
                    eventLogPanel.scrollToEvent(entry);
                    var timex = appController.getSession().getCanvasData().getTimex();
                    int idx = entry.getTimeIndex();
                    if (timex != null && idx >= 0 && idx < timex.size()) {
                        double timeSec = timex.get(idx);
                        chartController.centerChartOnTime(timeSec);
                        chartController.setHighlightTimeSec(timeSec);
                    }
                });
                eventLogPanel.setOnQuickAdd(label -> {
                    var cd = appController.getSession().getCanvasData();
                    var timex = cd.getTimex();
                    if (!timex.isEmpty()) {
                        int idx = timex.size() - 1;
                        double bt = idx < cd.getTemp2().size() ? cd.getTemp2().get(idx) : 0;
                        appController.addCustomEvent(EventType.CUSTOM, 0, idx, bt, label);
                        chartController.updateChart();
                    }
                });
            }
        }

        statusTimer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                if (appController == null) return;
                var events = appController.getSession().getEvents().getAll();
                var cd = appController.getSession().getCanvasData();
                viewModel.syncEvents(events, cd != null ? cd.getTimex() : null);
                eventLogPanel.setTimex(appController.getSession().getCanvasData().getTimex());
                eventLogPanel.setEvents(events);
                if (cd != null && cd.getTimex() != null && !cd.getTimex().isEmpty()) {
                    double latestTimeSec = cd.getTimex().get(cd.getTimex().size() - 1);
                    viewModel.setElapsedSec(computeElapsedSec(latestTimeSec));
                }
                if (chartController != null) {
                    chartController.setLiveRecording(appController.getSession().isActive());
                }
                boolean hasDrop = events.stream().anyMatch(ev -> ev.getType() == EventType.DROP);
                boolean hasChargeOnly = events.size() == 1
                    && events.get(0).getType() == EventType.CHARGE;
                if (hasDrop && !viewModel.isBbtActive()) bbpController.startBbp();
                if (hasChargeOnly && viewModel.isBbtActive()) bbpController.endBbp();
                double xMin = chartController != null ? chartController.getXAxisMin() : 0.0;
                double xMax = chartController != null ? chartController.getXAxisMax() : 900.0;
                phaseStrip.update(viewModel.getElapsedSec(), xMin, xMax);
                if (modulationRef != null) modulationRef.update(viewModel.getElapsedSec());
            }
        };

        if (chartController != null) {
            chartController.setOnChartBodyClick(info -> showAddEventPopover(info.timeIndex, info.bt, info.et));
        }

        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleShortcut);
    }

    private static String phaseNameFromResult(PhaseResult result, double elapsedSec) {
        if (result == null || result.isInvalid()) return "\u2014";
        double drying = result.getDryingTimeSec();
        double maillard = result.getMaillardTimeSec();
        double development = result.getDevelopmentTimeSec();
        if (elapsedSec <= drying) return "Drying";
        if (elapsedSec <= drying + maillard) return "Maillard";
        if (elapsedSec <= drying + maillard + development) return "Development";
        return "\u2014";
    }

    private void showAddEventPopover(int timeIndex, double bt, double et) {
        if (appController == null) return;
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Charge", "Dry End", "FC Start", "FC End", "Drop", "Custom");
        typeCombo.getSelectionModel().select(0);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        TextField noteField = new TextField();
        noteField.setPromptText("Note (optional)");
        Button okBtn = new Button("Add");
        okBtn.getStyleClass().add("ri5-primary-button");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> popup.hide());

        final int idx = timeIndex;
        final double btVal = bt;
        okBtn.setOnAction(e -> {
            String typeStr = typeCombo.getSelectionModel().getSelectedItem();
            String note = noteField.getText();
            EventType type = EventType.CUSTOM;
            if ("Charge".equals(typeStr))   type = EventType.CHARGE;
            else if ("Dry End".equals(typeStr))   type = EventType.DRY_END;
            else if ("FC Start".equals(typeStr))  type = EventType.FC_START;
            else if ("FC End".equals(typeStr))    type = EventType.FC_END;
            else if ("Drop".equals(typeStr))      type = EventType.DROP;
            if (type == EventType.CUSTOM) {
                String label = (note != null && !note.isEmpty()) ? note : "Custom";
                appController.addCustomEvent(EventType.CUSTOM, 0, idx, btVal, label);
            } else {
                appController.markEventAt(type, idx, null);
            }
            if (chartController != null) chartController.updateChart();
            popup.hide();
        });

        VBox content = new VBox(10);
        content.getStyleClass().add("ri5-popover-content");
        content.setPadding(new Insets(12));
        content.getChildren().addAll(
            new Label("Add event"),
            new Label("Type:"), typeCombo,
            new Label("Note:"), noteField,
            new HBox(8, okBtn, cancelBtn)
        );

        StackPane popupRoot = new StackPane(content);
        popupRoot.getStyleClass().add("ri5-popover");
        popupRoot.setPadding(new Insets(0));
        if (popup == null) {
            popup = new Popup();
            popup.setAutoHide(true);
        }
        popup.getContent().clear();
        popup.getContent().add(popupRoot);
        if (primaryStage != null && primaryStage.getScene() != null) {
            popupRoot.getStylesheets().setAll(primaryStage.getScene().getStylesheets());
        }
        if (primaryStage != null && primaryStage.isShowing()) {
            var sb = primaryStage.getScene().getRoot().localToScreen(
                primaryStage.getScene().getRoot().getLayoutBounds().getWidth() / 2 - 120,
                primaryStage.getScene().getRoot().getLayoutBounds().getHeight() / 2 - 80
            );
            popup.show(primaryStage, sb.getX(), sb.getY());
        }
    }

    private void handleShortcut(KeyEvent e) {
        if (e.getTarget() instanceof TextInputControl) return;
        if (e.getCode() == KeyCode.SPACE) {
            if (appController != null && chartController != null) {
                var cd = appController.getSession().getCanvasData();
                var timex = cd.getTimex();
                if (!timex.isEmpty()) {
                    int idx = timex.size() - 1;
                    double bt = idx < cd.getTemp2().size() ? cd.getTemp2().get(idx) : 0;
                    double et = idx < cd.getTemp1().size() ? cd.getTemp1().get(idx) : 0;
                    showAddEventPopover(idx, bt, et);
                }
            }
            e.consume();
        } else if (e.getCode() == KeyCode.DIGIT1) { markEvent(EventType.CHARGE); e.consume(); }
        else if (e.getCode() == KeyCode.DIGIT2) { markEvent(EventType.DRY_END); e.consume(); }
        else if (e.getCode() == KeyCode.DIGIT3) { markEvent(EventType.FC_START); e.consume(); }
        else if (e.getCode() == KeyCode.DIGIT4) { markEvent(EventType.FC_END); e.consume(); }
        else if (e.getCode() == KeyCode.DIGIT5) { markEvent(EventType.DROP); e.consume(); }
        else if (e.getCode() == KeyCode.SLASH && e.isShiftDown()) { ShortcutHelpDialog.show(primaryStage); e.consume(); }
        else if (e.getCode() == KeyCode.H) { if (chartController != null) chartController.resetZoom(); e.consume(); }
        else if (e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.ADD) { if (chartController != null) chartController.zoomIn(); e.consume(); }
        else if (e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT) { if (chartController != null) chartController.zoomOut(); e.consume(); }
        else if (e.getCode() == KeyCode.LEFT) { if (chartController != null) chartController.panLeft(); e.consume(); }
        else if (e.getCode() == KeyCode.RIGHT) { if (chartController != null) chartController.panRight(); e.consume(); }
        else if (e.getCode() == KeyCode.B && viewModel.isBbtActive() && bbpController != null) { bbpController.togglePause(); e.consume(); }
        else if (e.getCode() == KeyCode.G && e.isControlDown()) {
            viewModel.setGasValue(e.isShiftDown() ? Math.max(0, viewModel.getGasValue() - 1) : Math.min(100, viewModel.getGasValue() + 1));
            if (appController != null) appController.setControlOutput("Gas", viewModel.getGasValue());
            e.consume();
        } else if (e.getCode() == KeyCode.A && e.isControlDown()) {
            viewModel.setAirValue(e.isShiftDown() ? Math.max(0, viewModel.getAirValue() - 1) : Math.min(100, viewModel.getAirValue() + 1));
            if (appController != null) appController.setControlOutput("Air", viewModel.getAirValue());
            e.consume();
        } else if (e.getCode() == KeyCode.D && e.isControlDown()) {
            viewModel.setDrumValue(e.isShiftDown() ? Math.max(0, viewModel.getDrumValue() - 1) : Math.min(100, viewModel.getDrumValue() + 1));
            if (appController != null) appController.setControlOutput("Drum", viewModel.getDrumValue());
            e.consume();
        }
    }

    private void markEvent(EventType type) {
        if (appController != null) appController.markEvent(type);
    }

    private void startPulseAnimation(Button btn) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(750), btn);
        scaleUp.setToX(1.06);
        scaleUp.setToY(1.06);
        scaleUp.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(750), btn);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition pulse = new SequentialTransition(scaleUp, scaleDown);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        btn.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (!isVisible) {
                pulse.stop();
                btn.setScaleX(1.0);
                btn.setScaleY(1.0);
            } else {
                pulse.play();
            }
        });
    }

    public void setOnStart(Runnable r) {
        this.onStart = r;
        startButton.setOnAction(e -> { if (r != null) r.run(); });
    }

    public void setModulationTimelineRef(ModulationTimeline ref) {
        this.modulationRef = ref;
    }

    public void applyChartAppearance(ChartAppearance appearance) {
        if (appearance == null || chartController == null) return;
        chartController.setChartAppearance(appearance);
    }

    public void setRoastStateMachine(RoastStateMachine machine) {
        this.roastStateMachine = machine;
    }

    public void setPreRoastMode(boolean preRoast) {
        viewModel.setPreRoastActive(preRoast);
    }

    private double computeElapsedSec(double timeSec) {
        if (roastStateMachine == null || appController == null) return timeSec;
        var cd = appController.getSession().getCanvasData();
        if (cd == null) return timeSec;
        List<Double> timex = cd.getTimex();
        if (timex == null || timex.isEmpty()) return timeSec;
        int chargeIdx = cd.getChargeIndex();
        if (roastStateMachine.getState() == RoastStateMachine.State.PRE_ROAST && chargeIdx < 0) {
            double first = roastStateMachine.getPreRoastFirstTimeSec();
            if (Double.isFinite(first)) {
                return timeSec - first;
            }
            return timex.get(timex.size() - 1) - timex.get(0);
        }
        if (chargeIdx >= 0 && chargeIdx < timex.size()) {
            return timex.get(timex.size() - 1) - timex.get(chargeIdx);
        }
        return timeSec;
    }

    public void setPhaseStripProfile(ReferenceProfile rp) {
        phaseStrip.setReferenceProfile(rp);
    }

    public void tickModulationTimeline(double elapsed) {
        if (modulationRef != null) modulationRef.update(elapsed);
    }

    public void onScreenShown() {
        if (statusTimer != null) statusTimer.start();
        if (chartController != null) chartController.startUpdateTimer();
        if (appController != null) {
            String conn = appController.getCommController() != null
                && appController.getCommController().getActiveChannel() != null
                ? appController.getCommController().getActiveChannel().getDescription()
                : "Disconnected";
            viewModel.setConnectionStatus(conn);
        }
    }

    public Pane getRoot() { return root; }

    public RoastViewModel getViewModel() { return viewModel; }

    // --- Compatibility stubs (called from MainWindow.java) ---

    public void setOnHamburger(Runnable r) { /* no top bar */ }
    public void setOnTopBarSettings(Runnable r) { /* no top bar */ }
    public void setOnTopBarResetLayout(Runnable r) { /* no top bar */ }
    public void setOnTopBarKeyboardShortcuts(Runnable r) { /* no top bar */ }
    public void setOnTopBarAbout(Runnable r) { /* no top bar */ }

    public void expandDetailsPanel() { /* no dock panels */ }

    public void refreshCurveLegendColors(org.artisan.controller.DisplaySettings ds) { /* no dock panels */ }

    public void saveLayoutState() { /* no dock layout to save */ }

    public void restoreDetachedPanels() { /* no detached panels */ }

    public void closeDetachedPanels() { /* no detached panels */ }

    public void applyLayoutFromPreferences() { /* no dock layout */ }
}
