package org.artisan.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for filter classes: LiveFilter (NaN), LiveLFilter, LiveSosFilter, LiveMedian, LiveMean.
 */
class LiveFilterTest {

    @Test
    void liveFilterPassesNan() {
        LiveMean f = new LiveMean(3);
        double nan = Double.NaN;
        assertEquals(nan, f.process(nan), 0.0);
        f.process(1.0);
        assertEquals(nan, f.process(nan), 0.0);
    }

    @Test
    void liveLFilterIdentity() {
        // b=[1], a=[1] -> y = x
        LiveLFilter f = new LiveLFilter(new double[]{1.0}, new double[]{1.0});
        assertEquals(5.0, f.process(5.0), 1e-10);
        assertEquals(-3.0, f.process(-3.0), 1e-10);
    }

    @Test
    void liveLFilterSingleTap() {
        // b=[0.5, 0.5], a=[1] -> y = 0.5*x_new + 0.5*x_old (one sample delay for x_old)
        LiveLFilter f = new LiveLFilter(new double[]{0.5, 0.5}, new double[]{1.0});
        assertEquals(0.5 * 2.0, f.process(2.0), 1e-10);  // first: 0.5*2 + 0.5*0 = 1
        assertEquals(0.5 * 4.0 + 0.5 * 2.0, f.process(4.0), 1e-10);  // 2 + 1 = 3
        assertEquals(0.5 * 6.0 + 0.5 * 4.0, f.process(6.0), 1e-10);  // 3 + 2 = 5
    }

    @Test
    void liveSosFilterIdentity() {
        double[][] sos = {{1.0, 0.0, 0.0, 1.0, 0.0, 0.0}};
        LiveSosFilter f = new LiveSosFilter(sos);
        assertEquals(7.0, f.process(7.0), 1e-10);
        assertEquals(-2.0, f.process(-2.0), 1e-10);
    }

    @Test
    void liveSosFilterTwoSections() {
        // Two identity sections: output = input
        double[][] sos = {
            {1.0, 0.0, 0.0, 1.0, 0.0, 0.0},
            {1.0, 0.0, 0.0, 1.0, 0.0, 0.0}
        };
        LiveSosFilter f = new LiveSosFilter(sos);
        assertEquals(3.0, f.process(3.0), 1e-10);
    }

    @Test
    void liveMedianOddWindow() {
        LiveMedian f = new LiveMedian(3);
        assertEquals(1.0, f.process(1.0), 1e-10);   // [1] -> mean during init
        assertEquals(1.5, f.process(2.0), 1e-10);   // [1,2] -> mean
        assertEquals(2.0, f.process(3.0), 1e-10);  // [1,2,3] median = 2
        assertEquals(3.0, f.process(4.0), 1e-10);  // [2,3,4] median = 3
        assertEquals(4.0, f.process(5.0), 1e-10);  // [3,4,5] median = 4
    }

    @Test
    void liveMedianRejectsEvenK() {
        assertThrows(IllegalArgumentException.class, () -> new LiveMedian(4));
        assertThrows(IllegalArgumentException.class, () -> new LiveMedian(0));
    }

    @Test
    void liveMeanWindow() {
        LiveMean f = new LiveMean(3);
        assertEquals(1.0, f.process(1.0), 1e-10);
        assertEquals(1.5, f.process(2.0), 1e-10);
        assertEquals(2.0, f.process(3.0), 1e-10);  // (1+2+3)/3 = 2
        assertEquals(3.0, f.process(4.0), 1e-10);  // (2+3+4)/3 = 3
        assertEquals(4.0, f.process(5.0), 1e-10);  // (3+4+5)/3 = 4
    }

    @Test
    void liveMeanSingleElementWindow() {
        LiveMean f = new LiveMean(1);
        assertEquals(10.0, f.process(10.0), 1e-10);
        assertEquals(20.0, f.process(20.0), 1e-10);
    }
}
