package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Calculator: DTR, AUC, zero roast time.
 * Values from formula (main.py:12999, calcAUC/profileAUC); no sample files in repo.
 */
class CalculatorTest {

    @Test
    void developmentTimeRatio() {
        // DTR = 100 * (DROP_time - FCs_time) / DROP_time. main.py:12999
        // Example: 300s total, FC at 120s -> development 180s -> DTR = 100*180/300 = 60
        double dtr = Calculator.developmentTimeRatio(300.0, 120.0);
        assertEquals(60.0, dtr, 0.01);
    }

    @Test
    void developmentTimeRatioZeroDropTime() {
        double dtr = Calculator.developmentTimeRatio(0.0, 100.0);
        assertEquals(0.0, dtr);
    }

    @Test
    void areaUnderCurve() {
        // Trapezoidal: segment [0,60] with T 100, 110 C, base 90 -> (100+110)/2 - 90 = 15, * 60s = 900 C·s -> 15 C·min
        List<Double> timex = Arrays.asList(0.0, 60.0);
        List<Double> temp2 = Arrays.asList(100.0, 110.0);
        double auc = Calculator.areaUnderCurve(timex, temp2, 90.0, 0, 1);
        assertEquals(15.0, auc, 0.01);
    }

    @Test
    void areaUnderCurveZeroRoastTime() {
        List<Double> timex = Collections.singletonList(0.0);
        List<Double> temp2 = Collections.singletonList(200.0);
        double auc = Calculator.areaUnderCurve(timex, temp2, 100.0, 0, 0);
        assertEquals(0.0, auc);
    }

    @Test
    void areaUnderCurveFromProfile() {
        ProfileData p = new ProfileData();
        p.setTimex(Arrays.asList(0.0, 60.0, 120.0));
        p.setTemp2(Arrays.asList(100.0, 150.0, 200.0));
        p.setTimeindex(Arrays.asList(0, 0, 0, 0, 0, 0, 2, 0));
        double auc = Calculator.areaUnderCurve(p, 80.0);
        // Segment 0->1: (100+150)/2 - 80 = 45, * 60 = 2700. Segment 1->2: (150+200)/2 - 80 = 95, * 60 = 5700. Sum 8400/60 = 140 C·min
        assertEquals(140.0, auc, 0.1);
    }

    @Test
    void areaUnderCurveNullProfile() {
        assertEquals(0.0, Calculator.areaUnderCurve((ProfileData) null, 100.0));
    }

    @Test
    void developmentTimeRatioFromPhaseResult() {
        // total 300s, development 180s -> fcs at 120s -> DTR = 60%
        PhaseResult phase = new PhaseResult(300, 60, 120, 180, 20, 40, 40, false);
        assertEquals(60.0, Calculator.developmentTimeRatio(phase), 0.01);
    }

    @Test
    void developmentTimeRatioPhaseResultInvalid() {
        assertEquals(0.0, Calculator.developmentTimeRatio(null));
        PhaseResult invalid = new PhaseResult(100, 0, 0, 0, 0, 0, 0, true);
        assertEquals(0.0, Calculator.developmentTimeRatio(invalid), 0.0);
    }

    @Test
    void areaUnderCurve_belowBase() {
        // All BT below base -> AUC == 0
        List<Double> timex = Arrays.asList(0.0, 60.0, 120.0);
        List<Double> temp2 = Arrays.asList(50.0, 70.0, 90.0);
        double auc = Calculator.areaUnderCurve(timex, temp2, 100.0, 0, 2);
        assertEquals(0.0, auc, 0.01);
    }

    @Test
    void areaUnderCurve_linearRamp_matchesAnalytical() {
        // BT = 100 + t (linear 1 °C/s from 0 to 60s). Base 50. Average above base = 100 + 30 - 50 = 80 over 60s -> 80 * 60 / 60 = 80 C·min
        List<Double> timex = Arrays.asList(0.0, 60.0);
        List<Double> temp2 = Arrays.asList(100.0, 160.0);
        double auc = Calculator.areaUnderCurve(timex, temp2, 50.0, 0, 1);
        assertEquals(80.0, auc, 0.1);
    }
}
