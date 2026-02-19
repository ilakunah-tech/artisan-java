package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Statistics: mean BT/ET, RoR min/max/mean, profile delta, empty data.
 */
class StatisticsTest {

    private static ProfileData profile(
            List<Double> timex,
            List<Double> temp1,
            List<Double> temp2,
            int chargeIdx,
            int dropIdx) {
        ProfileData p = new ProfileData();
        p.setTimex(timex);
        p.setTemp1(temp1 != null ? temp1 : Collections.emptyList());
        p.setTemp2(temp2);
        List<Integer> ti = Arrays.asList(chargeIdx, 0, 0, 0, 0, 0, dropIdx, 0);
        p.setTimeindex(ti);
        return p;
    }

    @Test
    void meanBtAndEtOverRoast() {
        List<Double> timex = Arrays.asList(0.0, 60.0, 120.0, 180.0);
        List<Double> temp1 = Arrays.asList(100.0, 150.0, 200.0, 220.0);  // ET
        List<Double> temp2 = Arrays.asList(80.0, 130.0, 180.0, 200.0);  // BT
        ProfileData p = profile(timex, temp1, temp2, 0, 3);

        RoastStats s = Statistics.compute(p);

        assertFalse(s.isEmpty());
        assertEquals(180.0, s.getTotalTimeSec(), 1e-6);
        assertEquals((80 + 130 + 180 + 200) / 4.0, s.getMeanBt(), 0.01);
        assertEquals((100 + 150 + 200 + 220) / 4.0, s.getMeanEt(), 0.01);
    }

    @Test
    void rorMinMaxMeanOverPeriod() {
        // RoR per min = (T[i+1]-T[i]) / (time[i+1]-time[i]) * 60. So for 1 min intervals, RoR = (T[i+1]-T[i]).
        List<Double> timex = Arrays.asList(0.0, 60.0, 120.0, 180.0);
        List<Double> temp2 = Arrays.asList(100.0, 120.0, 150.0, 140.0);  // RoR: 20, 30, -10
        ProfileData p = profile(timex, null, temp2, 0, 3);

        RoastStats s = Statistics.compute(p);

        assertFalse(s.isEmpty());
        assertEquals(-10.0, s.getRorMin(), 0.01);   // RoR values 20, 30, -10
        assertEquals(30.0, s.getRorMax(), 0.01);
        assertEquals((20 + 30 - 10) / 3.0, s.getRorMean(), 0.01);
    }

    @Test
    void compareTwoProfilesDelta() {
        ProfileData a = profile(
                Arrays.asList(0.0, 60.0, 120.0),
                null,
                Arrays.asList(100.0, 150.0, 200.0),
                0, 2);
        ProfileData b = profile(
                Arrays.asList(0.0, 60.0, 120.0),
                null,
                Arrays.asList(90.0, 140.0, 190.0),
                0, 2);
        RoastStats sa = Statistics.compute(a);
        RoastStats sb = Statistics.compute(b);
        RoastStats delta = Statistics.delta(sa, sb);

        assertFalse(delta.isEmpty());
        assertEquals(150.0 - 140.0, delta.getMeanBt(), 0.1);  // mean BT a - mean BT b
        assertEquals(0.0, delta.getTotalTimeSec(), 1e-6);    // same total time -> delta 0
    }

    @Test
    void edgeCaseEmptyData() {
        ProfileData p = new ProfileData();
        p.setTimex(Collections.emptyList());
        p.setTemp2(Collections.emptyList());
        p.setTimeindex(Collections.emptyList());

        RoastStats s = Statistics.compute(p);

        assertTrue(s.isEmpty());
        assertEquals(0.0, s.getMeanBt());
        assertEquals(0.0, s.getMeanEt());
        assertEquals(0.0, s.getTotalTimeSec());
    }

    @Test
    void edgeCaseNullProfile() {
        RoastStats s = Statistics.compute((ProfileData) null);
        assertTrue(s.isEmpty());
    }

    @Test
    void deltaWithNullReturnsOther() {
        RoastStats a = Statistics.compute((ProfileData) null);
        ProfileData p = profile(
                Arrays.asList(0.0, 60.0),
                null,
                Arrays.asList(100.0, 200.0),
                0, 1);
        RoastStats b = Statistics.compute(p);
        RoastStats d = Statistics.delta(a, b);
        assertFalse(d.isEmpty());
        assertEquals(b.getMeanBt(), d.getMeanBt(), 0.01);
    }

    @Test
    void computeKnownData_returnsCorrectMeanBtRorMaxTotalTime() {
        List<Double> timex = Arrays.asList(0.0, 60.0, 120.0, 180.0);
        List<Double> temp2 = Arrays.asList(100.0, 120.0, 150.0, 140.0);
        ProfileData p = profile(timex, null, temp2, 0, 3);
        RoastStats s = Statistics.compute(p);
        assertFalse(s.isEmpty());
        assertEquals(180.0, s.getTotalTimeSec(), 1e-6);
        assertEquals(30.0, s.getRorMax(), 0.01);
        assertEquals((100 + 120 + 150 + 140) / 4.0, s.getMeanBt(), 0.01);
    }
}
