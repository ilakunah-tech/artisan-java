package org.artisan.view;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import org.artisan.controller.AlarmEngine;
import org.artisan.controller.AlarmListPersistence;
import org.artisan.model.Alarm;
import org.artisan.model.AlarmAction;
import org.artisan.model.AlarmCondition;
import org.artisan.model.AlarmList;

/**
 * Config » Alarms dialog: table of alarms with Add/Remove/Duplicate, inline editing, Test, OK/Cancel.
 * OK saves to ~/.artisan/alarms.json and runs onSaved (AppController reloads and redraws).
 */
public final class AlarmsDialog extends ArtisanDialog {

    private final AlarmList initialList;
    private final Runnable onSaved;
    private final AlarmEngine alarmEngine;
    private final ObservableList<Alarm> items;
    private TableView<Alarm> table;

    public AlarmsDialog(Window owner, AlarmList initialList, AlarmEngine alarmEngine, Runnable onSaved) {
        super(owner, true, false);
        this.initialList = initialList != null ? initialList : new AlarmList();
        this.alarmEngine = alarmEngine;
        this.onSaved = onSaved != null ? onSaved : () -> {};
        this.items = FXCollections.observableArrayList();
        for (int i = 0; i < this.initialList.size(); i++) {
            items.add(copy(this.initialList.get(i)));
        }
        getStage().setTitle("Config » Alarms");
    }

    private static Alarm copy(Alarm a) {
        return new Alarm(a);
    }

    @Override
    protected Node buildContent() {
        table = new TableView<>(items);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Alarm, Boolean> colEnabled = new TableColumn<>("On");
        colEnabled.setCellValueFactory(cb -> new javafx.beans.property.SimpleBooleanProperty(cb.getValue().isEnabled()));
        colEnabled.setCellFactory(tc -> new TableCell<Alarm, Boolean>() {
            private final CheckBox check = new CheckBox();
            { check.setOnAction(e -> { Alarm a = getTableRow().getItem(); if (a != null) a.setEnabled(check.isSelected()); }); }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                Alarm a = getTableRow().getItem();
                check.setSelected(a.isEnabled());
                setGraphic(check);
            }
        });
        colEnabled.setEditable(true);

        TableColumn<Alarm, Number> colNum = new TableColumn<>("#");
        colNum.setCellValueFactory(cb -> new javafx.beans.property.SimpleIntegerProperty(0));
        colNum.setCellFactory(tc -> new TableCell<Alarm, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) setText(null);
                else setText(String.valueOf(getIndex() + 1));
            }
        });
        colNum.setEditable(false);

        TableColumn<Alarm, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        colDesc.setEditable(true);
        colDesc.setOnEditCommit(e -> e.getRowValue().setDescription(e.getNewValue()));

        TableColumn<Alarm, AlarmCondition> colCond = new TableColumn<>("Condition");
        colCond.setCellValueFactory(new PropertyValueFactory<>("condition"));
        colCond.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(AlarmCondition.values())));
        colCond.setEditable(true);
        colCond.setOnEditCommit(e -> e.getRowValue().setCondition(e.getNewValue()));

        TableColumn<Alarm, Double> colThresh = new TableColumn<>("Threshold");
        colThresh.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        colThresh.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colThresh.setEditable(true);
        colThresh.setOnEditCommit(e -> e.getRowValue().setThreshold(e.getNewValue()));

        TableColumn<Alarm, AlarmAction> colAction = new TableColumn<>("Action");
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colAction.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(AlarmAction.values())));
        colAction.setEditable(true);
        colAction.setOnEditCommit(e -> e.getRowValue().setAction(e.getNewValue()));

        TableColumn<Alarm, String> colParam = new TableColumn<>("Param");
        colParam.setCellValueFactory(new PropertyValueFactory<>("actionParam"));
        colParam.setCellFactory(TextFieldTableCell.forTableColumn());
        colParam.setEditable(true);
        colParam.setOnEditCommit(e -> e.getRowValue().setActionParam(e.getNewValue()));

        TableColumn<Alarm, Boolean> colOnce = new TableColumn<>("Once");
        colOnce.setCellValueFactory(cb -> new javafx.beans.property.SimpleBooleanProperty(cb.getValue().isTriggerOnce()));
        colOnce.setCellFactory(tc -> new TableCell<Alarm, Boolean>() {
            private final CheckBox check = new CheckBox();
            { check.setOnAction(e -> { Alarm a = getTableRow().getItem(); if (a != null) a.setTriggerOnce(check.isSelected()); }); }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                Alarm a = getTableRow().getItem();
                check.setSelected(a.isTriggerOnce());
                setGraphic(check);
            }
        });
        colOnce.setEditable(true);

        TableColumn<Alarm, Integer> colGuard = new TableColumn<>("Guard");
        colGuard.setCellValueFactory(new PropertyValueFactory<>("guardAlarmIndex"));
        colGuard.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colGuard.setEditable(true);
        colGuard.setOnEditCommit(e -> e.getRowValue().setGuardAlarmIndex(e.getNewValue()));

        table.getColumns().addAll(colEnabled, colNum, colDesc, colCond, colThresh, colAction, colParam, colOnce, colGuard);

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> {
            items.add(new Alarm(true, "", AlarmCondition.BT_RISES_ABOVE, 200.0, AlarmAction.POPUP_MESSAGE, "", false, -1, false));
        });
        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(e -> {
            int idx = table.getSelectionModel().getSelectedIndex();
            if (idx >= 0) items.remove(idx);
        });
        Button duplicateBtn = new Button("Duplicate");
        duplicateBtn.setOnAction(e -> {
            Alarm sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) items.add(new Alarm(sel));
        });
        Button testBtn = new Button("Test");
        testBtn.setOnAction(e -> {
            Alarm sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && alarmEngine != null) alarmEngine.testAlarm(sel);
        });

        HBox buttons = new HBox(10, addBtn, removeBtn, duplicateBtn, testBtn);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox(10, new Label("Alarms (IF condition THEN action). Guard: index of alarm that must fire first (-1 = none)."), table, buttons);
        root.setPadding(new Insets(10));
        return root;
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        AlarmList toSave = new AlarmList();
        for (Alarm a : items) toSave.add(a);
        try {
            AlarmListPersistence.save(toSave);
        } catch (IOException ex) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Alarms");
            err.setHeaderText("Save failed");
            err.setContentText(ex.getMessage());
            err.showAndWait();
            return;
        }
        onSaved.run();
        super.onOk(e);
    }
}
