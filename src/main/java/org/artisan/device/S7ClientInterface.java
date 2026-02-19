package org.artisan.device;

/**
 * Abstraction for S7 PLC connection (real Moka7/Snap7 or stub for testing).
 */
public interface S7ClientInterface {

    /**
     * Connects to the PLC using the given config.
     *
     * @return true if connected successfully
     */
    boolean connect(S7Config cfg);

    /** Disconnects from the PLC. Idempotent. */
    void disconnect();

    /**
     * Reads a 4-byte float from the given data block at byte offset.
     *
     * @param dbNumber data block number
     * @param offset   byte offset within the block
     * @return float value (undefined if not connected)
     */
    float readFloat(int dbNumber, int offset);

    /** Returns true if currently connected. */
    boolean isConnected();
}
