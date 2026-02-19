package org.artisan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LiveRorCalculatorTest {

    private RorCalculator batchCalculator;

    @BeforeEach
    void setUp() {
        batchCalculator = new RorCalculator();
    }

    @Test
    void liveMatchesBatch_onSameData() {
        List<Double> timex = List.of(0.0, 1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> temps = List.of(0.0, 10.0, 20.0, 30.0, 40.0, 50.0);
        int window = 2;

        List<Double> batchRoR = batchCalculator.computeRoR(timex, temps, window);

        LiveRorCalculator live = new LiveRorCalculator(window);
        List<Double> liveRoR = new ArrayList<>();
        for (int i = 0; i < timex.size(); i++) {
            double r = live.addSample(timex.get(i), temps.get(i));
            liveRoR.add(r);
        }

        assertEquals(batchRoR.size(), liveRoR.size());
        for (int i = 0; i < batchRoR.size(); i++) {
            assertEquals(batchRoR.get(i), liveRoR.get(i), 0.01, "index " + i);
        }
    }

    @Test
    void reset_clearsBuffer() {
        LiveRorCalculator live = new LiveRorCalculator(2);
        live.addSample(0.0, 0.0);
        live.addSample(1.0, 10.0);
        live.addSample(2.0, 20.0);
        double r = live.addSample(3.0, 30.0);
        assertEquals(600.0, r, 0.1);

        live.reset();
        assertEquals(0.0, live.addSample(0.0, 100.0), 0.01);
        assertEquals(0.0, live.addSample(1.0, 100.0), 0.01);
    }

    @Test
    void notEnoughSamples_returnsZero() {
        LiveRorCalculator live = new LiveRorCalculator(3);
        assertEquals(0.0, live.addSample(0.0, 0.0), 0.01);
        assertEquals(0.0, live.addSample(1.0, 10.0), 0.01);
        assertEquals(0.0, live.addSample(2.0, 20.0), 0.01);
        double r = live.addSample(3.0, 30.0);
        assertEquals(600.0, r, 0.1);
    }
}
