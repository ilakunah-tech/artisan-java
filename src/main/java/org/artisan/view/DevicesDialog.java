package org.artisan.view;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.controller.DeviceManager;
import org.artisan.device.AillioR1Config;
import org.artisan.device.DeviceConfig;
import org.artisan.device.DeviceChannel;
import org.artisan.device.DeviceException;
import org.artisan.device.DeviceType;
import org.artisan.device.ModbusPortConfig;
import org.artisan.device.SerialPortConfig;
import org.artisan.device.SimulatorConfig;

import java.util.List;

/**
 * Config → Device... dialog: select device type, configure port/simulator/Aillio R1, Test Connection, OK/Apply.
 */
public final class DevicesDialog extends ArtisanDialog {

    private final AppController appController;
    private final SerialPortConfig serialPortConfig;
    private final ModbusPortConfig modbusPortConfig;
    private final DeviceConfig deviceConfig;
    private final SimulatorConfig simulatorConfig;
    private final AillioR1Config aillioR1Config;

    private ComboBox<DeviceType> deviceTypeCombo;
    private Label statusLabel;
    private VBox configStack;
    private Node serialPanel;
    private Node modbusPanel;
    private Node simulatorPanel;
    private Node aillioR1Panel;
    private Button testButton;
    private Label testResultLabel;

    private ComboBox<String> serialPortCombo;
    private ComboBox<Integer> baudCombo;
    private TextField modbusHostField;
    private Spinner<Integer> modbusPortSpinner;
    private Spinner<Integer> slaveIdSpinner;
    private Spinner<Integer> btRegSpinner;
    private Spinner<Integer> etRegSpinner;
    private Spinner<Double> scaleSpinner;
    private javafx.scene.control.RadioButton modbusTcpRadio;
    private javafx.scene.control.RadioButton modbusRtuRadio;

    private Spinner<Double> simBtStartSpinner;
    private Spinner<Double> simSpeedSpinner;
    private TextField aillioVidField;
    private TextField aillioPidField;
    private Button scanHidButton;

    private static final Integer[] BAUD_OPTIONS = { 2400, 4800, 9600, 19200, 38400, 57600, 115200 };

    public DevicesDialog(Window owner, AppController appController,
                         SerialPortConfig serialPortConfig, ModbusPortConfig modbusPortConfig,
                         DeviceConfig deviceConfig, SimulatorConfig simulatorConfig, AillioR1Config aillioR1Config) {
        super(owner, true, true);
        this.appController = appController;
        this.serialPortConfig = serialPortConfig != null ? serialPortConfig : new SerialPortConfig();
        this.modbusPortConfig = modbusPortConfig != null ? modbusPortConfig : new ModbusPortConfig();
        this.deviceConfig = deviceConfig != null ? deviceConfig : new DeviceConfig();
        this.simulatorConfig = simulatorConfig != null ? simulatorConfig : new SimulatorConfig();
        this.aillioR1Config = aillioR1Config != null ? aillioR1Config : new AillioR1Config();
        getStage().setTitle("Config » Device");
        getApplyButton().setOnAction(this::onApply);
    }

    @Override
    protected Node buildContent() {
        deviceConfig.load();
        SimulatorConfig.loadFromPreferences(simulatorConfig);
        AillioR1Config.loadFromPreferences(aillioR1Config);
        if (serialPortConfig != null) SerialPortConfig.loadFromPreferences(serialPortConfig);
        if (modbusPortConfig != null) ModbusPortConfig.loadFromPreferences(modbusPortConfig);

        deviceTypeCombo = new ComboBox<>();
        deviceTypeCombo.getItems().add(DeviceType.NONE);
        deviceTypeCombo.getItems().addAll(DeviceManager.listAvailable());
        deviceTypeCombo.setConverter(new javafx.util.StringConverter<DeviceType>() {
            @Override
            public String toString(DeviceType t) {
                return t != null ? t.getDisplayName() : "";
            }
            @Override
            public DeviceType fromString(String s) {
                return null;
            }
        });
        deviceTypeCombo.setValue(deviceConfig.getActiveType());

        statusLabel = new Label("Not connected");
        updateStatusLabel();

        serialPanel = buildSerialPanel();
        modbusPanel = buildModbusPanel();
        simulatorPanel = buildSimulatorPanel();
        aillioR1Panel = buildAillioR1Panel();

        configStack = new VBox(8);
        configStack.setPadding(new Insets(0, 0, 8, 0));
        showConfigFor(deviceTypeCombo.getValue());

        deviceTypeCombo.valueProperty().addListener((o, oldVal, newVal) -> {
            updateStatusLabel();
            showConfigFor(newVal);
        });

        testButton = new Button("Test Connection");
        testButton.setOnAction(this::onTestConnection);
        testResultLabel = new Label(" ");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(new Label("Device:"), 0, row);
        grid.add(deviceTypeCombo, 1, row++);
        grid.add(new Label("Status:"), 0, row);
        grid.add(statusLabel, 1, row++);
        grid.add(configStack, 0, row++, 2, 1);
        grid.add(testButton, 0, row);
        grid.add(testResultLabel, 1, row);

        VBox root = new VBox(10, grid);
        root.setPadding(new Insets(10));
        return root;
    }

    private void updateStatusLabel() {
        DeviceType t = deviceTypeCombo.getValue();
        if (t == null || t == DeviceType.NONE) {
            statusLabel.setText("Not connected");
        } else if (isStubType(t)) {
            statusLabel.setText("Stub");
        } else {
            statusLabel.setText("Not connected");
        }
    }

    private static boolean isStubType(DeviceType t) {
        return t == DeviceType.AILLIO_R2 || t == DeviceType.HOTTOP_KN8828B || t == DeviceType.IKAWA
            || t == DeviceType.KALEIDO_M1 || t == DeviceType.GIESEN || t == DeviceType.LORING
            || t == DeviceType.SANTOKER || t == DeviceType.STRONGHOLD_S7X || t == DeviceType.ROEST
            || t == DeviceType.ACAIA_LUNAR;
    }

    private void showConfigFor(DeviceType type) {
        configStack.getChildren().clear();
        if (type == null || type == DeviceType.NONE) {
            return;
        }
        if (type.isRequiresSerial() && !type.isRequiresModbus()) {
            configStack.getChildren().add(serialPanel);
        } else if (type.isRequiresModbus()) {
            configStack.getChildren().add(modbusPanel);
        } else if (type == DeviceType.SIMULATOR) {
            configStack.getChildren().add(simulatorPanel);
        } else if (type == DeviceType.AILLIO_R1) {
            configStack.getChildren().add(aillioR1Panel);
        } else if (isStubType(type) && type.isRequiresSerial()) {
            configStack.getChildren().add(serialPanel);
        }
    }

    private Node buildSerialPanel() {
        serialPortCombo = new ComboBox<>();
        serialPortCombo.setEditable(true);
        List<String> ports = org.artisan.device.DeviceManager.scanSerialPorts(); // device package
        serialPortCombo.getItems().addAll(ports);
        String port = serialPortConfig.getPortName();
        if (port != null && !port.isEmpty()) {
            if (!serialPortCombo.getItems().contains(port)) serialPortCombo.getItems().add(port);
            serialPortCombo.setValue(port);
        }
        baudCombo = new ComboBox<>();
        baudCombo.getItems().addAll(BAUD_OPTIONS);
        baudCombo.setValue(serialPortConfig.getBaudRate());
        if (!baudCombo.getItems().contains(serialPortConfig.getBaudRate())) {
            baudCombo.getItems().add(serialPortConfig.getBaudRate());
            baudCombo.setValue(serialPortConfig.getBaudRate());
        }
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(6);
        g.add(new Label("Port:"), 0, 0);
        g.add(serialPortCombo, 1, 0);
        g.add(new Label("Baud rate:"), 0, 1);
        g.add(baudCombo, 1, 1);
        return new VBox(6, g);
    }

    private Node buildModbusPanel() {
        modbusTcpRadio = new javafx.scene.control.RadioButton("TCP");
        modbusRtuRadio = new javafx.scene.control.RadioButton("RTU");
        javafx.scene.control.ToggleGroup tg = new javafx.scene.control.ToggleGroup();
        modbusTcpRadio.setToggleGroup(tg);
        modbusRtuRadio.setToggleGroup(tg);
        modbusTcpRadio.setSelected(modbusPortConfig.isUseTcp());
        modbusRtuRadio.setSelected(!modbusPortConfig.isUseTcp());

        modbusHostField = new TextField(modbusPortConfig.getHost());
        modbusHostField.setPromptText(modbusPortConfig.isUseTcp() ? "192.168.1.1" : "COM3");
        modbusPortSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, modbusPortConfig.getPort(), 1));
        modbusPortSpinner.setEditable(true);
        slaveIdSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, modbusPortConfig.getSlaveId(), 1));
        slaveIdSpinner.setEditable(true);
        btRegSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, modbusPortConfig.getBtRegister(), 1));
        btRegSpinner.setEditable(true);
        etRegSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, modbusPortConfig.getEtRegister(), 1));
        etRegSpinner.setEditable(true);
        scaleSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.001, 10.0, modbusPortConfig.getScale(), 0.01));
        scaleSpinner.setEditable(true);

        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(6);
        int r = 0;
        g.add(new javafx.scene.layout.HBox(10, modbusTcpRadio, modbusRtuRadio), 0, r++, 2, 1);
        g.add(new Label("Host / Port:"), 0, r);
        g.add(modbusHostField, 1, r++);
        g.add(new Label("TCP Port:"), 0, r);
        g.add(modbusPortSpinner, 1, r++);
        g.add(new Label("Slave ID:"), 0, r);
        g.add(slaveIdSpinner, 1, r++);
        g.add(new Label("BT reg:"), 0, r);
        g.add(btRegSpinner, 1, r++);
        g.add(new Label("ET reg:"), 0, r);
        g.add(etRegSpinner, 1, r++);
        g.add(new Label("Scale:"), 0, r);
        g.add(scaleSpinner, 1, r++);
        return new VBox(6, g);
    }

    private Node buildSimulatorPanel() {
        simBtStartSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, simulatorConfig.getBtStartTemp(), 1));
        simBtStartSpinner.setEditable(true);
        simSpeedSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 10, simulatorConfig.getSpeedMultiplier(), 0.1));
        simSpeedSpinner.setEditable(true);
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(6);
        g.add(new Label("Start temp (°C):"), 0, 0);
        g.add(simBtStartSpinner, 1, 0);
        g.add(new Label("Speed multiplier:"), 0, 1);
        g.add(simSpeedSpinner, 1, 1);
        return new VBox(6, g);
    }

    private Node buildAillioR1Panel() {
        aillioVidField = new TextField(String.format("0x%04X", aillioR1Config.getVid()));
        aillioVidField.setPromptText("0x0483");
        aillioPidField = new TextField(String.format("0x%04X", aillioR1Config.getPid()));
        aillioPidField.setPromptText("0x5741");
        scanHidButton = new Button("Scan HID");
        scanHidButton.setOnAction(e -> testResultLabel.setText("Use Test Connection to verify."));
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(6);
        g.add(new Label("VID:"), 0, 0);
        g.add(aillioVidField, 1, 0);
        g.add(new Label("PID:"), 0, 1);
        g.add(aillioPidField, 1, 1);
        g.add(scanHidButton, 0, 2, 2, 1);
        return new VBox(6, g);
    }

    private void syncFromUiToConfigs() {
        DeviceType t = deviceTypeCombo.getValue();
        if (t != null && (t.isRequiresSerial() && !t.isRequiresModbus() || isStubType(t))) {
            if (serialPortCombo != null) {
                String p = serialPortCombo.getValue() != null ? serialPortCombo.getValue() : (serialPortCombo.getEditor().getText() != null ? serialPortCombo.getEditor().getText().trim() : "");
                serialPortConfig.setPortName(p);
            }
            if (baudCombo != null) serialPortConfig.setBaudRate(baudCombo.getValue() != null ? baudCombo.getValue() : 115200);
        }
        if (t != null && t.isRequiresModbus() && modbusHostField != null) {
            modbusPortConfig.setUseTcp(modbusTcpRadio != null && modbusTcpRadio.isSelected());
            modbusPortConfig.setHost(modbusHostField.getText() != null ? modbusHostField.getText().trim() : "");
            if (modbusPortSpinner != null) modbusPortConfig.setPort(modbusPortSpinner.getValue());
            if (slaveIdSpinner != null) modbusPortConfig.setSlaveId(slaveIdSpinner.getValue());
            if (btRegSpinner != null) modbusPortConfig.setBtRegister(btRegSpinner.getValue());
            if (etRegSpinner != null) modbusPortConfig.setEtRegister(etRegSpinner.getValue());
            if (scaleSpinner != null) modbusPortConfig.setScale(scaleSpinner.getValue());
        }
        if (t == DeviceType.SIMULATOR && simBtStartSpinner != null) {
            simulatorConfig.setBtStartTemp(simBtStartSpinner.getValue());
            if (simSpeedSpinner != null) simulatorConfig.setSpeedMultiplier(simSpeedSpinner.getValue());
        }
        if (t == DeviceType.AILLIO_R1 && aillioVidField != null) {
            try {
                aillioR1Config.setVid(Integer.decode(aillioVidField.getText().trim()));
            } catch (NumberFormatException ignored) {}
            try {
                aillioR1Config.setPid(Integer.decode(aillioPidField.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
    }

    private void onTestConnection(ActionEvent e) {
        syncFromUiToConfigs();
        testResultLabel.setText("Testing...");
        DeviceType type = deviceTypeCombo.getValue();
        if (type == null || type == DeviceType.NONE) {
            testResultLabel.setText("Select a device.");
            return;
        }
        DeviceChannel ch = DeviceManager.createChannel(type, serialPortConfig, modbusPortConfig);
        try {
            ch.open();
            try {
                var result = ch.read();
                if (result != null && (Double.isFinite(result.bt()) || Double.isFinite(result.et()))) {
                    testResultLabel.setText(String.format("BT: %.1f°C  ET: %.1f°C", result.bt(), result.et()));
                } else {
                    testResultLabel.setText("Stub or no data.");
                }
            } finally {
                ch.close();
            }
        } catch (DeviceException ex) {
            testResultLabel.setText("Error: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
        } catch (Exception ex) {
            testResultLabel.setText("Error: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
        }
    }

    @Override
    protected void onApply(ActionEvent e) {
        syncFromUiToConfigs();
        DeviceType type = deviceTypeCombo.getValue();
        deviceConfig.setActiveType(type != null ? type : DeviceType.NONE);
        deviceConfig.save();
        SerialPortConfig.saveToPreferences(serialPortConfig);
        ModbusPortConfig.saveToPreferences(modbusPortConfig);
        SimulatorConfig.saveToPreferences(simulatorConfig);
        AillioR1Config.saveToPreferences(aillioR1Config);
        appController.setDevice(type, serialPortConfig, modbusPortConfig);
    }

    @Override
    protected void onOk(ActionEvent e) {
        onApply(e);
        super.onOk(e);
    }

    /** Apply and persist settings without closing. Used when this dialog is embedded in unified Settings. */
    public void applyFromUI() {
        onApply(null);
    }

    public DeviceType getSelectedDeviceType() {
        return deviceTypeCombo != null ? deviceTypeCombo.getValue() : DeviceType.NONE;
    }
}
