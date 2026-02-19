package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransposerTest {

    private static ProfileData profile(double[] timex, double[] temp2) {
        ProfileData p = new ProfileData();
        List<Double> tx = new java.util.ArrayList<>();
        for (double t : timex) tx.add(t);
        List<Double> t2 = new java.util.ArrayList<>();
        for (double t : temp2) t2.add(t);
        p.setTimex(tx);
        p.setTemp2(t2);
        p.setTemp1(new java.util.ArrayList<>(t2)); // same length
        return p;
    }

    @Test
    void transposeTime_positiveOffset() {
        ProfileData orig = profile(new double[]{0, 60, 120}, new double[]{100, 150, 200});
        ProfileData result = Transposer.transposeTime(orig, 30.0);
        assertNotSame(orig, result);
        assertEquals(Arrays.asList(30.0, 90.0, 150.0), result.getTimex());
        assertEquals(orig.getTemp2(), result.getTemp2());
        assertEquals(0.0, orig.getTimex().get(0));
    }

    @Test
    void transposeTime_negativeOffset() {
        ProfileData orig = profile(new double[]{30, 90, 150}, new double[]{100, 150, 200});
        ProfileData result = Transposer.transposeTime(orig, -30.0);
        assertEquals(Arrays.asList(0.0, 60.0, 120.0), result.getTimex());
        assertNotSame(orig, result);
        assertEquals(30.0, orig.getTimex().get(0));
    }

    @Test
    void transposeTemp_positiveOffset() {
        ProfileData orig = profile(new double[]{0, 60}, new double[]{100.0, 150.0});
        ProfileData result = Transposer.transposeTemp(orig, 10.0);
        assertNotSame(orig, result);
        assertEquals(Arrays.asList(110.0, 160.0), result.getTemp2());
        assertEquals(orig.getTimex(), result.getTimex());
        assertEquals(100.0, orig.getTemp2().get(0));
    }

    @Test
    void transposeTemp_negativeOffset() {
        ProfileData orig = profile(new double[]{0, 60}, new double[]{100.0, 150.0});
        ProfileData result = Transposer.transposeTemp(orig, -20.0);
        assertEquals(Arrays.asList(80.0, 130.0), result.getTemp2());
        assertEquals(100.0, orig.getTemp2().get(0));
    }

    @Test
    void transpose_combined() {
        ProfileData orig = profile(new double[]{0, 60, 120}, new double[]{100, 150, 200});
        ProfileData result = Transposer.transpose(orig, 15.0, 5.0);
        assertNotSame(orig, result);
        assertEquals(Arrays.asList(15.0, 75.0, 135.0), result.getTimex());
        assertEquals(Arrays.asList(105.0, 155.0, 205.0), result.getTemp2());
        assertEquals(0.0, orig.getTimex().get(0));
        assertEquals(100.0, orig.getTemp2().get(0));
    }

    @Test
    void transpose_emptyProfile() {
        ProfileData empty = new ProfileData();
        empty.setTimex(Collections.emptyList());
        empty.setTemp2(Collections.emptyList());
        ProfileData resultTime = Transposer.transposeTime(empty, 10.0);
        ProfileData resultTemp = Transposer.transposeTemp(empty, 5.0);
        ProfileData resultBoth = Transposer.transpose(empty, 10.0, 5.0);
        assertNotSame(empty, resultTime);
        assertNotSame(empty, resultTemp);
        assertNotSame(empty, resultBoth);
        assertTrue(resultTime.getTimex().isEmpty());
        assertTrue(resultTemp.getTemp2().isEmpty());
        assertTrue(resultBoth.getTimex().isEmpty());
    }

    @Test
    void transpose_nullProfile() {
        assertNull(Transposer.transposeTime(null, 10.0));
        assertNull(Transposer.transposeTemp(null, 5.0));
        assertNull(Transposer.transpose(null, 10.0, 5.0));
    }

    @Test
    void originalNotMutated() {
        List<Double> timex = Arrays.asList(0.0, 60.0);
        List<Double> temp2 = Arrays.asList(100.0, 150.0);
        ProfileData orig = new ProfileData();
        orig.setTimex(new java.util.ArrayList<>(timex));
        orig.setTemp2(new java.util.ArrayList<>(temp2));
        orig.setTemp1(new java.util.ArrayList<>(temp2));
        Transposer.transposeTime(orig, 20.0);
        Transposer.transposeTemp(orig, 10.0);
        Transposer.transpose(orig, 20.0, 10.0);
        assertEquals(timex, orig.getTimex());
        assertEquals(temp2, orig.getTemp2());
    }

    @Test
    void apply_withScale() {
        ProfileData orig = profile(new double[]{0, 60}, new double[]{100.0, 200.0});
        TransposerParams params = new TransposerParams(0, 0, 2.0);
        ProfileData result = Transposer.apply(orig, params);
        assertNotSame(orig, result);
        assertEquals(orig.getTimex(), result.getTimex());
        assertEquals(100.0 * 2.0, result.getTemp2().get(0), 0.01);
        assertEquals(200.0 * 2.0, result.getTemp2().get(1), 0.01);
    }

    @Test
    void transposeTemp_preservesInvalidReadings() {
        ProfileData orig = new ProfileData();
        orig.setTimex(Arrays.asList(0.0, 60.0));
        orig.setTemp2(Arrays.asList(100.0, -1.0));
        orig.setTemp1(Arrays.asList(90.0, -1.0));
        ProfileData result = Transposer.transposeTemp(orig, 5.0);
        assertEquals(105.0, result.getTemp2().get(0));
        assertEquals(-1.0, result.getTemp2().get(1));
        assertEquals(95.0, result.getTemp1().get(0));
        assertEquals(-1.0, result.getTemp1().get(1));
    }
}
