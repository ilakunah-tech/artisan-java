package org.artisan.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Live median low-pass filter (window size k, odd).
 * Ported from Python artisanlib.filters.LiveMedian.
 */
public final class LiveMedian extends LiveFilter {

    private final int k;
    private final List<Double> initList;
    private double total;
    private boolean initialized;
    private List<Double> sorted;
    private List<Double> window;
    private int midIdx;

    /**
     * @param k window size (must be odd)
     * @throws IllegalArgumentException if k is not odd
     */
    public LiveMedian(int k) {
        if (k % 2 != 1) {
            throw new IllegalArgumentException("Median filter length must be odd.");
        }
        this.k = k;
        this.initList = new ArrayList<>();
        this.total = 0.0;
        this.initialized = false;
        this.sorted = null;
        this.window = null;
        this.midIdx = 0;
    }

    @Override
    protected double processImpl(double x) {
        if (!initialized) {
            if (initList.size() < k) {
                initList.add(x);
                total += x;
                return total / initList.size();
            }
            initQueue();
        }
        double oldElem = window.get(0);
        window.remove(0);
        window.add(x);
        int removeIdx = Collections.binarySearch(sorted, oldElem);
        if (removeIdx >= 0) {
            sorted.remove(removeIdx);
        }
        int insertIdx = Collections.binarySearch(sorted, x);
        if (insertIdx < 0) {
            insertIdx = -(insertIdx + 1);
        }
        sorted.add(insertIdx, x);
        return sorted.get(midIdx);
    }

    private void initQueue() {
        window = new ArrayList<>(initList);
        sorted = new ArrayList<>(window);
        Collections.sort(sorted);
        midIdx = (window.size() - 1) / 2;
        initialized = true;
    }
}
