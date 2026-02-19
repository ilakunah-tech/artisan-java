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
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
import org.artisan.model.ProfileData;
import org.artisan.model.CanvasData;
import org.artisan.model.Roastlog;
import org.artisan.model.Sampling;
import org.artisan.model.SamplingConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
  private PhasesLCD phasesLCD;
  private StatisticsPanel statisticsPanel;
  private boolean samplingOn;
  private HBox customEventButtonsBox;
  private AxisConfig axisConfig;
  private SamplingConfig samplingConfig;
  private SerialPortConfig serialPortConfig;
  private ModbusPortConfig modbusPortConfig;
  private BlePortConfig blePortConfig;
  private DeviceConfig deviceConfig;
  private SimulatorConfig simulatorConfig;
  private AillioR1Config aillioR1Config;
  private CommController commController;

  @Override
  public void start(Stage primaryStage) {
    appSettings = AppSettings.load();
    displaySettings = DisplaySettings.load();
    phasesSettings = PhasesSettings.load();
    backgroundSettings = BackgroundSettings.load();
    applyAtlantaFXTheme(appSettings.isDarkTheme());

    fileSession = new FileSession();
    autoSave = new AutoSave();
    autoSave.load();

    RoastSession session = new RoastSession();
    ArtisanTime timeclock = new ArtisanTime();
    Sampling sampling = new Sampling(timeclock);
    DevicePort device = new StubDevice();
    ColorConfig colorConfig = new ColorConfig(appSettings.isDarkTheme()
        ? ColorConfig.Theme.DARK : ColorConfig.Theme.LIGHT);
    syncColorConfigFromDisplaySettings(colorConfig, displaySettings);
    axisConfig = new AxisConfig();
    AxisConfig.loadFromPreferences(axisConfig);
    samplingConfig = new SamplingConfig();
    SamplingConfig.loadFromPreferences(samplingConfig);
    RoastChartController chartController = new RoastChartController(
        session.getCanvasData(), colorConfig, axisConfig, displaySettings);
    chartController.setPhasesConfig(phasesSettings.toConfig());
    chartController.setBackgroundSettings(backgroundSettings);
    chartController.setEventList(session.getEvents());
    chartController.setOnEventMoved(() -> fileSession.markDirty());
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
      if (appController.getChartController() != null) {
        appController.getChartController().onSample(s.timeSec(), s.bt(), s.et());
      }
      appController.afterSample(s);
    }));

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

    HBox toolbarRow1 = new HBox(10);
    Button onOff = new Button("ON");
    onOff.setOnAction(e -> toggleSampling());
    Button charge = new Button("CHARGE");
    charge.setOnAction(e -> appController.onChargeButton());
    Button dryEnd = new Button("DRY END");
    dryEnd.setOnAction(e -> appController.onDryEndButton());
    Button fcStart = new Button("FC START");
    fcStart.setOnAction(e -> appController.onFcStartButton());
    Button fcEnd = new Button("FC END");
    fcEnd.setOnAction(e -> appController.onFcEndButton());
    Button drop = new Button("DROP");
    drop.setOnAction(e -> appController.onDropButton());
    Button coolEnd = new Button("COOL END");
    coolEnd.setOnAction(e -> appController.onCoolEndButton());
    Button deviceBtn = new Button("\u2699 Device");
    deviceBtn.setOnAction(e -> openDeviceSettings(root));
    Button colorsBtn = new Button("Colors");
    colorsBtn.setOnAction(e -> openColorsDialog(root, chartController));
    toolbarRow1.getChildren().addAll(onOff, charge, dryEnd, fcStart, fcEnd, drop, coolEnd, deviceBtn, colorsBtn);

    customEventButtonsBox = new HBox(6);
    rebuildCustomEventButtons();
    VBox toolbar = new VBox(4, toolbarRow1, customEventButtonsBox);

    primaryStage = primaryStage;

    MenuBar menuBar = new MenuBar();
    Menu fileMenu = buildFileMenu(root, chartController);
    Menu viewMenu = buildViewMenu(root, chartController);
    Menu configMenu = new Menu("Config");
    MenuItem axesItem = new MenuItem("Axes...");
    axesItem.setOnAction(e -> openAxesDialog(root, chartController));
    MenuItem samplingItem = new MenuItem("Sampling...");
    samplingItem.setOnAction(e -> openSamplingDialog(root, chartController));
    MenuItem portsItem = new MenuItem("Ports...");
    portsItem.setOnAction(e -> openPortsDialog(root));
    MenuItem deviceItem = new MenuItem("Device...");
    deviceItem.setOnAction(e -> openDevicesDialog(root));
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
    configMenu.getItems().addAll(axesItem, samplingItem, portsItem, deviceItem, new SeparatorMenuItem(), phasesItem, backgroundItem, new SeparatorMenuItem(), eventsItem, alarmsItem, autosaveItem, replayItem);
    menuBar.getMenus().addAll(fileMenu, viewMenu, configMenu);

    phasesLCD = new PhasesLCD(phasesSettings);

    Node chartView = chartController.getView();
    chartController.startUpdateTimer();

    statusBar = new Label("BT: —  ET: —  RoR: —");
    elapsedLabel = new Label("Elapsed: 0 s");
    HBox status = new HBox(20, statusBar, elapsedLabel);

    AnimationTimer statusTimer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        if (appController.getSession().getCanvasData().getTimex().isEmpty()) {
          statusBar.setText("BT: —  ET: —  RoR: —");
        } else {
          var tx = appController.getSession().getCanvasData().getTimex();
          var t1 = appController.getSession().getCanvasData().getTemp1();
          var t2 = appController.getSession().getCanvasData().getTemp2();
          var d1 = appController.getSession().getCanvasData().getDelta1();
          int i = tx.size() - 1;
          double bt = i < t2.size() ? t2.get(i) : 0;
          double et = i < t1.size() ? t1.get(i) : 0;
          double ror = i < d1.size() ? d1.get(i) : 0;
          String line = String.format("BT: %.1f  ET: %.1f  RoR: %.1f", bt, et, ror);
          if (samplingOn && appController.getCommController() != null && appController.getCommController().getActiveChannel() != null) {
            line = appController.getCommController().getActiveChannel().getDescription() + "  |  " + line;
          }
          statusBar.setText(line);
        }
        if (samplingOn) {
          double elapsed;
          if (appController.getCommController() != null && appController.getCommController().isRunning()) {
            elapsed = appController.getCommController().getElapsedMs() / 1000.0;
          } else {
            elapsed = appController.getSampling().getElapsedMs() / 1000.0;
          }
          elapsedLabel.setText(String.format("Elapsed: %.0f s", elapsed));
        }
        phasesLCD.update(appController.getSession().getCanvasData(), phasesSettings.toConfig());
      }
    };
    statusTimer.start();

    root.setTop(new VBox(menuBar, toolbar, phasesLCD));
    root.setCenter(chartView);
    root.setBottom(new VBox(statisticsPanel, status));

    appController.refreshStatistics();

    Scene scene = new Scene(root, 1000, 600);
    primaryStage.setScene(scene);
    updateWindowTitle();
    primaryStage.setOnCloseRequest(e -> {
      if (fileSession.isDirty()) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved changes");
        alert.setHeaderText("You have unsaved changes. Save before quitting?");
        alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES, ButtonType.CANCEL);
        alert.getButtonTypes().stream()
            .filter(b -> b == ButtonType.YES)
            .findFirst()
            .ifPresent(b -> ((Button) alert.getDialogPane().lookupButton(b)).setText("Save"));
        alert.getButtonTypes().stream()
            .filter(b -> b == ButtonType.NO)
            .findFirst()
            .ifPresent(b -> ((Button) alert.getDialogPane().lookupButton(b)).setText("Discard"));
        ButtonType choice = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (choice == ButtonType.CANCEL) {
          e.consume();
          return;
        }
        if (choice == ButtonType.YES) {
          doSave();
        }
      }
      appController.stopSampling();
      autoSave.stop();
      appSettings.save();
      Platform.exit();
    });
    primaryStage.show();
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
    MenuItem quitItem = new MenuItem("Quit");
    quitItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+Q"));
    quitItem.setOnAction(e -> primaryStage.getOnCloseRequest().handle(new javafx.stage.WindowEvent(primaryStage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST)));

    fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem,
        new SeparatorMenuItem(), recentMenu, new SeparatorMenuItem(), exportItem, new SeparatorMenuItem(), quitItem);
    refreshRecentMenu(recentMenu, clearRecentItem, root, chartController);
    return fileMenu;
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
    appController.getSession().reset();
    if (appController.getChartController() != null) {
      appController.getChartController().setRoastTitle(null);
      appController.getChartController().updateChart();
    }
    appController.refreshStatistics();
    fileSession.markNew();
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
    String base = "Artisan Java";
    Path p = fileSession.getCurrentFilePath();
    String name = p != null && p.getFileName() != null ? p.getFileName().toString() : "New Roast";
    String title = base + " — " + name;
    if (fileSession.isDirty()) title += " *";
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

  private void toggleSampling() {
    samplingOn = !samplingOn;
    if (appController.getChartController() != null) {
      appController.getChartController().setLiveRecording(samplingOn);
    }
    if (samplingOn) {
      appController.startSampling();
    } else {
      appController.stopSampling();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
