package org.artisan.device;

/**
 * Enum of all supported devices, mirroring Python Artisan devices.
 */
public enum DeviceType {

    NONE("None", false, false),
    GENERIC_SERIAL("Generic Serial (CSV BT,ET)", true, false),
    SIMULATOR("Simulator", false, false),

    AILLIO_R1("Aillio Bullet R1", false, false),
    AILLIO_R2("Aillio Bullet R2", false, false),
    HOTTOP_KN8828B("Hottop KN-8828B", true, false),
    IKAWA("Ikawa", false, false),
    KALEIDO_M1("Kaleido M1", true, false),
    GIESEN("Giesen", true, false),
    LORING("Loring", true, false),
    SANTOKER("Santoker", true, false),
    SANTOKER_R("Santoker R", true, false),
    STRONGHOLD_S7X("Stronghold S7X", true, false),
    ROEST("Roest", false, false),
    MUGMA("Mugma", false, false),
    LEBREW("LeBrew RoastSeeNEXT", false, false),
    BLUEDOT("BlueDOT", false, false),
    PETRONCINI("Petroncini", true, false),

    ACAIA_LUNAR("Acaia Lunar (scale)", true, false),

    MODBUS_TCP("Modbus TCP", false, true),
    MODBUS_RTU("Modbus RTU", true, true);

    private final String displayName;
    private final boolean requiresSerial;
    private final boolean requiresModbus;

    DeviceType(String displayName, boolean requiresSerial, boolean requiresModbus) {
        this.displayName = displayName;
        this.requiresSerial = requiresSerial;
        this.requiresModbus = requiresModbus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRequiresSerial() {
        return requiresSerial;
    }

    public boolean isRequiresModbus() {
        return requiresModbus;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
