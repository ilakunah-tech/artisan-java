package org.artisan.view;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import org.artisan.device.S7Config;

/**
 * Config » S7 PLC... modal dialog: edit S7Config (host, rack, slot, port, DB numbers/offsets).
 * OK saves to Preferences; Cancel discards changes.
 */
public final class S7Dialog extends ArtisanDialog {

    private final S7Config config;
    private TextField hostField;
    private Spinner<Integer> rackSpinner;
    private Spinner<Integer> slotSpinner;
    private Spinner<Integer> portSpinner;
    private Spinner<Integer> btDbNumberSpinner;
    private Spinner<Integer> btDbOffsetSpinner;
    private Spinner<Integer> etDbNumberSpinner;
    private Spinner<Integer> etDbOffsetSpinner;

    public S7Dialog(Window owner, S7Config config) {
        super(owner, true, false);
        this.config = config != null ? config : new S7Config();
        getStage().setTitle("Config » S7 PLC");
    }

    @Override
    protected void onOk(ActionEvent e) {
        applyToConfig();
        config.save();
        super.onOk(e);
    }

    @Override
    protected Node buildContent() {
        S7Config.loadFromPreferences(config);

        hostField = new TextField(config.getHost());
        hostField.setPromptText("e.g. 192.168.1.10");
        hostField.setPrefColumnCount(16);

        rackSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 7, config.getRack(), 1));
        rackSpinner.setEditable(true);
        slotSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 31, config.getSlot(), 1));
        slotSpinner.setEditable(true);
        portSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, config.getPort(), 1));
        portSpinner.setEditable(true);

        btDbNumberSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16000, config.getBtDbNumber(), 1));
        btDbNumberSpinner.setEditable(true);
        btDbOffsetSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, config.getBtDbOffset(), 4));
        btDbOffsetSpinner.setEditable(true);
        etDbNumberSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16000, config.getEtDbNumber(), 1));
        etDbNumberSpinner.setEditable(true);
        etDbOffsetSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, config.getEtDbOffset(), 4));
        etDbOffsetSpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));
        int row = 0;
        grid.add(new Label("Host:"), 0, row);
        grid.add(hostField, 1, row++);
        grid.add(new Label("Rack:"), 0, row);
        grid.add(rackSpinner, 1, row++);
        grid.add(new Label("Slot:"), 0, row);
        grid.add(slotSpinner, 1, row++);
        grid.add(new Label("Port:"), 0, row);
        grid.add(portSpinner, 1, row++);
        grid.add(new Label("BT DB number:"), 0, row);
        grid.add(btDbNumberSpinner, 1, row++);
        grid.add(new Label("BT DB offset (bytes):"), 0, row);
        grid.add(btDbOffsetSpinner, 1, row++);
        grid.add(new Label("ET DB number:"), 0, row);
        grid.add(etDbNumberSpinner, 1, row++);
        grid.add(new Label("ET DB offset (bytes):"), 0, row);
        grid.add(etDbOffsetSpinner, 1, row++);

        return grid;
    }

    private void applyToConfig() {
        config.setHost(hostField.getText() != null ? hostField.getText().trim() : "");
        config.setRack(rackSpinner.getValue() != null ? rackSpinner.getValue() : 0);
        config.setSlot(slotSpinner.getValue() != null ? slotSpinner.getValue() : 1);
        config.setPort(portSpinner.getValue() != null ? portSpinner.getValue() : 102);
        config.setBtDbNumber(btDbNumberSpinner.getValue() != null ? btDbNumberSpinner.getValue() : 1);
        config.setBtDbOffset(btDbOffsetSpinner.getValue() != null ? btDbOffsetSpinner.getValue() : 0);
        config.setEtDbNumber(etDbNumberSpinner.getValue() != null ? etDbNumberSpinner.getValue() : 1);
        config.setEtDbOffset(etDbOffsetSpinner.getValue() != null ? etDbOffsetSpinner.getValue() : 4);
    }
}
