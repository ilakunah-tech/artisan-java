package org.artisan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Time-value path with linear interpolation (roastpath module).
 * Represents a sequence of (x, y) points; value at any x is computed by linear interpolation
 * or extrapolation at the ends.
 */
public class RoastPath {

    private final List<Point> points;

    public RoastPath() {
        this.points = new ArrayList<>();
    }

    public RoastPath(List<Point> points) {
        this.points = new ArrayList<>(points != null ? points : Collections.emptyList());
    }

    /** Single (x, y) point. */
    public static final class Point {
        private final double x;
        private final double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    /** Returns an unmodifiable view of the points. */
    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    /** Adds a point (x, y). */
    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    /** Number of points. */
    public int size() {
        return points.size();
    }

    /**
     * Returns the interpolated (or extrapolated) value at x.
     * - Empty path: returns {@link Double#NaN}.
     * - Single point: returns that point's y for any x.
     * - x before first point: linear extrapolation from first segment.
     * - x after last point: linear extrapolation from last segment.
     * - x between points: linear interpolation.
     */
    public double getValueAt(double x) {
        if (points.isEmpty()) {
            return Double.NaN;
        }
        if (points.size() == 1) {
            return points.get(0).getY();
        }
        Point p0 = points.get(0);
        Point pLast = points.get(points.size() - 1);
        if (x <= p0.getX()) {
            Point p1 = points.get(1);
            return interpolate(p0.getX(), p0.getY(), p1.getX(), p1.getY(), x);
        }
        if (x >= pLast.getX()) {
            Point pPrev = points.get(points.size() - 2);
            return interpolate(pPrev.getX(), pPrev.getY(), pLast.getX(), pLast.getY(), x);
        }
        for (int i = 0; i < points.size() - 1; i++) {
            Point a = points.get(i);
            Point b = points.get(i + 1);
            if (x >= a.getX() && x <= b.getX()) {
                return interpolate(a.getX(), a.getY(), b.getX(), b.getY(), x);
            }
        }
        return Double.NaN;
    }

    private static double interpolate(double x0, double y0, double x1, double y1, double x) {
        if (x1 == x0) {
            return y0;
        }
        double t = (x - x0) / (x1 - x0);
        return y0 + t * (y1 - y0);
    }
}
