package org.artisan.view;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.model.Batch;
import org.artisan.model.BatchManager;
import org.artisan.model.RankingReport;

/**
 * Roast → Ranking Report... modal. Top N spinner, Generate, TextArea, Copy, Save as .txt.
 */
public final class RankingReportDialog extends ArtisanDialog {

    private static final int DEFAULT_TOP_N = 20;
    private static final int MIN_TOP_N = 1;
    private static final int MAX_TOP_N = 100;

    private final AppController appController;
    private Spinner<Integer> topNSpinner;
    private TextArea textArea;

    public RankingReportDialog(Window owner, AppController appController) {
        super(owner, true, false);
        this.appController = appController;
        getStage().setTitle("Ranking Report");
    }

    @Override
    protected Node buildContent() {
        topNSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_TOP_N, MAX_TOP_N, DEFAULT_TOP_N, 1));
        topNSpinner.setEditable(true);

        Button generateBtn = new Button("Generate");
        generateBtn.setOnAction(e -> doGenerate());

        HBox top = new HBox(10, new javafx.scene.control.Label("Top N batches:"), topNSpinner, generateBtn);
        top.setPadding(new Insets(8));

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefColumnCount(40);
        textArea.setPrefRowCount(20);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");

        doGenerate();

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
                    Files.writeString(path, textArea.getText(), StandardCharsets.UTF_8);
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
        BatchManager bm = appController != null ? appController.getBatchManager() : new BatchManager();
        bm.load();
        List<Batch> all = bm.getBatches();
        List<Batch> sorted = new ArrayList<>(all);
        sorted.sort(
                Comparator.comparingInt(Batch::getRoastColor).reversed()
                        .thenComparing(Comparator.comparingDouble(Batch::weightLossPercent)));
        int n = Math.min(topNSpinner.getValue(), sorted.size());
        List<Batch> topN = n <= 0 ? List.of() : sorted.subList(0, n);
        String report = RankingReport.generate(topN, bm);
        textArea.setText(report);
    }
}
