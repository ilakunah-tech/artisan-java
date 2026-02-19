package org.artisan.controller;

import org.artisan.model.AxisConfig;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;
import org.artisan.model.CurveSet;
import org.artisan.model.Roastlog;
import org.artisan.model.Sampling;
import org.artisan.device.DevicePort;
import org.artisan.device.SimulatorDevice;
import org.artisan.device.StubDevice;
import org.artisan.model.ArtisanTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppControllerTest {

  @TempDir
  Path tempDir;

  private RoastSession session;
  private Sampling sampling;
  private DevicePort device;
  private AppController appController;

  @BeforeEach
  void setUp() {
    session = new RoastSession();
    ArtisanTime timeclock = new ArtisanTime();
    sampling = new Sampling(timeclock);
    device = new StubDevice();
    appController = new AppController(
        session, sampling, device, null, new AxisConfig(), new ColorConfig(), CurveSet.createDefault());
  }

  @Test
  void startSamplingSetsSamplingRunning() {
    assertFalse(sampling.isRunning());
    appController.startSampling();
    assertTrue(sampling.isRunning());
    appController.stopSampling();
    assertFalse(sampling.isRunning());
  }

  @Test
  void stopSamplingStopsIt() {
    appController.startSampling();
    assertTrue(sampling.isRunning());
    appController.stopSampling();
    assertFalse(sampling.isRunning());
  }

  @Test
  void onChargeButtonWhenSessionNotActiveAddsNoEvent() {
    assertFalse(session.isActive());
    appController.onChargeButton();
    assertEquals(0, session.getEvents().size());
  }

  @Test
  void onChargeButtonWhenActiveAddsEvent() {
    session.start();
    session.getCanvasData().addDataPoint(0.0, 100.0, 200.0);
    appController.onChargeButton();
    assertEquals(1, session.getEvents().size());
  }

  @Test
  void startSamplingWithSimulatorDeviceAddsPointsAndCallsConsumer() throws InterruptedException {
    appController.setDevice(new SimulatorDevice());
    appController.getSampling().setSamplingRate(100);
    AtomicInteger callCount = new AtomicInteger(0);
    CountDownLatch threeCalls = new CountDownLatch(3);
    appController.setOnSampleConsumer(s -> {
      session.getCanvasData().addDataPoint(s.timeSec(), s.bt(), s.et());
      if (callCount.incrementAndGet() <= 3) {
        threeCalls.countDown();
      }
    });
    appController.startSampling();
    boolean completed = threeCalls.await(5, TimeUnit.SECONDS);
    appController.stopSampling();
    assertTrue(completed, "Expected at least 3 sample callbacks");
    assertTrue(callCount.get() >= 3, "onSampleConsumer should be called at least 3 times");
    assertTrue(session.getCanvasData().getTimex().size() >= 3,
        "canvasData should have at least 3 points, got " + session.getCanvasData().getTimex().size());
  }

  @Test
  void saveProfileCallsRoastlogSave() throws IOException {
    session.start();
    session.getCanvasData().addDataPoint(0.0, 100.0, 200.0);
    session.getCanvasData().addDataPoint(60.0, 150.0, 220.0);
    Path path = tempDir.resolve("test.alog");
    appController.saveProfile(path);
    assertTrue(Files.exists(path));
    var loaded = Roastlog.load(path);
    assertTrue(loaded != null);
    assertEquals(2, loaded.getTimex().size());
  }

  @Test
  void autoDRY_calledOnceWhenThresholdCrossed_notAgainOnSubsequentSamples() {
    PhasesSettings phasesSettings = PhasesSettings.load();
    phasesSettings.setAutoDRY(true);
    phasesSettings.setDryEndTempC(150.0);
    appController.setPhasesSettings(phasesSettings);
    appController.setDisplaySettings(DisplaySettings.load());

    session.start();
    session.getCanvasData().addDataPoint(0.0, 100.0, 80.0);
    appController.onChargeButton();
    assertTrue(session.getCanvasData().getDryEndIndex() < 0);

    session.getCanvasData().addDataPoint(1.0, 151.0, 120.0);
    appController.afterSample(new Sample(1.0, 151.0, 120.0));
    assertEquals(1, session.getCanvasData().getDryEndIndex());

    session.getCanvasData().addDataPoint(2.0, 160.0, 125.0);
    appController.afterSample(new Sample(2.0, 160.0, 125.0));
    assertEquals(1, session.getCanvasData().getDryEndIndex());
  }

  @Test
  void autoFCs_calledOnceWhenThresholdCrossed_notAgainOnSubsequentSamples() {
    PhasesSettings phasesSettings = PhasesSettings.load();
    phasesSettings.setAutoDRY(false);
    phasesSettings.setAutoFCs(true);
    phasesSettings.setFcsTempC(195.0);
    appController.setPhasesSettings(phasesSettings);
    appController.setDisplaySettings(DisplaySettings.load());

    session.start();
    session.getCanvasData().addDataPoint(0.0, 100.0, 80.0);
    appController.onChargeButton();
    session.getCanvasData().addDataPoint(1.0, 180.0, 150.0);
    assertTrue(session.getCanvasData().getFcStartIndex() < 0);

    session.getCanvasData().addDataPoint(2.0, 196.0, 200.0);
    appController.afterSample(new Sample(2.0, 196.0, 200.0));
    assertEquals(2, session.getCanvasData().getFcStartIndex());

    session.getCanvasData().addDataPoint(3.0, 210.0, 205.0);
    appController.afterSample(new Sample(3.0, 210.0, 205.0));
    assertEquals(2, session.getCanvasData().getFcStartIndex());
  }
}
