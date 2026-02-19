package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerialPortConfigTest {

    @Test
    void defaults_areCorrect() {
        SerialPortConfig c = new SerialPortConfig();
        assertEquals("", c.getPortName());
        assertEquals(SerialPortConfig.DEFAULT_BAUD_RATE, c.getBaudRate());
        assertEquals(SerialPortConfig.DEFAULT_DATA_BITS, c.getDataBits());
        assertEquals(SerialPortConfig.DEFAULT_STOP_BITS, c.getStopBits());
        assertEquals(SerialPortConfig.DEFAULT_PARITY, c.getParity());
        assertEquals(SerialPortConfig.DEFAULT_READ_TIMEOUT_MS, c.getReadTimeoutMs());
        assertEquals(SerialPortConfig.DEFAULT_LINE_ENDING, c.getLineEnding());
    }

    @Test
    void saveAndReload_roundtrip() {
        SerialPortConfig original = new SerialPortConfig();
        original.setPortName("COM3");
        original.setBaudRate(9600);
        original.setDataBits(7);
        original.setStopBits(2);
        original.setParity(1);
        original.setReadTimeoutMs(2000);
        original.setLineEnding("\n");
        SerialPortConfig.saveToPreferences(original);

        SerialPortConfig loaded = new SerialPortConfig();
        SerialPortConfig.loadFromPreferences(loaded);
        assertEquals(original.getPortName(), loaded.getPortName());
        assertEquals(original.getBaudRate(), loaded.getBaudRate());
        assertEquals(original.getDataBits(), loaded.getDataBits());
        assertEquals(original.getStopBits(), loaded.getStopBits());
        assertEquals(original.getParity(), loaded.getParity());
        assertEquals(original.getReadTimeoutMs(), loaded.getReadTimeoutMs());
        assertEquals(original.getLineEnding(), loaded.getLineEnding());
    }
}
