package org.artisan.view;

import java.nio.file.Path;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.artisan.controller.BackgroundSettings;
import org.artisan.model.BackgroundProfile;
import org.artisan.model.ProfileData;
import org.artisan.model.Roastlog;
import org.artisan.model.RorCalculator;

/**
 * Config » Background dialog: enable and load a background (.alog) profile overlay.
 */
public final class BackgroundDialog extends ArtisanDialog {

  private static final String DELTA = "\u0394"; // Δ

  private final BackgroundSettings settings;
  private final RoastChartController chartController;
  private final Runnable onApplyCallback;

  private CheckBox enabledCheck;
  private TextField fileField;
  private Label previewLabel;
  private Spinner<Integer> alignOffsetSpinner;
  private CheckBox showEtCheck;
  private CheckBox showBtCheck;
  private CheckBox showDeltaEtCheck;
  private CheckBox showDeltaBtCheck;

  private BackgroundProfile pendingProfile;

  public BackgroundDialog(Window owner, BackgroundSettings settings, RoastChartController chartController, Runnable onApply) {
    super(owner, true, true);
    this.settings = settings != null ? settings : BackgroundSettings.load();
    this.chartController = chartController;
    this.onApplyCallback = onApply != null ? onApply : () -> {};
    getStage().setTitle("Config » Background");
    getApplyButton().setOnAction(e -> applyAndClose(false));
  }

  @Override
  protected Node buildContent() {
    pendingProfile = chartController != null ? chartController.getBackgroundProfile() : null;

    BackgroundSettings.Config c = settings.toConfig();

    enabledCheck = new CheckBox("Enable background profile");
    enabledCheck.setSelected(c.isEnabled());

    fileField = new TextField(c.getLastFilePath() != null ? c.getLastFilePath() : "");
    fileField.setPrefColumnCount(35);
    Button browseBtn = new Button("Browse...");
    browseBtn.setOnAction(e -> browseFile());

    Button loadBtn = new Button("Load");
    loadBtn.setOnAction(e -> loadProfileFromField());

    previewLabel = new Label(buildPreviewText(pendingProfile));

    alignOffsetSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(-300, 300,
        (int) Math.round(c.getAlignOffset()), 1));
    alignOffsetSpinner.setEditable(true);
    alignOffsetSpinner.setTooltip(new Tooltip("Shift background in time relative to foreground CHARGE"));

    showEtCheck = new CheckBox("BG ET");
    showEtCheck.setSelected(c.isShowBgET());
    showBtCheck = new CheckBox("BG BT");
    showBtCheck.setSelected(c.isShowBgBT());
    showDeltaEtCheck = new CheckBox("BG " + DELTA + "ET");
    showDeltaEtCheck.setSelected(c.isShowBgDeltaET());
    showDeltaBtCheck = new CheckBox("BG " + DELTA + "BT");
    showDeltaBtCheck.setSelected(c.isShowBgDeltaBT());

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(8);
    int row = 0;
    grid.add(enabledCheck, 0, row++, 3, 1);

    grid.add(new Label("File (.alog):"), 0, row);
    grid.add(fileField, 1, row);
    grid.add(browseBtn, 2, row++);

    HBox loadRow = new HBox(10, loadBtn, previewLabel);
    grid.add(loadRow, 1, row++, 2, 1);

    grid.add(new Label("Align offset (s):"), 0, row);
    grid.add(alignOffsetSpinner, 1, row++);

    grid.add(new Label("Show:"), 0, row);
    grid.add(new HBox(12, showEtCheck, showBtCheck, showDeltaEtCheck, showDeltaBtCheck), 1, row++, 2, 1);

    VBox root = new VBox(10, grid);
    root.setPadding(new Insets(10));
    return root;
  }

  private void browseFile() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Select background profile");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Artisan log (*.alog)", "*.alog"));
    java.io.File chosen = chooser.showOpenDialog(getStage());
    if (chosen != null) {
      fileField.setText(chosen.getAbsolutePath());
    }
  }

  private void loadProfileFromField() {
    String pathStr = fileField.getText() != null ? fileField.getText().trim() : "";
    if (pathStr.isEmpty()) {
      warn("Background", "No file selected", "Please select a .alog file to load.");
      return;
    }
    ProfileData pd = Roastlog.load(Path.of(pathStr));
    if (pd == null || pd.getTimex() == null || pd.getTimex().isEmpty()) {
      warn("Background", "Load failed", "Could not load background profile from the selected file.");
      return;
    }

    // Ensure delta lists exist (optional in file).
    int smooth = 5;
    try {
      var ror = new RorCalculator();
      var d1 = ror.computeRoRSmoothed(pd.getTimex(), pd.getTemp1(), smooth);
      var d2 = ror.computeRoRSmoothed(pd.getTimex(), pd.getTemp2(), smooth);
      RorCalculator.clampRoR(d1, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
      RorCalculator.clampRoR(d2, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
      pd.setDelta1(d1);
      pd.setDelta2(d2);
    } catch (Exception ignored) {
      // keep empty deltas
    }

    String title = pd.getTitle();
    if (title == null || title.isBlank()) {
      title = Path.of(pathStr).getFileName() != null ? Path.of(pathStr).getFileName().toString() : "Background";
    }
    pendingProfile = new BackgroundProfile(pd, title, true, alignOffsetSpinner != null ? alignOffsetSpinner.getValue() : 0.0);
    previewLabel.setText(buildPreviewText(pendingProfile));
  }

  private static String buildPreviewText(BackgroundProfile p) {
    if (p == null || p.isEmpty() || p.getProfileData() == null) return "No background loaded";
    ProfileData pd = p.getProfileData();
    double total = 0.0;
    if (pd.getTimex() != null && !pd.getTimex().isEmpty()) {
      total = pd.getTimex().get(pd.getTimex().size() - 1);
      if (!Double.isFinite(total)) total = 0.0;
    }
    String title = p.getTitle() != null && !p.getTitle().isBlank() ? p.getTitle() : "Background";
    return String.format("Loaded: %s (%s)", title, formatSeconds(total));
  }

  private static String formatSeconds(double sec) {
    int s = (int) Math.round(sec);
    int m = s / 60;
    int r = Math.abs(s % 60);
    return String.format("%d:%02d", m, r);
  }

  private void warn(String title, String header, String content) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.initOwner(getStage());
    alert.showAndWait();
  }

  @Override
  protected void onOk(javafx.event.ActionEvent e) {
    applyAndClose(true);
  }

  /** Apply and persist settings without closing. Used when this dialog is embedded in unified Settings. */
  public void applyFromUI() {
    applyAndClose(false);
  }

  private void applyAndClose(boolean close) {
    BackgroundSettings.Config c = new BackgroundSettings.Config();
    c.setEnabled(enabledCheck != null && enabledCheck.isSelected());
    c.setLastFilePath(fileField != null ? fileField.getText() : "");
    c.setAlignOffset(alignOffsetSpinner != null ? alignOffsetSpinner.getValue() : 0.0);
    c.setShowBgET(showEtCheck != null && showEtCheck.isSelected());
    c.setShowBgBT(showBtCheck != null && showBtCheck.isSelected());
    c.setShowBgDeltaET(showDeltaEtCheck != null && showDeltaEtCheck.isSelected());
    c.setShowBgDeltaBT(showDeltaBtCheck != null && showDeltaBtCheck.isSelected());
    settings.fromConfig(c);
    settings.save();

    if (pendingProfile != null) {
      pendingProfile.setAlignOffset(settings.getAlignOffset());
      pendingProfile.setVisible(settings.isEnabled());
    }

    if (chartController != null) {
      chartController.setBackgroundSettings(settings);
      chartController.setBackgroundProfile(settings.isEnabled() ? pendingProfile : null);
      chartController.updateChart();
    }
    onApplyCallback.run();
    if (close) super.onOk(null);
  }
}

