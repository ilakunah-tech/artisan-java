package org.artisan.device;

import com.fazecast.jSerialComm.SerialPort;

// Ported from santoker.py — original Python lines: 95–346

/**
 * Santoker roaster device (serial or BLE). Serial protocol: header 0xEE 0xA5/0xB5,
 * target (0xF1=BT, 0xF2=ET), code 0x02 0x04, length, data, CRC, tail. This stub returns no temperatures until protocol is fully ported.
 */
public class SantokerDevice extends AbstractCommPort {

    /** WiFi header second byte. */
    private static final byte[] HEADER_WIFI = {(byte) 0xEE, (byte) 0xA5};
    /** BT header second byte. */
    private static final byte[] HEADER_BT = {(byte) 0xEE, (byte) 0xB5};

    public SantokerDevice(String portName) {
        super(portName);
    }

    @Override
    protected double[] readTemperaturesImpl() {
        // Full Santoker protocol parsing (read_msg, register_reading for BT/ET) not yet ported
        return new double[0];
    }
}
