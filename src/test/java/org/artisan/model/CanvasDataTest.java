package org.artisan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanvasDataTest {

    private CanvasData data;

    @BeforeEach
    void setUp() {
        data = new CanvasData();
    }

    @Test
    void addDataPointGrowsArrays() {
        data.addDataPoint(0.0, 20.0, 180.0);
        assertEquals(1, data.getTimex().size());
        assertEquals(1, data.getTemp1().size());
        assertEquals(1, data.getTemp2().size());
        assertEquals(0.0, data.getTimex().get(0));
        assertEquals(180.0, data.getTemp1().get(0));
        assertEquals(20.0, data.getTemp2().get(0));

        data.addDataPoint(2.0, 22.0, 185.0);
        assertEquals(2, data.getTimex().size());
        assertEquals(2, data.getTemp1().size());
        assertEquals(2, data.getTemp2().size());
        assertEquals(2.0, data.getTimex().get(1));
        assertEquals(185.0, data.getTemp1().get(1));
        assertEquals(22.0, data.getTemp2().get(1));
    }

    @Test
    void clearResetsAll() {
        data.addDataPoint(0.0, 20.0, 180.0);
        data.addDataPoint(2.0, 22.0, 185.0);
        data.setChargeIndex(0);
        data.setDropIndex(1);
        data.clear();

        assertTrue(data.getTimex().isEmpty());
        assertTrue(data.getTemp1().isEmpty());
        assertTrue(data.getTemp2().isEmpty());
        assertTrue(data.getDelta1().isEmpty());
        assertTrue(data.getDelta2().isEmpty());
        assertEquals(-1, data.getChargeIndex());
        assertEquals(0, data.getDryEndIndex());
        assertEquals(0, data.getFcStartIndex());
        assertEquals(0, data.getFcEndIndex());
        assertEquals(0, data.getDropIndex());
    }

    @Test
    void setDelta1AndSetDelta2UpdateLists() {
        data.addDataPoint(0.0, 20.0, 180.0);
        data.addDataPoint(2.0, 22.0, 185.0);
        data.setDelta1(List.of(2.5, 2.5));
        data.setDelta2(List.of(1.0, 1.0));
        assertEquals(2, data.getDelta1().size());
        assertEquals(2, data.getDelta2().size());
        assertEquals(2.5, data.getDelta1().get(0));
        assertEquals(1.0, data.getDelta2().get(0));
    }

    @Test
    void getTimexTemp1Temp2ReturnUnmodifiableLists() {
        data.addDataPoint(0.0, 20.0, 180.0);
        List<Double> timex = data.getTimex();
        assertNotNull(timex);
        assertTrue(timex.size() == 1);
        try {
            timex.add(1.0);
            throw new AssertionError("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    void eventIndicesDefaultAndSettable() {
        assertEquals(-1, data.getChargeIndex());
        data.setChargeIndex(3);
        assertEquals(3, data.getChargeIndex());
        data.setDryEndIndex(5);
        assertEquals(5, data.getDryEndIndex());
        data.setFcStartIndex(7);
        data.setFcEndIndex(9);
        data.setDropIndex(11);
        assertEquals(7, data.getFcStartIndex());
        assertEquals(9, data.getFcEndIndex());
        assertEquals(11, data.getDropIndex());
    }
}
