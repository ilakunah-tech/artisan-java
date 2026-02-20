package org.artisan.view;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import org.artisan.controller.DisplaySettings;
import org.artisan.controller.BackgroundSettings;
import org.artisan.model.AxisConfig;
import org.artisan.model.BackgroundProfile;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;
import org.artisan.model.EventAnnotation;
import org.artisan.model.EventEntry;
import org.artisan.model.EventList;
import org.artisan.model.EventType;
import org.artisan.model.LiveRorCalculator;
import org.artisan.model.Phases;
import org.artisan.model.PhasesConfig;
import org.artisan.model.ProfileData;
import org.artisan.model.MetCalculator;
import org.artisan.model.RorCalculator;
import org.artisan.util.CurveSmoothing;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.ParameterMeasurements;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.util.StringConverter;

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
    private final DoubleDataSet dataBgBT;
    private final DoubleDataSet dataBgET;
    private final DoubleDataSet dataBgDeltaBT;
    private final DoubleDataSet dataBgDeltaET;
    private final DoubleDataSet dataBT;
    private final DoubleDataSet dataET;
    private final DoubleDataSet dataDeltaBT;
    private final DoubleDataSet dataDeltaET;
    private AnimationTimer updateTimer;
    private static final int DEFAULT_ROR_SMOOTHING = 5;
    private static final int IDX_CHARGE = 0;
    private static final int IDX_DRY_END = 1;
    private static final int IDX_FC_START = 2;
    private static final int IDX_FC_END = 3;
    private static final int IDX_DROP = 6;
    private static final int IDX_COOL_END = 7;
    private static final double PHASE_SHADE_ALPHA = 0.25;
    private static final double SPECIAL_EVENT_BAR_HEIGHT = 20.0;
    private static final double EVENT_MARKER_HIT_PX = 8.0;

    private DisplaySettings displaySettings;
    private LiveRorCalculator liveRorET;
    private LiveRorCalculator liveRorBT;
    private PhasesConfig phasesConfig;
    private BackgroundSettings backgroundSettings;
    private BackgroundProfile backgroundProfile;
    private final Pane phaseShadeLayer;
    private final Rectangle rectDrying;
    private final Rectangle rectMaillard;
    private final Rectangle rectFinishing;
    private final Rectangle rectCooling;
    private final Pane aucLayer;
    private final Line aucGuideLine;
    private final Polygon aucAreaPolygon;
    private final Pane eventLayer;
    private final Pane crosshairLayer;
    private final Line crosshairV;
    private final Line crosshairH;
    private final Text crosshairLabel;
    private EventList eventList;
    private int draggedEventIndex = -1;
    private boolean liveRecording = false;
    private Runnable onEventMoved;
    /** Callback when user clicks on chart body (to add event marker). */
    private java.util.function.Consumer<ChartClickInfo> onChartBodyClick;
    /** Roast title for watermark (e.g. from ProfileData). */
    private String roastTitle;

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

        DefaultNumericAxis xAxis = new DefaultNumericAxis("Time");
        xAxis.setSide(Side.BOTTOM);
        xAxis.setMin(axisConfig.getTimeMinSec());
        xAxis.setMax(axisConfig.getTimeMaxSec());
        xAxis.setAutoRangeRounding(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number sec) {
                int totalSec = sec != null ? (int) Math.round(sec.doubleValue()) : 0;
                int m = totalSec / 60;
                int s = totalSec % 60;
                return String.format("%d:%02d", m, s);
            }
            @Override
            public Number fromString(String s) {
                try {
                    String[] parts = s != null ? s.split(":") : new String[0];
                    if (parts.length == 2) {
                        return Integer.parseInt(parts[0].trim()) * 60 + Integer.parseInt(parts[1].trim());
                    }
                } catch (NumberFormatException ignored) {}
                return 0;
            }
        });

        DefaultNumericAxis yAxis = new DefaultNumericAxis("Temp (°C)");
        yAxis.setSide(Side.LEFT);
        yAxis.setMin(0);
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

        dataBgBT = new DoubleDataSet("BG BT");
        dataBgET = new DoubleDataSet("BG ET");
        dataBgDeltaBT = new DoubleDataSet("BG Delta BT");
        dataBgDeltaET = new DoubleDataSet("BG Delta ET");

        chart.getDatasets().addAll(dataBT, dataET, dataDeltaBT, dataDeltaET);
        applyColors();

        phaseShadeLayer = new Pane();
        phaseShadeLayer.setMouseTransparent(true);
        rectDrying = new Rectangle();
        rectMaillard = new Rectangle();
        rectFinishing = new Rectangle();
        rectCooling = new Rectangle();
        phaseShadeLayer.getChildren().addAll(rectDrying, rectMaillard, rectFinishing, rectCooling);

        aucGuideLine = new Line();
        aucGuideLine.setStrokeWidth(1);
        aucAreaPolygon = new Polygon();
        aucLayer = new Pane();
        aucLayer.setMouseTransparent(true);
        aucLayer.getChildren().addAll(aucAreaPolygon, aucGuideLine);

        eventLayer = new Pane();
        eventLayer.setMouseTransparent(true);

        crosshairV = new Line();
        crosshairH = new Line();
        crosshairV.getStrokeDashArray().addAll(6.0, 4.0);
        crosshairH.getStrokeDashArray().addAll(6.0, 4.0);
        crosshairLabel = new Text();
        crosshairLabel.setStyle("-fx-font-size: 11px;");
        crosshairLayer = new Pane();
        crosshairLayer.setMouseTransparent(true);
        crosshairLayer.getChildren().addAll(crosshairV, crosshairH, crosshairLabel);
        updateCrosshairVisibility(false);

        pane = new StackPane(phaseShadeLayer, aucLayer, chart, eventLayer, crosshairLayer);
        pane.getStyleClass().add("chart-pane");
        pane.setOnMousePressed(this::onMousePressed);
        pane.setOnMouseDragged(this::onMouseDragged);
        pane.setOnMouseReleased(this::onMouseReleased);
        pane.setOnMouseMoved(this::onMouseMoved);
        pane.setOnMouseExited(this::onMouseExited);
    }

    private void updateCrosshairVisibility(boolean visible) {
        crosshairV.setVisible(visible);
        crosshairH.setVisible(visible);
        crosshairLabel.setVisible(visible);
    }

    private void onMouseMoved(MouseEvent e) {
        if (displaySettings != null && !displaySettings.isShowCrosshair()) {
            updateCrosshairVisibility(false);
            return;
        }
        double w = pane.getWidth();
        double h = pane.getHeight();
        if (w <= 0 || h <= 0) {
            updateCrosshairVisibility(false);
            return;
        }
        double mx = e.getX();
        double my = e.getY();
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
        double xMin = xAxis.getMin();
        double xMax = xAxis.getMax();
        double yMin = yAxis.getMin();
        double yMax = yAxis.getMax();
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;
        if (xRange <= 0 || yRange <= 0) {
            updateCrosshairVisibility(false);
            return;
        }
        double timeSec = xMin + (mx / w) * xRange;
        crosshairV.setStartX(mx);
        crosshairV.setStartY(0);
        crosshairV.setEndX(mx);
        crosshairV.setEndY(h);
        crosshairH.setStartX(0);
        crosshairH.setStartY(my);
        crosshairH.setEndX(w);
        crosshairH.setEndY(my);
        Color markersColor = colorConfig.getPaletteColor("markers");
        if (markersColor != null) {
            Color withAlpha = Color.color(markersColor.getRed(), markersColor.getGreen(), markersColor.getBlue(), 0.6);
            crosshairV.setStroke(withAlpha);
            crosshairH.setStroke(withAlpha);
        }
        double bt = Double.NaN;
        double et = Double.NaN;
        double deltaBT = Double.NaN;
        List<Double> timex = canvasData.getTimex();
        List<Double> temp1 = canvasData.getTemp1();
        List<Double> temp2 = canvasData.getTemp2();
        List<Double> delta2 = canvasData.getDelta2();
        if (timex != null && !timex.isEmpty()) {
            int idx = nearestTimeIndex(timex, timeSec);
            if (idx >= 0) {
                if (temp1 != null && idx < temp1.size()) et = temp1.get(idx);
                if (temp2 != null && idx < temp2.size()) bt = temp2.get(idx);
                if (delta2 != null && idx < delta2.size()) deltaBT = delta2.get(idx);
                if (idx + 1 < timex.size() && timeSec > timex.get(idx)) {
                    double t0 = timex.get(idx);
                    double t1 = timex.get(idx + 1);
                    double frac = (t1 - t0) > 0 ? (timeSec - t0) / (t1 - t0) : 0;
                    if (temp1 != null && idx + 1 < temp1.size())
                        et = lerp(temp1.get(idx), temp1.get(idx + 1), frac);
                    if (temp2 != null && idx + 1 < temp2.size())
                        bt = lerp(temp2.get(idx), temp2.get(idx + 1), frac);
                    if (delta2 != null && idx + 1 < delta2.size())
                        deltaBT = lerp(delta2.get(idx), delta2.get(idx + 1), frac);
                }
            }
        }
        if (axisConfig.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT) {
            if (Double.isFinite(bt)) bt = AxisConfig.celsiusToFahrenheit(bt);
            if (Double.isFinite(et)) et = AxisConfig.celsiusToFahrenheit(et);
        }
        String unitStr = axisConfig.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT ? "°F" : "°C";
        int totalSec = (int) Math.round(timeSec);
        int mm = totalSec / 60;
        int ss = totalSec % 60;
        String btStr = Double.isFinite(bt) ? String.format("%.1f", bt) : "—";
        String etStr = Double.isFinite(et) ? String.format("%.1f", et) : "—";
        String deltaStr = Double.isFinite(deltaBT) ? String.format("%.1f", deltaBT) : "—";
        crosshairLabel.setText(String.format("time: %d:%02d  BT: %s%s  ET: %s%s  ΔBT: %s°/min", mm, ss, btStr, unitStr, etStr, unitStr, deltaStr));
        crosshairLabel.setFill(markersColor != null ? markersColor : Color.BLACK);
        crosshairLabel.setX(Math.min(mx + 8, w - 180));
        crosshairLabel.setY(my - 6);
        updateCrosshairVisibility(true);
    }

    private void onMouseExited(MouseEvent e) {
        updateCrosshairVisibility(false);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** Sets the event list for drawing special events and markers. Call from MainWindow with session.getEvents(). */
    public void setEventList(EventList eventList) {
        this.eventList = eventList;
    }

    /** When true, dragging special event markers is disabled. Call from MainWindow based on sampling state. */
    public void setLiveRecording(boolean liveRecording) {
        this.liveRecording = liveRecording;
    }

    /** Sets the roast title shown in the watermark (e.g. from ProfileData). */
    public void setRoastTitle(String title) {
        this.roastTitle = title;
    }

    /** Called when user finishes dragging a special event (to mark file dirty). */
    public void setOnEventMoved(Runnable onEventMoved) {
        this.onEventMoved = onEventMoved;
    }

    private void onMousePressed(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        double w = pane.getWidth();
        double h = pane.getHeight();
        if (w <= 0 || h <= 0) return;
        double barY = h - SPECIAL_EVENT_BAR_HEIGHT;
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        double xMin = xAxis.getMin();
        double xMax = xAxis.getMax();
        double xRange = xMax - xMin;
        if (xRange <= 0) return;
        List<Double> timex = canvasData.getTimex();

        if (e.getY() < barY) {
            if (onChartBodyClick != null && timex != null && !timex.isEmpty()) {
                double mx = e.getX();
                double scaleX = w / xRange;
                double timeSec = xMin + mx / scaleX;
                int idx = nearestTimeIndex(timex, timeSec);
                if (idx >= 0) {
                    double bt = idx < canvasData.getTemp2().size() ? canvasData.getTemp2().get(idx) : 0;
                    double et = idx < canvasData.getTemp1().size() ? canvasData.getTemp1().get(idx) : 0;
                    onChartBodyClick.accept(new ChartClickInfo(timeSec, idx, bt, et));
                }
            }
            return;
        }

        if (liveRecording || eventList == null) return;
        double scaleX = w / xRange;
        double mx = e.getX();
        for (int i = 0; i < eventList.size(); i++) {
            EventEntry ev = eventList.get(i);
            int idx = ev.getTimeIndex();
            if (idx < 0 || idx >= timex.size()) continue;
            double xPx = (timex.get(idx) - xMin) * scaleX;
            if (Math.abs(mx - xPx) <= EVENT_MARKER_HIT_PX) {
                draggedEventIndex = i;
                return;
            }
        }
    }

    public void setOnChartBodyClick(java.util.function.Consumer<ChartClickInfo> onChartBodyClick) {
        this.onChartBodyClick = onChartBodyClick;
    }

    private void onMouseDragged(MouseEvent e) {
        if (draggedEventIndex < 0 || eventList == null) return;
        double w = pane.getWidth();
        if (w <= 0) return;
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        double xMin = xAxis.getMin();
        double xMax = xAxis.getMax();
        double xRange = xMax - xMin;
        if (xRange <= 0) return;
        double scaleX = w / xRange;
        List<Double> timex = canvasData.getTimex();
        if (timex.isEmpty()) return;
        double mx = e.getX();
        double timeSec = xMin + mx / scaleX;
        int newIdx = nearestTimeIndex(timex, timeSec);
        if (newIdx < 0) return;
        EventEntry old = eventList.get(draggedEventIndex);
        double temp = newIdx < canvasData.getTemp2().size() ? canvasData.getTemp2().get(newIdx) : 0.0;
        eventList.set(draggedEventIndex, new EventEntry(newIdx, temp, old.getLabel(), old.getType(), old.getValue()));
        updateChart();
    }

    private void onMouseReleased(MouseEvent e) {
        if (draggedEventIndex >= 0) {
            if (onEventMoved != null) onEventMoved.run();
            draggedEventIndex = -1;
        }
    }

    private static int nearestTimeIndex(List<Double> timex, double timeSec) {
        if (timex == null || timex.isEmpty()) return -1;
        int best = 0;
        double bestDist = Math.abs(timex.get(0) - timeSec);
        for (int i = 1; i < timex.size(); i++) {
            double d = Math.abs(timex.get(i) - timeSec);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    /** Set phases config for phase shading (rect1..rect4). When null, shading is hidden. */
    public void setPhasesConfig(PhasesConfig config) {
        this.phasesConfig = config;
    }

    /**
     * Applies axis config to the chart: time and temperature bounds, tick steps, unit label.
     * If autoScaleY, computes temp min/max from visible data + 5% margin (in display unit).
     * Call from updateChart() and after AxesDialog OK/Apply.
     */
    public void applyAxisConfig(AxisConfig cfg) {
        if (cfg == null) return;
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
        xAxis.setMin(cfg.getTimeMinSec());
        xAxis.setMax(cfg.getTimeMaxSec());
        xAxis.setTickUnit(cfg.getTimeTickStepSec());
        String unitLabel = cfg.getTempUnitString();
        yAxis.setName("Temp (" + unitLabel + ")");
        double yMin = cfg.getTempMin();
        double yMax = cfg.getTempMax();
        if (cfg.isAutoScaleY()) {
            List<Double> timex = canvasData.getTimex();
            List<Double> temp1 = canvasData.getTemp1();
            List<Double> temp2 = canvasData.getTemp2();
            double xMin = cfg.getTimeMinSec();
            double xMax = cfg.getTimeMaxSec();
            double dataMin = Double.POSITIVE_INFINITY;
            double dataMax = Double.NEGATIVE_INFINITY;
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
            if (Double.isFinite(dataMin) && Double.isFinite(dataMax) && dataMax >= dataMin) {
                if (cfg.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT) {
                    dataMin = AxisConfig.celsiusToFahrenheit(dataMin);
                    dataMax = AxisConfig.celsiusToFahrenheit(dataMax);
                }
                double margin = Math.max(1, (dataMax - dataMin) * 0.05);
                yMin = dataMin - margin;
                yMax = dataMax + margin;
            }
        }
        yAxis.setMin(yMin);
        yAxis.setMax(yMax);
        yAxis.setTickUnit(cfg.getTempTickStep());
    }

    public void setDisplaySettings(DisplaySettings displaySettings) {
        this.displaySettings = displaySettings;
        this.liveRorET = new LiveRorCalculator(getLiveRorWindow());
        this.liveRorBT = new LiveRorCalculator(getLiveRorWindow());
        applyColors();
    }

    public void setBackgroundSettings(BackgroundSettings backgroundSettings) {
        this.backgroundSettings = backgroundSettings;
        syncBackgroundDatasetsInChart();
        applyColors();
    }

    public BackgroundSettings getBackgroundSettings() {
        return backgroundSettings;
    }

    public void setBackgroundProfile(BackgroundProfile backgroundProfile) {
        this.backgroundProfile = backgroundProfile;
        syncBackgroundDatasetsInChart();
        applyColors();
    }

    public BackgroundProfile getBackgroundProfile() {
        return backgroundProfile;
    }

    private int getLiveRorWindow() {
        return displaySettings != null ? displaySettings.getSmoothingDelta() : DEFAULT_ROR_SMOOTHING;
    }

    /** Applies current ColorConfig and DisplaySettings (line width, visibility) to chart series. */
    public void applyColors() {
        int wBT = displaySettings != null ? displaySettings.getLineWidthBT() : 2;
        int wET = displaySettings != null ? displaySettings.getLineWidthET() : 2;
        int wDeltaBT = displaySettings != null ? displaySettings.getLineWidthDeltaBT() : 1;
        int wDeltaET = displaySettings != null ? displaySettings.getLineWidthDeltaET() : 1;
        boolean visBT = displaySettings == null || displaySettings.isVisibleBT();
        boolean visET = displaySettings == null || displaySettings.isVisibleET();
        boolean visDeltaBT = displaySettings == null || displaySettings.isVisibleDeltaBT();
        boolean visDeltaET = displaySettings == null || displaySettings.isVisibleDeltaET();
        dataBT.setStyle(styleFor(visBT, colorConfig.getCurveBT(), wBT));
        dataET.setStyle(styleFor(visET, colorConfig.getCurveET(), wET));
        dataDeltaBT.setStyle(styleFor(visDeltaBT, colorConfig.getCurveDeltaBT(), wDeltaBT));
        dataDeltaET.setStyle(styleFor(visDeltaET, colorConfig.getCurveDeltaET(), wDeltaET));
        applyBackgroundStyles();
    }

    private static String styleFor(boolean visible, javafx.scene.paint.Color color, int widthPx) {
        if (!visible) return "-fx-stroke: transparent; -fx-stroke-width: 0px;";
        return "-fx-stroke: " + toHex(color) + "; -fx-stroke-width: " + widthPx + "px;";
    }

    private void applyBackgroundStyles() {
        if (!isBackgroundEnabled()) return;
        double alpha = displaySettings != null ? displaySettings.getBackgroundAlpha() : 0.2;
        alpha = Math.max(0, Math.min(1, alpha));

        int wBT = displaySettings != null ? displaySettings.getLineWidthBT() : 2;
        int wET = displaySettings != null ? displaySettings.getLineWidthET() : 2;
        int wDeltaBT = displaySettings != null ? displaySettings.getLineWidthDeltaBT() : 1;
        int wDeltaET = displaySettings != null ? displaySettings.getLineWidthDeltaET() : 1;

        boolean visET = shouldShowBgET();
        boolean visBT = shouldShowBgBT();
        boolean visDeltaET = shouldShowBgDeltaET();
        boolean visDeltaBT = shouldShowBgDeltaBT();

        dataBgET.setStyle(styleForDashed(visET, colorConfig.getPaletteColor("backgroundmetcolor"), alpha, wET));
        dataBgBT.setStyle(styleForDashed(visBT, colorConfig.getPaletteColor("backgroundbtcolor"), alpha, wBT));
        dataBgDeltaET.setStyle(styleForDashed(visDeltaET, colorConfig.getPaletteColor("backgrounddeltaetcolor"), alpha, wDeltaET));
        dataBgDeltaBT.setStyle(styleForDashed(visDeltaBT, colorConfig.getPaletteColor("backgrounddeltabtcolor"), alpha, wDeltaBT));
    }

    private static String styleForDashed(boolean visible, javafx.scene.paint.Color color, double alpha, int widthPx) {
        if (!visible || color == null) return "-fx-stroke: transparent; -fx-stroke-width: 0px;";
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        String stroke = String.format("rgba(%d,%d,%d,%.3f)", r, g, b, alpha);
        return "-fx-stroke: " + stroke + "; -fx-stroke-width: " + widthPx + "px; -fx-stroke-dash-array: 8 6;";
    }

    private boolean shouldShowBgET() {
        if (!isBackgroundEnabled()) return false;
        boolean setting = backgroundSettings == null || backgroundSettings.isShowBgET();
        boolean visible = displaySettings == null || displaySettings.isVisibleET();
        return setting && visible;
    }

    private boolean shouldShowBgBT() {
        if (!isBackgroundEnabled()) return false;
        boolean setting = backgroundSettings == null || backgroundSettings.isShowBgBT();
        boolean visible = displaySettings == null || displaySettings.isVisibleBT();
        return setting && visible;
    }

    private boolean shouldShowBgDeltaET() {
        if (!isBackgroundEnabled()) return false;
        boolean setting = backgroundSettings != null && backgroundSettings.isShowBgDeltaET();
        boolean visible = displaySettings == null || displaySettings.isVisibleDeltaET();
        return setting && visible;
    }

    private boolean shouldShowBgDeltaBT() {
        if (!isBackgroundEnabled()) return false;
        boolean setting = backgroundSettings != null && backgroundSettings.isShowBgDeltaBT();
        boolean visible = displaySettings == null || displaySettings.isVisibleDeltaBT();
        return setting && visible;
    }

    private boolean isBackgroundEnabled() {
        if (backgroundSettings != null && !backgroundSettings.isEnabled()) return false;
        return backgroundProfile != null && backgroundProfile.isVisible() && !backgroundProfile.isEmpty();
    }

    private void syncBackgroundDatasetsInChart() {
        var list = chart.getDatasets();
        // Remove any existing background datasets.
        list.removeAll(dataBgET, dataBgBT, dataBgDeltaET, dataBgDeltaBT);
        if (!isBackgroundEnabled()) return;

        // Insert background datasets first so they render behind main curves (but above phase shading).
        int idx = 0;
        if (shouldShowBgET()) list.add(idx++, dataBgET);
        if (shouldShowBgBT()) list.add(idx++, dataBgBT);
        if (shouldShowBgDeltaET()) list.add(idx++, dataBgDeltaET);
        if (shouldShowBgDeltaBT()) list.add(idx++, dataBgDeltaBT);

        // Update legend labels to include title.
        String title = backgroundProfile != null ? backgroundProfile.getTitle() : "";
        String suffix = (title != null && !title.isBlank()) ? " (" + title + ")" : "";
        dataBgET.setName("BG ET" + suffix);
        dataBgBT.setName("BG BT" + suffix);
        dataBgDeltaET.setName("BG ΔET" + suffix);
        dataBgDeltaBT.setName("BG ΔBT" + suffix);
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
     * Applies smoothing from DisplaySettings when set. Call on JavaFX thread.
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
            clearBackgroundData();
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
            et[i] = i < etList.size() ? etList.get(i) : 0;
            bt[i] = i < btList.size() ? btList.get(i) : 0;
            d1[i] = i < d1List.size() ? d1List.get(i) : 0;
            d2[i] = i < d2List.size() ? d2List.get(i) : 0;
        }

        boolean useF = axisConfig.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT;
        if (useF) {
            for (int i = 0; i < n; i++) {
                et[i] = AxisConfig.celsiusToFahrenheit(et[i]);
                bt[i] = AxisConfig.celsiusToFahrenheit(bt[i]);
            }
        }
        dataET.set(tx, et);
        dataBT.set(tx, bt);
        dataDeltaET.set(tx, d1);
        dataDeltaBT.set(tx, d2);

        applyAxisConfig(axisConfig);
        List<Double> etDisplay = new ArrayList<>();
        List<Double> btDisplay = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            etDisplay.add(et[i]);
            btDisplay.add(bt[i]);
        }
        updatePhaseShading(timex, temp2);
        updateAucLayer(timex, btDisplay);
        updateEventLayers(timex, etDisplay, btDisplay);
        updateBackgroundChart();
    }

    private void clearBackgroundData() {
        dataBgET.clearData();
        dataBgBT.clearData();
        dataBgDeltaET.clearData();
        dataBgDeltaBT.clearData();
    }

    private void updateBackgroundChart() {
        if (!isBackgroundEnabled() || backgroundProfile == null || backgroundProfile.getProfileData() == null) {
            clearBackgroundData();
            return;
        }
        ProfileData pd = backgroundProfile.getProfileData();
        List<Double> timex = pd.getTimex();
        List<Double> etRaw = pd.getTemp1();
        List<Double> btRaw = pd.getTemp2();
        if (timex == null || etRaw == null || btRaw == null) {
            clearBackgroundData();
            return;
        }
        int n = Math.min(timex.size(), Math.min(etRaw.size(), btRaw.size()));
        if (n <= 0) {
            clearBackgroundData();
            return;
        }

        List<Double> etList = etRaw;
        List<Double> btList = btRaw;
        if (displaySettings != null) {
            int smoothET = displaySettings.getSmoothingET();
            int smoothBT = displaySettings.getSmoothingBT();
            if (smoothET > 1) etList = CurveSmoothing.smooth(etRaw, smoothET);
            if (smoothBT > 1) btList = CurveSmoothing.smooth(btRaw, smoothBT);
        }

        double offset = backgroundProfile.getAlignOffset();
        double[] x = new double[n];
        double[] et = new double[n];
        double[] bt = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = timex.get(i) + offset;
            et[i] = i < etList.size() ? etList.get(i) : 0.0;
            bt[i] = i < btList.size() ? btList.get(i) : 0.0;
        }

        if (chart.getDatasets().contains(dataBgET)) dataBgET.set(x, et);
        if (chart.getDatasets().contains(dataBgBT)) dataBgBT.set(x, bt);

        int smoothDelta = displaySettings != null ? displaySettings.getSmoothingDelta() : DEFAULT_ROR_SMOOTHING;
        var dEt = rorCalculator.computeRoRSmoothed(timex, etRaw, smoothDelta);
        var dBt = rorCalculator.computeRoRSmoothed(timex, btRaw, smoothDelta);
        RorCalculator.clampRoR(dEt, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
        RorCalculator.clampRoR(dBt, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
        double[] det = new double[n];
        double[] dbt = new double[n];
        for (int i = 0; i < n; i++) {
            det[i] = i < dEt.size() ? dEt.get(i) : 0.0;
            dbt[i] = i < dBt.size() ? dBt.get(i) : 0.0;
        }
        if (chart.getDatasets().contains(dataBgDeltaET)) dataBgDeltaET.set(x, det);
        if (chart.getDatasets().contains(dataBgDeltaBT)) dataBgDeltaBT.set(x, dbt);
    }

    private void updatePhaseShading(List<Double> timex, List<Double> temp2) {
        if (phasesConfig == null || timex.isEmpty()) {
            rectDrying.setVisible(false);
            rectMaillard.setVisible(false);
            rectFinishing.setVisible(false);
            rectCooling.setVisible(false);
            return;
        }
        var ti = Phases.timeindexFromIndices(
                canvasData.getChargeIndex(),
                canvasData.getDryEndIndex(),
                canvasData.getFcStartIndex(),
                canvasData.getDropIndex());
        var effective = Phases.getEffectiveTimeindex(timex, temp2, ti, phasesConfig);
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

        double w = pane.getWidth();
        double h = pane.getHeight();
        if (w <= 0 || h <= 0) return;
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        double xMin = xAxis.getMin();
        double xMax = xAxis.getMax();
        double xRange = xMax - xMin;
        if (xRange <= 0) return;
        double scaleX = w / xRange;

        Color c1 = colorConfig.getPaletteColor("rect1");
        Color c2 = colorConfig.getPaletteColor("rect2");
        Color c3 = colorConfig.getPaletteColor("rect3");
        Color c4 = colorConfig.getPaletteColor("rect4");
        rectDrying.setFill(c1 != null ? Color.color(c1.getRed(), c1.getGreen(), c1.getBlue(), PHASE_SHADE_ALPHA) : null);
        rectMaillard.setFill(c2 != null ? Color.color(c2.getRed(), c2.getGreen(), c2.getBlue(), PHASE_SHADE_ALPHA) : null);
        rectFinishing.setFill(c3 != null ? Color.color(c3.getRed(), c3.getGreen(), c3.getBlue(), PHASE_SHADE_ALPHA) : null);
        rectCooling.setFill(c4 != null ? Color.color(c4.getRed(), c4.getGreen(), c4.getBlue(), PHASE_SHADE_ALPHA) : null);

        double x1 = (tStart - xMin) * scaleX;
        double x2 = (tDryEnd - xMin) * scaleX;
        double x3 = (tFcStart - xMin) * scaleX;
        double x4 = (tDrop - xMin) * scaleX;
        rectDrying.setX(x1);
        rectDrying.setY(0);
        rectDrying.setWidth(Math.max(0, x2 - x1));
        rectDrying.setHeight(h);
        rectDrying.setVisible(rectDrying.getWidth() > 0);
        rectMaillard.setX(x2);
        rectMaillard.setY(0);
        rectMaillard.setWidth(Math.max(0, x3 - x2));
        rectMaillard.setHeight(h);
        rectMaillard.setVisible(rectMaillard.getWidth() > 0);
        rectFinishing.setX(x3);
        rectFinishing.setY(0);
        rectFinishing.setWidth(Math.max(0, x4 - x3));
        rectFinishing.setHeight(h);
        rectFinishing.setVisible(rectFinishing.getWidth() > 0);
        rectCooling.setX(x4);
        rectCooling.setY(0);
        rectCooling.setWidth(Math.max(0, w - x4));
        rectCooling.setHeight(h);
        rectCooling.setVisible(rectCooling.getWidth() > 0);
    }

    private static int idxAt(List<Integer> ti, int slot) {
        if (slot >= ti.size()) return -1;
        Integer v = ti.get(slot);
        if (v == null) return -1;
        return (slot == IDX_CHARGE && v >= 0) || (slot != IDX_CHARGE && v > 0) ? v : -1;
    }

    private void updateEventLayers(List<Double> timex, List<Double> temp1Display, List<Double> temp2Display) {
        eventLayer.getChildren().clear();
        double w = pane.getWidth();
        double h = pane.getHeight();
        if (w <= 0 || h <= 0) return;
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
        double xMin = xAxis.getMin();
        double xMax = xAxis.getMax();
        double yMin = yAxis.getMin();
        double yMax = yAxis.getMax();
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;
        if (xRange <= 0 || yRange <= 0) return;
        double scaleX = w / xRange;
        double scaleY = h / yRange;
        List<Double> temp2 = temp2Display;

        Color specialBoxColor = colorConfig.getPaletteColor("specialeventbox");
        Color specialTextColor = colorConfig.getPaletteColor("specialeventtext");
        if (specialBoxColor == null) specialBoxColor = Color.web("#ff5871");
        if (specialTextColor == null) specialTextColor = Color.WHITE;

        // (A) Special events bar at bottom
        double barY = h - SPECIAL_EVENT_BAR_HEIGHT;
        if (eventList != null && !timex.isEmpty()) {
            for (int i = 0; i < eventList.size(); i++) {
                EventEntry e = eventList.get(i);
                int idx = e.getTimeIndex();
                if (idx < 0 || idx >= timex.size()) continue;
                double xSec = timex.get(idx);
                double xPx = (xSec - xMin) * scaleX;
                if (xPx < -2 || xPx > w + 2) continue;
                Rectangle rect = new Rectangle(Math.max(2, 4), SPECIAL_EVENT_BAR_HEIGHT - 2);
                rect.setX(xPx - rect.getWidth() / 2);
                rect.setY(barY + 1);
                rect.setFill(specialBoxColor);
                rect.setStroke(specialTextColor);
                String tooltip = String.format("%s  value=%.1f  %s  t=%.0fs", e.getType(), e.getValue(), e.getLabel(), xSec);
                Tooltip.install(rect, new Tooltip(tooltip));
                eventLayer.getChildren().add(rect);
                Text label = new Text(e.getLabel() != null && !e.getLabel().isEmpty() ? e.getLabel() : e.getType().name());
                label.setFill(specialTextColor);
                label.setStyle("-fx-font-size: 10px;");
                label.setX(xPx);
                label.setY(barY - 2);
                eventLayer.getChildren().add(label);
            }
        }

        // (B) Main event markers: vertical dashed lines
        int chargeIdx = canvasData.getChargeIndex();
        int dryIdx = canvasData.getDryEndIndex();
        int fcStartIdx = canvasData.getFcStartIndex();
        int fcEndIdx = canvasData.getFcEndIndex();
        int dropIdx = canvasData.getDropIndex();
        int coolIdx = -1;
        if (eventList != null) {
            for (int i = 0; i < eventList.size(); i++) {
                if (eventList.get(i).getType() == EventType.COOL_END) {
                    coolIdx = eventList.get(i).getTimeIndex();
                    break;
                }
            }
        }
        int n = timex.size();
        Color rect1 = colorConfig.getPaletteColor("rect1");
        Color rect2 = colorConfig.getPaletteColor("rect2");
        Color rect3 = colorConfig.getPaletteColor("rect3");
        Color rect4 = colorConfig.getPaletteColor("rect4");
        addVerticalMarker(timex, temp2, chargeIdx, 0, h, scaleX, xMin, w, Color.WHITE, "CHARGE", eventLayer);
        addVerticalMarker(timex, temp2, dryIdx, 0, h, scaleX, xMin, w, rect1 != null ? rect1 : Color.GRAY, "DRY END", eventLayer);
        addVerticalMarker(timex, temp2, fcStartIdx, 0, h, scaleX, xMin, w, rect2 != null ? rect2 : Color.GRAY, "FC START", eventLayer);
        addVerticalMarker(timex, temp2, fcEndIdx, 0, h, scaleX, xMin, w, rect3 != null ? rect3 : Color.GRAY, "FC END", eventLayer);
        addVerticalMarker(timex, temp2, dropIdx, 0, h, scaleX, xMin, w, Color.WHITE, "DROP", eventLayer);
        if (coolIdx >= 0 && coolIdx < n) {
            double tCool = timex.get(coolIdx);
            double xPx = (tCool - xMin) * scaleX;
            if (xPx >= 0 && xPx <= w) {
                Line line = new Line(xPx, 0, xPx, h);
                line.setStroke(rect4 != null ? rect4 : Color.GRAY);
                line.getStrokeDashArray().addAll(8.0, 6.0);
                Text lab = new Text("COOL END");
                lab.setFill(rect4 != null ? rect4 : Color.GRAY);
                lab.setStyle("-fx-font-size: 10px;");
                lab.setX(xPx + 2);
                lab.setY(12);
                eventLayer.getChildren().addAll(line, lab);
            }
        }

        // (C) Annotations: callout at (timeSec, tempC)
        if (eventList != null && !timex.isEmpty() && temp2Display != null) {
            for (int i = 0; i < eventList.size(); i++) {
                EventEntry e = eventList.get(i);
                try {
                    EventAnnotation ann = EventAnnotation.fromEntry(e, timex);
                    int idx = e.getTimeIndex();
                    double tempC = idx < temp2Display.size() ? temp2Display.get(idx) : 0.0;
                    double xPx = (ann.getXSec() - xMin) * scaleX;
                    double yPx = (yMax - tempC) / yRange * h;
                    if (xPx < 0 || xPx > w || yPx < 0 || yPx > h) continue;
                    Text callout = new Text(ann.getDisplayLabel());
                    callout.setFill(specialTextColor != null ? specialTextColor : Color.WHITE);
                    callout.setStyle("-fx-font-size: 9px;");
                    callout.setX(xPx + 4);
                    callout.setY(yPx - 4);
                    eventLayer.getChildren().add(callout);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // (D) Time guide line
        if (displaySettings != null && displaySettings.getTimeguideSec() > 0) {
            double tg = displaySettings.getTimeguideSec();
            if (tg >= xMin && tg <= xMax) {
                double xPx = (tg - xMin) * scaleX;
                Line tgLine = new Line(xPx, 0, xPx, h);
                Color timeguideColor = colorConfig.getPaletteColor("timeguide");
                tgLine.setStroke(timeguideColor != null ? timeguideColor : Color.GRAY);
                tgLine.getStrokeDashArray().addAll(8.0, 6.0);
                eventLayer.getChildren().add(tgLine);
            }
        }

        // (E) Watermark
        if (displaySettings != null && displaySettings.isShowWatermark() && roastTitle != null && !roastTitle.isBlank()) {
            Color wmColor = colorConfig.getPaletteColor("watermarks");
            Text wm = new Text(roastTitle);
            wm.setFill(wmColor != null ? wmColor : Color.GRAY);
            wm.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
            wm.setX(Math.max(10, (w - 200) / 2));
            wm.setY(h / 2 - 14);
            eventLayer.getChildren().add(wm);
        }

        // (F) MET — Max ET line and label (temp1Display is ET in display unit)
        int cIdx = canvasData.getChargeIndex();
        int dIdx = canvasData.getDropIndex();
        if (temp1Display != null && cIdx >= 0 && dIdx >= cIdx) {
            double met = MetCalculator.compute(temp1Display, cIdx, dIdx);
            if (Double.isFinite(met) && met >= yMin && met <= yMax) {
                double metY = (yMax - met) / yRange * h;
                Line metLine = new Line(0, metY, w, metY);
                Color metBoxColor = colorConfig.getPaletteColor("metbox");
                metLine.setStroke(metBoxColor != null ? metBoxColor : Color.GRAY);
                metLine.getStrokeDashArray().addAll(8.0, 6.0);
                eventLayer.getChildren().add(metLine);
                Text metLabel = new Text(String.format("MET: %.1f°C", met));
                metLabel.setFill(colorConfig.getPaletteColor("mettext") != null ? colorConfig.getPaletteColor("mettext") : Color.WHITE);
                metLabel.setStyle("-fx-font-size: 10px;");
                metLabel.setX(w - 70);
                metLabel.setY(metY - 4);
                eventLayer.getChildren().add(metLabel);
            }
        }

        // (G) TP — Turning Point (use raw BT for index; temp2Display for display)
        List<Double> temp2Raw = canvasData.getTemp2();
        int endIdx = (dryIdx > 0 ? dryIdx : fcStartIdx > 0 ? fcStartIdx : timex.size() - 1);
        if (cIdx >= 0 && endIdx > cIdx && temp2Raw != null) {
            int tpIdx = RorCalculator.findTurningPoint(temp2Raw, cIdx, endIdx);
            if (tpIdx >= 0 && tpIdx < timex.size() && tpIdx < temp2Display.size()) {
                double tpSec = timex.get(tpIdx);
                double tpTemp = temp2Display.get(tpIdx);
                if (tpSec >= xMin && tpSec <= xMax && Double.isFinite(tpTemp)) {
                    double xPx = (tpSec - xMin) * scaleX;
                    double yPx = (yMax - tpTemp) / yRange * h;
                    Polygon tri = new Polygon();
                    double size = 6;
                    tri.getPoints().addAll(xPx, yPx - size, xPx - size * 0.9, yPx + size * 0.9, xPx + size * 0.9, yPx + size * 0.9);
                    Color textColor = colorConfig.getPaletteColor("text");
                    tri.setFill(textColor != null ? textColor : Color.BLACK);
                    tri.setStroke(textColor != null ? textColor : Color.BLACK);
                    int totalSec = (int) Math.round(tpSec);
                    int mm = totalSec / 60;
                    int ss = totalSec % 60;
                    Text tpLabel = new Text(String.format("TP: %.1f°C @ %d:%02d", tpTemp, mm, ss));
                    tpLabel.setFill(textColor != null ? textColor : Color.BLACK);
                    tpLabel.setStyle("-fx-font-size: 9px;");
                    tpLabel.setX(xPx + 8);
                    tpLabel.setY(yPx + 4);
                    eventLayer.getChildren().addAll(tri, tpLabel);
                }
            }
        }

        // (H) Legend
        if (displaySettings != null && displaySettings.isShowLegend()) {
            Color legendBg = colorConfig.getPaletteColor("legendbg");
            double legendAlpha = colorConfig.getPaletteAlpha("legendbg");
            Color legendBorder = colorConfig.getPaletteColor("legendborder");
            Color textColor = colorConfig.getPaletteColor("text");
            if (textColor == null) textColor = Color.BLACK;
            int row = 0;
            boolean visET = displaySettings.isVisibleET();
            boolean visBT = displaySettings.isVisibleBT();
            boolean visDeltaET = displaySettings.isVisibleDeltaET();
            boolean visDeltaBT = displaySettings.isVisibleDeltaBT();
            if (visET) row++;
            if (visBT) row++;
            if (visDeltaET) row++;
            if (visDeltaBT) row++;
            if (shouldShowBgET()) row++;
            if (shouldShowBgBT()) row++;
            double boxH = row > 0 ? 12 + row * 10 : 22;
            Rectangle legendRect = new Rectangle(8, 8, 140, boxH);
            legendRect.setFill(legendBg != null ? Color.color(legendBg.getRed(), legendBg.getGreen(), legendBg.getBlue(), legendAlpha) : Color.color(1, 1, 1, 0.8));
            legendRect.setStroke(legendBorder != null ? legendBorder : Color.GRAY);
            eventLayer.getChildren().add(legendRect);
            row = 0;
            if (visET) { addLegendRow(eventLayer, 12, 14 + row * 10, colorConfig.getCurveET(), "ET", textColor); row++; }
            if (visBT) { addLegendRow(eventLayer, 12, 14 + row * 10, colorConfig.getCurveBT(), "BT", textColor); row++; }
            if (visDeltaET) { addLegendRow(eventLayer, 12, 14 + row * 10, colorConfig.getCurveDeltaET(), "ΔET", textColor); row++; }
            if (visDeltaBT) { addLegendRow(eventLayer, 12, 14 + row * 10, colorConfig.getCurveDeltaBT(), "ΔBT", textColor); row++; }
            if (shouldShowBgET()) { addLegendRow(eventLayer, 12, 14 + row * 10, colorConfig.getPaletteColor("backgroundmetcolor"), "BG ET", textColor); row++; }
            if (shouldShowBgBT()) { addLegendRow(eventLayer, 12, 14 + row * 10, colorConfig.getPaletteColor("backgroundbtcolor"), "BG BT", textColor); row++; }
        }
    }

    private void addLegendRow(Pane layer, double x, double y, Color lineColor, String label, Color textColor) {
        Line seg = new Line(x, y - 2, x + 16, y - 2);
        seg.setStroke(lineColor != null ? lineColor : Color.GRAY);
        Text t = new Text(label);
        t.setFill(textColor);
        t.setStyle("-fx-font-size: 10px;");
        t.setX(x + 20);
        t.setY(y);
        layer.getChildren().addAll(seg, t);
    }

    private void addVerticalMarker(List<Double> timex, List<Double> temp2, int idx, double y0, double y1,
                                  double scaleX, double xMin, double w, Color color, String labelText, Pane layer) {
        if (idx < 0 || idx >= timex.size()) return;
        double t = timex.get(idx);
        double xPx = (t - xMin) * scaleX;
        if (xPx < 0 || xPx > w) return;
        Line line = new Line(xPx, y0, xPx, y1);
        line.setStroke(color != null ? color : Color.GRAY);
        line.getStrokeDashArray().addAll(8.0, 6.0);
        Text lab = new Text(labelText);
        lab.setFill(color != null ? color : Color.GRAY);
        lab.setStyle("-fx-font-size: 10px;");
        lab.setX(xPx + 2);
        lab.setY(12);
        layer.getChildren().addAll(line, lab);
    }

    private void updateAucLayer(List<Double> timex, List<Double> temp2Display) {
        if (displaySettings == null || timex.isEmpty() || temp2Display == null || temp2Display.size() < timex.size()) {
            aucGuideLine.setVisible(false);
            aucAreaPolygon.setVisible(false);
            return;
        }
        double baseTempC = displaySettings.getAucBaseTemp();
        if (axisConfig.getUnit() == AxisConfig.TemperatureUnit.FAHRENHEIT) {
            baseTempC = AxisConfig.celsiusToFahrenheit(baseTempC);
        }
        int chargeIdx = canvasData.getChargeIndex() >= 0 ? canvasData.getChargeIndex() : 0;
        int dropIdx = canvasData.getDropIndex() > 0 && canvasData.getDropIndex() < timex.size()
            ? canvasData.getDropIndex() : timex.size() - 1;
        if (chargeIdx >= dropIdx) {
            aucGuideLine.setVisible(false);
            aucAreaPolygon.setVisible(false);
            return;
        }

        double w = pane.getWidth();
        double h = pane.getHeight();
        if (w <= 0 || h <= 0) return;
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
        double xMin = xAxis.getMin();
        double xMax = xAxis.getMax();
        double xRange = xMax - xMin;
        if (xRange <= 0) return;
        double yMin = yAxis.getMin();
        double yMax = yAxis.getMax();
        double yRange = yMax - yMin;
        if (yRange <= 0) return;
        double scaleX = w / xRange;
        double scaleY = h / yRange;

        double baseY = (yMax - baseTempC) / yRange * h;
        if (baseY < 0) baseY = 0;
        if (baseY > h) baseY = h;

        Color guideColor = colorConfig.getPaletteColor("aucguide");
        if (guideColor != null) {
            aucGuideLine.setStroke(guideColor);
            aucGuideLine.setStartX(0);
            aucGuideLine.setStartY(baseY);
            aucGuideLine.setEndX(w);
            aucGuideLine.setEndY(baseY);
            aucGuideLine.setVisible(true);
        } else {
            aucGuideLine.setVisible(false);
        }

        Color areaColor = colorConfig.getPaletteColor("aucarea");
        double areaAlpha = colorConfig.getPaletteAlpha("aucarea");
        if (areaColor == null) areaAlpha = 0.3;
        Color fillColor = areaColor != null
            ? Color.color(areaColor.getRed(), areaColor.getGreen(), areaColor.getBlue(), Double.isFinite(areaAlpha) ? areaAlpha : 0.3)
            : Color.color(0.45, 0.45, 0.45, 0.3);
        aucAreaPolygon.setFill(fillColor);
        aucAreaPolygon.setStroke(null);

        java.util.List<Double> points = new java.util.ArrayList<>();
        double tStart = timex.get(chargeIdx);
        double tEnd = timex.get(dropIdx);
        double xStart = (tStart - xMin) * scaleX;
        double xEnd = (tEnd - xMin) * scaleX;
        points.add(xStart);
        points.add(baseY);
        for (int i = chargeIdx; i <= dropIdx && i < temp2Display.size(); i++) {
            double t = timex.get(i);
            double bt = temp2Display.get(i);
            if (!Double.isFinite(bt)) continue;
            double x = (t - xMin) * scaleX;
            double tempY = (yMax - bt) / yRange * h;
            if (tempY < 0) tempY = 0;
            if (tempY > h) tempY = h;
            points.add(x);
            points.add(tempY);
        }
        points.add(xEnd);
        points.add(baseY);
        aucAreaPolygon.getPoints().setAll(points);
        aucAreaPolygon.setVisible(true);
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
     * updates RoR via LiveRorCalculator (clamped), and updates chart on JavaFX thread.
     */
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
        javafx.application.Platform.runLater(this::updateChart);
    }

    private static double clamp(double v, double min, double max) {
        if (!Double.isFinite(v)) return 0.0;
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    /** Resets live RoR calculators. Call when recording starts (CHARGE event). */
    public void resetLiveRor() {
        liveRorET.reset();
        liveRorBT.reset();
    }
}
