package org.artisan.controller;

import org.artisan.model.AlarmList;
import org.artisan.model.CanvasData;
import org.artisan.model.EventEntry;
import org.artisan.model.EventList;
import org.artisan.model.EventType;
import org.artisan.model.RoastProperties;

/**
 * Holds the live roast session state: state machine, canvas data, events, alarms, properties.
 * Ported from Python main.py ApplicationWindow + qmc (timex, timeindex, events).
 */
public final class RoastSession {

  private RoastState state = RoastState.OFF;
  private final CanvasData canvasData;
  private final EventList events;
  private final AlarmList alarms;
  private RoastProperties properties;
  private long startTimeMs;

  public RoastSession() {
    this.canvasData = new CanvasData();
    this.events = new EventList();
    this.alarms = new AlarmList();
  }

  public RoastState getState() {
    return state;
  }

  public CanvasData getCanvasData() {
    return canvasData;
  }

  public EventList getEvents() {
    return events;
  }

  public AlarmList getAlarms() {
    return alarms;
  }

  public RoastProperties getProperties() {
    return properties;
  }

  public void setProperties(RoastProperties properties) {
    this.properties = properties;
  }

  public long getStartTimeMs() {
    return startTimeMs;
  }

  /** Starts the session: state = CHARGING, records start time. */
  public void start() {
    this.state = RoastState.CHARGING;
    this.startTimeMs = System.currentTimeMillis();
  }

  /** Records CHARGE event at the given timex index. */
  public void markCharge(int timexIndex) {
    double temp = tempAt(timexIndex);
    events.add(new EventEntry(timexIndex, temp, "Charge", EventType.CHARGE));
    canvasData.setChargeIndex(timexIndex);
    if (state == RoastState.CHARGING) {
      state = RoastState.ROASTING;
    }
  }

  public void markDryEnd(int timexIndex) {
    double temp = tempAt(timexIndex);
    events.add(new EventEntry(timexIndex, temp, "Dry End", EventType.DRY_END));
    canvasData.setDryEndIndex(timexIndex);
  }

  public void markFcStart(int timexIndex) {
    double temp = tempAt(timexIndex);
    events.add(new EventEntry(timexIndex, temp, "FC Start", EventType.FC_START));
    canvasData.setFcStartIndex(timexIndex);
  }

  public void markFcEnd(int timexIndex) {
    double temp = tempAt(timexIndex);
    events.add(new EventEntry(timexIndex, temp, "FC End", EventType.FC_END));
    canvasData.setFcEndIndex(timexIndex);
  }

  /** Records DROP and sets state to DROPPING. */
  public void markDrop(int timexIndex) {
    double temp = tempAt(timexIndex);
    events.add(new EventEntry(timexIndex, temp, "Drop", EventType.DROP));
    canvasData.setDropIndex(timexIndex);
    this.state = RoastState.DROPPING;
  }

  /** Records cool end and sets state to OFF. */
  public void markCoolEnd(int timexIndex) {
    double temp = tempAt(timexIndex);
    events.add(new EventEntry(timexIndex, temp, "Cool End", EventType.COOL_END));
    this.state = RoastState.OFF;
  }

  public boolean isActive() {
    return state != RoastState.OFF;
  }

  /** Clears all data and sets state to OFF. */
  public void reset() {
    canvasData.clear();
    events.clear();
    alarms.resetAll();
    this.state = RoastState.OFF;
  }

  private double tempAt(int timexIndex) {
    if (timexIndex < 0) return 0;
    var temp2 = canvasData.getTemp2();
    if (timexIndex < temp2.size()) return temp2.get(timexIndex);
    return 0;
  }
}
