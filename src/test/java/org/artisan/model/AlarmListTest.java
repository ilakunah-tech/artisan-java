package org.artisan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmListTest {

    private AlarmList list;

    @BeforeEach
    void setUp() {
        list = new AlarmList();
    }

    @Test
    void resetAllReArmsAllAlarms() {
        Alarm a1 = new Alarm(true, "T", AlarmCondition.TIME_AFTER_EVENT, 10.0, AlarmAction.POPUP_MESSAGE, "", false, -1, false);
        list.add(a1);
        a1.markTriggered();
        assertTrue(a1.isTriggered());

        list.resetAll();
        assertFalse(a1.isTriggered());
    }

    @Test
    void addRemoveSizeGet() {
        assertEquals(0, list.size());
        Alarm a = new Alarm(true, "BT high", AlarmCondition.BT_RISES_ABOVE, 200.0, AlarmAction.POPUP_MESSAGE, "", false, -1, false);
        list.add(a);
        assertEquals(1, list.size());
        assertEquals(a, list.get(0));
        list.remove(0);
        assertEquals(0, list.size());
    }

    @Test
    void getAlarmsReturnsCopySetAlarmsReplacesAll() {
        Alarm a = new Alarm(true, "A", AlarmCondition.BT_RISES_ABOVE, 100.0, AlarmAction.POPUP_MESSAGE, "", false, -1, false);
        list.add(a);
        List<Alarm> copy = list.getAlarms();
        assertEquals(1, copy.size());
        assertEquals(a, copy.get(0));
        copy.clear();
        assertEquals(1, list.size());

        AlarmList other = new AlarmList();
        other.add(new Alarm(false, "B", AlarmCondition.ET_RISES_ABOVE, 150.0, AlarmAction.PLAY_SOUND, "x.wav", true, 0, false));
        list.setAlarms(other.getAlarms());
        assertEquals(1, list.size());
        assertEquals("B", list.get(0).getDescription());
    }
}
