package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchTest {

    @Test
    void toMapContainsAllKeys() {
        Batch b = new Batch();
        b.setBatchNumber(1);
        b.setTitle("Test");
        b.setDate("2025-01-15");
        b.setGreenWeight(500);
        b.setRoastedWeight(400);
        b.setTotalRoastTimeSec(600);
        b.setProfilePath("/path/to.alog");
        b.setNotes("notes");
        b.setExported(true);

        Map<String, String> m = b.toMap();
        assertNotNull(m);
        assertTrue(m.containsKey("batchNumber"));
        assertTrue(m.containsKey("title"));
        assertTrue(m.containsKey("date"));
        assertTrue(m.containsKey("greenWeight"));
        assertTrue(m.containsKey("roastedWeight"));
        assertTrue(m.containsKey("totalRoastTimeSec"));
        assertTrue(m.containsKey("profilePath"));
        assertTrue(m.containsKey("notes"));
        assertTrue(m.containsKey("exported"));
        assertEquals("1", m.get("batchNumber"));
        assertEquals("Test", m.get("title"));
        assertEquals("2025-01-15", m.get("date"));
        assertEquals("500.0", m.get("greenWeight"));
        assertEquals("400.0", m.get("roastedWeight"));
        assertEquals("600.0", m.get("totalRoastTimeSec"));
        assertEquals("/path/to.alog", m.get("profilePath"));
        assertEquals("notes", m.get("notes"));
        assertEquals("true", m.get("exported"));
    }

    @Test
    void weightLossPercent() {
        Batch b = new Batch();
        b.setGreenWeight(500);
        b.setRoastedWeight(400);
        assertEquals(20.0, b.weightLossPercent(), 0.01);
    }
}
