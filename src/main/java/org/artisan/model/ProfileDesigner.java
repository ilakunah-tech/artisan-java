package org.artisan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Editable BT/ET profile built from control points, with monotone cubic spline interpolation
 * (Fritsch-Carlson) to generate smooth arrays at a fixed sampling interval.
 */
public final class ProfileDesigner {

    private final List<DesignerPoint> btPoints = new ArrayList<>();
    private final List<DesignerPoint> etPoints = new ArrayList<>();
    private double samplingInterval = 1.0;

    public List<DesignerPoint> getBtPoints() {
        return btPoints;
    }

    public List<DesignerPoint> getEtPoints() {
        return etPoints;
    }

    public double getSamplingInterval() {
        return samplingInterval;
    }

    public void setSamplingInterval(double samplingInterval) {
        if (!Double.isFinite(samplingInterval) || samplingInterval <= 0) return;
        this.samplingInterval = samplingInterval;
    }

    public void addBtPoint(double t, double temp) {
        btPoints.add(new DesignerPoint(t, temp));
        Collections.sort(btPoints);
    }

    public void removeBtPoint(int index) {
        if (index < 0 || index >= btPoints.size()) return;
        btPoints.remove(index);
    }

    public void moveBtPoint(int index, double newT, double newTemp) {
        if (index < 0 || index >= btPoints.size()) return;
        DesignerPoint p = btPoints.get(index);
        p.setTimeSec(newT);
        p.setTemp(newTemp);
        Collections.sort(btPoints);
    }

    public void addEtPoint(double t, double temp) {
        etPoints.add(new DesignerPoint(t, temp));
        Collections.sort(etPoints);
    }

    public void removeEtPoint(int index) {
        if (index < 0 || index >= etPoints.size()) return;
        etPoints.remove(index);
    }

    public void moveEtPoint(int index, double newT, double newTemp) {
        if (index < 0 || index >= etPoints.size()) return;
        DesignerPoint p = etPoints.get(index);
        p.setTimeSec(newT);
        p.setTemp(newTemp);
        Collections.sort(etPoints);
    }

    public void clear() {
        btPoints.clear();
        etPoints.clear();
    }

    public void loadDefaults() {
        clear();
        addBtPoint(0, 200);
        addBtPoint(90, 170);
        addBtPoint(270, 195);
        addBtPoint(480, 210);
        addBtPoint(600, 215);

        addEtPoint(0, 220);
        addEtPoint(90, 190);
        addEtPoint(270, 215);
        addEtPoint(480, 230);
        addEtPoint(600, 235);
    }

    /**
     * Generates a ProfileData using monotone cubic spline interpolation.
     * If ET points are empty, ET is synthesized as BT + 20°C.
     */
    public ProfileData generate() {
        if (btPoints.size() < 2) return null;
        List<DesignerPoint> bt = new ArrayList<>(btPoints);
        Collections.sort(bt);
        List<DesignerPoint> et = new ArrayList<>(etPoints);
        Collections.sort(et);

        double t0 = bt.get(0).getTimeSec();
        double t1 = bt.get(bt.size() - 1).getTimeSec();
        if (!Double.isFinite(t0) || !Double.isFinite(t1) || t1 <= t0) return null;
        double dt = samplingInterval > 0 ? samplingInterval : 1.0;

        int nSamples = Math.max(2, (int) Math.round((t1 - t0) / dt) + 1);
        List<Double> timex = new ArrayList<>(nSamples);
        List<Double> btArr = new ArrayList<>(nSamples);
        List<Double> etArr = new ArrayList<>(nSamples);

        Spline btSpline = buildSpline(bt);
        Spline etSpline = et.size() >= 2 ? buildSpline(et) : null;

        for (int i = 0; i < nSamples; i++) {
            double t = t0 + i * dt;
            if (i == nSamples - 1) t = t1; // ensure last point hits the endpoint
            timex.add(t);
            double btv = btSpline.eval(t);
            btArr.add(btv);
            double etv;
            if (etSpline != null) {
                etv = etSpline.eval(t);
            } else {
                etv = btv + 20.0;
            }
            etArr.add(etv);
        }

        ProfileData pd = new ProfileData();
        pd.setTimex(timex);
        pd.setTemp2(btArr);
        pd.setTemp1(etArr);
        pd.setSamplingInterval(dt);
        return pd;
    }

    private static Spline buildSpline(List<DesignerPoint> pts) {
        int n = pts.size();
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = pts.get(i).getTimeSec();
            y[i] = pts.get(i).getTemp();
        }
        double[] m = computeSlopes(x, y);
        return new Spline(x, y, m);
    }

    /**
     * Fritsch-Carlson monotone cubic spline slopes.
     */
    private static double[] computeSlopes(double[] x, double[] y) {
        int n = x.length;
        double[] m = new double[n];
        if (n == 2) {
            double h = x[1] - x[0];
            double d = h != 0 ? (y[1] - y[0]) / h : 0.0;
            m[0] = d;
            m[1] = d;
            return m;
        }

        double[] h = new double[n - 1];
        double[] d = new double[n - 1];
        for (int i = 0; i < n - 1; i++) {
            h[i] = x[i + 1] - x[i];
            if (h[i] <= 0) h[i] = 1e-9; // avoid division by zero; caller should keep x sorted
            d[i] = (y[i + 1] - y[i]) / h[i];
        }

        m[0] = d[0];
        m[n - 1] = d[n - 2];

        for (int i = 1; i < n - 1; i++) {
            if (d[i - 1] == 0.0 || d[i] == 0.0 || Math.signum(d[i - 1]) != Math.signum(d[i])) {
                m[i] = 0.0;
            } else {
                double w1 = 2 * h[i] + h[i - 1];
                double w2 = h[i] + 2 * h[i - 1];
                m[i] = (w1 + w2) / (w1 / d[i - 1] + w2 / d[i]);
            }
        }

        // Additional monotonicity constraint (Fritsch-Carlson)
        for (int i = 0; i < n - 1; i++) {
            if (d[i] == 0.0) {
                m[i] = 0.0;
                m[i + 1] = 0.0;
                continue;
            }
            double a = m[i] / d[i];
            double b = m[i + 1] / d[i];
            double sum = a * a + b * b;
            if (sum > 9.0) {
                double tau = 3.0 / Math.sqrt(sum);
                m[i] = tau * a * d[i];
                m[i + 1] = tau * b * d[i];
            }
        }
        return m;
    }

    private static double hermite(double x0, double x1, double y0, double y1, double m0, double m1, double x) {
        double h = x1 - x0;
        if (h <= 0) return y0;
        double t = (x - x0) / h;
        if (t <= 0) return y0;
        if (t >= 1) return y1;
        double t2 = t * t;
        double t3 = t2 * t;
        double h00 = 2 * t3 - 3 * t2 + 1;
        double h10 = t3 - 2 * t2 + t;
        double h01 = -2 * t3 + 3 * t2;
        double h11 = t3 - t2;
        return h00 * y0 + h10 * h * m0 + h01 * y1 + h11 * h * m1;
    }

    private record Spline(double[] x, double[] y, double[] m) {
        double eval(double xi) {
            int n = x.length;
            if (xi <= x[0]) return y[0];
            if (xi >= x[n - 1]) return y[n - 1];
            int i = 0;
            // Linear scan is fine for small point counts; designer uses a handful of control points.
            while (i + 1 < n && xi > x[i + 1]) i++;
            return hermite(x[i], x[i + 1], y[i], y[i + 1], m[i], m[i + 1], xi);
        }
    }
}

