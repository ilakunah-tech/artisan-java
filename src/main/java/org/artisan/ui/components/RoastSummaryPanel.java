package org.artisan.ui.components;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.artisan.ui.vm.RoastViewModel;

/**
 * Roast summary: Duration, Dev time, Dev ratio, Roast value, End temp.
 * Cropster RI5-like compact summary card for Live Roast right rail.
 */
public final class RoastSummaryPanel extends GridPane {

    private final Label durationLabel;
    private final Label devTimeLabel;
    private final Label devRatioLabel;
    private final Label roastValueLabel;
    private final Label endTempLabel;

    public RoastSummaryPanel(RoastViewModel viewModel) {
        getStyleClass().add("ri5-summary-card");
        setHgap(12);
        setVgap(6);
        setPadding(new Insets(10));

        durationLabel = new Label("—");
        devTimeLabel = new Label("—");
        devRatioLabel = new Label("—");
        roastValueLabel = new Label("—");
        endTempLabel = new Label("—");

        int row = 0;
        addRow(row++, new Label("Duration:"), durationLabel);
        addRow(row++, new Label("Dev time:"), devTimeLabel);
        addRow(row++, new Label("Dev ratio:"), devRatioLabel);
        addRow(row++, new Label("Roast value:"), roastValueLabel);
        addRow(row++, new Label("End temp:"), endTempLabel);

        if (viewModel != null) {
            ChangeListener<Number> updater = (a, b, n) -> updateFromViewModel(viewModel);
            viewModel.elapsedSecProperty().addListener(updater);
            viewModel.devTimeSecProperty().addListener(updater);
            viewModel.btProperty().addListener(updater);
            updateFromViewModel(viewModel);
        }
    }

    /** Call to set roast color (0–100) from RoastProperties. */
    public void setRoastColor(int roastColor) {
        if (roastColor >= 0) {
            roastValueLabel.setText(String.valueOf(roastColor));
        } else {
            roastValueLabel.setText("—");
        }
    }

    private void updateFromViewModel(RoastViewModel vm) {
        double elapsed = vm.getElapsedSec();
        double devTime = vm.getDevTimeSec();
        double bt = vm.getBt();

        if (Double.isFinite(elapsed) && elapsed >= 0) {
            int m = (int) (elapsed / 60);
            int s = (int) (elapsed % 60);
            durationLabel.setText(String.format("%d:%02d", m, s));
        } else {
            durationLabel.setText("—");
        }

        if (Double.isFinite(devTime) && devTime >= 0) {
            int m = (int) (devTime / 60);
            int s = (int) (devTime % 60);
            devTimeLabel.setText(String.format("%d:%02d min", m, s));
        } else {
            devTimeLabel.setText("—");
        }

        if (Double.isFinite(devTime) && Double.isFinite(elapsed) && elapsed > 0) {
            double ratio = devTime / elapsed * 100.0;
            devRatioLabel.setText(String.format("%.1f%%", ratio));
        } else {
            devRatioLabel.setText("—");
        }

        if (Double.isFinite(bt)) {
            endTempLabel.setText(String.format("%.1f °C", bt));
        } else {
            endTempLabel.setText("—");
        }
    }
}
