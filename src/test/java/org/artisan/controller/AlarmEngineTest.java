package org.artisan.controller;

import org.artisan.model.Alarm;
import org.artisan.model.AlarmAction;
import org.artisan.model.AlarmCondition;
import org.artisan.model.AlarmList;
import org.artisan.model.PhasesConfig;
import org.artisan.model.ProfileData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmEngineTest {

    private AlarmList alarms;
    private AlarmEngine engine;
    private List<String> markEventCalls;
    private List<Double> burnerCalls;

    @BeforeEach
    void setUp() {
        alarms = new AlarmList();
        engine = new AlarmEngine(alarms, () -> {});
        markEventCalls = new ArrayList<>();
        burnerCalls = new ArrayList<>();
        engine.setMarkEventCallback(markEventCalls::add);
        engine.setBurnerCallback(burnerCalls::add);
    }

    private ProfileData emptyProfile() {
        ProfileData p = new ProfileData();
        p.setTimex(new ArrayList<>());
        p.setTemp1(new ArrayList<>());
        p.setTemp2(new ArrayList<>());
        p.setTimeindex(new ArrayList<>());
        return p;
    }

    private ProfileData profileWithChargeAtZero() {
        ProfileData p = new ProfileData();
        List<Double> tx = new ArrayList<>();
        tx.add(0.0);
        tx.add(60.0);
        p.setTimex(tx);
        p.setTemp1(new ArrayList<>());
        p.setTemp2(new ArrayList<>());
        List<Integer> ti = new ArrayList<>();
        ti.add(0);
        for (int i = 0; i < 7; i++) ti.add(-1);
        p.setTimeindex(ti);
        return p;
    }

    @Test
    void alarmNotTriggered_whenDisabled() {
        Alarm a = new Alarm(false, "off", AlarmCondition.BT_RISES_ABOVE, 200.0, AlarmAction.MARK_EVENT, "ev", false, -1, false);
        alarms.add(a);
        ProfileData profile = emptyProfile();
        engine.evaluate(0.0, 205.0, 180.0, 5.0, profile, null);
        assertFalse(a.isTriggered());
        assertTrue(markEventCalls.isEmpty());
    }

    @Test
    void alarmTriggered_whenBtExceedsThreshold() {
        Alarm a = new Alarm(true, "BT", AlarmCondition.BT_RISES_ABOVE, 200.0, AlarmAction.MARK_EVENT, "bt_high", false, -1, false);
        alarms.add(a);
        ProfileData profile = emptyProfile();
        engine.evaluate(0.0, 205.0, 180.0, 5.0, profile, null);
        assertTrue(a.isTriggered());
        assertEquals(1, markEventCalls.size());
        assertEquals("bt_high", markEventCalls.get(0));
    }

    @Test
    void alarmTriggeredOnce_notAgainOnSecondSample() {
        Alarm a = new Alarm(true, "Once", AlarmCondition.BT_RISES_ABOVE, 200.0, AlarmAction.MARK_EVENT, "x", true, -1, false);
        alarms.add(a);
        ProfileData profile = emptyProfile();
        engine.evaluate(0.0, 205.0, 180.0, 5.0, profile, null);
        assertTrue(a.isTriggered());
        assertEquals(1, markEventCalls.size());
        markEventCalls.clear();
        engine.evaluate(0.0, 210.0, 180.0, 5.0, profile, null);
        assertEquals(0, markEventCalls.size());
    }

    @Test
    void guardPreventsAlarm_untilGuardAlarmFires() {
        Alarm guard = new Alarm(true, "G", AlarmCondition.TIME_AFTER_EVENT, 5.0, AlarmAction.MARK_EVENT, "guard", false, -1, false);
        Alarm dependent = new Alarm(true, "D", AlarmCondition.TIME_AFTER_EVENT, 10.0, AlarmAction.MARK_EVENT, "dep", false, 0, false);
        alarms.add(guard);
        alarms.add(dependent);
        ProfileData profile = emptyProfile();
        engine.evaluate(3.0, 100.0, 100.0, 0.0, profile, null);
        assertFalse(guard.isTriggered());
        assertFalse(dependent.isTriggered());
        assertEquals(0, markEventCalls.size());
        engine.evaluate(7.0, 100.0, 100.0, 0.0, profile, null);
        assertTrue(guard.isTriggered());
        assertFalse(dependent.isTriggered());
        assertEquals(1, markEventCalls.size());
        markEventCalls.clear();
        engine.evaluate(12.0, 100.0, 100.0, 0.0, profile, null);
        assertTrue(dependent.isTriggered());
        assertEquals(1, markEventCalls.size());
        assertEquals("dep", markEventCalls.get(0));
    }

    @Test
    void guardAllowsAlarm_afterGuardAlarmFires() {
        Alarm guard = new Alarm(true, "G", AlarmCondition.TIME_AFTER_EVENT, 2.0, AlarmAction.MARK_EVENT, "g", false, -1, false);
        Alarm dep = new Alarm(true, "D", AlarmCondition.TIME_AFTER_EVENT, 5.0, AlarmAction.MARK_EVENT, "d", false, 0, false);
        alarms.add(guard);
        alarms.add(dep);
        ProfileData profile = emptyProfile();
        engine.evaluate(3.0, 100.0, 100.0, 0.0, profile, null);
        assertTrue(guard.isTriggered());
        assertEquals(1, markEventCalls.size());
        engine.evaluate(6.0, 100.0, 100.0, 0.0, profile, null);
        assertTrue(dep.isTriggered());
        assertEquals(2, markEventCalls.size());
        assertEquals("d", markEventCalls.get(1));
    }

    @Test
    void reset_clearsTriggeredFlags() {
        Alarm a = new Alarm(true, "R", AlarmCondition.BT_RISES_ABOVE, 200.0, AlarmAction.MARK_EVENT, "r", false, -1, false);
        alarms.add(a);
        ProfileData profile = emptyProfile();
        engine.evaluate(0.0, 205.0, 180.0, 5.0, profile, null);
        assertTrue(a.isTriggered());
        engine.reset();
        assertFalse(a.isTriggered());
        markEventCalls.clear();
        engine.evaluate(0.0, 206.0, 180.0, 5.0, profile, null);
        assertTrue(a.isTriggered());
        assertEquals(1, markEventCalls.size());
    }
}
