package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetCalculatorTest {

    @Test
    void compute_returnsMaxEtBetweenChargeAndDrop() {
        ProfileData pd = new ProfileData();
        List<Double> timex = new ArrayList<>();
        List<Double> temp1 = new ArrayList<>();
        for (int i = 0; i <= 20; i++) {
            timex.add((double) i);
            temp1.add(100.0 + i * 2); // 100, 102, ..., 140
        }
        pd.setTimex(timex);
        pd.setTemp1(temp1);
        List<Integer> ti = new ArrayList<>();
        for (int i = 0; i < 8; i++) ti.add(0);
        ti.set(0, 2);   // CHARGE at index 2
        ti.set(6, 18);  // DROP at index 18
        pd.setTimeindex(ti);
        double met = MetCalculator.compute(pd);
        assertEquals(136.0, met, 0.01); // max ET in [2..18] is at 18: 100+36=136
    }

    @Test
    void compute_returnsNaN_whenNoChargeEvent() {
        ProfileData pd = new ProfileData();
        pd.setTimex(List.of(0.0, 1.0, 2.0));
        pd.setTemp1(List.of(100.0, 101.0, 102.0));
        List<Integer> ti = new ArrayList<>();
        for (int i = 0; i < 8; i++) ti.add(-1);
        ti.set(6, 2); // DROP set but CHARGE (index 0) is -1
        pd.setTimeindex(ti);
        double met = MetCalculator.compute(pd);
        assertTrue(Double.isNaN(met));
    }

    @Test
    void compute_withIndices_returnsMaxInRange() {
        List<Double> temp1 = List.of(100.0, 105.0, 98.0, 110.0, 115.0, 108.0);
        assertEquals(115.0, MetCalculator.compute(temp1, 2, 4), 0.01);
        assertTrue(Double.isNaN(MetCalculator.compute(temp1, -1, 2)));
        assertTrue(Double.isNaN(MetCalculator.compute(temp1, 2, 1)));
    }
}
