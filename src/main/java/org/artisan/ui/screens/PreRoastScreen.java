package org.artisan.ui.screens;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.artisan.controller.AppController;
import org.artisan.controller.CommController;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Pre-Roast (Setup) screen: two-column layout.
 * Left: profile selector (searchable), green coffee/batch, reference, Start Roast / Replay / Demo Mode.
 * Right: summary (profile, batch, machine, reference), validation warnings, quick settings, recent roasts.
 */
public final class PreRoastScreen {

    private static final String[] PROFILE_OPTIONS = { "(Default)", "Light", "Medium", "Dark", "City", "Full City", "Vienna", "French" };
    private static final String[] REF_OPTIONS = { "(None)", "Last roast", "Reference 1", "Reference 2" };

    private final GridPane root;
    private final Button startRoastBtn;
    private final Button demoModeBtn;
    private final Label validationLabel;
    private final Label connectionWarningLabel;
    private final VBox summaryCard;
    private final Label summaryProfile;
    private final Label summaryBatch;
    private final Label summaryMachine;
    private final Label summaryReference;
    private final VBox recentRoastsBox;
    private final ComboBox<String> profileCombo;
    private final TextField batchField;
    private final ComboBox<String> refCombo;

    private String machineName = "—";
    private Runnable onStartRoast;
    private Runnable onDemoMode;
    private Runnable onOpenReplay;
    private Consumer<Path> onOpenRecent;
    private Runnable onCurveVisibilitySync;
    private final AppController appController;
    private final UIPreferences uiPreferences;
    private final PreferencesStore preferencesStore;

    public PreRoastScreen(AppController appController, UIPreferences uiPreferences, PreferencesStore preferencesStore) {
        this.appController = appController;
        this.uiPreferences = uiPreferences != null ? uiPreferences : new UIPreferences();
        this.preferencesStore = preferencesStore;

        root = new GridPane();
        root.setHgap(24);
        root.setVgap(16);
        root.setPadding(new Insets(24));

        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setPercentWidth(50);
        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setPercentWidth(50);
        root.getColumnConstraints().addAll(leftCol, rightCol);

        int row = 0;

        // —— LEFT COLUMN ——
        Label leftTitle = new Label("Profile & batch");
        leftTitle.getStyleClass().add("section-title");
        root.add(leftTitle, 0, row++);

        root.add(new Label("Roast profile:"), 0, row);
        profileCombo = new ComboBox<>();
        profileCombo.setEditable(true);
        profileCombo.getItems().setAll(PROFILE_OPTIONS);
        profileCombo.getSelectionModel().select(0);
        profileCombo.setMaxWidth(Double.MAX_VALUE);
        makeSearchable(profileCombo, PROFILE_OPTIONS);
        root.add(profileCombo, 0, row++);

        root.add(new Label("Green coffee / lot:"), 0, row);
        TextField lotField = new TextField();
        lotField.setPromptText("Optional");
        lotField.setMaxWidth(Double.MAX_VALUE);
        root.add(lotField, 0, row++);

        root.add(new Label("Batch size (g):"), 0, row);
        batchField = new TextField();
        batchField.setPromptText("e.g. 500");
        batchField.setMaxWidth(Double.MAX_VALUE);
        root.add(batchField, 0, row++);

        root.add(new Label("Target roast level:"), 0, row);
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("(Optional)", "Light", "Medium", "Dark");
        levelCombo.getSelectionModel().select(0);
        levelCombo.setMaxWidth(Double.MAX_VALUE);
        root.add(levelCombo, 0, row++);

        root.add(new Label("Reference curve:"), 0, row);
        refCombo = new ComboBox<>();
        refCombo.getItems().addAll(REF_OPTIONS);
        refCombo.getSelectionModel().select(0);
        refCombo.setMaxWidth(Double.MAX_VALUE);
        root.add(refCombo, 0, row++);

        HBox leftButtons = new HBox(12);
        startRoastBtn = new Button("Start Roast");
        startRoastBtn.getStyleClass().add("ri5-primary-button");
        startRoastBtn.setTooltip(new Tooltip("Start sampling from connected device"));
        Button replayBtn = new Button("Replay...");
        replayBtn.setTooltip(new Tooltip("Configure event replay (Config → Replay)"));
        demoModeBtn = new Button("Demo Mode");
        demoModeBtn.setTooltip(new Tooltip("Run with synthetic data"));
        leftButtons.getChildren().addAll(startRoastBtn, replayBtn, demoModeBtn);
        root.add(leftButtons, 0, row++);

        startRoastBtn.setOnAction(e -> {
            if (onStartRoast != null) onStartRoast.run();
        });
        replayBtn.setOnAction(e -> {
            if (onOpenReplay != null) onOpenReplay.run();
        });
        demoModeBtn.setOnAction(e -> {
            if (onDemoMode != null) onDemoMode.run();
        });

        // —— RIGHT COLUMN ——
        int rightRow = 0;
        Label rightTitle = new Label("Summary & checks");
        rightTitle.getStyleClass().add("section-title");
        root.add(rightTitle, 1, rightRow++);

        summaryCard = new VBox(8);
        summaryCard.getStyleClass().add("ri5-summary-card");
        summaryCard.setPadding(new Insets(12));
        summaryProfile = new Label("Profile: —");
        summaryBatch = new Label("Batch: —");
        summaryMachine = new Label("Machine: —");
        summaryReference = new Label("Reference: —");
        summaryCard.getChildren().addAll(summaryProfile, summaryBatch, summaryMachine, summaryReference);
        root.add(summaryCard, 1, rightRow++);

        connectionWarningLabel = new Label("");
        connectionWarningLabel.getStyleClass().add("ri5-warning");
        connectionWarningLabel.setWrapText(true);
        root.add(connectionWarningLabel, 1, rightRow++);

        validationLabel = new Label("");
        validationLabel.getStyleClass().add("ri5-warning");
        validationLabel.setWrapText(true);
        root.add(validationLabel, 1, rightRow++);

        Label quickLabel = new Label("Quick settings");
        root.add(quickLabel, 1, rightRow++);
        CheckBox curveBT = new CheckBox("Show BT");
        curveBT.setSelected(uiPreferences.isVisibleBT());
        CheckBox curveET = new CheckBox("Show ET");
        curveET.setSelected(uiPreferences.isVisibleET());
        ComboBox<String> readoutCombo = new ComboBox<>(FXCollections.observableArrayList("S", "M", "L"));
        readoutCombo.getSelectionModel().select(uiPreferences.getReadoutSize().name());
        readoutCombo.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> densityCombo = new ComboBox<>(FXCollections.observableArrayList("Compact", "Comfortable"));
        densityCombo.getSelectionModel().select(uiPreferences.getDensity().name());
        densityCombo.setMaxWidth(Double.MAX_VALUE);
        VBox quickBox = new VBox(4, curveBT, curveET, new Label("Readout size:"), readoutCombo, new Label("Density:"), densityCombo);
        root.add(quickBox, 1, rightRow++);

        curveBT.selectedProperty().addListener((a, b, v) -> {
            uiPreferences.setVisibleBT(v);
            savePreferencesIfStore();
            if (onCurveVisibilitySync != null) onCurveVisibilitySync.run();
        });
        curveET.selectedProperty().addListener((a, b, v) -> {
            uiPreferences.setVisibleET(v);
            savePreferencesIfStore();
            if (onCurveVisibilitySync != null) onCurveVisibilitySync.run();
        });
        readoutCombo.getSelectionModel().selectedItemProperty().addListener((a, b, v) -> {
            if (v != null) {
                try {
                    uiPreferences.setReadoutSize(UIPreferences.ReadoutSize.valueOf(v));
                    savePreferencesIfStore();
                } catch (Exception ignored) {}
            }
        });
        densityCombo.getSelectionModel().selectedItemProperty().addListener((a, b, v) -> {
            if (v != null) {
                try {
                    uiPreferences.setDensity(UIPreferences.Density.valueOf(v.toUpperCase()));
                    savePreferencesIfStore();
                } catch (Exception ignored) {}
            }
        });

        Label recentLabel = new Label("Recent roasts");
        root.add(recentLabel, 1, rightRow++);
        recentRoastsBox = new VBox(4);
        root.add(recentRoastsBox, 1, rightRow++);

        // Bind summary and validation
        ChangeListener<Object> summaryUpdater = (a, b, c) -> updateSummary();
        profileCombo.getSelectionModel().selectedItemProperty().addListener(summaryUpdater);
        batchField.textProperty().addListener(summaryUpdater);
        refCombo.getSelectionModel().selectedItemProperty().addListener(summaryUpdater);
        batchField.textProperty().addListener((a, b, c) -> validate());

        updateSummary();
        validate();
        refreshConnectionWarning();
        refreshRecentRoasts();
    }

    private void savePreferencesIfStore() {
        if (preferencesStore != null) {
            preferencesStore.save(uiPreferences);
        }
    }

    private void makeSearchable(ComboBox<String> combo, String[] allItems) {
        combo.setEditable(true);
        final boolean[] updating = { false };
        combo.getEditor().textProperty().addListener((obs, old, text) -> {
            if (text == null || updating[0]) return;
            String lower = text.toLowerCase().trim();
            List<String> filtered = new ArrayList<>();
            for (String s : allItems) {
                if (lower.isEmpty() || s.toLowerCase().contains(lower)) filtered.add(s);
            }
            updating[0] = true;
            combo.getItems().setAll(filtered);
            combo.getEditor().setText(text);
            combo.getEditor().positionCaret(text.length());
            updating[0] = false;
        });
    }

    private void updateSummary() {
        String profile = profileCombo.getSelectionModel().getSelectedItem();
        summaryProfile.setText("Profile: " + (profile != null && !profile.isEmpty() ? profile : "(Default)"));
        String batch = batchField.getText();
        summaryBatch.setText("Batch: " + (batch != null && !batch.trim().isEmpty() ? batch.trim() + " g" : "—"));
        summaryMachine.setText("Machine: " + (machineName != null && !machineName.isBlank() ? machineName : "—"));
        String ref = refCombo.getSelectionModel().getSelectedItem();
        summaryReference.setText("Reference: " + (ref != null && !ref.isEmpty() ? ref : "(None)"));
    }

    private void validate() {
        String t = batchField.getText();
        if (t == null || t.trim().isEmpty()) {
            validationLabel.setText("Batch size required for Start Roast.");
            startRoastBtn.setDisable(true);
        } else {
            try {
                double v = Double.parseDouble(t.trim());
                if (v <= 0 || v > 10000) {
                    validationLabel.setText("Batch size should be between 1 and 10000 g.");
                    startRoastBtn.setDisable(true);
                } else {
                    validationLabel.setText("");
                    startRoastBtn.setDisable(false);
                }
            } catch (NumberFormatException e) {
                validationLabel.setText("Enter a valid number for batch size.");
                startRoastBtn.setDisable(true);
            }
        }
    }

    /** Call when screen is shown or after connection change to refresh connection warning and recent list. */
    public void refresh() {
        refreshConnectionWarning();
        refreshRecentRoasts();
    }

    public void refreshConnectionWarning() {
        CommController comm = appController != null ? appController.getCommController() : null;
        boolean running = comm != null && comm.isRunning();
        connectionWarningLabel.setText(running ? "" : "Machine disconnected. Connect in Settings to start roast.");
    }

    public void refreshRecentRoasts() {
        recentRoastsBox.getChildren().clear();
        if (appController == null || appController.getFileSession() == null) return;
        List<Path> recent = appController.getFileSession().getRecentFiles();
        for (Path p : recent) {
            String name = p.getFileName() != null ? p.getFileName().toString() : p.toString();
            Button b = new Button(name);
            b.setMaxWidth(Double.MAX_VALUE);
            b.getStyleClass().add("ri5-recent-file-button");
            Path path = p;
            b.setOnAction(e -> {
                if (onOpenRecent != null) onOpenRecent.accept(path);
            });
            recentRoastsBox.getChildren().add(b);
        }
        if (recent.isEmpty()) {
            Label empty = new Label("No recent files");
            empty.getStyleClass().add("ri5-muted");
            recentRoastsBox.getChildren().add(empty);
        }
    }

    public Pane getRoot() {
        return root;
    }

    public void setMachineName(String name) {
        this.machineName = name != null && !name.isBlank() ? name : "—";
        updateSummary();
    }

    public void setOnStartRoast(Runnable onStartRoast) {
        this.onStartRoast = onStartRoast;
    }

    public void setOnDemoMode(Runnable onDemoMode) {
        this.onDemoMode = onDemoMode;
    }

    public void setOnOpenReplay(Runnable onOpenReplay) {
        this.onOpenReplay = onOpenReplay;
    }

    public void setOnOpenRecent(Consumer<Path> onOpenRecent) {
        this.onOpenRecent = onOpenRecent;
    }

    /** Called when curve visibility (or other quick-display prefs) change so DisplaySettings can be synced. */
    public void setOnCurveVisibilitySync(Runnable onCurveVisibilitySync) {
        this.onCurveVisibilitySync = onCurveVisibilitySync;
    }
}
