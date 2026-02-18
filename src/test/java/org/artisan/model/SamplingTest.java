package org.artisan.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SamplingTest {

    private Sampling sampling;

    @AfterEach
    void tearDown() {
        if (sampling != null) {
            sampling.stop();
        }
    }

    @Test
    void initAndReset() {
        ArtisanTime timeclock = new ArtisanTime();
        sampling = new Sampling(timeclock);
        assertEquals(Sampling.DEFAULT_DELAY_MS, sampling.getDelayMs());
        assertFalse(sampling.isRunning());

        sampling.setSamplingRate(500);
        assertEquals(500, sampling.getDelayMs());
        sampling.reset();
        assertEquals(Sampling.DEFAULT_DELAY_MS, sampling.getDelayMs());
        assertFalse(sampling.isRunning());
    }

    @Test
    void intervalBetweenSamples() throws InterruptedException {
        ArtisanTime timeclock = new ArtisanTime();
        timeclock.setBase(1000);
        sampling = new Sampling(timeclock);
        int intervalMs = 80;
        sampling.setSamplingRate(intervalMs);

        List<Long> ticks = new ArrayList<>();
        CountDownLatch atLeastThree = new CountDownLatch(3);
        Runnable onSample = () -> {
            ticks.add(System.currentTimeMillis());
            atLeastThree.countDown();
        };

        sampling.start(onSample);
        assertTrue(sampling.isRunning());
        boolean completed = atLeastThree.await(500, TimeUnit.MILLISECONDS);
        assertTrue(completed, "Expected at least 3 sample ticks within 500ms");

        sampling.stop();
        assertFalse(sampling.isRunning());

        for (int i = 1; i < ticks.size(); i++) {
            long gap = ticks.get(i) - ticks.get(i - 1);
            assertTrue(gap >= intervalMs * 0.7,
                    "Gap between samples should be at least ~70% of interval: gap=" + gap + " interval=" + intervalMs);
        }
    }

    @Test
    void zeroIntervalClampedToMin() {
        ArtisanTime timeclock = new ArtisanTime();
        sampling = new Sampling(timeclock);
        sampling.setSamplingRate(0);
        assertEquals(Sampling.MIN_DELAY_MS, sampling.getDelayMs());
    }

    @Test
    void veryLargeInterval() {
        ArtisanTime timeclock = new ArtisanTime();
        sampling = new Sampling(timeclock);
        int large = 3600_000;
        sampling.setSamplingRate(large);
        assertEquals(large, sampling.getDelayMs());
        sampling.start(() -> {});
        assertTrue(sampling.isRunning());
        sampling.stop();
        assertFalse(sampling.isRunning());
    }

    @Test
    void afterStopNoMoreTicks() throws InterruptedException {
        ArtisanTime timeclock = new ArtisanTime();
        sampling = new Sampling(timeclock);
        sampling.setSamplingRate(50);

        AtomicInteger count = new AtomicInteger(0);
        sampling.start(count::incrementAndGet);

        while (count.get() < 2) {
            Thread.sleep(10);
        }
        int countBeforeStop = count.get();
        sampling.stop();
        assertFalse(sampling.isRunning());

        Thread.sleep(200);
        int countAfterWait = count.get();
        assertEquals(countBeforeStop, countAfterWait,
                "After stop(), tick count must not increase (before=" + countBeforeStop + " after=" + countAfterWait + ")");
    }

    @Test
    void getElapsedMsAdvancesAfterStart() throws InterruptedException {
        ArtisanTime timeclock = new ArtisanTime();
        timeclock.setBase(1000);
        sampling = new Sampling(timeclock);
        sampling.setSamplingRate(200);
        sampling.start(() -> {});

        Thread.sleep(60);
        double elapsed = sampling.getElapsedMs();
        assertTrue(elapsed >= 50, "Elapsed should advance after start: " + elapsed);

        sampling.stop();
    }
}
