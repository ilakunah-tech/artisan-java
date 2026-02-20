package org.artisan.view.chart;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.EditAxis;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.chartfx.ui.geometry.Side;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.util.StringConverter;
import org.artisan.model.AxisConfig;

/**
 * Factory that builds an {@link XYChart} with dual Y-axes (temperature left, RoR right)
 * and separate {@link ErrorDataSetRenderer}s for each axis. Replaces the single-axis
 * setup from the old monolithic RoastChartController.
 */
public final class ChartFactory {

    private final XYChart chart;
    private final DefaultNumericAxis xAxis;
    private final DefaultNumericAxis tempAxis;
    private final DefaultNumericAxis rorAxis;
    private final ErrorDataSetRenderer tempRenderer;
    private final ErrorDataSetRenderer rorRenderer;

    private final DoubleDataSet dataET;
    private final DoubleDataSet dataBT;
    private final DoubleDataSet dataDeltaET;
    private final DoubleDataSet dataDeltaBT;

    private final DoubleDataSet dataBgET;
    private final DoubleDataSet dataBgBT;
    private final DoubleDataSet dataBgDeltaET;
    private final DoubleDataSet dataBgDeltaBT;

    private int chargeTimeSec = -1;

    public ChartFactory(AxisConfig axisCfg) {
        AxisConfig cfg = axisCfg != null ? axisCfg : new AxisConfig();

        xAxis = new DefaultNumericAxis("Time");
        xAxis.setSide(Side.BOTTOM);
        xAxis.setMin(cfg.getTimeMinSec());
        xAxis.setMax(cfg.getTimeMaxSec());
        xAxis.setTickUnit(cfg.getTimeTickStepSec());
        xAxis.setAutoRangeRounding(false);
        xAxis.setAnimated(false);
        xAxis.setTickLabelFormatter(new TimeAxisFormatter());

        tempAxis = new DefaultNumericAxis("Temp (" + cfg.getTempUnitString() + ")");
        tempAxis.setSide(Side.LEFT);
        tempAxis.setMin(cfg.getTempMin());
        tempAxis.setMax(cfg.getTempMax());
        tempAxis.setTickUnit(cfg.getTempTickStep());
        tempAxis.setAutoRangeRounding(false);
        tempAxis.setAnimated(false);

        rorAxis = new DefaultNumericAxis("RoR (" + cfg.getTempUnitString() + "/min)");
        rorAxis.setSide(Side.RIGHT);
        rorAxis.setMin(cfg.getRorMin());
        rorAxis.setMax(cfg.getRorMax());
        rorAxis.setAutoRangeRounding(false);
        rorAxis.setAnimated(false);

        chart = new XYChart(xAxis, tempAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.getStyleClass().add("ri5-chart");
        xAxis.getStyleClass().add("ri5-axis");
        tempAxis.getStyleClass().add("ri5-axis");
        rorAxis.getStyleClass().add("ri5-axis");

        Zoomer zoomer = new Zoomer();
        chart.getPlugins().add(zoomer);

        dataET = new DoubleDataSet("ET");
        dataBT = new DoubleDataSet("BT");
        dataDeltaET = new DoubleDataSet("ΔET");
        dataDeltaBT = new DoubleDataSet("ΔBT");

        dataBgET = new DoubleDataSet("BG ET");
        dataBgBT = new DoubleDataSet("BG BT");
        dataBgDeltaET = new DoubleDataSet("BG ΔET");
        dataBgDeltaBT = new DoubleDataSet("BG ΔBT");

        tempRenderer = new ErrorDataSetRenderer();
        tempRenderer.getAxes().add(tempAxis);
        tempRenderer.getDatasets().addAll(dataET, dataBT);

        rorRenderer = new ErrorDataSetRenderer();
        rorRenderer.getAxes().add(rorAxis);
        rorRenderer.getDatasets().addAll(dataDeltaET, dataDeltaBT);

        chart.getRenderers().clear();
        chart.getRenderers().addAll(tempRenderer, rorRenderer);
    }

    public void setChargeTimeSec(int chargeTimeSec) {
        this.chargeTimeSec = chargeTimeSec;
    }

    public int getChargeTimeSec() {
        return chargeTimeSec;
    }

    public XYChart getChart()                   { return chart; }
    public DefaultNumericAxis getXAxis()         { return xAxis; }
    public DefaultNumericAxis getTempAxis()      { return tempAxis; }
    public DefaultNumericAxis getRorAxis()       { return rorAxis; }
    public ErrorDataSetRenderer getTempRenderer(){ return tempRenderer; }
    public ErrorDataSetRenderer getRorRenderer() { return rorRenderer; }

    public DoubleDataSet getDataET()          { return dataET; }
    public DoubleDataSet getDataBT()          { return dataBT; }
    public DoubleDataSet getDataDeltaET()     { return dataDeltaET; }
    public DoubleDataSet getDataDeltaBT()     { return dataDeltaBT; }
    public DoubleDataSet getDataBgET()        { return dataBgET; }
    public DoubleDataSet getDataBgBT()        { return dataBgBT; }
    public DoubleDataSet getDataBgDeltaET()   { return dataBgDeltaET; }
    public DoubleDataSet getDataBgDeltaBT()   { return dataBgDeltaBT; }

    /**
     * Applies axis config: time range, temp range, ror range, units, auto-scale.
     */
    public void applyAxisConfig(AxisConfig cfg) {
        if (cfg == null) return;
        xAxis.setMin(cfg.getTimeMinSec());
        xAxis.setMax(cfg.getTimeMaxSec());
        xAxis.setTickUnit(cfg.getTimeTickStepSec());

        String unitLabel = cfg.getTempUnitString();
        tempAxis.setName("Temp (" + unitLabel + ")");
        tempAxis.setMin(cfg.getTempMin());
        tempAxis.setMax(cfg.getTempMax());
        tempAxis.setTickUnit(cfg.getTempTickStep());

        rorAxis.setName("RoR (" + unitLabel + "/min)");
        rorAxis.setMin(cfg.getRorMin());
        rorAxis.setMax(cfg.getRorMax());
    }

    /**
     * Auto-scales the temperature Y-axis from visible data with 5% margin.
     */
    public void autoScaleTemp(double dataMin, double dataMax) {
        if (!Double.isFinite(dataMin) || !Double.isFinite(dataMax) || dataMax < dataMin) return;
        double margin = Math.max(1, (dataMax - dataMin) * 0.05);
        tempAxis.setMin(dataMin - margin);
        tempAxis.setMax(dataMax + margin);
    }

    /**
     * Auto-scales the RoR Y-axis from visible delta data with 10% margin.
     */
    public void autoScaleRor(double dataMin, double dataMax) {
        if (!Double.isFinite(dataMin) || !Double.isFinite(dataMax) || dataMax < dataMin) return;
        double margin = Math.max(1, (dataMax - dataMin) * 0.10);
        rorAxis.setMin(dataMin - margin);
        rorAxis.setMax(dataMax + margin);
    }

    /**
     * Adds background datasets to the appropriate renderers.
     */
    public void addBackgroundDatasets(boolean bgET, boolean bgBT, boolean bgDeltaET, boolean bgDeltaBT) {
        removeBackgroundDatasets();
        if (bgET) tempRenderer.getDatasets().add(0, dataBgET);
        if (bgBT) tempRenderer.getDatasets().add(bgET ? 1 : 0, dataBgBT);
        if (bgDeltaET) rorRenderer.getDatasets().add(0, dataBgDeltaET);
        if (bgDeltaBT) rorRenderer.getDatasets().add(bgDeltaET ? 1 : 0, dataBgDeltaBT);
    }

    /**
     * Removes all background datasets from renderers.
     */
    public void removeBackgroundDatasets() {
        tempRenderer.getDatasets().removeAll(dataBgET, dataBgBT);
        rorRenderer.getDatasets().removeAll(dataBgDeltaET, dataBgDeltaBT);
    }

    /**
     * Formats the X-axis: seconds to m:ss, optionally relative to CHARGE.
     */
    private class TimeAxisFormatter extends StringConverter<Number> {
        @Override
        public String toString(Number sec) {
            int totalSec = sec != null ? (int) Math.round(sec.doubleValue()) : 0;
            if (chargeTimeSec >= 0) {
                totalSec -= chargeTimeSec;
            }
            String sign = totalSec < 0 ? "-" : "";
            int abs = Math.abs(totalSec);
            int m = abs / 60;
            int s = abs % 60;
            if (s == 0 && m > 0) return sign + m + ":00";
            return String.format("%s%d:%02d", sign, m, s);
        }

        @Override
        public Number fromString(String s) {
            try {
                if (s == null) return 0;
                boolean neg = s.startsWith("-");
                String clean = neg ? s.substring(1) : s;
                String[] parts = clean.split(":");
                if (parts.length == 2) {
                    int val = Integer.parseInt(parts[0].trim()) * 60 + Integer.parseInt(parts[1].trim());
                    return neg ? -val + chargeTimeSec : val + (chargeTimeSec >= 0 ? chargeTimeSec : 0);
                }
            } catch (NumberFormatException ignored) {}
            return 0;
        }
    }

    public static String toHex(javafx.scene.paint.Color c) {
        if (c == null) return "#000000";
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    public static String styleFor(boolean visible, javafx.scene.paint.Color color, int widthPx) {
        if (!visible) return "-fx-stroke: transparent; -fx-stroke-width: 0px;";
        return "-fx-stroke: " + toHex(color) + "; -fx-stroke-width: " + widthPx + "px;";
    }

    public static String styleForDashed(boolean visible, javafx.scene.paint.Color color, double alpha, int widthPx) {
        if (!visible || color == null) return "-fx-stroke: transparent; -fx-stroke-width: 0px;";
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        String stroke = String.format("rgba(%d,%d,%d,%.3f)", r, g, b, alpha);
        return "-fx-stroke: " + stroke + "; -fx-stroke-width: " + widthPx + "px; -fx-stroke-dash-array: 8 6;";
    }
}
