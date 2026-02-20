package org.artisan.view;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import org.artisan.model.PhaseResult;


/**
 * Horizontal stacked bar of roast phases (Drying, Maillard, Development) with
 * percentage labels and clickable event markers (Charge, Dry End, FC Start, Drop).
 */
public final class PhasesCanvasPanel extends StackPane {

    public static final String MARKER_CHARGE = "CHARGE";
    public static final String MARKER_DRY_END = "DRY_END";
    public static final String MARKER_FC_START = "FC_START";
    public static final String MARKER_DROP = "DROP";

    private static final double HEIGHT_PX = 24.0;
    private static final double MARKER_HIT_WIDTH = 8.0;
    /* Pastel fills with darker tints for better label contrast */
    private static final String DRYING_COLOR = "#4A6BA8";
    private static final String MAILLARD_COLOR = "#3A7A9A";
    private static final String DEV_COLOR = "#6A50B0";
    private static final String LABEL_TEXT_COLOR = "#FFFFFF";
    private static final double MIN_SEGMENT_WIDTH_FOR_LABEL = 50.0;
    private static final String FONT_FAMILY = "System";
    private static final double FONT_SIZE = 10.0;

    private final Canvas canvas;
    private final Pane overlay;
    private PhaseResult result;
    private java.util.function.BiConsumer<String, Double> onMarkerClick;

    public PhasesCanvasPanel(PhaseResult result) {
        this.result = result != null ? result : PhaseResult.INVALID;
        canvas = new Canvas();
        canvas.setHeight(HEIGHT_PX);
        canvas.widthProperty().bind(widthProperty());
        overlay = new Pane();
        overlay.setMouseTransparent(true);
        overlay.setPickOnBounds(false);
        getChildren().addAll(canvas, overlay);
        setMinHeight(HEIGHT_PX);
        setMaxHeight(HEIGHT_PX);
        setPrefHeight(HEIGHT_PX);
        widthProperty().addListener((o, oldVal, newVal) -> {
            double w = newVal.doubleValue();
            paintComponent(canvas.getGraphicsContext2D(), w, HEIGHT_PX);
            updateMarkerOverlay(w, HEIGHT_PX);
        });
        paintComponent(canvas.getGraphicsContext2D(), getWidth(), HEIGHT_PX);
    }

    public void setOnMarkerClick(java.util.function.BiConsumer<String, Double> callback) {
        this.onMarkerClick = callback;
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
            updateMarkerOverlay(w, HEIGHT_PX);
        });
    }

    private void updateMarkerOverlay(double width, double height) {
        overlay.getChildren().clear();
        overlay.setMouseTransparent(true);
        if (result.isInvalid() || result.getTotalTimeSec() <= 0 || onMarkerClick == null) return;
        double total = result.getTotalTimeSec();
        double drying = result.getDryingTimeSec();
        double maillard = result.getMaillardTimeSec();
        double t0 = 0;
        double t1 = drying;
        double t2 = drying + maillard;
        double t3 = total;
        double x0 = 0;
        double x1 = width * (drying / total);
        double x2 = width * ((drying + maillard) / total);
        double x3 = width;
        addMarker(x0, height, MARKER_CHARGE, t0);
        addMarker(x1, height, MARKER_DRY_END, t1);
        addMarker(x2, height, MARKER_FC_START, t2);
        addMarker(x3, height, MARKER_DROP, t3);
    }

    private void addMarker(double centerX, double height, String id, double timeSec) {
        Rectangle r = new Rectangle(MARKER_HIT_WIDTH, height);
        r.setLayoutX(centerX - MARKER_HIT_WIDTH / 2);
        r.setLayoutY(0);
        r.setFill(javafx.scene.paint.Color.TRANSPARENT);
        r.setCursor(javafx.scene.Cursor.HAND);
        Tooltip.install(r, new Tooltip(id.replace("_", " ") + " @ " + (int)(timeSec / 60) + ":" + String.format("%02d", (int)(timeSec % 60))));
        double t = timeSec;
        r.setOnMouseClicked(e -> { if (onMarkerClick != null) onMarkerClick.accept(id, t); });
        overlay.getChildren().add(r);
        overlay.setMouseTransparent(false);
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

        if (dryingFrac > 0) {
            double segW = width * dryingFrac;
            gc.setFill(Color.web(DRYING_COLOR));
            gc.fillRect(x, 0, segW, height);
            if (segW >= MIN_SEGMENT_WIDTH_FOR_LABEL) {
                gc.setFill(Color.web(LABEL_TEXT_COLOR));
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
                gc.setFill(Color.web(LABEL_TEXT_COLOR));
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
                gc.setFill(Color.web(LABEL_TEXT_COLOR));
                int pct = (int) Math.round(result.getDevelopmentPercent());
                gc.fillText("Dev " + pct + "%", x + segW / 2, height / 2 + 3);
            }
        }
    }
}
