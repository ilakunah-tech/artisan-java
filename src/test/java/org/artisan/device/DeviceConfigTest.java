package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for DeviceConfig load/save.
 */
class DeviceConfigTest {

    @Test
    void defaults_activeType_isNone() {
        DeviceConfig cfg = new DeviceConfig();
        assertEquals(DeviceType.NONE, cfg.getActiveType());
    }

    @Test
    void saveAndReload_roundtrip() {
        DeviceConfig cfg = new DeviceConfig();
        cfg.setActiveType(DeviceType.SIMULATOR);
        cfg.setNotes("test note");
        cfg.save();

        DeviceConfig loaded = new DeviceConfig();
        loaded.load();
        assertEquals(DeviceType.SIMULATOR, loaded.getActiveType());
        assertEquals("test note", loaded.getNotes());
    }
}
