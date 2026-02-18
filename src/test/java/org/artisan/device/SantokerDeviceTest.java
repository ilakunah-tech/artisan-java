package org.artisan.device;

import com.fazecast.jSerialComm.SerialPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SantokerDevice} with mocked SerialPort.
 */
@ExtendWith(MockitoExtension.class)
class SantokerDeviceTest {

    @Mock
    private SerialPort mockSerialPort;

    private static void injectSerialPort(AbstractCommPort port, SerialPort serialPort) throws Exception {
        Field f = AbstractCommPort.class.getDeclaredField("serialPort");
        f.setAccessible(true);
        f.set(port, serialPort);
    }

    @Test
    void connectWhenPortUnavailableThrowsCommException() {
        SantokerDevice device = new SantokerDevice("COM99999");
        assertThrows(CommException.class, device::connect);
        assertFalse(device.isConnected());
    }

    @Test
    void readTemperaturesWhenNotConnectedReturnsEmpty() {
        SantokerDevice device = new SantokerDevice("COM1");
        double[] temps = device.readTemperatures();
        assertTrue(temps != null && temps.length == 0);
    }

    @Test
    void readTemperaturesWithMockSerialPortReturnsEmptyUntilProtocolPorted() throws Exception {
        when(mockSerialPort.isOpen()).thenReturn(true);
        SantokerDevice device = new SantokerDevice("COM1");
        injectSerialPort(device, mockSerialPort);
        double[] temps = device.readTemperatures();
        assertTrue(temps != null && temps.length == 0);
    }
}
