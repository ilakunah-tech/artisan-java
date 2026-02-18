package org.artisan.device;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simulates a realistic roast curve for testing without hardware.
 * BT phases: 0-60s preheat, 60-180s drying, 180-360s Maillard, 360-480s development, then flat.
 * ET = BT + 15°C + noise; BT has small noise.
 */
public final class SimulatorDevice implements DevicePort {

  private static final double NOISE_BT = 0.2;
  private static final double NOISE_ET = 0.3;
  private static final double ET_OFFSET = 15.0;

  private final AtomicLong connectTimeMs = new AtomicLong(0);
  private volatile boolean connected;

  @Override
  public void connect() {
    connectTimeMs.set(System.currentTimeMillis());
    connected = true;
  }

  @Override
  public void disconnect() {
    connected = false;
  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  @Override
  public double[] readTemperatures() {
    if (!connected) return new double[] { 0.0, 0.0 };
    double elapsedSec = (System.currentTimeMillis() - connectTimeMs.get()) / 1000.0;
    double bt = computeBT(elapsedSec) + (Math.random() * 2 - 1) * NOISE_BT;
    double et = bt + ET_OFFSET + (Math.random() * 2 - 1) * NOISE_ET;
    return new double[] { et, bt };
  }

  private static double computeBT(double t) {
    if (t <= 0) return 25.0;
    if (t <= 60) return 25.0 + (150.0 - 25.0) * (t / 60.0);
    if (t <= 180) return 150.0 + (165.0 - 150.0) * ((t - 60) / 120.0);
    if (t <= 360) return 165.0 + (195.0 - 165.0) * ((t - 180) / 180.0);
    if (t <= 480) return 195.0 + (210.0 - 195.0) * ((t - 360) / 120.0);
    return 210.0;
  }
}
