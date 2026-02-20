package org.artisan.ui.state;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregated UI preferences: layout, density, readout size, curve visibility, theme, shortcuts.
 * Loaded/saved via PreferencesStore.
 */
public final class UIPreferences {

    public static final int SCHEMA_VERSION = 1;

    /** Default keyboard shortcut action -> key name (e.g. "addEvent" -> "SPACE"). */
    public static final Map<String, String> DEFAULT_SHORTCUTS = defaultShortcuts();

    private static Map<String, String> defaultShortcuts() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("addEvent", "SPACE");
        m.put("charge", "1");
        m.put("dryEnd", "2");
        m.put("fcStart", "3");
        m.put("fcEnd", "4");
        m.put("drop", "5");
        m.put("toggleControls", "C");
        m.put("toggleLegend", "L");
        m.put("focusEventLog", "E");
        m.put("help", "SHIFT+SLASH");
        return Collections.unmodifiableMap(m);
    }

    public enum Density { COMPACT, COMFORTABLE }
    public enum ReadoutSize { S, M, L }

    private int schemaVersion = SCHEMA_VERSION;
    private String theme = "light"; // "light" | "dark"
    private Density density = Density.COMFORTABLE;
    private ReadoutSize readoutSize = ReadoutSize.M;
    private boolean visibleBT = true;
    private boolean visibleET = true;
    private boolean visibleDeltaBT = true;
    private boolean visibleDeltaET = true;
    private LayoutState layoutState = new LayoutState();
    /** Divider positions for main SplitPanes (0.0–1.0). First: center vs right dock. */
    private double mainDividerPosition = 0.75;
    /** Keyboard shortcuts: action id -> key name. Null means use DEFAULT_SHORTCUTS. */
    private Map<String, String> shortcuts;

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getTheme() {
        return theme != null ? theme : "dark";
    }

    public void setTheme(String theme) {
        this.theme = "light".equalsIgnoreCase(theme) ? "light" : "dark";
    }

    public Density getDensity() {
        return density != null ? density : Density.COMFORTABLE;
    }

    public void setDensity(Density density) {
        this.density = density != null ? density : Density.COMFORTABLE;
    }

    public ReadoutSize getReadoutSize() {
        return readoutSize != null ? readoutSize : ReadoutSize.M;
    }

    public void setReadoutSize(ReadoutSize readoutSize) {
        this.readoutSize = readoutSize != null ? readoutSize : ReadoutSize.M;
    }

    public boolean isVisibleBT() {
        return visibleBT;
    }

    public void setVisibleBT(boolean visibleBT) {
        this.visibleBT = visibleBT;
    }

    public boolean isVisibleET() {
        return visibleET;
    }

    public void setVisibleET(boolean visibleET) {
        this.visibleET = visibleET;
    }

    public boolean isVisibleDeltaBT() {
        return visibleDeltaBT;
    }

    public void setVisibleDeltaBT(boolean visibleDeltaBT) {
        this.visibleDeltaBT = visibleDeltaBT;
    }

    public boolean isVisibleDeltaET() {
        return visibleDeltaET;
    }

    public void setVisibleDeltaET(boolean visibleDeltaET) {
        this.visibleDeltaET = visibleDeltaET;
    }

    public LayoutState getLayoutState() {
        return layoutState != null ? layoutState : new LayoutState();
    }

    public void setLayoutState(LayoutState layoutState) {
        this.layoutState = layoutState != null ? layoutState : new LayoutState();
    }

    public double getMainDividerPosition() {
        double v = mainDividerPosition <= 0 || mainDividerPosition > 1 ? 0.75 : mainDividerPosition;
        return Math.max(0.1, Math.min(0.9, v));
    }

    public void setMainDividerPosition(double mainDividerPosition) {
        this.mainDividerPosition = Math.max(0.1, Math.min(0.9, mainDividerPosition));
    }

    /** Returns shortcuts map; if null returns copy of DEFAULT_SHORTCUTS. */
    public Map<String, String> getShortcuts() {
        if (shortcuts == null || shortcuts.isEmpty()) {
            return new LinkedHashMap<>(DEFAULT_SHORTCUTS);
        }
        Map<String, String> out = new LinkedHashMap<>(DEFAULT_SHORTCUTS);
        out.putAll(shortcuts);
        return out;
    }

    public void setShortcuts(Map<String, String> shortcuts) {
        this.shortcuts = shortcuts == null ? null : new LinkedHashMap<>(shortcuts);
    }
}
