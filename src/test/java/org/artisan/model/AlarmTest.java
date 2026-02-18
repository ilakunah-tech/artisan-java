package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmTest {

    @Test
    void firesWhenAboveTempThresholdCrossed() {
        Alarm a = new Alarm(true, AlarmCondition.ABOVE_TEMP, 200.0,
                AlarmAction.POP_UP, "", -1, false);
        assertFalse(a.evaluate(199.0, 0.0, Collections.emptyList()));
        assertTrue(a.evaluate(200.0, 0.0, Collections.emptyList()));
        assertTrue(a.evaluate(201.0, 0.0, Collections.emptyList()));
    }

    @Test
    void doesNotFireTwiceWhenTriggered() {
        Alarm a = new Alarm(true, AlarmCondition.ABOVE_TEMP, 200.0,
                AlarmAction.POP_UP, "", -1, false);
        assertTrue(a.evaluate(201.0, 0.0, Collections.emptyList()));
        a.markTriggered();
        assertFalse(a.evaluate(201.0, 0.0, Collections.emptyList()));
    }

    @Test
    void resetReArmsAlarm() {
        Alarm a = new Alarm(true, AlarmCondition.ABOVE_TEMP, 200.0,
                AlarmAction.POP_UP, "", -1, false);
        a.evaluate(201.0, 0.0, Collections.emptyList());
        a.markTriggered();
        assertTrue(a.isTriggered());
        a.reset();
        assertFalse(a.isTriggered());
        assertTrue(a.evaluate(201.0, 0.0, Collections.emptyList()));
    }

    @Test
    void firesAtTimeWhenCurrentTimeSecGeThreshold() {
        Alarm a = new Alarm(true, AlarmCondition.AT_TIME, 120.0,
                AlarmAction.POP_UP, "", -1, false);
        assertFalse(a.evaluate(0.0, 119.0, Collections.emptyList()));
        assertTrue(a.evaluate(0.0, 120.0, Collections.emptyList()));
        assertTrue(a.evaluate(0.0, 121.0, Collections.emptyList()));
    }

    @Test
    void inactiveAlarmNeverFires() {
        Alarm a = new Alarm(false, AlarmCondition.ABOVE_TEMP, 200.0,
                AlarmAction.POP_UP, "", -1, false);
        assertFalse(a.evaluate(201.0, 0.0, Collections.emptyList()));
    }

    @Test
    void belowTempFiresWhenTempLeThreshold() {
        Alarm a = new Alarm(true, AlarmCondition.BELOW_TEMP, 180.0,
                AlarmAction.POP_UP, "", -1, false);
        assertFalse(a.evaluate(181.0, 0.0, Collections.emptyList()));
        assertTrue(a.evaluate(180.0, 0.0, Collections.emptyList()));
        assertTrue(a.evaluate(179.0, 0.0, Collections.emptyList()));
    }
}
