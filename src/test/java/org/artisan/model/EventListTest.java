package org.artisan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventListTest {

    private EventList list;

    @BeforeEach
    void setUp() {
        list = new EventList();
    }

    @Test
    void addAndSize() {
        assertEquals(0, list.size());
        list.add(new EventEntry(0, 200.0, "Charge", EventType.CHARGE));
        assertEquals(1, list.size());
        list.add(new EventEntry(5, 205.0, "FC", EventType.FC_START));
        assertEquals(2, list.size());
    }

    @Test
    void addNullIgnored() {
        list.add(new EventEntry(0, 200.0, "A", EventType.CHARGE));
        list.add(null);
        assertEquals(1, list.size());
    }

    @Test
    void remove() {
        list.add(new EventEntry(0, 200.0, "A", EventType.CHARGE));
        list.add(new EventEntry(1, 210.0, "B", EventType.DRY_END));
        list.remove(0);
        assertEquals(1, list.size());
        assertEquals("B", list.get(0).getLabel());
    }

    @Test
    void getByType() {
        list.add(new EventEntry(0, 200.0, "Charge", EventType.CHARGE));
        list.add(new EventEntry(2, 205.0, "FCs", EventType.FC_START));
        list.add(new EventEntry(1, 198.0, "Dry", EventType.DRY_END));
        list.add(new EventEntry(3, 208.0, "FCe", EventType.FC_END));

        List<EventEntry> fc = list.getByType(EventType.FC_START);
        assertEquals(1, fc.size());
        assertEquals("FCs", fc.get(0).getLabel());

        List<EventEntry> charge = list.getByType(EventType.CHARGE);
        assertEquals(1, charge.size());
        assertEquals("Charge", charge.get(0).getLabel());

        List<EventEntry> custom = list.getByType(EventType.CUSTOM);
        assertTrue(custom.isEmpty());
    }

    @Test
    void clear() {
        list.add(new EventEntry(0, 200.0, "A", EventType.CHARGE));
        list.add(new EventEntry(1, 210.0, "B", EventType.DRY_END));
        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.getAll().isEmpty());
    }

    @Test
    void getAllIsUnmodifiable() {
        list.add(new EventEntry(0, 200.0, "A", EventType.CHARGE));
        List<EventEntry> all = list.getAll();
        assertThrows(UnsupportedOperationException.class, () -> all.add(
                new EventEntry(1, 210.0, "B", EventType.DRY_END)));
        assertThrows(UnsupportedOperationException.class, () -> all.clear());
    }

    @Test
    void get() {
        EventEntry a = new EventEntry(0, 200.0, "A", EventType.CHARGE);
        list.add(a);
        assertEquals(a, list.get(0));
    }

    @Test
    void getIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        list.add(new EventEntry(0, 200.0, "A", EventType.CHARGE));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
    }

    @Test
    void set_replacesEntry() {
        list.add(new EventEntry(0, 200.0, "A", EventType.CUSTOM));
        list.add(new EventEntry(5, 205.0, "B", EventType.CUSTOM));
        list.set(1, new EventEntry(3, 202.0, "B2", EventType.CUSTOM));
        assertEquals(2, list.size());
        assertEquals(3, list.get(1).getTimeIndex());
        assertEquals(202.0, list.get(1).getTemp());
        assertEquals("B2", list.get(1).getLabel());
    }
}
