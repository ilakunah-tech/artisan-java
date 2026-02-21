package org.artisan.view;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.layout.HBox;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.artisan.controller.AppController;
import org.artisan.controller.AppSettings;
import org.artisan.controller.AutoSave;
import org.artisan.controller.BackgroundSettings;
import org.artisan.controller.CommController;
import org.artisan.controller.DeviceManager;
import org.artisan.controller.DisplaySettings;
import org.artisan.controller.FileSession;
import org.artisan.controller.PhasesSettings;
import org.artisan.controller.RoastSession;
import org.artisan.controller.RoastStateMachine;
import org.artisan.controller.Sample;
import org.artisan.device.AillioR1Config;
import org.artisan.device.BleDeviceChannel;
import org.artisan.device.BlePortConfig;
import org.artisan.device.DeviceChannel;
import org.artisan.device.DeviceConfig;
import org.artisan.device.DevicePort;
import org.artisan.device.DeviceType;
import org.artisan.device.ModbusDeviceChannel;
import org.artisan.device.ModbusPortConfig;
import org.artisan.device.SerialDeviceChannel;
import org.artisan.device.SerialPortConfig;
import org.artisan.device.S7Config;
import org.artisan.device.SimulatorConfig;
import org.artisan.device.StubDevice;
import org.artisan.controller.EventButtonConfigPersistence;
import org.artisan.model.ArtisanTime;
import org.artisan.model.AxisConfig;
import org.artisan.model.ColorConfig;
import org.artisan.model.CurveSet;
import org.artisan.model.EventButtonConfig;
import org.artisan.model.EventEntry;
import org.artisan.model.EventType;
import org.artisan.model.BackgroundProfile;
import org.artisan.model.PhaseResult;
import org.artisan.model.ProfileData;
import org.artisan.model.CanvasData;
import org.artisan.model.Roastlog;
import org.artisan.model.Sampling;
import org.artisan.model.SamplingConfig;
import org.artisan.util.CropsterConverter;
import org.artisan.view.BatchesDialog;
import org.artisan.view.CalculatorDialog;
import org.artisan.view.ComparatorView;
import org.artisan.view.ControlsPanel;
import org.artisan.view.LargeLCDsDialog;
import org.artisan.view.LogViewer;
import org.artisan.view.NotificationLevel;
import org.artisan.view.ProductionReportDialog;
import org.artisan.view.QrCodeDialog;
import org.artisan.view.RankingReportDialog;
import org.artisan.view.RoastReportDialog;
import org.artisan.view.S7Dialog;
import org.artisan.view.SimulatorDialog;
import org.artisan.view.StatusBar;
import org.artisan.view.TransposerDialog;
import org.artisan.view.chart.RoastOverlayCanvas;
import org.artisan.ui.AppShell;
import org.artisan.ui.DemoRunner;
import org.artisan.ui.components.ShortcutHelpDialog;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Main application window: toolbar (ON/OFF, event buttons), chart, status bar.
 * Applies AtlantaFX Primer Dark theme. Wires Sampling to chart update on JavaFX thread.
 */
public final class MainWindow extends Application {

  private Stage primaryStage;
  private AppController appController;
  private AppSettings appSettings;
  private DisplaySettings displaySettings;
  private PhasesSettings phasesSettings;
  private BackgroundSettings backgroundSettings;
  private FileSession fileSession;
  private AutoSave autoSave;
  private Label statusBar;
  private Label elapsedLabel;
  private StatusBar statusBarComponent;
  private PhasesLCD phasesLCD;
  private StatisticsPanel statisticsPanel;
  private boolean samplingOn;
  private HBox customEventButtonsBox;
  private AxisConfig axisConfig;
  private RoastChartController chartController;
  private CanvasData canvasData;
  private final RoastStateMachine roastStateMachine = new RoastStateMachine();
  private SamplingConfig samplingConfig;
  private SerialPortConfig serialPortConfig;
  private ModbusPortConfig modbusPortConfig;
  private BlePortConfig blePortConfig;
  private DeviceConfig deviceConfig;
  private SimulatorConfig simulatorConfig;
  private S7Config s7Config;
  private AillioR1Config aillioR1Config;
  private CommController commController;
  private AppShell appShell;
  private DemoRunner demoRunner;
  private PreferencesStore preferencesStore;
  private UIPreferences uiPreferences;

  @Override
  public void start(Stage primaryStage) {
    final Stage stage = primaryStage;
    appSettings = AppSettings.load();
    preferencesStore = new PreferencesStore();
    uiPreferences = preferencesStore.load();
    displaySettings = DisplaySettings.load();
    phasesSettings = PhasesSettings.load();
    backgroundSettings = BackgroundSettings.load();
    boolean useLightTheme = (uiPreferences != null && "light".equals(uiPreferences.getTheme()))
        || (uiPreferences == null && !appSettings.isDarkTheme());
    applyAtlantaFXTheme(!useLightTheme);

    fileSession = new FileSession();
    autoSave = new AutoSave();
    autoSave.load();

    RoastSession session = new RoastSession();
    canvasData = session.getCanvasData();
    ArtisanTime timeclock = new ArtisanTime();
    Sampling sampling = new Sampling(timeclock);
    DevicePort device = new StubDevice();
    ColorConfig colorConfig = new ColorConfig(useLightTheme
        ? ColorConfig.Theme.LIGHT : ColorConfig.Theme.DARK);
    syncColorConfigFromDisplaySettings(colorConfig, displaySettings);
    axisConfig = new AxisConfig();
    AxisConfig.loadFromPreferences(axisConfig);
    samplingConfig = new SamplingConfig();
    SamplingConfig.loadFromPreferences(samplingConfig);
    chartController = new RoastChartController(
        session.getCanvasData(), colorConfig, axisConfig, displaySettings);
    chartController.setPhasesConfig(phasesSettings.toConfig());
    chartController.setBackgroundSettings(backgroundSettings);
    chartController.setEventList(session.getEvents());
    chartController.setOnEventMoved(() -> fileSession.markDirty());
    chartController.setOnChartRightClick(info ->
        Platform.runLater(() -> showPhaseContextMenu(info)));
    maybeLoadBackgroundProfile(chartController, backgroundSettings);

    appController = new AppController(
        session, sampling, device, chartController, axisConfig, colorConfig, CurveSet.createDefault());
    appController.setDisplaySettings(displaySettings);
    appController.setPhasesSettings(phasesSettings);
    appController.setFileSession(fileSession);
    appController.setAutoSave(autoSave);
    appController.setSamplingConfig(samplingConfig);

    serialPortConfig = new SerialPortConfig();
    SerialPortConfig.loadFromPreferences(serialPortConfig);
    modbusPortConfig = new ModbusPortConfig();
    ModbusPortConfig.loadFromPreferences(modbusPortConfig);
    blePortConfig = new BlePortConfig();
    BlePortConfig.loadFromPreferences(blePortConfig);
    deviceConfig = new DeviceConfig();
    deviceConfig.load();
    simulatorConfig = new SimulatorConfig();
    SimulatorConfig.loadFromPreferences(simulatorConfig);
    s7Config = new S7Config();
    S7Config.loadFromPreferences(s7Config);
    aillioR1Config = new AillioR1Config();
    AillioR1Config.loadFromPreferences(aillioR1Config);
    commController = new CommController();
    DeviceChannel defaultChannel = DeviceManager.createChannel(
        deviceConfig.getActiveType(), serialPortConfig, modbusPortConfig);
    commController.setChannel(defaultChannel);
    commController.setOnSample(result -> Platform.runLater(() ->
        appController.acceptSampleFromComm(commController.getElapsedMs() / 1000.0, result.bt(), result.et())));
    commController.setOnError(() -> Platform.runLater(() -> {
      if (statusBar != null) statusBar.setText("Device error");
    }));
    appController.setCommController(commController);

    fileSession.setOnStateChange(() -> Platform.runLater(() -> updateWindowTitle()));
    statisticsPanel = new StatisticsPanel();
    appController.setStatisticsUpdateConsumer(update -> statisticsPanel.update(
        update.getStats(), update.getPhase(), update.getDtr(), update.getAuc(), update.getAucBaseTempC()));

    appController.setOnSampleConsumer(s -> Platform.runLater(() -> {
      roastStateMachine.onSample(s.timeSec(), s.bt());
      if (appController.getChartController() != null) {
        appController.getChartController().onSample(s.timeSec(), s.bt(), s.et());
      }
      appController.afterSample(s);
      appController.notifySampleListeners(s);
    }));

    statusBarComponent = new StatusBar();
    appController.addSampleListener((bt, et, rorBT, rorET, timeSec) -> {
      statusBarComponent.updateSample(bt, et, rorBT, rorET, timeSec);
      statusBarComponent.setState(appController.getCurrentState());
    });

    appController.setMarkEventCallback(label -> {
      var cd = appController.getSession().getCanvasData();
      var timex = cd.getTimex();
      if (timex.isEmpty()) return;
      int idx = timex.size() - 1;
      double temp = idx < cd.getTemp2().size() ? cd.getTemp2().get(idx) : 0.0;
      appController.getSession().getEvents().add(new EventEntry(idx, temp, label != null ? label : "Event", EventType.CUSTOM));
      fileSession.markDirty();
      if (appController.getChartController() != null) appController.getChartController().updateChart();
    });

    appController.setBurnerCallback(pct -> {
      String msg = String.format("Burner: %.0f%%", pct);
      if (statusBar != null) statusBar.setText(msg);
    });

    BorderPane root = new BorderPane();
    primaryStage = primaryStage;

    syncDisplaySettingsFromUIPreferences(displaySettings, uiPreferences);

    root.getStyleClass().add("ri5-root");
    if ("light".equals(uiPreferences != null ? uiPreferences.getTheme() : null)) {
      root.getStyleClass().add("ri5-light");
    }
    UIPreferences.Density density = uiPreferences != null ? uiPreferences.getDensity() : UIPreferences.Density.COMFORTABLE;
    root.getStyleClass().add(density == UIPreferences.Density.COMPACT ? "ri5-density-compact" : "ri5-density-comfortable");

    Menu fileMenu = buildFileMenu(root, chartController);
    Menu viewMenu = buildViewMenu(root, chartController);
    Menu roastMenu = new Menu("Roast");
    MenuItem propertiesItem = new MenuItem("Properties...");
    propertiesItem.setOnAction(e -> openRoastPropertiesInPanel());
    MenuItem cupProfileItem = new MenuItem("Cup Profile...");
    cupProfileItem.setOnAction(e -> openCupProfileDialog(root));
    MenuItem batchesItem = new MenuItem("Batches...");
    batchesItem.setOnAction(e -> new BatchesDialog(stage, appController).showAndWait());
    MenuItem roastReportItem = new MenuItem("Roast Report...");
    roastReportItem.setOnAction(e -> new RoastReportDialog(stage, appController).showAndWait());
    MenuItem productionReportItem = new MenuItem("Production Report...");
    productionReportItem.setOnAction(e -> new ProductionReportDialog(stage, appController).showAndWait());
    MenuItem rankingReportItem = new MenuItem("Ranking Report...");
    rankingReportItem.setOnAction(e -> new RankingReportDialog(stage, appController).showAndWait());
    roastMenu.getItems().addAll(propertiesItem, cupProfileItem, batchesItem, roastReportItem, productionReportItem, rankingReportItem);
    Menu toolsMenu = new Menu("Tools");
    MenuItem comparatorItem = new MenuItem("Comparator...");
    comparatorItem.setOnAction(e -> appController.openComparator(stage));
    MenuItem designerItem = new MenuItem("Designer...");
    designerItem.setOnAction(e -> appController.openDesigner(stage));
    MenuItem transposerItem = new MenuItem("Transposer...");
    transposerItem.setOnAction(e -> new TransposerDialog(stage, appController).showAndWait());
    MenuItem simulatorItem = new MenuItem("Simulator...");
    simulatorItem.setOnAction(e -> new SimulatorDialog(stage, appController).showAndWait());
    MenuItem calculatorItem = new MenuItem("Calculator...");
    calculatorItem.setOnAction(e -> new CalculatorDialog(stage, appController).showAndWait());
    toolsMenu.getItems().addAll(comparatorItem, designerItem, transposerItem, simulatorItem, calculatorItem);

    Menu configMenu = new Menu("Config");
    MenuItem axesItem = new MenuItem("Axes...");
    axesItem.setOnAction(e -> openAxesDialog(root, chartController));
    MenuItem samplingItem = new MenuItem("Sampling...");
    samplingItem.setOnAction(e -> openSamplingDialog(root, chartController));
    MenuItem portsItem = new MenuItem("Ports...");
    portsItem.setOnAction(e -> openPortsDialog(root));
    MenuItem deviceItem = new MenuItem("Device...");
    deviceItem.setOnAction(e -> openDevicesDialog(root));
    MenuItem s7Item = new MenuItem("S7 PLC...");
    s7Item.setOnAction(e -> openS7Dialog(root));
    MenuItem phasesItem = new MenuItem("Phases");
    phasesItem.setOnAction(e -> openPhasesDialog(root, chartController));
    MenuItem backgroundItem = new MenuItem("Background...");
    backgroundItem.setOnAction(e -> openBackgroundDialog(root, chartController));
    MenuItem eventsItem = new MenuItem("Events...");
    eventsItem.setOnAction(e -> openEventButtonsDialog(root));
    MenuItem alarmsItem = new MenuItem("Alarms...");
    alarmsItem.setOnAction(e -> openAlarmsDialog(root));
    MenuItem autosaveItem = new MenuItem("Autosave...");
    autosaveItem.setOnAction(e -> openAutoSaveDialog(root));
    MenuItem replayItem = new MenuItem("Replay...");
    replayItem.setOnAction(e -> openReplayDialog(root));
    MenuItem pidItem = new MenuItem("PID...");
    pidItem.setOnAction(e -> openPidDialog(root));
    MenuItem resetLayoutItem = new MenuItem("Reset Layout...");
    resetLayoutItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Shift+R"));
    resetLayoutItem.setOnAction(e -> doResetLayout());
    configMenu.getItems().addAll(axesItem, samplingItem, portsItem, deviceItem, s7Item, new SeparatorMenuItem(), phasesItem, backgroundItem, new SeparatorMenuItem(), eventsItem, alarmsItem, autosaveItem, replayItem, new SeparatorMenuItem(), pidItem, new SeparatorMenuItem(), resetLayoutItem);

    Menu helpMenu = new Menu("Help");
    MenuItem shortcutsItem = new MenuItem("Keyboard Shortcuts...");
    shortcutsItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("F1"));
    shortcutsItem.setOnAction(e -> ShortcutHelpDialog.show(stage));
    MenuItem aboutItem = new MenuItem("About Artisan Java...");
    aboutItem.setOnAction(e -> new PlatformDialog(stage).showAndWait());
    MenuItem qrCodeItem = new MenuItem("QR Code...");
    qrCodeItem.setOnAction(e -> new QrCodeDialog(stage, appController).show());
    MenuItem logViewerItem = new MenuItem("Open Log Viewer");
    logViewerItem.setOnAction(e -> new LogViewer(stage).show());
    MenuItem githubItem = new MenuItem("GitHub Repository");
    githubItem.setOnAction(e -> {
      try {
        Desktop.getDesktop().browse(URI.create("https://github.com/ilakunah-tech/artisan-java"));
      } catch (Exception ex) {
        new Alert(Alert.AlertType.ERROR, "Could not open URL: " + ex.getMessage()).showAndWait();
      }
    });
    helpMenu.getItems().addAll(shortcutsItem, aboutItem, qrCodeItem, logViewerItem, githubItem);

    MenuButton menuOverflowBtn = new MenuButton("\u2630");
    menuOverflowBtn.setTooltip(new javafx.scene.control.Tooltip("Menu"));
    menuOverflowBtn.getStyleClass().add("ri5-menu-overflow");
    menuOverflowBtn.getItems().addAll(fileMenu, viewMenu, roastMenu, toolsMenu, configMenu, helpMenu);

    phasesLCD = new PhasesLCD(phasesSettings);

    appShell = new AppShell(primaryStage, appController, chartController, displaySettings, uiPreferences, preferencesStore);
    appShell.setOnStart(() -> {
      roastStateMachine.onStartPressed();
      RoastStateMachine.State st = roastStateMachine.getState();
      if (st == RoastStateMachine.State.PRE_ROAST) {
        startRecording();
        setTimerPreRoastMode();
      } else if (st == RoastStateMachine.State.ROASTING) {
        setTimerRoastingMode();
      }
    });
    appShell.addLeadingToTopBar(menuOverflowBtn);
    appShell.setOnCurveVisibilitySync(() -> syncDisplaySettingsFromUIPreferences(displaySettings, uiPreferences));
    appShell.setOnSettings(() -> openDeviceSettings(root));
    appShell.setOnResetLayout(this::doResetLayout);
    appShell.setOnOpenReplay(() -> openReplayDialog(root));
    appShell.setOnOpenRecent(path -> openRecentFile(path, root, chartController));
    appShell.setMachineName(deviceConfig.getActiveType() != null ? deviceConfig.getActiveType().getDisplayName() : "—");
    demoRunner = new DemoRunner(appController);
    appShell.setDemoRunner(demoRunner);
    appShell.getRoastLiveScreen().setRoastStateMachine(roastStateMachine);

    roastStateMachine.setOnAutoCharge(
        () -> Platform.runLater(this::handleAutoCharge));
    roastStateMachine.setOnPreRoastTimeout(
        () -> Platform.runLater(this::handlePreRoastTimeout));

    StackPane centerWithOverlay = appShell.getRoot();
    centerWithOverlay.setMinSize(0, 0);
    root.setCenter(centerWithOverlay);
    appController.setMainRoot(centerWithOverlay);

    appController.refreshStatistics();

    Scene scene = new Scene(root, 1366, 820);
    scene.getStylesheets().clear();
    scene.getStylesheets().add(
        getClass().getResource("/org/artisan/ui/theme/tokens.css").toExternalForm());
    scene.getStylesheets().add(
        getClass().getResource("/org/artisan/ui/theme/app.css").toExternalForm());
    scene.getStylesheets().add(
        Objects.requireNonNull(
            getClass().getResource("/org/artisan/roast-chart.css")
        ).toExternalForm());
    scene.getRoot().setStyle(null);
    scene.setFill(Color.TRANSPARENT);
    primaryStage.initStyle(StageStyle.UNDECORATED);
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(1280);
    primaryStage.setMinHeight(720);
    primaryStage.setMaximized(true);
    registerAccelerators(scene, root, chartController);
    updateWindowTitle();
    primaryStage.setOnCloseRequest(e -> {
      if (appController.isSessionDirty()) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved changes");
        alert.setContentText("Save before closing?");
        ButtonType save = new ButtonType("Save");
        ButtonType discard = new ButtonType("Discard");
        ButtonType cancel = ButtonType.CANCEL;
        alert.getButtonTypes().setAll(save, discard, cancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.orElse(cancel) == save) {
          doSave();
        } else if (result.orElse(cancel) == cancel) {
          e.consume();
          return;
        }
      }
      appController.stopSampling();
      if (demoRunner != null) demoRunner.stop();
      if (appShell != null && appShell.getRoastLiveScreen() != null) {
        appShell.getRoastLiveScreen().saveLayoutState();
        appShell.getRoastLiveScreen().closeDetachedPanels();
      }
      autoSave.stop();
      appSettings.save();
      org.artisan.Launcher.releaseLock();
      Platform.exit();
    });
    primaryStage.show();
    Platform.runLater(() -> {
      if (appShell != null && appShell.getRoastLiveScreen() != null) {
        appShell.getRoastLiveScreen().restoreDetachedPanels();
      }
    });
  }

  @Override
  public void stop() {
    org.artisan.Launcher.releaseLock();
  }

  private Menu buildViewMenu(BorderPane root, RoastChartController chartController) {
    Menu viewMenu = new Menu("View");
    CheckMenuItem showCrosshair = new CheckMenuItem("Show Crosshair");
    showCrosshair.setSelected(displaySettings.isShowCrosshair());
    showCrosshair.setOnAction(e -> {
      displaySettings.setShowCrosshair(showCrosshair.isSelected());
      if (appController.getChartController() != null) {
        appController.getChartController().applyColors();
        appController.getChartController().updateChart();
      }
    });
    CheckMenuItem showWatermark = new CheckMenuItem("Show Watermark");
    showWatermark.setSelected(displaySettings.isShowWatermark());
    showWatermark.setOnAction(e -> {
      displaySettings.setShowWatermark(showWatermark.isSelected());
      if (appController.getChartController() != null) {
        appController.getChartController().applyColors();
        appController.getChartController().updateChart();
      }
    });
    CheckMenuItem showLegend = new CheckMenuItem("Show Legend");
    showLegend.setSelected(displaySettings.isShowLegend());
    showLegend.setOnAction(e -> {
      displaySettings.setShowLegend(showLegend.isSelected());
      if (appController.getChartController() != null) {
        appController.getChartController().applyColors();
        appController.getChartController().updateChart();
      }
    });
    viewMenu.getItems().addAll(showCrosshair, showWatermark, showLegend, new SeparatorMenuItem());
    MenuItem largeLcdsItem = new MenuItem("Large LCDs...");
    largeLcdsItem.setOnAction(e -> new LargeLCDsDialog(primaryStage, appController).show());
    MenuItem logViewerItem = new MenuItem("Log Viewer...");
    logViewerItem.setOnAction(e -> new LogViewer(primaryStage).show());
    viewMenu.getItems().addAll(largeLcdsItem, logViewerItem, new SeparatorMenuItem());
    MenuItem fullScreenItem = new MenuItem("Full Screen");
    fullScreenItem.setOnAction(e -> primaryStage.setFullScreen(!primaryStage.isFullScreen()));
    viewMenu.getItems().addAll(fullScreenItem, new SeparatorMenuItem());
    CheckMenuItem showDeltaCurves = new CheckMenuItem("Show Delta Curves");
    showDeltaCurves.setSelected(displaySettings.isVisibleDeltaET() && displaySettings.isVisibleDeltaBT());
    showDeltaCurves.setOnAction(e -> {
      boolean on = showDeltaCurves.isSelected();
      displaySettings.setVisibleDeltaET(on);
      displaySettings.setVisibleDeltaBT(on);
      if (appController.getChartController() != null) {
        appController.getChartController().applyColors();
        appController.getChartController().updateChart();
      }
    });
    CheckMenuItem showET = new CheckMenuItem("Show ET");
    showET.setSelected(displaySettings.isVisibleET());
    showET.setOnAction(e -> {
      displaySettings.setVisibleET(showET.isSelected());
      if (appController.getChartController() != null) {
        appController.getChartController().applyColors();
        appController.getChartController().updateChart();
      }
    });
    CheckMenuItem showBT = new CheckMenuItem("Show BT");
    showBT.setSelected(displaySettings.isVisibleBT());
    showBT.setOnAction(e -> {
      displaySettings.setVisibleBT(showBT.isSelected());
      if (appController.getChartController() != null) {
        appController.getChartController().applyColors();
        appController.getChartController().updateChart();
      }
    });
    viewMenu.getItems().addAll(showDeltaCurves, showET, showBT);
    return viewMenu;
  }

  private Menu buildFileMenu(BorderPane root, RoastChartController chartController) {
    Menu fileMenu = new Menu("File");
    MenuItem newItem = new MenuItem("New");
    newItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+N"));
    newItem.setOnAction(e -> doNew(root));
    MenuItem openItem = new MenuItem("Open...");
    openItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+O"));
    openItem.setOnAction(e -> doOpen(root, chartController));
    MenuItem saveItem = new MenuItem("Save");
    saveItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+S"));
    saveItem.setOnAction(e -> doSave());
    MenuItem saveAsItem = new MenuItem("Save As...");
    saveAsItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Shift+S"));
    saveAsItem.setOnAction(e -> doSaveAs());
    Menu recentMenu = new Menu("Recent Files");
    MenuItem clearRecentItem = new MenuItem("Clear Recent Files");
    clearRecentItem.setOnAction(e -> {
      fileSession.clearRecentFiles();
      refreshRecentMenu(recentMenu, clearRecentItem, root, chartController);
    });
    fileMenu.setOnShowing(e -> refreshRecentMenu(recentMenu, clearRecentItem, root, chartController));
    MenuItem exportItem = new MenuItem("Export...");
    exportItem.setOnAction(e -> doExport(root));
    MenuItem exportCropsterItem = new MenuItem("Export to Cropster CSV...");
    exportCropsterItem.setOnAction(e -> doExportToCropster());
    MenuItem importCropsterItem = new MenuItem("Import from Cropster CSV...");
    importCropsterItem.setOnAction(e -> doImportFromCropster());
    MenuItem quitItem = new MenuItem("Quit");
    quitItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Q"));
    quitItem.setOnAction(e -> primaryStage.getOnCloseRequest().handle(new javafx.stage.WindowEvent(primaryStage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST)));

    fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem,
        new SeparatorMenuItem(), recentMenu, new SeparatorMenuItem(),
        exportItem, exportCropsterItem, importCropsterItem,
        new SeparatorMenuItem(), quitItem);
    refreshRecentMenu(recentMenu, clearRecentItem, root, chartController);
    return fileMenu;
  }

  private void doExportToCropster() {
    ProfileData pd = appController != null ? appController.getCurrentProfileData() : null;
    if (pd == null || pd.getTimex() == null || pd.getTimex().isEmpty()) {
      new Alert(Alert.AlertType.WARNING, "No data to export.").showAndWait();
      return;
    }
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
    java.io.File file = chooser.showSaveDialog(primaryStage);
    if (file == null) return;
    java.nio.file.Path path = file.toPath();
    try {
      CropsterConverter.exportToCropster(pd, appController.getRoastProperties(), path);
    } catch (IOException ex) {
      new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).showAndWait();
    }
  }

  private void doImportFromCropster() {
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
    java.io.File file = chooser.showOpenDialog(primaryStage);
    if (file == null) return;
    java.nio.file.Path path = file.toPath();
    ProfileData pd = CropsterConverter.importFromCropster(path);
    if (pd != null && appController != null) {
      appController.loadSimulatedProfile(pd);
    } else {
      new Alert(Alert.AlertType.WARNING, "Could not import Cropster CSV.").showAndWait();
    }
  }

  private void refreshRecentMenu(Menu recentMenu, MenuItem clearRecentItem, BorderPane root, RoastChartController chartController) {
    recentMenu.getItems().clear();
    for (Path p : fileSession.getRecentFiles()) {
      String name = p.getFileName() != null ? p.getFileName().toString() : p.toString();
      MenuItem mi = new MenuItem(name);
      mi.setOnAction(e -> openRecentFile(p, root, chartController));
      recentMenu.getItems().add(mi);
    }
    if (!recentMenu.getItems().isEmpty()) {
      recentMenu.getItems().add(new SeparatorMenuItem());
    }
    recentMenu.getItems().add(clearRecentItem);
  }

  private void doNew(BorderPane root) {
    if (fileSession.isDirty()) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Unsaved changes");
      alert.setHeaderText("Discard unsaved changes?");
      alert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
      if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
    }
    appController.newRoast();
    updateWindowTitle();
  }

  private void doOpen(BorderPane root, RoastChartController chartController) {
    if (fileSession.isDirty()) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Unsaved changes");
      alert.setHeaderText("Discard unsaved changes?");
      alert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
      if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
    }
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Artisan log (*.alog)", "*.alog"));
    Path initial = fileSession.getCurrentFilePath();
    if (initial != null && initial.getParent() != null && Files.isDirectory(initial.getParent())) {
      chooser.setInitialDirectory(initial.getParent().toFile());
    }
    java.io.File file = chooser.showOpenDialog(primaryStage);
    if (file == null) return;
    Path path = file.toPath();
    ProfileData loaded = Roastlog.load(path);
    if (loaded == null) {
      new Alert(Alert.AlertType.ERROR, "Failed to load file.").showAndWait();
      return;
    }
    appController.loadProfile(path);
    if (appController.getChartController() != null) {
      appController.getChartController().setRoastTitle(loaded.getTitle());
    }
    fileSession.markSaved(path);
    updateWindowTitle();
  }

  /** Syncs curve visibility from UI preferences to display settings so chart and CurveLegendPanel stay in sync. */
  private static void syncDisplaySettingsFromUIPreferences(DisplaySettings displaySettings, UIPreferences uiPreferences) {
    if (displaySettings == null || uiPreferences == null) return;
    displaySettings.setVisibleBT(uiPreferences.isVisibleBT());
    displaySettings.setVisibleET(uiPreferences.isVisibleET());
    displaySettings.setVisibleDeltaBT(uiPreferences.isVisibleDeltaBT());
    displaySettings.setVisibleDeltaET(uiPreferences.isVisibleDeltaET());
  }

  private void openRecentFile(Path path, BorderPane root, RoastChartController chartController) {
    if (fileSession.isDirty()) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Unsaved changes");
      alert.setHeaderText("Discard unsaved changes?");
      alert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
      if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
    }
    if (!Files.isRegularFile(path)) {
      new Alert(Alert.AlertType.WARNING, "File no longer exists: " + path).showAndWait();
      return;
    }
    ProfileData loaded = Roastlog.load(path);
    if (loaded == null) {
      new Alert(Alert.AlertType.ERROR, "Failed to load file.").showAndWait();
      return;
    }
    appController.loadProfile(path);
    if (appController.getChartController() != null) {
      appController.getChartController().setRoastTitle(loaded.getTitle());
    }
    fileSession.markSaved(path);
    updateWindowTitle();
  }

  private void doSave() {
    Path current = fileSession.getCurrentFilePath();
    if (current != null) {
      try {
        appController.saveProfile(current);
        fileSession.markSaved(current);
      } catch (IOException ex) {
        new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage()).showAndWait();
      }
    } else {
      doSaveAs();
    }
  }

  private void doSaveAs() {
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Artisan log (*.alog)", "*.alog"));
    Path initial = fileSession.getCurrentFilePath();
    if (initial != null && initial.getParent() != null && Files.isDirectory(initial.getParent())) {
      chooser.setInitialDirectory(initial.getParent().toFile());
    }
    java.io.File file = chooser.showSaveDialog(primaryStage);
    if (file == null) return;
    Path path = file.toPath();
    String name = path.getFileName().toString();
    if (name.isEmpty() || !name.toLowerCase().endsWith(".alog")) {
      path = path.getParent() != null ? path.getParent().resolve(name + ".alog") : Path.of(name + ".alog");
    }
    try {
      appController.saveProfile(path);
      fileSession.markSaved(path);
      updateWindowTitle();
    } catch (IOException ex) {
      new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage()).showAndWait();
    }
  }

  private void doResetLayout() {
    preferencesStore.resetLayout(uiPreferences);
    preferencesStore.save(uiPreferences);
    if (appShell != null && appShell.getRoastLiveScreen() != null) {
      appShell.getRoastLiveScreen().applyLayoutFromPreferences();
    }
    new Alert(Alert.AlertType.INFORMATION, "Layout reset to defaults.").showAndWait();
  }

  private void doExport(BorderPane root) {
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
    Path initial = fileSession.getCurrentFilePath();
    if (initial != null && initial.getParent() != null && Files.isDirectory(initial.getParent())) {
      chooser.setInitialDirectory(initial.getParent().toFile());
    }
    java.io.File file = chooser.showSaveDialog(primaryStage);
    if (file == null) return;
    Path path = file.toPath();
    CanvasData cd = appController.getSession().getCanvasData();
    List<Double> timex = cd.getTimex();
    List<Double> temp1 = cd.getTemp1();
    List<Double> temp2 = cd.getTemp2();
    List<Double> delta1 = cd.getDelta1();
    List<Double> delta2 = cd.getDelta2();
    int n = timex.size();
    if (n == 0) {
      new Alert(Alert.AlertType.WARNING, "No data to export.").showAndWait();
      return;
    }
    try (BufferedWriter w = Files.newBufferedWriter(path)) {
      w.write("time_sec,ET,BT,DeltaET,DeltaBT");
      w.newLine();
      for (int i = 0; i < n; i++) {
        double t = timex.get(i);
        double et = i < temp1.size() ? temp1.get(i) : 0;
        double bt = i < temp2.size() ? temp2.get(i) : 0;
        double d1 = i < delta1.size() ? delta1.get(i) : 0;
        double d2 = i < delta2.size() ? delta2.get(i) : 0;
        w.write(String.format("%.2f,%.2f,%.2f,%.2f,%.2f", t, et, bt, d1, d2));
        w.newLine();
      }
    } catch (IOException ex) {
      new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).showAndWait();
    }
  }

  private void openAutoSaveDialog(BorderPane root) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    AutoSaveDialog dialog = new AutoSaveDialog(owner, autoSave);
    dialog.showAndWait();
  }

  private void updateWindowTitle() {
    if (primaryStage == null) return;
    String title = "Artisan Java — " + appController.getCurrentFileName();
    String roastTitle = appController.getRoastProperties().getTitle();
    if (roastTitle != null && !roastTitle.isBlank()) title += " — " + roastTitle;
    primaryStage.setTitle(title);
  }

  private static void applyAtlantaFXTheme(boolean dark) {
    try {
      String themeClassName = dark
          ? "io.github.mkpaz.atlantafx.base.theme.PrimerDark"
          : "io.github.mkpaz.atlantafx.base.theme.PrimerLight";
      Class<?> themeClass = Class.forName(themeClassName);
      Object theme = themeClass.getDeclaredConstructor().newInstance();
      String css = (String) themeClass.getMethod("getUserAgentStylesheet").invoke(theme);
      Application.setUserAgentStylesheet(css);
    } catch (Exception e) {
      Application.setUserAgentStylesheet(null);
    }
  }

  private static void syncColorConfigFromDisplaySettings(ColorConfig colorConfig, DisplaySettings ds) {
    if (ds == null) return;
    colorConfig.setPaletteColor("et", ColorConfig.fromHex(ds.getPaletteCurveET()));
    colorConfig.setPaletteColor("bt", ColorConfig.fromHex(ds.getPaletteCurveBT()));
    colorConfig.setPaletteColor("deltaet", ColorConfig.fromHex(ds.getPaletteCurveDeltaET()));
    colorConfig.setPaletteColor("deltabt", ColorConfig.fromHex(ds.getPaletteCurveDeltaBT()));
    colorConfig.setPaletteColor("backgroundmetcolor", ColorConfig.fromHex(ds.getPaletteBackgroundET()));
    colorConfig.setPaletteColor("backgroundbtcolor", ColorConfig.fromHex(ds.getPaletteBackgroundBT()));
    colorConfig.setPaletteColor("backgrounddeltaetcolor", ColorConfig.fromHex(ds.getPaletteBackgroundDeltaET()));
    colorConfig.setPaletteColor("backgrounddeltabtcolor", ColorConfig.fromHex(ds.getPaletteBackgroundDeltaBT()));
    for (String k : new String[] { "background", "canvas", "grid", "title", "ylabel", "xlabel", "text", "markers", "watermarks",
        "timeguide", "aucguide", "aucarea", "legendbg", "legendborder", "rect1", "rect2", "rect3", "rect4", "rect5",
        "specialeventbox", "specialeventtext", "bgeventmarker", "bgeventtext", "metbox", "mettext", "analysismask", "statsanalysisbkgnd" }) {
      colorConfig.setPaletteColor(k, ColorConfig.fromHex(ds.getPalette(k)));
    }
    colorConfig.setPaletteAlpha("legendbg", ds.getAlphaLegendBg());
    colorConfig.setPaletteAlpha("analysismask", ds.getAlphaAnalysismask());
    colorConfig.setPaletteAlpha("statsanalysisbkgnd", ds.getAlphaStatsanalysisbkgnd());
  }

  private void openDeviceSettings(BorderPane root) {
    openDevicesDialog(root);
  }

  private void openDevicesDialog(BorderPane root) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    DevicesDialog dialog = new DevicesDialog(owner, appController,
        serialPortConfig, modbusPortConfig, deviceConfig, simulatorConfig, aillioR1Config);
    dialog.showAndWait();
    if (appShell != null) {
      appShell.setMachineName(deviceConfig.getActiveType() != null ? deviceConfig.getActiveType().getDisplayName() : "—");
      appShell.refreshTopBar();
    }
  }

  private void openS7Dialog(BorderPane root) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    new S7Dialog(owner, s7Config).showAndWait();
  }

  private void openColorsDialog(BorderPane root, RoastChartController chartController) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    ColorsDialog dialog = new ColorsDialog(owner, displaySettings, appController.getColorConfig(), () -> {
      RoastChartController cc = appController.getChartController();
      if (cc != null) {
        cc.applyColors();
        cc.updateChart();
      }
      appController.refreshStatistics();
      if (appShell != null && appShell.getRoastLiveScreen() != null) {
        appShell.getRoastLiveScreen().refreshCurveLegendColors(displaySettings);
      }
    });
    dialog.showAndWait();
  }

  private void openAxesDialog(BorderPane root, RoastChartController chartController) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null || axisConfig == null) return;
    AxesDialog dialog = new AxesDialog(owner, axisConfig, () -> {
      RoastChartController cc = appController.getChartController();
      if (cc != null) {
        cc.applyAxisConfig(axisConfig);
        cc.updateChart();
      }
    });
    dialog.showAndWait();
  }

  private void openSamplingDialog(BorderPane root, RoastChartController chartController) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null || samplingConfig == null) return;
    SamplingDialog dialog = new SamplingDialog(owner, samplingConfig, () -> {
      if (samplingOn) {
        appController.stopSampling();
        appController.startSampling();
      }
      RoastChartController cc = appController.getChartController();
      if (cc != null) cc.updateChart();
    });
    dialog.showAndWait();
  }

  private void openPortsDialog(BorderPane root) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null || commController == null) return;
    PortsDialog dialog = new PortsDialog(owner, serialPortConfig, modbusPortConfig, blePortConfig,
        commController, appController.getSamplingInterval());
    dialog.showAndWait();
    if (appShell != null) appShell.refreshTopBar();
  }

  private void openPhasesDialog(BorderPane root, RoastChartController chartController) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    PhasesDialog dialog = new PhasesDialog(owner, phasesSettings, () -> {
      RoastChartController cc = appController.getChartController();
      if (cc != null) {
        cc.setPhasesConfig(phasesSettings.toConfig());
        cc.updateChart();
      }
    });
    dialog.showAndWait();
  }

  private void rebuildCustomEventButtons() {
    customEventButtonsBox.getChildren().clear();
    for (EventButtonConfig config : EventButtonConfigPersistence.load()) {
      if (!config.isVisible()) continue;
      Button btn = new Button(config.getLabel());
      btn.setTooltip(new javafx.scene.control.Tooltip(config.getDescription()));
      javafx.scene.paint.Color c = config.getColor();
      if (c != null) {
        String hex = String.format("#%02x%02x%02x",
            (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
        btn.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: white;");
      }
      EventButtonConfig cfg = config;
      btn.setOnAction(e -> {
        if (!appController.getSession().isActive()) return;
        var cd = appController.getSession().getCanvasData();
        var timex = cd.getTimex();
        if (timex.isEmpty()) return;
        int idx = timex.size() - 1;
        double bt = idx < cd.getTemp2().size() ? cd.getTemp2().get(idx) : 0.0;
        appController.addCustomEvent(cfg.getType(), cfg.getValue(), idx, bt, cfg.getLabel());
        if (appController.getChartController() != null) appController.getChartController().updateChart();
      });
      customEventButtonsBox.getChildren().add(btn);
    }
  }

  private void openEventButtonsDialog(BorderPane root) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    EventButtonsDialog dialog = new EventButtonsDialog(owner, this::rebuildCustomEventButtons);
    dialog.showAndWait();
  }

  private void openReplayDialog(BorderPane root) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    EventReplayDialog dialog = new EventReplayDialog(owner, appController.getEventReplay());
    dialog.showAndWait();
  }

  private void openPidDialog(BorderPane root) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    PIDDialog dialog = new PIDDialog(owner, appController);
    dialog.showAndWait();
  }

  private void openRoastPropertiesInPanel() {
    if (appShell != null) {
      appShell.switchToRoastLive();
      appShell.getRoastLiveScreen().expandDetailsPanel();
    }
  }

  private void openCupProfileDialog(BorderPane root) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    CupProfileDialog dialog = new CupProfileDialog(owner, appController);
    dialog.showAndWait();
  }

  private void openAlarmsDialog(BorderPane root) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    AlarmsDialog dialog = new AlarmsDialog(owner, appController.getSession().getAlarms(),
        appController.getAlarmEngine(), () -> {
      appController.reloadAlarmsFromFile();
      if (appController.getChartController() != null) appController.getChartController().updateChart();
    });
    dialog.showAndWait();
  }

  private void openBackgroundDialog(BorderPane root, RoastChartController chartController) {
    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
    if (owner == null) return;
    BackgroundDialog dialog = new BackgroundDialog(owner, backgroundSettings, chartController, () -> {
      RoastChartController cc = appController.getChartController();
      if (cc != null) {
        cc.applyColors();
        cc.updateChart();
      }
      appController.refreshStatistics();
    });
    dialog.showAndWait();
  }

  private static void maybeLoadBackgroundProfile(RoastChartController chartController, BackgroundSettings settings) {
    if (chartController == null || settings == null) return;
    if (!settings.isEnabled()) return;
    String pathStr = settings.getLastFilePath();
    if (pathStr == null || pathStr.isBlank()) return;
    ProfileData pd = Roastlog.load(Path.of(pathStr));
    if (pd == null || pd.getTimex() == null || pd.getTimex().isEmpty()) return;
    String title = pd.getTitle();
    if (title == null || title.isBlank()) {
      Path p = Path.of(pathStr);
      title = p.getFileName() != null ? p.getFileName().toString() : "Background";
    }
    BackgroundProfile bg = new BackgroundProfile(pd, title, true, settings.getAlignOffset());
    chartController.setBackgroundProfile(bg);
    chartController.updateChart();
  }

  private void registerAccelerators(Scene scene, BorderPane root, RoastChartController chartController) {
    scene.getAccelerators().put(KeyCombination.keyCombination("Ctrl+N"), () -> doNew(root));
    scene.getAccelerators().put(KeyCombination.keyCombination("Ctrl+O"), () -> doOpen(root, chartController));
    scene.getAccelerators().put(KeyCombination.keyCombination("Ctrl+S"), this::doSave);
    scene.getAccelerators().put(KeyCombination.keyCombination("Ctrl+Shift+S"), this::doSaveAs);
    scene.getAccelerators().put(KeyCombination.keyCombination("Ctrl+Z"), () ->
        Logger.getLogger(MainWindow.class.getName()).info("Undo not yet implemented"));
    scene.getAccelerators().put(KeyCombination.keyCombination("F5"), this::toggleSampling);
    scene.getAccelerators().put(KeyCombination.keyCombination("Ctrl+P"), () -> openPidDialog(root));
    scene.getAccelerators().put(KeyCombination.keyCombination("Ctrl+D"), () -> openDevicesDialog(root));
    scene.getAccelerators().put(KeyCombination.keyCombination("Ctrl+Shift+R"), this::doResetLayout);
  }

  private void startRecording() {
    if (appController != null) appController.startSampling();
    samplingOn = true;
  }

  private void stopRecording() {
    if (appController != null) appController.stopSampling();
    samplingOn = false;
  }

  private void setTimerPreRoastMode() {
    if (appShell != null) appShell.setTimerPreRoastMode();
  }

  private void setTimerRoastingMode() {
    if (appShell != null) appShell.setTimerRoastingMode();
  }

  private void handleAutoCharge() {
    if (canvasData == null) return;
    java.util.List<Double> timex = canvasData.getTimex();
    int chargeIdx = timex.size() - 1;
    if (chargeIdx >= 0) {
      canvasData.setChargeIndex(chargeIdx);
      if (chartController != null) chartController.resetLiveRor();
    }
    setTimerRoastingMode();
    if (appController != null) {
      appController.notifyUser("CHARGE detected", NotificationLevel.INFO);
    }
    if (chartController != null) chartController.markDirty();
  }

  private void handlePreRoastTimeout() {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
        javafx.scene.control.Alert.AlertType.CONFIRMATION);
    alert.setTitle("Pre-start Timeout");
    alert.setHeaderText("5 minutes elapsed without CHARGE");
    alert.setContentText("Stop recording and reset timer?");
    alert.showAndWait().ifPresent(btn -> {
      if (btn == javafx.scene.control.ButtonType.OK) {
        stopRecording();
        roastStateMachine.reset();
        if (canvasData != null) canvasData.clear();
        if (chartController != null) chartController.updateChart();
      }
    });
  }

  private void showPhaseContextMenu(RoastOverlayCanvas.ChartRightClickInfo info) {
    if (canvasData == null || chartController == null) return;
    javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();
    int idx = info.timeIndex();

    addPhaseMenuItem(menu, "CHARGE",   canvasData.getChargeIndex(),  idx,
        () -> { canvasData.setChargeIndex(idx);  chartController.markDirty(); });
    addPhaseMenuItem(menu, "DRY END",  canvasData.getDryEndIndex(),  idx,
        () -> { canvasData.setDryEndIndex(idx);  chartController.markDirty(); });
    addPhaseMenuItem(menu, "FC START", canvasData.getFcStartIndex(), idx,
        () -> { canvasData.setFcStartIndex(idx); chartController.markDirty(); });
    addPhaseMenuItem(menu, "FC END",   canvasData.getFcEndIndex(),   idx,
        () -> { canvasData.setFcEndIndex(idx);   chartController.markDirty(); });
    addPhaseMenuItem(menu, "SC START", canvasData.getScStartIndex(), idx,
        () -> { canvasData.setScStartIndex(idx); chartController.markDirty(); });
    addPhaseMenuItem(menu, "SC END",   canvasData.getScEndIndex(),   idx,
        () -> { canvasData.setScEndIndex(idx);   chartController.markDirty(); });
    addPhaseMenuItem(menu, "DROP",     canvasData.getDropIndex(),    idx,
        () -> { canvasData.setDropIndex(idx);    chartController.markDirty(); });

    menu.getItems().add(new javafx.scene.control.SeparatorMenuItem());

    javafx.scene.control.MenuItem removeItem =
        new javafx.scene.control.MenuItem("Remove nearest event");
    removeItem.setOnAction(e -> removeNearestEvent(idx));
    menu.getItems().add(removeItem);

    menu.show(chartController.getView().getScene().getWindow(),
              info.screenX(), info.screenY());
  }

  private void addPhaseMenuItem(javafx.scene.control.ContextMenu menu,
                                 String label, int currentIdx, int clickIdx,
                                 Runnable action) {
    String text = (currentIdx == clickIdx) ? "✓ " + label : label;
    javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem(text);
    item.setOnAction(e -> action.run());
    menu.getItems().add(item);
  }

  private void removeNearestEvent(int idx) {
    if (canvasData == null) return;
    int[] vals = {
        canvasData.getChargeIndex(), canvasData.getDryEndIndex(),
        canvasData.getFcStartIndex(), canvasData.getFcEndIndex(),
        canvasData.getScStartIndex(), canvasData.getScEndIndex(),
        canvasData.getDropIndex()
    };
    int bestSlot = -1, bestDist = Integer.MAX_VALUE;
    for (int i = 0; i < vals.length; i++) {
      if (vals[i] >= 0) {
        int d = Math.abs(vals[i] - idx);
        if (d < bestDist) { bestDist = d; bestSlot = i; }
      }
    }
    switch (bestSlot) {
      case 0 -> canvasData.setChargeIndex(-1);
      case 1 -> canvasData.setDryEndIndex(-1);
      case 2 -> canvasData.setFcStartIndex(-1);
      case 3 -> canvasData.setFcEndIndex(-1);
      case 4 -> canvasData.setScStartIndex(-1);
      case 5 -> canvasData.setScEndIndex(-1);
      case 6 -> canvasData.setDropIndex(-1);
      default -> {}
    }
    if (bestSlot >= 0 && chartController != null) chartController.markDirty();
  }

  private void toggleSampling() {
    appController.toggleSampling();
    samplingOn = (appController.getCommController() != null && appController.getCommController().isRunning())
        || (appController.getSampling() != null && appController.getSampling().isRunning());
    if (appController.getChartController() != null) {
      appController.getChartController().setLiveRecording(samplingOn);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
