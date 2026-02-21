package org.artisan.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.model.SamplingConfig;

/**
 * Config » Sampling dialog: interval, oversampling, spike filter.
 * OK/Apply: save to SamplingConfig (Preferences "sampling.*") and restart sampling timer if running.
 */
public final class SamplingDialog extends ArtisanDialog {

    private final SamplingConfig config;
    private final Runnable onApply;

    private Spinner<Double> intervalSpinner;
    private Spinner<Integer> oversamplingSpinner;
    private CheckBox filterSpikesCheck;
    private Spinner<Double> spikeThresholdSpinner;

    public SamplingDialog(Window owner, SamplingConfig config, Runnable onApply) {
        super(owner, true, true);
        this.config = config != null ? config : new SamplingConfig();
        this.onApply = onApply != null ? onApply : () -> {};
        getStage().setTitle("Config » Sampling");
        getApplyButton().setOnAction(e -> applyAndRedraw(false));
    }

    @Override
    protected Node buildContent() {
        SamplingConfig.loadFromPreferences(config);
        intervalSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                SamplingConfig.MIN_INTERVAL, SamplingConfig.MAX_INTERVAL, config.getIntervalSeconds(), 0.5));
        intervalSpinner.setEditable(true);
        oversamplingSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                SamplingConfig.MIN_OVERSAMPLING, SamplingConfig.MAX_OVERSAMPLING, config.getOversampling(), 1));
        oversamplingSpinner.setEditable(true);
        filterSpikesCheck = new CheckBox("Filter spikes (discard samples exceeding threshold)");
        filterSpikesCheck.setSelected(config.isFilterSpikes());
        spikeThresholdSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.1, 200, config.getSpikeThreshold(), 1.0));
        spikeThresholdSpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(new Label("Interval (seconds):"), 0, row);
        grid.add(intervalSpinner, 1, row++);
        grid.add(new Label("Oversampling (1–10):"), 0, row);
        grid.add(oversamplingSpinner, 1, row++);
        grid.add(filterSpikesCheck, 0, row++, 2, 1);
        grid.add(new Label("Spike threshold (°C/s):"), 0, row);
        grid.add(spikeThresholdSpinner, 1, row++);

        Button restoreBtn = new Button("Restore Defaults");
        restoreBtn.setOnAction(e -> restoreDefaults());
        HBox bottom = new HBox(10, restoreBtn);
        bottom.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox(10, grid, bottom);
        root.setPadding(new Insets(10));
        return root;
    }

    private void syncFromConfig() {
        if (intervalSpinner != null) intervalSpinner.getValueFactory().setValue(config.getIntervalSeconds());
        if (oversamplingSpinner != null) oversamplingSpinner.getValueFactory().setValue(config.getOversampling());
        if (filterSpikesCheck != null) filterSpikesCheck.setSelected(config.isFilterSpikes());
        if (spikeThresholdSpinner != null) spikeThresholdSpinner.getValueFactory().setValue(config.getSpikeThreshold());
    }

    private void syncToConfig() {
        if (intervalSpinner != null) config.setIntervalSeconds(intervalSpinner.getValue());
        if (oversamplingSpinner != null) config.setOversampling(oversamplingSpinner.getValue());
        if (filterSpikesCheck != null) config.setFilterSpikes(filterSpikesCheck.isSelected());
        if (spikeThresholdSpinner != null) config.setSpikeThreshold(spikeThresholdSpinner.getValue());
    }

    private void restoreDefaults() {
        config.setIntervalSeconds(SamplingConfig.DEFAULT_INTERVAL_SECONDS);
        config.setOversampling(SamplingConfig.DEFAULT_OVERSAMPLING);
        config.setFilterSpikes(false);
        config.setSpikeThreshold(SamplingConfig.DEFAULT_SPIKE_THRESHOLD);
        syncFromConfig();
    }

    private void applyAndRedraw(boolean close) {
        syncToConfig();
        SamplingConfig.saveToPreferences(config);
        onApply.run();
        if (close) getStage().close();
    }

    /** Apply and persist settings without closing. Used when this dialog is embedded in unified Settings. */
    public void applyFromUI() {
        syncToConfig();
        SamplingConfig.saveToPreferences(config);
        onApply.run();
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        applyAndRedraw(false);
        super.onOk(e);
    }
}
