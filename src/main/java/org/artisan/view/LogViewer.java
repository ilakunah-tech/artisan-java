package org.artisan.view;

import java.util.List;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import java.util.logging.Level;

/**
 * View » Log Viewer: non-modal Stage with read-only TextArea showing recent log entries
 * from InMemoryLogHandler. Polls every 2 seconds, filter by ALL/INFO/WARNING/SEVERE, Clear and Copy All.
 * Window size/pos in Preferences: logviewer.*
 */
public final class LogViewer {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "logviewer.";
    private static final int POLL_INTERVAL_SEC = 2;

    private final Stage stage;
    private final TextArea textArea;
    private final ComboBox<String> filterCombo;
    private final InMemoryLogHandler handler;
    private final Label errorCountLabel;
    private Timeline pollTimer;
    private volatile boolean serialLoggingEnabled = false;

    public LogViewer(Window owner) {
        stage = new Stage();
        stage.setTitle("Log Viewer");
        stage.initOwner(owner);
        stage.initModality(javafx.stage.Modality.NONE);

        handler = InMemoryLogHandler.getInstance();
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        textArea.setPrefRowCount(24);
        textArea.setPrefColumnCount(100);

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("ALL", "INFO", "WARNING", "SEVERE");
        filterCombo.setValue("ALL");
        filterCombo.setOnAction(e -> refresh());

        // Serial log ON/OFF — parity with Python serialLogDlg.serialcheckbox
        CheckBox serialCheck = new CheckBox("Serial Log ON/OFF");
        serialCheck.setTooltip(new javafx.scene.control.Tooltip("Enable/disable logging of serial communication"));
        serialCheck.setSelected(serialLoggingEnabled);
        serialCheck.selectedProperty().addListener((obs, o, n) -> serialLoggingEnabled = n);

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            if (handler != null) handler.clear();
            refresh();
        });
        Button copyBtn = new Button("Copy All");
        copyBtn.setOnAction(e -> {
            String text = textArea.getText();
            if (text != null && !text.isEmpty()) {
                ClipboardContent content = new ClipboardContent();
                content.putString(text);
                Clipboard.getSystemClipboard().setContent(content);
            }
        });

        // Error count label — parity with Python errorDlg.elabel
        errorCountLabel = new Label("Errors: 0");
        errorCountLabel.setStyle("-fx-font-weight: bold;");

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().addAll(filterCombo, serialCheck, clearBtn, copyBtn);

        HBox footer = new HBox(8, errorCountLabel);
        footer.setPadding(new Insets(4, 8, 4, 8));

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(textArea);
        root.setBottom(footer);
        BorderPane.setMargin(textArea, new Insets(8));

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 700, 500);
        stage.setScene(scene);

        loadPreferences();
        stage.setOnCloseRequest(e -> {
            if (pollTimer != null) pollTimer.stop();
            savePreferences();
        });

        refresh();
        startPollTimer();
    }

    private void startPollTimer() {
        pollTimer = new Timeline(new KeyFrame(Duration.seconds(POLL_INTERVAL_SEC), e -> refresh()));
        pollTimer.setCycleCount(Timeline.INDEFINITE);
        pollTimer.play();
    }

    private void refresh() {
        if (handler == null) return;
        List<String> records = handler.getRecords();
        String filter = filterCombo.getValue() != null ? filterCombo.getValue() : "ALL";
        Level minLevel = levelForFilter(filter);
        StringBuilder sb = new StringBuilder();
        for (String line : records) {
            if (filter.equals("ALL") || lineContainsLevel(line, minLevel)) {
                sb.append(line);
                if (!line.endsWith("\n")) sb.append('\n');
            }
        }
        String text = sb.toString();
        long errorCount = records.stream()
                .filter(r -> r != null && r.toUpperCase().contains("SEVERE"))
                .count();
        Platform.runLater(() -> {
            textArea.setText(text);
            textArea.setScrollTop(Double.MAX_VALUE);
            errorCountLabel.setText("Errors found: " + errorCount);
        });
    }

    private static Level levelForFilter(String filter) {
        if (filter == null) return Level.ALL;
        switch (filter) {
            case "SEVERE": return Level.SEVERE;
            case "WARNING": return Level.WARNING;
            case "INFO": return Level.INFO;
            default: return Level.ALL;
        }
    }

    private static boolean lineContainsLevel(String line, Level minLevel) {
        if (line == null || minLevel == Level.ALL) return true;
        String upper = line.toUpperCase();
        if (minLevel == Level.SEVERE && upper.contains("SEVERE")) return true;
        if (minLevel == Level.WARNING && (upper.contains("WARNING") || upper.contains("SEVERE"))) return true;
        if (minLevel == Level.INFO && (upper.contains("INFO") || upper.contains("WARNING") || upper.contains("SEVERE"))) return true;
        return false;
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

    public Stage getStage() {
        return stage;
    }

    /** Returns whether serial communication logging is currently enabled (parity with Python seriallogflag). */
    public boolean isSerialLoggingEnabled() {
        return serialLoggingEnabled;
    }

    /** Sets serial logging enabled state. */
    public void setSerialLoggingEnabled(boolean enabled) {
        this.serialLoggingEnabled = enabled;
    }

    private void loadPreferences() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            double x = p.getDouble(PREFIX + "x", Double.NaN);
            double y = p.getDouble(PREFIX + "y", Double.NaN);
            double w = p.getDouble(PREFIX + "width", Double.NaN);
            double h = p.getDouble(PREFIX + "height", Double.NaN);
            if (Double.isFinite(x)) stage.setX(x);
            if (Double.isFinite(y)) stage.setY(y);
            if (Double.isFinite(w) && w > 100) stage.setWidth(w);
            if (Double.isFinite(h) && h > 100) stage.setHeight(h);
        } catch (Exception ignored) {}
    }

    private void savePreferences() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            p.putDouble(PREFIX + "x", stage.getX());
            p.putDouble(PREFIX + "y", stage.getY());
            p.putDouble(PREFIX + "width", stage.getWidth());
            p.putDouble(PREFIX + "height", stage.getHeight());
        } catch (Exception ignored) {}
    }
}
