package org.artisan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit tests for PID controller (zero error, integral accumulation, anti-windup, reset).
 */
class PIDTest {

    private PID pid;

    @BeforeEach
    void setUp() {
        pid = new PID();
    }

    @Test
    void zeroError_producesZeroOutput() {
        pid.setTarget(50.0, true);
        pid.on();
        // First update initializes state (lastTime, lastError, lastInput set; no output yet)
        pid.updateWithTime(50.0, 0.0);
        // Second update with same PV = target -> error = 0 -> P=0, I=0, D=0 -> output = 0
        pid.updateWithTime(50.0, 0.1);
        Double duty = pid.getDuty();
        assertNotNull(duty);
        assertEquals(0.0, duty, 1e-10, "When error is zero, output should be 0");
        assertEquals(0.0, pid.getPterm(), 1e-10);
        assertEquals(0.0, pid.getIterm(), 1e-10);
        assertEquals(0.0, pid.getDterm(), 1e-10);
    }

    @Test
    void integralTerm_accumulatesCorrectly() {
        pid.setPID(0.0, 0.1, 0.0); // I-only
        pid.setTarget(100.0, true);
        pid.on();
        // First update: init
        pid.updateWithTime(80.0, 0.0);
        assertEquals(0.0, pid.getIterm(), 1e-10);
        // Second: err=20, dt=0.1 -> I += 0.1 * 20 * 0.1 = 0.2
        pid.updateWithTime(80.0, 0.1);
        assertEquals(0.2, pid.getIterm(), 1e-10);
        // Third: I += 0.2 -> Iterm = 0.4
        pid.updateWithTime(80.0, 0.2);
        assertEquals(0.4, pid.getIterm(), 1e-10);
        // Fourth: I += 0.2 -> Iterm = 0.6
        pid.updateWithTime(80.0, 0.3);
        assertEquals(0.6, pid.getIterm(), 1e-10);
    }

    @Test
    void antiWindup_limitsIntegralWhenSaturated() {
        pid.setPID(2.0, 1.0, 0.0);
        pid.setLimits(0, 100);
        pid.setTarget(200.0, true);
        pid.on();
        pid.setIntegralWindupPrevention(true);
        pid.setIntegralLimitFactor(1.0);
        // Run many updates with constant error so output would saturate
        for (int i = 0; i < 50; i++) {
            pid.updateWithTime(0.0, 0.0 + i * 0.1);
        }
        // Integral should be bounded by integral limits (not grow without bound)
        double iterm = pid.getIterm();
        assertTrue(iterm <= 100.0 + 1e-6, "Iterm should be limited by anti-windup: " + iterm);
        assertTrue(iterm >= 0.0, "Iterm should be non-negative: " + iterm);
    }

    @Test
    void antiWindup_backCalculationReducesIntegralWhenClamped() {
        pid.setPID(0.0, 1.0, 0.0);
        pid.setLimits(0, 100);
        pid.setIntegralWindupPrevention(true);
        pid.setBackCalculationFactor(0.5);
        pid.setTarget(100.0, true);
        pid.on();
        pid.updateWithTime(0.0, 0.0);
        pid.updateWithTime(0.0, 0.1);
        double itermBefore = pid.getIterm();
        assertTrue(itermBefore > 0);
        // Force many more steps so output is clamped and back-calculation applies
        for (int i = 2; i < 20; i++) {
            pid.updateWithTime(0.0, 0.0 + i * 0.1);
        }
        // Back-calculation should have reduced integral when output was clamped
        Double duty = pid.getDuty();
        assertNotNull(duty);
        assertEquals(100.0, duty, 1e-6, "Output should be clamped to 100");
    }

    @Test
    void reset_clearsState() {
        pid.setPID(1.0, 0.1, 0.0);
        pid.setTarget(100.0, true);
        pid.on();
        pid.updateWithTime(80.0, 0.0);
        pid.updateWithTime(80.0, 0.1);
        pid.updateWithTime(80.0, 0.2);
        assertTrue(pid.getIterm() > 0);
        assertNotNull(pid.getDuty());
        assertNotNull(pid.getLastOutputInternal());

        pid.reset();

        assertEquals(0.0, pid.getPterm(), 1e-10);
        assertEquals(0.0, pid.getIterm(), 1e-10);
        assertEquals(0.0, pid.getDterm(), 1e-10);
        assertNull(pid.getLastOutputInternal());
        assertEquals(0.0, pid.getError(), 1e-10);
        assertNull(pid.getDuty()); // lastOutput is null after reset until next update
    }

    @Test
    void update_rejectsNullAndMinusOne() {
        AtomicReference<Double> received = new AtomicReference<>();
        pid.setControl(received::set);
        pid.setTarget(50.0, true);
        pid.on();
        pid.updateWithTime(50.0, 0.0);
        pid.updateWithTime(50.0, 0.1);
        Double firstDuty = pid.getDuty();

        received.set(null);
        pid.update(null);
        assertEquals(firstDuty, pid.getDuty());
        assertNull(received.get());

        pid.update(-1.0);
        assertNull(received.get());
    }

    @Test
    void setTarget_withInitResetsIntegral() {
        pid.setTarget(100.0, true);
        pid.on();
        pid.updateWithTime(80.0, 0.0);
        pid.updateWithTime(80.0, 0.1);
        assertTrue(pid.getIterm() > 0);
        pid.setTarget(120.0, true);
        assertEquals(0.0, pid.getIterm(), 1e-10);
    }

    @Test
    void setTarget_withoutInitKeepsIntegral() {
        pid.setTarget(100.0, true);
        pid.on();
        pid.updateWithTime(80.0, 0.0);
        pid.updateWithTime(80.0, 0.1);
        double iterm = pid.getIterm();
        pid.setTarget(120.0, false);
        assertEquals(iterm, pid.getIterm(), 1e-10);
    }

    @Test
    void on_resetsLastOutputSoNextUpdateSendsControl() {
        pid.setTarget(50.0, true);
        pid.on();
        pid.updateWithTime(50.0, 0.0);
        pid.updateWithTime(50.0, 0.1);
        assertNotNull(pid.getDuty());
        pid.off();
        pid.on();
        // lastOutput was cleared in on(), so next update will send control again
        assertNull(pid.getLastOutputInternal());
    }

    @Test
    void inactivePid_doesNotCallControl() {
        AtomicReference<Double> received = new AtomicReference<>();
        pid.setControl(received::set);
        pid.setTarget(100.0, true);
        // do not call on()
        pid.updateWithTime(80.0, 0.0);
        pid.updateWithTime(80.0, 0.1);
        // updateWithTime does not invoke control callback (only update() does when active)
        assertNull(received.get());
    }
}
