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
 * DiedrichDevice: rtuMode defaults, readTemperatures (input registers), 3 consecutive failures → disconnect/reconnect.
 */
class DiedrichDeviceTest {

  private ModbusPort mockModbus;

  @BeforeEach
  void setUp() {
    mockModbus = mock(ModbusPort.class);
  }

  @Test
  void rtuModeBuilderCreatesWithCorrectDefaults() {
    DiedrichDevice d = DiedrichDevice.rtuMode("COM3");
    d.setModbusPortForTest(mockModbus);
    when(mockModbus.isConnected()).thenReturn(true);
    when(mockModbus.readInputRegisters(eq(1), eq(0), eq(2))).thenReturn(new int[] { 1950, 2100 });
    d.connect();
    double[] t = d.readTemperatures();
    assertArrayEquals(new double[] { 210.0, 195.0 }, t, 0.01);
    d.disconnect();
  }

  @Test
  void readTemperaturesMockInputRegistersReturnsScaled() {
    DiedrichDevice d = DiedrichDevice.rtuMode("COM1");
    d.setModbusPortForTest(mockModbus);
    when(mockModbus.isConnected()).thenReturn(true);
    when(mockModbus.readInputRegisters(eq(1), eq(0), eq(2))).thenReturn(new int[] { 1950, 2100 });
    d.connect();
    double[] t = d.readTemperatures();
    assertNotNull(t);
    assertArrayEquals(new double[] { 210.0, 195.0 }, t, 0.01);
    d.disconnect();
  }

  @Test
  void twoConsecutiveCommExceptionsReturnLastValuesAndDeviceStaysConnected() {
    DiedrichDevice d = DiedrichDevice.rtuMode("COM1");
    d.setModbusPortForTest(mockModbus);
    when(mockModbus.isConnected()).thenReturn(true);
    when(mockModbus.readInputRegisters(anyInt(), anyInt(), anyInt()))
        .thenReturn(new int[] { 1950, 2100 })
        .thenThrow(new CommException("read failed"))
        .thenThrow(new CommException("read failed"));
    d.connect();
    double[] first = d.readTemperatures();
    assertArrayEquals(new double[] { 210.0, 195.0 }, first, 0.01);
    double[] second = d.readTemperatures();
    assertArrayEquals(new double[] { 210.0, 195.0 }, second, 0.01);
    assertTrue(d.isConnected());
    d.disconnect();
    assertFalse(d.isConnected());
  }
}
