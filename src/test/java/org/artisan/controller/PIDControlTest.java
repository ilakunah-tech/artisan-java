package org.artisan.controller;

import org.artisan.model.PIDConfig;
import org.artisan.model.PIDMode;
import org.artisan.model.RampSoakSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PIDControlTest {

    private PIDControl pidControl;

    @BeforeEach
    void setUp() {
        pidControl = new PIDControl();
    }

    @Test
    void tickManualMode_outputInRange() {
        pidControl.getConfig().setMode(PIDMode.MANUAL);
        pidControl.getConfig().setSetpoint(150.0);
        pidControl.getConfig().setKp(2.0);
        pidControl.getConfig().setKi(0.01);
        pidControl.getConfig().setKd(0.0);
        pidControl.getConfig().setOutputMin(0.0);
        pidControl.getConfig().setOutputMax(100.0);
        pidControl.start();
        double out = pidControl.tick(100.0, 10.0); // PV=100, setpoint=150 -> positive error -> positive output
        assertTrue(out >= 0.0 && out <= 100.0, "Output should be in [0, 100]");
        pidControl.stop();
    }

    @Test
    void tickRampSoakMode_setpointFollowsProgram() {
        pidControl.getConfig().setMode(PIDMode.RAMP_SOAK);
        pidControl.getRampSoakProgram().setSegments(List.of(
            new RampSoakSegment(60, 30, 180.0)
        ));
        pidControl.getConfig().setKp(1.0);
        pidControl.getConfig().setKi(0.0);
        pidControl.getConfig().setKd(0.0);
        pidControl.start();
        double out0 = pidControl.tick(50.0, 0.0);
        assertEquals(0.0, pidControl.getSetpoint(), 1e-6); // t=0 -> setpoint 0
        double out30 = pidControl.tick(50.0, 30.0);
        assertEquals(90.0, pidControl.getSetpoint(), 1e-6);  // half ramp -> 90
        double out60 = pidControl.tick(50.0, 60.0);
        assertEquals(180.0, pidControl.getSetpoint(), 1e-6); // end ramp
        assertTrue(out0 >= 0 && out0 <= 100);
        assertTrue(out30 >= 0 && out30 <= 100);
        assertTrue(out60 >= 0 && out60 <= 100);
        pidControl.stop();
    }

    @Test
    void startStop_isRunning() {
        assertFalse(pidControl.isRunning());
        pidControl.start();
        assertTrue(pidControl.isRunning());
        pidControl.stop();
        assertFalse(pidControl.isRunning());
    }

    @Test
    void tickWhenStopped_returnsZero() {
        pidControl.getConfig().setSetpoint(200.0);
        pidControl.getConfig().setKp(5.0);
        assertFalse(pidControl.isRunning());
        double out = pidControl.tick(100.0, 10.0);
        assertEquals(0.0, out, 1e-10);
    }
}
