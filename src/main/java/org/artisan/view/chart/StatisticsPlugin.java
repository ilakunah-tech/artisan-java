package org.artisan.view.chart;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.Axis;
import io.fair_acc.chartfx.plugins.ChartPlugin;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;
import org.artisan.model.PhasesConfig;
import org.artisan.model.Phases;

import java.util.ArrayList;
import java.util.List;

/**
 * Chart-FX plugin that renders post-roast statistics overlay inside the chart area:
 * horizontal phase-duration bars at the top (Drying / Maillard / Development),
 * phase time + percentage labels, DTR (Development Time Ratio), and RoR at key events.
 * Visible only after DROP event is set.
 */
public final class StatisticsPlugin extends ChartPlugin {

    private static final double BAR_HEIGHT = 14.0;
    private static final double BAR_TOP_MARGIN = 4.0;
    private static final int IDX_CHARGE = 0;
    private static final int IDX_DRY_END = 1;
    private static final int IDX_FC_START = 2;
    private static final int IDX_DROP = 6;

    private CanvasData canvasData;
    private ColorConfig colorConfig;
    private PhasesConfig phasesConfig;
    private List<Double> rorBT;

    private final List<javafx.scene.Node> dynamicNodes = new ArrayList<>();

    public StatisticsPlugin() {}

    public void setCanvasData(CanvasData data)        { this.canvasData = data; }
    public void setColorConfig(ColorConfig cfg)       { this.colorConfig = cfg; }
    public void setPhasesConfig(PhasesConfig cfg)     { this.phasesConfig = cfg; }
    public void setRorBT(List<Double> rorBT)          { this.rorBT = rorBT; }

    public void refresh(List<Double> timex) {
        getChartChildren().removeAll(dynamicNodes);
        dynamicNodes.clear();

        if (getChart() == null || canvasData == null || colorConfig == null
                || phasesConfig == null || timex == null || timex.isEmpty()) return;

        int dropIdx = canvasData.getDropIndex();
        if (dropIdx <= 0 || dropIdx >= timex.size()) return;

        List<Integer> ti = Phases.timeindexFromIndices(
                canvasData.getChargeIndex(),
                canvasData.getDryEndIndex(),
                canvasData.getFcStartIndex(),
                canvasData.getDropIndex());
        List<Integer> effective = Phases.getEffectiveTimeindex(timex, canvasData.getTemp2(), ti, phasesConfig);

        int chargeIdx = idxAt(effective, IDX_CHARGE);
        int dryEndIdx = idxAt(effective, IDX_DRY_END);
        int fcStartIdx = idxAt(effective, IDX_FC_START);

        if (chargeIdx < 0) return;
        int n = timex.size();
        double tCharge = timex.get(chargeIdx);
        double tDryEnd = dryEndIdx > 0 && dryEndIdx < n ? timex.get(dryEndIdx) : tCharge;
        double tFcStart = fcStartIdx > 0 && fcStartIdx < n ? timex.get(fcStartIdx) : tDryEnd;
        double tDrop = timex.get(dropIdx);
        if (tFcStart < tDryEnd) tFcStart = tDryEnd;
        if (tDrop < tFcStart) tDrop = tFcStart;

        double totalRoast = tDrop - tCharge;
        if (totalRoast <= 0) return;

        double drying = tDryEnd - tCharge;
        double maillard = tFcStart - tDryEnd;
        double development = tDrop - tFcStart;

        double dtr = development / totalRoast * 100.0;

        XYChart xyChart = (XYChart) getChart();
        Axis xAxis = xyChart.getXAxis();
        double canvasW = xyChart.getCanvas().getWidth();
        if (canvasW <= 0) return;
        double plotX = xyChart.getCanvas().getLayoutX();
        double plotY = xyChart.getCanvas().getLayoutY();

        double barX1 = xAxis.getDisplayPosition(tCharge) + plotX;
        double barX2 = xAxis.getDisplayPosition(tDryEnd) + plotX;
        double barX3 = xAxis.getDisplayPosition(tFcStart) + plotX;
        double barX4 = xAxis.getDisplayPosition(tDrop) + plotX;

        double barY = plotY + BAR_TOP_MARGIN;

        Color c1 = colorConfig.getPaletteColor("rect1");
        Color c2 = colorConfig.getPaletteColor("rect2");
        Color c3 = colorConfig.getPaletteColor("rect3");

        addBar(barX1, barY, barX2 - barX1, c1 != null ? c1 : Color.YELLOW, drying, totalRoast, "Drying");
        addBar(barX2, barY, barX3 - barX2, c2 != null ? c2 : Color.ORANGE, maillard, totalRoast, "Maillard");
        addBar(barX3, barY, barX4 - barX3, c3 != null ? c3 : Color.BROWN, development, totalRoast, "Dev");

        Color textColor = colorConfig.getPaletteColor("text");
        if (textColor == null) textColor = Color.WHITE;

        Text dtrLabel = new Text(String.format("DTR: %.1f%%", dtr));
        dtrLabel.setFill(textColor);
        dtrLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
        dtrLabel.setX(barX4 + 6);
        dtrLabel.setY(barY + BAR_HEIGHT - 2);
        dtrLabel.setMouseTransparent(true);
        addNode(dtrLabel);

        if (rorBT != null) {
            int fcStart = canvasData.getFcStartIndex();
            if (fcStart > 0 && fcStart < rorBT.size()) {
                Text rorFc = new Text(String.format("RoR@FCs: %.1f", rorBT.get(fcStart)));
                rorFc.setFill(textColor);
                rorFc.setStyle("-fx-font-size: 9px;");
                rorFc.setX(barX4 + 6);
                rorFc.setY(barY + BAR_HEIGHT + 12);
                rorFc.setMouseTransparent(true);
                addNode(rorFc);
            }
            if (dropIdx > 0 && dropIdx < rorBT.size()) {
                Text rorDrop = new Text(String.format("RoR@Drop: %.1f", rorBT.get(dropIdx)));
                rorDrop.setFill(textColor);
                rorDrop.setStyle("-fx-font-size: 9px;");
                rorDrop.setX(barX4 + 6);
                rorDrop.setY(barY + BAR_HEIGHT + 24);
                rorDrop.setMouseTransparent(true);
                addNode(rorDrop);
            }
        }
    }

    private void addBar(double x, double y, double w, Color color, double phaseSec, double totalSec, String name) {
        if (w <= 0) return;
        Rectangle rect = new Rectangle(x, y, w, BAR_HEIGHT);
        rect.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.7));
        rect.setStroke(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.9));
        rect.setArcWidth(3);
        rect.setArcHeight(3);
        rect.setMouseTransparent(true);
        addNode(rect);

        double pct = totalSec > 0 ? phaseSec / totalSec * 100.0 : 0;
        int mm = (int) (phaseSec / 60);
        int ss = (int) (phaseSec % 60);
        String label = String.format("%s %d:%02d (%.0f%%)", name, mm, ss, pct);
        Text text = new Text(label);
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-size: 9px;");
        double textWidth = text.getLayoutBounds().getWidth();
        if (textWidth < w - 4) {
            text.setX(x + (w - textWidth) / 2);
        } else {
            text.setX(x + 2);
        }
        text.setY(y + BAR_HEIGHT - 3);
        text.setMouseTransparent(true);
        addNode(text);
    }

    private void addNode(javafx.scene.Node node) {
        dynamicNodes.add(node);
        getChartChildren().add(node);
    }

    private static int idxAt(List<Integer> ti, int slot) {
        if (slot >= ti.size()) return -1;
        Integer v = ti.get(slot);
        if (v == null) return -1;
        return (slot == IDX_CHARGE && v >= 0) || (slot != IDX_CHARGE && v > 0) ? v : -1;
    }
}
