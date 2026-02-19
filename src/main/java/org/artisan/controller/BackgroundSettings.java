package org.artisan.controller;

import java.util.prefs.Preferences;

/**
 * Background profile overlay settings persisted under Preferences keys "background.*".
 */
public final class BackgroundSettings {

  private static final String NODE = "org/artisan/artisan-java";
  private static final String PREFIX = "background.";

  private static final String KEY_ENABLED = PREFIX + "enabled";
  private static final String KEY_LAST_FILE_PATH = PREFIX + "lastFilePath";
  private static final String KEY_ALIGN_OFFSET = PREFIX + "alignOffset";
  private static final String KEY_SHOW_BG_ET = PREFIX + "showBgET";
  private static final String KEY_SHOW_BG_BT = PREFIX + "showBgBT";
  private static final String KEY_SHOW_BG_DELTA_ET = PREFIX + "showBgDeltaET";
  private static final String KEY_SHOW_BG_DELTA_BT = PREFIX + "showBgDeltaBT";

  public static final boolean DEFAULT_ENABLED = false;
  public static final String DEFAULT_LAST_FILE_PATH = "";
  public static final double DEFAULT_ALIGN_OFFSET = 0.0;
  public static final boolean DEFAULT_SHOW_BG_ET = true;
  public static final boolean DEFAULT_SHOW_BG_BT = true;
  public static final boolean DEFAULT_SHOW_BG_DELTA_ET = false;
  public static final boolean DEFAULT_SHOW_BG_DELTA_BT = false;

  private boolean enabled = DEFAULT_ENABLED;
  private String lastFilePath = DEFAULT_LAST_FILE_PATH;
  private double alignOffset = DEFAULT_ALIGN_OFFSET;
  private boolean showBgET = DEFAULT_SHOW_BG_ET;
  private boolean showBgBT = DEFAULT_SHOW_BG_BT;
  private boolean showBgDeltaET = DEFAULT_SHOW_BG_DELTA_ET;
  private boolean showBgDeltaBT = DEFAULT_SHOW_BG_DELTA_BT;

  private static Preferences prefs() {
    return Preferences.userRoot().node(NODE);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getLastFilePath() {
    return lastFilePath;
  }

  public void setLastFilePath(String lastFilePath) {
    this.lastFilePath = lastFilePath != null ? lastFilePath : "";
  }

  public double getAlignOffset() {
    return alignOffset;
  }

  public void setAlignOffset(double alignOffset) {
    this.alignOffset = alignOffset;
  }

  public boolean isShowBgET() {
    return showBgET;
  }

  public void setShowBgET(boolean showBgET) {
    this.showBgET = showBgET;
  }

  public boolean isShowBgBT() {
    return showBgBT;
  }

  public void setShowBgBT(boolean showBgBT) {
    this.showBgBT = showBgBT;
  }

  public boolean isShowBgDeltaET() {
    return showBgDeltaET;
  }

  public void setShowBgDeltaET(boolean showBgDeltaET) {
    this.showBgDeltaET = showBgDeltaET;
  }

  public boolean isShowBgDeltaBT() {
    return showBgDeltaBT;
  }

  public void setShowBgDeltaBT(boolean showBgDeltaBT) {
    this.showBgDeltaBT = showBgDeltaBT;
  }

  /** Loads persisted settings from Preferences. */
  public static BackgroundSettings load() {
    BackgroundSettings s = new BackgroundSettings();
    Preferences p = prefs();
    s.enabled = p.getBoolean(KEY_ENABLED, DEFAULT_ENABLED);
    s.lastFilePath = p.get(KEY_LAST_FILE_PATH, DEFAULT_LAST_FILE_PATH);
    s.alignOffset = p.getDouble(KEY_ALIGN_OFFSET, DEFAULT_ALIGN_OFFSET);
    s.showBgET = p.getBoolean(KEY_SHOW_BG_ET, DEFAULT_SHOW_BG_ET);
    s.showBgBT = p.getBoolean(KEY_SHOW_BG_BT, DEFAULT_SHOW_BG_BT);
    s.showBgDeltaET = p.getBoolean(KEY_SHOW_BG_DELTA_ET, DEFAULT_SHOW_BG_DELTA_ET);
    s.showBgDeltaBT = p.getBoolean(KEY_SHOW_BG_DELTA_BT, DEFAULT_SHOW_BG_DELTA_BT);
    return s;
  }

  /** Saves this instance to Preferences. */
  public void save() {
    Preferences p = prefs();
    p.putBoolean(KEY_ENABLED, enabled);
    p.put(KEY_LAST_FILE_PATH, lastFilePath != null ? lastFilePath : "");
    p.putDouble(KEY_ALIGN_OFFSET, alignOffset);
    p.putBoolean(KEY_SHOW_BG_ET, showBgET);
    p.putBoolean(KEY_SHOW_BG_BT, showBgBT);
    p.putBoolean(KEY_SHOW_BG_DELTA_ET, showBgDeltaET);
    p.putBoolean(KEY_SHOW_BG_DELTA_BT, showBgDeltaBT);
  }

  public Config toConfig() {
    Config c = new Config();
    c.setEnabled(enabled);
    c.setLastFilePath(lastFilePath);
    c.setAlignOffset(alignOffset);
    c.setShowBgET(showBgET);
    c.setShowBgBT(showBgBT);
    c.setShowBgDeltaET(showBgDeltaET);
    c.setShowBgDeltaBT(showBgDeltaBT);
    return c;
  }

  public void fromConfig(Config c) {
    if (c == null) return;
    setEnabled(c.isEnabled());
    setLastFilePath(c.getLastFilePath());
    setAlignOffset(c.getAlignOffset());
    setShowBgET(c.isShowBgET());
    setShowBgBT(c.isShowBgBT());
    setShowBgDeltaET(c.isShowBgDeltaET());
    setShowBgDeltaBT(c.isShowBgDeltaBT());
  }

  /** Mutable DTO for dialog editing / apply+cancel. */
  public static final class Config {
    private boolean enabled = DEFAULT_ENABLED;
    private String lastFilePath = DEFAULT_LAST_FILE_PATH;
    private double alignOffset = DEFAULT_ALIGN_OFFSET;
    private boolean showBgET = DEFAULT_SHOW_BG_ET;
    private boolean showBgBT = DEFAULT_SHOW_BG_BT;
    private boolean showBgDeltaET = DEFAULT_SHOW_BG_DELTA_ET;
    private boolean showBgDeltaBT = DEFAULT_SHOW_BG_DELTA_BT;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getLastFilePath() {
      return lastFilePath;
    }

    public void setLastFilePath(String lastFilePath) {
      this.lastFilePath = lastFilePath != null ? lastFilePath : "";
    }

    public double getAlignOffset() {
      return alignOffset;
    }

    public void setAlignOffset(double alignOffset) {
      this.alignOffset = alignOffset;
    }

    public boolean isShowBgET() {
      return showBgET;
    }

    public void setShowBgET(boolean showBgET) {
      this.showBgET = showBgET;
    }

    public boolean isShowBgBT() {
      return showBgBT;
    }

    public void setShowBgBT(boolean showBgBT) {
      this.showBgBT = showBgBT;
    }

    public boolean isShowBgDeltaET() {
      return showBgDeltaET;
    }

    public void setShowBgDeltaET(boolean showBgDeltaET) {
      this.showBgDeltaET = showBgDeltaET;
    }

    public boolean isShowBgDeltaBT() {
      return showBgDeltaBT;
    }

    public void setShowBgDeltaBT(boolean showBgDeltaBT) {
      this.showBgDeltaBT = showBgDeltaBT;
    }
  }
}

