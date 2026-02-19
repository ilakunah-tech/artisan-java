package org.artisan.view;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests InMemoryLogHandler: publish adds record, buffer cap at 500, clear empties, getRecords returns copy.
 */
class InMemoryLogHandlerTest {

    private InMemoryLogHandler handler;

    @BeforeEach
    void setUp() {
        handler = new InMemoryLogHandler(500);
    }

    @Test
    void publish_addsRecord() {
        handler.publish(new LogRecord(Level.INFO, "test message"));
        List<String> records = handler.getRecords();
        assertNotNull(records);
        assertEquals(1, records.size());
        assertTrue(records.get(0).contains("test message") || records.get(0).contains("INFO"));
    }

    @Test
    void bufferCapAt500() {
        for (int i = 0; i < 600; i++) {
            LogRecord r = new LogRecord(Level.INFO, "msg " + i);
            r.setMillis(System.currentTimeMillis());
            handler.publish(r);
        }
        List<String> records = handler.getRecords();
        assertNotNull(records);
        assertTrue(records.size() <= 500, "size should be at most 500, was " + records.size());
    }

    @Test
    void clear_emptiesBuffer() {
        handler.publish(new LogRecord(Level.INFO, "one"));
        handler.publish(new LogRecord(Level.INFO, "two"));
        handler.clear();
        List<String> records = handler.getRecords();
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void getRecords_returnsCopy() {
        handler.publish(new LogRecord(Level.INFO, "x"));
        List<String> records = handler.getRecords();
        assertNotNull(records);
        assertEquals(1, records.size());
        records.clear();
        List<String> again = handler.getRecords();
        assertEquals(1, again.size(), "modifying returned list should not affect handler");
    }
}
