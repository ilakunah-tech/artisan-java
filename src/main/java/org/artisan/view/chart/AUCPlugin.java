package org.artisan.view.chart;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.Axis;
import io.fair_acc.chartfx.plugins.ChartPlugin;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.AxisConfig;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Chart-FX plugin that renders the Area Under the Curve (AUC) between the BT curve
 * and a configurable base temperature. Uses a vertical gradient fill (top = color, bottom = transparent)
 * for a modern look, plus a horizontal guide line at the base temperature and an AUC value label.
 */
public final class AUCPlugin extends ChartPlugin {

    private final Polygon aucPolygon = new Polygon();
    private final Line guideLine = new Line();
    private final Text aucLabel = new Text();

    private CanvasData canvasData;
    private ColorConfig colorConfig;
    private DisplaySettings displaySettings;
    private AxisConfig axisConfig;

    public AUCPlugin() {
        aucPolygon.setMouseTransparent(true);
        guideLine.setMouseTransparent(true);
        guideLine.setStrokeWidth(1);
        aucLabel.setMouseTransparent(true);
        aucLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        getChartChildren().addAll(aucPolygon, guideLine, aucLabel);
        hide();
    }

    public void setCanvasData(CanvasData data)         { this.canvasData = data; }
    public void setColorConfig(ColorConfig cfg)        { this.colorConfig = cfg; }
    public void setDisplaySettings(DisplaySettings ds) { this.displaySettings = ds; }
    public void setAxisConfig(AxisConfig cfg)          { this.axisConfig = cfg; }

    /**
     * Refreshes the AUC overlay from current data.
     * @param timex     time array
     * @param btDisplay BT values in display units
     */
    public void refresh(List<Double> timex, List<Double> btDisplay) {
        if (getChart() == null || canvasData == null || colorConfig == null
                || displaySettings == null || timex == null || timex.isEmpty()
                || btDisplay == null || btDisplay.size() < timex.size()) {
            hide();
            return;
        }

        double baseTempC = displaySettings.getAucBaseTemp();
        if (axisConfig != null && axisConfig.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT) {
            baseTempC = AxisConfig.celsiusToFahrenheit(baseTempC);
        }

        int chargeIdx = canvasData.getChargeIndex() >= 0 ? canvasData.getChargeIndex() : 0;
        int dropIdx = canvasData.getDropIndex() > 0 && canvasData.getDropIndex() < timex.size()
                ? canvasData.getDropIndex() : timex.size() - 1;
        if (chargeIdx >= dropIdx) { hide(); return; }

        XYChart xyChart = (XYChart) getChart();
        Axis xAxis = xyChart.getXAxis();
        Axis yAxis = xyChart.getYAxis();
        double canvasW = xyChart.getCanvas().getWidth();
        double canvasH = xyChart.getCanvas().getHeight();
        if (canvasW <= 0 || canvasH <= 0) { hide(); return; }
        double plotX = xyChart.getCanvas().getLayoutX();
        double plotY = xyChart.getCanvas().getLayoutY();

        double baseY = yAxis.getDisplayPosition(baseTempC) + plotY;
        baseY = clamp(baseY, plotY, plotY + canvasH);

        Color guideColor = colorConfig.getPaletteColor("aucguide");
        if (guideColor != null) {
            guideLine.setStroke(guideColor);
            guideLine.setStartX(plotX);
            guideLine.setStartY(baseY);
            guideLine.setEndX(plotX + canvasW);
            guideLine.setEndY(baseY);
            guideLine.setVisible(true);
        } else {
            guideLine.setVisible(false);
        }

        Color areaColor = colorConfig.getPaletteColor("aucarea");
        double areaAlpha = colorConfig.getPaletteAlpha("aucarea");
        if (!Double.isFinite(areaAlpha)) areaAlpha = 0.3;

        Color fillTop = areaColor != null
                ? Color.color(areaColor.getRed(), areaColor.getGreen(), areaColor.getBlue(), Math.min(0.5, areaAlpha + 0.1))
                : Color.color(0.45, 0.45, 0.45, 0.4);
        Color fillBottom = areaColor != null
                ? Color.color(areaColor.getRed(), areaColor.getGreen(), areaColor.getBlue(), 0.05)
                : Color.color(0.45, 0.45, 0.45, 0.05);

        double minY = plotY;
        double maxY = plotY + canvasH;
        LinearGradient gradient = new LinearGradient(
                0, minY, 0, maxY, false, CycleMethod.NO_CYCLE,
                new Stop(0, fillTop),
                new Stop(1, fillBottom));
        aucPolygon.setFill(gradient);
        aucPolygon.setStroke(null);

        List<Double> points = new ArrayList<>();
        double xStart = xAxis.getDisplayPosition(timex.get(chargeIdx)) + plotX;
        double xEnd = xAxis.getDisplayPosition(timex.get(dropIdx)) + plotX;

        points.add(xStart);
        points.add(baseY);

        double aucValue = 0.0;
        for (int i = chargeIdx; i <= dropIdx && i < btDisplay.size(); i++) {
            double bt = btDisplay.get(i);
            if (!Double.isFinite(bt)) continue;
            double x = xAxis.getDisplayPosition(timex.get(i)) + plotX;
            double y = yAxis.getDisplayPosition(bt) + plotY;
            y = clamp(y, plotY, plotY + canvasH);
            points.add(x);
            points.add(y);
            if (i > chargeIdx && i < btDisplay.size()) {
                double dt = timex.get(i) - timex.get(i - 1);
                double prevBt = btDisplay.get(i - 1);
                if (Double.isFinite(prevBt) && Double.isFinite(dt) && dt > 0) {
                    aucValue += ((prevBt + bt) / 2.0 - displaySettings.getAucBaseTemp()) * (dt / 60.0);
                }
            }
        }
        points.add(xEnd);
        points.add(baseY);

        aucPolygon.getPoints().setAll(points);
        aucPolygon.setVisible(true);

        aucLabel.setText(String.format("AUC: %.0f", aucValue));
        aucLabel.setFill(areaColor != null ? areaColor : Color.GRAY);
        aucLabel.setX(xEnd + 4);
        aucLabel.setY(baseY - 6);
        aucLabel.setVisible(true);
    }

    private void hide() {
        aucPolygon.setVisible(false);
        guideLine.setVisible(false);
        aucLabel.setVisible(false);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
