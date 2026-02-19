package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModbusPortConfigTest {

    @Test
    void defaults_areCorrect() {
        ModbusPortConfig c = new ModbusPortConfig();
        assertEquals("", c.getHost());
        assertEquals(ModbusPortConfig.DEFAULT_PORT, c.getPort());
        assertTrue(c.isUseTcp());
        assertEquals(ModbusPortConfig.DEFAULT_SLAVE_ID, c.getSlaveId());
        assertEquals(ModbusPortConfig.DEFAULT_BT_REGISTER, c.getBtRegister());
        assertEquals(ModbusPortConfig.DEFAULT_ET_REGISTER, c.getEtRegister());
        assertEquals(ModbusPortConfig.DEFAULT_SCALE, c.getScale(), 1e-6);
    }

    @Test
    void saveAndReload_roundtrip() {
        ModbusPortConfig original = new ModbusPortConfig();
        original.setHost("192.168.1.10");
        original.setPort(5020);
        original.setUseTcp(false);
        original.setSlaveId(5);
        original.setBtRegister(10);
        original.setEtRegister(11);
        original.setScale(0.01);
        ModbusPortConfig.saveToPreferences(original);

        ModbusPortConfig loaded = new ModbusPortConfig();
        ModbusPortConfig.loadFromPreferences(loaded);
        assertEquals(original.getHost(), loaded.getHost());
        assertEquals(original.getPort(), loaded.getPort());
        assertFalse(loaded.isUseTcp());
        assertEquals(original.getSlaveId(), loaded.getSlaveId());
        assertEquals(original.getBtRegister(), loaded.getBtRegister());
        assertEquals(original.getEtRegister(), loaded.getEtRegister());
        assertEquals(original.getScale(), loaded.getScale(), 1e-6);
    }
}
