package org.artisan.ui.model;

/**
 * View-model representation of a roast event for UI display.
 * Built from {@link org.artisan.model.EventEntry} via timex lookup for timeSeconds.
 */
public record RoastEvent(long timeSeconds, String type, String note) {}
