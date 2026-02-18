package org.artisan.model;

/**
 * Roast event type (from atypes / ProfileData timeindex and etypes).
 */
public enum EventType {
    CHARGE,
    DRY_END,
    FC_START,
    FC_END,
    SC_START,
    SC_END,
    DROP,
    COOL_END,
    CUSTOM
}
