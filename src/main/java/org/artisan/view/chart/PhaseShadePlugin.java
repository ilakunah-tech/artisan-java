package org.artisan.view.chart;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.Axis;
import io.fair_acc.chartfx.plugins.ChartPlugin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;
import org.artisan.model.Phases;
import org.artisan.model.PhasesConfig;

import java.util.List;

/**
 * Chart-FX plugin that renders semi-transparent phase shading rectangles
 * (Drying, Maillard, Finishing, Cooling) using native axis coordinate transforms.
 * Replaces the old StackPane-based phaseShadeLayer overlay.
 */
public final class PhaseShadePlugin extends ChartPlugin {

    private static final double PHASE_ALPHA = 0.25;
    private static final int IDX_CHARGE = 0;
    private static final int IDX_DRY_END = 1;
    private static final int IDX_FC_START = 2;
    private static final int IDX_DROP = 6;

    private final Rectangle rectDrying = new Rectangle();
    private final Rectangle rectMaillard = new Rectangle();
    private final Rectangle rectFinishing = new Rectangle();
    private final Rectangle rectCooling = new Rectangle();

    private CanvasData canvasData;
    private ColorConfig colorConfig;
    private PhasesConfig phasesConfig;

    public PhaseShadePlugin() {
        rectDrying.setMouseTransparent(true);
        rectMaillard.setMouseTransparent(true);
        rectFinishing.setMouseTransparent(true);
        rectCooling.setMouseTransparent(true);
        getChartChildren().addAll(rectDrying, rectMaillard, rectFinishing, rectCooling);
        hideAll();
    }

    public void setCanvasData(CanvasData canvasData)   { this.canvasData = canvasData; }
    public void setColorConfig(ColorConfig colorConfig) { this.colorConfig = colorConfig; }
    public void setPhasesConfig(PhasesConfig config)    { this.phasesConfig = config; }

    public void refresh() {
        if (getChart() == null || canvasData == null || phasesConfig == null || colorConfig == null) {
            hideAll();
            return;
        }
        List<Double> timex = canvasData.getTimex();
        List<Double> temp2 = canvasData.getTemp2();
        if (timex.isEmpty()) {
            hideAll();
            return;
        }

        List<Integer> ti = Phases.timeindexFromIndices(
                canvasData.getChargeIndex(),
                canvasData.getDryEndIndex(),
                canvasData.getFcStartIndex(),
                canvasData.getDropIndex());
        List<Integer> effective = Phases.getEffectiveTimeindex(timex, temp2, ti, phasesConfig);

        int n = timex.size();
        int chargeIdx = idxAt(effective, IDX_CHARGE);
        int dryEndIdx = idxAt(effective, IDX_DRY_END);
        int fcStartIdx = idxAt(effective, IDX_FC_START);
        int dropIdx = idxAt(effective, IDX_DROP);

        int startIdx = chargeIdx >= 0 ? chargeIdx : 0;
        double tStart = timex.get(startIdx);
        double tDryEnd = dryEndIdx > 0 && dryEndIdx < n ? timex.get(dryEndIdx) : tStart;
        double tFcStart = fcStartIdx > 0 && fcStartIdx < n ? timex.get(fcStartIdx) : tDryEnd;
        double tDrop = dropIdx > 0 && dropIdx < n ? timex.get(dropIdx) : timex.get(n - 1);
        if (tFcStart < tDryEnd) tFcStart = tDryEnd;
        if (tDrop < tFcStart) tDrop = tFcStart;
        double tEnd = timex.get(n - 1);

        XYChart xyChart = (XYChart) getChart();
        Axis xAxis = xyChart.getXAxis();

        double canvasW = xyChart.getCanvas().getWidth();
        double canvasH = xyChart.getCanvas().getHeight();
        if (canvasW <= 0 || canvasH <= 0) { hideAll(); return; }

        double plotX = xyChart.getCanvas().getLayoutX();
        double plotY = xyChart.getCanvas().getLayoutY();

        double x1 = xAxis.getDisplayPosition(tStart) + plotX;
        double x2 = xAxis.getDisplayPosition(tDryEnd) + plotX;
        double x3 = xAxis.getDisplayPosition(tFcStart) + plotX;
        double x4 = xAxis.getDisplayPosition(tDrop) + plotX;
        double x5 = xAxis.getDisplayPosition(tEnd) + plotX;

        placeRect(rectDrying, x1, plotY, x2 - x1, canvasH, colorConfig.getPaletteColor("rect1"));
        placeRect(rectMaillard, x2, plotY, x3 - x2, canvasH, colorConfig.getPaletteColor("rect2"));
        placeRect(rectFinishing, x3, plotY, x4 - x3, canvasH, colorConfig.getPaletteColor("rect3"));
        placeRect(rectCooling, x4, plotY, x5 - x4, canvasH, colorConfig.getPaletteColor("rect4"));
    }

    private void placeRect(Rectangle rect, double x, double y, double w, double h, Color color) {
        if (w <= 0 || color == null) {
            rect.setVisible(false);
            return;
        }
        rect.setX(x);
        rect.setY(y);
        rect.setWidth(w);
        rect.setHeight(h);
        rect.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), PHASE_ALPHA));
        rect.setVisible(true);
    }

    private void hideAll() {
        rectDrying.setVisible(false);
        rectMaillard.setVisible(false);
        rectFinishing.setVisible(false);
        rectCooling.setVisible(false);
    }

    private static int idxAt(List<Integer> ti, int slot) {
        if (slot >= ti.size()) return -1;
        Integer v = ti.get(slot);
        if (v == null) return -1;
        return (slot == IDX_CHARGE && v >= 0) || (slot != IDX_CHARGE && v > 0) ? v : -1;
    }
}
