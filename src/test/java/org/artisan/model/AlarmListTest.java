package org.artisan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
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
    void evaluateAllReturnsOnlyNewlyFiredAlarms() {
        Alarm a1 = new Alarm(true, AlarmCondition.ABOVE_TEMP, 200.0, AlarmAction.POP_UP, "", -1, false);
        Alarm a2 = new Alarm(true, AlarmCondition.ABOVE_TEMP, 210.0, AlarmAction.POP_UP, "", -1, false);
        list.add(a1);
        list.add(a2);

        List<Alarm> fired = list.evaluateAll(205.0, 0.0, Collections.emptyList());
        assertEquals(1, fired.size());
        assertTrue(fired.contains(a1));
        assertTrue(a1.isTriggered());
        assertFalse(a2.isTriggered());

        fired = list.evaluateAll(215.0, 0.0, Collections.emptyList());
        assertEquals(1, fired.size());
        assertTrue(fired.contains(a2));
    }

    @Test
    void resetAllReArmsAllAlarms() {
        Alarm a1 = new Alarm(true, AlarmCondition.AT_TIME, 10.0, AlarmAction.POP_UP, "", -1, false);
        list.add(a1);
        list.evaluateAll(0.0, 15.0, Collections.emptyList());
        assertTrue(a1.isTriggered());

        list.resetAll();
        assertFalse(a1.isTriggered());
        List<Alarm> fired = list.evaluateAll(0.0, 15.0, Collections.emptyList());
        assertEquals(1, fired.size());
    }

    @Test
    void guardAlarmMustFireFirst() {
        Alarm guard = new Alarm(true, AlarmCondition.AT_TIME, 5.0, AlarmAction.POP_UP, "", -1, false);
        Alarm dependent = new Alarm(true, AlarmCondition.AT_TIME, 10.0, AlarmAction.POP_UP, "", 0, false);
        list.add(guard);
        list.add(dependent);

        List<Alarm> fired = list.evaluateAll(0.0, 7.0, Collections.emptyList());
        assertEquals(1, fired.size());
        assertTrue(fired.contains(guard));

        fired = list.evaluateAll(0.0, 12.0, Collections.emptyList());
        assertEquals(1, fired.size());
        assertTrue(fired.contains(dependent));
    }

    @Test
    void addRemoveSizeGet() {
        assertEquals(0, list.size());
        Alarm a = new Alarm(true, AlarmCondition.ABOVE_TEMP, 200.0, AlarmAction.POP_UP, "", -1, false);
        list.add(a);
        assertEquals(1, list.size());
        assertEquals(a, list.get(0));
        list.remove(0);
        assertEquals(0, list.size());
    }
}
