package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventAnnotationTest {

    private static final List<Double> TIMEX = Arrays.asList(0.0, 60.0, 120.0, 300.0);

    @Test
    void fromEntryValidIndex() {
        EventEntry e = new EventEntry(2, 205.0, "FC Start", EventType.FC_START);
        EventAnnotation a = EventAnnotation.fromEntry(e, TIMEX);
        assertEquals(120.0, a.getXSec());
        assertEquals("FC Start", a.getDisplayLabel());
    }

    @Test
    void fromEntryIndexZero() {
        EventEntry e = new EventEntry(0, 200.0, "Charge", EventType.CHARGE);
        EventAnnotation a = EventAnnotation.fromEntry(e, TIMEX);
        assertEquals(0.0, a.getXSec());
        assertEquals("Charge", a.getDisplayLabel());
    }

    @Test
    void fromEntryNullTimexThrows() {
        EventEntry e = new EventEntry(0, 200.0, "A", EventType.CHARGE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> EventAnnotation.fromEntry(e, null));
        assertEquals("timex must not be null", ex.getMessage());
    }

    @Test
    void fromEntryIndexOutOfBoundsNegativeThrows() {
        EventEntry e = new EventEntry(-1, 200.0, "A", EventType.CHARGE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> EventAnnotation.fromEntry(e, TIMEX));
        assertTrue(ex.getMessage().contains("timeIndex"));
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void fromEntryIndexOutOfBoundsTooLargeThrows() {
        EventEntry e = new EventEntry(4, 200.0, "A", EventType.CHARGE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> EventAnnotation.fromEntry(e, TIMEX));
        assertTrue(ex.getMessage().contains("timeIndex"));
        assertTrue(ex.getMessage().contains("out of bounds"));
    }

    @Test
    void fromEntryEmptyTimexIndexZeroThrows() {
        EventEntry e = new EventEntry(0, 200.0, "A", EventType.CHARGE);
        assertThrows(IllegalArgumentException.class,
                () -> EventAnnotation.fromEntry(e, Collections.emptyList()));
    }
}
