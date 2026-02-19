package org.artisan.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.artisan.model.AlarmList;
import org.artisan.model.Calculator;
import org.artisan.model.AxisConfig;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;
import org.artisan.model.CurveSet;
import org.artisan.model.EventEntry;
import org.artisan.model.EventType;
import org.artisan.model.PhaseResult;
import org.artisan.model.Phases;
import org.artisan.model.PhasesConfig;
import org.artisan.model.ProfileData;
import org.artisan.model.RoastStats;
import org.artisan.model.Roastlog;
import org.artisan.model.Sampling;
import org.artisan.model.SamplingConfig;
import org.artisan.model.Statistics;
import org.artisan.view.RoastChartController;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.artisan.device.DevicePort;
import org.artisan.model.ArtisanTime;

/**
 * Main application controller (not JavaFX Application — testable, no UI dependencies).
 * Wires sampling, device, chart, and session; handles event buttons and save/load.
 */
public final class AppController {

  private final RoastSession session;
  private final Sampling sampling;
  private volatile DevicePort device;
  private final RoastChartController chartController;
  private final AxisConfig axisConfig;
  private final ColorConfig colorConfig;
  private final CurveSet curveSet;
  private Consumer<Sample> onSampleConsumer;
  private DisplaySettings displaySettings;
  private PhasesSettings phasesSettings;
  private Consumer<StatisticsUpdate> statisticsUpdateConsumer;
  private boolean autoDryTriggered;
  private boolean autoFcsTriggered;
  private AlarmEngine alarmEngine;
  private FileSession fileSession;
  private AutoSave autoSave;
  private EventReplay eventReplay;
  private SamplingConfig samplingConfig;
  private CommController commController;
  private double lastSampleBt = Double.NaN;
  private double lastSampleTimeSec = Double.NaN;
  private static final Logger LOG = Logger.getLogger(AppController.class.getName());

  public AppController(
      RoastSession session,
      Sampling sampling,
      DevicePort device,
      RoastChartController chartController,
      AxisConfig axisConfig,
      ColorConfig colorConfig,
      CurveSet curveSet) {
    this.session = session;
    this.sampling = sampling;
    this.device = device;
    this.chartController = chartController; // may be null in tests
    this.axisConfig = axisConfig;
    this.colorConfig = colorConfig;
    this.curveSet = curveSet;
    AlarmList alarms = session.getAlarms();
    AlarmList loaded = AlarmListPersistence.load();
    alarms.setAlarms(loaded.getAlarms());
    this.alarmEngine = new AlarmEngine(alarms, () -> {
      if (chartController != null) chartController.updateChart();
    });
    this.eventReplay = new EventReplay();
  }

  public EventReplay getEventReplay() {
    return eventReplay;
  }

  public AlarmEngine getAlarmEngine() {
    return alarmEngine;
  }

  /**
   * Reloads alarms from ~/.artisan/alarms.json into the session and reuses the same AlarmEngine.
   * Call after AlarmsDialog OK.
   */
  public void reloadAlarmsFromFile() {
    AlarmList loaded = AlarmListPersistence.load();
    session.getAlarms().setAlarms(loaded.getAlarms());
  }

  /**
   * Sets the callback for alarm action MARK_EVENT (event name). MainWindow wires to chart.
   */
  public void setMarkEventCallback(java.util.function.Consumer<String> callback) {
    if (alarmEngine != null) alarmEngine.setMarkEventCallback(callback);
  }

  /**
   * Sets the callback for alarm action SET_BURNER (percentage 0–100). MainWindow wires to slider/status.
   */
  public void setBurnerCallback(java.util.function.Consumer<Double> callback) {
    if (alarmEngine != null) alarmEngine.setBurnerCallback(callback);
  }

  public RoastSession getSession() {
    return session;
  }

  public Sampling getSampling() {
    return sampling;
  }

  /** Sets the device used for sampling (e.g. after DeviceSettingsDialog OK). */
  public void setDevice(DevicePort device) {
    this.device = device != null ? device : new org.artisan.device.StubDevice();
  }

  /** May be null in tests. */
  public RoastChartController getChartController() {
    return chartController;
  }

  public ColorConfig getColorConfig() {
    return colorConfig;
  }

  /**
   * Sets the consumer invoked each sample (e.g. to run chart update on JavaFX thread).
   * If null, no callback is invoked.
   */
  public void setOnSampleConsumer(Consumer<Sample> onSampleConsumer) {
    this.onSampleConsumer = onSampleConsumer;
  }

  public void setDisplaySettings(DisplaySettings displaySettings) {
    this.displaySettings = displaySettings;
  }

  public void setPhasesSettings(PhasesSettings phasesSettings) {
    this.phasesSettings = phasesSettings;
  }

  public void setFileSession(FileSession fileSession) {
    this.fileSession = fileSession;
  }

  public void setAutoSave(AutoSave autoSave) {
    this.autoSave = autoSave;
  }

  public void setSamplingConfig(SamplingConfig samplingConfig) {
    this.samplingConfig = samplingConfig;
  }

  /** Sets the CommController used for the sampling loop (Config » Ports). MainWindow injects it. */
  public void setCommController(CommController commController) {
    this.commController = commController;
  }

  public CommController getCommController() {
    return commController;
  }

  /**
   * Accepts a sample from CommController (Serial/Modbus/BLE). Applies spike filter,
   * then invokes onSampleConsumer (chart update + afterSample) if the sample is accepted.
   */
  public void acceptSampleFromComm(double timeSec, double bt, double et) {
    if (samplingConfig != null && samplingConfig.isFilterSpikes()) {
      if (Double.isFinite(lastSampleBt) && Double.isFinite(lastSampleTimeSec)) {
        double dt = timeSec - lastSampleTimeSec;
        if (dt > 0) {
          double ratePerSec = Math.abs(bt - lastSampleBt) / dt;
          if (ratePerSec > samplingConfig.getSpikeThreshold()) {
            LOG.log(Level.WARNING, "Spike filter: rejecting sample BT={0} (rate {1} °C/s > threshold {2})",
                new Object[] { bt, ratePerSec, samplingConfig.getSpikeThreshold() });
            return;
          }
        }
      }
    }
    lastSampleBt = bt;
    lastSampleTimeSec = timeSec;
    Sample s = new Sample(timeSec, bt, et);
    if (fileSession != null) fileSession.markDirty();
    if (onSampleConsumer != null) {
      onSampleConsumer.accept(s);
    }
  }

  /** Returns the sampling interval in seconds (from SamplingConfig or Sampling fallback). */
  public double getSamplingInterval() {
    if (samplingConfig != null) return samplingConfig.getIntervalSeconds();
    return sampling.getDelayMs() / 1000.0;
  }

  /**
   * Sets the consumer invoked after each sample with computed statistics (for StatisticsPanel).
   */
  public void setStatisticsUpdateConsumer(Consumer<StatisticsUpdate> consumer) {
    this.statisticsUpdateConsumer = consumer;
  }

  /**
   * Starts the sampling loop: each tick reads device, builds Sample, invokes onSampleConsumer
   * (and evaluates alarms). When CommController is set, uses it (Serial/Modbus/BLE); otherwise
   * uses the legacy DevicePort + Sampling timer.
   */
  public void startSampling() {
    session.start();
    lastSampleBt = Double.NaN;
    lastSampleTimeSec = Double.NaN;
    if (commController != null && commController.getActiveChannel() != null) {
      commController.start(getSamplingInterval());
      return;
    }
    if (device != null && !device.isConnected()) {
      device.connect();
    }
    int rateMs = samplingConfig != null ? samplingConfig.getIntervalMs() : sampling.getDelayMs();
    sampling.setSamplingRate(rateMs);
    sampling.start(() -> {
      double[] temps = device.readTemperatures();
      if (temps.length >= 2) {
        double timeSec = sampling.getElapsedMs() / 1000.0;
        double et = temps[0];
        double bt = temps[1];
        if (samplingConfig != null && samplingConfig.isFilterSpikes()) {
          if (Double.isFinite(lastSampleBt) && Double.isFinite(lastSampleTimeSec)) {
            double dt = timeSec - lastSampleTimeSec;
            if (dt > 0) {
              double ratePerSec = Math.abs(bt - lastSampleBt) / dt;
              if (ratePerSec > samplingConfig.getSpikeThreshold()) {
                LOG.log(Level.WARNING, "Spike filter: rejecting sample BT={0} (rate {1} °C/s > threshold {2})",
                    new Object[] { bt, ratePerSec, samplingConfig.getSpikeThreshold() });
                return;
              }
            }
          }
        }
        Sample s = new Sample(timeSec, bt, et);
        lastSampleBt = bt;
        lastSampleTimeSec = timeSec;
        if (fileSession != null) fileSession.markDirty();
        if (onSampleConsumer != null) {
          onSampleConsumer.accept(s);
        }
      }
    });
  }

  public void stopSampling() {
    if (commController != null && commController.isRunning()) {
      commController.stop();
    }
    sampling.stop();
    if (autoSave != null) autoSave.stop();
    if (device != null && device.isConnected()) {
      device.disconnect();
    }
  }

  /** Called when user presses CHARGE; records only if session is active. */
  public void onChargeButton() {
    if (!session.isActive()) return;
    autoDryTriggered = false;
    autoFcsTriggered = false;
    if (alarmEngine != null) alarmEngine.reset();
    if (eventReplay != null) eventReplay.reset();
    int idx = currentTimexIndex();
    session.markCharge(idx);
    if (fileSession != null) fileSession.markDirty();
    if (chartController != null) {
      chartController.resetLiveRor();
    }
    if (autoSave != null) {
      autoSave.start(this::buildProfileData, () -> {
        ProfileData p = buildProfileData();
        return p != null && p.getTitle() != null && !p.getTitle().isBlank() ? p.getTitle() : "roast";
      });
    }
  }

  public void onDryEndButton() {
    if (!session.isActive()) return;
    session.markDryEnd(currentTimexIndex());
    if (fileSession != null) fileSession.markDirty();
  }

  public void onFcStartButton() {
    if (!session.isActive()) return;
    session.markFcStart(currentTimexIndex());
    if (fileSession != null) fileSession.markDirty();
  }

  public void onFcEndButton() {
    if (!session.isActive()) return;
    session.markFcEnd(currentTimexIndex());
    if (fileSession != null) fileSession.markDirty();
  }

  public void onDropButton() {
    if (!session.isActive()) return;
    session.markDrop(currentTimexIndex());
    if (fileSession != null) fileSession.markDirty();
    if (autoSave != null) {
      autoSave.onDrop();
      autoSave.stop();
    }
  }

  public void onCoolEndButton() {
    if (!session.isActive()) return;
    session.markCoolEnd(currentTimexIndex());
    if (fileSession != null) fileSession.markDirty();
  }

  /**
   * Adds a custom event (e.g. from a programmable event button).
   * Creates EventEntry, adds to session event list, marks dirty, triggers alarm check, updates chart.
   */
  public void addCustomEvent(EventType type, double value, int timexIndex, double bt, String label) {
    if (type == null) type = EventType.CUSTOM;
    String l = label != null && !label.isBlank() ? label : type.name();
    session.getEvents().add(new EventEntry(timexIndex, bt, l, type, value));
    if (fileSession != null) fileSession.markDirty();
    ProfileData profile = buildProfileData();
    PhasesConfig phasesConfig = phasesSettings != null ? phasesSettings.toConfig() : null;
    if (alarmEngine != null) {
      int idx = Math.max(0, session.getCanvasData().getTimex().size() - 1);
      double timeSec = idx < profile.getTimex().size() ? profile.getTimex().get(idx) : 0.0;
      double et = idx < profile.getTemp1().size() ? profile.getTemp1().get(idx) : 0.0;
      double rorBt = idx < profile.getDelta2().size() && profile.getDelta2() != null
          ? profile.getDelta2().get(idx) : 0.0;
      alarmEngine.evaluate(timeSec, bt, et, rorBt, profile, phasesConfig);
    }
    if (chartController != null) chartController.updateChart();
  }

  private int currentTimexIndex() {
    return Math.max(0, session.getCanvasData().getTimex().size() - 1);
  }

  /**
   * Called after each sample (e.g. from the same callback that invokes chartController.onSample).
   * Runs AutoDRY/AutoFCs when thresholds are crossed (once per CHARGE), then computes stats and
   * invokes statisticsUpdateConsumer if set.
   */
  public void afterSample(Sample s) {
    int idx = currentTimexIndex();
    if (phasesSettings != null) {
      if (phasesSettings.isAutoDRY() && !autoDryTriggered && s.bt() >= phasesSettings.getDryEndTempC()) {
        session.markDryEnd(idx);
        autoDryTriggered = true;
      }
      if (phasesSettings.isAutoFCs() && !autoFcsTriggered && s.bt() >= phasesSettings.getFcsTempC()) {
        session.markFcStart(idx);
        autoFcsTriggered = true;
      }
    }
    ProfileData profile = buildProfileData();
    RoastStats stats = Statistics.compute(profile);
    PhasesConfig config = phasesSettings != null ? phasesSettings.toConfig() : null;
    PhaseResult phase = Phases.compute(profile, config);
    double dtr = Calculator.developmentTimeRatio(phase);
    double auc = displaySettings != null ? Calculator.areaUnderCurve(profile, displaySettings.getAucBaseTemp()) : 0.0;
    double baseTempC = displaySettings != null ? displaySettings.getAucBaseTemp() : Double.NaN;
    if (statisticsUpdateConsumer != null) {
      statisticsUpdateConsumer.accept(new StatisticsUpdate(stats, phase, dtr, auc, baseTempC));
    }
    double rorBt = 0.0;
    List<Double> delta2 = session.getCanvasData().getDelta2();
    if (delta2 != null && !delta2.isEmpty()) {
      rorBt = delta2.get(delta2.size() - 1);
    }
    PhasesConfig phasesConfig = phasesSettings != null ? phasesSettings.toConfig() : null;
    if (alarmEngine != null) {
      alarmEngine.evaluate(s.timeSec(), s.bt(), s.et(), rorBt, profile, phasesConfig);
    }
    if (eventReplay != null && eventReplay.isEnabled()) {
      ProfileData bg = null;
      if (chartController != null && chartController.getBackgroundProfile() != null) {
        bg = chartController.getBackgroundProfile().getProfileData();
      }
      if (bg != null) {
        double bt = s.bt();
        eventReplay.checkReplay(s.timeSec(), bg, ev ->
            addCustomEvent(ev.getType(), ev.getValue(), idx, bt, ev.getLabel()));
      }
    }
  }

  /**
   * Saves the current session to a .alog file. Builds ProfileData from session and calls Roastlog.save.
   */
  public void saveProfile(Path path) throws IOException {
    ProfileData profile = buildProfileData();
    Roastlog.save(profile, path);
  }

  /**
   * Loads a profile from file into the session (canvas data and event indices).
   */
  public void loadProfile(Path path) {
    ProfileData profile = Roastlog.load(path);
    if (profile == null) return;
    session.reset();
    CanvasData cd = session.getCanvasData();
    List<Double> timex = profile.getTimex();
    List<Double> temp1 = profile.getTemp1();
    List<Double> temp2 = profile.getTemp2();
    if (timex != null && temp1 != null && temp2 != null) {
      int n = Math.min(timex.size(), Math.min(temp1.size(), temp2.size()));
      for (int i = 0; i < n; i++) {
        cd.addDataPoint(timex.get(i), temp2.get(i), temp1.get(i));
      }
    }
    List<Integer> timeindex = profile.getTimeindex();
    if (timeindex != null && timeindex.size() >= 7) {
      if (timeindex.get(0) >= 0) cd.setChargeIndex(timeindex.get(0));
      if (timeindex.size() > 1 && timeindex.get(1) >= 0) cd.setDryEndIndex(timeindex.get(1));
      if (timeindex.size() > 2 && timeindex.get(2) >= 0) cd.setFcStartIndex(timeindex.get(2));
      if (timeindex.size() > 3 && timeindex.get(3) >= 0) cd.setFcEndIndex(timeindex.get(3));
      if (timeindex.size() > 6 && timeindex.get(6) >= 0) cd.setDropIndex(timeindex.get(6));
    }
    List<Integer> se = profile.getSpecialevents();
    List<Integer> set = profile.getSpecialeventstype();
    List<Double> sev = profile.getSpecialeventsvalue();
    if (se != null && set != null && sev != null) {
      int n = Math.min(se.size(), Math.min(set.size(), sev.size()));
      String[] labels = { "Charge", "Dry End", "FC Start", "FC End", "SC Start", "SC End", "Drop", "Cool End", "Custom" };
      for (int i = 0; i < n; i++) {
        int idx = se.get(i);
        int typeOrd = set.get(i);
        double val = i < sev.size() ? sev.get(i) : 0.0;
        EventType et = typeOrd >= 0 && typeOrd < EventType.values().length
            ? EventType.values()[typeOrd] : EventType.CUSTOM;
        String label = et.ordinal() < labels.length ? labels[et.ordinal()] : "Event";
        double temp = 0.0;
        if (idx >= 0 && idx < cd.getTemp2().size()) temp = cd.getTemp2().get(idx);
        session.getEvents().add(new EventEntry(idx, temp, label, et, val));
      }
    }
    if (chartController != null) {
      chartController.updateChart();
    }
    refreshStatistics();
  }

  /**
   * Recomputes statistics from current session and invokes statisticsUpdateConsumer.
   * Call after profile load or when chart is refreshed.
   */
  public void refreshStatistics() {
    ProfileData profile = buildProfileData();
    RoastStats stats = Statistics.compute(profile);
    PhasesConfig config = phasesSettings != null ? phasesSettings.toConfig() : null;
    PhaseResult phase = Phases.compute(profile, config);
    double dtr = Calculator.developmentTimeRatio(phase);
    double auc = displaySettings != null ? Calculator.areaUnderCurve(profile, displaySettings.getAucBaseTemp()) : 0.0;
    double baseTempC = displaySettings != null ? displaySettings.getAucBaseTemp() : Double.NaN;
    if (statisticsUpdateConsumer != null) {
      statisticsUpdateConsumer.accept(new StatisticsUpdate(stats, phase, dtr, auc, baseTempC));
    }
  }

  private ProfileData buildProfileData() {
    ProfileData p = new ProfileData();
    CanvasData cd = session.getCanvasData();
    p.setTimex(new ArrayList<>(cd.getTimex()));
    p.setTemp1(new ArrayList<>(cd.getTemp1()));
    p.setTemp2(new ArrayList<>(cd.getTemp2()));
    List<Integer> ti = new ArrayList<>();
    ti.add(cd.getChargeIndex());
    ti.add(cd.getDryEndIndex());
    ti.add(cd.getFcStartIndex());
    ti.add(cd.getFcEndIndex());
    ti.add(0);
    ti.add(0);
    ti.add(cd.getDropIndex());
    ti.add(0);
    p.setTimeindex(ti);
    List<Integer> se = new ArrayList<>();
    List<Integer> set = new ArrayList<>();
    List<Double> sev = new ArrayList<>();
    for (EventEntry e : session.getEvents().getAll()) {
      se.add(e.getTimeIndex());
      set.add(e.getType().ordinal());
      sev.add(e.getValue());
    }
    p.setSpecialevents(se);
    p.setSpecialeventstype(set);
    p.setSpecialeventsvalue(sev);
    p.setSamplingInterval(getSamplingInterval());
    return p;
  }
}
