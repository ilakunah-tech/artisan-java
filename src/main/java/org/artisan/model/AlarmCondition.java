package org.artisan.model;

/**
 * Alarm trigger condition (from atypes alarmcond and alarmtime).
 */
public enum AlarmCondition {
    /** Fire when temperature rises above threshold */
    ABOVE_TEMP,
    /** Fire when temperature falls below threshold */
    BELOW_TEMP,
    /** Fire when current time (sec) >= threshold */
    AT_TIME,
    /** Fire when current time (sec) >= threshold (e.g. time of a referenced event) */
    AFTER_EVENT
}
