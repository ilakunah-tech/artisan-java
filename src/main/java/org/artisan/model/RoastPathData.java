package org.artisan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for RoastPATH-extracted series (ported from Python RoastPathData TypedDict).
 * Holds lists of RoastPathDataItem for BT, ET, AT, events, RoR, notes, fuel, fan, drum.
 */
public class RoastPathData {

    private List<RoastPathDataItem> btData = new ArrayList<>();
    private List<RoastPathDataItem> etData = new ArrayList<>();
    private List<RoastPathDataItem> atData = new ArrayList<>();
    private List<RoastPathDataItem> eventData = new ArrayList<>();
    private List<RoastPathDataItem> rorData = new ArrayList<>();
    private List<RoastPathDataItem> noteData = new ArrayList<>();
    private List<RoastPathDataItem> fuelData = new ArrayList<>();
    private List<RoastPathDataItem> fanData = new ArrayList<>();
    private List<RoastPathDataItem> drumData = new ArrayList<>();

    public List<RoastPathDataItem> getBtData() {
        return btData;
    }

    public void setBtData(List<RoastPathDataItem> btData) {
        this.btData = btData != null ? btData : new ArrayList<>();
    }

    public List<RoastPathDataItem> getEtData() {
        return etData;
    }

    public void setEtData(List<RoastPathDataItem> etData) {
        this.etData = etData != null ? etData : new ArrayList<>();
    }

    public List<RoastPathDataItem> getAtData() {
        return atData;
    }

    public void setAtData(List<RoastPathDataItem> atData) {
        this.atData = atData != null ? atData : new ArrayList<>();
    }

    public List<RoastPathDataItem> getEventData() {
        return eventData;
    }

    public void setEventData(List<RoastPathDataItem> eventData) {
        this.eventData = eventData != null ? eventData : new ArrayList<>();
    }

    public List<RoastPathDataItem> getRorData() {
        return rorData;
    }

    public void setRorData(List<RoastPathDataItem> rorData) {
        this.rorData = rorData != null ? rorData : new ArrayList<>();
    }

    public List<RoastPathDataItem> getNoteData() {
        return noteData;
    }

    public void setNoteData(List<RoastPathDataItem> noteData) {
        this.noteData = noteData != null ? noteData : new ArrayList<>();
    }

    public List<RoastPathDataItem> getFuelData() {
        return fuelData;
    }

    public void setFuelData(List<RoastPathDataItem> fuelData) {
        this.fuelData = fuelData != null ? fuelData : new ArrayList<>();
    }

    public List<RoastPathDataItem> getFanData() {
        return fanData;
    }

    public void setFanData(List<RoastPathDataItem> fanData) {
        this.fanData = fanData != null ? fanData : new ArrayList<>();
    }

    public List<RoastPathDataItem> getDrumData() {
        return drumData;
    }

    public void setDrumData(List<RoastPathDataItem> drumData) {
        this.drumData = drumData != null ? drumData : new ArrayList<>();
    }
}
