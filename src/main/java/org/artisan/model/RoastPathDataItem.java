package org.artisan.model;

/**
 * Single data point from RoastPATH import (ported from Python RoastPathDataItem TypedDict).
 * Optional fields represented as nullable.
 */
public class RoastPathDataItem {

    private String timestamp;
    private Double standardValue;
    private String eventName;
    private Double note;
    private Integer noteTypeId;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getStandardValue() {
        return standardValue;
    }

    public void setStandardValue(Double standardValue) {
        this.standardValue = standardValue;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Double getNote() {
        return note;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public Integer getNoteTypeId() {
        return noteTypeId;
    }

    public void setNoteTypeId(Integer noteTypeId) {
        this.noteTypeId = noteTypeId;
    }
}
