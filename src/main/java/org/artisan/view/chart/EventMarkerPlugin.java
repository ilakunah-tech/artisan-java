package org.artisan.view.chart;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.Axis;
import io.fair_acc.chartfx.plugins.ChartPlugin;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Chart-FX plugin for event markers (CHARGE, DRY_END, FC_START, FC_END,
 * SC_START, SC_END, DROP, COOL_END), custom event annotations, the special
 * events bar, MET line, Turning Point marker, watermark, time guide,
 * and a custom hand-drawn legend.
 */
public final class EventMarkerPlugin extends ChartPlugin {

    private static final double SPECIAL_EVENT_BAR_HEIGHT = 20.0;
    private static final double EVENT_MARKER_HIT_PX = 8.0;

    private CanvasData canvasData;
    private ColorConfig colorConfig;
    private DisplaySettings displaySettings;
    private EventList eventList;
    private BackgroundProfile backgroundProfile;
    private String roastTitle;
    private boolean liveRecording;
    private int draggedEventIndex = -1;
    private Runnable onEventMoved;
    private Consumer<ChartClickInfo> onChartBodyClick;
    private Runnable requestUpdate;

    private final List<Node> dynamicNodes = new ArrayList<>();

    public EventMarkerPlugin() {
        registerInputEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        registerInputEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        registerInputEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
    }

    public void setCanvasData(CanvasData data)            { this.canvasData = data; }
    public void setColorConfig(ColorConfig cfg)           { this.colorConfig = cfg; }
    public void setDisplaySettings(DisplaySettings ds)    { this.displaySettings = ds; }
    public void setEventList(EventList list)              { this.eventList = list; }
    public void setRoastTitle(String title)               { this.roastTitle = title; }
    public void setLiveRecording(boolean live)            { this.liveRecording = live; }
    public void setOnEventMoved(Runnable r)               { this.onEventMoved = r; }
    public void setOnChartBodyClick(Consumer<ChartClickInfo> c) { this.onChartBodyClick = c; }
    public void setBackgroundProfile(BackgroundProfile bp) { this.backgroundProfile = bp; }
    public void setRequestUpdate(Runnable r)              { this.requestUpdate = r; }

    /** Data passed when user clicks chart body. */
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

    /**
     * Rebuilds all event markers. Call from updateChart().
     *
     * @param timex       time array
     * @param etDisplay   ET values in display units
     * @param btDisplay   BT values in display units
     * @param rorBT       RoR BT values
     */
    public void refresh(List<Double> timex, List<Double> etDisplay, List<Double> btDisplay, List<Double> rorBT) {
        getChartChildren().removeAll(dynamicNodes);
        dynamicNodes.clear();

        if (getChart() == null || canvasData == null || colorConfig == null) return;
        if (timex == null || timex.isEmpty()) return;

        XYChart xyChart = (XYChart) getChart();
        Axis xAxis = xyChart.getXAxis();
        Axis yAxis = xyChart.getYAxis();

        double canvasW = xyChart.getCanvas().getWidth();
        double canvasH = xyChart.getCanvas().getHeight();
        if (canvasW <= 0 || canvasH <= 0) return;
        double plotX = xyChart.getCanvas().getLayoutX();
        double plotY = xyChart.getCanvas().getLayoutY();

        Color specialBoxColor = colorConfig.getPaletteColor("specialeventbox");
        Color specialTextColor = colorConfig.getPaletteColor("specialeventtext");
        if (specialBoxColor == null) specialBoxColor = Color.web("#ff5871");
        if (specialTextColor == null) specialTextColor = Color.WHITE;

        // (A) Special events bar at bottom
        double barY = plotY + canvasH - SPECIAL_EVENT_BAR_HEIGHT;
        if (eventList != null) {
            for (int i = 0; i < eventList.size(); i++) {
                EventEntry e = eventList.get(i);
                int idx = e.getTimeIndex();
                if (idx < 0 || idx >= timex.size()) continue;
                double xSec = timex.get(idx);
                double xPx = xAxis.getDisplayPosition(xSec) + plotX;
                if (xPx < plotX - 2 || xPx > plotX + canvasW + 2) continue;
                Rectangle rect = new Rectangle(4, SPECIAL_EVENT_BAR_HEIGHT - 2);
                rect.setX(xPx - 2);
                rect.setY(barY + 1);
                rect.setFill(specialBoxColor);
                rect.setStroke(specialTextColor);
                rect.setMouseTransparent(true);
                Tooltip.install(rect, new Tooltip(
                    String.format("%s  value=%.1f  %s  t=%.0fs", e.getType(), e.getValue(), e.getLabel(), xSec)));
                addNode(rect);

                Text label = new Text(e.getLabel() != null && !e.getLabel().isEmpty() ? e.getLabel() : e.getType().name());
                label.setFill(specialTextColor);
                label.setStyle("-fx-font-size: 10px;");
                label.setX(xPx);
                label.setY(barY - 2);
                label.setMouseTransparent(true);
                addNode(label);
            }
        }

        // (B) Main event markers: vertical dashed lines
        Color rect1 = colorConfig.getPaletteColor("rect1");
        Color rect2 = colorConfig.getPaletteColor("rect2");
        Color rect3 = colorConfig.getPaletteColor("rect3");
        Color rect4 = colorConfig.getPaletteColor("rect4");
        addVerticalMarker(xAxis, timex, canvasData.getChargeIndex(), plotX, plotY, canvasH, canvasW, Color.WHITE, "CHARGE");
        addVerticalMarker(xAxis, timex, canvasData.getDryEndIndex(), plotX, plotY, canvasH, canvasW, rect1 != null ? rect1 : Color.GRAY, "DRY END");
        addVerticalMarker(xAxis, timex, canvasData.getFcStartIndex(), plotX, plotY, canvasH, canvasW, rect2 != null ? rect2 : Color.GRAY, "FC START");
        addVerticalMarker(xAxis, timex, canvasData.getFcEndIndex(), plotX, plotY, canvasH, canvasW, rect3 != null ? rect3 : Color.GRAY, "FC END");
        addVerticalMarker(xAxis, timex, canvasData.getScStartIndex(), plotX, plotY, canvasH, canvasW, rect3 != null ? rect3 : Color.GRAY, "SC START");
        addVerticalMarker(xAxis, timex, canvasData.getScEndIndex(), plotX, plotY, canvasH, canvasW, rect3 != null ? rect3 : Color.GRAY, "SC END");
        addVerticalMarker(xAxis, timex, canvasData.getDropIndex(), plotX, plotY, canvasH, canvasW, Color.WHITE, "DROP");

        int coolIdx = findCoolEndIndex();
        if (coolIdx >= 0 && coolIdx < timex.size()) {
            addVerticalMarker(xAxis, timex, coolIdx, plotX, plotY, canvasH, canvasW, rect4 != null ? rect4 : Color.GRAY, "COOL END");
        }

        // (C) Callout annotations for custom events
        if (eventList != null && btDisplay != null) {
            for (int i = 0; i < eventList.size(); i++) {
                EventEntry e = eventList.get(i);
                try {
                    EventAnnotation ann = EventAnnotation.fromEntry(e, timex);
                    int idx = e.getTimeIndex();
                    double tempC = idx < btDisplay.size() ? btDisplay.get(idx) : 0.0;
                    double xPx = xAxis.getDisplayPosition(ann.getXSec()) + plotX;
                    double yPx = yAxis.getDisplayPosition(tempC) + plotY;
                    if (xPx < plotX || xPx > plotX + canvasW || yPx < plotY || yPx > plotY + canvasH) continue;
                    Text callout = new Text(ann.getDisplayLabel());
                    callout.setFill(specialTextColor);
                    callout.setStyle("-fx-font-size: 9px;");
                    callout.setX(xPx + 4);
                    callout.setY(yPx - 4);
                    callout.setMouseTransparent(true);
                    addNode(callout);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // (D) Time guide line
        if (displaySettings != null && displaySettings.getTimeguideSec() > 0) {
            double tg = displaySettings.getTimeguideSec();
            double xPx = xAxis.getDisplayPosition(tg) + plotX;
            if (xPx >= plotX && xPx <= plotX + canvasW) {
                Line tgLine = new Line(xPx, plotY, xPx, plotY + canvasH);
                Color timeguideColor = colorConfig.getPaletteColor("timeguide");
                tgLine.setStroke(timeguideColor != null ? timeguideColor : Color.GRAY);
                tgLine.getStrokeDashArray().addAll(8.0, 6.0);
                tgLine.setMouseTransparent(true);
                addNode(tgLine);
            }
        }

        // (E) Watermark
        if (displaySettings != null && displaySettings.isShowWatermark() && roastTitle != null && !roastTitle.isBlank()) {
            Color wmColor = colorConfig.getPaletteColor("watermarks");
            Text wm = new Text(roastTitle);
            wm.setFill(wmColor != null ? wmColor : Color.GRAY);
            wm.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
            wm.setX(plotX + Math.max(10, (canvasW - 200) / 2));
            wm.setY(plotY + canvasH / 2 - 14);
            wm.setMouseTransparent(true);
            addNode(wm);
        }

        // (F) MET line
        int cIdx = canvasData.getChargeIndex();
        int dIdx = canvasData.getDropIndex();
        if (etDisplay != null && cIdx >= 0 && dIdx >= cIdx) {
            double met = MetCalculator.compute(etDisplay, cIdx, dIdx);
            if (Double.isFinite(met)) {
                double metY = yAxis.getDisplayPosition(met) + plotY;
                if (metY >= plotY && metY <= plotY + canvasH) {
                    Line metLine = new Line(plotX, metY, plotX + canvasW, metY);
                    Color metBoxColor = colorConfig.getPaletteColor("metbox");
                    metLine.setStroke(metBoxColor != null ? metBoxColor : Color.GRAY);
                    metLine.getStrokeDashArray().addAll(8.0, 6.0);
                    metLine.setMouseTransparent(true);
                    addNode(metLine);
                    String unitStr = "°C";
                    Text metLabel = new Text(String.format("MET: %.1f%s", met, unitStr));
                    metLabel.setFill(colorConfig.getPaletteColor("mettext") != null
                            ? colorConfig.getPaletteColor("mettext") : Color.WHITE);
                    metLabel.setStyle("-fx-font-size: 10px;");
                    metLabel.setX(plotX + canvasW - 80);
                    metLabel.setY(metY - 4);
                    metLabel.setMouseTransparent(true);
                    addNode(metLabel);
                }
            }
        }

        // (G) Turning Point
        List<Double> temp2Raw = canvasData.getTemp2();
        int dryIdx = canvasData.getDryEndIndex();
        int fcStartIdx = canvasData.getFcStartIndex();
        int endIdx = dryIdx > 0 ? dryIdx : (fcStartIdx > 0 ? fcStartIdx : timex.size() - 1);
        if (cIdx >= 0 && endIdx > cIdx && temp2Raw != null && btDisplay != null) {
            int tpIdx = RorCalculator.findTurningPoint(temp2Raw, cIdx, endIdx);
            if (tpIdx >= 0 && tpIdx < timex.size() && tpIdx < btDisplay.size()) {
                double tpSec = timex.get(tpIdx);
                double tpTemp = btDisplay.get(tpIdx);
                if (Double.isFinite(tpTemp)) {
                    double xPx = xAxis.getDisplayPosition(tpSec) + plotX;
                    double yPx = yAxis.getDisplayPosition(tpTemp) + plotY;
                    if (xPx >= plotX && xPx <= plotX + canvasW && yPx >= plotY && yPx <= plotY + canvasH) {
                        double size = 6;
                        Polygon tri = new Polygon(
                                xPx, yPx - size,
                                xPx - size * 0.9, yPx + size * 0.9,
                                xPx + size * 0.9, yPx + size * 0.9);
                        Color textColor = colorConfig.getPaletteColor("text");
                        tri.setFill(textColor != null ? textColor : Color.BLACK);
                        tri.setStroke(textColor != null ? textColor : Color.BLACK);
                        tri.setMouseTransparent(true);
                        addNode(tri);

                        int totalSec = (int) Math.round(tpSec);
                        Text tpLabel = new Text(String.format("TP: %.1f°C @ %d:%02d", tpTemp, totalSec / 60, totalSec % 60));
                        tpLabel.setFill(textColor != null ? textColor : Color.BLACK);
                        tpLabel.setStyle("-fx-font-size: 9px;");
                        tpLabel.setX(xPx + 8);
                        tpLabel.setY(yPx + 4);
                        tpLabel.setMouseTransparent(true);
                        addNode(tpLabel);
                    }
                }
            }
        }

        // (H.bg) Background profile event markers (semi-transparent dashed lines)
        if (backgroundProfile != null && backgroundProfile.getProfileData() != null && !backgroundProfile.isEmpty()) {
            ProfileData bgPd = backgroundProfile.getProfileData();
            List<Integer> bgTi = bgPd.getTimeindex();
            List<Double> bgTimex = bgPd.getTimex();
            double bgOffset = backgroundProfile.getAlignOffset();
            if (bgTi != null && bgTimex != null && !bgTimex.isEmpty()) {
                String[] bgNames = {"CHARGE", "DRY END", "FC START", "FC END", "SC START", "SC END", "DROP", "COOL END"};
                Color bgMarkerColor = Color.color(0.6, 0.6, 0.6, 0.4);
                for (int slot = 0; slot < Math.min(bgTi.size(), bgNames.length); slot++) {
                    Integer idx = bgTi.get(slot);
                    if (idx == null || idx <= 0) continue;
                    if (idx >= bgTimex.size()) continue;
                    double t = bgTimex.get(idx) + bgOffset;
                    double xPx = xAxis.getDisplayPosition(t) + plotX;
                    if (xPx < plotX || xPx > plotX + canvasW) continue;
                    Line bgLine = new Line(xPx, plotY, xPx, plotY + canvasH);
                    bgLine.setStroke(bgMarkerColor);
                    bgLine.getStrokeDashArray().addAll(4.0, 8.0);
                    bgLine.setMouseTransparent(true);
                    addNode(bgLine);
                    Text bgLabel = new Text("bg:" + bgNames[slot]);
                    bgLabel.setFill(bgMarkerColor);
                    bgLabel.setStyle("-fx-font-size: 8px;");
                    bgLabel.setX(xPx + 2);
                    bgLabel.setY(plotY + canvasH - 8);
                    bgLabel.setMouseTransparent(true);
                    addNode(bgLabel);
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
            double boxH = row > 0 ? 12 + row * 14 : 22;
            Rectangle legendRect = new Rectangle(plotX + 8, plotY + 8, 140, boxH);
            legendRect.setFill(legendBg != null
                    ? Color.color(legendBg.getRed(), legendBg.getGreen(), legendBg.getBlue(), Double.isFinite(legendAlpha) ? legendAlpha : 0.8)
                    : Color.color(1, 1, 1, 0.8));
            legendRect.setStroke(legendBorder != null ? legendBorder : Color.GRAY);
            legendRect.setMouseTransparent(true);
            addNode(legendRect);
            row = 0;
            double lx = plotX + 12;
            double ly = plotY + 18;
            if (visET) { addLegendRow(lx, ly + row * 14, colorConfig.getCurveET(), "ET", textColor); row++; }
            if (visBT) { addLegendRow(lx, ly + row * 14, colorConfig.getCurveBT(), "BT", textColor); row++; }
            if (visDeltaET) { addLegendRow(lx, ly + row * 14, colorConfig.getCurveDeltaET(), "ΔET", textColor); row++; }
            if (visDeltaBT) { addLegendRow(lx, ly + row * 14, colorConfig.getCurveDeltaBT(), "ΔBT", textColor); row++; }
        }
    }

    private void addVerticalMarker(Axis xAxis, List<Double> timex, int idx,
                                   double plotX, double plotY, double canvasH, double canvasW,
                                   Color color, String labelText) {
        if (idx < 0 || idx >= timex.size()) return;
        double t = timex.get(idx);
        double xPx = xAxis.getDisplayPosition(t) + plotX;
        if (xPx < plotX || xPx > plotX + canvasW) return;
        Line line = new Line(xPx, plotY, xPx, plotY + canvasH);
        line.setStroke(color);
        line.getStrokeDashArray().addAll(8.0, 6.0);
        line.setMouseTransparent(true);
        addNode(line);
        Text lab = new Text(labelText);
        lab.setFill(color);
        lab.setStyle("-fx-font-size: 10px;");
        lab.setX(xPx + 2);
        lab.setY(plotY + 12);
        lab.setMouseTransparent(true);
        addNode(lab);
    }

    private void addLegendRow(double x, double y, Color lineColor, String label, Color textColor) {
        Line seg = new Line(x, y - 2, x + 16, y - 2);
        seg.setStroke(lineColor != null ? lineColor : Color.GRAY);
        seg.setMouseTransparent(true);
        addNode(seg);
        Text t = new Text(label);
        t.setFill(textColor);
        t.setStyle("-fx-font-size: 10px;");
        t.setX(x + 20);
        t.setY(y);
        t.setMouseTransparent(true);
        addNode(t);
    }

    private void addNode(javafx.scene.Node node) {
        dynamicNodes.add(node);
        getChartChildren().add(node);
    }

    private int findCoolEndIndex() {
        if (eventList == null) return -1;
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getType() == EventType.COOL_END) {
                return eventList.get(i).getTimeIndex();
            }
        }
        return -1;
    }

    private void onMousePressed(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY || liveRecording || eventList == null || canvasData == null) return;
        XYChart xyChart = (XYChart) getChart();
        if (xyChart == null) return;
        Axis xAxis = xyChart.getXAxis();
        double plotX = xyChart.getCanvas().getLayoutX();
        double plotY = xyChart.getCanvas().getLayoutY();
        double canvasH = xyChart.getCanvas().getHeight();

        double barY = plotY + canvasH - SPECIAL_EVENT_BAR_HEIGHT;
        double mx = e.getX();
        double my = e.getY();

        if (my < barY) {
            if (onChartBodyClick != null) {
                List<Double> timex = canvasData.getTimex();
                if (timex != null && !timex.isEmpty()) {
                    double timeSec = xAxis.getValueForDisplay(mx - plotX);
                    int idx = nearestTimeIndex(timex, timeSec);
                    if (idx >= 0) {
                        double bt = idx < canvasData.getTemp2().size() ? canvasData.getTemp2().get(idx) : 0;
                        double et = idx < canvasData.getTemp1().size() ? canvasData.getTemp1().get(idx) : 0;
                        onChartBodyClick.accept(new ChartClickInfo(timeSec, idx, bt, et));
                    }
                }
            }
            return;
        }

        List<Double> timex = canvasData.getTimex();
        for (int i = 0; i < eventList.size(); i++) {
            EventEntry ev = eventList.get(i);
            int idx = ev.getTimeIndex();
            if (idx < 0 || idx >= timex.size()) continue;
            double xPx = xAxis.getDisplayPosition(timex.get(idx)) + plotX;
            if (Math.abs(mx - xPx) <= EVENT_MARKER_HIT_PX) {
                draggedEventIndex = i;
                return;
            }
        }
    }

    private void onMouseDragged(MouseEvent e) {
        if (draggedEventIndex < 0 || eventList == null || canvasData == null) return;
        XYChart xyChart = (XYChart) getChart();
        if (xyChart == null) return;
        Axis xAxis = xyChart.getXAxis();
        double plotX = xyChart.getCanvas().getLayoutX();
        List<Double> timex = canvasData.getTimex();
        if (timex.isEmpty()) return;
        double timeSec = xAxis.getValueForDisplay(e.getX() - plotX);
        int newIdx = nearestTimeIndex(timex, timeSec);
        if (newIdx < 0) return;
        EventEntry old = eventList.get(draggedEventIndex);
        double temp = newIdx < canvasData.getTemp2().size() ? canvasData.getTemp2().get(newIdx) : 0.0;
        eventList.set(draggedEventIndex, new EventEntry(newIdx, temp, old.getLabel(), old.getType(), old.getValue()));
        if (requestUpdate != null) requestUpdate.run();
    }

    private void onMouseReleased(MouseEvent e) {
        if (draggedEventIndex >= 0) {
            if (onEventMoved != null) onEventMoved.run();
            draggedEventIndex = -1;
        }
    }

    static int nearestTimeIndex(List<Double> timex, double timeSec) {
        if (timex == null || timex.isEmpty()) return -1;
        int best = 0;
        double bestDist = Math.abs(timex.get(0) - timeSec);
        for (int i = 1; i < timex.size(); i++) {
            double d = Math.abs(timex.get(i) - timeSec);
            if (d < bestDist) { bestDist = d; best = i; }
        }
        return best;
    }
}
