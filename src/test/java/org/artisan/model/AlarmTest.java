package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmTest {

    @Test
    void constructorAndGetters() {
        Alarm a = new Alarm(true, "BT high", AlarmCondition.BT_RISES_ABOVE, 200.0,
                AlarmAction.POPUP_MESSAGE, "msg", true, 0, false);
        assertTrue(a.isEnabled());
        assertEquals("BT high", a.getDescription());
        assertEquals(AlarmCondition.BT_RISES_ABOVE, a.getCondition());
        assertEquals(200.0, a.getThreshold());
        assertEquals(AlarmAction.POPUP_MESSAGE, a.getAction());
        assertEquals("msg", a.getActionParam());
        assertTrue(a.isTriggerOnce());
        assertEquals(0, a.getGuardAlarmIndex());
        assertFalse(a.isTriggered());
    }

    @Test
    void markTriggeredAndReset() {
        Alarm a = new Alarm(true, "", AlarmCondition.BT_RISES_ABOVE, 200.0,
                AlarmAction.POPUP_MESSAGE, "", false, -1, false);
        assertFalse(a.isTriggered());
        a.markTriggered();
        assertTrue(a.isTriggered());
        a.reset();
        assertFalse(a.isTriggered());
    }

    @Test
    void copyConstructorResetsTriggered() {
        Alarm a = new Alarm(true, "x", AlarmCondition.ET_RISES_ABOVE, 150.0,
                AlarmAction.MARK_EVENT, "ev", false, -1, false);
        a.markTriggered();
        Alarm copy = new Alarm(a);
        assertEquals(a.isEnabled(), copy.isEnabled());
        assertEquals(a.getDescription(), copy.getDescription());
        assertEquals(a.getCondition(), copy.getCondition());
        assertEquals(a.getThreshold(), copy.getThreshold());
        assertEquals(a.getAction(), copy.getAction());
        assertEquals(a.getActionParam(), copy.getActionParam());
        assertEquals(a.isTriggerOnce(), copy.isTriggerOnce());
        assertEquals(a.getGuardAlarmIndex(), copy.getGuardAlarmIndex());
        assertFalse(copy.isTriggered());
    }

    @Test
    void settersUpdateState() {
        Alarm a = new Alarm(true, "a", AlarmCondition.BT_RISES_ABOVE, 100.0,
                AlarmAction.POPUP_MESSAGE, "", false, -1, false);
        a.setEnabled(false);
        a.setDescription("b");
        a.setCondition(AlarmCondition.ROR_RISES_ABOVE);
        a.setThreshold(50.0);
        a.setAction(AlarmAction.PLAY_SOUND);
        a.setActionParam("file.wav");
        a.setTriggerOnce(true);
        a.setGuardAlarmIndex(1);
        assertFalse(a.isEnabled());
        assertEquals("b", a.getDescription());
        assertEquals(AlarmCondition.ROR_RISES_ABOVE, a.getCondition());
        assertEquals(50.0, a.getThreshold());
        assertEquals(AlarmAction.PLAY_SOUND, a.getAction());
        assertEquals("file.wav", a.getActionParam());
        assertTrue(a.isTriggerOnce());
        assertEquals(1, a.getGuardAlarmIndex());
    }
}
