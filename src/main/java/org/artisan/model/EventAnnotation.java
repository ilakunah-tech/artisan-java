package org.artisan.model;

import java.util.List;

/**
 * Maps an EventEntry to chart X position (time in seconds) and label text
 * for use by RoastChartController.
 */
public final class EventAnnotation {

    private final double xSec;
    private final String displayLabel;

    public EventAnnotation(double xSec, String displayLabel) {
        this.xSec = xSec;
        this.displayLabel = displayLabel != null ? displayLabel : "";
    }

    public double getXSec() {
        return xSec;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    /**
     * Builds an EventAnnotation from an event and the time axis.
     *
     * @param e    the event (timeIndex must be a valid index into timex)
     * @param timex time axis in seconds (must not be null)
     * @return annotation with xSec and displayLabel
     * @throws IllegalArgumentException if timex is null, or timeIndex is out of bounds
     */
    public static EventAnnotation fromEntry(EventEntry e, List<Double> timex) {
        if (timex == null) {
            throw new IllegalArgumentException("timex must not be null");
        }
        int idx = e.getTimeIndex();
        if (idx < 0 || idx >= timex.size()) {
            throw new IllegalArgumentException("timeIndex " + idx + " out of bounds for timex size " + timex.size());
        }
        double xSec = timex.get(idx);
        String displayLabel = e.getLabel();
        if (displayLabel == null) {
            displayLabel = "";
        }
        return new EventAnnotation(xSec, displayLabel);
    }
}
