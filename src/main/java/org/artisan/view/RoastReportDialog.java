package org.artisan.view;

import java.nio.file.Path;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.model.CupProfile;
import org.artisan.model.PhaseResult;
import org.artisan.model.Phases;
import org.artisan.model.ProfileData;
import org.artisan.model.RoastProperties;
import org.artisan.model.RoastReport;
import org.artisan.model.RoastStats;
import org.artisan.model.Statistics;

/**
 * Roast → Roast Report... modal dialog. TextArea with generated report, Refresh / Copy / Save as .txt.
 */
public final class RoastReportDialog extends ArtisanDialog {

    private final AppController appController;
    private TextArea textArea;

    public RoastReportDialog(Window owner, AppController appController) {
        super(owner, true, false);
        this.appController = appController;
        getStage().setTitle("Roast Report");
    }

    @Override
    protected Node buildContent() {
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefColumnCount(40);
        textArea.setPrefRowCount(30);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        refreshReport();

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshReport());
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

        HBox buttons = new HBox(10, refreshBtn, copyBtn, saveBtn);
        buttons.setPadding(new Insets(8));

        BorderPane center = new BorderPane();
        center.setCenter(new ScrollPane(textArea));
        center.setBottom(buttons);
        BorderPane.setMargin(center.getCenter(), new Insets(8));
        return center;
    }

    private void refreshReport() {
        ProfileData pd = appController != null ? appController.getCurrentProfileData() : null;
        RoastProperties props = appController != null ? appController.getRoastProperties() : null;
        CupProfile cup = appController != null ? appController.getCupProfile() : null;
        PhaseResult phases = pd != null ? Phases.compute(pd) : null;
        RoastStats stats = pd != null ? Statistics.compute(pd) : null;
        String report = RoastReport.generate(pd, props != null ? props : new RoastProperties(), cup, phases, stats);
        textArea.setText(report);
    }
}
