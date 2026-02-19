package org.artisan.controller;

import org.artisan.model.Alarm;
import org.artisan.model.AlarmAction;
import org.artisan.model.AlarmCondition;
import org.artisan.model.AlarmList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmListPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoad_roundtrip() throws IOException {
        Path path = tempDir.resolve("alarms.json");
        AlarmList list = new AlarmList();
        list.add(new Alarm(true, "BT high", AlarmCondition.BT_RISES_ABOVE, 205.0, AlarmAction.POPUP_MESSAGE, "Message", true, -1, true));
        list.add(new Alarm(false, "Guard", AlarmCondition.TIME_AFTER_CHARGE, 60.0, AlarmAction.MARK_EVENT, "dry", false, 0, false));

        AlarmListPersistence.save(list, path);
        AlarmList loaded = AlarmListPersistence.load(path);

        assertEquals(2, loaded.size());
        Alarm a0 = loaded.get(0);
        assertTrue(a0.isEnabled());
        assertEquals("BT high", a0.getDescription());
        assertEquals(AlarmCondition.BT_RISES_ABOVE, a0.getCondition());
        assertEquals(205.0, a0.getThreshold());
        assertEquals(AlarmAction.POPUP_MESSAGE, a0.getAction());
        assertEquals("Message", a0.getActionParam());
        assertTrue(a0.isTriggerOnce());
        assertEquals(-1, a0.getGuardAlarmIndex());
        assertFalse(a0.isTriggered());

        Alarm a1 = loaded.get(1);
        assertFalse(a1.isEnabled());
        assertEquals("Guard", a1.getDescription());
        assertEquals(AlarmCondition.TIME_AFTER_CHARGE, a1.getCondition());
        assertEquals(60.0, a1.getThreshold());
        assertEquals(AlarmAction.MARK_EVENT, a1.getAction());
        assertEquals("dry", a1.getActionParam());
        assertFalse(a1.isTriggerOnce());
        assertEquals(0, a1.getGuardAlarmIndex());
    }

    @Test
    void load_returnEmptyList_whenFileAbsent() {
        Path path = tempDir.resolve("nonexistent.json");
        AlarmList loaded = AlarmListPersistence.load(path);
        assertEquals(0, loaded.size());
    }
}
