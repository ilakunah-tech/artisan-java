package org.artisan.model;

import java.util.Objects;

/**
 * Single roast event: index into timex, temperature at that time, label, type, and optional value (e.g. gas %).
 * Immutable.
 */
public final class EventEntry {

    private final int timeIndex;
    private final double temp;
    private final String label;
    private final EventType type;
    private final double value;

    public EventEntry(int timeIndex, double temp, String label, EventType type) {
        this(timeIndex, temp, label, type, 0.0);
    }

    public EventEntry(int timeIndex, double temp, String label, EventType type, double value) {
        this.timeIndex = timeIndex;
        this.temp = temp;
        this.label = label != null ? label : "";
        this.type = type != null ? type : EventType.CUSTOM;
        this.value = Double.isFinite(value) ? value : 0.0;
    }

    public int getTimeIndex() {
        return timeIndex;
    }

    public double getTemp() {
        return temp;
    }

    public String getLabel() {
        return label;
    }

    public EventType getType() {
        return type;
    }

    /** Numeric value (0–100) e.g. for sliders/gas %. */
    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventEntry that = (EventEntry) o;
        return timeIndex == that.timeIndex
                && Double.compare(that.temp, temp) == 0
                && Double.compare(that.value, value) == 0
                && Objects.equals(label, that.label)
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeIndex, temp, label, type, value);
    }
}
