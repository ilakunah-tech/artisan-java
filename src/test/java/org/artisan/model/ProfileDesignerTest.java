package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileDesignerTest {

    @Test
    void addBtPoint_sortedByTime() {
        ProfileDesigner d = new ProfileDesigner();
        d.addBtPoint(100, 180);
        d.addBtPoint(50, 160);
        assertEquals(2, d.getBtPoints().size());
        assertTrue(d.getBtPoints().get(0).getTimeSec() <= d.getBtPoints().get(1).getTimeSec());
        assertEquals(50, d.getBtPoints().get(0).getTimeSec(), 1e-9);
    }

    @Test
    void removeBtPoint_decreasesSize() {
        ProfileDesigner d = new ProfileDesigner();
        d.loadDefaults();
        int n = d.getBtPoints().size();
        d.removeBtPoint(0);
        assertEquals(n - 1, d.getBtPoints().size());
    }

    @Test
    void generate_profileNotNull() {
        ProfileDesigner d = new ProfileDesigner();
        d.loadDefaults();
        assertNotNull(d.generate());
    }

    @Test
    void generate_timexMatchesSamplingInterval() {
        ProfileDesigner d = new ProfileDesigner();
        d.loadDefaults();
        d.setSamplingInterval(1.0);
        ProfileData pd = d.generate();
        assertNotNull(pd);
        int size = pd.getTimex().size();
        assertTrue(size >= 599 && size <= 603, "timex size expected ~601, got " + size);
    }

    @Test
    void generate_btAtFirstPoint_nearChargeTemp() {
        ProfileDesigner d = new ProfileDesigner();
        d.loadDefaults();
        d.setSamplingInterval(1.0);
        ProfileData pd = d.generate();
        assertNotNull(pd);
        assertFalse(pd.getTemp2().isEmpty());
        assertEquals(200.0, pd.getTemp2().get(0), 5.0);
    }

    @Test
    void loadDefaults_addsPoints() {
        ProfileDesigner d = new ProfileDesigner();
        d.loadDefaults();
        assertEquals(5, d.getBtPoints().size());
        assertEquals(5, d.getEtPoints().size());
    }

    @Test
    void clear_emptiesPoints() {
        ProfileDesigner d = new ProfileDesigner();
        d.loadDefaults();
        d.clear();
        assertTrue(d.getBtPoints().isEmpty());
        assertTrue(d.getEtPoints().isEmpty());
    }
}

