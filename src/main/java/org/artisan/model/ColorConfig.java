package org.artisan.model;

import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.Map;

/**
 * Color configuration for profile chart (curves and events).
 * Maps Python palette / EvalueColor semantics to JavaFX colors.
 * Supports light and dark theme (AtlantaFX Primer Light / Dark style).
 */
public final class ColorConfig {

    public enum Theme {
        LIGHT,
        DARK
    }

    /** Python-style hex keys (e.g. "#0a5c90", "#cc0f50") to JavaFX Color. */
    private static final Map<String, Color> HEX_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private Color curveBT;
    private Color curveET;
    private Color curveRoR;
    private Color curveDeltaBT;
    private Color curveDeltaET;
    private final Map<EventType, Color> eventColors = new EnumMap<>(EventType.class);
    private Theme theme;

    public ColorConfig() {
        this(Theme.LIGHT);
    }

    public ColorConfig(Theme theme) {
        this.theme = theme != null ? theme : Theme.LIGHT;
        applyTheme(this.theme);
    }

    private void applyTheme(Theme t) {
        if (t == Theme.DARK) {
            curveBT = fromHex("#5ca8d6");
            curveET = fromHex("#e05c7a");
            curveRoR = fromHex("#e05c7a");
            curveDeltaBT = fromHex("#5ca8d6");
            curveDeltaET = fromHex("#e05c7a");
            eventColors.put(EventType.CHARGE, fromHex("#5eb8e8"));
            eventColors.put(EventType.DRY_END, fromHex("#60c968"));
            eventColors.put(EventType.FC_START, fromHex("#b86dd4"));
            eventColors.put(EventType.FC_END, fromHex("#d03050"));
            eventColors.put(EventType.DROP, fromHex("#d03050"));
            eventColors.put(EventType.CUSTOM, fromHex("#a0a0a0"));
        } else {
            curveBT = fromHex("#0a5c90");
            curveET = fromHex("#cc0f50");
            curveRoR = fromHex("#cc0f50");
            curveDeltaBT = fromHex("#0a5c90");
            curveDeltaET = fromHex("#cc0f50");
            eventColors.put(EventType.CHARGE, fromHex("#43a7cf"));
            eventColors.put(EventType.DRY_END, fromHex("#49b160"));
            eventColors.put(EventType.FC_START, fromHex("#800080"));
            eventColors.put(EventType.FC_END, fromHex("#ad0427"));
            eventColors.put(EventType.DROP, fromHex("#ad0427"));
            eventColors.put(EventType.CUSTOM, fromHex("#404040"));
        }
    }

    /**
     * Parses a Python-style color string to JavaFX Color.
     * Supports: "#rgb", "#rrggbb", "#aarrggbb", and named colors like "darkred" (with or without "#").
     */
    public static Color fromHex(String hexOrName) {
        if (hexOrName == null || hexOrName.isBlank()) {
            return Color.BLACK;
        }
        String key = hexOrName.strip().toLowerCase();
        return HEX_CACHE.computeIfAbsent(key, k -> {
            if (k.startsWith("#")) {
                try {
                    return Color.web(k);
                } catch (Exception e) {
                    return Color.BLACK;
                }
            }
            try {
                return Color.web("#" + k);
            } catch (Exception e) {
                return resolveNamedColor(k);
            }
        });
    }

    private static Color resolveNamedColor(String name) {
        switch (name) {
            case "darkred": return Color.DARKRED;
            case "darkblue": return Color.DARKBLUE;
            case "darkgreen": return Color.DARKGREEN;
            case "black": return Color.BLACK;
            case "white": return Color.WHITE;
            case "gray":
            case "grey": return Color.GRAY;
            default:
                try {
                    return Color.web("#" + name);
                } catch (Exception e) {
                    return Color.BLACK;
                }
        }
    }

    public Color getCurveBT() { return curveBT; }
    public void setCurveBT(Color c) { this.curveBT = c != null ? c : Color.BLACK; }

    public Color getCurveET() { return curveET; }
    public void setCurveET(Color c) { this.curveET = c != null ? c : Color.BLACK; }

    public Color getCurveRoR() { return curveRoR; }
    public void setCurveRoR(Color c) { this.curveRoR = c != null ? c : Color.BLACK; }

    public Color getCurveDeltaBT() { return curveDeltaBT; }
    public void setCurveDeltaBT(Color c) { this.curveDeltaBT = c != null ? c : Color.BLACK; }

    public Color getCurveDeltaET() { return curveDeltaET; }
    public void setCurveDeltaET(Color c) { this.curveDeltaET = c != null ? c : Color.BLACK; }

    public Color getEventColor(EventType event) {
        return eventColors.getOrDefault(event != null ? event : EventType.CUSTOM, Color.GRAY);
    }

    public void setEventColor(EventType event, Color color) {
        if (event != null) {
            eventColors.put(event, color != null ? color : Color.GRAY);
        }
    }

    public Theme getTheme() { return theme; }

    public void setTheme(Theme theme) {
        if (theme != null && theme != this.theme) {
            this.theme = theme;
            applyTheme(theme);
        }
    }
}
