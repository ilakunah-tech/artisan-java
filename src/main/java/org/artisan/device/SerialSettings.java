package org.artisan.device;

/**
 * Serial port configuration for {@link AsyncCommPort} when using serial instead of TCP.
 */
public final class SerialSettings {

    private final String port;
    private final int baudrate;
    private final int bytesize;
    private final int stopbits;
    private final String parity;
    private final double timeout;

    public SerialSettings(String port, int baudrate, int bytesize, int stopbits, String parity, double timeout) {
        this.port = port;
        this.baudrate = baudrate;
        this.bytesize = bytesize;
        this.stopbits = stopbits;
        this.parity = parity;
        this.timeout = timeout;
    }

    public String getPort() { return port; }
    public int getBaudrate() { return baudrate; }
    public int getBytesize() { return bytesize; }
    public int getStopbits() { return stopbits; }
    public String getParity() { return parity; }
    public double getTimeout() { return timeout; }
}
