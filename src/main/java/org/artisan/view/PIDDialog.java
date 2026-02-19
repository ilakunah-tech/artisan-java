package org.artisan.view;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import org.artisan.controller.AppController;
import org.artisan.controller.PIDControl;
import org.artisan.model.PIDConfig;
import org.artisan.model.PIDMode;
import org.artisan.model.RampSoakProgram;
import org.artisan.model.RampSoakSegment;

/**
 * Config » PID dialog: PID Settings and Ramp/Soak tabs.
 * OK/Apply: save PIDConfig + RampSoakProgram, then pidControl.loadConfig().
 */
public final class PIDDialog extends ArtisanDialog {

    private final AppController appController;
    private PIDControl pidControl;
    private PIDConfig config;
    private RampSoakProgram rampSoakProgram;

    private CheckBox enablePidCheck;
    private Spinner<Double> kpSpinner;
    private Spinner<Double> kiSpinner;
    private Spinner<Double> kdSpinner;
    private ComboBox<PIDMode> modeCombo;
    private TextField manualSetpointField;
    private Label currentSetpointLabel;
    private Timeline setpointTimeline;

    private ObservableList<RampSoakRow> segmentRows;
    private TableView<RampSoakRow> segmentTable;

    public PIDDialog(Window owner, AppController appController) {
        super(owner, true, true);
        this.appController = appController;
        getStage().setTitle("Config » PID");
        getApplyButton().setOnAction(e -> apply(false));
    }

    /** Mutable row for editable TableView. */
    public static class RampSoakRow {
        private final IntegerProperty rampSeconds = new SimpleIntegerProperty(0);
        private final IntegerProperty soakSeconds = new SimpleIntegerProperty(0);
        private final DoubleProperty targetTemp = new SimpleDoubleProperty(0.0);

        public RampSoakRow(int rampSeconds, int soakSeconds, double targetTemp) {
            this.rampSeconds.set(rampSeconds);
            this.soakSeconds.set(soakSeconds);
            this.targetTemp.set(targetTemp);
        }

        public static RampSoakRow fromSegment(RampSoakSegment seg) {
            return new RampSoakRow(seg.rampSeconds(), seg.soakSeconds(), seg.targetTemp());
        }

        public RampSoakSegment toSegment() {
            return new RampSoakSegment(rampSeconds.get(), soakSeconds.get(), targetTemp.get());
        }

        public IntegerProperty rampSecondsProperty() { return rampSeconds; }
        public IntegerProperty soakSecondsProperty() { return soakSeconds; }
        public DoubleProperty targetTempProperty() { return targetTemp; }
    }

    @Override
    protected Node buildContent() {
        pidControl = appController.getPidControl();
        config = pidControl.getConfig();
        rampSoakProgram = pidControl.getRampSoakProgram();
        config.load();
        rampSoakProgram.load();

        TabPane tabs = new TabPane();
        tabs.getTabs().add(buildPidSettingsTab());
        tabs.getTabs().add(buildRampSoakTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        currentSetpointLabel = new Label("Current setpoint: — °C");
        setpointTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCurrentSetpointLabel()));
        setpointTimeline.setCycleCount(Timeline.INDEFINITE);

        VBox root = new VBox(10, tabs, currentSetpointLabel);
        root.setPadding(new Insets(10));
        return root;
    }

    @Override
    public boolean showAndWait() {
        boolean result = super.showAndWait();
        if (setpointTimeline != null) {
            setpointTimeline.stop();
        }
        return result;
    }

    private Tab buildPidSettingsTab() {
        Tab tab = new Tab("PID Settings");
        enablePidCheck = new CheckBox("Enable PID");
        enablePidCheck.setSelected(config.isEnabled());
        enablePidCheck.setOnAction(e -> {
            if (enablePidCheck.isSelected()) {
                pidControl.start();
            } else {
                pidControl.stop();
            }
        });

        kpSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, config.getKp(), 0.1));
        kpSpinner.setEditable(true);
        kiSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, config.getKi(), 0.001));
        kiSpinner.setEditable(true);
        kdSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, config.getKd(), 0.1));
        kdSpinner.setEditable(true);

        modeCombo = new ComboBox<>(FXCollections.observableArrayList(PIDMode.values()));
        modeCombo.getSelectionModel().select(config.getMode());
        manualSetpointField = new TextField(String.format("%.1f", config.getSetpoint()));
        manualSetpointField.setPromptText("°C");
        manualSetpointField.setDisable(config.getMode() != PIDMode.MANUAL);
        modeCombo.setOnAction(e -> {
            PIDMode m = modeCombo.getSelectionModel().getSelectedItem();
            manualSetpointField.setDisable(m != PIDMode.MANUAL);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(enablePidCheck, 0, row++, 2, 1);
        grid.add(new Label("Kp:"), 0, row);
        grid.add(kpSpinner, 1, row++);
        grid.add(new Label("Ki:"), 0, row);
        grid.add(kiSpinner, 1, row++);
        grid.add(new Label("Kd:"), 0, row);
        grid.add(kdSpinner, 1, row++);
        grid.add(new Label("Mode:"), 0, row);
        grid.add(modeCombo, 1, row++);
        grid.add(new Label("Manual setpoint (°C):"), 0, row);
        grid.add(manualSetpointField, 1, row++);

        tab.setContent(new VBox(10, grid));
        return tab;
    }

    private Tab buildRampSoakTab() {
        Tab tab = new Tab("Ramp/Soak");
        segmentRows = FXCollections.observableArrayList();
        for (RampSoakSegment seg : rampSoakProgram.getSegments()) {
            segmentRows.add(RampSoakRow.fromSegment(seg));
        }

        segmentTable = new TableView<>(segmentRows);
        segmentTable.setEditable(true);
        segmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RampSoakRow, Number> colNum = new TableColumn<>("#");
        colNum.setCellValueFactory(cb -> null);
        colNum.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(String.valueOf(getTableRow().getIndex() + 1));
            }
        });
        colNum.setEditable(false);

        TableColumn<RampSoakRow, Double> colTarget = new TableColumn<>("Target (°C)");
        colTarget.setCellValueFactory(cb -> cb.getValue().targetTempProperty().asObject());
        colTarget.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colTarget.setOnEditCommit(e -> e.getRowValue().targetTempProperty().set(e.getNewValue() != null ? e.getNewValue() : 0.0));

        TableColumn<RampSoakRow, Integer> colRamp = new TableColumn<>("Ramp (s)");
        colRamp.setCellValueFactory(cb -> cb.getValue().rampSecondsProperty().asObject());
        colRamp.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colRamp.setOnEditCommit(e -> e.getRowValue().rampSecondsProperty().set(e.getNewValue() != null ? e.getNewValue() : 0));

        TableColumn<RampSoakRow, Integer> colSoak = new TableColumn<>("Soak (s)");
        colSoak.setCellValueFactory(cb -> cb.getValue().soakSecondsProperty().asObject());
        colSoak.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colSoak.setOnEditCommit(e -> e.getRowValue().soakSecondsProperty().set(e.getNewValue() != null ? e.getNewValue() : 0));

        segmentTable.getColumns().addAll(colNum, colTarget, colRamp, colSoak);

        Button addRow = new Button("Add Row");
        addRow.setOnAction(e -> segmentRows.add(new RampSoakRow(60, 0, 200.0)));
        Button removeRow = new Button("Remove Row");
        removeRow.setOnAction(e -> {
            int idx = segmentTable.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < segmentRows.size()) {
                segmentRows.remove(idx);
            }
        });
        Button moveUp = new Button("Move Up");
        moveUp.setOnAction(e -> {
            int idx = segmentTable.getSelectionModel().getSelectedIndex();
            if (idx > 0 && idx < segmentRows.size()) {
                RampSoakRow r = segmentRows.remove(idx);
                segmentRows.add(idx - 1, r);
                segmentTable.getSelectionModel().select(idx - 1);
            }
        });
        Button moveDown = new Button("Move Down");
        moveDown.setOnAction(e -> {
            int idx = segmentTable.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < segmentRows.size() - 1) {
                RampSoakRow r = segmentRows.remove(idx);
                segmentRows.add(idx + 1, r);
                segmentTable.getSelectionModel().select(idx + 1);
            }
        });
        Button resetProgram = new Button("Reset Program");
        resetProgram.setOnAction(e -> rampSoakProgram.reset());

        HBox buttons = new HBox(10, addRow, removeRow, moveUp, moveDown, resetProgram);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        tab.setContent(new VBox(10, segmentTable, buttons));
        return tab;
    }

    private void updateCurrentSetpointLabel() {
        if (pidControl == null || currentSetpointLabel == null) return;
        double sp = pidControl.getSetpoint();
        currentSetpointLabel.setText(String.format("Current setpoint: %.1f °C", sp));
    }

    private void syncFromConfig() {
        if (enablePidCheck != null) enablePidCheck.setSelected(config.isEnabled());
        if (kpSpinner != null) kpSpinner.getValueFactory().setValue(config.getKp());
        if (kiSpinner != null) kiSpinner.getValueFactory().setValue(config.getKi());
        if (kdSpinner != null) kdSpinner.getValueFactory().setValue(config.getKd());
        if (modeCombo != null) modeCombo.getSelectionModel().select(config.getMode());
        if (manualSetpointField != null) manualSetpointField.setText(String.format("%.3f", config.getSetpoint()));
    }

    private void syncToConfig() {
        config.setEnabled(enablePidCheck != null && enablePidCheck.isSelected());
        if (kpSpinner != null) config.setKp(kpSpinner.getValue());
        if (kiSpinner != null) config.setKi(kiSpinner.getValue());
        if (kdSpinner != null) config.setKd(kdSpinner.getValue());
        if (modeCombo != null) {
            PIDMode m = modeCombo.getSelectionModel().getSelectedItem();
            config.setMode(m != null ? m : PIDMode.MANUAL);
        }
        if (manualSetpointField != null) {
            try {
                config.setSetpoint(Double.parseDouble(manualSetpointField.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
    }

    private void apply(boolean close) {
        syncToConfig();
        List<RampSoakSegment> segs = new ArrayList<>();
        if (segmentRows != null) {
            for (RampSoakRow r : segmentRows) {
                segs.add(r.toSegment());
            }
        }
        rampSoakProgram.setSegments(segs);
        config.save();
        rampSoakProgram.save();
        pidControl.loadConfig();
        if (close) getStage().close();
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        apply(true);
        super.onOk(e);
    }

    @Override
    protected void initLayout() {
        super.initLayout();
        setpointTimeline.play();
    }
}
