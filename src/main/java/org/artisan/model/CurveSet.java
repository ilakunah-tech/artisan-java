package org.artisan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Complete set of curves for one roast session: BT, ET, delta BT, delta ET, and up to 4 extra.
 * Immutable: with* and addExtraCurve return new instances.
 * Ported from Python artisanlib curves / atypes (main + extra curve definitions).
 */
public final class CurveSet {

  private static final int MAX_EXTRA_CURVES = 4;

  private final CurveDefinition bt;
  private final CurveDefinition et;
  private final CurveDefinition deltaBt;
  private final CurveDefinition deltaEt;
  private final List<CurveDefinition> extraCurves;

  private CurveSet(
      CurveDefinition bt,
      CurveDefinition et,
      CurveDefinition deltaBt,
      CurveDefinition deltaEt,
      List<CurveDefinition> extraCurves) {
    this.bt = bt != null ? bt : CurveDefinition.builder().name("BT").build();
    this.et = et != null ? et : CurveDefinition.builder().name("ET").build();
    this.deltaBt = deltaBt != null ? deltaBt : CurveDefinition.builder().name("ΔBT").visible(false).build();
    this.deltaEt = deltaEt != null ? deltaEt : CurveDefinition.builder().name("ΔET").visible(false).build();
    this.extraCurves = extraCurves != null ? new ArrayList<>(extraCurves) : new ArrayList<>();
  }

  public CurveDefinition getBt() {
    return bt;
  }

  public CurveDefinition getEt() {
    return et;
  }

  public CurveDefinition getDeltaBt() {
    return deltaBt;
  }

  public CurveDefinition getDeltaEt() {
    return deltaEt;
  }

  /** Unmodifiable list of extra curves (at most 4). */
  public List<CurveDefinition> getExtraCurves() {
    return Collections.unmodifiableList(extraCurves);
  }

  /**
   * Finds a curve by exact name match among BT, ET, delta BT, delta ET, and extra curves.
   */
  public Optional<CurveDefinition> getCurveByName(String name) {
    if (name == null) return Optional.empty();
    if (name.equals(bt.getName())) return Optional.of(bt);
    if (name.equals(et.getName())) return Optional.of(et);
    if (name.equals(deltaBt.getName())) return Optional.of(deltaBt);
    if (name.equals(deltaEt.getName())) return Optional.of(deltaEt);
    for (CurveDefinition c : extraCurves) {
      if (name.equals(c.getName())) return Optional.of(c);
    }
    return Optional.empty();
  }

  public CurveSet withBt(CurveDefinition bt) {
    return new CurveSet(bt, et, deltaBt, deltaEt, extraCurves);
  }

  public CurveSet withEt(CurveDefinition et) {
    return new CurveSet(bt, et, deltaBt, deltaEt, extraCurves);
  }

  public CurveSet withDeltaBt(CurveDefinition deltaBt) {
    return new CurveSet(bt, et, deltaBt, deltaEt, extraCurves);
  }

  public CurveSet withDeltaEt(CurveDefinition deltaEt) {
    return new CurveSet(bt, et, deltaBt, deltaEt, extraCurves);
  }

  /**
   * Returns a new CurveSet with the extra curve appended. At most 4 extra curves allowed.
   *
   * @throws IllegalStateException if there are already 4 extra curves
   */
  public CurveSet addExtraCurve(CurveDefinition curve) {
    if (curve == null) return this;
    if (extraCurves.size() >= MAX_EXTRA_CURVES) {
      throw new IllegalStateException("Cannot add more than " + MAX_EXTRA_CURVES + " extra curves");
    }
    List<CurveDefinition> next = new ArrayList<>(extraCurves);
    next.add(curve);
    return new CurveSet(bt, et, deltaBt, deltaEt, next);
  }

  /** Returns all curves that are visible (BT, ET, delta BT, delta ET, and extras). */
  public List<CurveDefinition> getAllVisible() {
    List<CurveDefinition> out = new ArrayList<>();
    if (bt.isVisible()) out.add(bt);
    if (et.isVisible()) out.add(et);
    if (deltaBt.isVisible()) out.add(deltaBt);
    if (deltaEt.isVisible()) out.add(deltaEt);
    for (CurveDefinition c : extraCurves) {
      if (c.isVisible()) out.add(c);
    }
    return out;
  }

  public static CurveSet createDefault() {
    return new CurveSet(null, null, null, null, null);
  }
}
