package org.artisan.device;

import java.util.prefs.Preferences;

/**
 * Device selection and notes. Persisted under Preferences "device.*".
 */
public final class DeviceConfig {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "device.";

    private DeviceType activeType = DeviceType.NONE;
    private String notes = "";

    public DeviceType getActiveType() {
        return activeType;
    }

    public void setActiveType(DeviceType activeType) {
        this.activeType = activeType != null ? activeType : DeviceType.NONE;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes != null ? notes : "";
    }

    public void load() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        String name = p.get(PREFIX + "activeType", DeviceType.NONE.name());
        try {
            activeType = DeviceType.valueOf(name);
        } catch (IllegalArgumentException e) {
            activeType = DeviceType.NONE;
        }
        notes = p.get(PREFIX + "notes", "");
    }

    public void save() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.put(PREFIX + "activeType", activeType.name());
        p.put(PREFIX + "notes", notes);
    }
}
