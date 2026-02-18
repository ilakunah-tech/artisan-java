package org.artisan.device;

import com.fazecast.jSerialComm.SerialPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HottopDevice} with mocked SerialPort.
 */
@ExtendWith(MockitoExtension.class)
class HottopDeviceTest {

    @Mock
    private SerialPort mockSerialPort;

    /** Build 36-byte Hottop message. ET = (bytes[22]<<8|bytes[23])/10.0, BT = (bytes[24]<<8|bytes[25])/10.0 (tenths of °C). */
    private static byte[] hottopMessage(int etCelsius, int btCelsius) {
        byte[] msg = new byte[36];
        msg[0] = (byte) 0xA5;
        msg[1] = (byte) 0x96;
        msg[2] = (byte) 0xB0;
        msg[3] = (byte) 0xA0;
        msg[4] = 0x01;
        msg[5] = 0x01;
        msg[6] = 0x24;
        int etWord = etCelsius * 10;  // value in tenths of degree
        int btWord = btCelsius * 10;
        msg[22] = (byte) (etWord >> 8);
        msg[23] = (byte) etWord;
        msg[24] = (byte) (btWord >> 8);
        msg[25] = (byte) btWord;
        int sum = 0;
        for (int i = 0; i < 35; i++) sum += (msg[i] & 0xFF);
        msg[35] = (byte) (sum & 0xFF);
        return msg;
    }

    private static void injectSerialPort(AbstractCommPort port, SerialPort serialPort) throws Exception {
        Field f = AbstractCommPort.class.getDeclaredField("serialPort");
        f.setAccessible(true);
        f.set(port, serialPort);
    }

    @Test
    void connectWhenPortUnavailableThrowsCommException() {
        HottopDevice device = new HottopDevice("COM99999");
        assertThrows(CommException.class, device::connect);
        assertFalse(device.isConnected());
    }

    @Test
    void readTemperaturesWithMockSerialPortReturnsEtBt() throws Exception {
        int et = 195;
        int bt = 210;
        byte[] message = hottopMessage(et, bt);
        when(mockSerialPort.isOpen()).thenReturn(true);
        when(mockSerialPort.getInputStream()).thenReturn(new ByteArrayInputStream(message));

        HottopDevice device = new HottopDevice("COM1");
        injectSerialPort(device, mockSerialPort);
        double[] temps = device.readTemperatures();
        assertTrue(temps != null && temps.length == 2);
        assertArrayEquals(new double[]{195.0, 210.0}, temps, 0.01);
    }

    @Test
    void readTemperaturesWhenNotConnectedReturnsEmpty() {
        HottopDevice device = new HottopDevice("COM1");
        double[] temps = device.readTemperatures();
        assertTrue(temps != null && temps.length == 0);
    }
}
