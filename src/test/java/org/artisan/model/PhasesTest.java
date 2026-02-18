package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Phases: phase boundaries and percentages (DRYING, MAILLARD, DEVELOPMENT).
 */
class PhasesTest {

    private static List<Double> timex(double... t) {
        return Arrays.asList(Arrays.stream(t).boxed().toArray(Double[]::new));
    }

    private static List<Integer> timeindex(int charge, int dryEnd, int fcStart, int drop) {
        return Arrays.asList(charge, dryEnd, fcStart, 0, 0, 0, drop, 0);
    }

    @Test
    void phaseBoundariesCorrect() {
        // CHARGE=0, DRY_END=2 (60s), FC_START=4 (180s), DROP=6 (300s). Total 300s.
        List<Double> timex = timex(0, 30, 60, 120, 180, 240, 300);
        List<Double> temp2 = Arrays.asList(20.0, 80.0, 140.0, 180.0, 200.0, 210.0, 215.0);
        List<Integer> ti = timeindex(0, 2, 4, 6);

        PhaseResult r = Phases.compute(timex, temp2, ti);

        assertFalse(r.isInvalid());
        assertEquals(300.0, r.getTotalTimeSec(), 1e-6);
        assertEquals(60.0, r.getDryingTimeSec(), 1e-6);   // 0 to 60
        assertEquals(120.0, r.getMaillardTimeSec(), 1e-6); // 60 to 180
        assertEquals(120.0, r.getDevelopmentTimeSec(), 1e-6); // 180 to 300
    }

    @Test
    void phasePercentagesOfTotalRoastTime() {
        List<Double> timex = timex(0, 60, 120, 180, 240, 300); // 300s total
        List<Double> temp2 = Arrays.asList(20.0, 80.0, 140.0, 200.0, 210.0, 215.0);
        // Drying 60s, Maillard 120s, Development 120s -> 20%, 40%, 40%
        List<Integer> ti = timeindex(0, 1, 3, 5);

        PhaseResult r = Phases.compute(timex, temp2, ti);

        assertFalse(r.isInvalid());
        assertEquals(300.0, r.getTotalTimeSec(), 1e-6);
        assertEquals(20.0, r.getDryingPercent(), 0.01);
        assertEquals(40.0, r.getMaillardPercent(), 0.01);
        assertEquals(40.0, r.getDevelopmentPercent(), 0.01);
    }

    @Test
    void edgeCaseDropBeforeFcInvalid() {
        // DROP at index 1 (60s), FC_START at index 3 (180s) -> DROP before FC = invalid profile
        List<Double> timex = timex(0, 60, 120, 180, 240);
        List<Double> temp2 = Arrays.asList(20.0, 80.0, 140.0, 200.0, 210.0);
        List<Integer> ti = Arrays.asList(0, 0, 3, 0, 0, 0, 1, 0); // charge=0, dryEnd=0, fcStart=3, drop=1

        PhaseResult r = Phases.compute(timex, temp2, ti);

        assertTrue(r.isInvalid());
    }

    @Test
    void edgeCaseMissingEvents() {
        // No DRY_END, no FC_START: only CHARGE and DROP
        List<Double> timex = timex(0, 60, 120, 180);
        List<Double> temp2 = Arrays.asList(20.0, 80.0, 140.0, 200.0);
        List<Integer> ti = timeindex(0, 0, 0, 3); // only charge and drop set

        PhaseResult r = Phases.compute(timex, temp2, ti);

        assertFalse(r.isInvalid());
        assertEquals(180.0, r.getTotalTimeSec(), 1e-6);
        assertEquals(0.0, r.getDryingTimeSec(), 1e-6);
        assertEquals(0.0, r.getMaillardTimeSec(), 1e-6);
        assertEquals(180.0, r.getDevelopmentTimeSec(), 1e-6);
        assertEquals(0.0, r.getDryingPercent(), 0.01);
        assertEquals(0.0, r.getMaillardPercent(), 0.01);
        assertEquals(100.0, r.getDevelopmentPercent(), 0.01);
    }

    @Test
    void edgeCaseEmptyData() {
        PhaseResult r = Phases.compute(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(r.isInvalid());
        assertEquals(0.0, r.getTotalTimeSec());
    }

    @Test
    void edgeCaseNullProfile() {
        PhaseResult r = Phases.compute((ProfileData) null);
        assertTrue(r.isInvalid());
    }

    @Test
    void computeFromProfileData() {
        ProfileData p = new ProfileData();
        p.setTimex(timex(0, 60, 120, 180));
        p.setTemp2(Arrays.asList(20.0, 80.0, 140.0, 200.0));
        p.setTimeindex(timeindex(0, 1, 2, 3));

        PhaseResult r = Phases.compute(p);

        assertFalse(r.isInvalid());
        assertEquals(180.0, r.getTotalTimeSec(), 1e-6);
        assertEquals(60.0, r.getDryingTimeSec(), 1e-6);
        assertEquals(60.0, r.getMaillardTimeSec(), 1e-6);
        assertEquals(60.0, r.getDevelopmentTimeSec(), 1e-6);
        assertEquals(100.0 / 3.0, r.getDryingPercent(), 0.1);
        assertEquals(100.0 / 3.0, r.getMaillardPercent(), 0.1);
        assertEquals(100.0 / 3.0, r.getDevelopmentPercent(), 0.1);
    }
}
