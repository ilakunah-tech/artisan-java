package org.artisan.view;

import java.nio.file.Path;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import org.artisan.controller.AutoSave;

/**
 * Config » Autosave... dialog: enable, interval, path, prefix, add timestamp, save on DROP.
 */
public final class AutoSaveDialog extends ArtisanDialog {

    private final AutoSave autoSave;

    private CheckBox enabledCheck;
    private Spinner<Integer> intervalSpinner;
    private TextField savePathField;
    private TextField prefixField;
    private CheckBox addTimestampCheck;
    private CheckBox saveOnDropCheck;
    private CheckBox saveImageCheck;
    private ComboBox<String> imageFormatCombo;
    private CheckBox addToRecentCheck;
    private Label prefixPreviewLabel;

    public AutoSaveDialog(Window owner, AutoSave autoSave) {
        super(owner, true, false);
        this.autoSave = autoSave != null ? autoSave : new AutoSave();
        getStage().setTitle("Config » Autosave");
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        applyFromFields();
        super.onOk(e);
    }

    @Override
    protected Node buildContent() {
        enabledCheck = new CheckBox("Enable autosave");
        enabledCheck.setSelected(autoSave.isEnabled());

        intervalSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60,
            Math.max(1, Math.min(60, autoSave.getIntervalMinutes())), 1));
        intervalSpinner.setEditable(true);

        savePathField = new TextField(autoSave.getSavePath());
        savePathField.setPrefColumnCount(35);
        Button browseBtn = new Button("Browse...");
        browseBtn.setOnAction(e -> browseDirectory());

        prefixField = new TextField(autoSave.getPrefix());
        prefixField.setPrefColumnCount(20);

        addTimestampCheck = new CheckBox("Add timestamp to filename");
        addTimestampCheck.setSelected(autoSave.isAddTimestamp());

        saveOnDropCheck = new CheckBox("Save on DROP event");
        saveOnDropCheck.setSelected(autoSave.isSaveOnDrop());

        saveImageCheck = new CheckBox("Save also image:");
        saveImageCheck.setSelected(autoSave.isSaveImage());
        imageFormatCombo = new ComboBox<>();
        imageFormatCombo.getItems().addAll(org.artisan.controller.AutoSave.IMAGE_FORMATS);
        imageFormatCombo.setValue(autoSave.getImageFormat());
        imageFormatCombo.setDisable(!autoSave.isSaveImage());
        saveImageCheck.selectedProperty().addListener((obs, o, n) -> imageFormatCombo.setDisable(!n));

        addToRecentCheck = new CheckBox("Add to recent file list");
        addToRecentCheck.setSelected(autoSave.isAddToRecentFiles());
        addToRecentCheck.setTooltip(new javafx.scene.control.Tooltip("Add auto-saved filenames to the recent files list"));

        prefixPreviewLabel = new Label(buildPreview());
        prefixField.textProperty().addListener((obs, o, n) -> prefixPreviewLabel.setText(buildPreview()));
        addTimestampCheck.selectedProperty().addListener((obs, o, n) -> prefixPreviewLabel.setText(buildPreview()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        int row = 0;
        grid.add(enabledCheck, 0, row++, 2, 1);
        grid.add(new Label("Interval (minutes):"), 0, row);
        grid.add(intervalSpinner, 1, row++);
        grid.add(new Label("Save path:"), 0, row);
        grid.add(new HBox(8, savePathField, browseBtn), 1, row++);
        grid.add(new Label("Prefix:"), 0, row);
        grid.add(prefixField, 1, row++);
        grid.add(new Label("Preview:"), 0, row);
        grid.add(prefixPreviewLabel, 1, row++);
        grid.add(addTimestampCheck, 0, row++, 2, 1);
        grid.add(saveOnDropCheck, 0, row++, 2, 1);
        grid.add(saveImageCheck, 0, row);
        grid.add(imageFormatCombo, 1, row++);
        grid.add(addToRecentCheck, 0, row++, 2, 1);

        Button restoreBtn = new Button("Restore Defaults");
        restoreBtn.setOnAction(e -> restoreDefaults());

        VBox content = new VBox(12, grid, new HBox(restoreBtn));
        content.setPadding(new Insets(0, 10, 10, 10));
        return content;
    }

    private void browseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select autosave directory");
        String current = savePathField.getText();
        if (current != null && !current.isBlank()) {
            try {
                Path p = Path.of(current);
                if (p.toFile().isDirectory()) {
                    chooser.setInitialDirectory(p.toFile());
                }
            } catch (Exception ignored) {}
        }
        var dir = chooser.showDialog(getStage().getOwner());
        if (dir != null) {
            savePathField.setText(dir.getAbsolutePath());
        }
    }

    private void restoreDefaults() {
        autoSave.restoreDefaults();
        enabledCheck.setSelected(autoSave.isEnabled());
        intervalSpinner.getValueFactory().setValue(autoSave.getIntervalMinutes());
        savePathField.setText(autoSave.getSavePath());
        prefixField.setText(autoSave.getPrefix());
        addTimestampCheck.setSelected(autoSave.isAddTimestamp());
        saveOnDropCheck.setSelected(autoSave.isSaveOnDrop());
        saveImageCheck.setSelected(autoSave.isSaveImage());
        imageFormatCombo.setValue(autoSave.getImageFormat());
        imageFormatCombo.setDisable(!autoSave.isSaveImage());
        addToRecentCheck.setSelected(autoSave.isAddToRecentFiles());
        prefixPreviewLabel.setText(buildPreview());
    }

    private void applyFromFields() {
        autoSave.setEnabled(enabledCheck.isSelected());
        int interval = intervalSpinner.getValue();
        autoSave.setIntervalMinutes(interval);
        autoSave.setSavePath(savePathField.getText() != null ? savePathField.getText().trim() : "");
        autoSave.setPrefix(prefixField.getText() != null ? prefixField.getText().trim() : "autosave");
        autoSave.setAddTimestamp(addTimestampCheck.isSelected());
        autoSave.setSaveOnDrop(saveOnDropCheck.isSelected());
        autoSave.setSaveImage(saveImageCheck.isSelected());
        autoSave.setImageFormat(imageFormatCombo.getValue());
        autoSave.setAddToRecentFiles(addToRecentCheck.isSelected());
        autoSave.save();
    }

    private String buildPreview() {
        String p = (prefixField != null && prefixField.getText() != null) ? prefixField.getText() : "autosave";
        boolean ts = (addTimestampCheck != null) && addTimestampCheck.isSelected();
        String name = p + "_<title>";
        if (ts) name += "_yyyyMMdd_HHmmss";
        return name + ".alog";
    }
}
