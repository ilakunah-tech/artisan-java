package org.artisan.device;

// Ported from acaia.py — original Python lines: 596–661

/**
 * Acaia scale device (BLE). Implements {@link DevicePort} as a scale: no temperature data.
 * Connection uses BLE in Python; this stub keeps connect/disconnect contract for future BLE implementation.
 */
public class AcaiaScale extends ScaleDevice {

    private final String ident;
    private final String name;
    private boolean scaleConnected;

    public AcaiaScale(String ident, String name) {
        this.ident = ident == null ? "" : ident;
        this.name = name == null ? "" : name;
        this.scaleConnected = false;
    }

    public String getIdent() {
        return ident;
    }

    public String getName() {
        return name;
    }

    @Override
    public void connect() {
        // BLE connect not implemented yet; keep contract
        scaleConnected = false;
    }

    @Override
    public void disconnect() {
        scaleConnected = false;
    }

    @Override
    public boolean isConnected() {
        return scaleConnected;
    }
}
