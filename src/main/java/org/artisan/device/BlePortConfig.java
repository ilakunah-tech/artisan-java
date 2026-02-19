package org.artisan.device;

import java.util.prefs.Preferences;

/**
 * BLE port configuration. Persisted under Preferences "ble.*".
 * Full BLE implementation is platform-specific and not yet implemented.
 */
public final class BlePortConfig {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "ble.";

    private String deviceAddress = "";
    private String serviceUuid = "";
    private String characteristicUuid = "";

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress != null ? deviceAddress : "";
    }

    public String getServiceUuid() {
        return serviceUuid;
    }

    public void setServiceUuid(String serviceUuid) {
        this.serviceUuid = serviceUuid != null ? serviceUuid : "";
    }

    public String getCharacteristicUuid() {
        return characteristicUuid;
    }

    public void setCharacteristicUuid(String characteristicUuid) {
        this.characteristicUuid = characteristicUuid != null ? characteristicUuid : "";
    }

    public static void loadFromPreferences(BlePortConfig target) {
        if (target == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        target.setDeviceAddress(p.get(PREFIX + "deviceAddress", ""));
        target.setServiceUuid(p.get(PREFIX + "serviceUuid", ""));
        target.setCharacteristicUuid(p.get(PREFIX + "characteristicUuid", ""));
    }

    public static void saveToPreferences(BlePortConfig config) {
        if (config == null) return;
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.put(PREFIX + "deviceAddress", config.getDeviceAddress());
        p.put(PREFIX + "serviceUuid", config.getServiceUuid());
        p.put(PREFIX + "characteristicUuid", config.getCharacteristicUuid());
    }
}
