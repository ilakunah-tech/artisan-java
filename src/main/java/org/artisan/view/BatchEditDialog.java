package org.artisan.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.artisan.model.Batch;

/**
 * Modal dialog for editing a single Batch. Used by BatchesDialog for New/Edit.
 */
public final class BatchEditDialog extends ArtisanDialog {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Batch batch;
    private final boolean isNew;

    private TextField titleField;
    private DatePicker datePicker;
    private Spinner<Double> greenSpinner;
    private Spinner<Double> roastedSpinner;
    private Spinner<Double> totalTimeSpinner;
    private TextField profilePathField;
    private TextArea notesArea;
    private CheckBox exportedCheckBox;

    public BatchEditDialog(Window owner, Batch batch, boolean isNew) {
        super(owner, true, false);
        this.batch = batch != null ? batch : new Batch();
        this.isNew = isNew;
        getStage().setTitle(isNew ? "New Batch" : "Edit Batch");
    }

    @Override
    protected Node buildContent() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        int row = 0;
        grid.add(new Label("Title:"), 0, row);
        titleField = new TextField(this.batch.getTitle());
        titleField.setPromptText("Batch title");
        grid.add(titleField, 1, row);
        row++;

        grid.add(new Label("Date:"), 0, row);
        datePicker = new DatePicker();
        try {
            if (this.batch.getDate() != null && !this.batch.getDate().isBlank()) {
                datePicker.setValue(LocalDate.parse(this.batch.getDate(), ISO));
            }
        } catch (DateTimeParseException ignored) {}
        grid.add(datePicker, 1, row);
        row++;

        grid.add(new Label("Green (g):"), 0, row);
        greenSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0, 10_000.0, this.batch.getGreenWeight(), 1.0));
        greenSpinner.setEditable(true);
        grid.add(greenSpinner, 1, row);
        row++;

        grid.add(new Label("Roasted (g):"), 0, row);
        roastedSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0, 10_000.0, this.batch.getRoastedWeight(), 1.0));
        roastedSpinner.setEditable(true);
        grid.add(roastedSpinner, 1, row);
        row++;

        grid.add(new Label("Total roast time (s):"), 0, row);
        totalTimeSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0, 3600.0, this.batch.getTotalRoastTimeSec(), 1.0));
        totalTimeSpinner.setEditable(true);
        grid.add(totalTimeSpinner, 1, row);
        row++;

        grid.add(new Label("Profile path:"), 0, row);
        HBox profileRow = new HBox(6);
        profilePathField = new TextField(this.batch.getProfilePath() != null ? this.batch.getProfilePath() : "");
        profilePathField.setPromptText(".alog file path");
        Button browseBtn = new Button("Browse");
        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select profile (.alog)");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Artisan log", "*.alog"));
            javafx.stage.Window w = getStage();
            java.io.File f = fc.showOpenDialog(w);
            if (f != null) profilePathField.setText(f.getAbsolutePath());
        });
        profileRow.getChildren().addAll(profilePathField, browseBtn);
        grid.add(profileRow, 1, row);
        row++;

        grid.add(new Label("Notes:"), 0, row);
        notesArea = new TextArea(this.batch.getNotes());
        notesArea.setPrefRowCount(2);
        notesArea.setWrapText(true);
        grid.add(notesArea, 1, row);
        row++;

        grid.add(new Label("Exported:"), 0, row);
        exportedCheckBox = new CheckBox();
        exportedCheckBox.setSelected(this.batch.isExported());
        grid.add(exportedCheckBox, 1, row);

        return grid;
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        String title = titleField.getText();
        if (title == null || title.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Title must not be empty.").showAndWait();
            return;
        }
        double green = greenSpinner.getValue();
        double roasted = roastedSpinner.getValue();
        if (green < roasted) {
            new Alert(Alert.AlertType.WARNING, "Green weight must be >= roasted weight.").showAndWait();
            return;
        }

        batch.setTitle(title.trim());
        batch.setDate(datePicker.getValue() != null ? datePicker.getValue().format(ISO) : "");
        batch.setGreenWeight(green);
        batch.setRoastedWeight(roasted);
        batch.setTotalRoastTimeSec(totalTimeSpinner.getValue());
        batch.setProfilePath(profilePathField.getText() != null && !profilePathField.getText().isBlank()
                ? profilePathField.getText().trim() : null);
        batch.setNotes(notesArea.getText() != null ? notesArea.getText() : "");
        batch.setExported(exportedCheckBox.isSelected());

        super.onOk(e);
    }
}
