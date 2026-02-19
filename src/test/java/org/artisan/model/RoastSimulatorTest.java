package org.artisan.model;

import org.artisan.device.SimulatorConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoastSimulatorTest {

    @Test
    void generate_profileNotNull() {
        RoastSimulator sim = new RoastSimulator();
        sim.setChargeTemp(200);
        sim.setDropTemp(210);
        sim.setTotalTimeSeconds(600);
        SimulatorConfig cfg = new SimulatorConfig();
        cfg.setSpeedMultiplier(1.0);
        cfg.setNoiseAmplitude(0);
        sim.setConfig(cfg);
        ProfileData pd = sim.generate();
        assertNotNull(pd);
    }

    @Test
    void generate_btLength_matchesTotalTime() {
        RoastSimulator sim = new RoastSimulator();
        sim.setTotalTimeSeconds(300);
        SimulatorConfig cfg = new SimulatorConfig();
        cfg.setSpeedMultiplier(1.0);
        cfg.setNoiseAmplitude(0);
        sim.setConfig(cfg);
        ProfileData pd = sim.generate();
        assertNotNull(pd);
        List<Double> timex = pd.getTimex();
        List<Double> bt = pd.getTemp2();
        assertNotNull(timex);
        assertNotNull(bt);
        int expectedPoints = (int) Math.round(300 / 1.0);
        assertEquals(expectedPoints, timex.size());
        assertEquals(timex.size(), bt.size());
    }

    @Test
    void generate_btStartsNearChargeTemp() {
        RoastSimulator sim = new RoastSimulator();
        sim.setChargeTemp(180);
        sim.setDropTemp(210);
        sim.setTotalTimeSeconds(600);
        SimulatorConfig cfg = new SimulatorConfig();
        cfg.setSpeedMultiplier(1.0);
        cfg.setNoiseAmplitude(0);
        sim.setConfig(cfg);
        ProfileData pd = sim.generate();
        List<Double> bt = pd.getTemp2();
        assertNotNull(bt);
        assertTrue(bt.size() >= 1);
        double first = bt.get(0);
        assertTrue(Math.abs(first - 180) <= 5, "First BT should be within ±5°C of charge temp 180, got " + first);
    }

    @Test
    void generate_btEndsNearDropTemp() {
        RoastSimulator sim = new RoastSimulator();
        sim.setChargeTemp(200);
        sim.setDropTemp(210);
        sim.setTotalTimeSeconds(400);
        SimulatorConfig cfg = new SimulatorConfig();
        cfg.setSpeedMultiplier(1.0);
        cfg.setNoiseAmplitude(0);
        sim.setConfig(cfg);
        ProfileData pd = sim.generate();
        List<Double> bt = pd.getTemp2();
        assertNotNull(bt);
        assertTrue(bt.size() >= 1);
        double last = bt.get(bt.size() - 1);
        assertTrue(Math.abs(last - 210) <= 10, "Last BT should be within ±10°C of drop temp 210, got " + last);
    }

    @Test
    void generate_etAboveBT() {
        RoastSimulator sim = new RoastSimulator();
        sim.setChargeTemp(200);
        sim.setDropTemp(210);
        sim.setTotalTimeSeconds(100);
        SimulatorConfig cfg = new SimulatorConfig();
        cfg.setEtOffset(20);
        cfg.setNoiseAmplitude(0.1);
        cfg.setSpeedMultiplier(1.0);
        sim.setConfig(cfg);
        ProfileData pd = sim.generate();
        List<Double> bt = pd.getTemp2();
        List<Double> et = pd.getTemp1();
        assertNotNull(bt);
        assertNotNull(et);
        assertEquals(bt.size(), et.size());
        double margin = 20 + 0.1 * 3; // etOffset ± noiseAmplitude*3
        for (int i = 0; i < bt.size(); i++) {
            double b = bt.get(i);
            double e = et.get(i);
            assertTrue(e >= b - 0.01, "ET should be >= BT at index " + i);
            assertTrue(e <= b + margin + 1, "ET should be within etOffset+noise of BT at index " + i);
        }
    }
}
