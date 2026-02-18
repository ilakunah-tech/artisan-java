package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoastPathTest {

    @Test
    void createPathWithPoints() {
        RoastPath path = new RoastPath();
        path.addPoint(0.0, 20.0);
        path.addPoint(60.0, 100.0);
        path.addPoint(120.0, 200.0);
        assertEquals(3, path.size());
        List<RoastPath.Point> points = path.getPoints();
        assertEquals(3, points.size());
        assertEquals(0.0, points.get(0).getX());
        assertEquals(20.0, points.get(0).getY());
        assertEquals(60.0, points.get(1).getX());
        assertEquals(100.0, points.get(1).getY());
        assertEquals(120.0, points.get(2).getX());
        assertEquals(200.0, points.get(2).getY());
    }

    @Test
    void createPathFromList() {
        List<RoastPath.Point> list = Arrays.asList(
            new RoastPath.Point(0, 10),
            new RoastPath.Point(10, 50),
            new RoastPath.Point(20, 90)
        );
        RoastPath path = new RoastPath(list);
        assertEquals(3, path.size());
        assertEquals(10.0, path.getValueAt(0));
        assertEquals(50.0, path.getValueAt(10));
        assertEquals(90.0, path.getValueAt(20));
    }

    @Test
    void interpolationBetweenPoints() {
        RoastPath path = new RoastPath();
        path.addPoint(0.0, 0.0);
        path.addPoint(10.0, 100.0);
        path.addPoint(20.0, 200.0);
        assertEquals(0.0, path.getValueAt(0), 1e-9);
        assertEquals(100.0, path.getValueAt(10), 1e-9);
        assertEquals(200.0, path.getValueAt(20), 1e-9);
        assertEquals(50.0, path.getValueAt(5), 1e-9);
        assertEquals(150.0, path.getValueAt(15), 1e-9);
    }

    @Test
    void emptyPath() {
        RoastPath path = new RoastPath();
        assertEquals(0, path.size());
        assertTrue(Double.isNaN(path.getValueAt(0)));
        assertTrue(Double.isNaN(path.getValueAt(100)));
    }

    @Test
    void singleElementPath() {
        RoastPath path = new RoastPath();
        path.addPoint(30.0, 150.0);
        assertEquals(1, path.size());
        assertEquals(150.0, path.getValueAt(0), 1e-9);
        assertEquals(150.0, path.getValueAt(30), 1e-9);
        assertEquals(150.0, path.getValueAt(100), 1e-9);
    }

    @Test
    void outOfRangeBeforeFirst() {
        RoastPath path = new RoastPath();
        path.addPoint(10.0, 100.0);
        path.addPoint(20.0, 200.0);
        // Extrapolate backward: slope 10 per unit, at x=0 -> 0
        double v = path.getValueAt(0);
        assertEquals(0.0, v, 1e-9);
        assertEquals(-100.0, path.getValueAt(-10), 1e-9);
    }

    @Test
    void outOfRangeAfterLast() {
        RoastPath path = new RoastPath();
        path.addPoint(0.0, 0.0);
        path.addPoint(10.0, 100.0);
        double v = path.getValueAt(20);
        assertEquals(200.0, v, 1e-9);
        assertEquals(300.0, path.getValueAt(30), 1e-9);
    }

    @Test
    void roastPathDataItemSetters() {
        RoastPathDataItem item = new RoastPathDataItem();
        item.setTimestamp("2024-01-15T10:00:00");
        item.setStandardValue(195.5);
        item.setEventName("First Crack");
        item.setNote(80.0);
        item.setNoteTypeId(2);
        assertEquals("2024-01-15T10:00:00", item.getTimestamp());
        assertEquals(195.5, item.getStandardValue());
        assertEquals("First Crack", item.getEventName());
        assertEquals(80.0, item.getNote());
        assertEquals(2, item.getNoteTypeId());
    }

    @Test
    void roastPathDataListsNotNull() {
        RoastPathData data = new RoastPathData();
        assertNotNull(data.getBtData());
        assertNotNull(data.getEtData());
        data.setBtData(null);
        assertNotNull(data.getBtData());
        assertEquals(0, data.getBtData().size());
    }
}
