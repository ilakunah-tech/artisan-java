package org.artisan.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.controller.PhasesSettings;
import org.artisan.model.PhaseDisplayMode;
import org.artisan.model.PhasesConfig;

/**
 * Config » Phases dialog: manual phase limits (BT thresholds), toggles,
 * per-phase display mode, finishing "show all info across 3 LCDs".
 * Persists to PhasesSettings (Preferences "phases.*"). OK/Apply updates phases shading and LCDs.
 */
public final class PhasesDialog extends ArtisanDialog {

    private final PhasesSettings phasesSettings;
    private final Runnable onApply;

    private Spinner<Double> dryEndTempSpinner;
    private Spinner<Double> fcsTempSpinner;
    private CheckBox autoAdjustedCheck;
    private CheckBox autoDRYCheck;
    private CheckBox autoFCsCheck;
    private CheckBox fromBackgroundCheck;
    private ComboBox<PhaseDisplayMode> dryingModeCombo;
    private ComboBox<PhaseDisplayMode> maillardModeCombo;
    private ComboBox<PhaseDisplayMode> finishingModeCombo;
    private CheckBox finishingShowAllLcdsCheck;

    public PhasesDialog(Window owner, PhasesSettings phasesSettings, Runnable onApply) {
        super(owner, true, true);
        this.phasesSettings = phasesSettings != null ? phasesSettings : PhasesSettings.load();
        this.onApply = onApply != null ? onApply : () -> {};
        getStage().setTitle("Config » Phases");
        getApplyButton().setOnAction(e -> applyAndClose(false));
    }

    @Override
    protected Node buildContent() {
        PhasesConfig c = phasesSettings.toConfig();

        dryEndTempSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 300.0, c.getDryEndTempC(), 1.0));
        dryEndTempSpinner.setEditable(true);
        fcsTempSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 300.0, c.getFcsTempC(), 1.0));
        fcsTempSpinner.setEditable(true);

        autoAdjustedCheck = new CheckBox("Auto adjusted limits (use DRY/FCs events)");
        autoAdjustedCheck.setSelected(c.isAutoAdjustedLimits());
        autoDRYCheck = new CheckBox("Auto DRY (generate DRY when BT reaches Drying end)");
        autoDRYCheck.setSelected(c.isAutoDRY());
        autoFCsCheck = new CheckBox("Auto FCs (generate FCs when BT reaches First crack start)");
        autoFCsCheck.setSelected(c.isAutoFCs());
        fromBackgroundCheck = new CheckBox("From background (use DRY/FCs from selected background profile)");
        fromBackgroundCheck.setSelected(c.isFromBackground());

        dryingModeCombo = modeCombo(c.getDryingEnterMode());
        maillardModeCombo = modeCombo(c.getMaillardEnterMode());
        finishingModeCombo = modeCombo(c.getFinishingEnterMode());
        finishingShowAllLcdsCheck = new CheckBox("Finishing: show all info (time, temp, %) across 3 LCDs");
        finishingShowAllLcdsCheck.setSelected(c.isFinishingShowAllLcds());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(new Label("Drying end (°C):"), 0, row);
        grid.add(dryEndTempSpinner, 1, row++);
        grid.add(new Label("First crack start (°C):"), 0, row);
        grid.add(fcsTempSpinner, 1, row++);
        grid.add(autoAdjustedCheck, 0, row++, 2, 1);
        grid.add(autoDRYCheck, 0, row++, 2, 1);
        grid.add(autoFCsCheck, 0, row++, 2, 1);
        grid.add(fromBackgroundCheck, 0, row++, 2, 1);
        grid.add(new Label("Display on entering Drying:"), 0, row);
        grid.add(dryingModeCombo, 1, row++);
        grid.add(new Label("Display on entering Maillard:"), 0, row);
        grid.add(maillardModeCombo, 1, row++);
        grid.add(new Label("Display on entering Finishing:"), 0, row);
        grid.add(finishingModeCombo, 1, row++);
        grid.add(finishingShowAllLcdsCheck, 0, row++, 2, 1);

        Button restoreBtn = new Button("Restore Defaults");
        restoreBtn.setOnAction(e -> restoreDefaults());

        HBox bottom = new HBox(10, restoreBtn);
        bottom.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox(10, grid, bottom);
        root.setPadding(new Insets(10));
        return root;
    }

    private static ComboBox<PhaseDisplayMode> modeCombo(PhaseDisplayMode selected) {
        ComboBox<PhaseDisplayMode> combo = new ComboBox<>();
        combo.getItems().addAll(PhaseDisplayMode.TIME, PhaseDisplayMode.PERCENTAGE, PhaseDisplayMode.TEMPERATURE);
        combo.setValue(selected != null ? selected : PhaseDisplayMode.TIME);
        return combo;
    }

    private void restoreDefaults() {
        phasesSettings.restoreDefaults();
        PhasesConfig c = phasesSettings.toConfig();
        dryEndTempSpinner.getValueFactory().setValue(c.getDryEndTempC());
        fcsTempSpinner.getValueFactory().setValue(c.getFcsTempC());
        autoAdjustedCheck.setSelected(c.isAutoAdjustedLimits());
        autoDRYCheck.setSelected(c.isAutoDRY());
        autoFCsCheck.setSelected(c.isAutoFCs());
        fromBackgroundCheck.setSelected(c.isFromBackground());
        dryingModeCombo.setValue(c.getDryingEnterMode());
        maillardModeCombo.setValue(c.getMaillardEnterMode());
        finishingModeCombo.setValue(c.getFinishingEnterMode());
        finishingShowAllLcdsCheck.setSelected(c.isFinishingShowAllLcds());
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        if (!validateAndApply()) return;
        super.onOk(e);
    }

    /** Apply to settings and run callback; if closeDialog then close. */
    private void applyAndClose(boolean closeDialog) {
        if (!validateAndApply()) return;
        if (closeDialog) getStage().close();
    }

    private boolean validateAndApply() {
        double dryEnd = dryEndTempSpinner.getValue();
        double fcs = fcsTempSpinner.getValue();
        if (dryEnd >= fcs) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Phases");
            alert.setHeaderText("Invalid limits");
            alert.setContentText("Drying end temperature must be less than First crack start.");
            alert.showAndWait();
            return false;
        }
        PhasesConfig c = new PhasesConfig();
        c.setDryEndTempC(dryEnd);
        c.setFcsTempC(fcs);
        c.setAutoAdjustedLimits(autoAdjustedCheck.isSelected());
        c.setAutoDRY(autoDRYCheck.isSelected());
        c.setAutoFCs(autoFCsCheck.isSelected());
        c.setFromBackground(fromBackgroundCheck.isSelected());
        c.setDryingEnterMode(dryingModeCombo.getValue());
        c.setMaillardEnterMode(maillardModeCombo.getValue());
        c.setFinishingEnterMode(finishingModeCombo.getValue());
        c.setFinishingShowAllLcds(finishingShowAllLcdsCheck.isSelected());
        phasesSettings.fromConfig(c);
        onApply.run();
        return true;
    }
}
