package org.artisan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RorCalculatorTest {

    private RorCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RorCalculator();
    }

    @Test
    void emptyInputReturnsEmptyOutput() {
        List<Double> timex = List.of();
        List<Double> temps = List.of();
        List<Double> ror = calculator.computeRoR(timex, temps, 2);
        assertNotNull(ror);
        assertTrue(ror.isEmpty());
    }

    @Test
    void singlePointReturnsSingleZero() {
        List<Double> timex = List.of(0.0);
        List<Double> temps = List.of(100.0);
        List<Double> ror = calculator.computeRoR(timex, temps, 2);
        assertNotNull(ror);
        assertEquals(1, ror.size());
        assertEquals(0.0, ror.get(0), 0.01);
    }

    @Test
    void knownTimeAndTempYieldsExpectedRoR() {
        // Linear ramp: 0s -> 0°C, 60s -> 60°C => 60°C/min
        List<Double> timex = List.of(0.0, 30.0, 60.0, 90.0, 120.0);
        List<Double> temps = List.of(0.0, 30.0, 60.0, 90.0, 120.0);
        int window = 1; // 2 points per RoR: (T[i]-T[i-1])/(t[i]-t[i-1])*60
        List<Double> ror = calculator.computeRoR(timex, temps, window);
        assertNotNull(ror);
        assertEquals(5, ror.size());
        assertEquals(0.0, ror.get(0), 0.1);
        // ror[1] = (30-0)/(30-0)*60 = 60
        assertEquals(60.0, ror.get(1), 0.1);
        assertEquals(60.0, ror.get(2), 0.1);
        assertEquals(60.0, ror.get(3), 0.1);
        assertEquals(60.0, ror.get(4), 0.1);
    }

    @Test
    void smoothingWindowTwoPoints() {
        // time 0,1,2,3 sec; temp 0,10,20,30 => 10°C/sec = 600 °C/min over each 1s step
        List<Double> timex = List.of(0.0, 1.0, 2.0, 3.0);
        List<Double> temps = List.of(0.0, 10.0, 20.0, 30.0);
        List<Double> ror = calculator.computeRoR(timex, temps, 1);
        assertEquals(4, ror.size());
        assertEquals(0.0, ror.get(0), 0.1);
        assertEquals(600.0, ror.get(1), 0.1);  // (10-0)/(1-0)*60
        assertEquals(600.0, ror.get(2), 0.1);
        assertEquals(600.0, ror.get(3), 0.1);
    }

    @Test
    void smoothingWindowLargerThanOne() {
        // window=2: need 3 points for first non-zero RoR. 0,2,4 sec -> 0,20,40 °C => (40-0)/(4-0)*60 = 600 °C/min at index 2
        List<Double> timex = List.of(0.0, 2.0, 4.0);
        List<Double> temps = List.of(0.0, 20.0, 40.0);
        List<Double> ror = calculator.computeRoR(timex, temps, 2);
        assertEquals(3, ror.size());
        assertEquals(0.0, ror.get(0), 0.1);
        assertEquals(0.0, ror.get(1), 0.1);
        assertEquals(600.0, ror.get(2), 0.1);
    }

    @Test
    void nullInputThrows() {
        try {
            calculator.computeRoR(null, List.of(1.0), 1);
            throw new AssertionError("Expected NPE");
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("timex") || e.getMessage() == null);
        }
        try {
            calculator.computeRoR(List.of(1.0), null, 1);
            throw new AssertionError("Expected NPE");
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("temps") || e.getMessage() == null);
        }
    }
}
