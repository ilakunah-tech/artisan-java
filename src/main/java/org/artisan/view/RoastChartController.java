package org.artisan.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import org.artisan.controller.BackgroundSettings;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.*;
import org.artisan.util.CurveSmoothing;
import org.artisan.view.chart.*;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.dataset.spi.DoubleDataSet;

/**
 * JavaFX controller for the roast profile chart.
 * <p>
 * Architecture: dual Y-axes (temperature left, RoR right) via {@link ChartFactory},
 * with composable {@code ChartPlugin}s for overlays:
 * <ul>
 *   <li>{@link PhaseShadePlugin} — phase shading rectangles</li>
 *   <li>{@link EventMarkerPlugin} — event vertical lines, annotations, drag, legend</li>
 *   <li>{@link AUCPlugin} — area-under-curve with gradient fill</li>
 *   <li>{@link PrecisionCrosshairPlugin} — crosshair with value readout</li>
 *   <li>{@link StatisticsPlugin} — post-roast phase bars and DTR</li>
 * </ul>
 * Background profiles managed by {@link BackgroundManager}.
 */
public final class RoastChartController {

    private static final int DEFAULT_ROR_SMOOTHING = 5;

    private final ChartFactory chartFactory;
    private final CanvasData canvasData;
    private final RorCalculator rorCalculator;
    private final ColorConfig colorConfig;
    private final AxisConfig axisConfig;

    private final PhaseShadePlugin phasePlugin;
    private final EventMarkerPlugin eventPlugin;
    private final AUCPlugin aucPlugin;
    private final PrecisionCrosshairPlugin crosshairPlugin;
    private final StatisticsPlugin statsPlugin;
    private final BackgroundManager bgManager;

    private DisplaySettings displaySettings;
    private LiveRorCalculator liveRorET;
    private LiveRorCalculator liveRorBT;
    private PhasesConfig phasesConfig;
    private CurveSet curveSet;

    private boolean chartDirty = false;
    private boolean liveRecording = false;
    private boolean zoomFollow = true;

    private java.util.function.Consumer<ChartClickInfo> onChartBodyClick;

    /** Data passed to onChartBodyClick: time (sec), index, BT, ET at click position. */
    public static final class ChartClickInfo {
        public final double timeSec;
        public final int timeIndex;
        public final double bt;
        public final double et;

        public ChartClickInfo(double timeSec, int timeIndex, double bt, double et) {
            this.timeSec = timeSec;
            this.timeIndex = timeIndex;
            this.bt = bt;
            this.et = et;
        }
    }

    public RoastChartController(CanvasData canvasData, ColorConfig colorConfig, AxisConfig axisConfig) {
        this(canvasData, colorConfig, axisConfig, null);
    }

    public RoastChartController(CanvasData canvasData, ColorConfig colorConfig, AxisConfig axisConfig, DisplaySettings displaySettings) {
        this.canvasData = canvasData;
        this.rorCalculator = new RorCalculator();
        this.colorConfig = colorConfig != null ? colorConfig : new ColorConfig(ColorConfig.Theme.DARK);
        this.axisConfig = axisConfig != null ? axisConfig : new AxisConfig();
        this.displaySettings = displaySettings;
        this.liveRorET = new LiveRorCalculator(getLiveRorWindow());
        this.liveRorBT = new LiveRorCalculator(getLiveRorWindow());

        chartFactory = new ChartFactory(this.axisConfig);

        phasePlugin = new PhaseShadePlugin();
        phasePlugin.setCanvasData(canvasData);
        phasePlugin.setColorConfig(this.colorConfig);

        eventPlugin = new EventMarkerPlugin();
        eventPlugin.setCanvasData(canvasData);
        eventPlugin.setColorConfig(this.colorConfig);
        eventPlugin.setDisplaySettings(displaySettings);
        eventPlugin.setRequestUpdate(this::updateChart);
        eventPlugin.setOnChartBodyClick(info -> {
            if (onChartBodyClick != null) {
                onChartBodyClick.accept(new ChartClickInfo(info.timeSec, info.timeIndex, info.bt, info.et));
            }
        });

        aucPlugin = new AUCPlugin();
        aucPlugin.setCanvasData(canvasData);
        aucPlugin.setColorConfig(this.colorConfig);
        aucPlugin.setDisplaySettings(displaySettings);
        aucPlugin.setAxisConfig(this.axisConfig);

        crosshairPlugin = new PrecisionCrosshairPlugin();
        crosshairPlugin.setCanvasData(canvasData);
        crosshairPlugin.setColorConfig(this.colorConfig);
        crosshairPlugin.setDisplaySettings(displaySettings);
        crosshairPlugin.setAxisConfig(this.axisConfig);

        statsPlugin = new StatisticsPlugin();
        statsPlugin.setCanvasData(canvasData);
        statsPlugin.setColorConfig(this.colorConfig);

        XYChart chart = chartFactory.getChart();
        chart.getPlugins().addAll(phasePlugin, aucPlugin, eventPlugin, statsPlugin, crosshairPlugin);

        bgManager = new BackgroundManager(chartFactory);
        bgManager.setColorConfig(this.colorConfig);
        bgManager.setDisplaySettings(displaySettings);

        applyColors();
        applyGlowEffect();
    }

    private int getLiveRorWindow() {
        return displaySettings != null ? displaySettings.getSmoothingDelta() : DEFAULT_ROR_SMOOTHING;
    }

    // ── Public API (backward-compatible) ──────────────────────────────

    public Node getView() {
        return chartFactory.getChart();
    }

    public XYChart getChart() {
        return chartFactory.getChart();
    }

    public void setPhasesConfig(PhasesConfig config) {
        this.phasesConfig = config;
        phasePlugin.setPhasesConfig(config);
        statsPlugin.setPhasesConfig(config);
    }

    /** Callback (timeSec, btCelsius) when mouse moves over chart. Used for CursorValueBar. */
    public void setOnCursorMoved(BiConsumer<Double, Double> c) {
        crosshairPlugin.setOnCursorMoved(c);
    }

    public void setCurveSet(CurveSet curveSet) {
        this.curveSet = curveSet;
        applyColors();
    }

    public void setDisplaySettings(DisplaySettings ds) {
        this.displaySettings = ds;
        this.liveRorET = new LiveRorCalculator(getLiveRorWindow());
        this.liveRorBT = new LiveRorCalculator(getLiveRorWindow());
        eventPlugin.setDisplaySettings(ds);
        aucPlugin.setDisplaySettings(ds);
        crosshairPlugin.setDisplaySettings(ds);
        bgManager.setDisplaySettings(ds);
        applyColors();
    }

    public void setBackgroundSettings(BackgroundSettings bs) {
        bgManager.setBackgroundSettings(bs);
        bgManager.syncDatasetsInChart();
        applyColors();
    }

    public BackgroundSettings getBackgroundSettings() {
        return bgManager.getBackgroundSettings();
    }

    public void setBackgroundProfile(BackgroundProfile bp) {
        bgManager.setBackgroundProfile(bp);
        bgManager.syncDatasetsInChart();
        eventPlugin.setBackgroundProfile(bp);
        applyColors();
    }

    public BackgroundProfile getBackgroundProfile() {
        return bgManager.getBackgroundProfile();
    }

    public void setEventList(EventList eventList) {
        eventPlugin.setEventList(eventList);
    }

    public void setLiveRecording(boolean liveRecording) {
        this.liveRecording = liveRecording;
        eventPlugin.setLiveRecording(liveRecording);
    }

    public void setZoomFollow(boolean zoomFollow) {
        this.zoomFollow = zoomFollow;
    }

    public void setRoastTitle(String title) {
        eventPlugin.setRoastTitle(title);
    }

    public void setOnEventMoved(Runnable onEventMoved) {
        eventPlugin.setOnEventMoved(onEventMoved);
    }

    public void setOnChartBodyClick(java.util.function.Consumer<ChartClickInfo> onChartBodyClick) {
        this.onChartBodyClick = onChartBodyClick;
    }

    /** Callback when user clicks an event marker in the events bar. Use to scroll EventLog + flash. */
    public void setOnEventBarClicked(java.util.function.Consumer<org.artisan.model.EventEntry> c) {
        eventPlugin.setOnEventBarClicked(c);
    }

    /** Resets chart X/Y axes to fixed defaults from axis config. */
    public void resetZoom() {
        if (axisConfig != null) {
            var xAxis = chartFactory.getXAxis();
            xAxis.setMin(axisConfig.getTimeMinSec());
            xAxis.setMax(axisConfig.getTimeMaxSec());
            var tempAxis = chartFactory.getTempAxis();
            tempAxis.setMin(axisConfig.getTempMin());
            tempAxis.setMax(axisConfig.getTempMax());
            var rorAxis = chartFactory.getRorAxis();
            rorAxis.setMin(axisConfig.getRorMin());
            rorAxis.setMax(axisConfig.getRorMax());
        }
    }

    /** Zooms X-axis in (narrows range by ~20%). */
    public void zoomIn() {
        var xAxis = chartFactory.getXAxis();
        double w = xAxis.getMax() - xAxis.getMin();
        if (w <= 20) return;
        double c = (xAxis.getMin() + xAxis.getMax()) / 2;
        double nw = w * 0.8;
        xAxis.setMin(Math.max(0, c - nw / 2));
        xAxis.setMax(c + nw / 2);
    }

    /** Zooms X-axis out (widens range by ~25%). */
    public void zoomOut() {
        var xAxis = chartFactory.getXAxis();
        double w = xAxis.getMax() - xAxis.getMin();
        double c = (xAxis.getMin() + xAxis.getMax()) / 2;
        double nw = Math.min(w * 1.25, axisConfig != null ? axisConfig.getTimeMaxSec() - axisConfig.getTimeMinSec() : 3600);
        xAxis.setMin(Math.max(0, c - nw / 2));
        xAxis.setMax(c + nw / 2);
    }

    /** Pans X-axis left by 10 seconds. */
    public void panLeft() {
        var xAxis = chartFactory.getXAxis();
        double w = xAxis.getMax() - xAxis.getMin();
        double shift = Math.min(10, xAxis.getMin());
        xAxis.setMin(xAxis.getMin() - shift);
        xAxis.setMax(xAxis.getMax() - shift);
    }

    /** Pans X-axis right by 10 seconds. */
    public void panRight() {
        var xAxis = chartFactory.getXAxis();
        double maxTime = axisConfig != null ? axisConfig.getTimeMaxSec() : 3600;
        double shift = Math.min(10, maxTime - xAxis.getMax());
        xAxis.setMin(xAxis.getMin() + shift);
        xAxis.setMax(xAxis.getMax() + shift);
    }

    /** Centers the X axis view on the given time (e.g. when user selects an event). */
    public void centerChartOnTime(double timeSec) {
        if (!Double.isFinite(timeSec)) return;
        var xAxis = chartFactory.getXAxis();
        double currentWidth = xAxis.getMax() - xAxis.getMin();
        if (currentWidth <= 0) currentWidth = 120.0;
        double half = currentWidth / 2.0;
        double xMin = Math.max(0, timeSec - half);
        double xMax = timeSec + half;
        xAxis.setMin(xMin);
        xAxis.setMax(xMax);
    }

    /** Sets the highlighted time (vertical line when event selected from list). */
    public void setHighlightTimeSec(double timeSec) {
        eventPlugin.setHighlightTimeSec(timeSec);
        updateChart();
    }

    // ── Colors / Styling ──────────────────────────────────────────────

    public void applyColors() {
        int wBT = displaySettings != null ? displaySettings.getLineWidthBT() : 2;
        int wET = displaySettings != null ? displaySettings.getLineWidthET() : 2;
        int wDeltaBT = displaySettings != null ? displaySettings.getLineWidthDeltaBT() : 1;
        int wDeltaET = displaySettings != null ? displaySettings.getLineWidthDeltaET() : 1;
        boolean visBT = displaySettings == null || displaySettings.isVisibleBT();
        boolean visET = displaySettings == null || displaySettings.isVisibleET();
        boolean visDeltaBT = displaySettings == null || displaySettings.isVisibleDeltaBT();
        boolean visDeltaET = displaySettings == null || displaySettings.isVisibleDeltaET();

        if (curveSet != null) {
            chartFactory.getDataBT().setStyle(curveSet.getBt().toChartFxStyle());
            chartFactory.getDataET().setStyle(curveSet.getEt().toChartFxStyle());
            chartFactory.getDataDeltaBT().setStyle(curveSet.getDeltaBt().toChartFxStyle());
            chartFactory.getDataDeltaET().setStyle(curveSet.getDeltaEt().toChartFxStyle());
        } else {
            chartFactory.getDataBT().setStyle(ChartFactory.styleFor(visBT, colorConfig.getCurveBT(), wBT));
            chartFactory.getDataET().setStyle(ChartFactory.styleFor(visET, colorConfig.getCurveET(), wET));
            chartFactory.getDataDeltaBT().setStyle(ChartFactory.styleFor(visDeltaBT, colorConfig.getCurveDeltaBT(), wDeltaBT));
            chartFactory.getDataDeltaET().setStyle(ChartFactory.styleFor(visDeltaET, colorConfig.getCurveDeltaET(), wDeltaET));
        }
        bgManager.applyStyles();
    }

    private void applyGlowEffect() {
        /* No glow — Cropster RI5 style: reduced visual noise. */
    }

    // ── Axis Config ───────────────────────────────────────────────────

    public void applyAxisConfig(AxisConfig cfg) {
        if (cfg == null) return;
        chartFactory.applyAxisConfig(cfg);
        if (cfg.isAutoScaleY()) {
            autoScaleTempAxis(cfg);
        }
        if (cfg.isAutoScaleY2()) {
            autoScaleRorAxis();
        }
    }

    private void autoScaleTempAxis(AxisConfig cfg) {
        List<Double> timex = canvasData.getTimex();
        List<Double> temp1 = canvasData.getTemp1();
        List<Double> temp2 = canvasData.getTemp2();
        double xMin = cfg.getTimeMinSec(), xMax = cfg.getTimeMaxSec();
        double dataMin = Double.POSITIVE_INFINITY, dataMax = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < timex.size(); i++) {
            double t = timex.get(i);
            if (t < xMin || t > xMax) continue;
            if (temp1 != null && i < temp1.size()) {
                double v = temp1.get(i);
                if (Double.isFinite(v)) { dataMin = Math.min(dataMin, v); dataMax = Math.max(dataMax, v); }
            }
            if (temp2 != null && i < temp2.size()) {
                double v = temp2.get(i);
                if (Double.isFinite(v)) { dataMin = Math.min(dataMin, v); dataMax = Math.max(dataMax, v); }
            }
        }
        if (Double.isFinite(dataMin) && Double.isFinite(dataMax)) {
            if (cfg.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT) {
                dataMin = AxisConfig.celsiusToFahrenheit(dataMin);
                dataMax = AxisConfig.celsiusToFahrenheit(dataMax);
            }
            chartFactory.autoScaleTemp(dataMin, dataMax);
        }
    }

    private void autoScaleRorAxis() {
        List<Double> delta1 = canvasData.getDelta1();
        List<Double> delta2 = canvasData.getDelta2();
        double dataMin = Double.POSITIVE_INFINITY, dataMax = Double.NEGATIVE_INFINITY;
        for (List<Double> dl : List.of(delta1, delta2)) {
            for (Double v : dl) {
                if (v != null && Double.isFinite(v)) {
                    dataMin = Math.min(dataMin, v);
                    dataMax = Math.max(dataMax, v);
                }
            }
        }
        if (Double.isFinite(dataMin) && Double.isFinite(dataMax)) {
            chartFactory.autoScaleRor(dataMin, dataMax);
        }
    }

    // ── Chart Update (dirty-flag throttled) ───────────────────────────

    /**
     * Marks the chart as needing a repaint. Coalesces rapid calls
     * into a single updateChart() on the FX thread at ~25 Hz.
     */
    public void markDirty() {
        if (!chartDirty) {
            chartDirty = true;
            Platform.runLater(() -> {
                chartDirty = false;
                updateChart();
            });
        }
    }

    /**
     * Refreshes the entire chart from CanvasData. Call on JavaFX thread.
     */
    public void updateChart() {
        List<Double> timex = canvasData.getTimex();
        List<Double> temp1 = canvasData.getTemp1();
        List<Double> temp2 = canvasData.getTemp2();

        int n = timex.size();
        if (n == 0) {
            chartFactory.getDataBT().clearData();
            chartFactory.getDataET().clearData();
            chartFactory.getDataDeltaBT().clearData();
            chartFactory.getDataDeltaET().clearData();
            bgManager.clearData();
            return;
        }

        List<Double> etList = temp1;
        List<Double> btList = temp2;
        int smoothDelta = displaySettings != null ? displaySettings.getSmoothingDelta() : DEFAULT_ROR_SMOOTHING;
        List<Double> d1List = rorCalculator.computeRoRSmoothed(timex, temp1, smoothDelta);
        List<Double> d2List = rorCalculator.computeRoRSmoothed(timex, temp2, smoothDelta);
        RorCalculator.clampRoR(d1List, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
        RorCalculator.clampRoR(d2List, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
        if (displaySettings != null) {
            int smoothET = displaySettings.getSmoothingET();
            int smoothBT = displaySettings.getSmoothingBT();
            if (smoothET > 1) etList = CurveSmoothing.smooth(temp1, smoothET);
            if (smoothBT > 1) btList = CurveSmoothing.smooth(temp2, smoothBT);
        }

        double[] tx = new double[n];
        double[] et = new double[n];
        double[] bt = new double[n];
        double[] d1 = new double[n];
        double[] d2 = new double[n];
        for (int i = 0; i < n; i++) {
            tx[i] = timex.get(i);
            double etVal = i < etList.size() ? etList.get(i) : 0;
            double btVal = i < btList.size() ? btList.get(i) : 0;
            et[i] = (etVal == -1) ? Double.NaN : etVal;
            bt[i] = (btVal == -1) ? Double.NaN : btVal;
            d1[i] = i < d1List.size() ? d1List.get(i) : 0;
            d2[i] = i < d2List.size() ? d2List.get(i) : 0;
        }

        boolean useF = axisConfig.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT;
        if (useF) {
            for (int i = 0; i < n; i++) {
                if (Double.isFinite(et[i])) et[i] = AxisConfig.celsiusToFahrenheit(et[i]);
                if (Double.isFinite(bt[i])) bt[i] = AxisConfig.celsiusToFahrenheit(bt[i]);
            }
        }

        chartFactory.getDataET().set(tx, et);
        chartFactory.getDataBT().set(tx, bt);
        chartFactory.getDataDeltaET().set(tx, d1);
        chartFactory.getDataDeltaBT().set(tx, d2);

        if (canvasData.getChargeIndex() >= 0 && canvasData.getChargeIndex() < timex.size()) {
            chartFactory.setChargeTimeSec((int) Math.round(timex.get(canvasData.getChargeIndex())));
        }

        applyAxisConfig(axisConfig);

        List<Double> etDisplay = new ArrayList<>(n);
        List<Double> btDisplay = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            etDisplay.add(et[i]);
            btDisplay.add(bt[i]);
        }

        phasePlugin.refresh();
        aucPlugin.refresh(timex, btDisplay);
        eventPlugin.refresh(timex, etDisplay, btDisplay, d2List);
        statsPlugin.setRorBT(d2List);
        statsPlugin.refresh(timex);
        bgManager.updateData();

        if (liveRecording && zoomFollow && n > 0) {
            autoScrollXAxis(timex);
        }
    }

    private void autoScrollXAxis(List<Double> timex) {
        double latest = timex.get(timex.size() - 1);
        double xMin = chartFactory.getXAxis().getMin();
        double xMax = chartFactory.getXAxis().getMax();
        double windowWidth = xMax - xMin;
        double margin = windowWidth * 0.1;
        if (latest > xMax - margin) {
            chartFactory.getXAxis().setMin(latest - windowWidth + margin);
            chartFactory.getXAxis().setMax(latest + margin);
        }
    }

    // ── Timer (backward compat) ───────────────────────────────────────

    private javafx.animation.AnimationTimer updateTimer;

    public void startUpdateTimer() {
        if (updateTimer != null) return;
        updateTimer = new javafx.animation.AnimationTimer() {
            private long lastUpdate = 0;
            private static final long MIN_INTERVAL_NS = 40_000_000L; // 25 Hz
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= MIN_INTERVAL_NS) {
                    lastUpdate = now;
                    updateChart();
                }
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

    // ── Live Sampling ─────────────────────────────────────────────────

    public void onSample(double timeSec, double bt, double et) {
        canvasData.addDataPoint(timeSec, bt, et);
        double rorET = liveRorET.addSample(timeSec, et);
        double rorBT = liveRorBT.addSample(timeSec, bt);
        double clampedET = clamp(rorET, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
        double clampedBT = clamp(rorBT, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
        List<Double> d1 = new ArrayList<>(canvasData.getDelta1());
        d1.add(clampedET);
        List<Double> d2 = new ArrayList<>(canvasData.getDelta2());
        d2.add(clampedBT);
        canvasData.setDelta1(d1);
        canvasData.setDelta2(d2);
        markDirty();
    }

    public void resetLiveRor() {
        liveRorET.reset();
        liveRorBT.reset();
    }

    private static double clamp(double v, double min, double max) {
        if (!Double.isFinite(v)) return 0.0;
        return Math.max(min, Math.min(max, v));
    }
}
