package org.artisan.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.controller.CommController;
import org.artisan.device.BleDeviceChannel;
import org.artisan.device.BlePortConfig;
import org.artisan.device.DeviceChannel;
import org.artisan.device.DeviceException;
import org.artisan.device.DeviceManager;
import org.artisan.device.ModbusDeviceChannel;
import org.artisan.device.ModbusPortConfig;
import org.artisan.device.SerialDeviceChannel;
import org.artisan.device.SerialPortConfig;

import java.util.List;
import java.util.prefs.Preferences;

/**
 * Config » Ports... dialog: Serial | Modbus | BLE tabs. OK/Apply saves configs to Preferences
 * and if CommController is running, stops it, re-opens with new config, restarts.
 */
public final class PortsDialog extends ArtisanDialog {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "ports.";
    private static final int[] BaudRates = { 300, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400 };

    private final SerialPortConfig serialConfig;
    private final ModbusPortConfig modbusConfig;
    private final BlePortConfig bleConfig;
    private final CommController commController;
    private final double intervalSeconds;

    private TabPane tabPane;
    private ComboBox<String> serialPortCombo;
    private ComboBox<Integer> baudCombo;
    private Spinner<Integer> dataBitsSpinner;
    private ComboBox<String> stopBitsCombo;
    private ComboBox<String> parityCombo;
    private Spinner<Integer> readTimeoutSpinner;

    private RadioButton modbusTcpRadio;
    private RadioButton modbusRtuRadio;
    private TextField modbusHostField;
    private Spinner<Integer> modbusPortSpinner;
    private Spinner<Integer> slaveIdSpinner;
    private Spinner<Integer> btRegSpinner;
    private Spinner<Integer> etRegSpinner;
    private Spinner<Double> scaleSpinner;

    private TextField bleAddressField;
    private TextField bleServiceUuidField;
    private TextField bleCharUuidField;

    public PortsDialog(Window owner, SerialPortConfig serialConfig, ModbusPortConfig modbusConfig,
                       BlePortConfig bleConfig, CommController commController, double intervalSeconds) {
        super(owner, true, true);
        this.serialConfig = serialConfig != null ? serialConfig : new SerialPortConfig();
        this.modbusConfig = modbusConfig != null ? modbusConfig : new ModbusPortConfig();
        this.bleConfig = bleConfig != null ? bleConfig : new BlePortConfig();
        this.commController = commController;
        this.intervalSeconds = intervalSeconds;
        getStage().setTitle("Config » Ports");
        getApplyButton().setOnAction(e -> applyAndMaybeRestart(false));
    }

    @Override
    protected Node buildContent() {
        SerialPortConfig.loadFromPreferences(serialConfig);
        ModbusPortConfig.loadFromPreferences(modbusConfig);
        BlePortConfig.loadFromPreferences(bleConfig);

        Tab serialTab = new Tab("Serial", buildSerialTab());
        Tab modbusTab = new Tab("Modbus", buildModbusTab());
        Tab bleTab = new Tab("BLE", buildBleTab());
        tabPane = new TabPane(serialTab, modbusTab, bleTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        String active = Preferences.userRoot().node(PREFS_NODE).get(PREFIX + "activeType", "serial");
        if ("modbus".equals(active)) tabPane.getSelectionModel().select(1);
        else if ("ble".equals(active)) tabPane.getSelectionModel().select(2);
        else tabPane.getSelectionModel().select(0);

        VBox root = new VBox(10, tabPane);
        root.setPadding(new Insets(10));
        return root;
    }

    private Node buildSerialTab() {
        serialPortCombo = new ComboBox<>();
        serialPortCombo.setEditable(true);
        refreshSerialPorts();
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshSerialPorts());

        baudCombo = new ComboBox<>();
        baudCombo.getItems().addAll(300, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400);
        baudCombo.setValue(Integer.valueOf(serialConfig.getBaudRate()));
        if (!baudCombo.getItems().contains(serialConfig.getBaudRate())) {
            baudCombo.getItems().add(serialConfig.getBaudRate());
            baudCombo.setValue(serialConfig.getBaudRate());
        }

        dataBitsSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 8, serialConfig.getDataBits(), 1));
        dataBitsSpinner.setEditable(true);
        stopBitsCombo = new ComboBox<>();
        stopBitsCombo.getItems().addAll("1", "1.5", "2");
        stopBitsCombo.setValue(serialConfig.getStopBits() == 2 ? "2" : "1");
        parityCombo = new ComboBox<>();
        parityCombo.getItems().addAll("None", "Odd", "Even");
        parityCombo.setValue(serialConfig.getParity() == 0 ? "None" : serialConfig.getParity() == 1 ? "Odd" : "Even");
        readTimeoutSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 5000, serialConfig.getReadTimeoutMs(), 100));
        readTimeoutSpinner.setEditable(true);

        Button testSerialBtn = new Button("Test Connection");
        testSerialBtn.setOnAction(e -> testSerialConnection());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(new Label("Port:"), 0, row);
        grid.add(new HBox(8, serialPortCombo, refreshBtn), 1, row++);
        grid.add(new Label("Baud rate:"), 0, row);
        grid.add(baudCombo, 1, row++);
        grid.add(new Label("Data bits:"), 0, row);
        grid.add(dataBitsSpinner, 1, row++);
        grid.add(new Label("Stop bits:"), 0, row);
        grid.add(stopBitsCombo, 1, row++);
        grid.add(new Label("Parity:"), 0, row);
        grid.add(parityCombo, 1, row++);
        grid.add(new Label("Read timeout (ms):"), 0, row);
        grid.add(readTimeoutSpinner, 1, row++);
        grid.add(testSerialBtn, 0, row++, 2, 1);
        return new VBox(8, grid);
    }

    private void refreshSerialPorts() {
        List<String> ports = DeviceManager.scanSerialPorts();
        String current = serialPortCombo != null ? serialPortCombo.getEditor().getText() : serialConfig.getPortName();
        if (serialPortCombo != null) {
            serialPortCombo.getItems().setAll(ports);
            if (current != null && !current.isEmpty()) serialPortCombo.getSelectionModel().select(current);
            if (serialPortCombo.getSelectionModel().getSelectedItem() == null && !current.isEmpty()) {
                serialPortCombo.getEditor().setText(current);
            }
        }
    }

    private void testSerialConnection() {
        syncSerialFromUi();
        SerialDeviceChannel ch = new SerialDeviceChannel(serialConfig);
        try {
            ch.open();
            try {
                var result = ch.read();
                new Alert(Alert.AlertType.INFORMATION, String.format("BT: %.2f  ET: %.2f", result.bt(), result.et())).showAndWait();
            } finally {
                ch.close();
            }
        } catch (DeviceException ex) {
            new Alert(Alert.AlertType.ERROR, "Connection failed: " + ex.getMessage()).showAndWait();
        }
    }

    private Node buildModbusTab() {
        ToggleGroup modbusType = new ToggleGroup();
        modbusTcpRadio = new RadioButton("TCP");
        modbusRtuRadio = new RadioButton("RTU");
        modbusTcpRadio.setToggleGroup(modbusType);
        modbusRtuRadio.setToggleGroup(modbusType);
        modbusTcpRadio.setSelected(modbusConfig.isUseTcp());
        modbusRtuRadio.setSelected(!modbusConfig.isUseTcp());

        modbusHostField = new TextField(modbusConfig.getHost());
        modbusHostField.setPromptText(modbusConfig.isUseTcp() ? "Host (e.g. 192.168.1.1)" : "Serial port (e.g. COM3)");
        modbusPortSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, modbusConfig.getPort(), 1));
        modbusPortSpinner.setEditable(true);
        slaveIdSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, modbusConfig.getSlaveId(), 1));
        slaveIdSpinner.setEditable(true);
        btRegSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, modbusConfig.getBtRegister(), 1));
        btRegSpinner.setEditable(true);
        etRegSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, modbusConfig.getEtRegister(), 1));
        etRegSpinner.setEditable(true);
        scaleSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.001, 10.0, modbusConfig.getScale(), 0.001));
        scaleSpinner.setEditable(true);

        Button testModbusBtn = new Button("Test Connection");
        testModbusBtn.setOnAction(e -> testModbusConnection());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(new HBox(10, modbusTcpRadio, modbusRtuRadio), 0, row++, 2, 1);
        grid.add(new Label(modbusConfig.isUseTcp() ? "Host:" : "Serial port:"), 0, row);
        grid.add(modbusHostField, 1, row++);
        grid.add(new Label("Port (TCP):"), 0, row);
        grid.add(modbusPortSpinner, 1, row++);
        grid.add(new Label("Slave ID:"), 0, row);
        grid.add(slaveIdSpinner, 1, row++);
        grid.add(new Label("BT register:"), 0, row);
        grid.add(btRegSpinner, 1, row++);
        grid.add(new Label("ET register:"), 0, row);
        grid.add(etRegSpinner, 1, row++);
        grid.add(new Label("Scale:"), 0, row);
        grid.add(scaleSpinner, 1, row++);
        grid.add(testModbusBtn, 0, row++, 2, 1);
        return new VBox(8, grid);
    }

    private void testModbusConnection() {
        syncModbusFromUi();
        ModbusDeviceChannel ch = new ModbusDeviceChannel(modbusConfig);
        try {
            ch.open();
            try {
                var result = ch.read();
                new Alert(Alert.AlertType.INFORMATION, String.format("BT: %.2f  ET: %.2f", result.bt(), result.et())).showAndWait();
            } finally {
                ch.close();
            }
        } catch (DeviceException ex) {
            new Alert(Alert.AlertType.ERROR, "Connection failed: " + ex.getMessage()).showAndWait();
        }
    }

    private Node buildBleTab() {
        bleAddressField = new TextField(bleConfig.getDeviceAddress());
        bleAddressField.setPromptText("Device address");
        bleServiceUuidField = new TextField(bleConfig.getServiceUuid());
        bleServiceUuidField.setPromptText("Service UUID");
        bleCharUuidField = new TextField(bleConfig.getCharacteristicUuid());
        bleCharUuidField.setPromptText("Characteristic UUID");
        Button scanBtn = new Button("Scan...");
        scanBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "BLE scan not yet implemented").showAndWait());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(new Label("Device address:"), 0, row);
        grid.add(bleAddressField, 1, row++);
        grid.add(new Label("Service UUID:"), 0, row);
        grid.add(bleServiceUuidField, 1, row++);
        grid.add(new Label("Characteristic UUID:"), 0, row);
        grid.add(bleCharUuidField, 1, row++);
        grid.add(scanBtn, 0, row++, 2, 1);
        return new VBox(8, grid);
    }

    private void syncSerialFromUi() {
        if (serialPortCombo != null) {
            String p = serialPortCombo.getSelectionModel().getSelectedItem() != null
                ? serialPortCombo.getSelectionModel().getSelectedItem()
                : serialPortCombo.getEditor().getText();
            serialConfig.setPortName(p != null ? p.trim() : "");
        }
        if (baudCombo != null) serialConfig.setBaudRate(baudCombo.getValue() != null ? baudCombo.getValue() : SerialPortConfig.DEFAULT_BAUD_RATE);
        if (dataBitsSpinner != null) serialConfig.setDataBits(dataBitsSpinner.getValue());
        if (stopBitsCombo != null) serialConfig.setStopBits("2".equals(stopBitsCombo.getValue()) ? 2 : 1);
        if (parityCombo != null) {
            String par = parityCombo.getValue();
            serialConfig.setParity("Odd".equals(par) ? 1 : "Even".equals(par) ? 2 : 0);
        }
        if (readTimeoutSpinner != null) serialConfig.setReadTimeoutMs(readTimeoutSpinner.getValue());
    }

    private void syncSerialToUi() {
        if (serialPortCombo != null && serialConfig.getPortName() != null) {
            serialPortCombo.getEditor().setText(serialConfig.getPortName());
        }
        if (baudCombo != null) baudCombo.setValue(serialConfig.getBaudRate());
        if (dataBitsSpinner != null) dataBitsSpinner.getValueFactory().setValue(serialConfig.getDataBits());
        if (stopBitsCombo != null) stopBitsCombo.setValue(serialConfig.getStopBits() == 2 ? "2" : "1");
        if (parityCombo != null) parityCombo.setValue(serialConfig.getParity() == 1 ? "Odd" : serialConfig.getParity() == 2 ? "Even" : "None");
        if (readTimeoutSpinner != null) readTimeoutSpinner.getValueFactory().setValue(serialConfig.getReadTimeoutMs());
    }

    private void syncModbusFromUi() {
        if (modbusTcpRadio != null) modbusConfig.setUseTcp(modbusTcpRadio.isSelected());
        if (modbusHostField != null) modbusConfig.setHost(modbusHostField.getText() != null ? modbusHostField.getText().trim() : "");
        if (modbusPortSpinner != null) modbusConfig.setPort(modbusPortSpinner.getValue());
        if (slaveIdSpinner != null) modbusConfig.setSlaveId(slaveIdSpinner.getValue());
        if (btRegSpinner != null) modbusConfig.setBtRegister(btRegSpinner.getValue());
        if (etRegSpinner != null) modbusConfig.setEtRegister(etRegSpinner.getValue());
        if (scaleSpinner != null) modbusConfig.setScale(scaleSpinner.getValue());
    }

    private void syncBleFromUi() {
        if (bleAddressField != null) bleConfig.setDeviceAddress(bleAddressField.getText() != null ? bleAddressField.getText().trim() : "");
        if (bleServiceUuidField != null) bleConfig.setServiceUuid(bleServiceUuidField.getText() != null ? bleServiceUuidField.getText().trim() : "");
        if (bleCharUuidField != null) bleConfig.setCharacteristicUuid(bleCharUuidField.getText() != null ? bleCharUuidField.getText().trim() : "");
    }

    private void syncAllFromUi() {
        Tab selected = tabPane != null ? tabPane.getSelectionModel().getSelectedItem() : null;
        if (selected != null) {
            String title = selected.getText();
            if ("Serial".equals(title)) syncSerialFromUi();
            else if ("Modbus".equals(title)) syncModbusFromUi();
            else if ("BLE".equals(title)) syncBleFromUi();
        }
        syncSerialFromUi();
        syncModbusFromUi();
        syncBleFromUi();
    }

    private DeviceChannel buildChannelFromSelectedTab() {
        if (tabPane == null) return buildChannelFromType("serial");
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        String type = selected != null && "Modbus".equals(selected.getText()) ? "modbus" : selected != null && "BLE".equals(selected.getText()) ? "ble" : "serial";
        return buildChannelFromType(type);
    }

    private DeviceChannel buildChannelFromType(String type) {
        switch (type) {
            case "modbus":
                return new ModbusDeviceChannel(modbusConfig);
            case "ble":
                return new BleDeviceChannel(bleConfig);
            default:
                return new SerialDeviceChannel(serialConfig);
        }
    }

    private String getActiveTypeFromSelectedTab() {
        if (tabPane == null) return "serial";
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        if (selected != null && "Modbus".equals(selected.getText())) return "modbus";
        if (selected != null && "BLE".equals(selected.getText())) return "ble";
        return "serial";
    }

    private void applyAndMaybeRestart(boolean close) {
        syncAllFromUi();
        SerialPortConfig.saveToPreferences(serialConfig);
        ModbusPortConfig.saveToPreferences(modbusConfig);
        BlePortConfig.saveToPreferences(bleConfig);
        String activeType = getActiveTypeFromSelectedTab();
        Preferences.userRoot().node(PREFS_NODE).put(PREFIX + "activeType", activeType);

        if (commController != null) {
            boolean wasRunning = commController.isRunning();
            if (wasRunning) commController.stop();
            DeviceChannel ch = buildChannelFromSelectedTab();
            commController.setChannel(ch);
            if (wasRunning) commController.start(intervalSeconds);
        }
        if (close) getStage().close();
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        applyAndMaybeRestart(false);
        super.onOk(e);
    }
}
