package org.artisan.ui.components;

import io.fair_acc.chartfx.XYChart;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.artisan.model.ReferenceProfile;

/**
 * Phase strip rendered as a Canvas overlay positioned directly above the chart's
 * plot-background area using scene-to-local coordinate transforms.
 *
 * The canvas is added to the same parent AnchorPane that holds the chart (not
 * inside the chart node), which avoids the fair-acc ".chart-content" lookup issue.
 * Its position and size are kept in sync via a layoutBounds listener on the chart.
 */
public final class PhaseStripOverlay extends Canvas {

    private static final double STRIP_H = 28.0;

    private double dryPx = 0;
    private double malPx = 0;
    private double devPx = 0;

    private final XYChart chart;
    private final Pane    parentPane;

    private ReferenceProfile referenceProfile;

    /**
     * @param chart      The fair-acc XYChart instance (may be null for tests)
     * @param parentPane The AnchorPane that contains the chart view
     */
    public PhaseStripOverlay(XYChart chart, Pane parentPane) {
        this.chart      = chart;
        this.parentPane = parentPane;

        setMouseTransparent(true);
        setWidth(0);
        setHeight(STRIP_H);

        if (chart != null && parentPane != null) {
            // Re-position whenever the chart is laid out (resize, zoom, etc.)
            chart.layoutBoundsProperty().addListener(
                (obs, o, n) -> Platform.runLater(this::repositionCanvas));

            // Also re-position when parentPane itself resizes
            parentPane.layoutBoundsProperty().addListener(
                (obs, o, n) -> Platform.runLater(this::repositionCanvas));

            parentPane.getChildren().add(this);
            toFront();
        }
    }

    // ── Coordinate transform: plot-background → parentPane space ──────────────

    private void repositionCanvas() {
        if (chart == null || parentPane == null) return;

        Node plotBg = chart.lookup(".chart-plot-background");
        if (plotBg == null) return;

        Bounds local    = plotBg.getBoundsInLocal();
        Bounds inScene  = plotBg.localToScene(local);
        if (inScene == null || parentPane.getScene() == null) return;
        Bounds inParent = parentPane.sceneToLocal(inScene);
        if (inParent == null) return;

        double plotX = inParent.getMinX();
        double plotY = inParent.getMinY();
        double plotW = inParent.getWidth();
        double plotH = inParent.getHeight();

        if (plotW <= 0 || plotH <= 0) return;

        setLayoutX(plotX);
        setLayoutY(plotY + plotH - STRIP_H);
        setWidth(plotW);
        setHeight(STRIP_H);

        redraw(plotW);
        toFront(); // stay on top after any child list changes
    }

    // ── Public update API ──────────────────────────────────────────────────────

    /**
     * Called every sampling tick to advance the phase strip.
     *
     * @param elapsedSec  current roast elapsed time (seconds from charge)
     * @param xAxisMin    current X axis minimum value (seconds)
     * @param xAxisMax    current X axis maximum value (seconds)
     */
    public void update(double elapsedSec, double xAxisMin, double xAxisMax) {
        if (referenceProfile == null || chart == null || parentPane == null) return;

        Node plotBg = chart.lookup(".chart-plot-background");
        if (plotBg == null) return;

        Bounds local    = plotBg.getBoundsInLocal();
        Bounds inScene  = plotBg.localToScene(local);
        if (inScene == null || parentPane.getScene() == null) return;
        Bounds inParent = parentPane.sceneToLocal(inScene);
        if (inParent == null) return;

        double plotW = inParent.getWidth();
        double range = xAxisMax - xAxisMin;
        if (plotW <= 0 || range <= 0) return;

        double pxPerSec = plotW / range;
        double dryEnd   = referenceProfile.getDryEndTimeSec();
        double fcStart  = referenceProfile.getFcStartTimeSec();
        double current  = Math.max(0, Math.min(elapsedSec, xAxisMax));

        if (current <= 0) {
            dryPx = malPx = devPx = 0;
        } else if (current <= dryEnd) {
            dryPx = (current - xAxisMin) * pxPerSec;
            malPx = 0;
            devPx = 0;
        } else if (current <= fcStart) {
            dryPx = (dryEnd  - xAxisMin) * pxPerSec;
            malPx = (current - xAxisMin) * pxPerSec;
            devPx = 0;
        } else {
            dryPx = (dryEnd  - xAxisMin) * pxPerSec;
            malPx = (fcStart - xAxisMin) * pxPerSec;
            devPx = (current - xAxisMin) * pxPerSec;
        }

        repositionCanvas();
    }

    // ── Canvas drawing ─────────────────────────────────────────────────────────

    private void redraw(double plotW) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, plotW, STRIP_H);

        if (dryPx <= 0) return;

        Font labelFont = Font.font("Segoe UI", FontWeight.BOLD, 11);

        // ── DRY (amber) ──────────────────────────────────────────────────────
        double dW = Math.min(dryPx, plotW);
        gc.setFill(Color.rgb(249, 168, 37, 0.85));
        gc.fillRect(0, 0, dW, STRIP_H);
        if (dW > 55) {
            gc.setFill(Color.rgb(30, 30, 30, 0.90));
            gc.setFont(labelFont);
            gc.fillText("DRY", 5, STRIP_H - 8);
        }

        // ── MAILLARD (dark brown) ─────────────────────────────────────────────
        if (malPx > dryPx) {
            double mW = Math.min(malPx, plotW) - dryPx;
            if (mW > 0) {
                gc.setFill(Color.rgb(78, 38, 10, 0.85));
                gc.fillRect(dryPx, 0, mW, STRIP_H);
                if (mW > 75) {
                    gc.setFill(Color.WHITE);
                    gc.setFont(labelFont);
                    gc.fillText("MAILLARD", dryPx + 5, STRIP_H - 8);
                }
            }
        }

        // ── DT / DEVELOPMENT (green) ──────────────────────────────────────────
        if (devPx > malPx) {
            double dvW = Math.min(devPx, plotW) - malPx;
            if (dvW > 0) {
                gc.setFill(Color.rgb(46, 125, 50, 0.85));
                gc.fillRect(malPx, 0, dvW, STRIP_H);
                if (dvW > 38) {
                    gc.setFill(Color.WHITE);
                    gc.setFont(labelFont);
                    gc.fillText("DT", malPx + 5, STRIP_H - 8);
                }
            }
        }
    }

    // ── Public accessors ──────────────────────────────────────────────────────

    public void setReferenceProfile(ReferenceProfile rp) {
        this.referenceProfile = rp;
        reset();
    }

    public void reset() {
        dryPx = 0;
        malPx = 0;
        devPx = 0;
        double w = getWidth();
        if (w > 0) {
            getGraphicsContext2D().clearRect(0, 0, w, STRIP_H);
        }
    }
}
