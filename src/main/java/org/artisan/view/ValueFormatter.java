package org.artisan.view;

/**
 * Utility for formatting numeric values for LCD-style displays.
 * Used by LCDPanel and tests.
 */
public final class ValueFormatter {

    private static final String NAN_PLACEHOLDER = "–––";

    private ValueFormatter() {}

    /**
     * Formats a value to the given decimal places, or "–––" for NaN/Infinite.
     *
     * @param value   the value to format
     * @param decimals number of decimal places
     * @return formatted string, or "–––" if value is not finite
     */
    public static String format(double value, int decimals) {
        if (!Double.isFinite(value)) {
            return NAN_PLACEHOLDER;
        }
        String fmt = "%." + Math.max(0, decimals) + "f";
        return String.format(java.util.Locale.ROOT, fmt, value);
    }
}
