package org.artisan.view.chart;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.Axis;
import io.fair_acc.chartfx.plugins.ChartPlugin;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.AxisConfig;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Chart-FX plugin providing a precision crosshair with snap-to-curve functionality
 * and a multi-value readout panel showing time, BT, ET, ΔBT, ΔET at the cursor position.
 * Replaces the old crosshairLayer StackPane overlay.
 */
public final class PrecisionCrosshairPlugin extends ChartPlugin {

    private final Line crosshairV = new Line();
    private final Line crosshairH = new Line();
    private final Circle snapDot = new Circle(4);
    private final Rectangle readoutBg = new Rectangle();
    private final Text readoutText = new Text();

    private CanvasData canvasData;
    private ColorConfig colorConfig;
    private DisplaySettings displaySettings;
    private AxisConfig axisConfig;
    private BiConsumer<Double, Double> onCursorMoved;

    public PrecisionCrosshairPlugin() {
        crosshairV.getStrokeDashArray().addAll(4.0, 4.0);
        crosshairH.getStrokeDashArray().addAll(4.0, 4.0);
        crosshairV.setStrokeWidth(1);
        crosshairH.setStrokeWidth(1);
        crosshairV.setMouseTransparent(true);
        crosshairH.setMouseTransparent(true);
        snapDot.setMouseTransparent(true);
        readoutBg.setMouseTransparent(true);
        readoutText.setMouseTransparent(true);
        readoutText.setStyle("-fx-font-size: 10px; -fx-font-weight: 500;");
        readoutBg.setArcWidth(8);
        readoutBg.setArcHeight(8);
        getChartChildren().addAll(crosshairV, crosshairH, snapDot, readoutBg, readoutText);
        hideAll();

        registerInputEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseMoved);
        registerInputEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            if (onCursorMoved != null) onCursorMoved.accept(Double.NaN, Double.NaN);
            hideAll();
        });
    }

    public void setCanvasData(CanvasData data)         { this.canvasData = data; }
    public void setColorConfig(ColorConfig cfg)        { this.colorConfig = cfg; }
    public void setDisplaySettings(DisplaySettings ds) { this.displaySettings = ds; }
    public void setAxisConfig(AxisConfig cfg)          { this.axisConfig = cfg; }
    /** Callback (timeSec, bt) when mouse moves over chart. */
    public void setOnCursorMoved(BiConsumer<Double, Double> c) { this.onCursorMoved = c; }

    private void onMouseMoved(MouseEvent e) {
        if (displaySettings != null && !displaySettings.isShowCrosshair()) {
            hideAll();
            return;
        }
        XYChart xyChart = (XYChart) getChart();
        if (xyChart == null || canvasData == null || colorConfig == null) {
            hideAll();
            return;
        }

        Axis xAxis = xyChart.getXAxis();
        Axis yAxis = xyChart.getYAxis();
        double canvasW = xyChart.getCanvas().getWidth();
        double canvasH = xyChart.getCanvas().getHeight();
        if (canvasW <= 0 || canvasH <= 0) { hideAll(); return; }
        double plotX = xyChart.getCanvas().getLayoutX();
        double plotY = xyChart.getCanvas().getLayoutY();

        double mx = e.getX();
        double my = e.getY();

        if (mx < plotX || mx > plotX + canvasW || my < plotY || my > plotY + canvasH) {
            if (onCursorMoved != null) onCursorMoved.accept(Double.NaN, Double.NaN);
            hideAll();
            return;
        }

        Color markersColor = colorConfig.getPaletteColor("markers");
        Color lineColor = markersColor != null
                ? Color.color(markersColor.getRed(), markersColor.getGreen(), markersColor.getBlue(), 0.6)
                : Color.color(0.5, 0.5, 0.5, 0.6);

        crosshairV.setStartX(mx); crosshairV.setStartY(plotY);
        crosshairV.setEndX(mx);   crosshairV.setEndY(plotY + canvasH);
        crosshairV.setStroke(lineColor);
        crosshairH.setStartX(plotX); crosshairH.setStartY(my);
        crosshairH.setEndX(plotX + canvasW); crosshairH.setEndY(my);
        crosshairH.setStroke(lineColor);
        crosshairV.setVisible(true);
        crosshairH.setVisible(true);

        double timeSec = xAxis.getValueForDisplay(mx - plotX);

        double bt = Double.NaN, et = Double.NaN, deltaBT = Double.NaN, deltaET = Double.NaN;
        List<Double> timex = canvasData.getTimex();
        List<Double> temp1 = canvasData.getTemp1();
        List<Double> temp2 = canvasData.getTemp2();
        List<Double> delta1 = canvasData.getDelta1();
        List<Double> delta2 = canvasData.getDelta2();
        if (timex != null && !timex.isEmpty()) {
            int idx = EventMarkerPlugin.nearestTimeIndex(timex, timeSec);
            if (idx >= 0) {
                if (temp1 != null && idx < temp1.size()) et = temp1.get(idx);
                if (temp2 != null && idx < temp2.size()) bt = temp2.get(idx);
                if (delta1 != null && idx < delta1.size()) deltaET = delta1.get(idx);
                if (delta2 != null && idx < delta2.size()) deltaBT = delta2.get(idx);
                if (idx + 1 < timex.size() && timeSec > timex.get(idx)) {
                    double t0 = timex.get(idx), t1 = timex.get(idx + 1);
                    double frac = (t1 - t0) > 0 ? (timeSec - t0) / (t1 - t0) : 0;
                    if (temp1 != null && idx + 1 < temp1.size()) et = lerp(temp1.get(idx), temp1.get(idx + 1), frac);
                    if (temp2 != null && idx + 1 < temp2.size()) bt = lerp(temp2.get(idx), temp2.get(idx + 1), frac);
                    if (delta1 != null && idx + 1 < delta1.size()) deltaET = lerp(delta1.get(idx), delta1.get(idx + 1), frac);
                    if (delta2 != null && idx + 1 < delta2.size()) deltaBT = lerp(delta2.get(idx), delta2.get(idx + 1), frac);
                }

                if (Double.isFinite(bt)) {
                    double displayBT = bt;
                    if (axisConfig != null && axisConfig.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT) {
                        displayBT = AxisConfig.celsiusToFahrenheit(bt);
                    }
                    double snapY = yAxis.getDisplayPosition(displayBT) + plotY;
                    snapDot.setCenterX(mx);
                    snapDot.setCenterY(snapY);
                    snapDot.setFill(colorConfig.getCurveBT() != null ? colorConfig.getCurveBT() : Color.RED);
                    snapDot.setVisible(true);
                } else {
                    snapDot.setVisible(false);
                }
            }
        }

        boolean useF = axisConfig != null && axisConfig.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT;
        if (useF) {
            if (Double.isFinite(bt)) bt = AxisConfig.celsiusToFahrenheit(bt);
            if (Double.isFinite(et)) et = AxisConfig.celsiusToFahrenheit(et);
        }
        if (onCursorMoved != null) {
            double btCelsius = Double.isFinite(bt) && useF ? AxisConfig.fahrenheitToCelsius(bt) : bt;
            onCursorMoved.accept(timeSec, btCelsius);
        }
        String unitStr = useF ? "°F" : "°C";
        int totalSec = (int) Math.round(timeSec);
        String btStr = Double.isFinite(bt) ? String.format("%.1f", bt) : "—";
        String etStr = Double.isFinite(et) ? String.format("%.1f", et) : "—";
        String deltaBTStr = Double.isFinite(deltaBT) ? String.format("%.1f", deltaBT) : "—";
        String deltaETStr = Double.isFinite(deltaET) ? String.format("%.1f", deltaET) : "—";

        String text = String.format("%d:%02d  BT: %s%s  ET: %s%s  ΔBT: %s  ΔET: %s",
                totalSec / 60, totalSec % 60, btStr, unitStr, etStr, unitStr, deltaBTStr, deltaETStr);
        readoutText.setText(text);
        readoutText.setFill(Color.WHITESMOKE);

        double textW = readoutText.getLayoutBounds().getWidth();
        double textH = readoutText.getLayoutBounds().getHeight();
        double rx = Math.min(mx + 12, plotX + canvasW - textW - 16);
        double ry = my - textH - 10;
        if (ry < plotY) ry = my + 16;

        readoutBg.setX(rx - 6);
        readoutBg.setY(ry - textH);
        readoutBg.setWidth(textW + 12);
        readoutBg.setHeight(textH + 8);
        readoutBg.setFill(Color.color(0.15, 0.15, 0.18, 0.92));
        readoutBg.setStroke(Color.color(1, 1, 1, 0.15));
        readoutBg.setStrokeWidth(1);
        readoutBg.setVisible(true);
        readoutText.setX(rx);
        readoutText.setY(ry);
        readoutText.setVisible(true);
    }

    private void hideAll() {
        crosshairV.setVisible(false);
        crosshairH.setVisible(false);
        snapDot.setVisible(false);
        readoutBg.setVisible(false);
        readoutText.setVisible(false);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
