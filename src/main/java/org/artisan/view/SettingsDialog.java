package org.artisan.view;

import com.fazecast.jSerialComm.SerialPort;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import org.artisan.controller.AppController;
import org.artisan.controller.AppSettings;
import org.artisan.controller.AutoSave;
import org.artisan.controller.BackgroundSettings;
import org.artisan.controller.CommController;
import org.artisan.controller.DisplaySettings;
import org.artisan.controller.PhasesSettings;
import org.artisan.controller.RoastStateMachine;
import org.artisan.model.AxisConfig;
import org.artisan.model.ColorConfig;
import org.artisan.model.SamplingConfig;
import org.artisan.ui.state.ChartAppearance;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SettingsDialog extends Dialog<ButtonType> {

    private static final String SECTION_STYLE = "-fx-font-weight:bold; -fx-font-size:12px; -fx-padding:12 0 4 0;";
    private static final String DELTA = "\u0394";

    private final AppSettings appSettings;
    private final DisplaySettings displaySettings;
    private final AxisConfig axisConfig;
    private final RoastStateMachine roastStateMachine;
    private final UIPreferences uiPreferences;
    private final PreferencesStore preferencesStore;

    // Device tab
    private ComboBox<String> deviceTypeCombo;
    private ComboBox<String> serialPortCombo;
    private ComboBox<Integer> baudRateCombo;
    private Spinner<Integer> samplingRateSpinner;
    private TextField tcpHostField;
    private Spinner<Integer> tcpPortSpinner;
    private Spinner<Integer> modbusSlaveIdSpinner;
    private Spinner<Integer> modbusBtRegisterSpinner;
    private Spinner<Integer> modbusEtRegisterSpinner;
    private Spinner<Double> modbusScaleFactorSpinner;
    private Label serialPortLabel;
    private Label baudRateLabel;
    private Label tcpHostLabel;
    private Label tcpPortLabel;
    private Label modbusSlaveLabel;
    private Label modbusBtLabel;
    private Label modbusEtLabel;
    private Label modbusScaleLabel;

    // Graph tab
    private RadioButton unitCelsiusRadio;
    private RadioButton unitFahrenheitRadio;
    private RadioButton themeDarkRadio;
    private RadioButton themeLightRadio;
    private CheckBox showCrosshairCheck;
    private CheckBox showWatermarkCheck;
    private CheckBox showLegendCheck;
    private Spinner<Double> timeGuideSpinner;
    private Spinner<Double> aucBaseSpinner;
    private CheckBox autoScaleYCheck;
    private Spinner<Double> tempMinSpinner;
    private Spinner<Double> tempMaxSpinner;
    private Spinner<Double> autoScaleFloorSpinner;
    private Spinner<Double> rorMinSpinner;
    private Spinner<Double> rorMaxSpinner;

    // Curves tab
    private ColorPicker curveEtPicker;
    private ColorPicker curveBtPicker;
    private ColorPicker curveDeltaEtPicker;
    private ColorPicker curveDeltaBtPicker;
    private Spinner<Integer> lineWidthEtSpinner;
    private Spinner<Integer> lineWidthBtSpinner;
    private Spinner<Integer> lineWidthDeltaEtSpinner;
    private Spinner<Integer> lineWidthDeltaBtSpinner;
    private Spinner<Integer> smoothingBtSpinner;
    private Spinner<Integer> smoothingEtSpinner;
    private Spinner<Integer> smoothingDeltaSpinner;
    private CheckBox visibleEtCheck;
    private CheckBox visibleBtCheck;
    private CheckBox visibleDeltaEtCheck;
    private CheckBox visibleDeltaBtCheck;
    private Slider backgroundAlphaSlider;
    private Label backgroundAlphaValueLabel;

    // Phases & Roast tab
    private Spinner<Double> autoChargeDropSpinner;
    private Spinner<Double> autoChargeSustainSpinner;
    private Spinner<Double> preRoastTimeoutSpinner;

    // Colors tab
    private final Map<String, ColorPicker> palettePickers = new LinkedHashMap<>();

    // Chart Appearance tab
    private ColorPicker apBtColorPicker;
    private ColorPicker apEtColorPicker;
    private ColorPicker apRorBtColorPicker;
    private ColorPicker apRorEtColorPicker;
    private ColorPicker apGasColorPicker;
    private ColorPicker apDrumColorPicker;
    private ColorPicker apEventLineColorPicker;
    private ColorPicker apEventDotColorPicker;
    private ColorPicker apGridColorPicker;
    private ColorPicker apBgMainColorPicker;
    private ColorPicker apBgBottomColorPicker;
    private ColorPicker apAxisFontColorPicker;
    private ColorPicker apAnnotationBgPicker;
    private ColorPicker apAnnotationTextPicker;
    private ColorPicker apReadoutBtPicker;
    private ColorPicker apReadoutEtPicker;
    private ColorPicker apReadoutRorBtPicker;
    private ColorPicker apReadoutRorEtPicker;
    private Spinner<Double> apBtWidthSpinner;
    private Spinner<Double> apEtWidthSpinner;
    private Spinner<Double> apRorBtWidthSpinner;
    private Spinner<Double> apRorEtWidthSpinner;
    private Spinner<Double> apGasWidthSpinner;
    private Spinner<Double> apDrumWidthSpinner;
    private ComboBox<ChartAppearance.LineStyle> apBtStyleCombo;
    private ComboBox<ChartAppearance.LineStyle> apEtStyleCombo;
    private ComboBox<ChartAppearance.LineStyle> apRorBtStyleCombo;
    private ComboBox<ChartAppearance.LineStyle> apRorEtStyleCombo;
    private ComboBox<ChartAppearance.LineStyle> apGasStyleCombo;
    private ComboBox<ChartAppearance.LineStyle> apDrumStyleCombo;
    private Spinner<Double> apGasFillOpacitySpinner;
    private Spinner<Double> apGridOpacitySpinner;
    private TextField apAxisFontField;
    private Spinner<Double> apAxisFontSizeSpinner;
    private Spinner<Double> apAnnotationFontSizeSpinner;
    private ComboBox<ChartAppearance.LegendPosition> apLegendPositionCombo;
    private Spinner<Double> apReadoutMainSizeSpinner;
    private Spinner<Double> apReadoutSecondarySizeSpinner;
    private ComboBox<String> apPresetCombo;

    /** When non-null, this dialog is in "unified" mode: tabs are embedded dialogs and apply runs their applyFromUI(). */
    private final SettingsContext unifiedContext;
    private final List<Runnable> embeddedApplyActions = new ArrayList<>();

    public SettingsDialog(AppSettings appSettings,
                          DisplaySettings displaySettings,
                          AxisConfig axisConfig,
                          RoastStateMachine roastStateMachine,
                          Window owner) {
        this.unifiedContext = null;
        this.appSettings = appSettings != null ? appSettings : new AppSettings();
        this.displaySettings = displaySettings != null ? displaySettings : DisplaySettings.load();
        this.axisConfig = axisConfig != null ? axisConfig : new AxisConfig();
        this.roastStateMachine = roastStateMachine;
        this.preferencesStore = new PreferencesStore();
        this.uiPreferences = preferencesStore.load();

        if (owner != null) initOwner(owner);
        setTitle("Settings");
        setResizable(true);
        getDialogPane().setPrefSize(700, 560);
        getDialogPane().addEventHandler(ScrollEvent.ANY, Event::consume);
        applyDialogStyles();

        TabPane tabs = new TabPane();
        tabs.getTabs().add(buildDeviceTab());
        tabs.getTabs().add(buildGraphTab());
        tabs.getTabs().add(buildCurvesTab());
        tabs.getTabs().add(buildChartAppearanceTab());
        if (roastStateMachine != null) {
            tabs.getTabs().add(buildRoastTab());
        }
        tabs.getTabs().add(buildColorsTab());
        getDialogPane().setContent(tabs);

        ButtonType restoreDefaultsType = new ButtonType("Restore Defaults", ButtonBar.ButtonData.LEFT);
        getDialogPane().getButtonTypes().addAll(restoreDefaultsType, ButtonType.APPLY, ButtonType.OK, ButtonType.CANCEL);

        Button restoreBtn = (Button) getDialogPane().lookupButton(restoreDefaultsType);
        if (restoreBtn != null) {
            restoreBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
                e.consume();
                restoreAllDefaults();
            });
        }

        setResultConverter(btn -> {
            if (btn == ButtonType.OK || btn == ButtonType.APPLY) {
                applyChanges();
            }
            return btn;
        });
    }

    /**
     * Unified settings dialog: one window with tabs matching Python order (Device, Ports, Curves, Events, Colors, Phases, …).
     * Each tab embeds the corresponding dialog content; OK/Apply calls applyFromUI() on each and then onSettingsApplied.
     */
    public SettingsDialog(SettingsContext ctx) {
        this.unifiedContext = ctx;
        this.appSettings = ctx.getAppSettings() != null ? ctx.getAppSettings() : new AppSettings();
        this.displaySettings = ctx.getDisplaySettings() != null ? ctx.getDisplaySettings() : DisplaySettings.load();
        this.axisConfig = ctx.getAxisConfig() != null ? ctx.getAxisConfig() : new AxisConfig();
        this.roastStateMachine = ctx.getRoastStateMachine();
        this.preferencesStore = ctx.getPreferencesStore() != null ? ctx.getPreferencesStore() : new PreferencesStore();
        this.uiPreferences = ctx.getUiPreferences() != null ? ctx.getUiPreferences() : preferencesStore.load();

        Window owner = ctx.getOwner();
        if (owner != null) initOwner(owner);
        setTitle("Settings");
        setResizable(true);
        getDialogPane().setPrefSize(780, 600);
        getDialogPane().addEventHandler(ScrollEvent.ANY, Event::consume);
        applyDialogStyles();

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        // Device
        DevicesDialog devicesDialog = new DevicesDialog(owner, ctx.getAppController(),
            ctx.getSerialPortConfig(), ctx.getModbusPortConfig(), ctx.getDeviceConfig(),
            ctx.getSimulatorConfig(), ctx.getAillioR1Config());
        embeddedApplyActions.add(devicesDialog::applyFromUI);

        // Ports
        Node portsNode = new Label("Ports are not available (no CommController).");
        if (ctx.getCommController() != null) {
            PortsDialog portsDialog = new PortsDialog(owner, ctx.getSerialPortConfig(), ctx.getModbusPortConfig(),
                ctx.getBlePortConfig(), ctx.getCommController(),
                ctx.getAppController() != null ? ctx.getAppController().getSamplingInterval() : 1.0);
            portsNode = portsDialog.getContentForEmbedding();
        // Graph (unit, theme, axis) – inline
        tabs.getTabs().add(buildGraphTab());
        // Curves – inline with sub-tabs (Line/visibility + RoR & Filters placeholder)
        Tab curveMainTab = buildCurvesTab();
        Tab lineTab = new Tab("Line & visibility", curveMainTab.getContent());
        javafx.scene.control.Label rorPlaceholder = new javafx.scene.control.Label(
            "RoR and filter settings (smoothing, RoR computation) – to be ported from Python Curves tab.");
        rorPlaceholder.setWrapText(true);
        Tab rorTab = new Tab("RoR & Filters", new ScrollPane(rorPlaceholder));
        TabPane curvesSub = new TabPane(lineTab, rorTab);
        curvesSub.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Curves", curvesSub));

        // Chart Appearance (RI5)
        // Roast (auto charge, timeout)
        if (roastStateMachine != null) {
            tabs.getTabs().add(buildRoastTab());
        }

            embeddedApplyActions.add(portsDialog::applyFromUI);
        }

        // Events
        EventButtonsDialog eventButtonsDialog = new EventButtonsDialog(owner, () -> {});
        embeddedApplyActions.add(eventButtonsDialog::applyFromUI);

        // Colors
        ColorsDialog colorsDialog = new ColorsDialog(owner, displaySettings, ctx.getColorConfig(), () -> {});
        embeddedApplyActions.add(colorsDialog::applyFromUI);

        // Phases
        PhasesDialog phasesDialog = new PhasesDialog(owner, ctx.getPhasesSettings(), () -> {});
        embeddedApplyActions.add(() -> phasesDialog.applyFromUI());

        // Axes
        AxesDialog axesDialog = new AxesDialog(owner, axisConfig, () -> {});
        embeddedApplyActions.add(axesDialog::applyFromUI);

        // Grouped tabs (fewer top-level tabs)
        VBox devicePortsBox = new VBox(14,
            sectionBox("Device", devicesDialog.getContentForEmbedding()),
            sectionBox("Ports", portsNode)
        );
        tabs.getTabs().add(new Tab("Device & Ports", wrap(devicePortsBox)));

        VBox appearanceBox = new VBox(14,
            sectionBox("Display & Axes", contentFromTab(buildGraphTab())),
            sectionBox("Curves (lines & visibility)", contentFromTab(buildCurvesTab())),
            sectionBox("Chart Appearance", contentFromTab(buildChartAppearanceTab())),
            sectionBox("Colors", colorsDialog.getContentForEmbedding())
        );
        tabs.getTabs().add(new Tab("Appearance", wrap(appearanceBox)));

        tabs.getTabs().add(new Tab("Events", wrap(eventButtonsDialog.getContentForEmbedding())));

        VBox roastBox = new VBox(14,
            sectionBox("Phases", phasesDialog.getContentForEmbedding())
        );
        if (roastStateMachine != null) {
            roastBox.getChildren().add(sectionBox("Roast (Auto-CHARGE)", contentFromTab(buildRoastTab())));
        }
        if (ctx.getSamplingConfig() != null) {
            SamplingDialog samplingDialog = new SamplingDialog(owner, ctx.getSamplingConfig(), () -> {});
            embeddedApplyActions.add(samplingDialog::applyFromUI);
            roastBox.getChildren().add(sectionBox("Sampling", samplingDialog.getContentForEmbedding()));
        }
        tabs.getTabs().add(new Tab("Roast & Phases", wrap(roastBox)));

        VBox systemBox = new VBox(14);
        if (ctx.getAutoSave() != null) {
            AutoSaveDialog autoSaveDialog = new AutoSaveDialog(owner, ctx.getAutoSave());
            embeddedApplyActions.add(autoSaveDialog::applyFromUI);
            systemBox.getChildren().add(sectionBox("Autosave", autoSaveDialog.getContentForEmbedding()));
        }
        if (ctx.getAppController() != null) {
            BatchesDialog batchesDialog = new BatchesDialog(owner, ctx.getAppController());
            embeddedApplyActions.add(() -> batchesDialog.applyFromUI());
            systemBox.getChildren().add(sectionBox("Batch", batchesDialog.getContentForEmbedding()));
        }
        systemBox.getChildren().add(sectionBox("Axes", axesDialog.getContentForEmbedding()));
        if (ctx.getAppController() != null) {
            AlarmsDialog alarmsDialog = new AlarmsDialog(owner, ctx.getAppController().getSession().getAlarms(),
                ctx.getAppController().getAlarmEngine(), () -> {});
            embeddedApplyActions.add(() -> alarmsDialog.applyFromUI());
            systemBox.getChildren().add(sectionBox("Alarms", alarmsDialog.getContentForEmbedding()));
        }
        if (ctx.getChartController() != null && ctx.getBackgroundSettings() != null) {
            BackgroundDialog backgroundDialog = new BackgroundDialog(owner, ctx.getBackgroundSettings(),
                ctx.getChartController(), () -> {});
            embeddedApplyActions.add(backgroundDialog::applyFromUI);
            systemBox.getChildren().add(sectionBox("Background", backgroundDialog.getContentForEmbedding()));
        }
        // Import – placeholder (actual import is via File menu)
        VBox importBox = new VBox(10);
        importBox.setPadding(new Insets(0));
        javafx.scene.control.Label importHeading = new javafx.scene.control.Label("Supported import formats (File menu):");
        importHeading.setStyle(SECTION_STYLE);
        String importText = "• Artisan (.alog)\n• Cropster CSV\n• Giesen\n• Roest\n• Loring\n• Petroncini\n• Stronghold\n• HiBean\n• Rubasse";
        javafx.scene.control.Label importList = new javafx.scene.control.Label(importText);
        importList.setWrapText(true);
        importBox.getChildren().addAll(importHeading, importList);
        systemBox.getChildren().add(sectionBox("Import", importBox));
        tabs.getTabs().add(new Tab("System", wrap(systemBox)));

        getDialogPane().setContent(tabs);
        getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(btn -> {
            if (btn == ButtonType.OK || btn == ButtonType.APPLY) {
                applyChanges();
            }
            return btn;
        });
    }

    private static Tab tab(String title, Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        return new Tab(title, scroll);
    }

    private static Node contentFromTab(Tab tab) {
        return tab != null ? tab.getContent() : new Label("Missing content.");
    }

    private static VBox sectionBox(String title, Node content) {
        Label label = new Label(title);
        label.setStyle(SECTION_STYLE);
        VBox box = new VBox(8, label, content);
        box.setPadding(new Insets(4, 0, 0, 0));
        return box;
    }

    private static ScrollPane wrap(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        return scroll;
    }

    private void applyDialogStyles() {
        getDialogPane().getStyleClass().add("artisan-dialog-root");
        try {
            getDialogPane().getStylesheets().add(
                SettingsDialog.class.getResource("/org/artisan/ui/theme/tokens.css").toExternalForm());
            getDialogPane().getStylesheets().add(
                SettingsDialog.class.getResource("/org/artisan/ui/theme/light-brand.css").toExternalForm());
        } catch (Exception ignored) {}
    }

    private Tab buildDeviceTab() {
        GridPane grid = buildGrid();
        int row = 0;

        grid.add(sectionLabel("Device"), 0, row++, 2, 1);

        deviceTypeCombo = new ComboBox<>();
        deviceTypeCombo.getItems().addAll("Simulator", "Modbus TCP", "Modbus RTU", "POLLER");
        if (!deviceTypeCombo.getItems().contains(appSettings.getDeviceType())) {
            deviceTypeCombo.getItems().add(appSettings.getDeviceType());
        }
        deviceTypeCombo.setValue(appSettings.getDeviceType());
        deviceTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDeviceVisibility(newVal));

        serialPortLabel = new Label("Serial Port");
        serialPortCombo = new ComboBox<>();
        serialPortCombo.setEditable(true);
        List<String> ports = Arrays.stream(SerialPort.getCommPorts())
            .map(SerialPort::getSystemPortName)
            .filter(p -> p != null && !p.isEmpty())
            .distinct()
            .toList();
        serialPortCombo.getItems().addAll(ports);
        if (!serialPortCombo.getItems().contains(appSettings.getLastDevicePort())) {
            serialPortCombo.getItems().add(appSettings.getLastDevicePort());
        }
        serialPortCombo.setValue(appSettings.getLastDevicePort());

        baudRateLabel = new Label("Baud Rate");
        baudRateCombo = new ComboBox<>();
        baudRateCombo.getItems().addAll(1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200);
        if (!baudRateCombo.getItems().contains(appSettings.getBaudRate())) {
            baudRateCombo.getItems().add(appSettings.getBaudRate());
        }
        baudRateCombo.setValue(appSettings.getBaudRate());

        samplingRateSpinner = intSpinner(500, 10000, appSettings.getSamplingRateMs(), 100);
        tcpHostLabel = new Label("TCP Host");
        tcpHostField = new TextField(appSettings.getTcpHost());
        tcpPortLabel = new Label("TCP Port");
        tcpPortSpinner = intSpinner(1, 65535, appSettings.getDeviceTcpPort(), 1);

        modbusSlaveLabel = new Label("Modbus Slave ID");
        modbusSlaveIdSpinner = intSpinner(1, 247, appSettings.getModbusSlaveId(), 1);
        modbusBtLabel = new Label("BT Register");
        modbusBtRegisterSpinner = intSpinner(0, 65535, appSettings.getModbusBtRegister(), 1);
        modbusEtLabel = new Label("ET Register");
        modbusEtRegisterSpinner = intSpinner(0, 65535, appSettings.getModbusEtRegister(), 1);
        modbusScaleLabel = new Label("Scale Factor");
        modbusScaleFactorSpinner = doubleSpinner(0.01, 1000.0, appSettings.getModbusScaleFactor(), 0.1);

        grid.add(new Label("Device Type"), 0, row);
        grid.add(deviceTypeCombo, 1, row++);
        grid.add(serialPortLabel, 0, row);
        grid.add(serialPortCombo, 1, row++);
        grid.add(baudRateLabel, 0, row);
        grid.add(baudRateCombo, 1, row++);
        grid.add(new Label("Sampling Rate (ms)"), 0, row);
        grid.add(samplingRateSpinner, 1, row++);
        grid.add(tcpHostLabel, 0, row);
        grid.add(tcpHostField, 1, row++);
        grid.add(tcpPortLabel, 0, row);
        grid.add(tcpPortSpinner, 1, row++);
        grid.add(modbusSlaveLabel, 0, row);
        grid.add(modbusSlaveIdSpinner, 1, row++);
        grid.add(modbusBtLabel, 0, row);
        grid.add(modbusBtRegisterSpinner, 1, row++);
        grid.add(modbusEtLabel, 0, row);
        grid.add(modbusEtRegisterSpinner, 1, row++);
        grid.add(modbusScaleLabel, 0, row);
        grid.add(modbusScaleFactorSpinner, 1, row++);

        updateDeviceVisibility(deviceTypeCombo.getValue());
        return new Tab("Device", grid);
    }

    private Tab buildGraphTab() {
        GridPane grid = buildGrid();
        int row = 0;

        grid.add(sectionLabel("Display"), 0, row++, 2, 1);

        ToggleGroup unitGroup = new ToggleGroup();
        unitCelsiusRadio = new RadioButton("°C");
        unitFahrenheitRadio = new RadioButton("°F");
        unitCelsiusRadio.setToggleGroup(unitGroup);
        unitFahrenheitRadio.setToggleGroup(unitGroup);
        boolean useF = appSettings.getTempUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT;
        unitFahrenheitRadio.setSelected(useF);
        unitCelsiusRadio.setSelected(!useF);

        ToggleGroup themeGroup = new ToggleGroup();
        themeDarkRadio = new RadioButton("Dark");
        themeLightRadio = new RadioButton("Light");
        themeDarkRadio.setToggleGroup(themeGroup);
        themeLightRadio.setToggleGroup(themeGroup);
        themeDarkRadio.setSelected(appSettings.isDarkTheme());
        themeLightRadio.setSelected(!appSettings.isDarkTheme());

        showCrosshairCheck = new CheckBox();
        showCrosshairCheck.setSelected(displaySettings.isShowCrosshair());
        showWatermarkCheck = new CheckBox();
        showWatermarkCheck.setSelected(displaySettings.isShowWatermark());
        showLegendCheck = new CheckBox();
        showLegendCheck.setSelected(displaySettings.isShowLegend());

        timeGuideSpinner = doubleSpinner(0.0, 3600.0, displaySettings.getTimeguideSec(), 1.0);
        aucBaseSpinner = doubleSpinner(0.0, 300.0, displaySettings.getAucBaseTemp(), 1.0);

        grid.add(new Label("Temperature Unit"), 0, row);
        grid.add(new HBox(10, unitCelsiusRadio, unitFahrenheitRadio), 1, row++);
        grid.add(new Label("Theme"), 0, row);
        grid.add(new HBox(10, themeDarkRadio, themeLightRadio), 1, row++);
        grid.add(new Label("Show Crosshair"), 0, row);
        grid.add(showCrosshairCheck, 1, row++);
        grid.add(new Label("Show Watermark"), 0, row);
        grid.add(showWatermarkCheck, 1, row++);
        grid.add(new Label("Show Legend"), 0, row);
        grid.add(showLegendCheck, 1, row++);
        grid.add(new Label("Time Guide (s)"), 0, row);
        grid.add(timeGuideSpinner, 1, row++);
        grid.add(new Label("AUC Base Temp (°C)"), 0, row);
        grid.add(aucBaseSpinner, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Y-Axis"), 0, row++, 2, 1);

        autoScaleYCheck = new CheckBox();
        autoScaleYCheck.setSelected(axisConfig.isAutoScaleY());
        autoScaleYCheck.selectedProperty().addListener((obs, oldVal, newVal) -> updateAutoScaleFields(newVal));

        tempMinSpinner = doubleSpinner(-40.0, 20.0, axisConfig.getTempMin(), 5.0);
        tempMaxSpinner = doubleSpinner(100.0, 350.0, axisConfig.getTempMax(), 5.0);
        autoScaleFloorSpinner = doubleSpinner(0.0, 100.0, axisConfig.getTempAutoScaleFloor(), 5.0);
        rorMinSpinner = doubleSpinner(-30.0, 0.0, axisConfig.getRorMin(), 1.0);
        rorMaxSpinner = doubleSpinner(10.0, 60.0, axisConfig.getRorMax(), 1.0);

        grid.add(new Label("Auto Scale Y"), 0, row);
        grid.add(autoScaleYCheck, 1, row++);
        grid.add(new Label("Temp Min (°C)"), 0, row);
        grid.add(tempMinSpinner, 1, row++);
        grid.add(new Label("Temp Max (°C)"), 0, row);
        grid.add(tempMaxSpinner, 1, row++);
        grid.add(new Label("Auto Scale Floor"), 0, row);
        grid.add(autoScaleFloorSpinner, 1, row++);
        grid.add(new Label("RoR Min"), 0, row);
        grid.add(rorMinSpinner, 1, row++);
        grid.add(new Label("RoR Max"), 0, row);
        grid.add(rorMaxSpinner, 1, row++);

        updateAutoScaleFields(autoScaleYCheck.isSelected());
        return new Tab("Graph", grid);
    }

    private Tab buildCurvesTab() {
        GridPane grid = buildGrid();
        int row = 0;

        grid.add(sectionLabel("Line Colors"), 0, row++, 2, 1);
        curveEtPicker = colorPicker(displaySettings.getPaletteCurveET());
        curveBtPicker = colorPicker(displaySettings.getPaletteCurveBT());
        curveDeltaEtPicker = colorPicker(displaySettings.getPaletteCurveDeltaET());
        curveDeltaBtPicker = colorPicker(displaySettings.getPaletteCurveDeltaBT());

        grid.add(new Label("ET Color"), 0, row);
        grid.add(curveEtPicker, 1, row++);
        grid.add(new Label("BT Color"), 0, row);
        grid.add(curveBtPicker, 1, row++);
        grid.add(new Label(DELTA + "ET Color"), 0, row);
        grid.add(curveDeltaEtPicker, 1, row++);
        grid.add(new Label(DELTA + "BT Color"), 0, row);
        grid.add(curveDeltaBtPicker, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Line Widths"), 0, row++, 2, 1);
        lineWidthEtSpinner = intSpinner(1, 6, displaySettings.getLineWidthET(), 1);
        lineWidthBtSpinner = intSpinner(1, 6, displaySettings.getLineWidthBT(), 1);
        lineWidthDeltaEtSpinner = intSpinner(1, 6, displaySettings.getLineWidthDeltaET(), 1);
        lineWidthDeltaBtSpinner = intSpinner(1, 6, displaySettings.getLineWidthDeltaBT(), 1);

        grid.add(new Label("ET Width (px)"), 0, row);
        grid.add(lineWidthEtSpinner, 1, row++);
        grid.add(new Label("BT Width (px)"), 0, row);
        grid.add(lineWidthBtSpinner, 1, row++);
        grid.add(new Label(DELTA + "ET Width (px)"), 0, row);
        grid.add(lineWidthDeltaEtSpinner, 1, row++);
        grid.add(new Label(DELTA + "BT Width (px)"), 0, row);
        grid.add(lineWidthDeltaBtSpinner, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Smoothing (Savitzky-Golay window, odd)"), 0, row++, 2, 1);
        smoothingBtSpinner = intSpinner(1, 99, toOdd(displaySettings.getSmoothingBT()), 2);
        smoothingEtSpinner = intSpinner(1, 99, toOdd(displaySettings.getSmoothingET()), 2);
        smoothingDeltaSpinner = intSpinner(1, 99, toOdd(displaySettings.getSmoothingDelta()), 2);

        grid.add(new Label("BT Smoothing"), 0, row);
        grid.add(smoothingBtSpinner, 1, row++);
        grid.add(new Label("ET Smoothing"), 0, row);
        grid.add(smoothingEtSpinner, 1, row++);
        grid.add(new Label("RoR Smoothing"), 0, row);
        grid.add(smoothingDeltaSpinner, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Visibility"), 0, row++, 2, 1);
        visibleEtCheck = new CheckBox();
        visibleEtCheck.setSelected(displaySettings.isVisibleET());
        visibleBtCheck = new CheckBox();
        visibleBtCheck.setSelected(displaySettings.isVisibleBT());
        visibleDeltaEtCheck = new CheckBox();
        visibleDeltaEtCheck.setSelected(displaySettings.isVisibleDeltaET());
        visibleDeltaBtCheck = new CheckBox();
        visibleDeltaBtCheck.setSelected(displaySettings.isVisibleDeltaBT());
        backgroundAlphaSlider = new Slider(0.0, 1.0, displaySettings.getBackgroundAlpha());
        backgroundAlphaSlider.setBlockIncrement(0.05);
        backgroundAlphaValueLabel = new Label(String.format("%.2f", backgroundAlphaSlider.getValue()));
        backgroundAlphaSlider.valueProperty().addListener((obs, oldVal, newVal) ->
            backgroundAlphaValueLabel.setText(String.format("%.2f", newVal.doubleValue())));

        grid.add(new Label("Show ET"), 0, row);
        grid.add(visibleEtCheck, 1, row++);
        grid.add(new Label("Show BT"), 0, row);
        grid.add(visibleBtCheck, 1, row++);
        grid.add(new Label("Show " + DELTA + "ET"), 0, row);
        grid.add(visibleDeltaEtCheck, 1, row++);
        grid.add(new Label("Show " + DELTA + "BT"), 0, row);
        grid.add(visibleDeltaBtCheck, 1, row++);
        grid.add(new Label("BG Profile Alpha"), 0, row);
        grid.add(new HBox(10, backgroundAlphaSlider, backgroundAlphaValueLabel), 1, row++);

        return new Tab("Curves", grid);
    }

    private Tab buildRoastTab() {
        GridPane grid = buildGrid();
        int row = 0;
        grid.add(sectionLabel("Auto-CHARGE Detection"), 0, row++, 2, 1);

        autoChargeDropSpinner = doubleSpinner(1.0, 20.0,
            roastStateMachine != null ? roastStateMachine.getAutoChargeTempDropDeg() : appSettings.getAutoChargeDrop(), 0.5);
        autoChargeSustainSpinner = doubleSpinner(5.0, 120.0,
            roastStateMachine != null ? roastStateMachine.getAutoChargeDropSustainSec() : appSettings.getAutoChargeSustain(), 5.0);
        preRoastTimeoutSpinner = doubleSpinner(60.0, 600.0,
            roastStateMachine != null ? roastStateMachine.getPreRoastTimeoutSec() : appSettings.getPreRoastTimeout(), 30.0);

        grid.add(new Label("BT Drop Threshold (°C)"), 0, row);
        grid.add(autoChargeDropSpinner, 1, row++);
        grid.add(new Label("Sustain Duration (s)"), 0, row);
        grid.add(autoChargeSustainSpinner, 1, row++);
        grid.add(new Label("Pre-start Timeout (s)"), 0, row);
        grid.add(preRoastTimeoutSpinner, 1, row++);

        Label info = new Label("Auto-CHARGE fires when BT drops \u2265 threshold and stays below for \u2265 sustain seconds.\n"
            + "If neither auto nor manual CHARGE occurs within timeout, recording will be cancelled.");
        info.setWrapText(true);
        grid.add(info, 0, row++, 2, 1);

        return new Tab("Phases & Roast", grid);
    }

    private Tab buildColorsTab() {
        ScrollPane scroll = new ScrollPane();
        scroll.setPrefHeight(400);
        GridPane grid = buildGrid();
        scroll.setContent(grid);
        scroll.setFitToWidth(true);

        String[][] paletteLabels = {
            { "background", "Background" },
            { "canvas", "Canvas" },
            { "grid", "Grid Lines" },
            { "title", "Title" },
            { "ylabel", "Y Axis Label" },
            { "xlabel", "X Axis Label" },
            { "text", "Text" },
            { "markers", "Markers" },
            { "watermarks", "Watermarks" },
            { "timeguide", "Time Guide" },
            { "aucguide", "AUC Guide" },
            { "aucarea", "AUC Area" },
            { "legendbg", "Legend Background" },
            { "legendborder", "Legend Border" },
            { "rect1", "Phase: Drying" },
            { "rect2", "Phase: Maillard" },
            { "rect3", "Phase: Development" },
            { "rect4", "Phase: Cooling" },
            { "rect5", "Phase: Extra" },
            { "specialeventbox", "Event Box" },
            { "specialeventtext", "Event Text" },
            { "bgeventmarker", "BG Event Marker" },
            { "bgeventtext", "BG Event Text" },
            { "metbox", "MET Box" },
            { "mettext", "MET Text" }
        };

        int row = 0;
        for (String[] pair : paletteLabels) {
            String key = pair[0];
            ColorPicker picker = colorPicker(displaySettings.getPalette(key));
            palettePickers.put(key, picker);
            grid.add(new Label(pair[1]), 0, row);
            grid.add(picker, 1, row++);
        }

        Button restoreDefaults = new Button("Restore Defaults");
        restoreDefaults.setOnAction(e -> restoreDisplayDefaults());
        Button setGrey = new Button("Set Grey");
        setGrey.setOnAction(e -> setGreyPalette());
        Button setDark = new Button("Set Dark Theme");
        setDark.setOnAction(e -> setDarkThemePalette());
        HBox buttons = new HBox(10, restoreDefaults, setGrey, setDark);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox(8, scroll, buttons);
        root.setPadding(new Insets(10));
        return new Tab("Colors", root);
    }

    private Tab buildChartAppearanceTab() {
        GridPane grid = buildGrid();
        int row = 0;

        grid.add(sectionLabel("Series Colors"), 0, row++, 2, 1);
        apBtColorPicker = colorPicker("#E74C3C");
        apEtColorPicker = colorPicker("#3498DB");
        apRorBtColorPicker = colorPicker("#2ECC71");
        apRorEtColorPicker = colorPicker("#5DADE2");
        apGasColorPicker = colorPicker("#95A5A6");
        apDrumColorPicker = colorPicker("#F1C40F");
        apEventLineColorPicker = colorPicker("#2C3E50");
        apEventDotColorPicker = colorPicker("#3498DB");
        addRow(grid, row++, "Bean Temp", apBtColorPicker);
        addRow(grid, row++, "Exhaust Temp", apEtColorPicker);
        addRow(grid, row++, "Bean RoR", apRorBtColorPicker);
        addRow(grid, row++, "Exhaust RoR", apRorEtColorPicker);
        addRow(grid, row++, "Gas", apGasColorPicker);
        addRow(grid, row++, "Drum Pressure", apDrumColorPicker);
        addRow(grid, row++, "Event Line", apEventLineColorPicker);
        addRow(grid, row++, "Event Dot", apEventDotColorPicker);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Line Widths (px)"), 0, row++, 2, 1);
        apBtWidthSpinner = doubleSpinner(0.5, 4.0, 2.0, 0.1);
        apEtWidthSpinner = doubleSpinner(0.5, 4.0, 1.8, 0.1);
        apRorBtWidthSpinner = doubleSpinner(0.5, 4.0, 1.5, 0.1);
        apRorEtWidthSpinner = doubleSpinner(0.5, 4.0, 1.2, 0.1);
        apGasWidthSpinner = doubleSpinner(0.5, 4.0, 1.8, 0.1);
        apDrumWidthSpinner = doubleSpinner(0.5, 4.0, 1.6, 0.1);
        addRow(grid, row++, "Bean Temp", apBtWidthSpinner);
        addRow(grid, row++, "Exhaust Temp", apEtWidthSpinner);
        addRow(grid, row++, "Bean RoR", apRorBtWidthSpinner);
        addRow(grid, row++, "Exhaust RoR", apRorEtWidthSpinner);
        addRow(grid, row++, "Gas", apGasWidthSpinner);
        addRow(grid, row++, "Drum Pressure", apDrumWidthSpinner);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Line Style"), 0, row++, 2, 1);
        apBtStyleCombo = new ComboBox<>();
        apEtStyleCombo = new ComboBox<>();
        apRorBtStyleCombo = new ComboBox<>();
        apRorEtStyleCombo = new ComboBox<>();
        apGasStyleCombo = new ComboBox<>();
        apDrumStyleCombo = new ComboBox<>();
        apBtStyleCombo.getItems().addAll(ChartAppearance.LineStyle.values());
        apEtStyleCombo.getItems().addAll(ChartAppearance.LineStyle.values());
        apRorBtStyleCombo.getItems().addAll(ChartAppearance.LineStyle.values());
        apRorEtStyleCombo.getItems().addAll(ChartAppearance.LineStyle.values());
        apGasStyleCombo.getItems().addAll(ChartAppearance.LineStyle.values());
        apDrumStyleCombo.getItems().addAll(ChartAppearance.LineStyle.values());
        addRow(grid, row++, "Bean Temp", apBtStyleCombo);
        addRow(grid, row++, "Exhaust Temp", apEtStyleCombo);
        addRow(grid, row++, "Bean RoR", apRorBtStyleCombo);
        addRow(grid, row++, "Exhaust RoR", apRorEtStyleCombo);
        addRow(grid, row++, "Gas", apGasStyleCombo);
        addRow(grid, row++, "Drum Pressure", apDrumStyleCombo);
        apGasFillOpacitySpinner = doubleSpinner(0.0, 1.0, 0.2, 0.05);
        addRow(grid, row++, "Gas Fill Opacity", apGasFillOpacitySpinner);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Background & Grid"), 0, row++, 2, 1);
        apBgMainColorPicker = colorPicker("#FAFAFA");
        apBgBottomColorPicker = colorPicker("#FAFAFA");
        apGridColorPicker = colorPicker("#E5E5E5");
        apGridOpacitySpinner = doubleSpinner(0.1, 1.0, 1.0, 0.05);
        addRow(grid, row++, "Main Background", apBgMainColorPicker);
        addRow(grid, row++, "Bottom Background", apBgBottomColorPicker);
        addRow(grid, row++, "Grid Color", apGridColorPicker);
        addRow(grid, row++, "Grid Opacity", apGridOpacitySpinner);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Axis"), 0, row++, 2, 1);
        apAxisFontField = new TextField("Arial");
        apAxisFontSizeSpinner = doubleSpinner(8.0, 24.0, 12.0, 1.0);
        apAxisFontColorPicker = colorPicker("#303030");
        addRow(grid, row++, "Font Family", apAxisFontField);
        addRow(grid, row++, "Font Size", apAxisFontSizeSpinner);
        addRow(grid, row++, "Font Color", apAxisFontColorPicker);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Legend"), 0, row++, 2, 1);
        apLegendPositionCombo = new ComboBox<>();
        apLegendPositionCombo.getItems().addAll(ChartAppearance.LegendPosition.values());
        addRow(grid, row++, "Position", apLegendPositionCombo);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Annotations"), 0, row++, 2, 1);
        apAnnotationBgPicker = colorPicker("#FFFFFF");
        apAnnotationTextPicker = colorPicker("#000000");
        apAnnotationFontSizeSpinner = doubleSpinner(8.0, 20.0, 11.0, 1.0);
        addRow(grid, row++, "Box Background", apAnnotationBgPicker);
        addRow(grid, row++, "Text Color", apAnnotationTextPicker);
        addRow(grid, row++, "Font Size", apAnnotationFontSizeSpinner);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Readouts"), 0, row++, 2, 1);
        apReadoutBtPicker = colorPicker("#E74C3C");
        apReadoutEtPicker = colorPicker("#3498DB");
        apReadoutRorBtPicker = colorPicker("#2ECC71");
        apReadoutRorEtPicker = colorPicker("#5DADE2");
        apReadoutMainSizeSpinner = doubleSpinner(16.0, 40.0, 28.0, 1.0);
        apReadoutSecondarySizeSpinner = doubleSpinner(10.0, 20.0, 14.0, 1.0);
        addRow(grid, row++, "BT Color", apReadoutBtPicker);
        addRow(grid, row++, "ET Color", apReadoutEtPicker);
        addRow(grid, row++, "RoR BT Color", apReadoutRorBtPicker);
        addRow(grid, row++, "RoR ET Color", apReadoutRorEtPicker);
        addRow(grid, row++, "Main Font Size", apReadoutMainSizeSpinner);
        addRow(grid, row++, "Secondary Font Size", apReadoutSecondarySizeSpinner);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Presets"), 0, row++, 2, 1);
        apPresetCombo = new ComboBox<>();
        apPresetCombo.setEditable(true);
        Button presetLoadBtn = new Button("Load");
        Button presetSaveBtn = new Button("Save");
        Button presetDeleteBtn = new Button("Delete");
        Button presetResetBtn = new Button("Reset to RI5 defaults");
        HBox presetRow = new HBox(8, apPresetCombo, presetLoadBtn, presetSaveBtn, presetDeleteBtn);
        grid.add(presetRow, 0, row++, 2, 1);
        grid.add(presetResetBtn, 0, row++, 2, 1);

        presetLoadBtn.setOnAction(e -> loadAppearancePreset());
        presetSaveBtn.setOnAction(e -> saveAppearancePreset());
        presetDeleteBtn.setOnAction(e -> deleteAppearancePreset());
        presetResetBtn.setOnAction(e -> resetAppearanceDefaults());

        reloadChartAppearanceUI();
        wireAppearanceListeners();
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        return new Tab("Chart Appearance", scroll);
    }

    private void updateDeviceVisibility(String deviceType) {
        boolean isRtu = "Modbus RTU".equals(deviceType);
        boolean isTcp = "Modbus TCP".equals(deviceType);
        boolean isModbus = isRtu || isTcp;

        setRowVisible(serialPortLabel, serialPortCombo, isRtu);
        setRowVisible(baudRateLabel, baudRateCombo, isRtu);
        setRowVisible(tcpHostLabel, tcpHostField, isTcp);
        setRowVisible(tcpPortLabel, tcpPortSpinner, isTcp);
        setRowVisible(modbusSlaveLabel, modbusSlaveIdSpinner, isModbus);
        setRowVisible(modbusBtLabel, modbusBtRegisterSpinner, isModbus);
        setRowVisible(modbusEtLabel, modbusEtRegisterSpinner, isModbus);
        setRowVisible(modbusScaleLabel, modbusScaleFactorSpinner, isModbus);
    }

    private void addRow(GridPane grid, int row, String label, Node control) {
        grid.add(new Label(label), 0, row);
        grid.add(control, 1, row);
    }

    private void reloadChartAppearanceUI() {
        if (uiPreferences == null) return;
        ChartAppearance ap = uiPreferences.getChartAppearance();
        apBtColorPicker.setValue(Color.web(ap.getBtColor()));
        apEtColorPicker.setValue(Color.web(ap.getEtColor()));
        apRorBtColorPicker.setValue(Color.web(ap.getRorBtColor()));
        apRorEtColorPicker.setValue(Color.web(ap.getRorEtColor()));
        apGasColorPicker.setValue(Color.web(ap.getGasColor()));
        apDrumColorPicker.setValue(Color.web(ap.getDrumColor()));
        apEventLineColorPicker.setValue(Color.web(ap.getEventLineColor()));
        apEventDotColorPicker.setValue(Color.web(ap.getEventDotColor()));
        apGridColorPicker.setValue(Color.web(ap.getGridColor()));
        apBgMainColorPicker.setValue(Color.web(ap.getBackgroundMain()));
        apBgBottomColorPicker.setValue(Color.web(ap.getBackgroundBottom()));
        apAxisFontColorPicker.setValue(Color.web(ap.getAxisFontColor()));
        apAnnotationBgPicker.setValue(Color.web(ap.getAnnotationBoxBg()));
        apAnnotationTextPicker.setValue(Color.web(ap.getAnnotationTextColor()));
        apReadoutBtPicker.setValue(Color.web(ap.getReadoutBtColor()));
        apReadoutEtPicker.setValue(Color.web(ap.getReadoutEtColor()));
        apReadoutRorBtPicker.setValue(Color.web(ap.getReadoutRorBtColor()));
        apReadoutRorEtPicker.setValue(Color.web(ap.getReadoutRorEtColor()));
        apBtWidthSpinner.getValueFactory().setValue(ap.getBtWidth());
        apEtWidthSpinner.getValueFactory().setValue(ap.getEtWidth());
        apRorBtWidthSpinner.getValueFactory().setValue(ap.getRorBtWidth());
        apRorEtWidthSpinner.getValueFactory().setValue(ap.getRorEtWidth());
        apGasWidthSpinner.getValueFactory().setValue(ap.getGasWidth());
        apDrumWidthSpinner.getValueFactory().setValue(ap.getDrumWidth());
        apGasFillOpacitySpinner.getValueFactory().setValue(ap.getGasFillOpacity());
        apGridOpacitySpinner.getValueFactory().setValue(ap.getGridOpacity());
        apAxisFontField.setText(ap.getAxisFontFamily());
        apAxisFontSizeSpinner.getValueFactory().setValue(ap.getAxisFontSize());
        apAnnotationFontSizeSpinner.getValueFactory().setValue(ap.getAnnotationFontSize());
        apLegendPositionCombo.setValue(ap.getLegendPosition());
        apReadoutMainSizeSpinner.getValueFactory().setValue(ap.getReadoutMainFontSize());
        apReadoutSecondarySizeSpinner.getValueFactory().setValue(ap.getReadoutSecondaryFontSize());
        apBtStyleCombo.setValue(ap.getBtLineStyle());
        apEtStyleCombo.setValue(ap.getEtLineStyle());
        apRorBtStyleCombo.setValue(ap.getRorBtLineStyle());
        apRorEtStyleCombo.setValue(ap.getRorEtLineStyle());
        apGasStyleCombo.setValue(ap.getGasLineStyle());
        apDrumStyleCombo.setValue(ap.getDrumLineStyle());
        apPresetCombo.getItems().setAll(uiPreferences.getChartAppearancePresets().keySet());
        apPresetCombo.setValue(uiPreferences.getChartAppearanceActivePreset());
    }

    private ChartAppearance buildChartAppearanceFromUI() {
        ChartAppearance ap = ChartAppearance.ri5Default();
        ap.setBtColor(toHex(apBtColorPicker.getValue()));
        ap.setEtColor(toHex(apEtColorPicker.getValue()));
        ap.setRorBtColor(toHex(apRorBtColorPicker.getValue()));
        ap.setRorEtColor(toHex(apRorEtColorPicker.getValue()));
        ap.setGasColor(toHex(apGasColorPicker.getValue()));
        ap.setDrumColor(toHex(apDrumColorPicker.getValue()));
        ap.setEventLineColor(toHex(apEventLineColorPicker.getValue()));
        ap.setEventDotColor(toHex(apEventDotColorPicker.getValue()));
        ap.setGridColor(toHex(apGridColorPicker.getValue()));
        ap.setBackgroundMain(toHex(apBgMainColorPicker.getValue()));
        ap.setBackgroundBottom(toHex(apBgBottomColorPicker.getValue()));
        ap.setAxisFontColor(toHex(apAxisFontColorPicker.getValue()));
        ap.setAnnotationBoxBg(toHex(apAnnotationBgPicker.getValue()));
        ap.setAnnotationTextColor(toHex(apAnnotationTextPicker.getValue()));
        ap.setReadoutBtColor(toHex(apReadoutBtPicker.getValue()));
        ap.setReadoutEtColor(toHex(apReadoutEtPicker.getValue()));
        ap.setReadoutRorBtColor(toHex(apReadoutRorBtPicker.getValue()));
        ap.setReadoutRorEtColor(toHex(apReadoutRorEtPicker.getValue()));
        ap.setBtWidth(apBtWidthSpinner.getValue());
        ap.setEtWidth(apEtWidthSpinner.getValue());
        ap.setRorBtWidth(apRorBtWidthSpinner.getValue());
        ap.setRorEtWidth(apRorEtWidthSpinner.getValue());
        ap.setGasWidth(apGasWidthSpinner.getValue());
        ap.setDrumWidth(apDrumWidthSpinner.getValue());
        ap.setGasFillOpacity(apGasFillOpacitySpinner.getValue());
        ap.setGridOpacity(apGridOpacitySpinner.getValue());
        ap.setAxisFontFamily(apAxisFontField.getText());
        ap.setAxisFontSize(apAxisFontSizeSpinner.getValue());
        ap.setAnnotationFontSize(apAnnotationFontSizeSpinner.getValue());
        ap.setLegendPosition(apLegendPositionCombo.getValue());
        ap.setReadoutMainFontSize(apReadoutMainSizeSpinner.getValue());
        ap.setReadoutSecondaryFontSize(apReadoutSecondarySizeSpinner.getValue());
        ap.setBtLineStyle(apBtStyleCombo.getValue());
        ap.setEtLineStyle(apEtStyleCombo.getValue());
        ap.setRorBtLineStyle(apRorBtStyleCombo.getValue());
        ap.setRorEtLineStyle(apRorEtStyleCombo.getValue());
        ap.setGasLineStyle(apGasStyleCombo.getValue());
        ap.setDrumLineStyle(apDrumStyleCombo.getValue());
        return ap;
    }

    private void applyChartAppearanceChanges() {
        if (uiPreferences == null) return;
        ChartAppearance ap = buildChartAppearanceFromUI();
        uiPreferences.setChartAppearance(ap);
        if (apPresetCombo.getValue() != null) {
            uiPreferences.setChartAppearanceActivePreset(apPresetCombo.getValue());
        }
        if (preferencesStore != null) preferencesStore.save(uiPreferences);
        applyChartAppearancePreview(ap);
    }

    private void applyChartAppearancePreview(ChartAppearance ap) {
        if (unifiedContext != null && unifiedContext.getChartController() != null) {
            unifiedContext.getChartController().setChartAppearance(ap);
        }
    }

    private void saveAppearancePreset() {
        if (uiPreferences == null) return;
        String name = apPresetCombo.getEditor().getText();
        if (name == null || name.isBlank()) return;
        uiPreferences.getChartAppearancePresets().put(name, buildChartAppearanceFromUI());
        uiPreferences.setChartAppearanceActivePreset(name);
        if (preferencesStore != null) preferencesStore.save(uiPreferences);
        reloadChartAppearanceUI();
    }

    private void loadAppearancePreset() {
        if (uiPreferences == null) return;
        String name = apPresetCombo.getValue();
        ChartAppearance ap = uiPreferences.getChartAppearancePresets().get(name);
        if (ap == null) return;
        uiPreferences.setChartAppearance(ap.copy());
        uiPreferences.setChartAppearanceActivePreset(name);
        if (preferencesStore != null) preferencesStore.save(uiPreferences);
        reloadChartAppearanceUI();
        applyChartAppearancePreview(ap);
    }

    private void deleteAppearancePreset() {
        if (uiPreferences == null) return;
        String name = apPresetCombo.getValue();
        if (name == null) return;
        uiPreferences.getChartAppearancePresets().remove(name);
        uiPreferences.setChartAppearanceActivePreset("RI5 Default");
        if (preferencesStore != null) preferencesStore.save(uiPreferences);
        reloadChartAppearanceUI();
    }

    private void resetAppearanceDefaults() {
        if (uiPreferences == null) return;
        uiPreferences.setChartAppearance(ChartAppearance.ri5Default());
        uiPreferences.setChartAppearanceActivePreset("RI5 Default");
        if (preferencesStore != null) preferencesStore.save(uiPreferences);
        reloadChartAppearanceUI();
        applyChartAppearancePreview(uiPreferences.getChartAppearance());
    }

    private void wireAppearanceListeners() {
        Runnable r = () -> applyChartAppearanceChanges();
        apBtColorPicker.setOnAction(e -> r.run());
        apEtColorPicker.setOnAction(e -> r.run());
        apRorBtColorPicker.setOnAction(e -> r.run());
        apRorEtColorPicker.setOnAction(e -> r.run());
        apGasColorPicker.setOnAction(e -> r.run());
        apDrumColorPicker.setOnAction(e -> r.run());
        apEventLineColorPicker.setOnAction(e -> r.run());
        apEventDotColorPicker.setOnAction(e -> r.run());
        apGridColorPicker.setOnAction(e -> r.run());
        apBgMainColorPicker.setOnAction(e -> r.run());
        apBgBottomColorPicker.setOnAction(e -> r.run());
        apAxisFontColorPicker.setOnAction(e -> r.run());
        apAnnotationBgPicker.setOnAction(e -> r.run());
        apAnnotationTextPicker.setOnAction(e -> r.run());
        apReadoutBtPicker.setOnAction(e -> r.run());
        apReadoutEtPicker.setOnAction(e -> r.run());
        apReadoutRorBtPicker.setOnAction(e -> r.run());
        apReadoutRorEtPicker.setOnAction(e -> r.run());

        apBtWidthSpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apEtWidthSpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apRorBtWidthSpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apRorEtWidthSpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apGasWidthSpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apDrumWidthSpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apGasFillOpacitySpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apGridOpacitySpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apAxisFontSizeSpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apAnnotationFontSizeSpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apReadoutMainSizeSpinner.valueProperty().addListener((o, ov, nv) -> r.run());
        apReadoutSecondarySizeSpinner.valueProperty().addListener((o, ov, nv) -> r.run());

        apBtStyleCombo.valueProperty().addListener((o, ov, nv) -> r.run());
        apEtStyleCombo.valueProperty().addListener((o, ov, nv) -> r.run());
        apRorBtStyleCombo.valueProperty().addListener((o, ov, nv) -> r.run());
        apRorEtStyleCombo.valueProperty().addListener((o, ov, nv) -> r.run());
        apGasStyleCombo.valueProperty().addListener((o, ov, nv) -> r.run());
        apDrumStyleCombo.valueProperty().addListener((o, ov, nv) -> r.run());
        apLegendPositionCombo.valueProperty().addListener((o, ov, nv) -> r.run());
        apAxisFontField.textProperty().addListener((o, ov, nv) -> r.run());
    }

    private void updateAutoScaleFields(boolean enabled) {
        if (tempMinSpinner != null) tempMinSpinner.setDisable(!enabled);
        if (tempMaxSpinner != null) tempMaxSpinner.setDisable(!enabled);
        if (autoScaleFloorSpinner != null) autoScaleFloorSpinner.setDisable(!enabled);
    }

    private void restoreAllDefaults() {
        deviceTypeCombo.setValue("Simulator");
        serialPortCombo.setValue("");
        baudRateCombo.setValue(9600);
        samplingRateSpinner.getValueFactory().setValue(2000);
        tcpHostField.setText("localhost");
        tcpPortSpinner.getValueFactory().setValue(502);
        modbusSlaveIdSpinner.getValueFactory().setValue(1);
        modbusBtRegisterSpinner.getValueFactory().setValue(1);
        modbusEtRegisterSpinner.getValueFactory().setValue(2);
        modbusScaleFactorSpinner.getValueFactory().setValue(10.0);

        unitCelsiusRadio.setSelected(true);
        themeDarkRadio.setSelected(true);
        showCrosshairCheck.setSelected(true);
        showWatermarkCheck.setSelected(true);
        showLegendCheck.setSelected(true);
        timeGuideSpinner.getValueFactory().setValue(0.0);
        aucBaseSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_AUC_BASE_TEMP_C);

        autoScaleYCheck.setSelected(true);
        tempMinSpinner.getValueFactory().setValue(0.0);
        tempMaxSpinner.getValueFactory().setValue(275.0);
        autoScaleFloorSpinner.getValueFactory().setValue(50.0);
        rorMinSpinner.getValueFactory().setValue(-20.0);
        rorMaxSpinner.getValueFactory().setValue(50.0);

        restoreDisplayDefaults();

        if (autoChargeDropSpinner != null) autoChargeDropSpinner.getValueFactory().setValue(5.0);
        if (autoChargeSustainSpinner != null) autoChargeSustainSpinner.getValueFactory().setValue(20.0);
        if (preRoastTimeoutSpinner != null) preRoastTimeoutSpinner.getValueFactory().setValue(300.0);

        updateDeviceVisibility(deviceTypeCombo.getValue());
        updateAutoScaleFields(autoScaleYCheck.isSelected());
    }

    private void restoreDisplayDefaults() {
        curveEtPicker.setValue(Color.web(DisplaySettings.DEFAULT_ET));
        curveBtPicker.setValue(Color.web(DisplaySettings.DEFAULT_BT));
        curveDeltaEtPicker.setValue(Color.web(DisplaySettings.DEFAULT_DELTAET));
        curveDeltaBtPicker.setValue(Color.web(DisplaySettings.DEFAULT_DELTABT));

        lineWidthEtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_LINEWIDTH_ET);
        lineWidthBtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_LINEWIDTH_BT);
        lineWidthDeltaEtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_LINEWIDTH_DELTAET);
        lineWidthDeltaBtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_LINEWIDTH_DELTABT);
        smoothingBtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_SMOOTHING);
        smoothingEtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_SMOOTHING);
        smoothingDeltaSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_SMOOTHING);
        visibleEtCheck.setSelected(true);
        visibleBtCheck.setSelected(true);
        visibleDeltaEtCheck.setSelected(true);
        visibleDeltaBtCheck.setSelected(true);
        backgroundAlphaSlider.setValue(0.2);
        showCrosshairCheck.setSelected(true);
        showWatermarkCheck.setSelected(true);
        showLegendCheck.setSelected(true);
        timeGuideSpinner.getValueFactory().setValue(0.0);
        aucBaseSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_AUC_BASE_TEMP_C);

        Map<String, String> defaults = defaultPalette();
        for (Map.Entry<String, ColorPicker> entry : palettePickers.entrySet()) {
            String hex = defaults.getOrDefault(entry.getKey(), "#000000");
            entry.getValue().setValue(Color.web(hex));
        }
    }

    private void setGreyPalette() {
        Map<String, String> grey = greyPalette();
        curveEtPicker.setValue(Color.web("#404040"));
        curveBtPicker.setValue(Color.web("#404040"));
        curveDeltaEtPicker.setValue(Color.web("#808080"));
        curveDeltaBtPicker.setValue(Color.web("#808080"));
        for (Map.Entry<String, ColorPicker> entry : palettePickers.entrySet()) {
            String hex = grey.getOrDefault(entry.getKey(), "#000000");
            entry.getValue().setValue(Color.web(hex));
        }
    }

    private void setDarkThemePalette() {
        Map<String, String> dark = darkPalette();
        for (Map.Entry<String, ColorPicker> entry : palettePickers.entrySet()) {
            String hex = dark.getOrDefault(entry.getKey(), "#000000");
            entry.getValue().setValue(Color.web(hex));
        }
    }

    private void applyChanges() {
        if (unifiedContext != null) {
            applyInlineChangesOnly();
            for (Runnable r : embeddedApplyActions) {
                r.run();
            }
            applyChartAppearanceChanges();
            if (unifiedContext.getOnSettingsApplied() != null) {
                unifiedContext.getOnSettingsApplied().run();
            }
            return;
        }

        commitAllSpinners();

        appSettings.setDeviceType(deviceTypeCombo.getValue());
        String port = serialPortCombo.getValue() != null ? serialPortCombo.getValue() : serialPortCombo.getEditor().getText();
        appSettings.setLastDevicePort(port != null ? port.trim() : "");
        Integer baud = baudRateCombo.getValue();
        if (baud != null) appSettings.setBaudRate(baud);
        if (samplingRateSpinner != null) appSettings.setSamplingRateMs(samplingRateSpinner.getValue());
        if (tcpHostField != null) appSettings.setTcpHost(tcpHostField.getText() != null ? tcpHostField.getText().trim() : "localhost");
        if (tcpPortSpinner != null) appSettings.setDeviceTcpPort(tcpPortSpinner.getValue());
        if (modbusSlaveIdSpinner != null) appSettings.setModbusSlaveId(modbusSlaveIdSpinner.getValue());
        if (modbusBtRegisterSpinner != null) appSettings.setModbusBtRegister(modbusBtRegisterSpinner.getValue());
        if (modbusEtRegisterSpinner != null) appSettings.setModbusEtRegister(modbusEtRegisterSpinner.getValue());
        if (modbusScaleFactorSpinner != null) appSettings.setModbusScaleFactor(modbusScaleFactorSpinner.getValue());

        if (unitFahrenheitRadio != null && unitFahrenheitRadio.isSelected()) {
            appSettings.setTempUnit(AxisConfig.TemperatureUnit.FAHRENHEIT);
        } else {
            appSettings.setTempUnit(AxisConfig.TemperatureUnit.CELSIUS);
        }
        appSettings.setDarkTheme(themeDarkRadio != null && themeDarkRadio.isSelected());

        displaySettings.setShowCrosshair(showCrosshairCheck != null && showCrosshairCheck.isSelected());
        displaySettings.setShowWatermark(showWatermarkCheck != null && showWatermarkCheck.isSelected());
        displaySettings.setShowLegend(showLegendCheck != null && showLegendCheck.isSelected());
        displaySettings.setTimeguideSec(timeGuideSpinner != null ? timeGuideSpinner.getValue() : displaySettings.getTimeguideSec());
        displaySettings.setAucBaseTemp(aucBaseSpinner != null ? aucBaseSpinner.getValue() : displaySettings.getAucBaseTemp());

        axisConfig.setAutoScaleY(autoScaleYCheck != null && autoScaleYCheck.isSelected());
        axisConfig.setTempMin(tempMinSpinner != null ? tempMinSpinner.getValue() : axisConfig.getTempMin());
        axisConfig.setTempMax(tempMaxSpinner != null ? tempMaxSpinner.getValue() : axisConfig.getTempMax());
        axisConfig.setTempAutoScaleFloor(autoScaleFloorSpinner != null ? autoScaleFloorSpinner.getValue() : axisConfig.getTempAutoScaleFloor());
        axisConfig.setRorMin(rorMinSpinner != null ? rorMinSpinner.getValue() : axisConfig.getRorMin());
        axisConfig.setRorMax(rorMaxSpinner != null ? rorMaxSpinner.getValue() : axisConfig.getRorMax());
        axisConfig.setUnit(appSettings.getTempUnit());

        appSettings.setAxisAutoScaleY(axisConfig.isAutoScaleY());
        appSettings.setAxisTempMin(axisConfig.getTempMin());
        appSettings.setAxisTempMax(axisConfig.getTempMax());
        appSettings.setAxisAutoScaleFloor(axisConfig.getTempAutoScaleFloor());
        appSettings.setAxisRorMin(axisConfig.getRorMin());
        appSettings.setAxisRorMax(axisConfig.getRorMax());

        if (curveEtPicker != null) displaySettings.setPaletteCurveET(toHex(curveEtPicker.getValue()));
        if (curveBtPicker != null) displaySettings.setPaletteCurveBT(toHex(curveBtPicker.getValue()));
        if (curveDeltaEtPicker != null) displaySettings.setPaletteCurveDeltaET(toHex(curveDeltaEtPicker.getValue()));
        if (curveDeltaBtPicker != null) displaySettings.setPaletteCurveDeltaBT(toHex(curveDeltaBtPicker.getValue()));
        if (lineWidthEtSpinner != null) displaySettings.setLineWidthET(lineWidthEtSpinner.getValue());
        if (lineWidthBtSpinner != null) displaySettings.setLineWidthBT(lineWidthBtSpinner.getValue());
        if (lineWidthDeltaEtSpinner != null) displaySettings.setLineWidthDeltaET(lineWidthDeltaEtSpinner.getValue());
        if (lineWidthDeltaBtSpinner != null) displaySettings.setLineWidthDeltaBT(lineWidthDeltaBtSpinner.getValue());
        if (smoothingBtSpinner != null) displaySettings.setSmoothingBT(toOdd(smoothingBtSpinner.getValue()));
        if (smoothingEtSpinner != null) displaySettings.setSmoothingET(toOdd(smoothingEtSpinner.getValue()));
        if (smoothingDeltaSpinner != null) displaySettings.setSmoothingDelta(toOdd(smoothingDeltaSpinner.getValue()));
        if (visibleEtCheck != null) displaySettings.setVisibleET(visibleEtCheck.isSelected());
        if (visibleBtCheck != null) displaySettings.setVisibleBT(visibleBtCheck.isSelected());
        if (visibleDeltaEtCheck != null) displaySettings.setVisibleDeltaET(visibleDeltaEtCheck.isSelected());
        if (visibleDeltaBtCheck != null) displaySettings.setVisibleDeltaBT(visibleDeltaBtCheck.isSelected());
        if (backgroundAlphaSlider != null) displaySettings.setBackgroundAlpha(backgroundAlphaSlider.getValue());
        for (Map.Entry<String, ColorPicker> entry : palettePickers.entrySet()) {
            displaySettings.setPalette(entry.getKey(), toHex(entry.getValue().getValue()));
        }

        if (roastStateMachine != null && autoChargeDropSpinner != null) {
            roastStateMachine.setAutoChargeTempDropDeg(autoChargeDropSpinner.getValue());
            roastStateMachine.setAutoChargeDropSustainSec(autoChargeSustainSpinner != null ? autoChargeSustainSpinner.getValue() : roastStateMachine.getAutoChargeDropSustainSec());
            roastStateMachine.setPreRoastTimeoutSec(preRoastTimeoutSpinner != null ? preRoastTimeoutSpinner.getValue() : roastStateMachine.getPreRoastTimeoutSec());
        }
        if (autoChargeDropSpinner != null) appSettings.setAutoChargeDrop(autoChargeDropSpinner.getValue());
        if (autoChargeSustainSpinner != null) appSettings.setAutoChargeSustain(autoChargeSustainSpinner.getValue());
        if (preRoastTimeoutSpinner != null) appSettings.setPreRoastTimeout(preRoastTimeoutSpinner.getValue());

        applyChartAppearanceChanges();
        appSettings.save();
    }

    /** Applies settings programmatically (used by UnifiedSettingsDialog). */
    public void applyFromUI() {
        applyChanges();
    }

    /** Applies only the inline tabs (Graph, Curves, Roast) when in unified mode. */
    private void applyInlineChangesOnly() {
        commitAllSpinners();
        if (unitFahrenheitRadio != null) {
            if (unitFahrenheitRadio.isSelected()) {
                appSettings.setTempUnit(AxisConfig.TemperatureUnit.FAHRENHEIT);
            } else {
                appSettings.setTempUnit(AxisConfig.TemperatureUnit.CELSIUS);
            }
        }
        if (themeDarkRadio != null) appSettings.setDarkTheme(themeDarkRadio.isSelected());
        if (showCrosshairCheck != null) displaySettings.setShowCrosshair(showCrosshairCheck.isSelected());
        if (showWatermarkCheck != null) displaySettings.setShowWatermark(showWatermarkCheck.isSelected());
        if (showLegendCheck != null) displaySettings.setShowLegend(showLegendCheck.isSelected());
        if (timeGuideSpinner != null) displaySettings.setTimeguideSec(timeGuideSpinner.getValue());
        if (aucBaseSpinner != null) displaySettings.setAucBaseTemp(aucBaseSpinner.getValue());
        if (autoScaleYCheck != null) axisConfig.setAutoScaleY(autoScaleYCheck.isSelected());
        if (tempMinSpinner != null) axisConfig.setTempMin(tempMinSpinner.getValue());
        if (tempMaxSpinner != null) axisConfig.setTempMax(tempMaxSpinner.getValue());
        if (autoScaleFloorSpinner != null) axisConfig.setTempAutoScaleFloor(autoScaleFloorSpinner.getValue());
        if (rorMinSpinner != null) axisConfig.setRorMin(rorMinSpinner.getValue());
        if (rorMaxSpinner != null) axisConfig.setRorMax(rorMaxSpinner.getValue());
        axisConfig.setUnit(appSettings.getTempUnit());
        appSettings.setAxisAutoScaleY(axisConfig.isAutoScaleY());
        appSettings.setAxisTempMin(axisConfig.getTempMin());
        appSettings.setAxisTempMax(axisConfig.getTempMax());
        appSettings.setAxisAutoScaleFloor(axisConfig.getTempAutoScaleFloor());
        appSettings.setAxisRorMin(axisConfig.getRorMin());
        appSettings.setAxisRorMax(axisConfig.getRorMax());
        if (curveEtPicker != null) displaySettings.setPaletteCurveET(toHex(curveEtPicker.getValue()));
        if (curveBtPicker != null) displaySettings.setPaletteCurveBT(toHex(curveBtPicker.getValue()));
        if (curveDeltaEtPicker != null) displaySettings.setPaletteCurveDeltaET(toHex(curveDeltaEtPicker.getValue()));
        if (curveDeltaBtPicker != null) displaySettings.setPaletteCurveDeltaBT(toHex(curveDeltaBtPicker.getValue()));
        if (lineWidthEtSpinner != null) displaySettings.setLineWidthET(lineWidthEtSpinner.getValue());
        if (lineWidthBtSpinner != null) displaySettings.setLineWidthBT(lineWidthBtSpinner.getValue());
        if (lineWidthDeltaEtSpinner != null) displaySettings.setLineWidthDeltaET(lineWidthDeltaEtSpinner.getValue());
        if (lineWidthDeltaBtSpinner != null) displaySettings.setLineWidthDeltaBT(lineWidthDeltaBtSpinner.getValue());
        if (smoothingBtSpinner != null) displaySettings.setSmoothingBT(toOdd(smoothingBtSpinner.getValue()));
        if (smoothingEtSpinner != null) displaySettings.setSmoothingET(toOdd(smoothingEtSpinner.getValue()));
        if (smoothingDeltaSpinner != null) displaySettings.setSmoothingDelta(toOdd(smoothingDeltaSpinner.getValue()));
        if (visibleEtCheck != null) displaySettings.setVisibleET(visibleEtCheck.isSelected());
        if (visibleBtCheck != null) displaySettings.setVisibleBT(visibleBtCheck.isSelected());
        if (visibleDeltaEtCheck != null) displaySettings.setVisibleDeltaET(visibleDeltaEtCheck.isSelected());
        if (visibleDeltaBtCheck != null) displaySettings.setVisibleDeltaBT(visibleDeltaBtCheck.isSelected());
        if (backgroundAlphaSlider != null) displaySettings.setBackgroundAlpha(backgroundAlphaSlider.getValue());
        if (roastStateMachine != null && autoChargeDropSpinner != null) {
            roastStateMachine.setAutoChargeTempDropDeg(autoChargeDropSpinner.getValue());
            roastStateMachine.setAutoChargeDropSustainSec(autoChargeSustainSpinner != null ? autoChargeSustainSpinner.getValue() : roastStateMachine.getAutoChargeDropSustainSec());
            roastStateMachine.setPreRoastTimeoutSec(preRoastTimeoutSpinner != null ? preRoastTimeoutSpinner.getValue() : roastStateMachine.getPreRoastTimeoutSec());
        }
        if (autoChargeDropSpinner != null) appSettings.setAutoChargeDrop(autoChargeDropSpinner.getValue());
        if (autoChargeSustainSpinner != null) appSettings.setAutoChargeSustain(autoChargeSustainSpinner.getValue());
        if (preRoastTimeoutSpinner != null) appSettings.setPreRoastTimeout(preRoastTimeoutSpinner.getValue());
        applyChartAppearanceChanges();
        appSettings.save();
    }

    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        return grid;
    }

    private static Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle(SECTION_STYLE);
        return label;
    }

    private static void setRowVisible(Node label, Node control, boolean visible) {
        if (label != null) {
            label.setVisible(visible);
            label.setManaged(visible);
        }
        if (control != null) {
            control.setVisible(visible);
            control.setManaged(visible);
        }
    }

    private static int toOdd(int v) {
        if (v < 1) return 1;
        if (v > 99) return 99;
        return (v % 2 == 0) ? v + 1 : v;
    }

    private static Spinner<Integer> intSpinner(int min, int max, int initial, int step) {
        Spinner<Integer> spinner = new Spinner<>();
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial, step);
        factory.setConverter(new IntegerStringConverter());
        spinner.setValueFactory(factory);
        configureSpinner(spinner);
        return spinner;
    }

    private static Spinner<Double> doubleSpinner(double min, double max, double initial, double step) {
        Spinner<Double> spinner = new Spinner<>();
        SpinnerValueFactory.DoubleSpinnerValueFactory factory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, step);
        factory.setConverter(new DoubleStringConverter());
        spinner.setValueFactory(factory);
        configureSpinner(spinner);
        return spinner;
    }

    private static void configureSpinner(Spinner<?> spinner) {
        spinner.setEditable(true);
        spinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitEditorText(spinner);
            }
        });
    }

    private void commitAllSpinners() {
        commitEditorText(samplingRateSpinner);
        commitEditorText(tcpPortSpinner);
        commitEditorText(modbusSlaveIdSpinner);
        commitEditorText(modbusBtRegisterSpinner);
        commitEditorText(modbusEtRegisterSpinner);
        commitEditorText(modbusScaleFactorSpinner);
        commitEditorText(timeGuideSpinner);
        commitEditorText(aucBaseSpinner);
        commitEditorText(tempMinSpinner);
        commitEditorText(tempMaxSpinner);
        commitEditorText(autoScaleFloorSpinner);
        commitEditorText(rorMinSpinner);
        commitEditorText(rorMaxSpinner);
        commitEditorText(lineWidthEtSpinner);
        commitEditorText(lineWidthBtSpinner);
        commitEditorText(lineWidthDeltaEtSpinner);
        commitEditorText(lineWidthDeltaBtSpinner);
        commitEditorText(smoothingBtSpinner);
        commitEditorText(smoothingEtSpinner);
        commitEditorText(smoothingDeltaSpinner);
        commitEditorText(apBtWidthSpinner);
        commitEditorText(apEtWidthSpinner);
        commitEditorText(apRorBtWidthSpinner);
        commitEditorText(apRorEtWidthSpinner);
        commitEditorText(apGasWidthSpinner);
        commitEditorText(apDrumWidthSpinner);
        commitEditorText(apGasFillOpacitySpinner);
        commitEditorText(apGridOpacitySpinner);
        commitEditorText(apAxisFontSizeSpinner);
        commitEditorText(apAnnotationFontSizeSpinner);
        commitEditorText(apReadoutMainSizeSpinner);
        commitEditorText(apReadoutSecondarySizeSpinner);
        commitEditorText(autoChargeDropSpinner);
        commitEditorText(autoChargeSustainSpinner);
        commitEditorText(preRoastTimeoutSpinner);
    }

    private static <T> void commitEditorText(Spinner<T> spinner) {
        if (spinner == null || !spinner.isEditable()) return;
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory == null) return;
        StringConverter<T> converter = valueFactory.getConverter();
        if (converter == null) return;
        String text = spinner.getEditor().getText();
        if (text == null) return;
        try {
            T value = converter.fromString(text);
            valueFactory.setValue(value);
        } catch (Exception ex) {
            spinner.getEditor().setText(converter.toString(valueFactory.getValue()));
        }
    }

    private static ColorPicker colorPicker(String hex) {
        ColorPicker picker = new ColorPicker();
        picker.setValue(Color.web(hex != null && !hex.isEmpty() ? hex : "#000000"));
        picker.setStyle("-fx-color-label-visible: false;");
        return picker;
    }

    private static String toHex(Color c) {
        if (c == null) return "#000000";
        return String.format("#%02x%02x%02x",
            (int) Math.round(c.getRed() * 255),
            (int) Math.round(c.getGreen() * 255),
            (int) Math.round(c.getBlue() * 255));
    }

    private static Map<String, String> defaultPalette() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("background", "#ffffff");
        map.put("canvas", "#f8f8f8");
        map.put("grid", "#e5e5e5");
        map.put("title", "#0c6aa6");
        map.put("ylabel", "#808080");
        map.put("xlabel", "#808080");
        map.put("text", "#000000");
        map.put("markers", "#000000");
        map.put("watermarks", "#ffff00");
        map.put("timeguide", "#0a5c90");
        map.put("aucguide", "#0c6aa6");
        map.put("aucarea", "#767676");
        map.put("legendbg", "#ffffff");
        map.put("legendborder", "#a9a9a9");
        map.put("rect1", "#e5e5e5");
        map.put("rect2", "#b2b2b2");
        map.put("rect3", "#e5e5e5");
        map.put("rect4", "#bde0ee");
        map.put("rect5", "#d3d3d3");
        map.put("specialeventbox", "#ff5871");
        map.put("specialeventtext", "#ffffff");
        map.put("bgeventmarker", "#7f7f7f");
        map.put("bgeventtext", "#000000");
        map.put("metbox", "#cc0f50");
        map.put("mettext", "#ffffff");
        return map;
    }

    private static Map<String, String> greyPalette() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("background", "#ffffff");
        map.put("canvas", "#f0f0f0");
        map.put("grid", "#c0c0c0");
        map.put("title", "#000000");
        map.put("ylabel", "#404040");
        map.put("xlabel", "#404040");
        map.put("text", "#000000");
        map.put("markers", "#000000");
        map.put("watermarks", "#606060");
        map.put("timeguide", "#404040");
        map.put("aucguide", "#404040");
        map.put("aucarea", "#a0a0a0");
        map.put("legendbg", "#e0e0e0");
        map.put("legendborder", "#808080");
        map.put("rect1", "#e5e5e5");
        map.put("rect2", "#b2b2b2");
        map.put("rect3", "#e5e5e5");
        map.put("rect4", "#b0b0b0");
        map.put("rect5", "#d3d3d3");
        map.put("specialeventbox", "#808080");
        map.put("specialeventtext", "#ffffff");
        map.put("bgeventmarker", "#606060");
        map.put("bgeventtext", "#000000");
        map.put("metbox", "#606060");
        map.put("mettext", "#ffffff");
        return map;
    }

    private static Map<String, String> darkPalette() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("background", "#1a1a2e");
        map.put("canvas", "#16213e");
        map.put("grid", "#2d4059");
        map.put("title", "#e0e0e0");
        map.put("ylabel", "#aaaaaa");
        map.put("xlabel", "#aaaaaa");
        map.put("text", "#e0e0e0");
        map.put("markers", "#cccccc");
        map.put("watermarks", "#333366");
        map.put("timeguide", "#5680e9");
        map.put("aucguide", "#5680e9");
        map.put("aucarea", "#334455");
        map.put("legendbg", "#1e1e3a");
        map.put("legendborder", "#444466");
        map.put("rect1", "#1e3a5f");
        map.put("rect2", "#2d1b1b");
        map.put("rect3", "#1b2d1b");
        map.put("rect4", "#1a1a2e");
        map.put("rect5", "#2a2a3e");
        map.put("specialeventbox", "#c0392b");
        map.put("specialeventtext", "#ffffff");
        map.put("bgeventmarker", "#555577");
        map.put("bgeventtext", "#cccccc");
        map.put("metbox", "#8e44ad");
        map.put("mettext", "#ffffff");
        return map;
    }
}
