package org.artisan.controller;

import org.artisan.model.CanvasData;
import org.artisan.model.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoastSessionTest {

  private RoastSession session;

  @BeforeEach
  void setUp() {
    session = new RoastSession();
  }

  @Test
  void initialStateIsOff() {
    assertEquals(RoastState.OFF, session.getState());
    assertFalse(session.isActive());
  }

  @Test
  void startSetsCharging() {
    session.start();
    assertEquals(RoastState.CHARGING, session.getState());
    assertTrue(session.isActive());
    assertTrue(session.getStartTimeMs() > 0);
  }

  @Test
  void markChargeRecordsEventWithTypeCharge() {
    session.start();
    CanvasData cd = session.getCanvasData();
    cd.addDataPoint(0.0, 100.0, 200.0);
    cd.addDataPoint(60.0, 150.0, 220.0);
    session.markCharge(0);
    assertEquals(1, session.getEvents().size());
    assertEquals(EventType.CHARGE, session.getEvents().get(0).getType());
    assertEquals(0, session.getEvents().get(0).getTimeIndex());
    assertEquals(100.0, session.getEvents().get(0).getTemp());
    assertEquals(0, cd.getChargeIndex());
  }

  @Test
  void markDropSetsStateToDropping() {
    session.start();
    session.getCanvasData().addDataPoint(0.0, 20.0, 180.0);
    session.getCanvasData().addDataPoint(120.0, 200.0, 220.0);
    session.markDrop(1);
    assertEquals(RoastState.DROPPING, session.getState());
    assertEquals(1, session.getEvents().getByType(EventType.DROP).size());
  }

  @Test
  void markCoolEndSetsStateToOff() {
    session.start();
    session.getCanvasData().addDataPoint(0.0, 20.0, 180.0);
    session.markCoolEnd(0);
    assertEquals(RoastState.OFF, session.getState());
    assertFalse(session.isActive());
  }

  @Test
  void resetClearsDataAndStateOff() {
    session.start();
    session.getCanvasData().addDataPoint(0.0, 100.0, 200.0);
    session.markCharge(0);
    session.reset();
    assertEquals(RoastState.OFF, session.getState());
    assertTrue(session.getCanvasData().getTimex().isEmpty());
    assertEquals(0, session.getEvents().size());
    assertEquals(-1, session.getCanvasData().getChargeIndex());
  }

  @Test
  void isActiveTrueWhenChargingOrRoasting() {
    session.start();
    assertTrue(session.isActive());
    session.getCanvasData().addDataPoint(0.0, 100.0, 200.0);
    session.markCharge(0);
    assertTrue(session.isActive());
  }
}
