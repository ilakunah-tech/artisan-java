package org.artisan.model;

/**
 * Roast event marker types (same semantics as Python artisanlib events).
 * Used for CHARGE, DRY END, FC START, FC END, DROP, etc.
 */
public enum EventType {
    CHARGE,
    DRY_END,
    FC_START,
    FC_END,
    DROP,
    /** Additional custom events use ordinals >= 5 or separate handling. */
    CUSTOM
}
