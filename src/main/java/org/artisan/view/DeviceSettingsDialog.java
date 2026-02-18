package org.artisan.view;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import org.artisan.controller.AppSettings;
import org.artisan.device.DeviceManager;
import org.artisan.device.DevicePort;

import java.util.List;

/**
 * Device selection and port settings: type, port, baud, sampling rate, Test Connection.
 */
public final class DeviceSettingsDialog extends ArtisanDialog {

  private final AppSettings settings;
  private final ArtisanComboBox<String> deviceTypeCombo;
  private final ArtisanComboBox<String> portCombo;
  private final ArtisanComboBox<Integer> baudCombo;
  private final ArtisanSpinBox samplingRateSpin;
  private final Label testResultLabel;
  private final Button refreshButton;
  private final Button testButton;

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

    baudCombo = new ArtisanComboBox<>();
    baudCombo.getItems().addAll(BAUD_OPTIONS);
    baudCombo.setEditable(false);
    baudCombo.setSelectedValue(9600);
    samplingRateSpin = new ArtisanSpinBox(500, 10000, 2000);

    refreshButton = new Button("Refresh");
    refreshButton.setOnAction(e -> refreshPorts());

    testButton = new Button("Test Connection");
    testButton.setOnAction(this::onTestConnection);
    testResultLabel = new Label(" ");

    loadFromSettings();
  }

  @Override
  protected Node buildContent() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(8);
    int row = 0;
    grid.add(new Label("Device type:"), 0, row);
    grid.add(deviceTypeCombo, 1, row++);
    grid.add(new Label("Port:"), 0, row);
    grid.add(portCombo, 1, row);
    grid.add(refreshButton, 2, row++);
    grid.add(new Label("Baud rate:"), 0, row);
    grid.add(baudCombo, 1, row++);
    grid.add(new Label("Sampling (ms):"), 0, row);
    grid.add(samplingRateSpin, 1, row++);
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
    }
    int baud = settings.getBaudRate();
    Integer match = baudCombo.getItems().stream().filter(b -> b != null && b.intValue() == baud).findFirst().orElse(null);
    baudCombo.setSelectedValue(match != null ? match : 9600);
    samplingRateSpin.setIntValue(settings.getSamplingRateMs());
  }

  private void onTestConnection(ActionEvent e) {
    testResultLabel.setText("Testing...");
    String type = deviceTypeCombo.getSelectedValue() != null ? deviceTypeCombo.getSelectedValue() : "Simulator";
    String port = portCombo.getEditor() != null && portCombo.getEditor().getText() != null
        ? portCombo.getEditor().getText().trim()
        : (portCombo.getSelectedValue() != null ? portCombo.getSelectedValue() : "");
    int baud = baudCombo.getSelectedValue() != null ? baudCombo.getSelectedValue() : 9600;
    DevicePort device = DeviceManager.createDevice(type, port, baud);
    try {
      device.connect();
      double[] t = device.readTemperatures();
      device.disconnect();
      if (t != null && t.length >= 2) {
        double et = t[0];
        double bt = t[1];
        testResultLabel.setText(String.format("OK: BT=%.1f°C ET=%.1f°C", bt, et));
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
    settings.setLastDevicePort(portCombo.getEditor() != null ? portCombo.getEditor().getText().trim() : (portCombo.getSelectedValue() != null ? portCombo.getSelectedValue() : ""));
    settings.setBaudRate(baudCombo.getSelectedValue() != null ? baudCombo.getSelectedValue() : 9600);
    settings.setSamplingRateMs(samplingRateSpin.getIntValue());
    settings.save();
    super.onOk(e);
  }

  public String getSelectedDeviceType() {
    return deviceTypeCombo.getSelectedValue() != null ? deviceTypeCombo.getSelectedValue() : "Simulator";
  }

  public String getSelectedPort() {
    if (portCombo.getEditor() != null) return portCombo.getEditor().getText().trim();
    return portCombo.getSelectedValue() != null ? portCombo.getSelectedValue() : "";
  }

  public int getBaudRate() {
    return baudCombo.getSelectedValue() != null ? baudCombo.getSelectedValue() : 9600;
  }

  public int getSamplingRateMs() {
    return samplingRateSpin.getIntValue();
  }
}
