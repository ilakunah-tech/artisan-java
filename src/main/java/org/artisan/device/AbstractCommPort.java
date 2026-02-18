package org.artisan.device;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Base implementation of {@link DevicePort} using jSerialComm for serial/COM access.
 * Handles open/close, port parameters, exception handling, and optional reconnect logic.
 * Subclasses implement device-specific {@link #readTemperatures()}.
 */
public abstract class AbstractCommPort implements DevicePort {

    /** Default baud rate (e.g. 9600). */
    public static final int BAUD_RATE = 9600;

    /** Data bits per word (e.g. 8). */
    public static final int DATA_BITS = 8;

    /** Stop bits: use {@link SerialPort#ONE_STOP_BIT} or {@link SerialPort#TWO_STOP_BITS}. */
    public static final int STOP_BITS = SerialPort.ONE_STOP_BIT;

    /** Parity: use {@link SerialPort#NO_PARITY}, {@link SerialPort#ODD_PARITY}, {@link SerialPort#EVEN_PARITY}. */
    public static final int PARITY = SerialPort.ODD_PARITY;

    /** Read timeout in milliseconds. */
    public static final int READ_TIMEOUT_MS = 400;

    /** Number of reconnect attempts after open failure. */
    private static final int RECONNECT_ATTEMPTS = 2;

    /** Delay between reconnect attempts (ms). */
    private static final int RECONNECT_DELAY_MS = 100;

    private final String portName;
    private SerialPort serialPort;

    protected AbstractCommPort(String portName) {
        this.portName = portName;
        this.serialPort = null;
    }

    /**
     * Returns the system port name (e.g. "COM4", "/dev/ttyUSB0").
     */
    protected final String getPortName() {
        return portName;
    }

    /**
     * Returns the underlying serial port, or null if not connected.
     */
    protected final SerialPort getSerialPort() {
        return serialPort;
    }

    @Override
    public void connect() {
        if (serialPort != null && serialPort.isOpen()) {
            return;
        }
        SerialPort port = SerialPort.getCommPort(portName);
        configurePort(port);
        Exception lastException = null;
        for (int attempt = 0; attempt <= RECONNECT_ATTEMPTS; attempt++) {
            if (attempt > 0) {
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CommException("Interrupted during reconnect", e);
                }
            }
            if (port.openPort()) {
                this.serialPort = port;
                return;
            }
            lastException = new IllegalStateException(
                "openPort() returned false: " + port.getLastErrorCode() + " at " + port.getLastErrorLocation());
        }
        if (port.isOpen()) {
            port.closePort();
        }
        throw new CommException("Unable to open serial port: " + portName, lastException);
    }

    @Override
    public void disconnect() {
        if (serialPort == null) {
            return;
        }
        try {
            if (serialPort.isOpen()) {
                serialPort.closePort();
            }
        } finally {
            serialPort = null;
        }
    }

    @Override
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }

    @Override
    public double[] readTemperatures() {
        if (!isConnected()) {
            return new double[0];
        }
        return readTemperaturesImpl();
    }

    /**
     * Device-specific temperature read. Called only when port is open.
     *
     * @return array of temperatures (e.g. ET, BT); never null
     * @throws CommException if read fails
     */
    protected abstract double[] readTemperaturesImpl();

    /**
     * Applies default port parameters (baud, data bits, stop bits, parity, timeout).
     */
    protected void configurePort(SerialPort port) {
        port.setBaudRate(BAUD_RATE);
        port.setNumDataBits(DATA_BITS);
        port.setNumStopBits(STOP_BITS);
        port.setParity(PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, READ_TIMEOUT_MS, READ_TIMEOUT_MS);
    }
}
