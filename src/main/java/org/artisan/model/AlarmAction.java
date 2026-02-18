package org.artisan.model;

/**
 * Action to perform when an alarm fires (from atypes alarmaction).
 */
public enum AlarmAction {
    TRIGGER_EVENT,
    SET_SV,
    CALL_PROGRAM,
    POP_UP
}
