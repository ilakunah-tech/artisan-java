package org.artisan.device;

import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * DeviceChannel implementation using Modbus (TCP or RTU over serial).
 * Reads holding registers for BT and ET, applies scale factor.
 */
public final class ModbusDeviceChannel implements DeviceChannel {

    private final ModbusPortConfig config;
    private AbstractModbusMaster master;
    private volatile boolean connected;

    public ModbusDeviceChannel(ModbusPortConfig config) {
        this.config = config != null ? config : new ModbusPortConfig();
        this.master = null;
        this.connected = false;
    }

    @Override
    public void open() throws DeviceException {
        if (master != null && connected) {
            return;
        }
        if (master != null) {
            try {
                master.disconnect();
            } catch (Exception ignored) {
            }
            master = null;
        }
        try {
            if (config.isUseTcp()) {
                String h = config.getHost();
                if (h == null || h.isBlank()) {
                    throw new DeviceException("Modbus TCP host is empty");
                }
                master = new ModbusTCPMaster(h, config.getPort(), 1000, false);
            } else {
                String portName = config.getHost();
                if (portName == null || portName.isBlank()) {
                    throw new DeviceException("Modbus RTU serial port is empty");
                }
                SerialParameters params = new SerialParameters();
                params.setPortName(portName);
                params.setBaudRate(9600);
                params.setDatabits(8);
                params.setParity("N");
                params.setStopbits(1);
                params.setEncoding(com.ghgande.j2mod.modbus.Modbus.SERIAL_ENCODING_RTU);
                master = new ModbusSerialMaster(params, 1000);
            }
            master.connect();
            connected = true;
        } catch (Exception e) {
            master = null;
            connected = false;
            throw new DeviceException("Modbus connect failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (master == null) return;
        try {
            master.disconnect();
        } catch (Exception ignored) {
        } finally {
            master = null;
            connected = false;
        }
    }

    @Override
    public boolean isOpen() {
        return master != null && connected;
    }

    @Override
    public SampleResult read() throws DeviceException {
        if (!isOpen()) {
            throw new DeviceException("Modbus port is not open");
        }
        int slaveId = config.getSlaveId();
        int btReg = config.getBtRegister();
        int etReg = config.getEtRegister();
        double scale = config.getScale();
        try {
            int start = Math.min(btReg, etReg);
            int count = Math.abs(etReg - btReg) + 1;
            com.ghgande.j2mod.modbus.procimg.Register[] regs = master.readMultipleRegisters(slaveId, start, count);
            double bt = Double.NaN;
            double et = Double.NaN;
            if (regs != null) {
                int btOffset = btReg - start;
                int etOffset = etReg - start;
                if (btOffset >= 0 && btOffset < regs.length) {
                    bt = regs[btOffset].getValue() * scale;
                }
                if (etOffset >= 0 && etOffset < regs.length) {
                    et = regs[etOffset].getValue() * scale;
                }
            }
            return SampleResult.now(bt, et);
        } catch (Exception e) {
            throw new DeviceException("Modbus read failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDescription() {
        if (config.isUseTcp()) {
            return "Modbus " + config.getHost() + ":" + config.getPort();
        }
        return "Modbus RTU " + config.getHost();
    }
}
