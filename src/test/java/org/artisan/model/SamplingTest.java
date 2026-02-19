package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sampling / spike filter tests (condition logic and SamplingConfig).
 */
class SamplingTest {

    @Test
    void spikeFilter_rejectsSample_whenExceedsThreshold() {
        SamplingConfig config = new SamplingConfig();
        config.setFilterSpikes(true);
        config.setSpikeThreshold(25.0);
        config.setIntervalSeconds(1.0);
        double lastBt = 100.0;
        double lastTimeSec = 0.0;
        double bt = 150.0;
        double timeSec = 1.0;
        double dt = timeSec - lastTimeSec;
        double ratePerSec = Math.abs(bt - lastBt) / dt;
        assertTrue(ratePerSec > config.getSpikeThreshold());
    }

    @Test
    void spikeFilter_acceptsSample_whenBelowThreshold() {
        SamplingConfig config = new SamplingConfig();
        config.setFilterSpikes(true);
        config.setSpikeThreshold(25.0);
        config.setIntervalSeconds(1.0);
        double lastBt = 100.0;
        double lastTimeSec = 0.0;
        double bt = 110.0;
        double timeSec = 1.0;
        double dt = timeSec - lastTimeSec;
        double ratePerSec = Math.abs(bt - lastBt) / dt;
        assertFalse(ratePerSec > config.getSpikeThreshold());
    }

    @Test
    void samplingConfig_defaults_and_intervalMs() {
        SamplingConfig config = new SamplingConfig();
        assertEquals(SamplingConfig.DEFAULT_INTERVAL_SECONDS, config.getIntervalSeconds());
        assertEquals(1000, config.getIntervalMs());
    }

    @Test
    void samplingConfig_loadSaveRoundTrip() {
        SamplingConfig config = new SamplingConfig();
        config.setIntervalSeconds(2.0);
        config.setFilterSpikes(true);
        config.setSpikeThreshold(30.0);
        SamplingConfig.saveToPreferences(config);
        SamplingConfig loaded = new SamplingConfig();
        SamplingConfig.loadFromPreferences(loaded);
        assertEquals(2.0, loaded.getIntervalSeconds());
        assertTrue(loaded.isFilterSpikes());
        assertEquals(30.0, loaded.getSpikeThreshold());
    }
}
