package org.artisan.controller;

import java.util.prefs.Preferences;

/**
 * Display and palette settings: curve colors, graph colors, LCD colors,
 * line widths, smoothing (Savitzky-Golay window), curve visibility, background alpha.
 * Persisted via Preferences (same node as AppSettings). Keys aligned with Python qmc.palette.
 */
public final class DisplaySettings {

    private static final String NODE = "org/artisan/artisan-java";
    private static DisplaySettings instance;

    // --- Curve colors (Curves tab) ---
    private static final String PREFIX_PALETTE = "palette.";
    private static final String KEY_ET = PREFIX_PALETTE + "et";
    private static final String KEY_BT = PREFIX_PALETTE + "bt";
    private static final String KEY_DELTAET = PREFIX_PALETTE + "deltaet";
    private static final String KEY_DELTABT = PREFIX_PALETTE + "deltabt";
    private static final String KEY_BG_ET = PREFIX_PALETTE + "backgroundmetcolor";
    private static final String KEY_BG_BT = PREFIX_PALETTE + "backgroundbtcolor";
    private static final String KEY_BG_DELTAET = PREFIX_PALETTE + "backgrounddeltaetcolor";
    private static final String KEY_BG_DELTABT = PREFIX_PALETTE + "backgrounddeltabtcolor";
    private static final String KEY_BACKGROUND_ALPHA = "backgroundalpha";

    // --- Graph colors ---
    private static final String KEY_GRAPH_BACKGROUND = PREFIX_PALETTE + "background";
    private static final String KEY_GRAPH_CANVAS = PREFIX_PALETTE + "canvas";
    private static final String KEY_GRAPH_GRID = PREFIX_PALETTE + "grid";
    private static final String KEY_GRAPH_TITLE = PREFIX_PALETTE + "title";
    private static final String KEY_GRAPH_YLABEL = PREFIX_PALETTE + "ylabel";
    private static final String KEY_GRAPH_XLABEL = PREFIX_PALETTE + "xlabel";
    private static final String KEY_GRAPH_TEXT = PREFIX_PALETTE + "text";
    private static final String KEY_GRAPH_MARKERS = PREFIX_PALETTE + "markers";
    private static final String KEY_GRAPH_WATERMARKS = PREFIX_PALETTE + "watermarks";
    private static final String KEY_GRAPH_TIMEGUIDE = PREFIX_PALETTE + "timeguide";
    private static final String KEY_GRAPH_AUCGUIDE = PREFIX_PALETTE + "aucguide";
    private static final String KEY_GRAPH_AUCAREA = PREFIX_PALETTE + "aucarea";
    private static final String KEY_GRAPH_LEGENDBG = PREFIX_PALETTE + "legendbg";
    private static final String KEY_GRAPH_LEGENDBORDER = PREFIX_PALETTE + "legendborder";
    private static final String KEY_GRAPH_RECT1 = PREFIX_PALETTE + "rect1";
    private static final String KEY_GRAPH_RECT2 = PREFIX_PALETTE + "rect2";
    private static final String KEY_GRAPH_RECT3 = PREFIX_PALETTE + "rect3";
    private static final String KEY_GRAPH_RECT4 = PREFIX_PALETTE + "rect4";
    private static final String KEY_GRAPH_RECT5 = PREFIX_PALETTE + "rect5";
    private static final String KEY_GRAPH_SPECIALEVENTBOX = PREFIX_PALETTE + "specialeventbox";
    private static final String KEY_GRAPH_SPECIALEVENTTEXT = PREFIX_PALETTE + "specialeventtext";
    private static final String KEY_GRAPH_BGEVENTMARKER = PREFIX_PALETTE + "bgeventmarker";
    private static final String KEY_GRAPH_BGEVENTTEXT = PREFIX_PALETTE + "bgeventtext";
    private static final String KEY_GRAPH_METBOX = PREFIX_PALETTE + "metbox";
    private static final String KEY_GRAPH_METTEXT = PREFIX_PALETTE + "mettext";
    private static final String KEY_GRAPH_ANALYSISMASK = PREFIX_PALETTE + "analysismask";
    private static final String KEY_GRAPH_STATSANALYSISBKGND = PREFIX_PALETTE + "statsanalysisbkgnd";
    private static final String KEY_ALPHA_LEGENDBG = "alpha.legendbg";
    private static final String KEY_ALPHA_ANALYSISMASK = "alpha.analysismask";
    private static final String KEY_ALPHA_STATSANALYSISBKGND = "alpha.statsanalysisbkgnd";
    private static final String KEY_AUC_BASE_TEMP = "display.aucBaseTemp";
    private static final String KEY_SHOW_CROSSHAIR = "display.showCrosshair";
    private static final String KEY_SHOW_WATERMARK = "display.showWatermark";
    private static final String KEY_SHOW_LEGEND = "display.showLegend";
    private static final String KEY_TIMEGUIDE_SEC = "display.timeguideSec";

    // --- LCD: foreground (digits) and background per LCD ---
    private static final String PREFIX_LCD_F = "lcd.f.";
    private static final String PREFIX_LCD_B = "lcd.b.";
    private static final String[] LCD_KEYS = { "timer", "et", "bt", "deltaet", "deltabt", "sv", "rstimer", "slowcoolingtimer" };

    // --- Line widths (px) ---
    private static final String KEY_LINEWIDTH_ET = "lineWidthET";
    private static final String KEY_LINEWIDTH_BT = "lineWidthBT";
    private static final String KEY_LINEWIDTH_DELTAET = "lineWidthDeltaET";
    private static final String KEY_LINEWIDTH_DELTABT = "lineWidthDeltaBT";

    // --- Smoothing: Savitzky-Golay window (1-99 odd, default 5) ---
    private static final String KEY_SMOOTHING_BT = "smoothingBT";
    private static final String KEY_SMOOTHING_ET = "smoothingET";
    private static final String KEY_SMOOTHING_DELTA = "smoothingDelta";

    // --- Curve visibility ---
    private static final String KEY_VISIBLE_ET = "visibleET";
    private static final String KEY_VISIBLE_BT = "visibleBT";
    private static final String KEY_VISIBLE_DELTAET = "visibleDeltaET";
    private static final String KEY_VISIBLE_DELTABT = "visibleDeltaBT";

    // --- Defaults (Python Artisan light theme) ---
    public static final String DEFAULT_ET = "#cc0f50";
    public static final String DEFAULT_BT = "#0a5c90";
    public static final String DEFAULT_DELTAET = "#cc0f50";
    public static final String DEFAULT_DELTABT = "#0a5c90";
    public static final String DEFAULT_BACKGROUND_ALPHA = "0.2";
    public static final int DEFAULT_LINEWIDTH_ET = 2;
    public static final int DEFAULT_LINEWIDTH_BT = 2;
    public static final int DEFAULT_LINEWIDTH_DELTAET = 1;
    public static final int DEFAULT_LINEWIDTH_DELTABT = 1;
    public static final int DEFAULT_SMOOTHING = 5;
    public static final double DEFAULT_ALPHA_LEGENDBG = 0.8;
    public static final double DEFAULT_ALPHA_ANALYSISMASK = 0.4;
    public static final double DEFAULT_ALPHA_STATSANALYSISBKGND = 1.0;
    public static final double DEFAULT_AUC_BASE_TEMP_C = 100.0;

    private String getPref(String key, String def) {
        return Preferences.userRoot().node(NODE).get(key, def);
    }

    private void setPref(String key, String value) {
        Preferences.userRoot().node(NODE).put(key, value != null ? value : "");
    }

    private int getPrefInt(String key, int def) {
        return Preferences.userRoot().node(NODE).getInt(key, def);
    }

    private void setPrefInt(String key, int value) {
        Preferences.userRoot().node(NODE).putInt(key, value);
    }

    private double getPrefDouble(String key, double def) {
        return Preferences.userRoot().node(NODE).getDouble(key, def);
    }

    private void setPrefDouble(String key, double value) {
        Preferences.userRoot().node(NODE).putDouble(key, value);
    }

    private boolean getPrefBoolean(String key, boolean def) {
        return Preferences.userRoot().node(NODE).getBoolean(key, def);
    }

    private void setPrefBoolean(String key, boolean value) {
        Preferences.userRoot().node(NODE).putBoolean(key, value);
    }

    public String getPaletteCurveET() { return getPref(KEY_ET, DEFAULT_ET); }
    public void setPaletteCurveET(String v) { setPref(KEY_ET, v); }

    public String getPaletteCurveBT() { return getPref(KEY_BT, DEFAULT_BT); }
    public void setPaletteCurveBT(String v) { setPref(KEY_BT, v); }

    public String getPaletteCurveDeltaET() { return getPref(KEY_DELTAET, DEFAULT_DELTAET); }
    public void setPaletteCurveDeltaET(String v) { setPref(KEY_DELTAET, v); }

    public String getPaletteCurveDeltaBT() { return getPref(KEY_DELTABT, DEFAULT_DELTABT); }
    public void setPaletteCurveDeltaBT(String v) { setPref(KEY_DELTABT, v); }

    public String getPaletteBackgroundET() { return getPref(KEY_BG_ET, DEFAULT_ET); }
    public void setPaletteBackgroundET(String v) { setPref(KEY_BG_ET, v); }

    public String getPaletteBackgroundBT() { return getPref(KEY_BG_BT, DEFAULT_BT); }
    public void setPaletteBackgroundBT(String v) { setPref(KEY_BG_BT, v); }

    public String getPaletteBackgroundDeltaET() { return getPref(KEY_BG_DELTAET, DEFAULT_DELTAET); }
    public void setPaletteBackgroundDeltaET(String v) { setPref(KEY_BG_DELTAET, v); }

    public String getPaletteBackgroundDeltaBT() { return getPref(KEY_BG_DELTABT, DEFAULT_DELTABT); }
    public void setPaletteBackgroundDeltaBT(String v) { setPref(KEY_BG_DELTABT, v); }

    /** 0.0–1.0 */
    public double getBackgroundAlpha() {
        try {
            return Double.parseDouble(getPref(KEY_BACKGROUND_ALPHA, DEFAULT_BACKGROUND_ALPHA));
        } catch (NumberFormatException e) {
            return 0.2;
        }
    }
    public void setBackgroundAlpha(double v) {
        setPref(KEY_BACKGROUND_ALPHA, String.valueOf(Math.max(0, Math.min(1, v))));
    }

    public int getLineWidthET() { return Math.max(1, getPrefInt(KEY_LINEWIDTH_ET, DEFAULT_LINEWIDTH_ET)); }
    public void setLineWidthET(int v) { setPrefInt(KEY_LINEWIDTH_ET, Math.max(1, v)); }

    public int getLineWidthBT() { return Math.max(1, getPrefInt(KEY_LINEWIDTH_BT, DEFAULT_LINEWIDTH_BT)); }
    public void setLineWidthBT(int v) { setPrefInt(KEY_LINEWIDTH_BT, Math.max(1, v)); }

    public int getLineWidthDeltaET() { return Math.max(1, getPrefInt(KEY_LINEWIDTH_DELTAET, DEFAULT_LINEWIDTH_DELTAET)); }
    public void setLineWidthDeltaET(int v) { setPrefInt(KEY_LINEWIDTH_DELTAET, Math.max(1, v)); }

    public int getLineWidthDeltaBT() { return Math.max(1, getPrefInt(KEY_LINEWIDTH_DELTABT, DEFAULT_LINEWIDTH_DELTABT)); }
    public void setLineWidthDeltaBT(int v) { setPrefInt(KEY_LINEWIDTH_DELTABT, Math.max(1, v)); }

    /** Odd 1–99 */
    public int getSmoothingBT() { return toOdd1_99(getPrefInt(KEY_SMOOTHING_BT, DEFAULT_SMOOTHING)); }
    public void setSmoothingBT(int v) { setPrefInt(KEY_SMOOTHING_BT, toOdd1_99(v)); }

    public int getSmoothingET() { return toOdd1_99(getPrefInt(KEY_SMOOTHING_ET, DEFAULT_SMOOTHING)); }
    public void setSmoothingET(int v) { setPrefInt(KEY_SMOOTHING_ET, toOdd1_99(v)); }

    public int getSmoothingDelta() { return toOdd1_99(getPrefInt(KEY_SMOOTHING_DELTA, DEFAULT_SMOOTHING)); }
    public void setSmoothingDelta(int v) { setPrefInt(KEY_SMOOTHING_DELTA, toOdd1_99(v)); }

    private static int toOdd1_99(int v) {
        if (v < 1) return 1;
        if (v > 99) return 99;
        return (v & 1) == 1 ? v : v + 1;
    }

    public boolean isVisibleET() { return getPrefBoolean(KEY_VISIBLE_ET, true); }
    public void setVisibleET(boolean v) { setPrefBoolean(KEY_VISIBLE_ET, v); }

    public boolean isVisibleBT() { return getPrefBoolean(KEY_VISIBLE_BT, true); }
    public void setVisibleBT(boolean v) { setPrefBoolean(KEY_VISIBLE_BT, v); }

    public boolean isVisibleDeltaET() { return getPrefBoolean(KEY_VISIBLE_DELTAET, true); }
    public void setVisibleDeltaET(boolean v) { setPrefBoolean(KEY_VISIBLE_DELTAET, v); }

    public boolean isVisibleDeltaBT() { return getPrefBoolean(KEY_VISIBLE_DELTABT, true); }
    public void setVisibleDeltaBT(boolean v) { setPrefBoolean(KEY_VISIBLE_DELTABT, v); }

    // --- Graph palette keys (defaults from Python) ---
    public String getPalette(String key) {
        String def = defaultGraphPalette(key);
        if (def != null) return getPref(PREFIX_PALETTE + key, def);
        return getPref(PREFIX_PALETTE + key, "#000000");
    }
    public void setPalette(String key, String hex) {
        setPref(PREFIX_PALETTE + key, hex != null ? hex : "#000000");
    }

    private static String defaultGraphPalette(String key) {
        switch (key) {
            case "background": return "#ffffff";
            case "canvas": return "#f8f8f8";
            case "grid": return "#e5e5e5";
            case "title": return "#0c6aa6";
            case "ylabel": case "xlabel": return "#808080";
            case "text": case "markers": return "#000000";
            case "watermarks": return "#ffff00";
            case "timeguide": return "#0a5c90";
            case "aucguide": return "#0c6aa6";
            case "aucarea": return "#767676";
            case "legendbg": return "#ffffff";
            case "legendborder": return "#a9a9a9";
            case "rect1": return "#e5e5e5";
            case "rect2": return "#b2b2b2";
            case "rect3": return "#e5e5e5";
            case "rect4": return "#bde0ee";
            case "rect5": return "#d3d3d3";
            case "specialeventbox": return "#ff5871";
            case "specialeventtext": return "#ffffff";
            case "bgeventmarker": return "#7f7f7f";
            case "bgeventtext": return "#000000";
            case "metbox": return "#cc0f50";
            case "mettext": return "#ffffff";
            case "backgroundmetcolor": return DEFAULT_ET;
            case "backgroundbtcolor": return DEFAULT_BT;
            case "backgrounddeltaetcolor": return DEFAULT_DELTAET;
            case "backgrounddeltabtcolor": return DEFAULT_DELTABT;
            case "analysismask": return "#bababa";
            case "statsanalysisbkgnd": return "#ffffff";
            default: return null;
        }
    }

    public double getAlphaLegendBg() { return getPrefDouble(KEY_ALPHA_LEGENDBG, DEFAULT_ALPHA_LEGENDBG); }
    public void setAlphaLegendBg(double v) { setPrefDouble(KEY_ALPHA_LEGENDBG, Math.max(0, Math.min(1, v))); }
    public double getAlphaAnalysismask() { return getPrefDouble(KEY_ALPHA_ANALYSISMASK, DEFAULT_ALPHA_ANALYSISMASK); }
    public void setAlphaAnalysismask(double v) { setPrefDouble(KEY_ALPHA_ANALYSISMASK, Math.max(0, Math.min(1, v))); }
    public double getAlphaStatsanalysisbkgnd() { return getPrefDouble(KEY_ALPHA_STATSANALYSISBKGND, DEFAULT_ALPHA_STATSANALYSISBKGND); }
    public void setAlphaStatsanalysisbkgnd(double v) { setPrefDouble(KEY_ALPHA_STATSANALYSISBKGND, Math.max(0, Math.min(1, v))); }

    /** AUC base temperature (°C). Area above this line is integrated. */
    public double getAucBaseTemp() { return getPrefDouble(KEY_AUC_BASE_TEMP, DEFAULT_AUC_BASE_TEMP_C); }
    public void setAucBaseTemp(double v) { setPrefDouble(KEY_AUC_BASE_TEMP, Math.max(0, Math.min(300, v))); }

    public boolean isShowCrosshair() { return getPrefBoolean(KEY_SHOW_CROSSHAIR, true); }
    public void setShowCrosshair(boolean v) { setPrefBoolean(KEY_SHOW_CROSSHAIR, v); }

    public boolean isShowWatermark() { return getPrefBoolean(KEY_SHOW_WATERMARK, true); }
    public void setShowWatermark(boolean v) { setPrefBoolean(KEY_SHOW_WATERMARK, v); }

    public boolean isShowLegend() { return getPrefBoolean(KEY_SHOW_LEGEND, true); }
    public void setShowLegend(boolean v) { setPrefBoolean(KEY_SHOW_LEGEND, v); }

    /** Time guide line position (seconds); 0 = off. */
    public double getTimeguideSec() { return getPrefDouble(KEY_TIMEGUIDE_SEC, 0.0); }
    public void setTimeguideSec(double v) { setPrefDouble(KEY_TIMEGUIDE_SEC, Math.max(0, Math.min(3600, v))); }

    public String getLcdForeground(String lcdKey) {
        return getPref(PREFIX_LCD_F + lcdKey, "#000000");
    }
    public void setLcdForeground(String lcdKey, String hex) {
        setPref(PREFIX_LCD_F + lcdKey, hex != null ? hex : "#000000");
    }
    public String getLcdBackground(String lcdKey) {
        return getPref(PREFIX_LCD_B + lcdKey, "#ffffff");
    }
    public void setLcdBackground(String lcdKey, String hex) {
        setPref(PREFIX_LCD_B + lcdKey, hex != null ? hex : "#ffffff");
    }
    public static String[] getLcdKeys() { return LCD_KEYS.clone(); }

    /** Load from preferences into this in-memory (DisplaySettings is stateless read-through). */
    public static DisplaySettings load() {
        DisplaySettings settings = new DisplaySettings();
        instance = settings;
        return settings;
    }

    /** Shared instance (loaded lazily). */
    public static synchronized DisplaySettings getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    /** Palette keys used by the UI. */
    public static String[] getPaletteKeys() {
        return new String[] {
            "background", "canvas", "grid", "title", "ylabel", "xlabel", "text", "markers", "watermarks",
            "timeguide", "aucguide", "aucarea", "legendbg", "legendborder",
            "rect1", "rect2", "rect3", "rect4", "rect5",
            "specialeventbox", "specialeventtext", "bgeventmarker", "bgeventtext", "metbox", "mettext",
            "analysismask", "statsanalysisbkgnd",
            "backgroundmetcolor", "backgroundbtcolor", "backgrounddeltaetcolor", "backgrounddeltabtcolor"
        };
    }

    /** Flush preferences to storage. */
    public void save() {
        try {
            Preferences.userRoot().node(NODE).flush();
        } catch (Exception ignored) {}
    }

    /** Restore all curve/graph/LCD and display defaults. Caller should persist (we write to prefs in setters when using load()). */
    public void restoreDefaults() {
        setPaletteCurveET(DEFAULT_ET);
        setPaletteCurveBT(DEFAULT_BT);
        setPaletteCurveDeltaET(DEFAULT_DELTAET);
        setPaletteCurveDeltaBT(DEFAULT_DELTABT);
        setPaletteBackgroundET(DEFAULT_ET);
        setPaletteBackgroundBT(DEFAULT_BT);
        setPaletteBackgroundDeltaET(DEFAULT_DELTAET);
        setPaletteBackgroundDeltaBT(DEFAULT_DELTABT);
        setBackgroundAlpha(0.2);
        setLineWidthET(DEFAULT_LINEWIDTH_ET);
        setLineWidthBT(DEFAULT_LINEWIDTH_BT);
        setLineWidthDeltaET(DEFAULT_LINEWIDTH_DELTAET);
        setLineWidthDeltaBT(DEFAULT_LINEWIDTH_DELTABT);
        setSmoothingBT(DEFAULT_SMOOTHING);
        setSmoothingET(DEFAULT_SMOOTHING);
        setSmoothingDelta(DEFAULT_SMOOTHING);
        setVisibleET(true);
        setVisibleBT(true);
        setVisibleDeltaET(true);
        setVisibleDeltaBT(true);
        for (String k : new String[] { "background", "canvas", "grid", "title", "ylabel", "xlabel", "text", "markers", "watermarks",
                "timeguide", "aucguide", "aucarea", "legendbg", "legendborder", "rect1", "rect2", "rect3", "rect4", "rect5",
                "specialeventbox", "specialeventtext", "bgeventmarker", "bgeventtext", "metbox", "mettext", "analysismask", "statsanalysisbkgnd" }) {
            String d = defaultGraphPalette(k);
            if (d != null) setPalette(k, d);
        }
        setAlphaLegendBg(DEFAULT_ALPHA_LEGENDBG);
        setAlphaAnalysismask(DEFAULT_ALPHA_ANALYSISMASK);
        setAlphaStatsanalysisbkgnd(DEFAULT_ALPHA_STATSANALYSISBKGND);
        setShowCrosshair(true);
        setShowWatermark(true);
        setShowLegend(true);
        setTimeguideSec(0.0);
        for (String lcd : LCD_KEYS) {
            setLcdForeground(lcd, "#000000");
            setLcdBackground(lcd, "#ffffff");
        }
    }

    /** Set all curve/graph colors to greyscale and LCDs to B/W. */
    public void setGrey() {
        setPaletteCurveET("#404040");
        setPaletteCurveBT("#404040");
        setPaletteCurveDeltaET("#808080");
        setPaletteCurveDeltaBT("#808080");
        setPaletteBackgroundET("#a0a0a0");
        setPaletteBackgroundBT("#a0a0a0");
        setPaletteBackgroundDeltaET("#a0a0a0");
        setPaletteBackgroundDeltaBT("#a0a0a0");
        setPalette("background", "#ffffff");
        setPalette("canvas", "#f0f0f0");
        setPalette("grid", "#c0c0c0");
        setPalette("title", "#000000");
        setPalette("ylabel", "#404040");
        setPalette("xlabel", "#404040");
        setPalette("text", "#000000");
        setPalette("markers", "#000000");
        setPalette("watermarks", "#606060");
        setPalette("timeguide", "#404040");
        setPalette("aucguide", "#404040");
        setPalette("aucarea", "#a0a0a0");
        setPalette("legendbg", "#e0e0e0");
        setPalette("legendborder", "#808080");
        setPalette("rect1", "#e5e5e5");
        setPalette("rect2", "#b2b2b2");
        setPalette("rect3", "#e5e5e5");
        setPalette("rect4", "#b0b0b0");
        setPalette("rect5", "#d3d3d3");
        setPalette("specialeventbox", "#808080");
        setPalette("specialeventtext", "#ffffff");
        setPalette("bgeventmarker", "#606060");
        setPalette("bgeventtext", "#000000");
        setPalette("metbox", "#606060");
        setPalette("mettext", "#ffffff");
        setPalette("analysismask", "#c0c0c0");
        setPalette("statsanalysisbkgnd", "#e0e0e0");
        for (String lcd : LCD_KEYS) {
            setLcdForeground(lcd, "#000000");
            setLcdBackground(lcd, "#ffffff");
        }
    }
}
