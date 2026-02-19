package org.artisan.controller;

import org.artisan.model.EventEntry;
import org.artisan.model.EventType;
import org.artisan.model.ProfileData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventReplayTest {

    private EventReplay replay;
    private ProfileData background;

    @BeforeEach
    void setUp() {
        replay = new EventReplay();
        replay.setEnabled(true);
        replay.setOffsetSeconds(0.0);
        replay.setReplayBackground(true);

        background = new ProfileData();
        background.setTimex(new ArrayList<>(List.of(0.0, 10.0, 20.0, 30.0)));
        background.setTemp2(new ArrayList<>(List.of(100.0, 150.0, 200.0, 210.0)));
        List<Integer> se = new ArrayList<>(List.of(1, 3));
        List<Integer> set = new ArrayList<>(List.of(EventType.CUSTOM.ordinal(), EventType.CUSTOM.ordinal()));
        List<Double> sev = new ArrayList<>(List.of(50.0, 80.0));
        background.setSpecialevents(se);
        background.setSpecialeventstype(set);
        background.setSpecialeventsvalue(sev);
    }

    @Test
    void noFire_whenDisabled() {
        replay.setEnabled(false);
        replay.reset();
        AtomicInteger count = new AtomicInteger(0);
        replay.checkReplay(100.0, background, e -> count.incrementAndGet());
        assertEquals(0, count.get());
    }

    @Test
    void fires_whenTimeReached() {
        replay.reset();
        List<EventEntry> fired = new ArrayList<>();
        replay.checkReplay(15.0, background, fired::add);
        assertEquals(1, fired.size());
        assertEquals(1, fired.get(0).getTimeIndex());
        assertEquals(150.0, fired.get(0).getTemp());
        assertEquals(EventType.CUSTOM, fired.get(0).getType());
        assertEquals(50.0, fired.get(0).getValue());

        replay.checkReplay(35.0, background, fired::add);
        assertEquals(2, fired.size());
        assertEquals(3, fired.get(1).getTimeIndex());
        assertEquals(210.0, fired.get(1).getTemp());
    }

    @Test
    void doesNotFireTwice_sameEvent() {
        replay.reset();
        AtomicInteger count = new AtomicInteger(0);
        replay.checkReplay(15.0, background, e -> count.incrementAndGet());
        replay.checkReplay(20.0, background, e -> count.incrementAndGet());
        assertEquals(1, count.get());
    }

    @Test
    void reset_allowsRefireAfterCharge() {
        replay.reset();
        AtomicInteger count = new AtomicInteger(0);
        replay.checkReplay(15.0, background, e -> count.incrementAndGet());
        assertEquals(1, count.get());

        replay.reset();
        replay.checkReplay(20.0, background, e -> count.incrementAndGet());
        assertEquals(2, count.get());
    }
}
