package org.artisan.controller;

import org.artisan.model.AxisConfig;
import org.artisan.model.CanvasData;
import org.artisan.model.ColorConfig;
import org.artisan.model.CurveSet;
import org.artisan.model.Roastlog;
import org.artisan.model.Sampling;
import org.artisan.device.DevicePort;
import org.artisan.device.StubDevice;
import org.artisan.model.ArtisanTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
