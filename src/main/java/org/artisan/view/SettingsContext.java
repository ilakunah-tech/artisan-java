package org.artisan.view;

import org.artisan.controller.AppController;
import org.artisan.controller.AppSettings;
import org.artisan.controller.CommController;
import org.artisan.controller.DisplaySettings;
import org.artisan.controller.PhasesSettings;
import org.artisan.controller.RoastStateMachine;
import org.artisan.controller.AutoSave;
import org.artisan.controller.BackgroundSettings;
import org.artisan.device.AillioR1Config;
import org.artisan.device.BlePortConfig;
import org.artisan.device.DeviceConfig;
import org.artisan.device.ModbusPortConfig;
import org.artisan.device.SerialPortConfig;
import org.artisan.device.SimulatorConfig;
import org.artisan.model.AxisConfig;
import org.artisan.model.ColorConfig;
import org.artisan.model.SamplingConfig;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;
import org.artisan.view.RoastChartController;

/**
 * Holds all config and controller references needed to build the unified Settings dialog
 * and its embedded tabs (Device, Ports, Curves, Events, Colors, Phases, etc.).
 */
public final class SettingsContext {

    private final javafx.stage.Window owner;
    private final AppController appController;
    private final AppSettings appSettings;
    private final DisplaySettings displaySettings;
    private final AxisConfig axisConfig;
    private final RoastStateMachine roastStateMachine;
    private final PhasesSettings phasesSettings;
    private final BackgroundSettings backgroundSettings;
    private final AutoSave autoSave;
    private final SamplingConfig samplingConfig;
    private final SerialPortConfig serialPortConfig;
    private final ModbusPortConfig modbusPortConfig;
    private final BlePortConfig blePortConfig;
    private final DeviceConfig deviceConfig;
    private final SimulatorConfig simulatorConfig;
    private final AillioR1Config aillioR1Config;
    private final CommController commController;
    private final ColorConfig colorConfig;
    private final RoastChartController chartController;
    private final UIPreferences uiPreferences;
    private final PreferencesStore preferencesStore;
    private Runnable onSettingsApplied;

    private SettingsContext(Builder b) {
        this.owner = b.owner;
        this.appController = b.appController;
        this.appSettings = b.appSettings;
        this.displaySettings = b.displaySettings;
        this.axisConfig = b.axisConfig;
        this.roastStateMachine = b.roastStateMachine;
        this.phasesSettings = b.phasesSettings;
        this.backgroundSettings = b.backgroundSettings;
        this.autoSave = b.autoSave;
        this.samplingConfig = b.samplingConfig;
        this.serialPortConfig = b.serialPortConfig;
        this.modbusPortConfig = b.modbusPortConfig;
        this.blePortConfig = b.blePortConfig;
        this.deviceConfig = b.deviceConfig;
        this.simulatorConfig = b.simulatorConfig;
        this.aillioR1Config = b.aillioR1Config;
        this.commController = b.commController;
        this.colorConfig = b.colorConfig;
        this.chartController = b.chartController;
        this.uiPreferences = b.uiPreferences;
        this.preferencesStore = b.preferencesStore;
        this.onSettingsApplied = b.onSettingsApplied;
    }

    public static Builder builder() {
        return new Builder();
    }

    public javafx.stage.Window getOwner() { return owner; }
    public AppController getAppController() { return appController; }
    public AppSettings getAppSettings() { return appSettings; }
    public DisplaySettings getDisplaySettings() { return displaySettings; }
    public AxisConfig getAxisConfig() { return axisConfig; }
    public RoastStateMachine getRoastStateMachine() { return roastStateMachine; }
    public PhasesSettings getPhasesSettings() { return phasesSettings; }
    public BackgroundSettings getBackgroundSettings() { return backgroundSettings; }
    public AutoSave getAutoSave() { return autoSave; }
    public SamplingConfig getSamplingConfig() { return samplingConfig; }
    public SerialPortConfig getSerialPortConfig() { return serialPortConfig; }
    public ModbusPortConfig getModbusPortConfig() { return modbusPortConfig; }
    public BlePortConfig getBlePortConfig() { return blePortConfig; }
    public DeviceConfig getDeviceConfig() { return deviceConfig; }
    public SimulatorConfig getSimulatorConfig() { return simulatorConfig; }
    public AillioR1Config getAillioR1Config() { return aillioR1Config; }
    public CommController getCommController() { return commController; }
    public ColorConfig getColorConfig() { return colorConfig; }
    public RoastChartController getChartController() { return chartController; }
    public UIPreferences getUiPreferences() { return uiPreferences; }
    public PreferencesStore getPreferencesStore() { return preferencesStore; }
    public Runnable getOnSettingsApplied() { return onSettingsApplied; }

    public static final class Builder {
        private javafx.stage.Window owner;
        private AppController appController;
        private AppSettings appSettings;
        private DisplaySettings displaySettings;
        private AxisConfig axisConfig;
        private RoastStateMachine roastStateMachine;
        private PhasesSettings phasesSettings;
        private BackgroundSettings backgroundSettings;
        private AutoSave autoSave;
        private SamplingConfig samplingConfig;
        private SerialPortConfig serialPortConfig;
        private ModbusPortConfig modbusPortConfig;
        private BlePortConfig blePortConfig;
        private DeviceConfig deviceConfig;
        private SimulatorConfig simulatorConfig;
        private AillioR1Config aillioR1Config;
        private CommController commController;
        private ColorConfig colorConfig;
        private RoastChartController chartController;
        private UIPreferences uiPreferences;
        private PreferencesStore preferencesStore;
        private Runnable onSettingsApplied;

        public Builder owner(javafx.stage.Window v) { this.owner = v; return this; }
        public Builder appController(AppController v) { this.appController = v; return this; }
        public Builder appSettings(AppSettings v) { this.appSettings = v; return this; }
        public Builder displaySettings(DisplaySettings v) { this.displaySettings = v; return this; }
        public Builder axisConfig(AxisConfig v) { this.axisConfig = v; return this; }
        public Builder roastStateMachine(RoastStateMachine v) { this.roastStateMachine = v; return this; }
        public Builder phasesSettings(PhasesSettings v) { this.phasesSettings = v; return this; }
        public Builder backgroundSettings(BackgroundSettings v) { this.backgroundSettings = v; return this; }
        public Builder autoSave(AutoSave v) { this.autoSave = v; return this; }
        public Builder samplingConfig(SamplingConfig v) { this.samplingConfig = v; return this; }
        public Builder serialPortConfig(SerialPortConfig v) { this.serialPortConfig = v; return this; }
        public Builder modbusPortConfig(ModbusPortConfig v) { this.modbusPortConfig = v; return this; }
        public Builder blePortConfig(BlePortConfig v) { this.blePortConfig = v; return this; }
        public Builder deviceConfig(DeviceConfig v) { this.deviceConfig = v; return this; }
        public Builder simulatorConfig(SimulatorConfig v) { this.simulatorConfig = v; return this; }
        public Builder aillioR1Config(AillioR1Config v) { this.aillioR1Config = v; return this; }
        public Builder commController(CommController v) { this.commController = v; return this; }
        public Builder colorConfig(ColorConfig v) { this.colorConfig = v; return this; }
        public Builder chartController(RoastChartController v) { this.chartController = v; return this; }
        public Builder uiPreferences(UIPreferences v) { this.uiPreferences = v; return this; }
        public Builder preferencesStore(PreferencesStore v) { this.preferencesStore = v; return this; }
        public Builder onSettingsApplied(Runnable v) { this.onSettingsApplied = v; return this; }

        public SettingsContext build() {
            return new SettingsContext(this);
        }
    }
}
