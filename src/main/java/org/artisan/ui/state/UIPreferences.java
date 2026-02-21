package org.artisan.ui.state;

import org.artisan.ui.model.AlertRule;
import org.artisan.ui.model.MeasurementConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated UI preferences: layout, density, readout size, curve visibility, theme, shortcuts.
 * Loaded/saved via PreferencesStore.
 */
public final class UIPreferences {

    public static final int SCHEMA_VERSION = 1;
    public static final String DEFAULT_CHART_APPEARANCE_PRESET = "RI5 Default";

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
    public enum SliderButtonPosition { TOP, BOTTOM }
    public enum MachineReadoutSize { SMALL, LARGE }

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
    private List<AlertRule> alertRules = new ArrayList<>();
    private Map<String, Boolean> curveVisibility = new LinkedHashMap<>();
    private SliderButtonPosition sliderButtonPosition = SliderButtonPosition.TOP;
    private MachineReadoutSize machineReadoutSize = MachineReadoutSize.SMALL;
    private boolean tourCompleted = false;
    private String lastSeenVersion = "";
    private Map<String, Boolean> referenceInfoSections = new LinkedHashMap<>();
    private String replayFilePath = null;
    private List<MeasurementConfig> measurementConfigs = new ArrayList<>();
    private String drawerLastOpenedSection = null;
    private ChartAppearance chartAppearance = ChartAppearance.ri5Default();
    private Map<String, ChartAppearance> chartAppearancePresets;
    private String chartAppearanceActivePreset = DEFAULT_CHART_APPEARANCE_PRESET;

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

    public List<AlertRule> getAlertRules() {
        return alertRules != null ? alertRules : new ArrayList<>();
    }

    public void setAlertRules(List<AlertRule> alertRules) {
        this.alertRules = alertRules != null ? new ArrayList<>(alertRules) : new ArrayList<>();
    }

    public Map<String, Boolean> getCurveVisibility() {
        return curveVisibility != null ? curveVisibility : new LinkedHashMap<>();
    }

    public void setCurveVisibility(Map<String, Boolean> curveVisibility) {
        this.curveVisibility = curveVisibility != null ? new LinkedHashMap<>(curveVisibility) : new LinkedHashMap<>();
    }

    public SliderButtonPosition getSliderButtonPosition() {
        return sliderButtonPosition != null ? sliderButtonPosition : SliderButtonPosition.TOP;
    }

    public void setSliderButtonPosition(SliderButtonPosition sliderButtonPosition) {
        this.sliderButtonPosition = sliderButtonPosition != null ? sliderButtonPosition : SliderButtonPosition.TOP;
    }

    public MachineReadoutSize getMachineReadoutSize() {
        return machineReadoutSize != null ? machineReadoutSize : MachineReadoutSize.SMALL;
    }

    public void setMachineReadoutSize(MachineReadoutSize machineReadoutSize) {
        this.machineReadoutSize = machineReadoutSize != null ? machineReadoutSize : MachineReadoutSize.SMALL;
    }

    public boolean isTourCompleted() {
        return tourCompleted;
    }

    public void setTourCompleted(boolean tourCompleted) {
        this.tourCompleted = tourCompleted;
    }

    public String getLastSeenVersion() {
        return lastSeenVersion != null ? lastSeenVersion : "";
    }

    public void setLastSeenVersion(String lastSeenVersion) {
        this.lastSeenVersion = lastSeenVersion != null ? lastSeenVersion : "";
    }

    public Map<String, Boolean> getReferenceInfoSections() {
        return referenceInfoSections != null ? referenceInfoSections : new LinkedHashMap<>();
    }

    public void setReferenceInfoSections(Map<String, Boolean> referenceInfoSections) {
        this.referenceInfoSections = referenceInfoSections != null ? new LinkedHashMap<>(referenceInfoSections) : new LinkedHashMap<>();
    }

    public String getReplayFilePath() {
        return replayFilePath;
    }

    public void setReplayFilePath(String replayFilePath) {
        this.replayFilePath = replayFilePath;
    }

    public List<MeasurementConfig> getMeasurementConfigs() {
        return measurementConfigs != null ? measurementConfigs : new ArrayList<>();
    }

    public void setMeasurementConfigs(List<MeasurementConfig> measurementConfigs) {
        this.measurementConfigs = measurementConfigs != null ? new ArrayList<>(measurementConfigs) : new ArrayList<>();
    }

    public String getDrawerLastOpenedSection() {
        return drawerLastOpenedSection;
    }

    public void setDrawerLastOpenedSection(String drawerLastOpenedSection) {
        this.drawerLastOpenedSection = drawerLastOpenedSection;
    }

    public ChartAppearance getChartAppearance() {
        return chartAppearance != null ? chartAppearance : ChartAppearance.ri5Default();
    }

    public void setChartAppearance(ChartAppearance chartAppearance) {
        this.chartAppearance = chartAppearance != null ? chartAppearance : ChartAppearance.ri5Default();
    }

    public Map<String, ChartAppearance> getChartAppearancePresets() {
        if (chartAppearancePresets == null) {
            chartAppearancePresets = new LinkedHashMap<>();
        }
        if (!chartAppearancePresets.containsKey(DEFAULT_CHART_APPEARANCE_PRESET)) {
            chartAppearancePresets.put(DEFAULT_CHART_APPEARANCE_PRESET, ChartAppearance.ri5Default());
        }
        return chartAppearancePresets;
    }

    public void setChartAppearancePresets(Map<String, ChartAppearance> presets) {
        chartAppearancePresets = presets != null ? new LinkedHashMap<>(presets) : new LinkedHashMap<>();
        if (!chartAppearancePresets.containsKey(DEFAULT_CHART_APPEARANCE_PRESET)) {
            chartAppearancePresets.put(DEFAULT_CHART_APPEARANCE_PRESET, ChartAppearance.ri5Default());
        }
    }

    public String getChartAppearanceActivePreset() {
        if (chartAppearanceActivePreset == null || chartAppearanceActivePreset.isBlank()) {
            return DEFAULT_CHART_APPEARANCE_PRESET;
        }
        return chartAppearanceActivePreset;
    }

    public void setChartAppearanceActivePreset(String chartAppearanceActivePreset) {
        this.chartAppearanceActivePreset = chartAppearanceActivePreset != null && !chartAppearanceActivePreset.isBlank()
            ? chartAppearanceActivePreset
            : DEFAULT_CHART_APPEARANCE_PRESET;
    }
}
