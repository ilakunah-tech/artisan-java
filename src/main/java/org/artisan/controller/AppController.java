package org.artisan.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.artisan.model.AlarmList;
import org.artisan.model.AxisConfig;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;
import org.artisan.model.CurveSet;
import org.artisan.model.EventEntry;
import org.artisan.model.EventType;
import org.artisan.model.ProfileData;
import org.artisan.model.Roastlog;
import org.artisan.model.Sampling;
import org.artisan.view.RoastChartController;

import org.artisan.device.DevicePort;
import org.artisan.model.ArtisanTime;

/**
 * Main application controller (not JavaFX Application — testable, no UI dependencies).
 * Wires sampling, device, chart, and session; handles event buttons and save/load.
 */
public final class AppController {

  private final RoastSession session;
  private final Sampling sampling;
  private final DevicePort device;
  private final RoastChartController chartController;
  private final AxisConfig axisConfig;
  private final ColorConfig colorConfig;
  private final CurveSet curveSet;
  private Consumer<Sample> onSampleConsumer;

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
  }

  public RoastSession getSession() {
    return session;
  }

  public Sampling getSampling() {
    return sampling;
  }

  /** May be null in tests. */
  public RoastChartController getChartController() {
    return chartController;
  }

  /**
   * Sets the consumer invoked each sample (e.g. to run chart update on JavaFX thread).
   * If null, no callback is invoked.
   */
  public void setOnSampleConsumer(Consumer<Sample> onSampleConsumer) {
    this.onSampleConsumer = onSampleConsumer;
  }

  /**
   * Starts the sampling loop: each tick reads device, builds Sample, invokes onSampleConsumer
   * (and evaluates alarms). Set onSampleConsumer via setOnSampleConsumer to update chart on FX thread.
   */
  public void startSampling() {
    session.start();
    sampling.setSamplingRate(sampling.getDelayMs());
    sampling.start(() -> {
      double[] temps = device.readTemperatures();
      if (temps.length >= 2) {
        double timeSec = sampling.getElapsedMs() / 1000.0;
        double et = temps[0];
        double bt = temps[1];
        Sample s = new Sample(timeSec, bt, et);
        if (onSampleConsumer != null) {
          onSampleConsumer.accept(s);
        }
        session.getAlarms().evaluateAll(bt, timeSec, session.getEvents().getAll());
      }
    });
  }

  public void stopSampling() {
    sampling.stop();
  }

  /** Called when user presses CHARGE; records only if session is active. */
  public void onChargeButton() {
    if (!session.isActive()) return;
    int idx = currentTimexIndex();
    session.markCharge(idx);
  }

  public void onDryEndButton() {
    if (!session.isActive()) return;
    session.markDryEnd(currentTimexIndex());
  }

  public void onFcStartButton() {
    if (!session.isActive()) return;
    session.markFcStart(currentTimexIndex());
  }

  public void onFcEndButton() {
    if (!session.isActive()) return;
    session.markFcEnd(currentTimexIndex());
  }

  public void onDropButton() {
    if (!session.isActive()) return;
    session.markDrop(currentTimexIndex());
  }

  public void onCoolEndButton() {
    if (!session.isActive()) return;
    session.markCoolEnd(currentTimexIndex());
  }

  private int currentTimexIndex() {
    return Math.max(0, session.getCanvasData().getTimex().size() - 1);
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
    if (chartController != null) {
      chartController.updateChart();
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
      sev.add(e.getTemp());
    }
    p.setSpecialevents(se);
    p.setSpecialeventstype(set);
    p.setSpecialeventsvalue(sev);
    p.setSamplingInterval(sampling.getDelayMs() / 1000.0);
    return p;
  }
}
