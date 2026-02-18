package org.artisan.view;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import org.artisan.controller.AppSettings;
import org.artisan.device.DeviceManager;
import org.artisan.device.DeviceOptions;
import org.artisan.device.DevicePort;

import java.util.List;

/**
 * Device selection and port settings: type, port/host, baud/TCP port, sampling rate,
 * Advanced Modbus options, Test Connection.
 */
public final class DeviceSettingsDialog extends ArtisanDialog {

  private final AppSettings settings;
  private final ArtisanComboBox<String> deviceTypeCombo;
  private final ArtisanComboBox<String> portCombo;
  private final TextField hostField;
  private final ArtisanSpinBox tcpPortSpin;
  private final ArtisanComboBox<Integer> baudCombo;
  private final ArtisanSpinBox samplingRateSpin;
  private final ArtisanSpinBox slaveIdSpin;
  private final ArtisanSpinBox btRegisterSpin;
  private final ArtisanSpinBox etRegisterSpin;
  private final TextField scaleFactorField;
  private final Label testResultLabel;
  private final Button refreshButton;
  private final Button testButton;

  private final Label portLabel;
  private final Node portComboNode;
  private final Node refreshNode;
  private final Label hostLabel;
  private final Node hostFieldNode;
  private final Label tcpPortLabel;
  private final Node tcpPortNode;
  private final TitledPane advancedPane;

  private static final Integer[] BAUD_OPTIONS = { 2400, 4800, 9600, 19200, 38400, 57600, 115200 };

  public DeviceSettingsDialog(Window owner, AppSettings settings) {
    super(owner, true, false);
    this.settings = settings;
    getStage().setTitle("Device Settings");

    deviceTypeCombo = new ArtisanComboBox<>();
    deviceTypeCombo.getItems().addAll(DeviceManager.getAvailableDeviceTypes());
    deviceTypeCombo.setEditable(false);

    portCombo = new ArtisanComboBox<>();
    portCombo.setEditable(true);
    refreshPorts();

    portLabel = new Label("Port:");
    portComboNode = portCombo;
    refreshButton = new Button("Refresh");
    refreshButton.setOnAction(e -> refreshPorts());
    refreshNode = refreshButton;

    hostLabel = new Label("Host / IP:");
    hostField = new TextField("192.168.1.1");
    hostField.setPromptText("192.168.1.1");
    hostFieldNode = hostField;

    tcpPortLabel = new Label("TCP Port:");
    tcpPortSpin = new ArtisanSpinBox(1, 65535, 502);
    tcpPortNode = tcpPortSpin;

    baudCombo = new ArtisanComboBox<>();
    baudCombo.getItems().addAll(BAUD_OPTIONS);
    baudCombo.setEditable(false);
    baudCombo.setSelectedValue(9600);
    samplingRateSpin = new ArtisanSpinBox(500, 10000, 2000);

    slaveIdSpin = new ArtisanSpinBox(1, 247, 1);
    btRegisterSpin = new ArtisanSpinBox(0, 9999, 1);
    etRegisterSpin = new ArtisanSpinBox(0, 9999, 2);
    scaleFactorField = new TextField("10.0");
    scaleFactorField.setPromptText("10.0");

    GridPane advancedGrid = new GridPane();
    advancedGrid.setHgap(10);
    advancedGrid.setVgap(6);
    advancedGrid.add(new Label("Slave ID:"), 0, 0);
    advancedGrid.add(slaveIdSpin, 1, 0);
    advancedGrid.add(new Label("BT Register:"), 0, 1);
    advancedGrid.add(btRegisterSpin, 1, 1);
    advancedGrid.add(new Label("ET Register:"), 0, 2);
    advancedGrid.add(etRegisterSpin, 1, 2);
    advancedGrid.add(new Label("Scale factor:"), 0, 3);
    advancedGrid.add(scaleFactorField, 1, 3);
    advancedPane = new TitledPane("Advanced...", advancedGrid);
    advancedPane.setCollapsible(true);
    advancedPane.setExpanded(false);

    testButton = new Button("Test Connection");
    testButton.setOnAction(this::onTestConnection);
    testResultLabel = new Label(" ");

    deviceTypeCombo.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> updatePortHostVisibility());

    loadFromSettings();
    updatePortHostVisibility();
  }

  private boolean isBescaTcp() {
    return DeviceManager.BESCA_TCP.equals(deviceTypeCombo.getSelectedValue());
  }

  private boolean isModbusDevice() {
    String t = deviceTypeCombo.getSelectedValue();
    return DeviceManager.BESCA_TCP.equals(t) || DeviceManager.BESCA_RTU.equals(t) || DeviceManager.DIEDRICH_RTU.equals(t);
  }

  private void updatePortHostVisibility() {
    boolean tcp = isBescaTcp();
    portLabel.setVisible(!tcp);
    portComboNode.setVisible(!tcp);
    refreshNode.setVisible(!tcp);
    hostLabel.setVisible(tcp);
    hostFieldNode.setVisible(tcp);
    tcpPortLabel.setVisible(tcp);
    tcpPortNode.setVisible(tcp);
    advancedPane.setVisible(isModbusDevice());
  }

  @Override
  protected Node buildContent() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(8);
    int row = 0;
    grid.add(new Label("Device type:"), 0, row);
    grid.add(deviceTypeCombo, 1, row++);
    grid.add(portLabel, 0, row);
    grid.add(portComboNode, 1, row);
    grid.add(refreshNode, 2, row++);
    grid.add(hostLabel, 0, row);
    grid.add(hostFieldNode, 1, row);
    row++;
    grid.add(tcpPortLabel, 0, row);
    grid.add(tcpPortNode, 1, row++);
    grid.add(new Label("Baud rate:"), 0, row);
    grid.add(baudCombo, 1, row++);
    grid.add(new Label("Sampling (ms):"), 0, row);
    grid.add(samplingRateSpin, 1, row++);
    grid.add(advancedPane, 0, row++, 2, 1);
    grid.add(testButton, 0, row);
    grid.add(testResultLabel, 1, row);
    return grid;
  }

  private void refreshPorts() {
    List<String> ports = DeviceManager.scanSerialPorts();
    String current = portCombo.getSelectedValue() != null ? portCombo.getSelectedValue().toString() : null;
    portCombo.getItems().clear();
    portCombo.getItems().addAll(ports);
    if (current != null && ports.contains(current)) {
      portCombo.setSelectedValue(current);
    } else if (!ports.isEmpty()) {
      portCombo.getSelectionModel().selectFirst();
    }
  }

  private void loadFromSettings() {
    String type = settings.getDeviceType();
    if (type != null && deviceTypeCombo.getItems().contains(type)) {
      deviceTypeCombo.setSelectedValue(type);
    } else {
      deviceTypeCombo.getSelectionModel().selectFirst();
    }
    String port = settings.getLastDevicePort();
    if (port != null && !port.isEmpty()) {
      if (!portCombo.getItems().contains(port)) {
        portCombo.getItems().add(port);
      }
      portCombo.setSelectedValue(port);
      hostField.setText(port);
    }
    int baud = settings.getBaudRate();
    Integer match = baudCombo.getItems().stream().filter(b -> b != null && b.intValue() == baud).findFirst().orElse(null);
    baudCombo.setSelectedValue(match != null ? match : 9600);
    samplingRateSpin.setIntValue(settings.getSamplingRateMs());
    tcpPortSpin.setIntValue(settings.getDeviceTcpPort());
    slaveIdSpin.setIntValue(settings.getModbusSlaveId());
    btRegisterSpin.setIntValue(settings.getModbusBtRegister());
    etRegisterSpin.setIntValue(settings.getModbusEtRegister());
    scaleFactorField.setText(String.valueOf(settings.getModbusScaleFactor()));
  }

  private DeviceOptions buildOptionsFromDialog() {
    double scale = 10.0;
    try {
      scale = Double.parseDouble(scaleFactorField.getText().trim());
      if (scale <= 0) scale = 10.0;
    } catch (NumberFormatException ignored) {}
    return new DeviceOptions(
        tcpPortSpin.getIntValue(),
        slaveIdSpin.getIntValue(),
        btRegisterSpin.getIntValue(),
        etRegisterSpin.getIntValue(),
        scale
    );
  }

  private void onTestConnection(ActionEvent e) {
    testResultLabel.setText("Testing...");
    String type = deviceTypeCombo.getSelectedValue() != null ? deviceTypeCombo.getSelectedValue() : "Simulator";
    String portOrHost;
    int baudOrTcpPort;
    if (isBescaTcp()) {
      portOrHost = hostField.getText() != null ? hostField.getText().trim() : "192.168.1.1";
      baudOrTcpPort = tcpPortSpin.getIntValue();
    } else {
      portOrHost = portCombo.getEditor() != null && portCombo.getEditor().getText() != null
          ? portCombo.getEditor().getText().trim()
          : (portCombo.getSelectedValue() != null ? portCombo.getSelectedValue() : "");
      baudOrTcpPort = baudCombo.getSelectedValue() != null ? baudCombo.getSelectedValue() : 9600;
    }
    DeviceOptions options = isModbusDevice() ? buildOptionsFromDialog() : null;
    DevicePort device = DeviceManager.createDevice(type, portOrHost, baudOrTcpPort, options);
    try {
      device.connect();
      double[] t = device.readTemperatures();
      device.disconnect();
      if (t != null && t.length >= 2) {
        double et = t[0];
        double bt = t[1];
        testResultLabel.setText(String.format("BT=%.1f°C  ET=%.1f°C", bt, et));
      } else {
        testResultLabel.setText("No data");
      }
    } catch (Exception ex) {
      testResultLabel.setText("Error: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
      try {
        device.disconnect();
      } catch (Exception ignored) {}
    }
  }

  @Override
  protected void onOk(ActionEvent e) {
    settings.setDeviceType(deviceTypeCombo.getSelectedValue() != null ? deviceTypeCombo.getSelectedValue() : "Simulator");
    if (isBescaTcp()) {
      settings.setLastDevicePort(hostField.getText() != null ? hostField.getText().trim() : "192.168.1.1");
      settings.setBaudRate(tcpPortSpin.getIntValue());
    } else {
      settings.setLastDevicePort(portCombo.getEditor() != null ? portCombo.getEditor().getText().trim() : (portCombo.getSelectedValue() != null ? portCombo.getSelectedValue() : ""));
      settings.setBaudRate(baudCombo.getSelectedValue() != null ? baudCombo.getSelectedValue() : 9600);
    }
    settings.setSamplingRateMs(samplingRateSpin.getIntValue());
    settings.setDeviceTcpPort(tcpPortSpin.getIntValue());
    settings.setModbusSlaveId(slaveIdSpin.getIntValue());
    settings.setModbusBtRegister(btRegisterSpin.getIntValue());
    settings.setModbusEtRegister(etRegisterSpin.getIntValue());
    try {
      settings.setModbusScaleFactor(Double.parseDouble(scaleFactorField.getText().trim()));
    } catch (NumberFormatException ex) {
      settings.setModbusScaleFactor(10.0);
    }
    settings.save();
    super.onOk(e);
  }

  public String getSelectedDeviceType() {
    return deviceTypeCombo.getSelectedValue() != null ? deviceTypeCombo.getSelectedValue() : "Simulator";
  }

  public String getSelectedPort() {
    if (isBescaTcp()) {
      return hostField.getText() != null ? hostField.getText().trim() : "192.168.1.1";
    }
    if (portCombo.getEditor() != null) return portCombo.getEditor().getText().trim();
    return portCombo.getSelectedValue() != null ? portCombo.getSelectedValue() : "";
  }

  public int getBaudRate() {
    if (isBescaTcp()) return tcpPortSpin.getIntValue();
    return baudCombo.getSelectedValue() != null ? baudCombo.getSelectedValue() : 9600;
  }

  public int getSamplingRateMs() {
    return samplingRateSpin.getIntValue();
  }
}
