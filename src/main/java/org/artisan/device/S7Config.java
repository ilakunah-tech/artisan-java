package org.artisan.device;

import java.util.prefs.Preferences;

/**
 * Configuration for Siemens S7 PLC connection (BT/ET floats from data blocks).
 * Persisted under Preferences "s7.*".
 */
public final class S7Config {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "s7.";

    public static final int DEFAULT_PORT = 102;
    public static final int DEFAULT_RACK = 0;
    public static final int DEFAULT_SLOT = 1;

    private String host = "";
    private int rack = DEFAULT_RACK;
    private int slot = DEFAULT_SLOT;
    private int port = DEFAULT_PORT;
    private int btDbNumber = 1;
    private int btDbOffset = 0;
    private int etDbNumber = 1;
    private int etDbOffset = 4;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host != null ? host : "";
    }

    public int getRack() {
        return rack;
    }

    public void setRack(int rack) {
        this.rack = rack;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBtDbNumber() {
        return btDbNumber;
    }

    public void setBtDbNumber(int btDbNumber) {
        this.btDbNumber = btDbNumber;
    }

    public int getBtDbOffset() {
        return btDbOffset;
    }

    public void setBtDbOffset(int btDbOffset) {
        this.btDbOffset = btDbOffset;
    }

    public int getEtDbNumber() {
        return etDbNumber;
    }

    public void setEtDbNumber(int etDbNumber) {
        this.etDbNumber = etDbNumber;
    }

    public int getEtDbOffset() {
        return etDbOffset;
    }

    public void setEtDbOffset(int etDbOffset) {
        this.etDbOffset = etDbOffset;
    }

    public static void loadFromPreferences(S7Config target) {
        if (target == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        target.setHost(p.get(PREFIX + "host", ""));
        target.setRack(p.getInt(PREFIX + "rack", DEFAULT_RACK));
        target.setSlot(p.getInt(PREFIX + "slot", DEFAULT_SLOT));
        target.setPort(p.getInt(PREFIX + "port", DEFAULT_PORT));
        target.setBtDbNumber(p.getInt(PREFIX + "btDbNumber", 1));
        target.setBtDbOffset(p.getInt(PREFIX + "btDbOffset", 0));
        target.setEtDbNumber(p.getInt(PREFIX + "etDbNumber", 1));
        target.setEtDbOffset(p.getInt(PREFIX + "etDbOffset", 4));
    }

    public void save() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.put(PREFIX + "host", host);
        p.putInt(PREFIX + "rack", rack);
        p.putInt(PREFIX + "slot", slot);
        p.putInt(PREFIX + "port", port);
        p.putInt(PREFIX + "btDbNumber", btDbNumber);
        p.putInt(PREFIX + "btDbOffset", btDbOffset);
        p.putInt(PREFIX + "etDbNumber", etDbNumber);
        p.putInt(PREFIX + "etDbOffset", etDbOffset);
    }
}
