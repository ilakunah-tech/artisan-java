package org.artisan.device;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Modbus port for TCP or serial RTU/ASCII.
 * Wraps j2mod master; equivalent to Python artisanlib.modbusport.
 */
public class ModbusPort {

    /** Connection type: 0 = Serial RTU, 1 = Serial ASCII, 3 = TCP, 4 = UDP */
    private final int type;

    private final String host;
    private final int port;
    private final String comport;
    private final int baudrate;
    private final int bytesize;
    private final String parity;
    private final int stopbits;
    private final int timeoutMs;

    private AbstractModbusMaster master;
    private boolean connected;

    public static final int TYPE_SERIAL_RTU = 0;
    public static final int TYPE_SERIAL_ASCII = 1;
    public static final int TYPE_TCP = 3;
    public static final int TYPE_UDP = 4;

    public static final int DEFAULT_PORT = 502;
    public static final int DEFAULT_TIMEOUT_MS = 400;

    public ModbusPort(String host, int port, int timeoutMs) {
        this.type = TYPE_TCP;
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
        this.comport = null;
        this.baudrate = 0;
        this.bytesize = 8;
        this.parity = "N";
        this.stopbits = 1;
    }

    public ModbusPort(String host, int port) {
        this(host, port, DEFAULT_TIMEOUT_MS);
    }

    public ModbusPort(String comport, int baudrate, int bytesize, String parity, int stopbits, int timeoutMs, int type) {
        this.type = type;
        this.host = null;
        this.port = -1;
        this.comport = comport;
        this.baudrate = baudrate;
        this.bytesize = bytesize;
        this.parity = parity;
        this.stopbits = stopbits;
        this.timeoutMs = timeoutMs;
    }

    public void connect() {
        if (connected && master != null) {
            return;
        }
        try {
            if (type == TYPE_TCP) {
                master = new ModbusTCPMaster(host, port, timeoutMs, false);
            } else if (type == TYPE_SERIAL_RTU || type == TYPE_SERIAL_ASCII) {
                SerialParameters params = new SerialParameters();
                params.setPortName(comport);
                params.setBaudRate(baudrate);
                params.setDatabits(bytesize);
                params.setParity(parity);
                params.setStopbits(stopbits);
                params.setEncoding(type == TYPE_SERIAL_ASCII ? Modbus.SERIAL_ENCODING_ASCII : Modbus.SERIAL_ENCODING_RTU);
                master = new ModbusSerialMaster(params, timeoutMs);
            } else {
                throw new UnsupportedOperationException("Modbus type not supported: " + type);
            }
            master.connect();
            connected = true;
        } catch (Exception e) {
            master = null;
            connected = false;
            throw new CommException("Modbus connect failed", e);
        }
    }

    public void disconnect() {
        if (master == null) {
            return;
        }
        try {
            master.disconnect();
        } catch (Exception ignored) {
        } finally {
            master = null;
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected && master != null;
    }

    /**
     * Read holding registers (function 3).
     *
     * @param unitId  slave unit id
     * @param address register start address (0-based)
     * @param count   number of registers
     * @return register values, or null on error
     */
    public int[] readHoldingRegisters(int unitId, int address, int count) {
        if (!isConnected()) {
            return null;
        }
        try {
            Register[] regs = master.readMultipleRegisters(unitId, address, count);
            if (regs == null) return null;
            int[] out = new int[regs.length];
            for (int i = 0; i < regs.length; i++) {
                out[i] = regs[i].getValue();
            }
            return out;
        } catch (Exception e) {
            throw new CommException("Modbus read holding registers failed", e);
        }
    }

    /**
     * Read input registers (function 4).
     */
    public int[] readInputRegisters(int unitId, int address, int count) {
        if (!isConnected()) {
            return null;
        }
        try {
            InputRegister[] regs = master.readInputRegisters(unitId, address, count);
            if (regs == null) return null;
            int[] out = new int[regs.length];
            for (int i = 0; i < regs.length; i++) {
                out[i] = regs[i].getValue();
            }
            return out;
        } catch (Exception e) {
            throw new CommException("Modbus read input registers failed", e);
        }
    }

    /**
     * Write single holding register (function 6).
     */
    public void writeSingleRegister(int unitId, int address, int value) {
        if (!isConnected()) {
            throw new CommException("Modbus not connected");
        }
        try {
            master.writeSingleRegister(unitId, address, new SimpleRegister(value));
        } catch (Exception e) {
            throw new CommException("Modbus write single register failed", e);
        }
    }

    /**
     * Convert Modbus address (e.g. 40001) to 0-based register index for function 3/6.
     */
    public static int addressToRegister(int address, int functionCode) {
        if (functionCode == 3 || functionCode == 6) {
            return address - 40001;
        }
        return address - 30001;
    }
}
