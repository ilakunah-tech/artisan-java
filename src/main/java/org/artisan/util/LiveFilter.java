package org.artisan.util;

/**
 * Base class for live (streaming) digital filters.
 * Ported from Python artisanlib.filters.LiveFilter.
 */
public abstract class LiveFilter {

    /**
     * Process one sample. NaNs are not filtered and are returned as-is.
     *
     * @param x input sample
     * @return filtered value, or x if x is NaN
     */
    public double process(double x) {
        if (Double.isNaN(x)) {
            return x;
        }
        return processImpl(x);
    }

    /**
     * Implementation of the filter. Override in subclasses.
     *
     * @param x input sample (guaranteed not NaN)
     * @return filtered value
     */
    protected abstract double processImpl(double x);
}
