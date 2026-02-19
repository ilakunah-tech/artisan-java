package org.artisan.model;

/**
 * Reference (background) roast profile shown behind the current roast.
 * Similar to Python Artisan "Background" feature.
 */
public final class BackgroundProfile {

  private ProfileData profileData;
  /** Shown in chart legend/title bar. */
  private String title;
  private boolean visible;
  /** Seconds. Positive shifts background to the right. */
  private double alignOffset;

  public BackgroundProfile() {
    this(null, "", false, 0.0);
  }

  public BackgroundProfile(ProfileData profileData, String title, boolean visible, double alignOffset) {
    this.profileData = profileData;
    this.title = title != null ? title : "";
    this.visible = visible;
    this.alignOffset = alignOffset;
  }

  public boolean isEmpty() {
    return profileData == null || profileData.getTimex() == null || profileData.getTimex().isEmpty();
  }

  public ProfileData getProfileData() {
    return profileData;
  }

  public void setProfileData(ProfileData profileData) {
    this.profileData = profileData;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title != null ? title : "";
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public double getAlignOffset() {
    return alignOffset;
  }

  public void setAlignOffset(double alignOffset) {
    this.alignOffset = alignOffset;
  }
}
