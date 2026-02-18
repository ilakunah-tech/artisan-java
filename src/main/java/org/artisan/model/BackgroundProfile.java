package org.artisan.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds reference (background) roast data for overlay display.
 * Snapshot copy from CanvasData so changes to the original do not affect the background.
 * Ported from Python artisanlib background/curves (btimex, btemp1, btemp2, bdelta1, bdelta2).
 */
public final class BackgroundProfile {

  private final List<Double> timex;
  private final List<Double> temp1;
  private final List<Double> temp2;
  private final List<Double> delta1;
  private final List<Double> delta2;
  private final String profileName;
  private final boolean visible;

  private BackgroundProfile(
      List<Double> timex,
      List<Double> temp1,
      List<Double> temp2,
      List<Double> delta1,
      List<Double> delta2,
      String profileName,
      boolean visible) {
    this.timex = timex != null ? new ArrayList<>(timex) : new ArrayList<>();
    this.temp1 = temp1 != null ? new ArrayList<>(temp1) : new ArrayList<>();
    this.temp2 = temp2 != null ? new ArrayList<>(temp2) : new ArrayList<>();
    this.delta1 = delta1 != null ? new ArrayList<>(delta1) : new ArrayList<>();
    this.delta2 = delta2 != null ? new ArrayList<>(delta2) : new ArrayList<>();
    this.profileName = profileName != null ? profileName : "";
    this.visible = visible;
  }

  /** Creates a snapshot copy from canvas data. Later changes to canvasData do not affect this. */
  public static BackgroundProfile fromCanvasData(CanvasData canvasData, String name) {
    if (canvasData == null) {
      return new BackgroundProfile(null, null, null, null, null, name != null ? name : "", false);
    }
    return new BackgroundProfile(
        canvasData.getTimex(),
        canvasData.getTemp1(),
        canvasData.getTemp2(),
        canvasData.getDelta1(),
        canvasData.getDelta2(),
        name != null ? name : "",
        true);
  }

  public boolean isEmpty() {
    return timex.isEmpty();
  }

  public List<Double> getTimex() {
    return Collections.unmodifiableList(timex);
  }

  public List<Double> getTemp1() {
    return Collections.unmodifiableList(temp1);
  }

  public List<Double> getTemp2() {
    return Collections.unmodifiableList(temp2);
  }

  public List<Double> getDelta1() {
    return Collections.unmodifiableList(delta1);
  }

  public List<Double> getDelta2() {
    return Collections.unmodifiableList(delta2);
  }

  public String getProfileName() {
    return profileName;
  }

  public boolean isVisible() {
    return visible;
  }
}
