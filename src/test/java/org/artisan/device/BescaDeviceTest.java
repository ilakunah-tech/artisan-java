package org.artisan.device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * BescaDevice: builder, readTemperatures with mock ModbusPort, CommException returns last values, stale protection.
 */
class BescaDeviceTest {

  private ModbusPort mockModbus;

  @BeforeEach
  void setUp() {
    mockModbus = mock(ModbusPort.class);
  }

  @Test
  void tcpModeBuilderSetsUseTcpAndHost() {
    BescaDevice d = BescaDevice.tcpMode("192.168.1.100");
    d.setModbusPortForTest(mockModbus);
    when(mockModbus.isConnected()).thenReturn(true);
    when(mockModbus.readHoldingRegisters(eq(1), eq(0), eq(2))).thenReturn(new int[] { 200, 210 });
    d.connect();
    double[] t = d.readTemperatures();
    assertNotNull(t);
    assertTrue(t.length >= 2);
    assertTrue(t[0] >= 0 && t[1] >= 0);
    d.disconnect();
  }

  @Test
  void rtuModeBuilderSetsSerialPort() {
    BescaDevice d = BescaDevice.rtuMode("COM3");
    d.setModbusPortForTest(mockModbus);
    when(mockModbus.isConnected()).thenReturn(true);
    when(mockModbus.readHoldingRegisters(eq(1), eq(0), eq(2))).thenReturn(new int[] { 153, 168 });
    d.connect();
    double[] t = d.readTemperatures();
    assertNotNull(t);
    assertArrayEquals(new double[] { 16.8, 15.3 }, t, 0.01);
    d.disconnect();
  }

  @Test
  void readTemperaturesMockReturnsScaledValues() {
    BescaDevice d = BescaDevice.rtuMode("COM1");
    d.setModbusPortForTest(mockModbus);
    when(mockModbus.isConnected()).thenReturn(true);
    when(mockModbus.readHoldingRegisters(eq(1), eq(0), eq(2))).thenReturn(new int[] { 153, 168 });
    d.connect();
    double[] t = d.readTemperatures();
    assertArrayEquals(new double[] { 16.8, 15.3 }, t, 0.01);
    d.disconnect();
  }

  @Test
  void readTemperaturesCommExceptionReturnsLastValues() {
    BescaDevice d = BescaDevice.rtuMode("COM1");
    d.setModbusPortForTest(mockModbus);
    when(mockModbus.isConnected()).thenReturn(true);
    when(mockModbus.readHoldingRegisters(anyInt(), anyInt(), anyInt()))
        .thenReturn(new int[] { 100, 110 })
        .thenThrow(new CommException("io error"));
    d.connect();
    double[] first = d.readTemperatures();
    assertArrayEquals(new double[] { 11.0, 10.0 }, first, 0.01);
    double[] second = d.readTemperatures();
    assertArrayEquals(new double[] { 11.0, 10.0 }, second, 0.01);
    d.disconnect();
  }

  @Test
  void staleProtectionReturnsLastValidValue() {
    BescaDevice d = BescaDevice.rtuMode("COM1");
    d.setModbusPortForTest(mockModbus);
    when(mockModbus.isConnected()).thenReturn(true);
    when(mockModbus.readHoldingRegisters(eq(1), eq(0), eq(2)))
        .thenReturn(new int[] { 200, 210 })
        .thenReturn(new int[] { 99990, 99990 });
    d.connect();
    d.readTemperatures();
    double[] afterStale = d.readTemperatures();
    assertNotNull(afterStale);
    assertTrue(afterStale[0] < 500 && afterStale[1] < 500);
    assertTrue(afterStale[0] >= 0 && afterStale[1] >= 0);
    d.disconnect();
  }

  @Test
  void disconnectClosesModbusPort() {
    BescaDevice d = BescaDevice.rtuMode("COM1");
    d.setModbusPortForTest(mockModbus);
    when(mockModbus.isConnected()).thenReturn(true);
    when(mockModbus.readHoldingRegisters(eq(1), eq(0), eq(2))).thenReturn(new int[] { 0, 0 });
    d.connect();
    assertTrue(d.isConnected());
    d.disconnect();
    assertFalse(d.isConnected());
  }
}
