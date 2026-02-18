package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventTypeTest {

    @Test
    void eventTypesExist() {
        assertNotNull(EventType.CHARGE);
        assertNotNull(EventType.DRY_END);
        assertNotNull(EventType.FC_START);
        assertNotNull(EventType.FC_END);
        assertNotNull(EventType.SC_START);
        assertNotNull(EventType.SC_END);
        assertNotNull(EventType.DROP);
        assertNotNull(EventType.COOL_END);
        assertNotNull(EventType.CUSTOM);
        assertEquals(9, EventType.values().length);
    }
}
