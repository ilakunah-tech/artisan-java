package org.artisan.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import org.artisan.model.CupProfile;

/**
 * Canvas-based spider/radar chart for CupProfile scores.
 * 10 axes, scale 0–10, filled polygon, concentric grid circles.
 */
public final class FlavorWheelView extends StackPane {

    private static final int MIN_SIZE = 300;
    private static final int PREF_SIZE = 320;
    private static final int N_AXES = 10;
    private static final double MAX_SCORE = 10.0;
    private static final double[] GRID_LEVELS = { 2.0, 4.0, 6.0, 8.0, 10.0 };

    private final Canvas canvas;
    private CupProfile cupProfile;

    public FlavorWheelView() {
        canvas = new Canvas(PREF_SIZE, PREF_SIZE);
        canvas.widthProperty().addListener((a, b, c) -> draw());
        canvas.heightProperty().addListener((a, b, c) -> draw());
        getChildren().add(canvas);
        setMinSize(MIN_SIZE, MIN_SIZE);
        setPrefSize(PREF_SIZE, PREF_SIZE);
    }

    public void setCupProfile(CupProfile profile) {
        this.cupProfile = profile;
        draw();
    }

    public CupProfile getCupProfile() {
        return cupProfile;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = snapSize(getWidth());
        double h = snapSize(getHeight());
        if (w != canvas.getWidth() || h != canvas.getHeight()) {
            canvas.setWidth(w);
            canvas.setHeight(h);
        }
        draw();
    }

    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        double cx = w / 2.0;
        double cy = h / 2.0;
        double radius = Math.min(w, h) / 2.0 - 40;

        // Background concentric circles at 2, 4, 6, 8, 10
        gc.setStroke(Color.gray(0.85));
        gc.setLineWidth(0.5);
        for (double level : GRID_LEVELS) {
            double r = radius * (level / MAX_SCORE);
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        }

        // Axes and labels
        String[] labels = CupProfile.DEFAULT_ATTRIBUTES;
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.gray(0.7));
        gc.setLineWidth(1.0);

        for (int i = 0; i < N_AXES; i++) {
            double angleRad = Math.toRadians(i * 360.0 / N_AXES - 90);
            double ax = cx + radius * Math.cos(angleRad);
            double ay = cy + radius * Math.sin(angleRad);
            gc.strokeLine(cx, cy, ax, ay);

            // Label: place outside the circle
            double labelRadius = radius + 18;
            double lx = cx + labelRadius * Math.cos(angleRad);
            double ly = cy + labelRadius * Math.sin(angleRad);
            String text = labels[i];
            gc.setFont(javafx.scene.text.Font.font(10));
            gc.setTextAlign(angleRad >= -Math.PI / 2 && angleRad <= Math.PI / 2 ? javafx.scene.text.TextAlignment.CENTER : javafx.scene.text.TextAlignment.CENTER);
            gc.fillText(text, lx, ly);
        }

        // Filled polygon from scores
        if (cupProfile != null) {
            double[] xs = new double[N_AXES];
            double[] ys = new double[N_AXES];
            for (int i = 0; i < N_AXES; i++) {
                String attr = labels[i];
                double score = cupProfile.getScores().getOrDefault(attr, 0.0);
                if (score < 0) score = 0;
                if (score > MAX_SCORE) score = MAX_SCORE;
                double r = radius * (score / MAX_SCORE);
                double angleRad = Math.toRadians(i * 360.0 / N_AXES - 90);
                xs[i] = cx + r * Math.cos(angleRad);
                ys[i] = cy + r * Math.sin(angleRad);
            }
            gc.setFill(Color.rgb(70, 130, 180, 0.35));
            gc.setStroke(Color.rgb(70, 130, 180, 0.9));
            gc.setLineWidth(1.5);
            gc.fillPolygon(xs, ys, N_AXES);
            gc.strokePolygon(xs, ys, N_AXES);
        }
    }

    /** Export as image for PDF/print. */
    public WritableImage toImage() {
        int w = (int) Math.ceil(canvas.getWidth());
        int h = (int) Math.ceil(canvas.getHeight());
        if (w <= 0 || h <= 0) return null;
        WritableImage img = new WritableImage(w, h);
        canvas.snapshot(null, img);
        return img;
    }
}
