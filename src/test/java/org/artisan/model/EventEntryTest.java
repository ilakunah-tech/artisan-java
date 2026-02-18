package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EventEntryTest {

    @Test
    void constructorAndGetters() {
        EventEntry e = new EventEntry(3, 205.5, "FC Start", EventType.FC_START);
        assertEquals(3, e.getTimeIndex());
        assertEquals(205.5, e.getTemp());
        assertEquals("FC Start", e.getLabel());
        assertEquals(EventType.FC_START, e.getType());
    }

    @Test
    void nullLabelBecomesEmpty() {
        EventEntry e = new EventEntry(0, 200.0, null, EventType.CHARGE);
        assertEquals("", e.getLabel());
    }

    @Test
    void nullTypeBecomesCustom() {
        EventEntry e = new EventEntry(0, 200.0, "x", null);
        assertEquals(EventType.CUSTOM, e.getType());
    }

    @Test
    void equality() {
        EventEntry a = new EventEntry(1, 210.0, "Dry", EventType.DRY_END);
        EventEntry b = new EventEntry(1, 210.0, "Dry", EventType.DRY_END);
        EventEntry c = new EventEntry(2, 210.0, "Dry", EventType.DRY_END);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertEquals(a, a);
    }
}
