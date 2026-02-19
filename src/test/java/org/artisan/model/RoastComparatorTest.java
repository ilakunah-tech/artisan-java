package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoastComparatorTest {

    private static ProfileData profile(double[] timex, double[] bt) {
        ProfileData p = new ProfileData();
        List<Double> tx = new java.util.ArrayList<>();
        for (double t : timex) tx.add(t);
        List<Double> t2 = new java.util.ArrayList<>();
        for (double t : bt) t2.add(t);
        p.setTimex(tx);
        p.setTemp2(t2);
        return p;
    }

    @Test
    void addProfile_increasesSize() {
        RoastComparator c = new RoastComparator();
        assertEquals(0, c.getProfiles().size());
        c.addProfile(profile(new double[]{0, 60}, new double[]{100, 150}), "a.alog");
        assertEquals(1, c.getProfiles().size());
        c.addProfile(profile(new double[]{0, 120}, new double[]{90, 200}), "b.alog");
        assertEquals(2, c.getProfiles().size());
    }

    @Test
    void removeProfile_decreasesSize() {
        RoastComparator c = new RoastComparator();
        c.addProfile(profile(new double[]{0}, new double[]{100}), "a.alog");
        c.addProfile(profile(new double[]{0}, new double[]{100}), "b.alog");
        c.removeProfile(0);
        assertEquals(1, c.getProfiles().size());
        assertEquals("b.alog", c.getFilename(0));
    }

    @Test
    void clear_emptiesProfiles() {
        RoastComparator c = new RoastComparator();
        c.addProfile(profile(new double[]{0}, new double[]{100}), "a.alog");
        c.clear();
        assertEquals(0, c.getProfiles().size());
        assertTrue(c.getProfiles().isEmpty());
    }

    @Test
    void getAlignedBT_appliesOffset() {
        RoastComparator c = new RoastComparator();
        c.addProfile(profile(new double[]{0, 60, 120}, new double[]{100, 150, 200}), "p.alog");
        double[] bt = c.getAlignedBT(0, 0);
        assertArrayEquals(new double[]{100, 150, 200}, bt, 0.01);
        double[] timeNoOffset = c.getAlignedTime(0, 0);
        assertArrayEquals(new double[]{0, 60, 120}, timeNoOffset, 0.01);
        double[] timeWithOffset = c.getAlignedTime(0, 30);
        assertArrayEquals(new double[]{30, 90, 150}, timeWithOffset, 0.01);
        // BT values unchanged; only time axis shifts
        double[] btUnchanged = c.getAlignedBT(0, 30);
        assertArrayEquals(new double[]{100, 150, 200}, btUnchanged, 0.01);
    }
}
