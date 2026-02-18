package org.artisan.model;

/**
 * Line style for a curve (BT, ET, delta, or extra).
 * Ported from Python artisanlib curves/comparator (linestyle: '-', '--', ':').
 */
public enum CurveStyle {
  SOLID,
  DASHED,
  DOTTED
}
