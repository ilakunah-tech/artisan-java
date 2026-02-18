package org.artisan.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Live mean (moving average) low-pass filter with window size k.
 * Ported from Python artisanlib.filters.LiveMean.
 */
public final class LiveMean extends LiveFilter {

    private final int k;
    private final List<Double> initList;
    private double total;
    private boolean initialized;
    private Deque<Double> window;

    /**
     * @param k window size
     */
    public LiveMean(int k) {
        this.k = k;
        this.initList = new ArrayList<>();
        this.total = 0.0;
        this.initialized = false;
        this.window = null;
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
        double oldVal = window.removeFirst();
        total -= oldVal;
        total += x;
        window.addLast(x);
        return total / k;
    }

    private void initQueue() {
        window = new ArrayDeque<>(initList);
        initialized = true;
    }
}
