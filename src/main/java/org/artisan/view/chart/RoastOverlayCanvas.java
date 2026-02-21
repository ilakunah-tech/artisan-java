package org.artisan.view.chart;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.Axis;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.*;
import org.artisan.view.RoastChartController;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Single Canvas overlay that paints ALL chart overlays via GraphicsContext.
 * No Scene Graph nodes are created during live recording.
 * Drawing order (back to front):
 *   1. Phase shading
 *   2. AUC gradient fill
 *   3. Time guide line
 *   4. Event vertical markers + pill labels
 *   5. Background profile markers
 *   6. MET horizontal line
 *   7. Turning Point triangle
 *   8. Watermark
 *   9. Highlight line
 *  10. Crosshair + pill tooltip
 *  11. PhaseStrip (Cropster RI5 style — bottom of plot)
 */
public final class RoastOverlayCanvas extends Canvas {

    // PhaseStrip constants
    private static final double STRIP_H      = 30.0;
    private static final Color  COL_DRY      = Color.rgb(255, 193,   7, 0.90);
    private static final Color  COL_MAILLARD = Color.rgb(121,  85,  72, 0.85);
    private static final Color  COL_DEV      = Color.rgb( 56, 142,  60, 0.90);
    private static final Color  COL_REMAINDER= Color.rgb(200, 200, 200, 0.50);

    private XYChart chart;
    private Axis xAxis;
    private Axis yAxis;

    private CanvasData      canvasData;
    private ColorConfig     colorConfig;
    private DisplaySettings displaySettings;
    private EventList       eventList;
    private BackgroundProfile backgroundProfile;
    private PhasesConfig    phasesConfig;
    private String          roastTitle;

    private double crosshairX       = Double.NaN;
    private double highlightTimeSec = Double.NaN;

    private List<Double> lastTimex;
    private List<Double> lastBT;
    private List<Double> lastET;
    private List<Double> lastRorBT;

    private BiConsumer<Double, Double>                    onCursorMoved;
    private Consumer<RoastChartController.ChartClickInfo> onChartBodyClick;

    public RoastOverlayCanvas() {
        // The canvas itself is transparent to mouse — handlers are added to the chart in setChart()
        setMouseTransparent(true);
    }

    // ── Setters ──────────────────────────────────────────────────────

    public void setChart(XYChart chart, Axis xAxis, Axis yAxis) {
        this.chart = chart;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        widthProperty().bind(chart.widthProperty());
        heightProperty().bind(chart.heightProperty());
        widthProperty().addListener(obs  -> redraw());
        heightProperty().addListener(obs -> redraw());

        // Attach mouse handlers to the chart node so the Zoomer plugin still receives events
        chart.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            crosshairX = e.getX();
            redraw();
        });
        chart.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            crosshairX = Double.NaN;
            redraw();
        });
        chart.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (onChartBodyClick == null || lastTimex == null) return;
            javafx.scene.canvas.Canvas ic = chart.getCanvas();
            javafx.geometry.Bounds cb;
            try {
                cb = chart.sceneToLocal(ic.localToScene(ic.getBoundsInLocal()));
            } catch (Exception ex) { return; }
            double plotX = cb.getMinX();
            double timeSec = xAxis.getValueForDisplay(e.getX() - plotX);
            int idx = nearestIndex(lastTimex, timeSec);
            if (idx < 0) return;
            double bt = (lastBT != null && idx < lastBT.size()) ? lastBT.get(idx) : 0;
            double et = (lastET != null && idx < lastET.size()) ? lastET.get(idx) : 0;
            onChartBodyClick.accept(new RoastChartController.ChartClickInfo(timeSec, idx, bt, et));
        });
    }

    public void setCanvasData(CanvasData d)               { this.canvasData   = d; }
    public void setColorConfig(ColorConfig c)              { this.colorConfig  = c; }
    public void setDisplaySettings(DisplaySettings ds)     { this.displaySettings = ds; }
    public void setEventList(EventList el)                 { this.eventList    = el; }
    public void setBackgroundProfile(BackgroundProfile bp) { this.backgroundProfile = bp; }
    public void setPhasesConfig(PhasesConfig pc)           { this.phasesConfig = pc; }
    public void setRoastTitle(String t)                    { this.roastTitle   = t; }
    public void setHighlightTimeSec(double t)              { this.highlightTimeSec = t; }
    public void setOnCursorMoved(BiConsumer<Double, Double> cb) { this.onCursorMoved = cb; }
    public void setOnChartBodyClick(Consumer<RoastChartController.ChartClickInfo> cb) {
        this.onChartBodyClick = cb;
    }

    // ── Main entry ───────────────────────────────────────────────────

    public void redraw(List<Double> timex, List<Double> bt,
                       List<Double> et,    List<Double> rorBT) {
        this.lastTimex = timex;
        this.lastBT    = bt;
        this.lastET    = et;
        this.lastRorBT = rorBT;
        redraw();
    }

    public void redraw() {
        if (chart == null) return;
        double w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        javafx.scene.canvas.Canvas innerCanvas = chart.getCanvas();
        javafx.geometry.Bounds canvasBounds;
        try {
            canvasBounds = chart.sceneToLocal(innerCanvas.localToScene(innerCanvas.getBoundsInLocal()));
        } catch (Exception e) {
            return;
        }
        double plotX = canvasBounds.getMinX();
        double plotY = canvasBounds.getMinY();
        double plotW = canvasBounds.getWidth();
        double plotH = canvasBounds.getHeight();
        if (plotW <= 0 || plotH <= 0) return;

        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        drawPhaseShading    (gc, plotX, plotY, plotW, plotH);
        drawAUCGradient     (gc, plotX, plotY, plotW, plotH);
        drawTimeGuide       (gc, plotX, plotY, plotW, plotH);
        drawEventMarkers    (gc, plotX, plotY, plotW, plotH);
        drawBackgroundMarkers(gc, plotX, plotY, plotW, plotH);
        drawMETLine         (gc, plotX, plotY, plotW, plotH);
        drawTurningPoint    (gc, plotX, plotY, plotW, plotH);
        drawWatermark       (gc, plotX, plotY, plotW, plotH);
        drawHighlightLine   (gc, plotX, plotY, plotW, plotH);
        drawCrosshair       (gc, plotX, plotY, plotW, plotH);
        drawPhaseStrip      (gc, plotX, plotY, plotW, plotH);
    }

    // ═══════════════════════════════════════════════════
    // PHASE STRIP — Cropster RI5 style
    // ═══════════════════════════════════════════════════
    private void drawPhaseStrip(GraphicsContext gc,
                                double px, double py, double pw, double ph) {
        if (lastTimex == null || lastTimex.isEmpty() || canvasData == null) return;

        double stripY = py + ph - STRIP_H;

        int chargeIdx = canvasData.getChargeIndex();
        int dryIdx    = canvasData.getDryEndIndex();
        int fcIdx     = canvasData.getFcStartIndex();
        int dropIdx   = canvasData.getDropIndex();

        if (chargeIdx < 0 || chargeIdx >= lastTimex.size()) return;

        double chargePx  = xAxis.getDisplayPosition(lastTimex.get(chargeIdx)) + px;
        double stripStart = Math.max(px, chargePx);
        double stripEnd   = px + pw;

        // Background: dark grey remainder bar
        gc.setFill(COL_REMAINDER);
        gc.fillRect(stripStart, stripY, stripEnd - stripStart, STRIP_H);

        // DRY phase: CHARGE → DRY_END
        if (dryIdx >= 0 && dryIdx < lastTimex.size()) {
            double dryPx  = xAxis.getDisplayPosition(lastTimex.get(dryIdx)) + px;
            double dryEnd = Math.min(dryPx, stripEnd);
            if (dryEnd > stripStart) {
                gc.setFill(COL_DRY);
                gc.fillRect(stripStart, stripY, dryEnd - stripStart, STRIP_H);
                gc.setStroke(Color.web("#000000", 0.25));
                gc.setLineWidth(1.0);
                gc.strokeLine(dryEnd, stripY, dryEnd, stripY + STRIP_H);
                double segW = dryEnd - stripStart;
                if (segW > 50) {
                    gc.setFont(Font.font("System", FontWeight.BOLD, 11));
                    gc.setFill(Color.rgb(60, 40, 0, 0.95));
                    String pct = formatPct(chargeIdx, chargeIdx, dryIdx, dropIdx);
                    gc.fillText("DRY " + pct, stripStart + 6, stripY + STRIP_H - 8);
                }
            }
        }

        // MAILLARD phase: DRY_END → FC_START
        if (dryIdx >= 0 && fcIdx >= 0
                && dryIdx < lastTimex.size() && fcIdx < lastTimex.size()) {
            double malStart = Math.max(
                xAxis.getDisplayPosition(lastTimex.get(dryIdx)) + px, px);
            double malEnd   = Math.min(
                xAxis.getDisplayPosition(lastTimex.get(fcIdx))  + px, stripEnd);
            if (malEnd > malStart) {
                gc.setFill(COL_MAILLARD);
                gc.fillRect(malStart, stripY, malEnd - malStart, STRIP_H);
                gc.setStroke(Color.web("#000000", 0.25));
                gc.setLineWidth(1.0);
                gc.strokeLine(malEnd, stripY, malEnd, stripY + STRIP_H);
                double segW = malEnd - malStart;
                if (segW > 75) {
                    gc.setFont(Font.font("System", FontWeight.BOLD, 11));
                    gc.setFill(Color.WHITE);
                    String pct = formatPct(chargeIdx, dryIdx, fcIdx, dropIdx);
                    gc.fillText("MAILLARD " + pct, malStart + 6, stripY + STRIP_H - 8);
                }
            }
        }

        // DEV phase: FC_START → DROP
        if (fcIdx >= 0 && dropIdx >= 0
                && fcIdx < lastTimex.size() && dropIdx < lastTimex.size()) {
            double devStart = Math.max(
                xAxis.getDisplayPosition(lastTimex.get(fcIdx))   + px, px);
            double devEnd   = Math.min(
                xAxis.getDisplayPosition(lastTimex.get(dropIdx)) + px, stripEnd);
            if (devEnd > devStart) {
                gc.setFill(COL_DEV);
                gc.fillRect(devStart, stripY, devEnd - devStart, STRIP_H);
                double segW = devEnd - devStart;
                if (segW > 40) {
                    gc.setFont(Font.font("System", FontWeight.BOLD, 11));
                    gc.setFill(Color.WHITE);
                    String dtr = formatDTR(chargeIdx, fcIdx, dropIdx);
                    gc.fillText("DT " + dtr, devStart + 6, stripY + STRIP_H - 8);
                }
            }
        }

        // Top border
        gc.setStroke(Color.web("#aaaaaa", 0.60));
        gc.setLineWidth(1.0);
        gc.strokeLine(px, stripY, px + pw, stripY);
    }

    /** Returns "NN%" of a segment relative to total roast time (CHARGE→DROP). */
    private String formatPct(int chargeIdx, int segStart, int segEnd, int dropIdx) {
        if (lastTimex == null) return "";
        if (chargeIdx < 0 || dropIdx <= chargeIdx
                || segStart < 0 || segEnd < 0
                || dropIdx >= lastTimex.size()
                || segStart >= lastTimex.size()
                || segEnd >= lastTimex.size()) return "";
        double total = lastTimex.get(dropIdx) - lastTimex.get(chargeIdx);
        if (total <= 0) return "";
        double seg = lastTimex.get(segEnd) - lastTimex.get(segStart);
        int pct = (int) Math.round(100 * seg / total);
        return pct + "%";
    }

    /** DTR = (FC_START → DROP) / (CHARGE → DROP) × 100%. */
    private String formatDTR(int chargeIdx, int fcIdx, int dropIdx) {
        return formatPct(chargeIdx, fcIdx, dropIdx, dropIdx);
    }

    // ═══════════════════════════════════════════════════
    // PHASE SHADING
    // ═══════════════════════════════════════════════════
    private void drawPhaseShading(GraphicsContext gc,
                                   double px, double py, double pw, double ph) {
        if (canvasData == null || lastTimex == null || lastTimex.isEmpty()) return;
        double shadingH = ph - STRIP_H;
        drawPhaseRect(gc, px, py, pw, shadingH,
            canvasData.getChargeIndex(), canvasData.getDryEndIndex(),
            color("phasesdry", Color.web("#85c1e9", 0.12)));
        drawPhaseRect(gc, px, py, pw, shadingH,
            canvasData.getDryEndIndex(), canvasData.getFcStartIndex(),
            color("phasesmid", Color.web("#f8c471", 0.12)));
        drawPhaseRect(gc, px, py, pw, shadingH,
            canvasData.getFcStartIndex(), canvasData.getDropIndex(),
            color("phasesdev", Color.web("#82e0aa", 0.12)));
    }

    private void drawPhaseRect(GraphicsContext gc,
                                double px, double py, double pw, double ph,
                                int startIdx, int endIdx, Color fill) {
        if (startIdx < 0 || endIdx <= startIdx || lastTimex == null) return;
        if (startIdx >= lastTimex.size() || endIdx >= lastTimex.size()) return;
        double x1 = xAxis.getDisplayPosition(lastTimex.get(startIdx)) + px;
        double x2 = xAxis.getDisplayPosition(lastTimex.get(endIdx))   + px;
        if (x2 < px || x1 > px + pw) return;
        x1 = Math.max(px, x1);
        x2 = Math.min(px + pw, x2);
        gc.setFill(fill);
        gc.fillRect(x1, py, x2 - x1, ph);
    }

    // ═══════════════════════════════════════════════════
    // AUC GRADIENT FILL
    // ═══════════════════════════════════════════════════
    private void drawAUCGradient(GraphicsContext gc,
                                  double px, double py, double pw, double ph) {
        if (canvasData == null || lastTimex == null || lastBT == null || lastTimex.isEmpty()) return;
        int chargeIdx = canvasData.getChargeIndex();
        int dropIdx   = canvasData.getDropIndex();
        if (chargeIdx < 0 || dropIdx <= chargeIdx) return;
        double aucBaseTemp = displaySettings != null ? displaySettings.getAucBaseTemp() : 100.0;

        gc.beginPath();
        boolean started = false;
        for (int i = chargeIdx; i <= Math.min(dropIdx, lastTimex.size() - 1); i++) {
            double bT = i < lastBT.size() ? lastBT.get(i) : Double.NaN;
            if (!Double.isFinite(bT)) continue;
            double xPx = xAxis.getDisplayPosition(lastTimex.get(i)) + px;
            double yPx = yAxis.getDisplayPosition(bT)               + py;
            if (!started) { gc.moveTo(xPx, yPx); started = true; }
            else           gc.lineTo(xPx, yPx);
        }
        if (!started) return;
        double baseY  = yAxis.getDisplayPosition(aucBaseTemp) + py;
        int    safeD  = Math.min(dropIdx, lastTimex.size() - 1);
        gc.lineTo(xAxis.getDisplayPosition(lastTimex.get(safeD))     + px, baseY);
        gc.lineTo(xAxis.getDisplayPosition(lastTimex.get(chargeIdx)) + px, baseY);
        gc.closePath();
        Color aucC = colorConfig != null ? colorConfig.getCurveBT() : Color.web("#E05C47");
        LinearGradient grad = new LinearGradient(0, py, 0, py + ph, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(aucC.getRed(), aucC.getGreen(), aucC.getBlue(), 0.22)),
            new Stop(1, Color.color(aucC.getRed(), aucC.getGreen(), aucC.getBlue(), 0.02)));
        gc.setFill(grad);
        gc.fill();
    }

    // ═══════════════════════════════════════════════════
    // TIME GUIDE
    // ═══════════════════════════════════════════════════
    private void drawTimeGuide(GraphicsContext gc,
                                double px, double py, double pw, double ph) {
        if (displaySettings == null || displaySettings.getTimeguideSec() <= 0) return;
        double xPx = xAxis.getDisplayPosition(displaySettings.getTimeguideSec()) + px;
        if (xPx < px || xPx > px + pw) return;
        gc.setStroke(color("timeguide", Color.GRAY));
        gc.setLineWidth(1.5);
        gc.setLineDashes(8, 6);
        gc.strokeLine(xPx, py, xPx, py + ph - STRIP_H);
        gc.setLineDashes(null);
    }

    // ═══════════════════════════════════════════════════
    // EVENT MARKERS
    // ═══════════════════════════════════════════════════
    private void drawEventMarkers(GraphicsContext gc,
                                   double px, double py, double pw, double ph) {
        if (canvasData == null || lastTimex == null || lastTimex.isEmpty()) return;
        double lineH = ph - STRIP_H;

        drawVerticalMarker(gc, px, py, pw, lineH, canvasData.getChargeIndex(),  "CH",   Color.web("#555555", 0.80));
        drawVerticalMarker(gc, px, py, pw, lineH, canvasData.getDryEndIndex(),  "DE",   Color.web("#2980b9", 0.80));
        drawVerticalMarker(gc, px, py, pw, lineH, canvasData.getFcStartIndex(), "FC\u2191", Color.web("#e67e22", 0.90));
        drawVerticalMarker(gc, px, py, pw, lineH, canvasData.getFcEndIndex(),   "FC\u2193", Color.web("#e67e22", 0.70));
        drawVerticalMarker(gc, px, py, pw, lineH, canvasData.getScStartIndex(), "SC\u2191", Color.web("#c0392b", 0.80));
        drawVerticalMarker(gc, px, py, pw, lineH, canvasData.getScEndIndex(),   "SC\u2193", Color.web("#c0392b", 0.65));
        drawVerticalMarker(gc, px, py, pw, lineH, canvasData.getDropIndex(),    "DROP", Color.web("#c0392b", 0.95));

        // Special events bar (18 px above the phase strip)
        if (eventList == null) return;
        double barH = 18, barY = py + lineH - barH;
        Color boxC = color("specialeventbox", Color.web("#ff5871"));
        Color txtC = color("specialeventtext", Color.WHITE);
        gc.setFont(Font.font("System", 9));
        for (int i = 0; i < eventList.size(); i++) {
            EventEntry e = eventList.get(i);
            int idx = e.getTimeIndex();
            if (idx < 0 || idx >= lastTimex.size()) continue;
            double xPx = xAxis.getDisplayPosition(lastTimex.get(idx)) + px;
            if (xPx < px || xPx > px + pw) continue;
            gc.setFill(Color.color(boxC.getRed(), boxC.getGreen(), boxC.getBlue(), 0.85));
            gc.fillRoundRect(xPx - 9, barY + 1, 18, barH - 2, 3, 3);
            gc.setFill(txtC);
            gc.fillText(abbrev(e), xPx - 4, barY + barH - 5);
        }
        // Custom event callout annotations
        if (lastBT == null) return;
        gc.setFont(Font.font("System", 9));
        for (int i = 0; i < eventList.size(); i++) {
            EventEntry e = eventList.get(i);
            int idx = e.getTimeIndex();
            if (idx < 0 || idx >= lastTimex.size() || idx >= lastBT.size()) continue;
            if (e.getType() != EventType.CUSTOM
                    || e.getLabel() == null || e.getLabel().isBlank()) continue;
            double xPx = xAxis.getDisplayPosition(lastTimex.get(idx)) + px;
            double yPx = yAxis.getDisplayPosition(lastBT.get(idx))    + py;
            gc.setFill(Color.web("#ffffff", 0.80));
            gc.fillText(e.getLabel(), xPx + 4, yPx - 5);
        }
    }

    private void drawVerticalMarker(GraphicsContext gc,
                                     double px, double py, double pw, double lineH,
                                     int idx, String label, Color color) {
        if (idx < 0 || lastTimex == null || idx >= lastTimex.size()) return;
        double xPx = xAxis.getDisplayPosition(lastTimex.get(idx)) + px;
        if (xPx < px || xPx > px + pw) return;
        gc.setStroke(color);
        gc.setLineWidth(1.5);
        gc.setLineDashes(6, 4);
        gc.strokeLine(xPx, py, xPx, py + lineH - 22);
        gc.setLineDashes(null);
        // Pill label
        gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        double tw    = computeTextWidth(label, 10);
        double pillW = tw + 8, pillH = 14;
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.85));
        gc.fillRoundRect(xPx - pillW / 2, py + 2, pillW, pillH, 4, 4);
        gc.setFill(Color.WHITE);
        gc.fillText(label, xPx - tw / 2, py + 12);
    }

    // ═══════════════════════════════════════════════════
    // BACKGROUND PROFILE MARKERS
    // ═══════════════════════════════════════════════════
    private void drawBackgroundMarkers(GraphicsContext gc,
                                        double px, double py, double pw, double ph) {
        if (backgroundProfile == null || backgroundProfile.isEmpty()) return;
        ProfileData bgPd = backgroundProfile.getProfileData();
        if (bgPd == null) return;
        List<Integer> bgTi    = bgPd.getTimeindex();
        List<Double>  bgTimex = bgPd.getTimex();
        if (bgTi == null || bgTimex == null || bgTimex.isEmpty()) return;
        double offset = backgroundProfile.getAlignOffset();
        Color bgC = Color.web("#888888", 0.35);
        String[] labels = {"CH", "DE", "FC\u2191", "FC\u2193", "SC\u2191", "SC\u2193", "DR", "CMT"};
        gc.setFont(Font.font("System", 8));
        gc.setLineDashes(3, 7);
        gc.setLineWidth(1.0);
        gc.setStroke(bgC);
        for (int s = 0; s < Math.min(bgTi.size(), labels.length); s++) {
            Integer idx = bgTi.get(s);
            if (idx == null || idx <= 0 || idx >= bgTimex.size()) continue;
            double xPx = xAxis.getDisplayPosition(bgTimex.get(idx) + offset) + px;
            if (xPx < px || xPx > px + pw) continue;
            gc.strokeLine(xPx, py, xPx, py + ph - STRIP_H);
            gc.setFill(bgC);
            gc.fillText("bg:" + labels[s], xPx + 2, py + ph - STRIP_H - 4);
        }
        gc.setLineDashes(null);
    }

    // ═══════════════════════════════════════════════════
    // MET LINE
    // ═══════════════════════════════════════════════════
    private void drawMETLine(GraphicsContext gc,
                              double px, double py, double pw, double ph) {
        if (lastET == null || canvasData == null) return;
        int cIdx = canvasData.getChargeIndex();
        int dIdx = canvasData.getDropIndex();
        if (cIdx < 0 || dIdx <= cIdx) return;
        double met = MetCalculator.compute(lastET, cIdx, dIdx);
        if (!Double.isFinite(met)) return;
        double metY = yAxis.getDisplayPosition(met) + py;
        if (metY < py || metY > py + ph - STRIP_H) return;
        Color metC = color("metbox", Color.web("#aaaaaa", 0.7));
        gc.setStroke(metC);
        gc.setLineWidth(1.5);
        gc.setLineDashes(8, 5);
        gc.strokeLine(px, metY, px + pw, metY);
        gc.setLineDashes(null);
        gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        String lbl = String.format("MET %.1f\u00b0", met);
        double tw = computeTextWidth(lbl, 10);
        gc.setFill(Color.color(metC.getRed(), metC.getGreen(), metC.getBlue(), 0.75));
        gc.fillRoundRect(px + pw - tw - 12, metY - 10, tw + 10, 13, 3, 3);
        gc.setFill(Color.WHITE);
        gc.fillText(lbl, px + pw - tw - 7, metY - 1);
    }

    // ═══════════════════════════════════════════════════
    // TURNING POINT
    // ═══════════════════════════════════════════════════
    private void drawTurningPoint(GraphicsContext gc,
                                   double px, double py, double pw, double ph) {
        if (lastBT == null || canvasData == null || lastTimex == null) return;
        int cIdx   = canvasData.getChargeIndex();
        int dryIdx = canvasData.getDryEndIndex();
        int fcIdx  = canvasData.getFcStartIndex();
        int endIdx = dryIdx > 0 ? dryIdx : (fcIdx > 0 ? fcIdx : lastTimex.size() - 1);
        if (cIdx < 0 || endIdx <= cIdx) return;
        List<Double> rawBT = canvasData.getTemp2();
        if (rawBT == null) return;
        int tpIdx = RorCalculator.findTurningPoint(rawBT, cIdx, endIdx);
        if (tpIdx < 0 || tpIdx >= lastTimex.size() || tpIdx >= lastBT.size()) return;
        double tpSec  = lastTimex.get(tpIdx);
        double tpTemp = lastBT.get(tpIdx);
        if (!Double.isFinite(tpTemp)) return;
        double xPx = xAxis.getDisplayPosition(tpSec)  + px;
        double yPx = yAxis.getDisplayPosition(tpTemp) + py;
        if (xPx < px || xPx > px + pw || yPx < py || yPx > py + ph - STRIP_H) return;
        double s = 7;
        gc.setFill(Color.web("#60c8ff", 0.90));
        gc.beginPath();
        gc.moveTo(xPx,              yPx + s);
        gc.lineTo(xPx - s * 0.9,   yPx - s * 0.5);
        gc.lineTo(xPx + s * 0.9,   yPx - s * 0.5);
        gc.closePath();
        gc.fill();
        gc.setFont(Font.font("System", 9));
        gc.setFill(Color.web("#60c8ff", 0.90));
        int ts = (int) Math.round(tpSec);
        gc.fillText(String.format("TP %.1f\u00b0 %d:%02d", tpTemp, ts / 60, ts % 60),
            xPx + 8, yPx + 4);
    }

    // ═══════════════════════════════════════════════════
    // WATERMARK
    // ═══════════════════════════════════════════════════
    private void drawWatermark(GraphicsContext gc,
                                double px, double py, double pw, double ph) {
        if (displaySettings == null || !displaySettings.isShowWatermark()) return;
        if (roastTitle == null || roastTitle.isBlank()) return;
        gc.setFont(Font.font("System", FontWeight.BOLD, 32));
        gc.setFill(color("watermarks", Color.web("#ffffff", 0.05)));
        double tw = computeTextWidth(roastTitle, 32);
        gc.fillText(roastTitle, px + (pw - tw) / 2, py + (ph - STRIP_H) / 2);
    }

    // ═══════════════════════════════════════════════════
    // HIGHLIGHT LINE
    // ═══════════════════════════════════════════════════
    private void drawHighlightLine(GraphicsContext gc,
                                    double px, double py, double pw, double ph) {
        if (!Double.isFinite(highlightTimeSec)) return;
        double xPx = xAxis.getDisplayPosition(highlightTimeSec) + px;
        if (xPx < px || xPx > px + pw) return;
        gc.setStroke(Color.web("#5680E9", 0.90));
        gc.setLineWidth(2.5);
        gc.strokeLine(xPx, py, xPx, py + ph - STRIP_H);
    }

    // ═══════════════════════════════════════════════════
    // CROSSHAIR + PILL TOOLTIP
    // ═══════════════════════════════════════════════════
    private void drawCrosshair(GraphicsContext gc,
                                double px, double py, double pw, double ph) {
        if (!Double.isFinite(crosshairX) || lastTimex == null || lastTimex.isEmpty()) return;
        double relX = crosshairX - px;
        if (relX < 0 || relX > pw) return;

        double timeSec = xAxis.getValueForDisplay(relX);
        int    idx     = nearestIndex(lastTimex, timeSec);
        if (idx < 0) return;
        double snapTime  = lastTimex.get(idx);
        double snapXPx   = xAxis.getDisplayPosition(snapTime) + px;
        double btVal     = (lastBT    != null && idx < lastBT.size())    ? lastBT.get(idx)    : Double.NaN;
        double etVal     = (lastET    != null && idx < lastET.size())    ? lastET.get(idx)    : Double.NaN;
        double rorBTVal  = (lastRorBT != null && idx < lastRorBT.size()) ? lastRorBT.get(idx) : Double.NaN;

        if (onCursorMoved != null && Double.isFinite(btVal))
            onCursorMoved.accept(snapTime, btVal);

        double lineBottom = py + ph - STRIP_H;
        gc.setStroke(Color.web("#333333", 0.40));
        gc.setLineWidth(1.0);
        gc.setLineDashes(4, 4);
        gc.strokeLine(snapXPx, py, snapXPx, lineBottom);
        gc.setLineDashes(null);

        // BT snap dot
        if (Double.isFinite(btVal)) {
            double btYPx  = yAxis.getDisplayPosition(btVal) + py;
            Color  btColor = colorConfig != null ? colorConfig.getCurveBT() : Color.web("#E05C47");
            gc.setFill(btColor);
            gc.fillOval(snapXPx - 4, btYPx - 4, 8, 8);
            gc.setStroke(Color.web("#ffffff", 0.8));
            gc.setLineWidth(1.5);
            gc.strokeOval(snapXPx - 4, btYPx - 4, 8, 8);
        }

        // ET snap dot
        if (Double.isFinite(etVal)) {
            double etYPx  = yAxis.getDisplayPosition(etVal) + py;
            Color  etColor = colorConfig != null ? colorConfig.getCurveET() : Color.web("#4A90D9");
            gc.setFill(etColor);
            gc.fillOval(snapXPx - 3, etYPx - 3, 6, 6);
            gc.setStroke(Color.web("#ffffff", 0.7));
            gc.setLineWidth(1.0);
            gc.strokeOval(snapXPx - 3, etYPx - 3, 6, 6);
        }

        // Pill tooltip
        int    chargeSec = (canvasData != null && canvasData.getChargeIndex() >= 0
            && canvasData.getChargeIndex() < lastTimex.size())
            ? (int) Math.round(lastTimex.get(canvasData.getChargeIndex())) : 0;
        int    relSec  = (int) Math.round(snapTime) - chargeSec;
        String sign    = relSec < 0 ? "-" : "";
        int    abs     = Math.abs(relSec);
        String timeStr = String.format("%s%d:%02d", sign, abs / 60, abs % 60);
        String btStr   = Double.isFinite(btVal)    ? String.format("BT  %.1f\u00b0",   btVal)    : "";
        String etStr   = Double.isFinite(etVal)    ? String.format("ET  %.1f\u00b0",   etVal)    : "";
        String rorStr  = Double.isFinite(rorBTVal) ? String.format("RoR %.1f\u00b0/m", rorBTVal) : "";

        gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        double pad = 10, lineH = 15;
        String[] lines  = {timeStr, btStr, etStr, rorStr};
        double pillW = 0;
        for (String l : lines) if (!l.isEmpty()) pillW = Math.max(pillW, computeTextWidth(l, 11));
        pillW += pad * 2;
        int visLines = 0;
        for (String l : lines) if (!l.isEmpty()) visLines++;
        double pillH = visLines * lineH + pad;

        double tipX = snapXPx + 14;
        double tipY = py + 12;
        if (tipX + pillW > px + pw) tipX = snapXPx - pillW - 14;
        if (tipY + pillH > lineBottom - 4) tipY = lineBottom - pillH - 4;

        gc.setFill(Color.color(0.98, 0.98, 0.98, 0.95));
        gc.fillRoundRect(tipX, tipY, pillW, pillH, 8, 8);
        gc.setStroke(Color.web("#cccccc", 0.80));
        gc.setLineWidth(0.8);
        gc.strokeRoundRect(tipX, tipY, pillW, pillH, 8, 8);

        Color[] colors = {
            Color.web("#222222", 0.90),
            colorConfig != null ? colorConfig.getCurveBT()      : Color.web("#e74c3c"),
            colorConfig != null ? colorConfig.getCurveET()      : Color.web("#3498db"),
            colorConfig != null ? colorConfig.getCurveDeltaBT() : Color.web("#27ae60")
        };
        double ty = tipY + pad + lineH * 0.7;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) continue;
            gc.setFill(colors[i]);
            gc.fillText(lines[i], tipX + pad, ty);
            ty += lineH;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private Color color(String key, Color fallback) {
        if (colorConfig == null) return fallback;
        Color c = colorConfig.getPaletteColor(key);
        return c != null ? c : fallback;
    }

    private static int nearestIndex(List<Double> timex, double target) {
        if (timex == null || timex.isEmpty()) return -1;
        int best  = 0;
        double bestD = Math.abs(timex.get(0) - target);
        for (int i = 1; i < timex.size(); i++) {
            double d = Math.abs(timex.get(i) - target);
            if (d < bestD) { bestD = d; best = i; }
        }
        return best;
    }

    private static double computeTextWidth(String text, double fontSize) {
        Text t = new Text(text);
        t.setFont(Font.font("System", fontSize));
        return t.getLayoutBounds().getWidth();
    }

    private static String abbrev(EventEntry e) {
        if (e.getType() == EventType.CUSTOM
                && e.getLabel() != null && !e.getLabel().isEmpty()) {
            return e.getLabel().length() > 3
                ? e.getLabel().substring(0, 3)
                : e.getLabel();
        }
        return switch (e.getType()) {
            case CHARGE   -> "CH";
            case DRY_END  -> "DE";
            case FC_START -> "FC+";
            case FC_END   -> "FC-";
            case SC_START -> "SC+";
            case SC_END   -> "SC-";
            case DROP     -> "DR";
            case COOL_END -> "CMT";
            default       -> e.getType().name().substring(
                0, Math.min(3, e.getType().name().length()));
        };
    }
}
