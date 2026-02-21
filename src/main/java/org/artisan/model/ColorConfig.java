package org.artisan.model;

import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Color configuration for profile chart (curves and events).
 * Maps Python qmc.palette semantics to JavaFX colors. Supports full palette keys
 * (curve, background curve, graph, LCD) and optional alpha for legendbg, analysismask, statsanalysisbkgnd.
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
    /** Full palette by key (et, bt, deltaet, deltabt, background*, graph keys). Overrides theme when set. */
    private final Map<String, Color> palette = new HashMap<>();
    /** Alpha 0.0–1.0 for keys: legendbg, analysismask, statsanalysisbkgnd */
    private final Map<String, Double> paletteAlpha = new HashMap<>();

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

    public Color getCurveBT() { return palette.getOrDefault("bt", curveBT); }
    public void setCurveBT(Color c) { this.curveBT = c != null ? c : Color.BLACK; palette.put("bt", this.curveBT); }

    public Color getCurveET() { return palette.getOrDefault("et", curveET); }
    public void setCurveET(Color c) { this.curveET = c != null ? c : Color.BLACK; palette.put("et", this.curveET); }

    public Color getCurveRoR() { return curveRoR; }
    public void setCurveRoR(Color c) { this.curveRoR = c != null ? c : Color.BLACK; }

    public Color getCurveDeltaBT() { return palette.getOrDefault("deltabt", curveDeltaBT); }
    public void setCurveDeltaBT(Color c) { this.curveDeltaBT = c != null ? c : Color.BLACK; palette.put("deltabt", this.curveDeltaBT); }

    public Color getCurveDeltaET() { return palette.getOrDefault("deltaet", curveDeltaET); }
    public void setCurveDeltaET(Color c) { this.curveDeltaET = c != null ? c : Color.BLACK; palette.put("deltaet", this.curveDeltaET); }

    /** Get palette color by key (curve: et, bt, deltaet, deltabt; graph: background, canvas, grid, etc.). */
    public Color getPaletteColor(String key) {
        if (key == null) return Color.BLACK;
        Color c = palette.get(key);
        if (c != null) return c;
        switch (key) {
            case "et": return curveET;
            case "bt": return curveBT;
            case "deltaet": return curveDeltaET;
            case "deltabt": return curveDeltaBT;
            default: return Color.BLACK;
        }
    }

    /** Returns palette color only if explicitly set; null otherwise. */
    public Color getPaletteColorOrNull(String key) {
        if (key == null) return null;
        return palette.get(key);
    }

    /** Set palette color by key. */
    public void setPaletteColor(String key, Color color) {
        if (key == null) return;
        Color c = color != null ? color : Color.BLACK;
        palette.put(key, c);
        switch (key) {
            case "et": curveET = c; break;
            case "bt": curveBT = c; break;
            case "deltaet": curveDeltaET = c; break;
            case "deltabt": curveDeltaBT = c; break;
            default: break;
        }
    }

    /** Alpha 0.0–1.0 for legendbg, analysismask, statsanalysisbkgnd. */
    public double getPaletteAlpha(String key) {
        return paletteAlpha.getOrDefault(key != null ? key : "", 0.8);
    }
    public void setPaletteAlpha(String key, double value) {
        if (key != null) paletteAlpha.put(key, Math.max(0, Math.min(1, value)));
    }

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
