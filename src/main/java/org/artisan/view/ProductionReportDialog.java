package org.artisan.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.model.BatchManager;
import org.artisan.model.ProductionReport;

/**
 * Roast → Production Report... modal dialog. From/To DatePickers, Generate, Copy, Save as .txt.
 */
public final class ProductionReportDialog extends ArtisanDialog {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final AppController appController;
    private DatePicker fromPicker;
    private DatePicker toPicker;
    private TextArea textArea;

    public ProductionReportDialog(Window owner, AppController appController) {
        super(owner, true, false);
        this.appController = appController;
        getStage().setTitle("Production Report");
    }

    @Override
    protected Node buildContent() {
        fromPicker = new DatePicker();
        toPicker = new DatePicker();
        LocalDate now = LocalDate.now();
        fromPicker.setValue(now.minusMonths(1));
        toPicker.setValue(now);

        Button generateBtn = new Button("Generate");
        generateBtn.setOnAction(e -> doGenerate());

        GridPane top = new GridPane();
        top.setHgap(10);
        top.setVgap(8);
        top.setPadding(new Insets(8));
        top.add(new Label("From:"), 0, 0);
        top.add(fromPicker, 1, 0);
        top.add(new Label("To:"), 0, 1);
        top.add(toPicker, 1, 1);
        top.add(generateBtn, 1, 2);

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefColumnCount(60);
        textArea.setPrefRowCount(25);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");

        Button copyBtn = new Button("Copy");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textArea.getText());
            clipboard.setContent(content);
        });
        Button saveBtn = new Button("Save as .txt");
        saveBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save report");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text", "*.txt"));
            java.io.File f = fc.showSaveDialog(getStage());
            if (f != null) {
                try {
                    Path path = f.toPath();
                    java.nio.file.Files.writeString(path, textArea.getText(), java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception ex) {
                    new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                            "Save failed: " + ex.getMessage()).showAndWait();
                }
            }
        });
        HBox buttons = new HBox(10, copyBtn, saveBtn);
        buttons.setPadding(new Insets(8));

        BorderPane center = new BorderPane();
        center.setTop(top);
        center.setCenter(new ScrollPane(textArea));
        center.setBottom(buttons);
        BorderPane.setMargin(center.getCenter(), new Insets(8));
        return center;
    }

    private void doGenerate() {
        BatchManager bm = appController != null ? appController.getBatchManager() : null;
        String from = fromPicker.getValue() != null ? fromPicker.getValue().format(ISO) : "";
        String to = toPicker.getValue() != null ? toPicker.getValue().format(ISO) : "";
        String report = ProductionReport.generate(bm, from, to);
        textArea.setText(report);
    }
}
