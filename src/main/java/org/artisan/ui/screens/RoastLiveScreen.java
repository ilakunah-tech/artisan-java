package org.artisan.ui.screens;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.artisan.controller.AppController;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.EventEntry;
import org.artisan.model.EventType;
import org.artisan.ui.components.*;
import org.artisan.ui.vm.BbpController;
import org.artisan.ui.state.LayoutState;
import org.artisan.ui.vm.RoastViewModel;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;
import org.artisan.view.RoastChartController;
import org.artisan.model.PhaseResult;

import java.util.List;

/**
 * Roast (Live) screen: dominant chart, right dock (collapsible/detachable panels), bottom status strip.
 */
public final class RoastLiveScreen {

    private final BorderPane root;
    private final VBox dockContainer;
    private final VBox bottomBars;
    private final BottomStatusBar statusBar;
    private final RoastViewModel viewModel;
    private final AppController appController;
    private final RoastChartController chartController;
    private final DisplaySettings displaySettings;
    private final UIPreferences uiPreferences;
    private final PreferencesStore preferencesStore;
    private final DetachablePanelManager panelManager;
    private final Stage primaryStage;
    private RoastTopBar topBar;
    private DockPanel controlsDockPanel;
    private DockPanel legendDockPanel;
    private DockPanel detailsDockPanel;
    private RoastSummaryPanel roastSummaryPanel;
    private EventLogPanel eventLogPanel;
    private ReadoutTile btReadoutTile;
    private ReadoutTile etReadoutTile;
    private ReadoutTile rorReadoutTile;
    private javafx.animation.AnimationTimer statusTimer;
    private boolean controlsVisible = true;
    private Popup popup;
    private BbpController bbpController;

    public RoastLiveScreen(Stage primaryStage, AppController appController,
                           RoastChartController chartController, DisplaySettings displaySettings,
                           UIPreferences uiPreferences, PreferencesStore preferencesStore) {
        this.primaryStage = primaryStage;
        this.appController = appController;
        this.chartController = chartController;
        this.displaySettings = displaySettings;
        this.uiPreferences = uiPreferences;
        this.preferencesStore = preferencesStore;
        this.viewModel = new RoastViewModel();

        root = new BorderPane();
        root.setMinSize(0, 0);
        root.getStyleClass().add("ri5-live-workspace");
        dockContainer = new VBox(8);
        dockContainer.getStyleClass().add("ri5-dock-container");
        dockContainer.setMinWidth(LayoutState.MIN_DOCK_WIDTH);
        dockContainer.setPrefWidth(uiPreferences != null ? uiPreferences.getLayoutState().getDockWidth() : LayoutState.DEFAULT_DOCK_WIDTH);
        dockContainer.setMaxWidth(LayoutState.MAX_DOCK_WIDTH);
        dockContainer.setMaxHeight(Double.MAX_VALUE);
        statusBar = new BottomStatusBar();
        bottomBars = new VBox(6);
        bottomBars.getStyleClass().add("ri5-bottom-bars");
        panelManager = new DetachablePanelManager();
        panelManager.setPrimaryWindow(primaryStage);
        panelManager.setLayoutState(uiPreferences != null ? uiPreferences.getLayoutState() : new LayoutState());

        Node chartView = chartController != null ? chartController.getView() : new Pane();
        if (chartView instanceof Region) {
            ((Region) chartView).setMinSize(300, 0);
        }

        LiveValueOverlay liveValueOverlay = new LiveValueOverlay(viewModel.btProperty(), viewModel.etProperty());
        StackPane chartStack = new StackPane(chartView, liveValueOverlay);
        StackPane.setAlignment(liveValueOverlay, javafx.geometry.Pos.TOP_RIGHT);
        chartStack.setMinSize(0, 0);

        CursorValueBar cursorValueBar = new CursorValueBar();
        cursorValueBar.setVisible(false);

        if (appController != null) {
            appController.addPhaseListener(result -> javafx.application.Platform.runLater(() -> {
                double elapsed = viewModel.getElapsedSec();
                String phaseName = phaseNameFromResult(result, elapsed);
                viewModel.setPhaseName(phaseName);
                viewModel.setDevTimeSec(result != null && !result.isInvalid() ? result.getDevelopmentTimeSec() : Double.NaN);
                statusBar.setPhase(phaseName);
            }));
        }

        if (chartController != null) {
            chartController.setOnCursorMoved((timeSec, bt) -> {
                if (!Double.isFinite(timeSec)) {
                    cursorValueBar.setVisible(false);
                    cursorValueBar.clear();
                } else {
                    cursorValueBar.setVisible(true);
                    cursorValueBar.update(timeSec, bt);
                }
            });
        }

        VBox centerVBox = new VBox(0);
        centerVBox.getStyleClass().add("ri5-chart-container");
        centerVBox.setMinSize(0, 0);
        centerVBox.getChildren().addAll(cursorValueBar, chartStack);
        VBox.setVgrow(chartStack, Priority.ALWAYS);

        bbpController = new BbpController(viewModel);
        topBar = new RoastTopBar(viewModel, bbpController::togglePause);
        root.setTop(topBar);

        controlsVisible = uiPreferences != null && uiPreferences.getLayoutState().isControlsVisible();

        LayoutState layoutState = uiPreferences != null ? uiPreferences.getLayoutState() : new LayoutState();
        List<String> panelOrder = layoutState.getPanelOrder();

        ModeStripPanel modeStripPanel = new ModeStripPanel();
        DockPanel modeStripDock = new DockPanel(LayoutState.PANEL_MODE_STRIP, "Mode", modeStripPanel);
        modeStripDock.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_MODE_STRIP));
        addPanelInOrder(modeStripDock, panelOrder);

        roastSummaryPanel = new RoastSummaryPanel(viewModel);
        DockPanel roastSummaryDock = new DockPanel(LayoutState.PANEL_ROAST_SUMMARY, "Summary", roastSummaryPanel);
        roastSummaryDock.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_ROAST_SUMMARY));
        addPanelInOrder(roastSummaryDock, panelOrder);

        CurveLegendPanel legendPanel = new CurveLegendPanel(displaySettings);
        legendPanel.setOnVisibilityChanged(() -> {
            if (chartController != null) {
                chartController.applyColors();
                chartController.updateChart();
            }
        });
        legendDockPanel = new DockPanel(LayoutState.PANEL_LEGEND, "Curves", legendPanel);
        legendDockPanel.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_LEGEND));
        addPanelInOrder(legendDockPanel, panelOrder);

        btReadoutTile = new ReadoutTile("BT", viewModel.btProperty(), "%.1f", "bt", "°C");
        btReadoutTile.getStyleClass().add("ri5-readout-hero");
        etReadoutTile = new ReadoutTile("ET", viewModel.etProperty(), "%.1f", "et", "°C");
        etReadoutTile.getStyleClass().add("ri5-readout-large");
        rorReadoutTile = new ReadoutTile("RoR", viewModel.rorBTProperty(), "%.1f", "ror", "°C/min");
        rorReadoutTile.getStyleClass().add("ri5-readout-large");
        ReadoutTile timeTile = new ReadoutTile("Time", viewModel.elapsedSecProperty(), "%.1f", null, "",
            sec -> String.format("%d:%02d", (int)(sec / 60), (int)(sec % 60)));
        timeTile.getStyleClass().add("ri5-readout-secondary");
        ReadoutTile devTimeTile = new ReadoutTile("Dev Time", viewModel.devTimeSecProperty(), "%.1f", null, "",
            sec -> Double.isFinite(sec) && sec >= 0 ? String.format("%d:%02d min", (int)(sec / 60), (int)(sec % 60)) : "—");
        devTimeTile.getStyleClass().add("ri5-readout-secondary");
        ReadoutTile gasTile = new ReadoutTile("Gas", viewModel.gasPercentProperty(), "%.0f", null, "%");
        gasTile.getStyleClass().add("ri5-readout-secondary");
        ReadoutTile airTile = new ReadoutTile("Air", viewModel.airPercentProperty(), "%.0f", null, "%");
        airTile.getStyleClass().add("ri5-readout-secondary");
        ReadoutTile drumTile = new ReadoutTile("Drum", viewModel.drumPercentProperty(), "%.0f", null, "%");
        drumTile.getStyleClass().add("ri5-readout-secondary");
        if (uiPreferences != null) {
            btReadoutTile.setReadoutSize(uiPreferences.getReadoutSize());
            etReadoutTile.setReadoutSize(uiPreferences.getReadoutSize());
            rorReadoutTile.setReadoutSize(uiPreferences.getReadoutSize());
            timeTile.setReadoutSize(uiPreferences.getReadoutSize());
            devTimeTile.setReadoutSize(uiPreferences.getReadoutSize());
            gasTile.setReadoutSize(uiPreferences.getReadoutSize());
            airTile.setReadoutSize(uiPreferences.getReadoutSize());
            drumTile.setReadoutSize(uiPreferences.getReadoutSize());
        }
        VBox readoutsFlow = new VBox(8);
        readoutsFlow.getChildren().addAll(btReadoutTile, rorReadoutTile, etReadoutTile, timeTile, devTimeTile, gasTile, airTile, drumTile);
        DockPanel readoutsDock = new DockPanel(LayoutState.PANEL_READOUTS, "Readouts", readoutsFlow);
        readoutsDock.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_READOUTS));
        addPanelInOrder(readoutsDock, panelOrder);

        org.artisan.view.ControlsPanel controlsPanel = appController != null ? new org.artisan.view.ControlsPanel(appController, viewModel) : null;
        Node controlsContent = controlsPanel != null ? controlsPanel : new VBox();
        controlsDockPanel = new DockPanel(LayoutState.PANEL_CONTROLS, "Controls", controlsContent,
            controlsPanel != null ? controlsPanel.getShowControlsToggle() : null);
        controlsDockPanel.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_CONTROLS));
        controlsDockPanel.setVisible(controlsVisible);
        addPanelInOrder(controlsDockPanel, panelOrder);

        eventLogPanel = new EventLogPanel();
        if (appController != null) {
            eventLogPanel.setOnEventSelected(entry -> {
                if (chartController == null) return;
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
                    if (chartController != null) chartController.updateChart();
                }
            });
        }
        MachineReadoutsPanel machineReadoutsPanel = new MachineReadoutsPanel();
        DockPanel machineReadoutsDock = new DockPanel(LayoutState.PANEL_MACHINE_READOUTS, "Machine", machineReadoutsPanel);
        machineReadoutsDock.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_MACHINE_READOUTS));
        addPanelInOrder(machineReadoutsDock, panelOrder);

        ReferenceInfoPanel referenceInfoPanel = new ReferenceInfoPanel();
        DockPanel referenceInfoDock = new DockPanel(LayoutState.PANEL_REFERENCE_INFO, "Reference", referenceInfoPanel);
        referenceInfoDock.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_REFERENCE_INFO));
        addPanelInOrder(referenceInfoDock, panelOrder);

        DockPanel eventLogDock = new DockPanel(LayoutState.PANEL_EVENT_LOG, "Events", eventLogPanel);
        eventLogDock.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_EVENT_LOG));
        VBox.setVgrow(eventLogDock, Priority.ALWAYS);
        addPanelInOrder(eventLogDock, panelOrder);

        RoastPropertiesPanel detailsPanel = appController != null
            ? new RoastPropertiesPanel(appController, () -> {
                if (roastSummaryPanel != null) {
                    roastSummaryPanel.setRoastColor(appController.getRoastProperties().getRoastColor());
                }
            })
            : null;
        Node detailsContent = detailsPanel != null ? detailsPanel : new VBox(8, new Label("Roast Properties"));
        detailsDockPanel = new DockPanel(LayoutState.PANEL_DETAILS, "Details", detailsContent);
        detailsDockPanel.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_DETAILS));
        addPanelInOrder(detailsDockPanel, panelOrder);

        panelManager.registerPanel(modeStripDock, dockContainer);
        panelManager.registerPanel(roastSummaryDock, dockContainer);
        panelManager.registerPanel(legendDockPanel, dockContainer);
        panelManager.registerPanel(readoutsDock, dockContainer);
        panelManager.registerPanel(controlsDockPanel, dockContainer);
        panelManager.registerPanel(machineReadoutsDock, dockContainer);
        panelManager.registerPanel(referenceInfoDock, dockContainer);
        panelManager.registerPanel(eventLogDock, dockContainer);
        panelManager.registerPanel(detailsDockPanel, dockContainer);

        bottomBars.getChildren().setAll(statusBar);
        bottomBars.setMinHeight(64);
        bottomBars.setPrefHeight(80);
        bottomBars.setMaxHeight(80);
        VBox.setVgrow(statusBar, Priority.NEVER);

        root.setCenter(centerVBox);
        root.setRight(dockContainer);
        root.setBottom(bottomBars);

        statusBar.setControlsVisible(controlsVisible);
        statusBar.setOnEndRoast(() -> {
            if (appController != null) appController.stopSampling();
        });
        statusBar.setOnControlsToggle(this::toggleControlsPanel);

        if (appController != null) {
            appController.addSampleListener((bt, et, rorBT, rorET, timeSec) ->
                javafx.application.Platform.runLater(() -> {
                    viewModel.setBt(bt);
                    viewModel.setEt(et);
                    viewModel.setRorBT(rorBT);
                    viewModel.setRorET(rorET);
                    viewModel.setElapsedSec(timeSec);
                    viewModel.setSamplingActive(true);
                    statusBar.setElapsedSeconds(timeSec);
                }));
            viewModel.setConnectionStatus(appController.getCommController() != null && appController.getCommController().getActiveChannel() != null
                ? appController.getCommController().getActiveChannel().getDescription() : "Disconnected");
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
                boolean hasData = !appController.getSession().getCanvasData().getTimex().isEmpty();
                if (!hasData) statusBar.setPhase("—");
                if (chartController != null) {
                    chartController.setLiveRecording(appController.getSession().isActive());
                }
                boolean hasDrop = events.stream().anyMatch(ev -> ev.getType() == org.artisan.model.EventType.DROP);
                boolean hasChargeOnly = events.size() == 1 && !events.isEmpty() && events.get(0).getType() == org.artisan.model.EventType.CHARGE;
                if (hasDrop && !viewModel.isBbtActive()) bbpController.startBbp();
                if (hasChargeOnly && viewModel.isBbtActive()) bbpController.endBbp();
            }
        };

        if (chartController != null) {
            chartController.setOnChartBodyClick(info -> {
                showAddEventPopover(info.timeIndex, info.bt, info.et);
            });
        }

        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleShortcut);
    }

    private static String phaseNameFromResult(PhaseResult result, double elapsedSec) {
        if (result == null || result.isInvalid()) return "—";
        double drying = result.getDryingTimeSec();
        double maillard = result.getMaillardTimeSec();
        double development = result.getDevelopmentTimeSec();
        if (elapsedSec <= drying) return "Drying";
        if (elapsedSec <= drying + maillard) return "Maillard";
        if (elapsedSec <= drying + maillard + development) return "Development";
        return "—";
    }

    private void addPanelInOrder(DockPanel panel, List<String> order) {
        int idx = order != null ? order.indexOf(panel.getPanelId()) : -1;
        if (idx < 0) dockContainer.getChildren().add(panel);
        else {
            int insert = 0;
            for (Node n : dockContainer.getChildren()) {
                if (n instanceof DockPanel) {
                    int o = order.indexOf(((DockPanel) n).getPanelId());
                    if (o > idx) break;
                }
                insert++;
            }
            dockContainer.getChildren().add(Math.min(insert, dockContainer.getChildren().size()), panel);
        }
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
            if ("Charge".equals(typeStr)) type = EventType.CHARGE;
            else if ("Dry End".equals(typeStr)) type = EventType.DRY_END;
            else if ("FC Start".equals(typeStr)) type = EventType.FC_START;
            else if ("FC End".equals(typeStr)) type = EventType.FC_END;
            else if ("Drop".equals(typeStr)) type = EventType.DROP;
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
                    double t = timex.get(idx);
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
        else if (e.getCode() == KeyCode.C) { toggleControlsPanel(); e.consume(); }
        else if (e.getCode() == KeyCode.L) { toggleLegendPanel(); e.consume(); }
        else if (e.getCode() == KeyCode.E) { focusEventLog(); e.consume(); }
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
        }
        else if (e.getCode() == KeyCode.A && e.isControlDown()) {
            viewModel.setAirValue(e.isShiftDown() ? Math.max(0, viewModel.getAirValue() - 1) : Math.min(100, viewModel.getAirValue() + 1));
            if (appController != null) appController.setControlOutput("Air", viewModel.getAirValue());
            e.consume();
        }
        else if (e.getCode() == KeyCode.D && e.isControlDown()) {
            viewModel.setDrumValue(e.isShiftDown() ? Math.max(0, viewModel.getDrumValue() - 1) : Math.min(100, viewModel.getDrumValue() + 1));
            if (appController != null) appController.setControlOutput("Drum", viewModel.getDrumValue());
            e.consume();
        }
    }

    private void markEvent(EventType type) {
        if (appController != null) appController.markEvent(type);
    }

    private void toggleControlsPanel() {
        controlsVisible = !controlsVisible;
        if (controlsDockPanel != null) controlsDockPanel.setVisible(controlsVisible);
        if (statusBar != null) statusBar.setControlsVisible(controlsVisible);
        if (uiPreferences != null) uiPreferences.getLayoutState().setControlsVisible(controlsVisible);
    }

    private void toggleLegendPanel() {
        if (legendDockPanel != null) legendDockPanel.setCollapsed(!legendDockPanel.isCollapsed());
    }

    private void focusEventLog() {
        for (Node p : dockContainer.getChildren()) {
            if (p instanceof DockPanel && LayoutState.PANEL_EVENT_LOG.equals(((DockPanel) p).getPanelId())) {
                ((DockPanel) p).setCollapsed(false);
                if (eventLogPanel != null) eventLogPanel.requestFocusFilter();
                return;
            }
        }
    }

    public void setOnHamburger(Runnable r) { if (topBar != null) topBar.setOnHamburger(r); }
    public void setOnTopBarSettings(Runnable r) { if (topBar != null) topBar.setOnSettings(r); }
    public void setOnTopBarResetLayout(Runnable r) { if (topBar != null) topBar.setOnResetLayout(r); }
    public void setOnTopBarKeyboardShortcuts(Runnable r) { if (topBar != null) topBar.setOnKeyboardShortcuts(r); }
    public void setOnTopBarAbout(Runnable r) { if (topBar != null) topBar.setOnAbout(r); }

    /** Expands the Details (Roast Properties) panel. Call when user opens Properties from menu. */
    public void expandDetailsPanel() {
        if (detailsDockPanel != null) {
            detailsDockPanel.setCollapsed(false);
        }
    }

    /** Refreshes curve legend colors when palette changes (e.g. from Colors dialog). */
    public void refreshCurveLegendColors(org.artisan.controller.DisplaySettings ds) {
        if (legendDockPanel == null || ds == null) return;
        Node content = legendDockPanel.getContentNode();
        if (content instanceof CurveLegendPanel) {
            ((CurveLegendPanel) content).refreshColors(ds);
        }
    }

    public void onScreenShown() {
        if (uiPreferences != null && btReadoutTile != null) {
            btReadoutTile.setReadoutSize(uiPreferences.getReadoutSize());
            etReadoutTile.setReadoutSize(uiPreferences.getReadoutSize());
            rorReadoutTile.setReadoutSize(uiPreferences.getReadoutSize());
        }
        if (roastSummaryPanel != null && appController != null) {
            roastSummaryPanel.setRoastColor(appController.getRoastProperties().getRoastColor());
        }
        if (statusTimer != null) statusTimer.start();
        if (chartController != null) chartController.startUpdateTimer();
        if (appController != null) {
            String conn = appController.getCommController() != null && appController.getCommController().getActiveChannel() != null
                ? appController.getCommController().getActiveChannel().getDescription() : "Disconnected";
            statusBar.setConnectionStatus(conn);
        }
    }

    public Pane getRoot() {
        return root;
    }

    public RoastViewModel getViewModel() {
        return viewModel;
    }

    public void saveLayoutState() {
        if (uiPreferences == null || preferencesStore == null) return;
        LayoutState layout = uiPreferences.getLayoutState();
        double w = dockContainer.getWidth();
        if (w > 0) {
            layout.setDockWidth(Math.max(LayoutState.MIN_DOCK_WIDTH, Math.min(LayoutState.MAX_DOCK_WIDTH, w)));
        }
        layout.setControlsVisible(controlsVisible);
        List<String> currentOrder = new java.util.ArrayList<>();
        for (Node child : dockContainer.getChildren()) {
            if (child instanceof DockPanel) {
                DockPanel panel = (DockPanel) child;
                currentOrder.add(panel.getPanelId());
                layout.setPanelCollapsed(panel.getPanelId(), panel.isCollapsed());
            }
        }
        if (!currentOrder.isEmpty()) {
            layout.setPanelOrder(currentOrder);
        }
        if (panelManager != null) {
            panelManager.syncDetachedBoundsToLayoutState();
            for (String panelId : layout.getPanelOrder()) {
                layout.setPanelDetached(panelId, panelManager.isDetached(panelId));
            }
        }
        preferencesStore.save(uiPreferences);
    }

    /** Restores panels that were saved as detached. Call after main window is shown. */
    public void restoreDetachedPanels() {
        if (panelManager != null) panelManager.restoreDetachedPanels();
    }

    public void closeDetachedPanels() {
        if (panelManager != null) panelManager.closeAllDetached();
    }

    /**
     * Applies layout from uiPreferences to the live UI (divider, dock width, panel states).
     * Call after reset layout or when preferences change. Redocks any detached panels.
     */
    public void applyLayoutFromPreferences() {
        if (uiPreferences == null) return;
        LayoutState layout = uiPreferences.getLayoutState();
        if (layout == null) return;

        if (panelManager != null) panelManager.closeAllDetached();

        dockContainer.setPrefWidth(layout.getDockWidth());

        controlsVisible = layout.isControlsVisible();
        if (controlsDockPanel != null) controlsDockPanel.setVisible(controlsVisible);
        if (statusBar != null) statusBar.setControlsVisible(controlsVisible);

        for (javafx.scene.Node child : dockContainer.getChildren()) {
            if (child instanceof DockPanel) {
                DockPanel panel = (DockPanel) child;
                panel.setCollapsed(layout.isPanelCollapsed(panel.getPanelId()));
            }
        }
    }
}
