package org.artisan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RampSoakProgramTest {

    private RampSoakProgram program;

    @BeforeEach
    void setUp() {
        program = new RampSoakProgram();
    }

    @Test
    void getSetpointDuringRamp() {
        program.setSegments(List.of(
            new RampSoakSegment(100, 50, 200.0)
        ));
        // At t=0: before segment start -> first segment starts at 0, prev temp = 0
        assertEquals(0.0, program.getCurrentSetpoint(0), 1e-10);
        // At t=50 (half of 100s ramp): linear interpolation 0 -> 200
        assertEquals(100.0, program.getCurrentSetpoint(50), 1e-6);
        // At t=100: end of ramp
        assertEquals(200.0, program.getCurrentSetpoint(100), 1e-10);
    }

    @Test
    void getSetpointDuringSoak() {
        program.setSegments(List.of(
            new RampSoakSegment(60, 40, 180.0)
        ));
        // During soak (60..100): flat at 180
        assertEquals(180.0, program.getCurrentSetpoint(60), 1e-10);
        assertEquals(180.0, program.getCurrentSetpoint(80), 1e-10);
        assertEquals(180.0, program.getCurrentSetpoint(100), 1e-10);
    }

    @Test
    void getSetpointAfterFinish() {
        program.setSegments(List.of(
            new RampSoakSegment(10, 10, 150.0)
        ));
        // After segment end (t>=20): last targetTemp
        assertEquals(150.0, program.getCurrentSetpoint(20), 1e-10);
        assertEquals(150.0, program.getCurrentSetpoint(100), 1e-10);
    }

    @Test
    void isFinished() {
        program.setSegments(List.of(
            new RampSoakSegment(10, 5, 200.0)
        ));
        assertFalse(program.isFinished(0));
        assertFalse(program.isFinished(14));
        assertTrue(program.isFinished(15));
        assertTrue(program.isFinished(20));
    }

    @Test
    void reset() {
        program.setSegments(List.of(new RampSoakSegment(10, 10, 100.0)));
        program.reset();
        assertEquals(0, program.getCurrentSegmentIndex());
        assertEquals(0.0, program.getSegmentElapsedSeconds(), 1e-10);
    }

    @Test
    void twoSegments_rampAndSoak() {
        program.setSegments(List.of(
            new RampSoakSegment(20, 10, 100.0),   // 0..20 ramp, 20..30 soak
            new RampSoakSegment(10, 5, 150.0)    // 30..40 ramp, 40..45 soak
        ));
        assertEquals(0.0, program.getCurrentSetpoint(0), 1e-10);
        assertEquals(50.0, program.getCurrentSetpoint(10), 1e-6);
        assertEquals(100.0, program.getCurrentSetpoint(20), 1e-10);
        assertEquals(100.0, program.getCurrentSetpoint(30), 1e-10);
        assertEquals(125.0, program.getCurrentSetpoint(35), 1e-6); // half ramp 100->150
        assertEquals(150.0, program.getCurrentSetpoint(40), 1e-10);
        assertEquals(150.0, program.getCurrentSetpoint(45), 1e-10);
        assertEquals(150.0, program.getCurrentSetpoint(100), 1e-10);
    }
}
