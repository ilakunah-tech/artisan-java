package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoastReportTest {

    @Test
    void generate_containsRoastInfo() {
        ProfileData pd = new ProfileData();
        RoastProperties props = new RoastProperties();
        props.setTitle("Test Roast");
        CupProfile cup = new CupProfile();
        PhaseResult phases = new PhaseResult(0, 0, 0, 0, 0, 0, 0, true);
        RoastStats stats = new RoastStats(0, 0, 0, 0, 0, 0, true);
        String result = RoastReport.generate(pd, props, cup, phases, stats);
        assertNotNull(result);
        assertTrue(result.contains("ROAST INFO"));
    }

    @Test
    void generate_containsPhases() {
        ProfileData pd = new ProfileData();
        RoastProperties props = new RoastProperties();
        CupProfile cup = new CupProfile();
        PhaseResult phases = new PhaseResult(300, 60, 120, 120, 20, 40, 40, false);
        RoastStats stats = new RoastStats(0, 0, 0, 0, 0, 300, false);
        String result = RoastReport.generate(pd, props, cup, phases, stats);
        assertNotNull(result);
        assertTrue(result.contains("PHASES"));
    }

    @Test
    void generate_containsCupProfile() {
        ProfileData pd = new ProfileData();
        RoastProperties props = new RoastProperties();
        CupProfile cup = new CupProfile();
        PhaseResult phases = new PhaseResult(0, 0, 0, 0, 0, 0, 0, true);
        RoastStats stats = new RoastStats(0, 0, 0, 0, 0, 0, true);
        String result = RoastReport.generate(pd, props, cup, phases, stats);
        assertNotNull(result);
        assertTrue(result.contains("CUP PROFILE"));
    }
}
