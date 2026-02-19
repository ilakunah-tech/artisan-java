package org.artisan.util;

import javafx.scene.paint.Color;

/**
 * Maps roast color value (Agtron/Tonino 0–100) to JavaFX Color.
 */
public final class ColorTrack {

    private ColorTrack() {}

    /** Very dark (0). */
    private static final String HEX_0 = "#1A0A00";
    /** Dark brown (25). */
    private static final String HEX_25 = "#5C2A0D";
    /** Medium brown (50). */
    private static final String HEX_50 = "#9B5E23";
    /** Cinnamon (75). */
    private static final String HEX_75 = "#C4864A";
    /** Light tan (100). */
    private static final String HEX_100 = "#E8C49A";

    /**
     * Maps Agtron value 0..100 to a Color (dark brown → light tan).
     * Values outside [0,100] are clamped.
     */
    public static Color fromAgtron(double agtronValue) {
        double v = clamp(agtronValue, 0, 100);
        if (v <= 0) return Color.web(HEX_0);
        if (v >= 100) return Color.web(HEX_100);
        if (v <= 25) {
            double t = v / 25.0;
            return interpolate(Color.web(HEX_0), Color.web(HEX_25), t);
        }
        if (v <= 50) {
            double t = (v - 25) / 25.0;
            return interpolate(Color.web(HEX_25), Color.web(HEX_50), t);
        }
        if (v <= 75) {
            double t = (v - 50) / 25.0;
            return interpolate(Color.web(HEX_50), Color.web(HEX_75), t);
        }
        double t = (v - 75) / 25.0;
        return interpolate(Color.web(HEX_75), Color.web(HEX_100), t);
    }

    private static Color interpolate(Color a, Color b, double t) {
        return Color.color(
            a.getRed() + (b.getRed() - a.getRed()) * t,
            a.getGreen() + (b.getGreen() - a.getGreen()) * t,
            a.getBlue() + (b.getBlue() - a.getBlue()) * t
        );
    }

    /** Returns "#RRGGBB" for the given color. */
    public static String toHex(Color c) {
        if (c == null) return "#000000";
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /** Clamps value to [min, max]. */
    public static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
