package org.artisan.view;

import com.fazecast.jSerialComm.SerialPort;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import org.artisan.controller.AppSettings;
import org.artisan.controller.DisplaySettings;
import org.artisan.controller.RoastStateMachine;
import org.artisan.model.AxisConfig;

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

    public SettingsDialog(AppSettings appSettings,
                          DisplaySettings displaySettings,
                          AxisConfig axisConfig,
                          RoastStateMachine roastStateMachine,
                          Window owner) {
        this.appSettings = appSettings != null ? appSettings : new AppSettings();
        this.displaySettings = displaySettings != null ? displaySettings : DisplaySettings.load();
        this.axisConfig = axisConfig != null ? axisConfig : new AxisConfig();
        this.roastStateMachine = roastStateMachine;

        if (owner != null) initOwner(owner);
        setTitle("Settings");
        setResizable(true);
        getDialogPane().setPrefSize(700, 560);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(buildDeviceTab());
        tabs.getTabs().add(buildGraphTab());
        tabs.getTabs().add(buildCurvesTab());
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

        if (unitFahrenheitRadio.isSelected()) {
            appSettings.setTempUnit(AxisConfig.TemperatureUnit.FAHRENHEIT);
        } else {
            appSettings.setTempUnit(AxisConfig.TemperatureUnit.CELSIUS);
        }
        appSettings.setDarkTheme(themeDarkRadio.isSelected());

        displaySettings.setShowCrosshair(showCrosshairCheck.isSelected());
        displaySettings.setShowWatermark(showWatermarkCheck.isSelected());
        displaySettings.setShowLegend(showLegendCheck.isSelected());
        displaySettings.setTimeguideSec(timeGuideSpinner.getValue());
        displaySettings.setAucBaseTemp(aucBaseSpinner.getValue());

        axisConfig.setAutoScaleY(autoScaleYCheck.isSelected());
        axisConfig.setTempMin(tempMinSpinner.getValue());
        axisConfig.setTempMax(tempMaxSpinner.getValue());
        axisConfig.setTempAutoScaleFloor(autoScaleFloorSpinner.getValue());
        axisConfig.setRorMin(rorMinSpinner.getValue());
        axisConfig.setRorMax(rorMaxSpinner.getValue());
        axisConfig.setUnit(appSettings.getTempUnit());

        appSettings.setAxisAutoScaleY(axisConfig.isAutoScaleY());
        appSettings.setAxisTempMin(axisConfig.getTempMin());
        appSettings.setAxisTempMax(axisConfig.getTempMax());
        appSettings.setAxisAutoScaleFloor(axisConfig.getTempAutoScaleFloor());
        appSettings.setAxisRorMin(axisConfig.getRorMin());
        appSettings.setAxisRorMax(axisConfig.getRorMax());

        displaySettings.setPaletteCurveET(toHex(curveEtPicker.getValue()));
        displaySettings.setPaletteCurveBT(toHex(curveBtPicker.getValue()));
        displaySettings.setPaletteCurveDeltaET(toHex(curveDeltaEtPicker.getValue()));
        displaySettings.setPaletteCurveDeltaBT(toHex(curveDeltaBtPicker.getValue()));
        displaySettings.setLineWidthET(lineWidthEtSpinner.getValue());
        displaySettings.setLineWidthBT(lineWidthBtSpinner.getValue());
        displaySettings.setLineWidthDeltaET(lineWidthDeltaEtSpinner.getValue());
        displaySettings.setLineWidthDeltaBT(lineWidthDeltaBtSpinner.getValue());
        displaySettings.setSmoothingBT(toOdd(smoothingBtSpinner.getValue()));
        displaySettings.setSmoothingET(toOdd(smoothingEtSpinner.getValue()));
        displaySettings.setSmoothingDelta(toOdd(smoothingDeltaSpinner.getValue()));
        displaySettings.setVisibleET(visibleEtCheck.isSelected());
        displaySettings.setVisibleBT(visibleBtCheck.isSelected());
        displaySettings.setVisibleDeltaET(visibleDeltaEtCheck.isSelected());
        displaySettings.setVisibleDeltaBT(visibleDeltaBtCheck.isSelected());
        displaySettings.setBackgroundAlpha(backgroundAlphaSlider.getValue());
        for (Map.Entry<String, ColorPicker> entry : palettePickers.entrySet()) {
            displaySettings.setPalette(entry.getKey(), toHex(entry.getValue().getValue()));
        }

        if (roastStateMachine != null) {
            roastStateMachine.setAutoChargeTempDropDeg(autoChargeDropSpinner.getValue());
            roastStateMachine.setAutoChargeDropSustainSec(autoChargeSustainSpinner.getValue());
            roastStateMachine.setPreRoastTimeoutSec(preRoastTimeoutSpinner.getValue());
        }
        if (autoChargeDropSpinner != null) appSettings.setAutoChargeDrop(autoChargeDropSpinner.getValue());
        if (autoChargeSustainSpinner != null) appSettings.setAutoChargeSustain(autoChargeSustainSpinner.getValue());
        if (preRoastTimeoutSpinner != null) appSettings.setPreRoastTimeout(preRoastTimeoutSpinner.getValue());

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
