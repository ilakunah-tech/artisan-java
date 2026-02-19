package org.artisan.model;

/**
 * Mutable point on a designer profile curve.
 */
public final class DesignerPoint implements Comparable<DesignerPoint> {

    private double timeSec;
    private double temp;

    public DesignerPoint(double timeSec, double temp) {
        this.timeSec = timeSec;
        this.temp = temp;
    }

    public double getTimeSec() {
        return timeSec;
    }

    public void setTimeSec(double timeSec) {
        this.timeSec = timeSec;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    @Override
    public int compareTo(DesignerPoint o) {
        if (o == null) return 1;
        return Double.compare(this.timeSec, o.timeSec);
    }
}

