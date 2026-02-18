package org.artisan.device;

/**
 * Stub DevicePort for testing and when no hardware is connected.
 * Returns ET=0, BT=0; connect/disconnect are no-ops.
 */
public final class StubDevice implements DevicePort {

  @Override
  public void connect() {}

  @Override
  public void disconnect() {}

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public double[] readTemperatures() {
    return new double[] { 0.0, 0.0 };
  }
}
