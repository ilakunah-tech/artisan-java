package org.artisan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ordered list of EventEntry objects.
 */
public class EventList {

    private final List<EventEntry> entries = new ArrayList<>();

    public void add(EventEntry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }

    public void remove(int index) {
        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
        }
    }

    /**
     * Replaces the entry at the given index (e.g. after dragging to new time).
     */
    public void set(int index, EventEntry entry) {
        if (entry == null || index < 0 || index >= entries.size()) return;
        entries.set(index, entry);
    }

    public EventEntry get(int index) {
        return entries.get(index);
    }

    public List<EventEntry> getByType(EventType type) {
        if (type == null) return Collections.emptyList();
        return entries.stream()
                .filter(e -> type.equals(e.getType()))
                .collect(Collectors.toList());
    }

    public int size() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
    }

    /**
     * Returns an unmodifiable view of all entries.
     */
    public List<EventEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }
}
