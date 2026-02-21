package org.artisan.ui.components;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.chartfx.ui.geometry.Side;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.artisan.ui.vm.RoastViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact XYChart (90px) showing Gas, Air, Drum as step-function series.
 * X-axis synchronized with main chart via binding.
 *
 * @Disabled — not rendered in current layout version
 */
public final class ModulationChart extends javafx.scene.layout.VBox {

    private static final Color GAS_COLOR = Color.web("#F39C12");
    private static final Color AIR_COLOR = Color.web("#3498DB");
    private static final Color DRUM_COLOR = Color.web("#9B59B6");

    private final XYChart chart;
    private final DefaultNumericAxis xAxis;
    private final DefaultNumericAxis yAxis;
    private final DoubleDataSet dataGas;
    private final DoubleDataSet dataAir;
    private final DoubleDataSet dataDrum;

    private final List<Double> times = new ArrayList<>();
    private final List<Double> gasVals = new ArrayList<>();
    private final List<Double> airVals = new ArrayList<>();
    private final List<Double> drumVals = new ArrayList<>();

    public ModulationChart(RoastViewModel vm) {
        setMinHeight(90);
        setPrefHeight(90);
        setMaxHeight(90);

        xAxis = new DefaultNumericAxis("");
        xAxis.setSide(Side.BOTTOM);
        xAxis.setMin(0);
        xAxis.setMax(1500);
        xAxis.setTickUnit(300);
        xAxis.setAutoRangeRounding(false);
        xAxis.setAnimated(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number sec) {
                int s = sec != null ? (int) Math.round(sec.doubleValue()) : 0;
                return String.format("%d:%02d", s / 60, s % 60);
            }
            @Override
            public Number fromString(String s) { return 0; }
        });

        yAxis = new DefaultNumericAxis("");
        yAxis.setSide(Side.LEFT);
        yAxis.setMin(0);
        yAxis.setMax(100);
        yAxis.setTickUnit(25);
        yAxis.setAutoRangeRounding(false);
        yAxis.setAnimated(false);

        chart = new XYChart(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.getStyleClass().add("modulation-chart");

        dataGas = new DoubleDataSet("Gas");
        dataAir = new DoubleDataSet("Air");
        dataDrum = new DoubleDataSet("Drum");

        ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
        renderer.getAxes().add(yAxis);
        renderer.getDatasets().addAll(dataGas, dataAir, dataDrum);
        chart.getRenderers().clear();
        chart.getRenderers().add(renderer);

        applyColors();

        getChildren().add(chart);

        vm.gasValueProperty().addListener((o, ov, nv) -> addPoint(vm.getElapsedSec(), nv.doubleValue(), vm.getAirValue(), vm.getDrumValue()));
        vm.airValueProperty().addListener((o, ov, nv) -> addPoint(vm.getElapsedSec(), vm.getGasValue(), nv.doubleValue(), vm.getDrumValue()));
        vm.drumValueProperty().addListener((o, ov, nv) -> addPoint(vm.getElapsedSec(), vm.getGasValue(), vm.getAirValue(), nv.doubleValue()));
        vm.elapsedSecProperty().addListener((o, ov, nv) -> {
            double t = nv != null ? nv.doubleValue() : 0;
            addPoint(t, vm.getGasValue(), vm.getAirValue(), vm.getDrumValue());
        });
    }

    private void applyColors() {
        dataGas.setStyle("-fx-stroke: " + toHex(GAS_COLOR) + "; -fx-stroke-width: 1.5px;");
        dataAir.setStyle("-fx-stroke: " + toHex(AIR_COLOR) + "; -fx-stroke-width: 1.5px;");
        dataDrum.setStyle("-fx-stroke: " + toHex(DRUM_COLOR) + "; -fx-stroke-width: 1.5px;");
    }

    private static String toHex(Color c) {
        if (c == null) return "#000000";
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private void addPoint(double timeSec, double gas, double air, double drum) {
        if (!Double.isFinite(timeSec) || timeSec < 0) return;
        double g = Double.isFinite(gas) ? Math.max(0, Math.min(100, gas)) : 0;
        double a = Double.isFinite(air) ? Math.max(0, Math.min(100, air)) : 0;
        double d = Double.isFinite(drum) ? Math.max(0, Math.min(100, drum)) : 0;

        if (!times.isEmpty() && timeSec <= times.get(times.size() - 1) + 0.01) return;

        if (times.isEmpty()) {
            times.add(timeSec);
            gasVals.add(g);
            airVals.add(a);
            drumVals.add(d);
        } else {
            double lastG = gasVals.get(gasVals.size() - 1);
            double lastA = airVals.get(airVals.size() - 1);
            double lastD = drumVals.get(drumVals.size() - 1);
            times.add(timeSec);
            gasVals.add(lastG);
            airVals.add(lastA);
            drumVals.add(lastD);
            times.add(timeSec);
            gasVals.add(g);
            airVals.add(a);
            drumVals.add(d);
        }
        commitToChart();
    }

    private void commitToChart() {
        int n = times.size();
        if (n == 0) {
            dataGas.clearData();
            dataAir.clearData();
            dataDrum.clearData();
            return;
        }
        double[] tx = new double[n];
        double[] g = new double[n];
        double[] a = new double[n];
        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            tx[i] = times.get(i);
            g[i] = gasVals.get(i);
            a[i] = airVals.get(i);
            d[i] = drumVals.get(i);
        }
        dataGas.set(tx, g);
        dataAir.set(tx, a);
        dataDrum.set(tx, d);
    }

    /** Bind this chart's X-axis range to the main chart. */
    public void bindXAxis(DefaultNumericAxis mainXAxis) {
        if (mainXAxis != null) {
            xAxis.autoRangingProperty().bind(mainXAxis.autoRangingProperty());
            xAxis.minProperty().bindBidirectional(mainXAxis.minProperty());
            xAxis.maxProperty().bindBidirectional(mainXAxis.maxProperty());
        }
    }

    public XYChart getChart() { return chart; }
    public DefaultNumericAxis getXAxis() { return xAxis; }
    public Node getView() { return this; }

    /** Clear all data (e.g. on new roast). */
    public void clear() {
        times.clear();
        gasVals.clear();
        airVals.clear();
        drumVals.clear();
        dataGas.clearData();
        dataAir.clearData();
        dataDrum.clearData();
    }
}
