package org.artisan.model;

import org.artisan.controller.EventButtonConfigPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventButtonConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoad_roundtrip() throws IOException {
        Path path = tempDir.resolve("eventbuttons.json");
        List<EventButtonConfig> list = new ArrayList<>();
        list.add(new EventButtonConfig("E1", "First", EventType.CUSTOM, 50.0, Color.RED, true, "SET_BURNER", "80"));
        list.add(new EventButtonConfig("E2", "Second", EventType.CUSTOM, 0.0, Color.BLUE, false, "", ""));

        EventButtonConfigPersistence.save(list, path);
        List<EventButtonConfig> loaded = EventButtonConfigPersistence.load(path);

        assertEquals(2, loaded.size());
        EventButtonConfig c0 = loaded.get(0);
        assertEquals("E1", c0.getLabel());
        assertEquals("First", c0.getDescription());
        assertEquals(EventType.CUSTOM, c0.getType());
        assertEquals(50.0, c0.getValue());
        assertTrue(c0.isVisible());
        assertEquals("SET_BURNER", c0.getAction());
        assertEquals("80", c0.getActionParam());

        EventButtonConfig c1 = loaded.get(1);
        assertEquals("E2", c1.getLabel());
        assertEquals("Second", c1.getDescription());
        assertTrue(!c1.isVisible());
    }

    @Test
    void load_returnsEmpty_whenFileAbsent() {
        Path path = tempDir.resolve("nonexistent.json");
        List<EventButtonConfig> loaded = EventButtonConfigPersistence.load(path);
        assertTrue(loaded.isEmpty());
    }
}
