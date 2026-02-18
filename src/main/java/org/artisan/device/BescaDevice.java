package org.artisan.device;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Besca roaster device: Modbus TCP (automatic BSC) or Modbus RTU over USB (manual BSC / Bee).
 * BT = holding register 1, ET = holding register 2 (function 3), scale value/10.0 → °C.
 */
public final class BescaDevice implements DevicePort {

    private static final Logger LOG = Logger.getLogger(BescaDevice.class.getName());
    private static final int DEFAULT_SLAVE_ID = 1;
    private static final int DEFAULT_BT_REGISTER = 1;
    private static final int DEFAULT_ET_REGISTER = 2;
    private static final double DEFAULT_SCALE_FACTOR = 10.0;
    private static final int DEFAULT_TCP_PORT = 502;
    private static final int DEFAULT_RTU_BAUD = 19200;
    private static final double STALE_MIN_C = -50.0;
    private static final double STALE_MAX_C = 500.0;

    private final boolean useTcp;
    private final String host;
    private final int tcpPort;
    private final String serialPort;
    private final int baudRate;
    private final int slaveId;
    private final int btRegister;
    private final int etRegister;
    private final double scaleFactor;

    private ModbusPort modbusPort;
    private volatile double lastBt;
    private volatile double lastEt;

    private BescaDevice(boolean useTcp, String host, int tcpPort, String serialPort, int baudRate,
                        int slaveId, int btRegister, int etRegister, double scaleFactor) {
        this.useTcp = useTcp;
        this.host = host != null ? host : "192.168.1.1";
        this.tcpPort = tcpPort > 0 ? tcpPort : DEFAULT_TCP_PORT;
        this.serialPort = serialPort;
        this.baudRate = baudRate > 0 ? baudRate : DEFAULT_RTU_BAUD;
        this.slaveId = slaveId >= 1 && slaveId <= 247 ? slaveId : DEFAULT_SLAVE_ID;
        this.btRegister = btRegister >= 0 ? btRegister : DEFAULT_BT_REGISTER;
        this.etRegister = etRegister >= 0 ? etRegister : DEFAULT_ET_REGISTER;
        this.scaleFactor = scaleFactor > 0 ? scaleFactor : DEFAULT_SCALE_FACTOR;
    }

    /**
     * TCP mode: connect to Besca over network (automatic BSC models).
     * Default port 502.
     */
    public static BescaDevice tcpMode(String host) {
        return new BescaDevice(true, host, DEFAULT_TCP_PORT, null, 0, DEFAULT_SLAVE_ID, DEFAULT_BT_REGISTER, DEFAULT_ET_REGISTER, DEFAULT_SCALE_FACTOR);
    }

    /**
     * RTU mode: connect over serial/USB (manual BSC and Bee models).
     * Default 19200 baud, EVEN parity, 1 stop bit.
     */
    public static BescaDevice rtuMode(String serialPort) {
        return new BescaDevice(false, null, 0, serialPort, DEFAULT_RTU_BAUD, DEFAULT_SLAVE_ID, DEFAULT_BT_REGISTER, DEFAULT_ET_REGISTER, DEFAULT_SCALE_FACTOR);
    }

    public BescaDevice withSlaveId(int id) {
        return new BescaDevice(useTcp, host, tcpPort, serialPort, baudRate, id, btRegister, etRegister, scaleFactor);
    }

    public BescaDevice withBtRegister(int reg) {
        return new BescaDevice(useTcp, host, tcpPort, serialPort, baudRate, slaveId, reg, etRegister, scaleFactor);
    }

    public BescaDevice withEtRegister(int reg) {
        return new BescaDevice(useTcp, host, tcpPort, serialPort, baudRate, slaveId, btRegister, reg, scaleFactor);
    }

    public BescaDevice withScaleFactor(double factor) {
        return new BescaDevice(useTcp, host, tcpPort, serialPort, baudRate, slaveId, btRegister, etRegister, factor);
    }

    public BescaDevice withTcpPort(int port) {
        return new BescaDevice(useTcp, host, port, serialPort, baudRate, slaveId, btRegister, etRegister, scaleFactor);
    }

    public BescaDevice withBaudRate(int baud) {
        return new BescaDevice(useTcp, host, tcpPort, serialPort, baud, slaveId, btRegister, etRegister, scaleFactor);
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
        if (useTcp) {
            modbusPort = new ModbusPort(host, tcpPort);
        } else {
            if (serialPort == null || serialPort.isEmpty()) {
                throw new CommException("Besca RTU: serial port not set");
            }
            modbusPort = new ModbusPort(serialPort, baudRate, 8, "E", 1, ModbusPort.DEFAULT_TIMEOUT_MS, ModbusPort.TYPE_SERIAL_RTU);
        }
        }
        modbusPort.connect();
        // Verify connection with one read
        int start = Math.min(btRegister, etRegister) - 1;
        int count = Math.abs(etRegister - btRegister) + 1;
        if (start < 0) start = 0;
        int[] regs = modbusPort.readHoldingRegisters(slaveId, start, count);
        if (regs == null || regs.length < 2) {
            modbusPort.disconnect();
            throw new CommException("Besca: failed to read registers on connect");
        }
        int btIdx = btRegister <= etRegister ? 0 : 1;
        int etIdx = btRegister <= etRegister ? 1 : 0;
        lastBt = regs[btIdx] / scaleFactor;
        lastEt = regs[etIdx] / scaleFactor;
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
            int[] regs = modbusPort.readHoldingRegisters(slaveId, start, count);
            if (regs == null || regs.length < 2) {
                return new double[]{lastEt, lastBt};
            }
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
            LOG.log(Level.WARNING, "Besca read failed, using last values", e);
            return new double[]{lastEt, lastBt};
        }
    }
}
