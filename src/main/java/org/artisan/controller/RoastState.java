package org.artisan.controller;

/**
 * Roast session state machine: OFF → CHARGING → ROASTING → DROPPING → COOLING → OFF.
 * Ported from Python main.py / qmc (flagon, timeindex, button states).
 */
public enum RoastState {
  OFF,
  CHARGING,
  ROASTING,
  DROPPING,
  COOLING
}
