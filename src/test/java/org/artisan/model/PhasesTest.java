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
 * Includes manual threshold (BT crossing) and effective timeindex tests.
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

    @Test
    void timeindexFromIndices() {
        List<Integer> ti = Phases.timeindexFromIndices(0, 2, 4, 6);
        assertEquals(8, ti.size());
        assertEquals(0, ti.get(0));
        assertEquals(2, ti.get(1));
        assertEquals(4, ti.get(2));
        assertEquals(6, ti.get(6));
        List<Integer> ti2 = Phases.timeindexFromIndices(-1, 0, 0, 0);
        assertEquals(0, ti2.get(0));
        assertEquals(0, ti2.get(1));
    }

    @Test
    void manualThresholdsBtCrossingDryAndFcs() {
        // BT: 100, 120, 145, 155, 180, 198, 205 -> first >=150 at index 3, first >=195 at index 5
        List<Double> timex = timex(0, 60, 120, 180, 240, 300, 360);
        List<Double> temp2 = Arrays.asList(100.0, 120.0, 145.0, 155.0, 180.0, 198.0, 205.0);
        List<Integer> ti = timeindex(0, 0, 0, 6); // charge=0, no dry/fcs set, drop=6
        PhasesConfig config = new PhasesConfig();
        config.setAutoAdjustedLimits(false);
        config.setDryEndTempC(150.0);
        config.setFcsTempC(195.0);

        List<Integer> effective = Phases.getEffectiveTimeindex(timex, temp2, ti, config);

        assertEquals(3, effective.get(1).intValue()); // dryEnd at index 3
        assertEquals(5, effective.get(2).intValue()); // fcStart at index 5
        PhaseResult r = Phases.compute(timex, temp2, effective);
        assertFalse(r.isInvalid());
        assertEquals(180.0, r.getDryingTimeSec(), 1e-6);   // timex[3]-timex[0] = 180
        assertEquals(120.0, r.getMaillardTimeSec(), 1e-6); // timex[5]-timex[3] = 120
        assertEquals(60.0, r.getDevelopmentTimeSec(), 1e-6); // timex[6]-timex[5] = 60
    }

    @Test
    void computeWithConfigUsesManualWhenNoEvents() {
        ProfileData p = new ProfileData();
        p.setTimex(timex(0, 60, 120, 180, 240, 300));
        p.setTemp2(Arrays.asList(90.0, 130.0, 160.0, 200.0, 210.0, 215.0)); // cross 150 at 2, 195 at 3
        p.setTimeindex(timeindex(0, 0, 0, 5));
        PhasesConfig config = new PhasesConfig();
        config.setAutoAdjustedLimits(false);
        config.setDryEndTempC(150.0);
        config.setFcsTempC(195.0);

        PhaseResult r = Phases.compute(p, config);

        assertFalse(r.isInvalid());
        assertEquals(300.0, r.getTotalTimeSec(), 1e-6);
        assertEquals(120.0, r.getDryingTimeSec(), 1e-6);   // 0 to index 2
        assertEquals(60.0, r.getMaillardTimeSec(), 1e-6);  // index 2 to 3
        assertEquals(120.0, r.getDevelopmentTimeSec(), 1e-6); // index 3 to 5
    }

    @Test
    void autoAdjustedUsesExistingEventsWhenSet() {
        List<Double> timex = timex(0, 60, 120, 180, 240, 300);
        List<Double> temp2 = Arrays.asList(90.0, 130.0, 160.0, 200.0, 210.0, 215.0);
        List<Integer> ti = timeindex(0, 1, 3, 5); // explicit dry=1, fcs=3
        PhasesConfig config = new PhasesConfig();
        config.setAutoAdjustedLimits(true);

        List<Integer> effective = Phases.getEffectiveTimeindex(timex, temp2, ti, config);

        assertEquals(1, effective.get(1).intValue());
        assertEquals(3, effective.get(2).intValue());
    }

    @Test
    void edgeCaseNullConfigReturnsCopyOfTimeindex() {
        List<Double> timex = timex(0, 60, 120);
        List<Double> temp2 = Arrays.asList(100.0, 150.0, 200.0);
        List<Integer> ti = timeindex(0, 1, 2, 2);
        List<Integer> effective = Phases.getEffectiveTimeindex(timex, temp2, ti, null);
        assertEquals(8, effective.size());
        assertEquals(0, effective.get(0).intValue());
        assertEquals(1, effective.get(1).intValue());
        assertEquals(2, effective.get(2).intValue());
    }

    @Test
    void edgeCaseOutOfOrderEventsInvalidResultDoesNotThrow() {
        List<Double> timex = timex(0, 60, 120, 180);
        List<Double> temp2 = Arrays.asList(20.0, 80.0, 140.0, 200.0);
        List<Integer> ti = Arrays.asList(0, 2, 1, 0, 0, 0, 3, 0); // dryEnd=2, fcStart=1 -> fc before dry
        PhaseResult r = Phases.compute(timex, temp2, ti);
        // Computation still returns; development may be 0 or invalid
        assertTrue(r.getTotalTimeSec() >= 0);
    }

    @Test
    void edgeCaseEmptyTimeindexGraceful() {
        List<Double> timex = timex(0, 60);
        List<Double> temp2 = Arrays.asList(100.0, 200.0);
        List<Integer> empty = Collections.emptyList();
        List<Integer> effective = Phases.getEffectiveTimeindex(timex, temp2, empty, new PhasesConfig());
        assertEquals(8, effective.size());
        PhaseResult r = Phases.compute(timex, temp2, effective);
        assertFalse(r.isInvalid());
        assertEquals(60.0, r.getTotalTimeSec(), 1e-6);
    }
}
