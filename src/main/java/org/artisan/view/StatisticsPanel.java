package org.artisan.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import org.artisan.model.PhaseResult;
import org.artisan.model.RoastStats;

/**
 * Collapsible panel showing roast statistics: development time, DTR, AUC,
 * BT/ET means, RoR min/max/mean, total time. Updates live during recording and on profile load.
 */
public final class StatisticsPanel extends VBox {

    private static final String DASH = "—";

    private final Label devTimeValue;
    private final Label dtrValue;
    private final Label aucValue;
    private final Label meanBtValue;
    private final Label meanEtValue;
    private final Label rorMinValue;
    private final Label rorMaxValue;
    private final Label rorMeanValue;
    private final Label totalTimeValue;

    public StatisticsPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);
        int row = 0;

        devTimeValue = new Label(DASH);
        grid.add(new Label("Development Time (s):"), 0, row);
        grid.add(devTimeValue, 1, row++);

        dtrValue = new Label(DASH);
        grid.add(new Label("DTR (%):"), 0, row);
        grid.add(dtrValue, 1, row++);

        Label aucLabel = new Label("AUC (°C·min):");
        aucValue = new Label(DASH);
        grid.add(aucLabel, 0, row);
        grid.add(aucValue, 1, row++);
        aucValue.setTooltip(new javafx.scene.control.Tooltip(
            "Area under the BT curve above a base temperature (set in Colors → Graph)."));

        meanBtValue = new Label(DASH);
        grid.add(new Label("BT Mean (°C):"), 0, row);
        grid.add(meanBtValue, 1, row++);

        meanEtValue = new Label(DASH);
        grid.add(new Label("ET Mean (°C):"), 0, row);
        grid.add(meanEtValue, 1, row++);

        rorMinValue = new Label(DASH);
        grid.add(new Label("BT RoR min (°C/min):"), 0, row);
        grid.add(rorMinValue, 1, row++);

        rorMaxValue = new Label(DASH);
        grid.add(new Label("BT RoR max (°C/min):"), 0, row);
        grid.add(rorMaxValue, 1, row++);

        rorMeanValue = new Label(DASH);
        grid.add(new Label("BT RoR mean (°C/min):"), 0, row);
        grid.add(rorMeanValue, 1, row++);

        totalTimeValue = new Label(DASH);
        grid.add(new Label("Total time (mm:ss):"), 0, row);
        grid.add(totalTimeValue, 1, row++);

        TitledPane titled = new TitledPane("Statistics", grid);
        titled.setCollapsible(true);
        titled.setExpanded(true);
        getChildren().add(titled);
        setPadding(new Insets(4, 0, 0, 0));
    }

    /**
     * Updates displayed values. Pass baseTempC for the AUC tooltip (optional).
     */
    public void update(RoastStats stats, PhaseResult phase, double dtr, double auc) {
        update(stats, phase, dtr, auc, Double.NaN);
    }

    /**
     * Updates displayed values. baseTempC is used for the AUC row tooltip when finite.
     */
    public void update(RoastStats stats, PhaseResult phase, double dtr, double auc, double baseTempC) {
        if (stats == null || stats.isEmpty()) {
            devTimeValue.setText(DASH);
            dtrValue.setText(DASH);
            aucValue.setText(DASH);
            meanBtValue.setText(DASH);
            meanEtValue.setText(DASH);
            rorMinValue.setText(DASH);
            rorMaxValue.setText(DASH);
            rorMeanValue.setText(DASH);
            totalTimeValue.setText(DASH);
            if (Double.isFinite(baseTempC)) {
                aucValue.setTooltip(new javafx.scene.control.Tooltip(
                    String.format("Area under BT curve above base temperature. Base: %.1f °C (Colors → Graph).", baseTempC)));
            }
            return;
        }

        devTimeValue.setText(phase != null && !phase.isInvalid()
            ? formatDouble(phase.getDevelopmentTimeSec())
            : DASH);
        dtrValue.setText(Double.isFinite(dtr) ? formatDouble(dtr) : DASH);
        aucValue.setText(Double.isFinite(auc) ? formatDouble(auc) : DASH);
        if (Double.isFinite(baseTempC)) {
            aucValue.setTooltip(new javafx.scene.control.Tooltip(
                String.format("Area under BT curve above base temperature. Base: %.1f °C (Colors → Graph).", baseTempC)));
        }
        meanBtValue.setText(formatDouble(stats.getMeanBt()));
        meanEtValue.setText(formatDouble(stats.getMeanEt()));
        rorMinValue.setText(formatDouble(stats.getRorMin()));
        rorMaxValue.setText(formatDouble(stats.getRorMax()));
        rorMeanValue.setText(formatDouble(stats.getRorMean()));
        totalTimeValue.setText(formatMmSs(stats.getTotalTimeSec()));
    }

    private static String formatDouble(double v) {
        if (!Double.isFinite(v)) return "—";
        return String.format("%.2f", v);
    }

    private static String formatMmSs(double totalTimeSec) {
        if (!Double.isFinite(totalTimeSec) || totalTimeSec < 0) return DASH;
        int totalSec = (int) Math.round(totalTimeSec);
        int m = totalSec / 60;
        int s = totalSec % 60;
        return String.format("%d:%02d", m, s);
    }
}
