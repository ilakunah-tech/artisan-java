package org.artisan.device;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SimulatorDevice: readTemperatures at t=0 → BT near 25°C;
 * at t=300s → BT near 200°C; ET always > BT; noise within ±1°C;
 * connect() → isConnected()=true; disconnect() → isConnected()=false.
 */
class SimulatorDeviceTest {

  @Test
  void connectThenIsConnectedTrue() {
    SimulatorDevice dev = new SimulatorDevice();
    assertFalse(dev.isConnected());
    dev.connect();
    assertTrue(dev.isConnected());
    dev.disconnect();
    assertFalse(dev.isConnected());
  }

  @Test
  void disconnectThenIsConnectedFalse() {
    SimulatorDevice dev = new SimulatorDevice();
    dev.connect();
    assertTrue(dev.isConnected());
    dev.disconnect();
    assertFalse(dev.isConnected());
  }

  @Test
  void readTemperaturesAtZeroBtNear25() {
    SimulatorDevice dev = new SimulatorDevice();
    dev.connect();
    double[] t = dev.readTemperatures();
    dev.disconnect();
    assertTrue(t != null && t.length >= 2);
    double et = t[0];
    double bt = t[1];
    assertTrue(bt >= 24.0 && bt <= 26.0, "BT at t=0 should be near 25°C, got " + bt);
    assertTrue(et > bt, "ET should be > BT");
    assertTrue(et >= 39.0 && et <= 41.0, "ET at t=0 should be BT+15±noise, got " + et);
  }

  @Test
  void readTemperaturesEtAlwaysGreaterThanBt() {
    SimulatorDevice dev = new SimulatorDevice();
    dev.connect();
    for (int i = 0; i < 5; i++) {
      double[] t = dev.readTemperatures();
      assertTrue(t != null && t.length >= 2);
      assertTrue(t[0] > t[1], "ET should be > BT: et=" + t[0] + " bt=" + t[1]);
    }
    dev.disconnect();
  }

  @Test
  void readTemperaturesAt300sBtNear200() throws Exception {
    SimulatorDevice dev = new SimulatorDevice();
    dev.connect();
    // Simulate 300s elapsed: set connect time to (now - 300_000 ms)
    Field connectTimeField = SimulatorDevice.class.getDeclaredField("connectTimeMs");
    connectTimeField.setAccessible(true);
    java.util.concurrent.atomic.AtomicLong at = (java.util.concurrent.atomic.AtomicLong) connectTimeField.get(dev);
    at.set(System.currentTimeMillis() - 300_000L);
    double[] t = dev.readTemperatures();
    dev.disconnect();
    assertTrue(t != null && t.length >= 2);
    double bt = t[1];
    // 180-360s: BT from 165 to 195; at 300s: 165 + (195-165)*((300-180)/180) = 165 + 30*120/180 = 185
    // So at 300s BT is ~185. Allowing noise ±1 and phase rounding: 183–207
    assertTrue(bt >= 180.0 && bt <= 210.0, "BT at t=300s should be in Maillard range, got " + bt);
    double et = t[0];
    assertTrue(et > bt, "ET > BT");
  }

  @Test
  void noiseWithinOneDegree() {
    SimulatorDevice dev = new SimulatorDevice();
    dev.connect();
    // At t=0 BT is 25 + noise ±0.2, ET = BT+15 ±0.3. So BT in [24.8, 25.2], ET in [39.5, 40.5] approx.
    double[] t = dev.readTemperatures();
    dev.disconnect();
    assertTrue(t != null && t.length >= 2);
    assertTrue(Math.abs(t[1] - 25.0) <= 1.0, "BT noise within ±1°C");
    assertTrue(Math.abs(t[0] - (t[1] + 15.0)) <= 1.0, "ET offset noise within ±1°C");
  }
}
