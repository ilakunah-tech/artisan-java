package org.artisan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PIDConfigTest {

    private PIDConfig config;

    @BeforeEach
    void setUp() {
        config = new PIDConfig();
    }

    @Test
    void defaultValues() {
        config.defaults();
        assertFalse(config.isEnabled());
        assertEquals(PIDConfig.DEFAULT_KP, config.getKp(), 1e-10);
        assertEquals(PIDConfig.DEFAULT_KI, config.getKi(), 1e-10);
        assertEquals(PIDConfig.DEFAULT_KD, config.getKd(), 1e-10);
        assertEquals(PIDConfig.DEFAULT_OUTPUT_MIN, config.getOutputMin(), 1e-10);
        assertEquals(PIDConfig.DEFAULT_OUTPUT_MAX, config.getOutputMax(), 1e-10);
        assertEquals(PIDConfig.DEFAULT_SETPOINT, config.getSetpoint(), 1e-10);
        assertEquals(PIDMode.MANUAL, config.getMode());
    }

    @Test
    void saveLoadRoundtrip() {
        config.setEnabled(true);
        config.setKp(3.0);
        config.setKi(0.02);
        config.setKd(0.5);
        config.setOutputMin(5.0);
        config.setOutputMax(95.0);
        config.setSetpoint(180.0);
        config.setMode(PIDMode.RAMP_SOAK);
        config.save();

        PIDConfig loaded = new PIDConfig();
        loaded.load();
        assertEquals(config.isEnabled(), loaded.isEnabled());
        assertEquals(config.getKp(), loaded.getKp(), 1e-10);
        assertEquals(config.getKi(), loaded.getKi(), 1e-10);
        assertEquals(config.getKd(), loaded.getKd(), 1e-10);
        assertEquals(config.getOutputMin(), loaded.getOutputMin(), 1e-10);
        assertEquals(config.getOutputMax(), loaded.getOutputMax(), 1e-10);
        assertEquals(config.getSetpoint(), loaded.getSetpoint(), 1e-10);
        assertEquals(config.getMode(), loaded.getMode());
    }

    @Test
    void persistsAllFields() {
        config.setEnabled(true);
        config.setKp(1.5);
        config.setKi(0.005);
        config.setKd(2.0);
        config.setOutputMin(10.0);
        config.setOutputMax(90.0);
        config.setSetpoint(200.0);
        config.setMode(PIDMode.RAMP_SOAK);
        config.save();

        PIDConfig other = new PIDConfig();
        other.load();
        assertTrue(other.isEnabled());
        assertEquals(1.5, other.getKp(), 1e-10);
        assertEquals(0.005, other.getKi(), 1e-10);
        assertEquals(2.0, other.getKd(), 1e-10);
        assertEquals(10.0, other.getOutputMin(), 1e-10);
        assertEquals(90.0, other.getOutputMax(), 1e-10);
        assertEquals(200.0, other.getSetpoint(), 1e-10);
        assertEquals(PIDMode.RAMP_SOAK, other.getMode());
    }
}
