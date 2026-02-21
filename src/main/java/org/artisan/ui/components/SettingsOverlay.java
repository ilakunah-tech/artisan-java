package org.artisan.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.artisan.controller.AppSettings;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.AxisConfig;
import org.artisan.model.ProfileData;
import org.artisan.model.importer.GiesenImporter;
import org.artisan.model.importer.HiBeanImporter;
import org.artisan.model.importer.LoringImporter;
import org.artisan.model.importer.PetronciniImporter;
import org.artisan.model.importer.RoestImporter;
import org.artisan.model.importer.RubasseImporter;
import org.artisan.model.importer.StrongholdImporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Settings overlay panel shown in the center area when the settings icon is active.
 */
public final class SettingsOverlay extends StackPane {

    private static final String[] TAB_NAMES = { "Device", "Graph", "Curves", "Phases & Roast", "Colors", "Import" };
    private static final String DELTA = "\u0394";

    private final StackPane contentArea;
    private final ToggleGroup tabGroup;
    private final Map<String, VBox> tabPanes = new LinkedHashMap<>();
    private final Map<String, ToggleButton> tabButtons = new LinkedHashMap<>();

    private final AppSettings appSettings;
    private final DisplaySettings displaySettings;

    /** Called when the user imports a profile file — receives the loaded ProfileData. */
    private Consumer<ProfileData> onImport;

    // Device tab
    private ComboBox<String> deviceTypeCombo;
    private TextField serialPortField;
    private ComboBox<Integer> baudRateCombo;
    private TextField tcpHostField;
    private Spinner<Integer> tcpPortSpinner;
    private Spinner<Integer> modbusSlaveIdSpinner;
    private Spinner<Integer> modbusBtRegisterSpinner;
    private Spinner<Integer> modbusEtRegisterSpinner;
    private Spinner<Double> modbusScaleFactorSpinner;
    private Spinner<Integer> samplingRateSpinner;
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
    private Label aucBaseLabel;
    private CheckBox autoScaleYCheck;
    private Spinner<Double> tempMinSpinner;
    private Spinner<Double> tempMaxSpinner;
    private Spinner<Double> autoScaleFloorSpinner;
    private Spinner<Double> rorMinSpinner;
    private Spinner<Double> rorMaxSpinner;

    // Curves tab
    private Button curveEtBtn;
    private Button curveBtBtn;
    private Button curveDeltaEtBtn;
    private Button curveDeltaBtBtn;
    private Spinner<Integer> lineWidthEtSpinner;
    private Spinner<Integer> lineWidthBtSpinner;
    private Spinner<Integer> lineWidthDeltaEtSpinner;
    private Spinner<Integer> lineWidthDeltaBtSpinner;
    private Spinner<Integer> smoothingBtSpinner;
    private Spinner<Integer> smoothingEtSpinner;
    private Spinner<Integer> smoothingRorSpinner;
    private Spinner<Double> backgroundAlphaSpinner;
    private CheckBox visibleEtCheck;
    private CheckBox visibleBtCheck;
    private CheckBox visibleDeltaEtCheck;
    private CheckBox visibleDeltaBtCheck;

    // Phases & Roast tab
    private Spinner<Double> autoChargeDropSpinner;
    private Spinner<Double> autoChargeSustainSpinner;
    private Spinner<Double> preRoastTimeoutSpinner;

    // Colors tab
    private final Map<String, Button> paletteButtons = new LinkedHashMap<>();

    public SettingsOverlay() {
        setVisible(false);
        setManaged(false);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        this.appSettings = AppSettings.getInstance();
        this.displaySettings = DisplaySettings.getInstance();

        Pane bgPane = new Pane();
        bgPane.setStyle("-fx-background-color: rgba(0,0,0,0.15);");
        bgPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        bgPane.prefWidthProperty().bind(widthProperty());
        bgPane.prefHeightProperty().bind(heightProperty());
        bgPane.setOnMouseClicked(e -> hide());

        VBox card = new VBox(12);
        card.getStyleClass().add("settings-card");
        card.setMaxWidth(780);
        card.setMaxHeight(620);
        card.setOnMouseClicked(Event::consume);

        Label header = new Label("Settings");
        header.getStyleClass().add("settings-header");

        tabGroup = new ToggleGroup();
        HBox tabRow = new HBox(8);
        tabRow.setPadding(new Insets(0, 0, 8, 0));

        for (int i = 0; i < TAB_NAMES.length; i++) {
            String name = TAB_NAMES[i];
            ToggleButton tab = new ToggleButton(name);
            tab.getStyleClass().add("settings-tab");
            tab.setToggleGroup(tabGroup);
            tabRow.getChildren().add(tab);
            tabButtons.put(name, tab);
            VBox content = buildTabContent(name);
            tabPanes.put(name, content);
            if (i == 0) tab.setSelected(true);
        }

        contentArea = new StackPane();
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        for (int i = 0; i < TAB_NAMES.length; i++) {
            VBox pane = tabPanes.get(TAB_NAMES[i]);
            pane.setVisible(i == 0);
            pane.setManaged(i == 0);
            contentArea.getChildren().add(pane);
        }

        tabGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                if (oldToggle != null) oldToggle.setSelected(true);
                return;
            }
            int selectedIdx = tabRow.getChildren().indexOf((Node) newToggle);
            for (int i = 0; i < TAB_NAMES.length; i++) {
                VBox pane = tabPanes.get(TAB_NAMES[i]);
                pane.setVisible(i == selectedIdx);
                pane.setManaged(i == selectedIdx);
            }
        });

        HBox footer = buildFooter();

        card.getChildren().addAll(header, tabRow, contentArea, footer);
        StackPane.setAlignment(card, Pos.CENTER);

        getChildren().addAll(bgPane, card);

        reloadFromSettings();
    }

    private VBox buildTabContent(String tabName) {
        return switch (tabName) {
            case "Device" -> buildDeviceTab();
            case "Graph" -> buildGraphTab();
            case "Curves" -> buildCurvesTab();
            case "Phases & Roast" -> buildRoastTab();
            case "Colors" -> buildColorsTab();
            case "Import" -> buildImportTab();
            default -> new VBox(10);
        };
    }

    private VBox buildImportTab() {
        VBox rows = new VBox(8);
        rows.setPadding(new Insets(12));

        Label info = new Label("Open a roast profile from a third-party device:");
        info.setWrapText(true);
        rows.getChildren().add(info);
        rows.getChildren().add(new Separator());

        rows.getChildren().addAll(
            importButton("HiBean JSON...",       "*.json",         p -> HiBeanImporter.importFile(p)),
            importButton("Rubasse CSV...",        "*.csv",          p -> RubasseImporter.importFile(p)),
            importButton("Giesen CSV...",         "*.csv",          p -> GiesenImporter.importFile(p)),
            importButton("Loring CSV...",         "*.csv",          p -> LoringImporter.importFile(p)),
            importButton("Roest CSV...",          "*.csv",          p -> RoestImporter.importFile(p)),
            importButton("Petroncini CSV...",     "*.csv",          p -> PetronciniImporter.importFile(p)),
            importButton("Stronghold CSV/XLSX...", "*.csv;*.xlsx",  p -> StrongholdImporter.importFile(p))
        );

        VBox pane = new VBox(rows);
        pane.setPadding(new Insets(12));
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return pane;
    }

    @FunctionalInterface
    private interface ImportFn {
        ProfileData apply(Path path) throws IOException;
    }

    private Button importButton(String label, String extensions, ImportFn fn) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            for (String ext : extensions.split(";")) {
                chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter(ext.replace("*.", "").toUpperCase() + " files", ext.trim()));
            }
            javafx.stage.Window owner = getScene() != null ? getScene().getWindow() : null;
            java.io.File file = chooser.showOpenDialog(owner);
            if (file == null) return;
            try {
                ProfileData pd = fn.apply(file.toPath());
                if (pd != null && onImport != null) {
                    onImport.accept(pd);
                    hide();
                }
            } catch (Exception ex) {
                new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Import failed: " + ex.getMessage()).showAndWait();
            }
        });
        return btn;
    }

    /** Set the callback invoked when a profile is successfully imported. */
    public void setOnImport(Consumer<ProfileData> onImport) {
        this.onImport = onImport;
    }

    private VBox buildDeviceTab() {
        GridPane grid = buildGrid();
        int row = 0;

        grid.add(sectionLabel("Device"), 0, row++, 2, 1);

        deviceTypeCombo = new ComboBox<>();
        deviceTypeCombo.getItems().addAll("Simulator", "Serial", "TCP", "Modbus");
        deviceTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDeviceVisibility(newVal));

        serialPortLabel = new Label("Serial Port");
        serialPortField = new TextField();

        baudRateLabel = new Label("Baud Rate");
        baudRateCombo = new ComboBox<>();
        baudRateCombo.getItems().addAll(9600, 19200, 38400, 57600, 115200);

        tcpHostLabel = new Label("TCP Host");
        tcpHostField = new TextField();

        tcpPortLabel = new Label("TCP Port");
        tcpPortSpinner = intSpinner(1, 65535, 502, 1);

        modbusSlaveLabel = new Label("Modbus Slave ID");
        modbusSlaveIdSpinner = intSpinner(1, 247, 1, 1);

        modbusBtLabel = new Label("Modbus BT Register");
        modbusBtRegisterSpinner = intSpinner(0, 65535, 1, 1);

        modbusEtLabel = new Label("Modbus ET Register");
        modbusEtRegisterSpinner = intSpinner(0, 65535, 2, 1);

        modbusScaleLabel = new Label("Modbus Scale Factor");
        modbusScaleFactorSpinner = doubleSpinner(0.01, 1000.0, 10.0, 0.1);

        samplingRateSpinner = intSpinner(100, 10000, 2000, 100);

        grid.add(new Label("Device Type"), 0, row);
        grid.add(deviceTypeCombo, 1, row++);
        grid.add(serialPortLabel, 0, row);
        grid.add(serialPortField, 1, row++);
        grid.add(baudRateLabel, 0, row);
        grid.add(baudRateCombo, 1, row++);
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
        grid.add(new Label("Sampling Rate (ms)"), 0, row);
        grid.add(samplingRateSpinner, 1, row++);

        forceGrow(deviceTypeCombo, serialPortField, baudRateCombo, tcpHostField, tcpPortSpinner,
            modbusSlaveIdSpinner, modbusBtRegisterSpinner, modbusEtRegisterSpinner, modbusScaleFactorSpinner,
            samplingRateSpinner);

        VBox pane = wrapTab(grid);
        updateDeviceVisibility(deviceTypeCombo.getValue());
        return pane;
    }

    private VBox buildGraphTab() {
        GridPane grid = buildGrid();
        int row = 0;

        grid.add(sectionLabel("Display"), 0, row++, 2, 1);

        ToggleGroup unitGroup = new ToggleGroup();
        unitCelsiusRadio = new RadioButton("°C");
        unitFahrenheitRadio = new RadioButton("°F");
        unitCelsiusRadio.setToggleGroup(unitGroup);
        unitFahrenheitRadio.setToggleGroup(unitGroup);
        HBox unitBox = new HBox(10, unitCelsiusRadio, unitFahrenheitRadio);

        ToggleGroup themeGroup = new ToggleGroup();
        themeDarkRadio = new RadioButton("Dark");
        themeLightRadio = new RadioButton("Light");
        themeDarkRadio.setToggleGroup(themeGroup);
        themeLightRadio.setToggleGroup(themeGroup);
        HBox themeBox = new HBox(10, themeDarkRadio, themeLightRadio);

        showCrosshairCheck = new CheckBox();
        showWatermarkCheck = new CheckBox();
        showLegendCheck = new CheckBox();

        timeGuideSpinner = doubleSpinner(0.0, 3600.0, 0.0, 1.0);
        aucBaseSpinner = doubleSpinner(0.0, 300.0, DisplaySettings.DEFAULT_AUC_BASE_TEMP_C, 1.0);
        aucBaseLabel = new Label("AUC Base Temp (°C)");

        unitGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateAucLabel());

        grid.add(new Label("Temperature Unit"), 0, row);
        grid.add(unitBox, 1, row++);
        grid.add(new Label("Theme"), 0, row);
        grid.add(themeBox, 1, row++);
        grid.add(new Label("Show Crosshair"), 0, row);
        grid.add(showCrosshairCheck, 1, row++);
        grid.add(new Label("Show Watermark"), 0, row);
        grid.add(showWatermarkCheck, 1, row++);
        grid.add(new Label("Show Legend"), 0, row);
        grid.add(showLegendCheck, 1, row++);
        grid.add(new Label("Time Guide (s)"), 0, row);
        grid.add(timeGuideSpinner, 1, row++);
        grid.add(aucBaseLabel, 0, row);
        grid.add(aucBaseSpinner, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Y-Axis"), 0, row++, 2, 1);

        autoScaleYCheck = new CheckBox();
        autoScaleYCheck.selectedProperty().addListener((obs, oldVal, newVal) -> updateAutoScaleFields(newVal));

        tempMinSpinner = doubleSpinner(-40.0, 20.0, 0.0, 5.0);
        tempMaxSpinner = doubleSpinner(100.0, 350.0, 275.0, 5.0);
        autoScaleFloorSpinner = doubleSpinner(0.0, 100.0, 50.0, 5.0);
        rorMinSpinner = doubleSpinner(-30.0, 0.0, -20.0, 1.0);
        rorMaxSpinner = doubleSpinner(10.0, 60.0, 50.0, 1.0);

        grid.add(new Label("Auto Scale Y"), 0, row);
        grid.add(autoScaleYCheck, 1, row++);
        grid.add(new Label("Temp Min"), 0, row);
        grid.add(tempMinSpinner, 1, row++);
        grid.add(new Label("Temp Max"), 0, row);
        grid.add(tempMaxSpinner, 1, row++);
        grid.add(new Label("Auto Scale Floor"), 0, row);
        grid.add(autoScaleFloorSpinner, 1, row++);
        grid.add(new Label("RoR Min"), 0, row);
        grid.add(rorMinSpinner, 1, row++);
        grid.add(new Label("RoR Max"), 0, row);
        grid.add(rorMaxSpinner, 1, row++);

        forceGrow(unitBox, themeBox, timeGuideSpinner, aucBaseSpinner, tempMinSpinner,
            tempMaxSpinner, autoScaleFloorSpinner, rorMinSpinner, rorMaxSpinner);

        return wrapTab(grid);
    }

    private VBox buildCurvesTab() {
        GridPane grid = buildGrid();
        int row = 0;

        grid.add(sectionLabel("Line Colors"), 0, row++, 2, 1);
        curveEtBtn = colorButton(DisplaySettings.DEFAULT_ET);
        curveBtBtn = colorButton(DisplaySettings.DEFAULT_BT);
        curveDeltaEtBtn = colorButton(DisplaySettings.DEFAULT_DELTAET);
        curveDeltaBtBtn = colorButton(DisplaySettings.DEFAULT_DELTABT);

        grid.add(new Label("ET Color"), 0, row);
        grid.add(curveEtBtn, 1, row++);
        grid.add(new Label("BT Color"), 0, row);
        grid.add(curveBtBtn, 1, row++);
        grid.add(new Label(DELTA + "ET Color"), 0, row);
        grid.add(curveDeltaEtBtn, 1, row++);
        grid.add(new Label(DELTA + "BT Color"), 0, row);
        grid.add(curveDeltaBtBtn, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Line Widths"), 0, row++, 2, 1);

        lineWidthEtSpinner = intSpinner(1, 10, DisplaySettings.DEFAULT_LINEWIDTH_ET, 1);
        lineWidthBtSpinner = intSpinner(1, 10, DisplaySettings.DEFAULT_LINEWIDTH_BT, 1);
        lineWidthDeltaEtSpinner = intSpinner(1, 10, DisplaySettings.DEFAULT_LINEWIDTH_DELTAET, 1);
        lineWidthDeltaBtSpinner = intSpinner(1, 10, DisplaySettings.DEFAULT_LINEWIDTH_DELTABT, 1);

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

        smoothingBtSpinner = intSpinner(1, 99, DisplaySettings.DEFAULT_SMOOTHING, 2);
        smoothingEtSpinner = intSpinner(1, 99, DisplaySettings.DEFAULT_SMOOTHING, 2);
        smoothingRorSpinner = intSpinner(1, 99, DisplaySettings.DEFAULT_SMOOTHING, 2);

        grid.add(new Label("BT Smoothing"), 0, row);
        grid.add(smoothingBtSpinner, 1, row++);
        grid.add(new Label("ET Smoothing"), 0, row);
        grid.add(smoothingEtSpinner, 1, row++);
        grid.add(new Label("RoR Smoothing"), 0, row);
        grid.add(smoothingRorSpinner, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Visibility"), 0, row++, 2, 1);

        visibleEtCheck = new CheckBox("Show ET");
        visibleBtCheck = new CheckBox("Show BT");
        visibleDeltaEtCheck = new CheckBox("Show " + DELTA + "ET");
        visibleDeltaBtCheck = new CheckBox("Show " + DELTA + "BT");

        grid.add(visibleEtCheck, 0, row++, 2, 1);
        grid.add(visibleBtCheck, 0, row++, 2, 1);
        grid.add(visibleDeltaEtCheck, 0, row++, 2, 1);
        grid.add(visibleDeltaBtCheck, 0, row++, 2, 1);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(sectionLabel("Background"), 0, row++, 2, 1);

        backgroundAlphaSpinner = doubleSpinner(0.0, 1.0, 0.2, 0.05);
        grid.add(new Label("Background Alpha"), 0, row);
        grid.add(backgroundAlphaSpinner, 1, row++);

        forceGrow(lineWidthEtSpinner, lineWidthBtSpinner, lineWidthDeltaEtSpinner, lineWidthDeltaBtSpinner,
            smoothingBtSpinner, smoothingEtSpinner, smoothingRorSpinner, backgroundAlphaSpinner);

        wireColorButton(curveEtBtn);
        wireColorButton(curveBtBtn);
        wireColorButton(curveDeltaEtBtn);
        wireColorButton(curveDeltaBtBtn);

        return wrapTab(grid);
    }

    private VBox buildRoastTab() {
        GridPane grid = buildGrid();
        int row = 0;

        grid.add(sectionLabel("Auto-CHARGE Detection"), 0, row++, 2, 1);

        autoChargeDropSpinner = doubleSpinner(1.0, 20.0, 5.0, 0.5);
        autoChargeSustainSpinner = doubleSpinner(5.0, 120.0, 20.0, 5.0);
        preRoastTimeoutSpinner = doubleSpinner(60.0, 600.0, 300.0, 30.0);

        grid.add(new Label("BT Drop Threshold (°C)"), 0, row);
        grid.add(autoChargeDropSpinner, 1, row++);
        grid.add(new Label("Sustain Duration (s)"), 0, row);
        grid.add(autoChargeSustainSpinner, 1, row++);
        grid.add(new Label("Pre-start Timeout (s)"), 0, row);
        grid.add(preRoastTimeoutSpinner, 1, row++);

        Label info = new Label("Auto-CHARGE fires when BT drops ≥ threshold and stays below for ≥ sustain seconds.\n"
            + "If neither auto nor manual CHARGE occurs within timeout, recording will be cancelled.");
        info.setWrapText(true);
        grid.add(info, 0, row++, 2, 1);

        forceGrow(autoChargeDropSpinner, autoChargeSustainSpinner, preRoastTimeoutSpinner);

        return wrapTab(grid);
    }

    private VBox buildColorsTab() {
        VBox rows = new VBox(8);
        rows.setPadding(new Insets(10));

        for (String key : DisplaySettings.getPaletteKeys()) {
            Label label = new Label(key);
            Button btn = colorButton(displaySettings.getPalette(key));
            wireColorButton(btn);
            paletteButtons.put(key, btn);

            HBox row = new HBox(12, label, btn);
            row.setAlignment(Pos.CENTER_LEFT);
            rows.getChildren().add(row);
        }

        Button restoreDefaultsBtn = new Button("Restore Defaults");
        restoreDefaultsBtn.setOnAction(e -> applyPaletteMap(defaultPalette()));
        Button setGreyBtn = new Button("Set Grey");
        setGreyBtn.setOnAction(e -> applyPaletteMap(greyPalette()));
        Button setDarkBtn = new Button("Set Dark Theme");
        setDarkBtn.setOnAction(e -> applyPaletteMap(darkPalette()));
        HBox buttons = new HBox(10, restoreDefaultsBtn, setGreyBtn, setDarkBtn);
        buttons.setPadding(new Insets(8, 0, 0, 0));
        rows.getChildren().add(buttons);

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);

        VBox pane = new VBox(scroll);
        pane.setPadding(new Insets(12));
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return pane;
    }

    private HBox buildFooter() {
        Button restoreBtn = new Button("Restore Defaults");
        restoreBtn.setOnAction(e -> restoreDefaultsForCurrentTab());

        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("settings-apply-btn");
        okBtn.setOnAction(e -> {
            applyAllTabs();
            hide();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> {
            reloadFromSettings();
            hide();
        });

        Button applyBtn = new Button("Apply");
        applyBtn.getStyleClass().add("settings-apply-btn");
        applyBtn.setOnAction(e -> applyCurrentTab());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(10, restoreBtn, spacer, okBtn, cancelBtn, applyBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        return footer;
    }

    public void showTab(String tabName) {
        ToggleButton tab = tabButtons.get(tabName);
        if (tab != null) tab.setSelected(true);
        show();
    }

    public void show() {
        setOpacity(0);
        setVisible(true);
        setManaged(true);
        toFront();
        FadeTransition ft = new FadeTransition(Duration.millis(200), this);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    public void hide() {
        FadeTransition ft = new FadeTransition(Duration.millis(160), this);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setInterpolator(Interpolator.EASE_IN);
        ft.setOnFinished(e -> {
            setVisible(false);
            setManaged(false);
        });
        ft.play();
    }

    public void toggle() {
        if (isVisible()) hide(); else show();
    }

    private void updateDeviceVisibility(String deviceType) {
        boolean isSerial = "Serial".equals(deviceType);
        boolean isTcp = "TCP".equals(deviceType);
        boolean isModbus = "Modbus".equals(deviceType);

        setRowVisible(serialPortLabel, serialPortField, isSerial);
        setRowVisible(baudRateLabel, baudRateCombo, isSerial);
        setRowVisible(tcpHostLabel, tcpHostField, isTcp || isModbus);
        setRowVisible(tcpPortLabel, tcpPortSpinner, isTcp || isModbus);
        setRowVisible(modbusSlaveLabel, modbusSlaveIdSpinner, isModbus);
        setRowVisible(modbusBtLabel, modbusBtRegisterSpinner, isModbus);
        setRowVisible(modbusEtLabel, modbusEtRegisterSpinner, isModbus);
        setRowVisible(modbusScaleLabel, modbusScaleFactorSpinner, isModbus);
    }

    private void updateAutoScaleFields(boolean autoScaleY) {
        boolean enable = !autoScaleY;
        if (tempMinSpinner != null) tempMinSpinner.setDisable(!enable);
        if (tempMaxSpinner != null) tempMaxSpinner.setDisable(!enable);
        if (autoScaleFloorSpinner != null) autoScaleFloorSpinner.setDisable(!enable);
        if (rorMinSpinner != null) rorMinSpinner.setDisable(!enable);
        if (rorMaxSpinner != null) rorMaxSpinner.setDisable(!enable);
    }

    private void updateAucLabel() {
        boolean useF = unitFahrenheitRadio != null && unitFahrenheitRadio.isSelected();
        if (aucBaseLabel != null) aucBaseLabel.setText("AUC Base Temp (" + (useF ? "°F" : "°C") + ")");
    }

    private void reloadFromSettings() {
        if (deviceTypeCombo != null) {
            String type = appSettings.getDeviceType();
            if (!deviceTypeCombo.getItems().contains(type)) {
                deviceTypeCombo.getItems().add(type);
            }
            deviceTypeCombo.setValue(type);
        }
        if (serialPortField != null) serialPortField.setText(appSettings.getLastDevicePort());
        if (baudRateCombo != null) {
            int baud = appSettings.getBaudRate();
            if (!baudRateCombo.getItems().contains(baud)) baudRateCombo.getItems().add(baud);
            baudRateCombo.setValue(baud);
        }
        if (tcpHostField != null) tcpHostField.setText(appSettings.getTcpHost());
        if (tcpPortSpinner != null) tcpPortSpinner.getValueFactory().setValue(appSettings.getDeviceTcpPort());
        if (modbusSlaveIdSpinner != null) modbusSlaveIdSpinner.getValueFactory().setValue(appSettings.getModbusSlaveId());
        if (modbusBtRegisterSpinner != null) modbusBtRegisterSpinner.getValueFactory().setValue(appSettings.getModbusBtRegister());
        if (modbusEtRegisterSpinner != null) modbusEtRegisterSpinner.getValueFactory().setValue(appSettings.getModbusEtRegister());
        if (modbusScaleFactorSpinner != null) modbusScaleFactorSpinner.getValueFactory().setValue(appSettings.getModbusScaleFactor());
        if (samplingRateSpinner != null) samplingRateSpinner.getValueFactory().setValue(appSettings.getSamplingRateMs());
        updateDeviceVisibility(deviceTypeCombo != null ? deviceTypeCombo.getValue() : null);

        if (unitCelsiusRadio != null && unitFahrenheitRadio != null) {
            boolean useF = appSettings.getTempUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT;
            unitFahrenheitRadio.setSelected(useF);
            unitCelsiusRadio.setSelected(!useF);
        }
        if (themeDarkRadio != null && themeLightRadio != null) {
            themeDarkRadio.setSelected(appSettings.isDarkTheme());
            themeLightRadio.setSelected(!appSettings.isDarkTheme());
        }
        if (showCrosshairCheck != null) showCrosshairCheck.setSelected(displaySettings.isShowCrosshair());
        if (showWatermarkCheck != null) showWatermarkCheck.setSelected(displaySettings.isShowWatermark());
        if (showLegendCheck != null) showLegendCheck.setSelected(displaySettings.isShowLegend());
        if (timeGuideSpinner != null) timeGuideSpinner.getValueFactory().setValue(displaySettings.getTimeguideSec());
        if (aucBaseSpinner != null) aucBaseSpinner.getValueFactory().setValue(displaySettings.getAucBaseTemp());
        if (autoScaleYCheck != null) autoScaleYCheck.setSelected(appSettings.isAxisAutoScaleY());
        if (tempMinSpinner != null) tempMinSpinner.getValueFactory().setValue(appSettings.getAxisTempMin());
        if (tempMaxSpinner != null) tempMaxSpinner.getValueFactory().setValue(appSettings.getAxisTempMax());
        if (autoScaleFloorSpinner != null) autoScaleFloorSpinner.getValueFactory().setValue(appSettings.getAxisAutoScaleFloor());
        if (rorMinSpinner != null) rorMinSpinner.getValueFactory().setValue(appSettings.getAxisRorMin());
        if (rorMaxSpinner != null) rorMaxSpinner.getValueFactory().setValue(appSettings.getAxisRorMax());
        updateAutoScaleFields(autoScaleYCheck != null && autoScaleYCheck.isSelected());
        updateAucLabel();

        setButtonColor(curveEtBtn, displaySettings.getPaletteCurveET());
        setButtonColor(curveBtBtn, displaySettings.getPaletteCurveBT());
        setButtonColor(curveDeltaEtBtn, displaySettings.getPaletteCurveDeltaET());
        setButtonColor(curveDeltaBtBtn, displaySettings.getPaletteCurveDeltaBT());
        if (lineWidthEtSpinner != null) lineWidthEtSpinner.getValueFactory().setValue(displaySettings.getLineWidthET());
        if (lineWidthBtSpinner != null) lineWidthBtSpinner.getValueFactory().setValue(displaySettings.getLineWidthBT());
        if (lineWidthDeltaEtSpinner != null) lineWidthDeltaEtSpinner.getValueFactory().setValue(displaySettings.getLineWidthDeltaET());
        if (lineWidthDeltaBtSpinner != null) lineWidthDeltaBtSpinner.getValueFactory().setValue(displaySettings.getLineWidthDeltaBT());
        if (smoothingBtSpinner != null) smoothingBtSpinner.getValueFactory().setValue(toOdd(displaySettings.getSmoothingBT()));
        if (smoothingEtSpinner != null) smoothingEtSpinner.getValueFactory().setValue(toOdd(displaySettings.getSmoothingET()));
        if (smoothingRorSpinner != null) smoothingRorSpinner.getValueFactory().setValue(toOdd(displaySettings.getSmoothingDelta()));
        if (backgroundAlphaSpinner != null) backgroundAlphaSpinner.getValueFactory().setValue(displaySettings.getBackgroundAlpha());
        if (visibleEtCheck != null) visibleEtCheck.setSelected(displaySettings.isVisibleET());
        if (visibleBtCheck != null) visibleBtCheck.setSelected(displaySettings.isVisibleBT());
        if (visibleDeltaEtCheck != null) visibleDeltaEtCheck.setSelected(displaySettings.isVisibleDeltaET());
        if (visibleDeltaBtCheck != null) visibleDeltaBtCheck.setSelected(displaySettings.isVisibleDeltaBT());

        if (autoChargeDropSpinner != null) autoChargeDropSpinner.getValueFactory().setValue(appSettings.getAutoChargeDrop());
        if (autoChargeSustainSpinner != null) autoChargeSustainSpinner.getValueFactory().setValue(appSettings.getAutoChargeSustain());
        if (preRoastTimeoutSpinner != null) preRoastTimeoutSpinner.getValueFactory().setValue(appSettings.getPreRoastTimeout());

        for (Map.Entry<String, Button> entry : paletteButtons.entrySet()) {
            String key = entry.getKey();
            setButtonColor(entry.getValue(), displaySettings.getPalette(key));
        }
    }

    private void applyCurrentTab() {
        ToggleButton selected = (ToggleButton) tabGroup.getSelectedToggle();
        String name = selected != null ? selected.getText() : "";
        if ("Device".equals(name)) applyDeviceTab();
        else if ("Graph".equals(name)) applyGraphTab();
        else if ("Curves".equals(name)) applyCurvesTab();
        else if ("Phases & Roast".equals(name)) applyRoastTab();
        else if ("Colors".equals(name)) applyColorsTab();
        appSettings.save();
        displaySettings.save();
    }

    private void applyAllTabs() {
        applyDeviceTab();
        applyGraphTab();
        applyCurvesTab();
        applyRoastTab();
        applyColorsTab();
        appSettings.save();
        displaySettings.save();
    }

    private void applyDeviceTab() {
        commitAllSpinners();
        if (deviceTypeCombo != null) appSettings.setDeviceType(deviceTypeCombo.getValue());
        if (serialPortField != null) {
            appSettings.setLastDevicePort(serialPortField.getText() != null ? serialPortField.getText().trim() : "");
        }
        if (baudRateCombo != null && baudRateCombo.getValue() != null) appSettings.setBaudRate(baudRateCombo.getValue());
        if (tcpHostField != null) appSettings.setTcpHost(tcpHostField.getText() != null ? tcpHostField.getText().trim() : "localhost");
        if (tcpPortSpinner != null) appSettings.setDeviceTcpPort(tcpPortSpinner.getValue());
        if (modbusSlaveIdSpinner != null) appSettings.setModbusSlaveId(modbusSlaveIdSpinner.getValue());
        if (modbusBtRegisterSpinner != null) appSettings.setModbusBtRegister(modbusBtRegisterSpinner.getValue());
        if (modbusEtRegisterSpinner != null) appSettings.setModbusEtRegister(modbusEtRegisterSpinner.getValue());
        if (modbusScaleFactorSpinner != null) appSettings.setModbusScaleFactor(modbusScaleFactorSpinner.getValue());
        if (samplingRateSpinner != null) appSettings.setSamplingRateMs(samplingRateSpinner.getValue());
    }

    private void applyGraphTab() {
        commitAllSpinners();
        if (unitFahrenheitRadio != null && unitFahrenheitRadio.isSelected()) {
            appSettings.setTempUnit(AxisConfig.TemperatureUnit.FAHRENHEIT);
        } else {
            appSettings.setTempUnit(AxisConfig.TemperatureUnit.CELSIUS);
        }
        if (themeDarkRadio != null) appSettings.setDarkTheme(themeDarkRadio.isSelected());
        if (showCrosshairCheck != null) displaySettings.setShowCrosshair(showCrosshairCheck.isSelected());
        if (showWatermarkCheck != null) displaySettings.setShowWatermark(showWatermarkCheck.isSelected());
        if (showLegendCheck != null) displaySettings.setShowLegend(showLegendCheck.isSelected());
        if (timeGuideSpinner != null) displaySettings.setTimeguideSec(timeGuideSpinner.getValue());
        if (aucBaseSpinner != null) displaySettings.setAucBaseTemp(aucBaseSpinner.getValue());

        if (autoScaleYCheck != null) appSettings.setAxisAutoScaleY(autoScaleYCheck.isSelected());
        if (tempMinSpinner != null) appSettings.setAxisTempMin(tempMinSpinner.getValue());
        if (tempMaxSpinner != null) appSettings.setAxisTempMax(tempMaxSpinner.getValue());
        if (autoScaleFloorSpinner != null) appSettings.setAxisAutoScaleFloor(autoScaleFloorSpinner.getValue());
        if (rorMinSpinner != null) appSettings.setAxisRorMin(rorMinSpinner.getValue());
        if (rorMaxSpinner != null) appSettings.setAxisRorMax(rorMaxSpinner.getValue());
    }

    private void applyCurvesTab() {
        commitAllSpinners();
        if (curveEtBtn != null) displaySettings.setPaletteCurveET(getButtonColor(curveEtBtn));
        if (curveBtBtn != null) displaySettings.setPaletteCurveBT(getButtonColor(curveBtBtn));
        if (curveDeltaEtBtn != null) displaySettings.setPaletteCurveDeltaET(getButtonColor(curveDeltaEtBtn));
        if (curveDeltaBtBtn != null) displaySettings.setPaletteCurveDeltaBT(getButtonColor(curveDeltaBtBtn));
        if (lineWidthEtSpinner != null) displaySettings.setLineWidthET(lineWidthEtSpinner.getValue());
        if (lineWidthBtSpinner != null) displaySettings.setLineWidthBT(lineWidthBtSpinner.getValue());
        if (lineWidthDeltaEtSpinner != null) displaySettings.setLineWidthDeltaET(lineWidthDeltaEtSpinner.getValue());
        if (lineWidthDeltaBtSpinner != null) displaySettings.setLineWidthDeltaBT(lineWidthDeltaBtSpinner.getValue());
        if (smoothingBtSpinner != null) displaySettings.setSmoothingBT(toOdd(smoothingBtSpinner.getValue()));
        if (smoothingEtSpinner != null) displaySettings.setSmoothingET(toOdd(smoothingEtSpinner.getValue()));
        if (smoothingRorSpinner != null) displaySettings.setSmoothingDelta(toOdd(smoothingRorSpinner.getValue()));
        if (backgroundAlphaSpinner != null) displaySettings.setBackgroundAlpha(backgroundAlphaSpinner.getValue());
        if (visibleEtCheck != null) displaySettings.setVisibleET(visibleEtCheck.isSelected());
        if (visibleBtCheck != null) displaySettings.setVisibleBT(visibleBtCheck.isSelected());
        if (visibleDeltaEtCheck != null) displaySettings.setVisibleDeltaET(visibleDeltaEtCheck.isSelected());
        if (visibleDeltaBtCheck != null) displaySettings.setVisibleDeltaBT(visibleDeltaBtCheck.isSelected());
    }

    private void applyRoastTab() {
        commitAllSpinners();
        if (autoChargeDropSpinner != null) appSettings.setAutoChargeDrop(autoChargeDropSpinner.getValue());
        if (autoChargeSustainSpinner != null) appSettings.setAutoChargeSustain(autoChargeSustainSpinner.getValue());
        if (preRoastTimeoutSpinner != null) appSettings.setPreRoastTimeout(preRoastTimeoutSpinner.getValue());
    }

    private void applyColorsTab() {
        for (Map.Entry<String, Button> entry : paletteButtons.entrySet()) {
            displaySettings.setPalette(entry.getKey(), getButtonColor(entry.getValue()));
        }
    }

    private void restoreDefaultsForCurrentTab() {
        ToggleButton selected = (ToggleButton) tabGroup.getSelectedToggle();
        String name = selected != null ? selected.getText() : "";
        if ("Device".equals(name)) {
            deviceTypeCombo.setValue("Simulator");
            serialPortField.setText("");
            baudRateCombo.setValue(9600);
            tcpHostField.setText("localhost");
            tcpPortSpinner.getValueFactory().setValue(502);
            modbusSlaveIdSpinner.getValueFactory().setValue(1);
            modbusBtRegisterSpinner.getValueFactory().setValue(1);
            modbusEtRegisterSpinner.getValueFactory().setValue(2);
            modbusScaleFactorSpinner.getValueFactory().setValue(10.0);
            samplingRateSpinner.getValueFactory().setValue(2000);
            updateDeviceVisibility(deviceTypeCombo.getValue());
        } else if ("Graph".equals(name)) {
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
            updateAutoScaleFields(autoScaleYCheck.isSelected());
            updateAucLabel();
        } else if ("Curves".equals(name)) {
            setButtonColor(curveEtBtn, DisplaySettings.DEFAULT_ET);
            setButtonColor(curveBtBtn, DisplaySettings.DEFAULT_BT);
            setButtonColor(curveDeltaEtBtn, DisplaySettings.DEFAULT_DELTAET);
            setButtonColor(curveDeltaBtBtn, DisplaySettings.DEFAULT_DELTABT);
            lineWidthEtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_LINEWIDTH_ET);
            lineWidthBtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_LINEWIDTH_BT);
            lineWidthDeltaEtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_LINEWIDTH_DELTAET);
            lineWidthDeltaBtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_LINEWIDTH_DELTABT);
            smoothingBtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_SMOOTHING);
            smoothingEtSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_SMOOTHING);
            smoothingRorSpinner.getValueFactory().setValue(DisplaySettings.DEFAULT_SMOOTHING);
            backgroundAlphaSpinner.getValueFactory().setValue(0.2);
            visibleEtCheck.setSelected(true);
            visibleBtCheck.setSelected(true);
            visibleDeltaEtCheck.setSelected(true);
            visibleDeltaBtCheck.setSelected(true);
        } else if ("Phases & Roast".equals(name)) {
            autoChargeDropSpinner.getValueFactory().setValue(5.0);
            autoChargeSustainSpinner.getValueFactory().setValue(20.0);
            preRoastTimeoutSpinner.getValueFactory().setValue(300.0);
        } else if ("Colors".equals(name)) {
            applyPaletteMap(defaultPalette());
        }
    }

    private void applyPaletteMap(Map<String, String> map) {
        for (String key : paletteButtons.keySet()) {
            String hex = map.getOrDefault(key, "#000000");
            setButtonColor(paletteButtons.get(key), hex);
        }
    }

    private static GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        return grid;
    }

    private static VBox wrapTab(Node content) {
        VBox pane = new VBox(10, content);
        pane.setPadding(new Insets(12));
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return pane;
    }

    private static Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("settings-section-label");
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
        commitEditorText(smoothingRorSpinner);
        commitEditorText(backgroundAlphaSpinner);
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

    private static void forceGrow(Node... nodes) {
        if (nodes == null) return;
        for (Node node : nodes) {
            if (node instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(region, Priority.ALWAYS);
                GridPane.setHgrow(region, Priority.ALWAYS);
            }
        }
    }

    private static Button colorButton(String hex) {
        Button b = new Button();
        b.getStyleClass().add("settings-color-btn");
        b.setPrefSize(24, 24);
        setButtonColor(b, hex);
        return b;
    }

    private void wireColorButton(Button btn) {
        btn.setOnAction(e -> {
            Color current = Color.web(getButtonColor(btn));
            Color selected = pickColor(btn, current);
            if (selected == null) return;
            setButtonColor(btn, toHex(selected));
        });
    }

    private static void setButtonColor(Button btn, String hex) {
        if (btn == null) return;
        String safe = (hex != null && !hex.isBlank()) ? hex : "#000000";
        btn.setUserData(safe);
        btn.setStyle("-fx-background-color: " + safe + "; -fx-border-color: #555;");
    }

    private static String getButtonColor(Button btn) {
        Object data = btn != null ? btn.getUserData() : null;
        return data != null ? data.toString() : "#000000";
    }

    private static Color pickColor(Button owner, Color initial) {
        ColorPicker picker = new ColorPicker(initial != null ? initial : Color.BLACK);
        Button ok = new Button("OK");
        Stage popup = new Stage();
        popup.initOwner(owner.getScene() != null ? owner.getScene().getWindow() : null);
        popup.initModality(Modality.APPLICATION_MODAL);
        final Color[] result = { null };
        ok.setOnAction(e2 -> {
            result[0] = picker.getValue();
            popup.close();
        });
        VBox v = new VBox(10, picker, ok);
        v.setPadding(new Insets(10));
        popup.setScene(new Scene(v));
        popup.showAndWait();
        return result[0];
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
        map.put("analysismask", "#bababa");
        map.put("statsanalysisbkgnd", "#ffffff");
        map.put("backgroundmetcolor", DisplaySettings.DEFAULT_ET);
        map.put("backgroundbtcolor", DisplaySettings.DEFAULT_BT);
        map.put("backgrounddeltaetcolor", DisplaySettings.DEFAULT_DELTAET);
        map.put("backgrounddeltabtcolor", DisplaySettings.DEFAULT_DELTABT);
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
        map.put("analysismask", "#c0c0c0");
        map.put("statsanalysisbkgnd", "#e0e0e0");
        map.put("backgroundmetcolor", "#a0a0a0");
        map.put("backgroundbtcolor", "#a0a0a0");
        map.put("backgrounddeltaetcolor", "#a0a0a0");
        map.put("backgrounddeltabtcolor", "#a0a0a0");
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
        map.put("analysismask", "#3f3f55");
        map.put("statsanalysisbkgnd", "#2a2a3e");
        map.put("backgroundmetcolor", DisplaySettings.DEFAULT_ET);
        map.put("backgroundbtcolor", DisplaySettings.DEFAULT_BT);
        map.put("backgrounddeltaetcolor", DisplaySettings.DEFAULT_DELTAET);
        map.put("backgrounddeltabtcolor", DisplaySettings.DEFAULT_DELTABT);
        return map;
    }
}
