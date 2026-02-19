package org.artisan.view;

import org.artisan.model.EventEntry;
import org.artisan.model.EventList;
import org.artisan.model.EventType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Data-only tests for event bar layer: marker positions match event time indices.
 * No JavaFX.
 */
class EventBarLayerTest {

    /** Compute marker time positions (seconds) from event list and timex, as used by the chart layer. */
    private static List<Double> markerTimePositions(EventList events, List<Double> timex) {
        List<Double> out = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            EventEntry e = events.get(i);
            int idx = e.getTimeIndex();
            if (idx >= 0 && idx < timex.size()) {
                out.add(timex.get(idx));
            }
        }
        return out;
    }

    @Test
    void markerPositions_matchEventTimex() {
        List<Double> timex = List.of(0.0, 10.0, 20.0, 30.0);
        EventList list = new EventList();
        list.add(new EventEntry(1, 200.0, "A", EventType.CUSTOM));
        list.add(new EventEntry(3, 210.0, "B", EventType.CUSTOM));

        List<Double> positions = markerTimePositions(list, timex);

        assertEquals(2, positions.size());
        assertEquals(10.0, positions.get(0));
        assertEquals(30.0, positions.get(1));
    }

    @Test
    void noMarkers_whenEventListEmpty() {
        List<Double> timex = List.of(0.0, 10.0, 20.0);
        EventList list = new EventList();

        List<Double> positions = markerTimePositions(list, timex);

        assertTrue(positions.isEmpty());
    }
}
