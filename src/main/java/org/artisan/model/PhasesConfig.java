package org.artisan.model;

/**
 * DTO for Phases dialog: manual limits, toggles, per-phase display mode, finishing LCD option.
 * Persisted via PhasesSettings (Preferences keys "phases.*").
 */
public final class PhasesConfig {

    private double dryEndTempC = 150.0;
    private double fcsTempC = 195.0;
    private boolean autoAdjustedLimits = true;
    private boolean autoDRY = false;
    private boolean autoFCs = false;
    private boolean fromBackground = false;
    private PhaseDisplayMode dryingEnterMode = PhaseDisplayMode.TIME;
    private PhaseDisplayMode maillardEnterMode = PhaseDisplayMode.TIME;
    private PhaseDisplayMode finishingEnterMode = PhaseDisplayMode.TIME;
    private boolean finishingShowAllLcds = false;
    private PhaseDisplayMode lcdMode = PhaseDisplayMode.TIME;

    public double getDryEndTempC() { return dryEndTempC; }
    public void setDryEndTempC(double dryEndTempC) { this.dryEndTempC = dryEndTempC; }

    public double getFcsTempC() { return fcsTempC; }
    public void setFcsTempC(double fcsTempC) { this.fcsTempC = fcsTempC; }

    public boolean isAutoAdjustedLimits() { return autoAdjustedLimits; }
    public void setAutoAdjustedLimits(boolean autoAdjustedLimits) { this.autoAdjustedLimits = autoAdjustedLimits; }

    public boolean isAutoDRY() { return autoDRY; }
    public void setAutoDRY(boolean autoDRY) { this.autoDRY = autoDRY; }

    public boolean isAutoFCs() { return autoFCs; }
    public void setAutoFCs(boolean autoFCs) { this.autoFCs = autoFCs; }

    public boolean isFromBackground() { return fromBackground; }
    public void setFromBackground(boolean fromBackground) { this.fromBackground = fromBackground; }

    public PhaseDisplayMode getDryingEnterMode() { return dryingEnterMode; }
    public void setDryingEnterMode(PhaseDisplayMode dryingEnterMode) { this.dryingEnterMode = dryingEnterMode; }

    public PhaseDisplayMode getMaillardEnterMode() { return maillardEnterMode; }
    public void setMaillardEnterMode(PhaseDisplayMode maillardEnterMode) { this.maillardEnterMode = maillardEnterMode; }

    public PhaseDisplayMode getFinishingEnterMode() { return finishingEnterMode; }
    public void setFinishingEnterMode(PhaseDisplayMode finishingEnterMode) { this.finishingEnterMode = finishingEnterMode; }

    public boolean isFinishingShowAllLcds() { return finishingShowAllLcds; }
    public void setFinishingShowAllLcds(boolean finishingShowAllLcds) { this.finishingShowAllLcds = finishingShowAllLcds; }

    public PhaseDisplayMode getLcdMode() { return lcdMode; }
    public void setLcdMode(PhaseDisplayMode lcdMode) { this.lcdMode = lcdMode; }
}
