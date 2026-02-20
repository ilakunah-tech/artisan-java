package org.artisan.ui.screens;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.artisan.controller.AppController;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.EventEntry;
import org.artisan.model.EventType;
import org.artisan.ui.components.*;
import org.artisan.ui.state.LayoutState;
import org.artisan.ui.vm.RoastViewModel;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;
import org.artisan.view.PhasesCanvasPanel;
import org.artisan.view.RoastChartController;
import org.artisan.model.PhaseResult;

import java.util.List;

/**
 * Roast (Live) screen: dominant chart, right dock (collapsible/detachable panels), bottom status strip.
 */
public final class RoastLiveScreen {

    private final BorderPane root;
    private final SplitPane mainSplit;
    private final VBox dockContainer;
    private final BottomStatusBar statusBar;
    private final RoastViewModel viewModel;
    private final AppController appController;
    private final RoastChartController chartController;
    private final DisplaySettings displaySettings;
    private final UIPreferences uiPreferences;
    private final PreferencesStore preferencesStore;
    private final DetachablePanelManager panelManager;
    private final Stage primaryStage;
    private DockPanel controlsDockPanel;
    private DockPanel legendDockPanel;
    private EventLogPanel eventLogPanel;
    private ReadoutTile btReadoutTile;
    private ReadoutTile etReadoutTile;
    private ReadoutTile rorReadoutTile;
    private javafx.animation.AnimationTimer statusTimer;
    private boolean controlsVisible = true;

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
        mainSplit = new SplitPane();
        mainSplit.setMinSize(0, 0);
        dockContainer = new VBox(8);
        dockContainer.getStyleClass().add("ri5-dock-container");
        dockContainer.setMinWidth(LayoutState.MIN_DOCK_WIDTH);
        dockContainer.setPrefWidth(uiPreferences != null ? uiPreferences.getLayoutState().getDockWidth() : LayoutState.DEFAULT_DOCK_WIDTH);
        statusBar = new BottomStatusBar();
        panelManager = new DetachablePanelManager();
        panelManager.setPrimaryWindow(primaryStage);
        panelManager.setLayoutState(uiPreferences != null ? uiPreferences.getLayoutState() : new LayoutState());

        Node chartView = chartController != null ? chartController.getView() : new Pane();
        if (chartView instanceof Region) {
            ((Region) chartView).setMinSize(0, 0);
        }
        PhasesCanvasPanel phasesPanel = new PhasesCanvasPanel(PhaseResult.INVALID);
        if (chartController != null) {
            phasesPanel.setOnMarkerClick((id, timeSec) -> {
                /* Optional: center chart on marker time; axis config controls visible range */
            });
        }
        if (appController != null) {
            appController.addPhaseListener(result -> javafx.application.Platform.runLater(() -> {
                phasesPanel.refresh(result);
                double elapsed = viewModel.getElapsedSec();
                String phaseName = phaseNameFromResult(result, elapsed);
                viewModel.setPhaseName(phaseName);
                viewModel.setDevTimeSec(result != null && !result.isInvalid() ? result.getDevelopmentTimeSec() : Double.NaN);
                statusBar.setPhase(phaseName);
            }));
        }

        VBox centerContent = new VBox(chartView, phasesPanel);
        centerContent.getStyleClass().add("ri5-chart-container");
        centerContent.setMinSize(0, 0);
        VBox.setVgrow(chartView, Priority.ALWAYS);
        centerContent.setSpacing(4);

        controlsVisible = uiPreferences != null && uiPreferences.getLayoutState().isControlsVisible();

        mainSplit.getItems().addAll(centerContent, dockContainer);
        mainSplit.setDividerPosition(0, uiPreferences != null ? uiPreferences.getMainDividerPosition() : 0.75);

        LayoutState layoutState = uiPreferences != null ? uiPreferences.getLayoutState() : new LayoutState();
        List<String> panelOrder = layoutState.getPanelOrder();

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
        etReadoutTile = new ReadoutTile("ET", viewModel.etProperty(), "%.1f", "et", "°C");
        rorReadoutTile = new ReadoutTile("RoR", viewModel.rorBTProperty(), "%.1f", "ror", "°C/min");
        ReadoutTile timeTile = new ReadoutTile("Time", viewModel.elapsedSecProperty(), "%.1f", null, "",
            sec -> String.format("%d:%02d", (int)(sec / 60), (int)(sec % 60)));
        ReadoutTile devTimeTile = new ReadoutTile("Dev Time", viewModel.devTimeSecProperty(), "%.1f", null, "",
            sec -> Double.isFinite(sec) && sec >= 0 ? String.format("%d:%02d min", (int)(sec / 60), (int)(sec % 60)) : "—");
        if (uiPreferences != null) {
            btReadoutTile.setReadoutSize(uiPreferences.getReadoutSize());
            etReadoutTile.setReadoutSize(uiPreferences.getReadoutSize());
            rorReadoutTile.setReadoutSize(uiPreferences.getReadoutSize());
            timeTile.setReadoutSize(uiPreferences.getReadoutSize());
            devTimeTile.setReadoutSize(uiPreferences.getReadoutSize());
        }
        GridPane readoutsGrid = new GridPane();
        readoutsGrid.setHgap(8);
        readoutsGrid.setVgap(8);
        readoutsGrid.add(btReadoutTile, 0, 0);
        readoutsGrid.add(etReadoutTile, 1, 0);
        readoutsGrid.add(rorReadoutTile, 0, 1);
        readoutsGrid.add(timeTile, 1, 1);
        readoutsGrid.add(devTimeTile, 0, 2);
        GridPane.setHgrow(btReadoutTile, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(etReadoutTile, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(rorReadoutTile, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(timeTile, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(devTimeTile, javafx.scene.layout.Priority.ALWAYS);
        DockPanel readoutsDock = new DockPanel(LayoutState.PANEL_READOUTS, "Readouts", readoutsGrid);
        readoutsDock.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_READOUTS));
        addPanelInOrder(readoutsDock, panelOrder);

        org.artisan.view.ControlsPanel controlsPanel = appController != null ? new org.artisan.view.ControlsPanel(appController) : null;
        Node controlsContent = controlsPanel != null ? controlsPanel : new VBox();
        controlsDockPanel = new DockPanel(LayoutState.PANEL_CONTROLS, "Controls", controlsContent,
            controlsPanel != null ? controlsPanel.getShowControlsToggle() : null);
        controlsDockPanel.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_CONTROLS));
        controlsDockPanel.setVisible(controlsVisible);
        addPanelInOrder(controlsDockPanel, panelOrder);

        eventLogPanel = new EventLogPanel();
        if (appController != null) {
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
        DockPanel eventLogDock = new DockPanel(LayoutState.PANEL_EVENT_LOG, "Event Log", eventLogPanel);
        eventLogDock.setCollapsed(layoutState.isPanelCollapsed(LayoutState.PANEL_EVENT_LOG));
        addPanelInOrder(eventLogDock, panelOrder);

        panelManager.registerPanel(legendDockPanel, dockContainer);
        panelManager.registerPanel(readoutsDock, dockContainer);
        panelManager.registerPanel(controlsDockPanel, dockContainer);
        panelManager.registerPanel(eventLogDock, dockContainer);

        root.setCenter(mainSplit);
        root.setBottom(statusBar);

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
                viewModel.syncEvents(appController.getSession().getEvents().getAll());
                eventLogPanel.setTimex(appController.getSession().getCanvasData().getTimex());
                eventLogPanel.setEvents(appController.getSession().getEvents().getAll());
                boolean hasData = !appController.getSession().getCanvasData().getTimex().isEmpty();
                if (!hasData) statusBar.setPhase("—");
                if (chartController != null) {
                    chartController.setLiveRecording(appController.getSession().isActive());
                }
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
        Dialog<EventType> d = new Dialog<>();
        d.initOwner(primaryStage);
        d.setTitle("Add event");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Charge", "Dry End", "FC Start", "FC End", "Drop", "Custom");
        typeCombo.getSelectionModel().select(0);
        TextField noteField = new TextField();
        noteField.setPromptText("Note (optional)");
        VBox content = new VBox(8, new Label("Type:"), typeCombo, new Label("Note:"), noteField);
        content.setPadding(new Insets(12));
        d.getDialogPane().setContent(content);

        final int idx = timeIndex;
        final double btVal = bt;
        d.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
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
            return type;
        });
        d.showAndWait();
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
        Node n = root.getCenter();
        if (n instanceof SplitPane) {
            for (Node child : ((SplitPane) n).getItems()) {
                if (child instanceof VBox) {
                    for (Node p : ((VBox) child).getChildren()) {
                        if (p instanceof DockPanel && LayoutState.PANEL_EVENT_LOG.equals(((DockPanel) p).getPanelId())) {
                            ((DockPanel) p).setCollapsed(false);
                            if (eventLogPanel != null) eventLogPanel.requestFocusFilter();
                            return;
                        }
                    }
                }
            }
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

    public void saveLayoutState() {
        if (uiPreferences == null || preferencesStore == null) return;
        LayoutState layout = uiPreferences.getLayoutState();
        layout.setDockWidth(dockContainer.getWidth() > 0 ? dockContainer.getWidth() : layout.getDockWidth());
        layout.setControlsVisible(controlsVisible);
        double[] div = mainSplit.getDividerPositions();
        if (div != null && div.length > 0) uiPreferences.setMainDividerPosition(div[0]);
        for (Node child : dockContainer.getChildren()) {
            if (child instanceof DockPanel) {
                DockPanel panel = (DockPanel) child;
                layout.setPanelCollapsed(panel.getPanelId(), panel.isCollapsed());
            }
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

        mainSplit.setDividerPosition(0, uiPreferences.getMainDividerPosition());
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
