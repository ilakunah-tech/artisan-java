package org.artisan.model;

/**
 * Action to perform when an alarm fires (THEN part). Matches Python Artisan alarmaction.
 */
public enum AlarmAction {
    /** Show a popup/message notification */
    POPUP_MESSAGE,
    /** Play a sound file */
    PLAY_SOUND,
    /** Mark an event on the chart (event name in actionParam) */
    MARK_EVENT,
    /** Set burner/gas percentage (0–100 in actionParam) */
    SET_BURNER,
    /** Execute external program (command in actionParam) */
    CALL_PROGRAM
}
