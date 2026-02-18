package org.artisan.device;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Diedrich roaster device: Modbus RTU over serial, 9600 8N1.
 * BT = input register 1, ET = input register 2 (function 4), scale value/10.0 → °C.
 */
public final class DiedrichDevice implements DevicePort {

    private static final Logger LOG = Logger.getLogger(DiedrichDevice.class.getName());
    private static final int DEFAULT_SLAVE_ID = 1;
    private static final int DEFAULT_BT_REGISTER = 1;
    private static final int DEFAULT_ET_REGISTER = 2;
    private static final double DEFAULT_SCALE_FACTOR = 10.0;
    private static final int DEFAULT_BAUD = 9600;
    private static final double STALE_MIN_C = -50.0;
    private static final double STALE_MAX_C = 500.0;
    private static final int FAILURES_BEFORE_RECONNECT = 3;
    private static final int RECONNECT_DELAY_MS = 2000;

    private final String serialPort;
    private final int baudRate;
    private final int slaveId;
    private final int btRegister;
    private final int etRegister;
    private final double scaleFactor;

    private ModbusPort modbusPort;
    private volatile double lastBt;
    private volatile double lastEt;
    private int consecutiveFailures;

    private DiedrichDevice(String serialPort, int baudRate, int slaveId, int btRegister, int etRegister, double scaleFactor) {
        this.serialPort = serialPort != null ? serialPort : "";
        this.baudRate = baudRate > 0 ? baudRate : DEFAULT_BAUD;
        this.slaveId = slaveId >= 1 && slaveId <= 247 ? slaveId : DEFAULT_SLAVE_ID;
        this.btRegister = btRegister >= 0 ? btRegister : DEFAULT_BT_REGISTER;
        this.etRegister = etRegister >= 0 ? etRegister : DEFAULT_ET_REGISTER;
        this.scaleFactor = scaleFactor > 0 ? scaleFactor : DEFAULT_SCALE_FACTOR;
    }

    /**
     * RTU mode: connect over serial (9600 8N1 by default).
     */
    public static DiedrichDevice rtuMode(String serialPort) {
        return new DiedrichDevice(serialPort, DEFAULT_BAUD, DEFAULT_SLAVE_ID, DEFAULT_BT_REGISTER, DEFAULT_ET_REGISTER, DEFAULT_SCALE_FACTOR);
    }

    public DiedrichDevice withBaudRate(int baud) {
        return new DiedrichDevice(serialPort, baud, slaveId, btRegister, etRegister, scaleFactor);
    }

    public DiedrichDevice withSlaveId(int id) {
        return new DiedrichDevice(serialPort, baudRate, id, btRegister, etRegister, scaleFactor);
    }

    public DiedrichDevice withBtRegister(int reg) {
        return new DiedrichDevice(serialPort, baudRate, slaveId, reg, etRegister, scaleFactor);
    }

    public DiedrichDevice withEtRegister(int reg) {
        return new DiedrichDevice(serialPort, baudRate, slaveId, btRegister, reg, scaleFactor);
    }

    public DiedrichDevice withScaleFactor(double factor) {
        return new DiedrichDevice(serialPort, baudRate, slaveId, btRegister, etRegister, factor);
    }

    /** For tests: inject a mock ModbusPort before calling connect(). */
    void setModbusPortForTest(ModbusPort port) {
        this.modbusPort = port;
    }

    @Override
    public void connect() {
        if (modbusPort != null && modbusPort.isConnected()) {
            return;
        }
        if (modbusPort == null) {
            if (serialPort == null || serialPort.isEmpty()) {
                throw new CommException("Diedrich RTU: serial port not set");
            }
            modbusPort = new ModbusPort(serialPort, baudRate, 8, "N", 1, ModbusPort.DEFAULT_TIMEOUT_MS, ModbusPort.TYPE_SERIAL_RTU);
        }
        modbusPort.connect();
        consecutiveFailures = 0;
    }

    @Override
    public void disconnect() {
        try {
            if (modbusPort != null) {
                modbusPort.disconnect();
            }
        } finally {
            modbusPort = null;
        }
    }

    @Override
    public boolean isConnected() {
        return modbusPort != null && modbusPort.isConnected();
    }

    @Override
    public double[] readTemperatures() {
        if (modbusPort == null || !modbusPort.isConnected()) {
            return new double[]{lastEt, lastBt};
        }
        try {
            int start = Math.min(btRegister, etRegister) - 1;
            int count = Math.abs(etRegister - btRegister) + 1;
            if (start < 0) start = 0;
            int[] regs = modbusPort.readInputRegisters(slaveId, start, count);
            if (regs == null || regs.length < 2) {
                onReadFailure();
                return new double[]{lastEt, lastBt};
            }
            consecutiveFailures = 0;
            int btIdx = btRegister <= etRegister ? 0 : 1;
            int etIdx = btRegister <= etRegister ? 1 : 0;
            double bt = regs[btIdx] / scaleFactor;
            double et = regs[etIdx] / scaleFactor;
            if (bt < STALE_MIN_C || bt > STALE_MAX_C) {
                bt = lastBt;
            }
            if (et < STALE_MIN_C || et > STALE_MAX_C) {
                et = lastEt;
            }
            lastBt = bt;
            lastEt = et;
            return new double[]{et, bt};
        } catch (CommException e) {
            LOG.log(Level.WARNING, "Diedrich read failed", e);
            onReadFailure();
            return new double[]{lastEt, lastBt};
        }
    }

    private void onReadFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= FAILURES_BEFORE_RECONNECT) {
            try {
                disconnect();
                Thread.sleep(RECONNECT_DELAY_MS);
                connect();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                LOG.log(Level.WARNING, "Diedrich reconnect interrupted");
            }
            consecutiveFailures = 0;
        }
    }
}
