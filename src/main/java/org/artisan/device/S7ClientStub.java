package org.artisan.device;

/**
 * Stub S7 client for testing: connect always succeeds, readFloat returns a synthetic
 * value so it behaves like a live sensor.
 * Real S7Client using Moka7 (Snap7 Java wrapper) is left as a TODO.
 */
public final class S7ClientStub implements S7ClientInterface {

    private boolean connected;

    @Override
    public boolean connect(S7Config cfg) {
        connected = true;
        return true;
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public float readFloat(int dbNumber, int offset) {
        // Synthetic value for testing: sine wave around 200°C
        return (float) (Math.sin(System.nanoTime() * 1e-9) * 10 + 200);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}
