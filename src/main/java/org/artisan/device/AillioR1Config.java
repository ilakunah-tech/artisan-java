package org.artisan.device;

import java.util.prefs.Preferences;

/**
 * Aillio Bullet R1 HID configuration. Persisted under Preferences "aillio_r1.*".
 */
public final class AillioR1Config {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "aillio_r1.";

    public static final int DEFAULT_VID = 0x0483;
    public static final int DEFAULT_PID = 0x5741;

    private int vid = DEFAULT_VID;
    private int pid = DEFAULT_PID;

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid & 0xFFFF;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid & 0xFFFF;
    }

    public static void loadFromPreferences(AillioR1Config target) {
        if (target == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        target.setVid(p.getInt(PREFIX + "vid", DEFAULT_VID));
        target.setPid(p.getInt(PREFIX + "pid", DEFAULT_PID));
    }

    public static void saveToPreferences(AillioR1Config config) {
        if (config == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.putInt(PREFIX + "vid", config.getVid());
        p.putInt(PREFIX + "pid", config.getPid());
    }
}
