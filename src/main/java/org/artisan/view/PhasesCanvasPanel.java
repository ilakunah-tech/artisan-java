package org.artisan.view;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import org.artisan.model.PhaseResult;

/**
 * JavaFX Canvas-based panel showing a horizontal stacked bar of roast phases
 * (Drying, Maillard, Development) with percentage labels.
 */
public final class PhasesCanvasPanel extends Pane {

    private static final double HEIGHT_PX = 24.0;
    /* Pastel fills: ~14% opacity of brand colors (Primary #5680E9, Primary alt #5AB9EA, Violet #8860D0) */
    private static final String DRYING_COLOR = "#245680E9";
    private static final String MAILLARD_COLOR = "#245AB9EA";
    private static final String DEV_COLOR = "#248860D0";
    private static final double MIN_SEGMENT_WIDTH_FOR_LABEL = 50.0;
    private static final String FONT_FAMILY = "System";
    private static final double FONT_SIZE = 10.0;

    private final Canvas canvas;
    private PhaseResult result;

    public PhasesCanvasPanel(PhaseResult result) {
        this.result = result != null ? result : PhaseResult.INVALID;
        canvas = new Canvas();
        canvas.setHeight(HEIGHT_PX);
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        setMinHeight(HEIGHT_PX);
        setMaxHeight(HEIGHT_PX);
        setPrefHeight(HEIGHT_PX);
        widthProperty().addListener((o, oldVal, newVal) -> {
            double w = newVal.doubleValue();
            paintComponent(canvas.getGraphicsContext2D(), w, HEIGHT_PX);
        });
        paintComponent(canvas.getGraphicsContext2D(), getWidth(), HEIGHT_PX);
    }

    /**
     * Updates the stored phase result and re-renders the canvas.
     * Must run on the JavaFX application thread.
     */
    public void refresh(PhaseResult result) {
        PhaseResult r = result != null ? result : PhaseResult.INVALID;
        Platform.runLater(() -> {
            this.result = r;
            double w = getWidth() > 0 ? getWidth() : canvas.getWidth();
            paintComponent(canvas.getGraphicsContext2D(), w, HEIGHT_PX);
        });
    }

    /**
     * Returns the Canvas node for embedding in layouts.
     */
    public Canvas getPhasesCanvas() {
        return canvas;
    }

    private void paintComponent(GraphicsContext gc, double width, double height) {
        if (width <= 0 || height <= 0) return;
        gc.clearRect(0, 0, width, height);

        if (result.isInvalid() || result.getTotalTimeSec() <= 0) {
            gc.setFill(Color.web("#EEF2FA"));
            gc.fillRect(0, 0, width, height);
            gc.setFill(Color.web("#6B7280"));
            gc.setFont(Font.font(FONT_FAMILY, FONT_SIZE));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No phase data", width / 2, height / 2 + 3);
            return;
        }

        double total = result.getTotalTimeSec();
        double drying = result.getDryingTimeSec();
        double maillard = result.getMaillardTimeSec();
        double dev = result.getDevelopmentTimeSec();
        double dryingFrac = total > 0 ? drying / total : 0;
        double maillardFrac = total > 0 ? maillard / total : 0;
        double devFrac = total > 0 ? dev / total : 0;

        double x = 0;
        gc.setFont(Font.font(FONT_FAMILY, FONT_SIZE));
        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(Color.web("#6B7280"));
        gc.setFont(Font.font(FONT_FAMILY, FONT_SIZE));
        gc.setTextAlign(TextAlignment.CENTER);
        if (dryingFrac > 0) {
            double segW = width * dryingFrac;
            gc.setFill(Color.web(DRYING_COLOR));
            gc.fillRect(x, 0, segW, height);
            if (segW >= MIN_SEGMENT_WIDTH_FOR_LABEL) {
                int pct = (int) Math.round(result.getDryingPercent());
                gc.fillText("Drying " + pct + "%", x + segW / 2, height / 2 + 3);
            }
            x += segW;
        }
        if (maillardFrac > 0) {
            double segW = width * maillardFrac;
            gc.setFill(Color.web(MAILLARD_COLOR));
            gc.fillRect(x, 0, segW, height);
            if (segW >= MIN_SEGMENT_WIDTH_FOR_LABEL) {
                int pct = (int) Math.round(result.getMaillardPercent());
                gc.fillText("Maillard " + pct + "%", x + segW / 2, height / 2 + 3);
            }
            x += segW;
        }
        if (devFrac > 0) {
            double segW = width * devFrac;
            gc.setFill(Color.web(DEV_COLOR));
            gc.fillRect(x, 0, segW, height);
            if (segW >= MIN_SEGMENT_WIDTH_FOR_LABEL) {
                int pct = (int) Math.round(result.getDevelopmentPercent());
                gc.fillText("Dev " + pct + "%", x + segW / 2, height / 2 + 3);
            }
        }
    }
}
