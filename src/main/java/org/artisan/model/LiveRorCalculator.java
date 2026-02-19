package org.artisan.model;

/**
 * Live (streaming) Rate of Rise calculator for real-time recording.
 * Keeps a circular buffer of the last (smoothingWindow + 1) time+temp samples
 * and returns current RoR using the same sliding-window formula as {@link RorCalculator}.
 */
public final class LiveRorCalculator {

    private final int window;
    private final double[] timeSec;
    private final double[] tempC;
    private int writePos;
    private int count;

    /**
     * @param smoothingWindow number of samples in the delta span (≥ 1); uses (smoothingWindow + 1) points per RoR
     */
    public LiveRorCalculator(int smoothingWindow) {
        this.window = Math.max(1, smoothingWindow);
        int size = this.window + 1;
        this.timeSec = new double[size];
        this.tempC = new double[size];
        this.writePos = 0;
        this.count = 0;
    }

    /**
     * Adds a sample and returns the current RoR (degrees/min), or 0.0 if not enough samples yet.
     * Formula: RoR = (tempNewest - tempOldest) / (timeNewest - timeOldest) * 60.
     *
     * @param timeSec time in seconds
     * @param tempC   temperature in Celsius
     * @return current RoR or 0.0
     */
    public double addSample(double timeSec, double tempC) {
        this.timeSec[writePos] = timeSec;
        this.tempC[writePos] = tempC;
        writePos = (writePos + 1) % (window + 1);
        if (count < window + 1) {
            count++;
        }
        if (count < window + 1) {
            return 0.0;
        }
        int oldestIdx = writePos;
        int newestIdx = (writePos + window) % (window + 1);
        double t0 = this.timeSec[oldestIdx];
        double t1 = this.timeSec[newestIdx];
        double temp0 = this.tempC[oldestIdx];
        double temp1 = this.tempC[newestIdx];
        double dtSec = t1 - t0;
        if (dtSec <= 0 || !Double.isFinite(temp0) || !Double.isFinite(temp1)) {
            return 0.0;
        }
        return (temp1 - temp0) / dtSec * 60.0;
    }

    /** Clears the buffer; next addSample will start filling again. */
    public void reset() {
        writePos = 0;
        count = 0;
    }
}
