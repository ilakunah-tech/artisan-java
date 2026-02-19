package org.artisan.view;

import java.io.IOException;
import java.nio.file.Path;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.model.Batch;
import org.artisan.model.BatchManager;

/**
 * Roast → Batches... modal dialog. TableView of batches, toolbar New/Edit/Delete/Export CSV.
 * On open: batchManager.load(); on OK: batchManager.save().
 */
public final class BatchesDialog extends ArtisanDialog {

    private final AppController appController;
    private final BatchManager batchManager;
    private final ObservableList<Batch> items;
    private TableView<Batch> table;

    public BatchesDialog(Window owner, AppController appController) {
        super(owner, true, false);
        this.appController = appController;
        this.batchManager = appController != null ? appController.getBatchManager() : new BatchManager();
        this.items = FXCollections.observableArrayList();
        getStage().setTitle("Batches");
    }

    @Override
    protected Node buildContent() {
        batchManager.load();
        items.clear();
        items.addAll(batchManager.getBatches());

        table = new TableView<>(items);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new javafx.scene.control.Label("No batches. Click New Batch to add one."));

        TableColumn<Batch, Number> colNum = new TableColumn<>("#");
        colNum.setCellValueFactory(cb -> new javafx.beans.property.SimpleIntegerProperty(cb.getValue().getBatchNumber()));
        colNum.setPrefWidth(50);

        TableColumn<Batch, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Batch, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Batch, Number> colGreen = new TableColumn<>("Green (g)");
        colGreen.setCellValueFactory(cb -> new javafx.beans.property.SimpleDoubleProperty(cb.getValue().getGreenWeight()));

        TableColumn<Batch, Number> colRoasted = new TableColumn<>("Roasted (g)");
        colRoasted.setCellValueFactory(cb -> new javafx.beans.property.SimpleDoubleProperty(cb.getValue().getRoastedWeight()));

        TableColumn<Batch, String> colLoss = new TableColumn<>("Weight Loss (%)");
        colLoss.setCellValueFactory(cb -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f%%", cb.getValue().weightLossPercent())));

        TableColumn<Batch, Number> colColor = new TableColumn<>("Color");
        colColor.setCellValueFactory(cb -> new javafx.beans.property.SimpleIntegerProperty(cb.getValue().getRoastColor()));

        TableColumn<Batch, Number> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(cb -> new javafx.beans.property.SimpleDoubleProperty(cb.getValue().getTotalRoastTimeSec()));

        TableColumn<Batch, String> colNotes = new TableColumn<>("Notes");
        colNotes.setCellValueFactory(cb -> new javafx.beans.property.SimpleStringProperty(cb.getValue().getNotes()));

        TableColumn<Batch, Boolean> colExported = new TableColumn<>("Exported");
        colExported.setCellValueFactory(cb -> new javafx.beans.property.SimpleBooleanProperty(cb.getValue().isExported()));

        table.getColumns().addAll(colNum, colTitle, colDate, colGreen, colRoasted, colLoss, colColor, colTime, colNotes, colExported);

        Button newBtn = new Button("New Batch");
        newBtn.setOnAction(e -> {
            Batch b = new Batch();
            if (new BatchEditDialog(getStage(), b, true).showAndWait()) {
                batchManager.addBatch(b);
                items.clear();
                items.addAll(batchManager.getBatches());
            }
        });
        Button editBtn = new Button("Edit Batch");
        editBtn.setOnAction(e -> {
            Batch sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                new Alert(Alert.AlertType.INFORMATION, "Select a batch to edit.").showAndWait();
                return;
            }
            Batch copy = new Batch();
            copy.setBatchNumber(sel.getBatchNumber());
            copy.setTitle(sel.getTitle());
            copy.setDate(sel.getDate());
            copy.setGreenWeight(sel.getGreenWeight());
            copy.setRoastedWeight(sel.getRoastedWeight());
            copy.setTotalRoastTimeSec(sel.getTotalRoastTimeSec());
            copy.setRoastColor(sel.getRoastColor());
            copy.setProfilePath(sel.getProfilePath());
            copy.setNotes(sel.getNotes());
            copy.setExported(sel.isExported());
            if (new BatchEditDialog(getStage(), copy, false).showAndWait()) {
                batchManager.removeBatch(sel.getBatchNumber());
                batchManager.addBatch(copy);
                items.clear();
                items.addAll(batchManager.getBatches());
            }
        });
        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Batch sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                new Alert(Alert.AlertType.INFORMATION, "Select a batch to delete.").showAndWait();
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete batch #" + sel.getBatchNumber() + " \"" + sel.getTitle() + "\"?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                batchManager.removeBatch(sel.getBatchNumber());
                items.clear();
                items.addAll(batchManager.getBatches());
            }
        });
        Button exportCsvBtn = new Button("Export CSV");
        exportCsvBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export batches CSV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            java.io.File f = fc.showSaveDialog(getStage());
            if (f != null) {
                Path path = f.toPath();
                try {
                    batchManager.exportCsv(path);
                    new Alert(Alert.AlertType.INFORMATION, "Exported to " + path).showAndWait();
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).showAndWait();
                }
            }
        });

        ToolBar toolbar = new ToolBar(newBtn, editBtn, deleteBtn, exportCsvBtn);
        toolbar.getItems().forEach(n -> n.getStyleClass().add("tool-bar-button"));

        VBox center = new VBox(8, toolbar, table);
        center.setPadding(new Insets(8));
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
        return center;
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        try {
            batchManager.save();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage()).showAndWait();
            return;
        }
        super.onOk(e);
    }
}
