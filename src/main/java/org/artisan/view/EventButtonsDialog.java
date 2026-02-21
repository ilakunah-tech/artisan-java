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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.converter.DoubleStringConverter;

import org.artisan.controller.EventButtonConfigPersistence;
import org.artisan.model.AlarmAction;
import org.artisan.model.EventButtonConfig;
import org.artisan.model.EventType;

/**
 * Config » Events... dialog: table of programmable event buttons (up to 4).
 * Columns: Visible | Label | Description | Type | Value | Color | Action | Param.
 * OK saves to ~/.artisan/eventbuttons.json and runs onSaved (MainWindow rebuilds toolbar).
 */
public final class EventButtonsDialog extends ArtisanDialog {

    private static final int MAX_BUTTONS = 4;

    private final ObservableList<EventButtonConfig> items;
    private final Runnable onSaved;
    private TableView<EventButtonConfig> table;

    public EventButtonsDialog(Window owner, Runnable onSaved) {
        super(owner, true, false);
        this.onSaved = onSaved != null ? onSaved : () -> {};
        this.items = FXCollections.observableArrayList();
        for (EventButtonConfig c : EventButtonConfigPersistence.load()) {
            items.add(new EventButtonConfig(c));
        }
        getStage().setTitle("Config » Events");
    }

    @Override
    protected Node buildContent() {
        table = new TableView<>(items);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<EventButtonConfig, Boolean> colVisible = new TableColumn<>("Visible");
        colVisible.setCellValueFactory(cb -> new javafx.beans.property.SimpleBooleanProperty(cb.getValue().isVisible()));
        colVisible.setCellFactory(tc -> new TableCell<EventButtonConfig, Boolean>() {
            private final CheckBox check = new CheckBox();
            {
                check.setOnAction(e -> {
                    EventButtonConfig c = getTableRow().getItem();
                    if (c != null) c.setVisible(check.isSelected());
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                EventButtonConfig c = getTableRow().getItem();
                check.setSelected(c.isVisible());
                setGraphic(check);
            }
        });
        colVisible.setEditable(true);

        TableColumn<EventButtonConfig, String> colLabel = new TableColumn<>("Label");
        colLabel.setCellValueFactory(new PropertyValueFactory<>("label"));
        colLabel.setCellFactory(TextFieldTableCell.forTableColumn());
        colLabel.setEditable(true);
        colLabel.setOnEditCommit(e -> e.getRowValue().setLabel(e.getNewValue()));

        TableColumn<EventButtonConfig, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        colDesc.setEditable(true);
        colDesc.setOnEditCommit(e -> e.getRowValue().setDescription(e.getNewValue()));

        TableColumn<EventButtonConfig, EventType> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(EventType.values())));
        colType.setEditable(true);
        colType.setOnEditCommit(e -> e.getRowValue().setType(e.getNewValue()));

        TableColumn<EventButtonConfig, Double> colValue = new TableColumn<>("Value");
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colValue.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colValue.setEditable(true);
        colValue.setOnEditCommit(e -> e.getRowValue().setValue(e.getNewValue()));

        TableColumn<EventButtonConfig, Color> colColor = new TableColumn<>("Color");
        colColor.setCellValueFactory(cb -> new javafx.beans.property.SimpleObjectProperty<>(cb.getValue().getColor()));
        colColor.setCellFactory(tc -> new TableCell<EventButtonConfig, Color>() {
            private final javafx.scene.control.ColorPicker picker = new javafx.scene.control.ColorPicker();
            @Override
            protected void updateItem(Color item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                EventButtonConfig c = getTableRow().getItem();
                Color col = c.getColor();
                picker.setValue(col != null ? col : Color.GRAY);
                picker.setOnAction(ev -> {
                    Color chosen = picker.getValue();
                    if (chosen != null) c.setColor(chosen);
                });
                setGraphic(picker);
            }
        });
        colColor.setEditable(false);

        String[] actionNames = new String[AlarmAction.values().length + 1];
        actionNames[0] = "";
        for (int i = 0; i < AlarmAction.values().length; i++) {
            actionNames[i + 1] = AlarmAction.values()[i].name();
        }
        TableColumn<EventButtonConfig, String> colAction = new TableColumn<>("Action");
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colAction.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(actionNames)));
        colAction.setEditable(true);
        colAction.setOnEditCommit(e -> e.getRowValue().setAction(e.getNewValue() != null ? e.getNewValue() : ""));

        TableColumn<EventButtonConfig, String> colParam = new TableColumn<>("Param");
        colParam.setCellValueFactory(new PropertyValueFactory<>("actionParam"));
        colParam.setCellFactory(TextFieldTableCell.forTableColumn());
        colParam.setEditable(true);
        colParam.setOnEditCommit(e -> e.getRowValue().setActionParam(e.getNewValue()));

        table.getColumns().addAll(colVisible, colLabel, colDesc, colType, colValue, colColor, colAction, colParam);

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> {
            if (items.size() >= MAX_BUTTONS) return;
            items.add(new EventButtonConfig());
        });
        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(e -> {
            int idx = table.getSelectionModel().getSelectedIndex();
            if (idx >= 0) items.remove(idx);
        });
        Button duplicateBtn = new Button("Duplicate");
        duplicateBtn.setOnAction(e -> {
            EventButtonConfig sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && items.size() < MAX_BUTTONS) items.add(new EventButtonConfig(sel));
        });

        HBox buttons = new HBox(10, addBtn, removeBtn, duplicateBtn);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox(10, new Label("Programmable event buttons (max " + MAX_BUTTONS + "). Label max 10 chars."), table, buttons);
        root.setPadding(new Insets(10));
        return root;
    }

    private static String toHex(Color c) {
        if (c == null) return "#808080";
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    /** Returns a copy of the current list (for toolbar rebuild). */
    public java.util.List<EventButtonConfig> getConfigs() {
        return new java.util.ArrayList<>(items);
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        java.util.List<EventButtonConfig> toSave = new java.util.ArrayList<>();
        for (int i = 0; i < items.size() && i < MAX_BUTTONS; i++) {
            toSave.add(items.get(i));
        }
        try {
            EventButtonConfigPersistence.save(toSave);
        } catch (IOException ex) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Events");
            err.setHeaderText("Save failed");
            err.setContentText(ex.getMessage());
            err.showAndWait();
            return;
        }
        onSaved.run();
        super.onOk(e);
    }

    /** Apply and persist settings without closing. Used when this dialog is embedded in unified Settings. */
    public boolean applyFromUI() {
        java.util.List<EventButtonConfig> toSave = new java.util.ArrayList<>();
        for (int i = 0; i < items.size() && i < MAX_BUTTONS; i++) {
            toSave.add(items.get(i));
        }
        try {
            EventButtonConfigPersistence.save(toSave);
        } catch (IOException ex) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Events");
            err.setHeaderText("Save failed");
            err.setContentText(ex.getMessage());
            err.showAndWait();
            return false;
        }
        onSaved.run();
        return true;
    }
}
