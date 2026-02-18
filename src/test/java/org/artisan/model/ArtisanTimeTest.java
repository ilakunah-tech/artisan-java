package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtisanTimeTest {

    @Test
    void startAndElapsed() {
        ArtisanTime t = new ArtisanTime();
        t.start();
        try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        double e = t.elapsed();
        assertTrue(e >= 15, "elapsed ms should be at least 15: " + e);
    }

    @Test
    void baseAndElapsedMilli() {
        ArtisanTime t = new ArtisanTime();
        t.setBase(1000);
        t.start();
        double milli = t.elapsedMilli();
        assertTrue(milli >= 0);
    }

    @Test
    void addClock() {
        ArtisanTime t = new ArtisanTime();
        t.start();
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        double before = t.elapsed();
        t.addClock(10.0); // add 10 seconds to clock -> elapsed drops
        double after = t.elapsed();
        assertTrue(after < before, "addClock(10) should reduce elapsed: before=" + before + " after=" + after);
    }
}
