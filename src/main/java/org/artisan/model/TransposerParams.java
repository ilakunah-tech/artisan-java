package org.artisan.model;

/**
 * Parameters for Transposer.apply(): time shift, temperature shift, and temperature scale.
 */
public final class TransposerParams {

    private final double timeShift;
    private final double tempShift;
    private final double tempScale;

    public TransposerParams(double timeShift, double tempShift, double tempScale) {
        this.timeShift = timeShift;
        this.tempShift = tempShift;
        this.tempScale = tempScale;
    }

    public double getTimeShift() { return timeShift; }
    public double getTempShift() { return tempShift; }
    public double getTempScale() { return tempScale; }
}
