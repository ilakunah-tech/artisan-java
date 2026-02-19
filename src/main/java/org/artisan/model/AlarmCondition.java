package org.artisan.model;

/**
 * Alarm trigger condition (IF part). Matches Python Artisan alarmcond/alarmtime.
 */
public enum AlarmCondition {
    /** Fire when BT rises above threshold (°C) */
    BT_RISES_ABOVE,
    /** Fire when BT falls below threshold (°C) */
    BT_FALLS_BELOW,
    /** Fire when ET rises above threshold (°C) */
    ET_RISES_ABOVE,
    /** Fire when ET falls below threshold (°C) */
    ET_FALLS_BELOW,
    /** Fire when RoR (BT) rises above threshold (°C/min) */
    ROR_RISES_ABOVE,
    /** Fire when RoR (BT) falls below threshold (°C/min) */
    ROR_FALLS_BELOW,
    /** Fire when time since CHARGE (seconds) >= threshold */
    TIME_AFTER_CHARGE,
    /** Fire when time (seconds) >= threshold (e.g. time of a referenced event) */
    TIME_AFTER_EVENT
}
