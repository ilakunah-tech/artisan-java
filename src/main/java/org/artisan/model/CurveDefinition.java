package org.artisan.model;

import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Describes a single curve: name, color, visibility, line width, and style.
 * Immutable. Used for BT, ET, delta BT, delta ET, and extra curves.
 * Ported from Python artisanlib curves / atypes (plotcurvecolor, linewidth, linestyle).
 */
public final class CurveDefinition {

  private final String name;
  private final Color color;
  private final boolean visible;
  private final float lineWidth;
  private final CurveStyle style;

  private CurveDefinition(Builder b) {
    this.name = b.name != null ? b.name : "";
    this.color = b.color != null ? b.color : Color.BLACK;
    this.visible = b.visible;
    this.lineWidth = b.lineWidth >= 0 ? b.lineWidth : 1.0f;
    this.style = b.style != null ? b.style : CurveStyle.SOLID;
  }

  public String getName() {
    return name;
  }

  public Color getColor() {
    return color;
  }

  public boolean isVisible() {
    return visible;
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public CurveStyle getStyle() {
    return style;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String name = "";
    private Color color = Color.BLACK;
    private boolean visible = true;
    private float lineWidth = 1.0f;
    private CurveStyle style = CurveStyle.SOLID;

    private Builder() {}

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder color(Color color) {
      this.color = color;
      return this;
    }

    public Builder visible(boolean visible) {
      this.visible = visible;
      return this;
    }

    public Builder lineWidth(float lineWidth) {
      this.lineWidth = lineWidth;
      return this;
    }

    public Builder style(CurveStyle style) {
      this.style = style;
      return this;
    }

    public CurveDefinition build() {
      return new CurveDefinition(this);
    }
  }

  /**
   * Generates a Chart-FX CSS style string from this curve's properties.
   * Used to apply curve styling to DoubleDataSet instances.
   */
  public String toChartFxStyle() {
    if (!visible) return "-fx-stroke: transparent; -fx-stroke-width: 0px;";
    String hex = String.format("#%02x%02x%02x",
        (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    StringBuilder sb = new StringBuilder();
    sb.append("-fx-stroke: ").append(hex).append("; ");
    sb.append("-fx-stroke-width: ").append((int) lineWidth).append("px;");
    if (style == CurveStyle.DASHED) sb.append(" -fx-stroke-dash-array: 8 6;");
    else if (style == CurveStyle.DOTTED) sb.append(" -fx-stroke-dash-array: 2 4;");
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CurveDefinition that = (CurveDefinition) o;
    return visible == that.visible
        && Float.compare(that.lineWidth, lineWidth) == 0
        && Objects.equals(name, that.name)
        && Objects.equals(color, that.color)
        && style == that.style;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, color, visible, lineWidth, style);
  }
}
