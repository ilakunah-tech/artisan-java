package org.artisan.controller;

import java.util.prefs.Preferences;

import org.artisan.model.PhasesConfig;
import org.artisan.model.PhaseDisplayMode;

/**
 * Phases settings persisted in Preferences (same node as AppSettings/DisplaySettings).
 * Keys namespaced with "phases." to avoid collisions.
 * See Artisan docs: https://artisan-scope.org/docs/phases/
 */
public final class PhasesSettings {

    private static final String NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "phases.";

    private static final String KEY_DRY_END_TEMP_C = PREFIX + "dryEndTempC";
    private static final String KEY_FCS_TEMP_C = PREFIX + "fcsTempC";
    private static final String KEY_AUTO_ADJUSTED_LIMITS = PREFIX + "autoAdjustedLimits";
    private static final String KEY_AUTO_DRY = PREFIX + "autoDRY";
    private static final String KEY_AUTO_FCS = PREFIX + "autoFCs";
    private static final String KEY_FROM_BACKGROUND = PREFIX + "fromBackground";
    private static final String KEY_DRYING_ENTER_MODE = PREFIX + "dryingEnterMode";
    private static final String KEY_MAILLARD_ENTER_MODE = PREFIX + "maillardEnterMode";
    private static final String KEY_FINISHING_ENTER_MODE = PREFIX + "finishingEnterMode";
    private static final String KEY_FINISHING_SHOW_ALL_LCDS = PREFIX + "finishingShowAllLcds";
    /** Current LCD display mode (session or persisted). Right-click cycles TIME → PERCENTAGE → TEMPERATURE. */
    private static final String KEY_LCD_MODE = PREFIX + "lcdMode";

    public static final double DEFAULT_DRY_END_TEMP_C = 150.0;
    public static final double DEFAULT_FCS_TEMP_C = 195.0;
    public static final boolean DEFAULT_AUTO_ADJUSTED_LIMITS = true;
    public static final boolean DEFAULT_AUTO_DRY = false;
    public static final boolean DEFAULT_AUTO_FCS = false;
    public static final boolean DEFAULT_FROM_BACKGROUND = false;
    public static final boolean DEFAULT_FINISHING_SHOW_ALL_LCDS = false;

    private Preferences prefs() {
        return Preferences.userRoot().node(NODE);
    }

    public double getDryEndTempC() {
        return prefs().getDouble(KEY_DRY_END_TEMP_C, DEFAULT_DRY_END_TEMP_C);
    }

    public void setDryEndTempC(double v) {
        prefs().putDouble(KEY_DRY_END_TEMP_C, v);
    }

    public double getFcsTempC() {
        return prefs().getDouble(KEY_FCS_TEMP_C, DEFAULT_FCS_TEMP_C);
    }

    public void setFcsTempC(double v) {
        prefs().putDouble(KEY_FCS_TEMP_C, v);
    }

    public boolean isAutoAdjustedLimits() {
        return prefs().getBoolean(KEY_AUTO_ADJUSTED_LIMITS, DEFAULT_AUTO_ADJUSTED_LIMITS);
    }

    public void setAutoAdjustedLimits(boolean v) {
        prefs().putBoolean(KEY_AUTO_ADJUSTED_LIMITS, v);
    }

    public boolean isAutoDRY() {
        return prefs().getBoolean(KEY_AUTO_DRY, DEFAULT_AUTO_DRY);
    }

    public void setAutoDRY(boolean v) {
        prefs().putBoolean(KEY_AUTO_DRY, v);
    }

    public boolean isAutoFCs() {
        return prefs().getBoolean(KEY_AUTO_FCS, DEFAULT_AUTO_FCS);
    }

    public void setAutoFCs(boolean v) {
        prefs().putBoolean(KEY_AUTO_FCS, v);
    }

    public boolean isFromBackground() {
        return prefs().getBoolean(KEY_FROM_BACKGROUND, DEFAULT_FROM_BACKGROUND);
    }

    public void setFromBackground(boolean v) {
        prefs().putBoolean(KEY_FROM_BACKGROUND, v);
    }

    public PhaseDisplayMode getDryingEnterMode() {
        return parseMode(prefs().get(KEY_DRYING_ENTER_MODE, PhaseDisplayMode.TIME.name()));
    }

    public void setDryingEnterMode(PhaseDisplayMode v) {
        prefs().put(KEY_DRYING_ENTER_MODE, v != null ? v.name() : PhaseDisplayMode.TIME.name());
    }

    public PhaseDisplayMode getMaillardEnterMode() {
        return parseMode(prefs().get(KEY_MAILLARD_ENTER_MODE, PhaseDisplayMode.TIME.name()));
    }

    public void setMaillardEnterMode(PhaseDisplayMode v) {
        prefs().put(KEY_MAILLARD_ENTER_MODE, v != null ? v.name() : PhaseDisplayMode.TIME.name());
    }

    public PhaseDisplayMode getFinishingEnterMode() {
        return parseMode(prefs().get(KEY_FINISHING_ENTER_MODE, PhaseDisplayMode.TIME.name()));
    }

    public void setFinishingEnterMode(PhaseDisplayMode v) {
        prefs().put(KEY_FINISHING_ENTER_MODE, v != null ? v.name() : PhaseDisplayMode.TIME.name());
    }

    public boolean isFinishingShowAllLcds() {
        return prefs().getBoolean(KEY_FINISHING_SHOW_ALL_LCDS, DEFAULT_FINISHING_SHOW_ALL_LCDS);
    }

    public void setFinishingShowAllLcds(boolean v) {
        prefs().putBoolean(KEY_FINISHING_SHOW_ALL_LCDS, v);
    }

    /** Current Phases LCD display mode (right-click cycles). */
    public PhaseDisplayMode getLcdMode() {
        return parseMode(prefs().get(KEY_LCD_MODE, PhaseDisplayMode.TIME.name()));
    }

    public void setLcdMode(PhaseDisplayMode v) {
        prefs().put(KEY_LCD_MODE, v != null ? v.name() : PhaseDisplayMode.TIME.name());
    }

    private static PhaseDisplayMode parseMode(String s) {
        if (s == null || s.isBlank()) return PhaseDisplayMode.TIME;
        try {
            return PhaseDisplayMode.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return PhaseDisplayMode.TIME;
        }
    }

    /** Copy current preferences into a PhasesConfig DTO (e.g. for dialog editing). */
    public PhasesConfig toConfig() {
        PhasesConfig c = new PhasesConfig();
        c.setDryEndTempC(getDryEndTempC());
        c.setFcsTempC(getFcsTempC());
        c.setAutoAdjustedLimits(isAutoAdjustedLimits());
        c.setAutoDRY(isAutoDRY());
        c.setAutoFCs(isAutoFCs());
        c.setFromBackground(isFromBackground());
        c.setDryingEnterMode(getDryingEnterMode());
        c.setMaillardEnterMode(getMaillardEnterMode());
        c.setFinishingEnterMode(getFinishingEnterMode());
        c.setFinishingShowAllLcds(isFinishingShowAllLcds());
        c.setLcdMode(getLcdMode());
        return c;
    }

    /** Apply a PhasesConfig (e.g. from dialog OK) to preferences. */
    public void fromConfig(PhasesConfig c) {
        if (c == null) return;
        setDryEndTempC(c.getDryEndTempC());
        setFcsTempC(c.getFcsTempC());
        setAutoAdjustedLimits(c.isAutoAdjustedLimits());
        setAutoDRY(c.isAutoDRY());
        setAutoFCs(c.isAutoFCs());
        setFromBackground(c.isFromBackground());
        setDryingEnterMode(c.getDryingEnterMode());
        setMaillardEnterMode(c.getMaillardEnterMode());
        setFinishingEnterMode(c.getFinishingEnterMode());
        setFinishingShowAllLcds(c.isFinishingShowAllLcds());
        if (c.getLcdMode() != null) setLcdMode(c.getLcdMode());
    }

    /** Restore defaults (reasonable values consistent with Artisan). */
    public void restoreDefaults() {
        setDryEndTempC(DEFAULT_DRY_END_TEMP_C);
        setFcsTempC(DEFAULT_FCS_TEMP_C);
        setAutoAdjustedLimits(DEFAULT_AUTO_ADJUSTED_LIMITS);
        setAutoDRY(DEFAULT_AUTO_DRY);
        setAutoFCs(DEFAULT_AUTO_FCS);
        setFromBackground(DEFAULT_FROM_BACKGROUND);
        setDryingEnterMode(PhaseDisplayMode.TIME);
        setMaillardEnterMode(PhaseDisplayMode.TIME);
        setFinishingEnterMode(PhaseDisplayMode.TIME);
        setFinishingShowAllLcds(DEFAULT_FINISHING_SHOW_ALL_LCDS);
        setLcdMode(PhaseDisplayMode.TIME);
    }

    public static PhasesSettings load() {
        return new PhasesSettings();
    }
}
