package org.artisan.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.model.AxisConfig;

/**
 * Config » Axes dialog: temperature range, RoR range, time range, grid steps, units.
 * Persists to Preferences "axis.*". OK/Apply calls chartController.applyAxisConfig(config) + updateChart().
 */
public final class AxesDialog extends ArtisanDialog {

    private final AxisConfig config;
    private final Runnable onApply;

    private Spinner<Double> timeMinSpinner;
    private Spinner<Double> timeMaxSpinner;
    private Spinner<Integer> timeStepSpinner;
    private Spinner<Double> tempMinSpinner;
    private Spinner<Double> tempMaxSpinner;
    private Spinner<Integer> tempStepSpinner;
    private Spinner<Double> rorMinSpinner;
    private Spinner<Double> rorMaxSpinner;
    private CheckBox autoScaleYCheck;
    private CheckBox autoScaleY2Check;
    private RadioButton celsiusRadio;
    private RadioButton fahrenheitRadio;

    public AxesDialog(Window owner, AxisConfig config, Runnable onApply) {
        super(owner, true, true);
        this.config = config != null ? config : new AxisConfig();
        this.onApply = onApply != null ? onApply : () -> {};
        getStage().setTitle("Config » Axes");
        getApplyButton().setOnAction(e -> applyAndRedraw(false));
    }

    @Override
    protected Node buildContent() {
        AxisConfig.loadFromPreferences(config);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;

        grid.add(new Label("Temperature axis"), 0, row++, 2, 1);
        tempMinSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-50, 400, config.getTempMin(), 5));
        tempMinSpinner.setEditable(true);
        tempMaxSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-50, 400, config.getTempMax(), 5));
        tempMaxSpinner.setEditable(true);
        autoScaleYCheck = new CheckBox("Auto scale");
        autoScaleYCheck.setSelected(config.isAutoScaleY());
        grid.add(new Label("Min:"), 0, row); grid.add(tempMinSpinner, 1, row++);
        grid.add(new Label("Max:"), 0, row); grid.add(tempMaxSpinner, 1, row++);
        grid.add(autoScaleYCheck, 0, row++, 2, 1);

        grid.add(new Label("RoR axis"), 0, row++, 2, 1);
        rorMinSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-50, 0, config.getRorMin(), 5));
        rorMinSpinner.setEditable(true);
        rorMaxSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, config.getRorMax(), 5));
        rorMaxSpinner.setEditable(true);
        autoScaleY2Check = new CheckBox("Auto scale RoR");
        autoScaleY2Check.setSelected(config.isAutoScaleY2());
        grid.add(new Label("Min (°/min):"), 0, row); grid.add(rorMinSpinner, 1, row++);
        grid.add(new Label("Max (°/min):"), 0, row); grid.add(rorMaxSpinner, 1, row++);
        grid.add(autoScaleY2Check, 0, row++, 2, 1);

        grid.add(new Label("Time axis"), 0, row++, 2, 1);
        timeMinSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-60, 3600, config.getTimeMinSec(), 30));
        timeMinSpinner.setEditable(true);
        timeMaxSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 7200, config.getTimeMaxSec(), 60));
        timeMaxSpinner.setEditable(true);
        timeStepSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 600, (int) config.getTimeTickStepSec(), 15));
        timeStepSpinner.setEditable(true);
        grid.add(new Label("Min (s):"), 0, row); grid.add(timeMinSpinner, 1, row++);
        grid.add(new Label("Max (s):"), 0, row); grid.add(timeMaxSpinner, 1, row++);
        grid.add(new Label("Grid step (s):"), 0, row); grid.add(timeStepSpinner, 1, row++);

        grid.add(new Label("Temp grid step:"), 0, row);
        tempStepSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 100, (int) config.getTempTickStep(), 5));
        tempStepSpinner.setEditable(true);
        grid.add(tempStepSpinner, 1, row++);

        ToggleGroup unitGroup = new ToggleGroup();
        celsiusRadio = new RadioButton("°C");
        fahrenheitRadio = new RadioButton("°F");
        celsiusRadio.setToggleGroup(unitGroup);
        fahrenheitRadio.setToggleGroup(unitGroup);
        boolean isF = config.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT;
        fahrenheitRadio.setSelected(isF);
        celsiusRadio.setSelected(!isF);
        grid.add(new Label("Units:"), 0, row);
        grid.add(new HBox(10, celsiusRadio, fahrenheitRadio), 1, row++);

        Button restoreBtn = new Button("Restore Defaults");
        restoreBtn.setOnAction(e -> restoreDefaults());
        HBox bottom = new HBox(10, restoreBtn);
        bottom.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox(10, grid, bottom);
        root.setPadding(new Insets(10));
        return root;
    }

    private void syncSpinnersFromConfig() {
        if (timeMinSpinner != null) timeMinSpinner.getValueFactory().setValue(config.getTimeMinSec());
        if (timeMaxSpinner != null) timeMaxSpinner.getValueFactory().setValue(config.getTimeMaxSec());
        if (timeStepSpinner != null) timeStepSpinner.getValueFactory().setValue((int) config.getTimeTickStepSec());
        if (tempMinSpinner != null) tempMinSpinner.getValueFactory().setValue(config.getTempMin());
        if (tempMaxSpinner != null) tempMaxSpinner.getValueFactory().setValue(config.getTempMax());
        if (tempStepSpinner != null) tempStepSpinner.getValueFactory().setValue((int) config.getTempTickStep());
        if (rorMinSpinner != null) rorMinSpinner.getValueFactory().setValue(config.getRorMin());
        if (rorMaxSpinner != null) rorMaxSpinner.getValueFactory().setValue(config.getRorMax());
        if (autoScaleYCheck != null) autoScaleYCheck.setSelected(config.isAutoScaleY());
        if (autoScaleY2Check != null) autoScaleY2Check.setSelected(config.isAutoScaleY2());
        if (celsiusRadio != null) celsiusRadio.setSelected(config.getUnit() == AxisConfig.TemperatureUnit.CELSIUS);
        if (fahrenheitRadio != null) fahrenheitRadio.setSelected(config.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT);
    }

    private void syncConfigFromSpinners() {
        if (timeMinSpinner != null) config.setTimeMinSec(timeMinSpinner.getValue());
        if (timeMaxSpinner != null) config.setTimeMaxSec(timeMaxSpinner.getValue());
        if (timeStepSpinner != null) config.setTimeTickStepSec(timeStepSpinner.getValue());
        if (tempMinSpinner != null) config.setTempMin(tempMinSpinner.getValue());
        if (tempMaxSpinner != null) config.setTempMax(tempMaxSpinner.getValue());
        if (tempStepSpinner != null) config.setTempTickStep(tempStepSpinner.getValue());
        if (rorMinSpinner != null) config.setRorMin(rorMinSpinner.getValue());
        if (rorMaxSpinner != null) config.setRorMax(rorMaxSpinner.getValue());
        if (autoScaleYCheck != null) config.setAutoScaleY(autoScaleYCheck.isSelected());
        if (autoScaleY2Check != null) config.setAutoScaleY2(autoScaleY2Check.isSelected());
        if (fahrenheitRadio != null && fahrenheitRadio.isSelected()) config.setUnit(AxisConfig.TemperatureUnit.FAHRENHEIT);
        else if (celsiusRadio != null) config.setUnit(AxisConfig.TemperatureUnit.CELSIUS);
    }

    private void restoreDefaults() {
        AxisConfig def = new AxisConfig();
        config.setTimeMinSec(def.getTimeMinSec());
        config.setTimeMaxSec(def.getTimeMaxSec());
        config.setTimeTickStepSec(def.getTimeTickStepSec());
        config.setTempMin(def.getTempMin());
        config.setTempMax(def.getTempMax());
        config.setTempTickStep(def.getTempTickStep());
        config.setUnit(def.getUnit());
        config.setAutoScaleY(true);
        config.setAutoScaleY2(true);
        config.setRorMin(AxisConfig.DEFAULT_MIN_ROR);
        config.setRorMax(AxisConfig.DEFAULT_MAX_ROR);
        syncSpinnersFromConfig();
    }

    private void applyAndRedraw(boolean close) {
        syncConfigFromSpinners();
        AxisConfig.saveToPreferences(config);
        onApply.run();
        if (close) getStage().close();
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        applyAndRedraw(false);
        super.onOk(e);
    }
}
