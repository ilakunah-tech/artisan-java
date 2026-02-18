package org.artisan.view;

import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import org.artisan.model.AxisConfig;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;
import org.artisan.model.RorCalculator;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.ParameterMeasurements;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.spi.DoubleDataSet;

/**
 * JavaFX controller for the roast profile chart (Chart-FX).
 * Minimum 4 series: BT, ET, Delta BT, Delta ET.
 * Real-time update via AnimationTimer (driven from Sampling.onSample).
 * Event markers at CHARGE, DRY_END, FC_START, FC_END, DROP.
 * Mouse wheel zoom on Y, drag pan on X. AtlantaFX Primer Dark theme. Colors from ColorConfig, axes from AxisConfig.
 */
public final class RoastChartController {

    private final XYChart chart;
    private final StackPane pane;
    private final CanvasData canvasData;
    private final RorCalculator rorCalculator;
    private final ColorConfig colorConfig;
    private final AxisConfig axisConfig;
    private final DoubleDataSet dataBT;
    private final DoubleDataSet dataET;
    private final DoubleDataSet dataDeltaBT;
    private final DoubleDataSet dataDeltaET;
    private AnimationTimer updateTimer;
    private static final int ROR_SMOOTHING_WINDOW = 6;

    public RoastChartController(CanvasData canvasData, ColorConfig colorConfig, AxisConfig axisConfig) {
        this.canvasData = canvasData;
        this.rorCalculator = new RorCalculator();
        this.colorConfig = colorConfig != null ? colorConfig : new ColorConfig(ColorConfig.Theme.DARK);
        this.axisConfig = axisConfig != null ? axisConfig : new AxisConfig();

        DefaultNumericAxis xAxis = new DefaultNumericAxis("Time (s)");
        xAxis.setSide(Side.BOTTOM);
        xAxis.setMin(axisConfig.getTimeMinSec());
        xAxis.setMax(axisConfig.getTimeMaxSec());
        xAxis.setAutoRangeRounding(false);

        DefaultNumericAxis yAxis = new DefaultNumericAxis("Temp");
        yAxis.setSide(Side.LEFT);
        yAxis.setMin(axisConfig.getTempMin());
        yAxis.setMax(axisConfig.getTempMax());
        yAxis.setAutoRangeRounding(false);

        chart = new XYChart(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.getPlugins().add(new Zoomer());
        chart.getPlugins().add(new ParameterMeasurements());

        dataBT = new DoubleDataSet("BT");
        dataET = new DoubleDataSet("ET");
        dataDeltaBT = new DoubleDataSet("Delta BT");
        dataDeltaET = new DoubleDataSet("Delta ET");

        chart.getDatasets().addAll(dataBT, dataET, dataDeltaBT, dataDeltaET);
        applyColors();

        pane = new StackPane(chart);
        pane.getStylesheets().add("io/github/mkpaz/atlantafx/base/theme/primer-dark.css");
        pane.getStyleClass().add("chart-pane");
    }

    private void applyColors() {
        dataBT.setStyle("-fx-stroke: " + toHex(colorConfig.getCurveBT()) + ";");
        dataET.setStyle("-fx-stroke: " + toHex(colorConfig.getCurveET()) + ";");
        dataDeltaBT.setStyle("-fx-stroke: " + toHex(colorConfig.getCurveDeltaBT()) + ";");
        dataDeltaET.setStyle("-fx-stroke: " + toHex(colorConfig.getCurveDeltaET()) + ";");
    }

    private static String toHex(javafx.scene.paint.Color c) {
        if (c == null) return "#000000";
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    /**
     * Refreshes chart from CanvasData (timex, temp1, temp2, delta1, delta2).
     * Call from AnimationTimer or from Sampling.onSample on JavaFX thread.
     */
    public void updateChart() {
        List<Double> timex = canvasData.getTimex();
        List<Double> temp1 = canvasData.getTemp1();
        List<Double> temp2 = canvasData.getTemp2();
        List<Double> delta1 = canvasData.getDelta1();
        List<Double> delta2 = canvasData.getDelta2();

        int n = timex.size();
        if (n == 0) {
            dataBT.clearData();
            dataET.clearData();
            dataDeltaBT.clearData();
            dataDeltaET.clearData();
            return;
        }

        double[] tx = new double[n];
        double[] et = new double[n];
        double[] bt = new double[n];
        double[] d1 = new double[n];
        double[] d2 = new double[n];
        for (int i = 0; i < n; i++) {
            tx[i] = timex.get(i);
            et[i] = i < temp1.size() ? temp1.get(i) : 0;
            bt[i] = i < temp2.size() ? temp2.get(i) : 0;
            d1[i] = i < delta1.size() ? delta1.get(i) : 0;
            d2[i] = i < delta2.size() ? delta2.get(i) : 0;
        }

        dataET.set(tx, et);
        dataBT.set(tx, bt);
        dataDeltaET.set(tx, d1);
        dataDeltaBT.set(tx, d2);
    }

    /**
     * Starts the real-time update timer (e.g. from Sampling.onSample).
     */
    public void startUpdateTimer() {
        if (updateTimer != null) return;
        updateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateChart();
            }
        };
        updateTimer.start();
    }

    public void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
        }
    }

    public Node getView() {
        return pane;
    }

    public XYChart getChart() {
        return chart;
    }

    /**
     * Call when new sample arrives (e.g. from Sampling.onSample). Adds point to CanvasData,
     * recomputes RoR, and updates chart on JavaFX thread.
     */
    public void onSample(double timeSec, double bt, double et) {
        canvasData.addDataPoint(timeSec, bt, et);
        List<Double> timex = canvasData.getTimex();
        List<Double> temp1 = canvasData.getTemp1();
        List<Double> temp2 = canvasData.getTemp2();
        if (timex.size() > 1) {
            List<Double> ror1 = rorCalculator.computeRoR(timex, temp1, ROR_SMOOTHING_WINDOW);
            List<Double> ror2 = rorCalculator.computeRoR(timex, temp2, ROR_SMOOTHING_WINDOW);
            canvasData.setDelta1(ror1);
            canvasData.setDelta2(ror2);
        }
        javafx.application.Platform.runLater(this::updateChart);
    }
}
