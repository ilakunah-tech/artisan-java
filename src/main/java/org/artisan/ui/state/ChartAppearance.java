package org.artisan.ui.state;

import java.util.Objects;

/**
 * Full visual customization for the live roast chart (RI5-style).
 * Stored in UIPreferences JSON and applied live by the chart controller.
 */
public final class ChartAppearance {

    public enum LineStyle { SOLID, DASHED, DOTTED, STEPPED }
    public enum LegendPosition { LEFT, RIGHT, TOP, BOTTOM, HIDDEN }

    private String backgroundMain = "#FAFAFA";
    private String backgroundBottom = "#FAFAFA";
    private String gridColor = "#E5E5E5";
    private double gridOpacity = 1.0;

    private String axisFontFamily = "Arial";
    private double axisFontSize = 12.0;
    private String axisFontColor = "#303030";

    private String btColor = "#E74C3C";
    private String etColor = "#3498DB";
    private String rorBtColor = "#2ECC71";
    private String rorEtColor = "#5DADE2";
    private String gasColor = "#95A5A6";
    private String drumColor = "#F1C40F";
    private String eventLineColor = "#2C3E50";
    private String eventDotColor = "#3498DB";

    private double btWidth = 2.0;
    private double etWidth = 1.8;
    private double rorBtWidth = 1.5;
    private double rorEtWidth = 1.2;
    private double gasWidth = 1.8;
    private double drumWidth = 1.6;

    private LineStyle btLineStyle = LineStyle.SOLID;
    private LineStyle etLineStyle = LineStyle.SOLID;
    private LineStyle rorBtLineStyle = LineStyle.DASHED;
    private LineStyle rorEtLineStyle = LineStyle.DASHED;
    private LineStyle gasLineStyle = LineStyle.STEPPED;
    private LineStyle drumLineStyle = LineStyle.STEPPED;

    private double gasFillOpacity = 0.20;

    private LegendPosition legendPosition = LegendPosition.RIGHT;

    private String annotationBoxBg = "#FFFFFF";
    private String annotationTextColor = "#000000";
    private double annotationFontSize = 11.0;

    private String readoutBtColor = "#E74C3C";
    private String readoutEtColor = "#3498DB";
    private String readoutRorBtColor = "#2ECC71";
    private String readoutRorEtColor = "#5DADE2";
    private double readoutMainFontSize = 28.0;
    private double readoutSecondaryFontSize = 14.0;

    public static ChartAppearance ri5Default() {
        return new ChartAppearance();
    }

    public static ChartAppearance darkMode() {
        ChartAppearance a = new ChartAppearance();
        a.backgroundMain = "#1B1F24";
        a.backgroundBottom = "#1B1F24";
        a.gridColor = "#2A2F36";
        a.axisFontColor = "#D0D7DE";
        a.annotationBoxBg = "#232833";
        a.annotationTextColor = "#F0F0F0";
        return a;
    }

    public static ChartAppearance minimal() {
        ChartAppearance a = new ChartAppearance();
        a.gridOpacity = 0.6;
        a.btWidth = 1.6;
        a.etWidth = 1.4;
        a.rorBtWidth = 1.1;
        a.rorEtWidth = 1.0;
        a.gasFillOpacity = 0.10;
        a.legendPosition = LegendPosition.TOP;
        return a;
    }

    public ChartAppearance copy() {
        ChartAppearance c = new ChartAppearance();
        c.backgroundMain = backgroundMain;
        c.backgroundBottom = backgroundBottom;
        c.gridColor = gridColor;
        c.gridOpacity = gridOpacity;
        c.axisFontFamily = axisFontFamily;
        c.axisFontSize = axisFontSize;
        c.axisFontColor = axisFontColor;
        c.btColor = btColor;
        c.etColor = etColor;
        c.rorBtColor = rorBtColor;
        c.rorEtColor = rorEtColor;
        c.gasColor = gasColor;
        c.drumColor = drumColor;
        c.eventLineColor = eventLineColor;
        c.eventDotColor = eventDotColor;
        c.btWidth = btWidth;
        c.etWidth = etWidth;
        c.rorBtWidth = rorBtWidth;
        c.rorEtWidth = rorEtWidth;
        c.gasWidth = gasWidth;
        c.drumWidth = drumWidth;
        c.btLineStyle = btLineStyle;
        c.etLineStyle = etLineStyle;
        c.rorBtLineStyle = rorBtLineStyle;
        c.rorEtLineStyle = rorEtLineStyle;
        c.gasLineStyle = gasLineStyle;
        c.drumLineStyle = drumLineStyle;
        c.gasFillOpacity = gasFillOpacity;
        c.legendPosition = legendPosition;
        c.annotationBoxBg = annotationBoxBg;
        c.annotationTextColor = annotationTextColor;
        c.annotationFontSize = annotationFontSize;
        c.readoutBtColor = readoutBtColor;
        c.readoutEtColor = readoutEtColor;
        c.readoutRorBtColor = readoutRorBtColor;
        c.readoutRorEtColor = readoutRorEtColor;
        c.readoutMainFontSize = readoutMainFontSize;
        c.readoutSecondaryFontSize = readoutSecondaryFontSize;
        return c;
    }

    public String getBackgroundMain() { return backgroundMain; }
    public void setBackgroundMain(String v) { backgroundMain = safeHex(v, "#FAFAFA"); }
    public String getBackgroundBottom() { return backgroundBottom; }
    public void setBackgroundBottom(String v) { backgroundBottom = safeHex(v, "#FAFAFA"); }
    public String getGridColor() { return gridColor; }
    public void setGridColor(String v) { gridColor = safeHex(v, "#E5E5E5"); }
    public double getGridOpacity() { return gridOpacity; }
    public void setGridOpacity(double v) { gridOpacity = clamp(v, 0.0, 1.0); }

    public String getAxisFontFamily() { return axisFontFamily; }
    public void setAxisFontFamily(String v) { axisFontFamily = v != null && !v.isBlank() ? v : "Arial"; }
    public double getAxisFontSize() { return axisFontSize; }
    public void setAxisFontSize(double v) { axisFontSize = clamp(v, 8.0, 24.0); }
    public String getAxisFontColor() { return axisFontColor; }
    public void setAxisFontColor(String v) { axisFontColor = safeHex(v, "#303030"); }

    public String getBtColor() { return btColor; }
    public void setBtColor(String v) { btColor = safeHex(v, "#E74C3C"); }
    public String getEtColor() { return etColor; }
    public void setEtColor(String v) { etColor = safeHex(v, "#3498DB"); }
    public String getRorBtColor() { return rorBtColor; }
    public void setRorBtColor(String v) { rorBtColor = safeHex(v, "#2ECC71"); }
    public String getRorEtColor() { return rorEtColor; }
    public void setRorEtColor(String v) { rorEtColor = safeHex(v, "#5DADE2"); }
    public String getGasColor() { return gasColor; }
    public void setGasColor(String v) { gasColor = safeHex(v, "#95A5A6"); }
    public String getDrumColor() { return drumColor; }
    public void setDrumColor(String v) { drumColor = safeHex(v, "#F1C40F"); }
    public String getEventLineColor() { return eventLineColor; }
    public void setEventLineColor(String v) { eventLineColor = safeHex(v, "#2C3E50"); }
    public String getEventDotColor() { return eventDotColor; }
    public void setEventDotColor(String v) { eventDotColor = safeHex(v, "#3498DB"); }

    public double getBtWidth() { return btWidth; }
    public void setBtWidth(double v) { btWidth = clamp(v, 0.5, 4.0); }
    public double getEtWidth() { return etWidth; }
    public void setEtWidth(double v) { etWidth = clamp(v, 0.5, 4.0); }
    public double getRorBtWidth() { return rorBtWidth; }
    public void setRorBtWidth(double v) { rorBtWidth = clamp(v, 0.5, 4.0); }
    public double getRorEtWidth() { return rorEtWidth; }
    public void setRorEtWidth(double v) { rorEtWidth = clamp(v, 0.5, 4.0); }
    public double getGasWidth() { return gasWidth; }
    public void setGasWidth(double v) { gasWidth = clamp(v, 0.5, 4.0); }
    public double getDrumWidth() { return drumWidth; }
    public void setDrumWidth(double v) { drumWidth = clamp(v, 0.5, 4.0); }

    public LineStyle getBtLineStyle() { return btLineStyle; }
    public void setBtLineStyle(LineStyle v) { btLineStyle = v != null ? v : LineStyle.SOLID; }
    public LineStyle getEtLineStyle() { return etLineStyle; }
    public void setEtLineStyle(LineStyle v) { etLineStyle = v != null ? v : LineStyle.SOLID; }
    public LineStyle getRorBtLineStyle() { return rorBtLineStyle; }
    public void setRorBtLineStyle(LineStyle v) { rorBtLineStyle = v != null ? v : LineStyle.DASHED; }
    public LineStyle getRorEtLineStyle() { return rorEtLineStyle; }
    public void setRorEtLineStyle(LineStyle v) { rorEtLineStyle = v != null ? v : LineStyle.DASHED; }
    public LineStyle getGasLineStyle() { return gasLineStyle; }
    public void setGasLineStyle(LineStyle v) { gasLineStyle = v != null ? v : LineStyle.STEPPED; }
    public LineStyle getDrumLineStyle() { return drumLineStyle; }
    public void setDrumLineStyle(LineStyle v) { drumLineStyle = v != null ? v : LineStyle.STEPPED; }

    public double getGasFillOpacity() { return gasFillOpacity; }
    public void setGasFillOpacity(double v) { gasFillOpacity = clamp(v, 0.0, 1.0); }

    public LegendPosition getLegendPosition() { return legendPosition; }
    public void setLegendPosition(LegendPosition v) { legendPosition = v != null ? v : LegendPosition.RIGHT; }

    public String getAnnotationBoxBg() { return annotationBoxBg; }
    public void setAnnotationBoxBg(String v) { annotationBoxBg = safeHex(v, "#FFFFFF"); }
    public String getAnnotationTextColor() { return annotationTextColor; }
    public void setAnnotationTextColor(String v) { annotationTextColor = safeHex(v, "#000000"); }
    public double getAnnotationFontSize() { return annotationFontSize; }
    public void setAnnotationFontSize(double v) { annotationFontSize = clamp(v, 8.0, 20.0); }

    public String getReadoutBtColor() { return readoutBtColor; }
    public void setReadoutBtColor(String v) { readoutBtColor = safeHex(v, "#E74C3C"); }
    public String getReadoutEtColor() { return readoutEtColor; }
    public void setReadoutEtColor(String v) { readoutEtColor = safeHex(v, "#3498DB"); }
    public String getReadoutRorBtColor() { return readoutRorBtColor; }
    public void setReadoutRorBtColor(String v) { readoutRorBtColor = safeHex(v, "#2ECC71"); }
    public String getReadoutRorEtColor() { return readoutRorEtColor; }
    public void setReadoutRorEtColor(String v) { readoutRorEtColor = safeHex(v, "#5DADE2"); }
    public double getReadoutMainFontSize() { return readoutMainFontSize; }
    public void setReadoutMainFontSize(double v) { readoutMainFontSize = clamp(v, 16.0, 40.0); }
    public double getReadoutSecondaryFontSize() { return readoutSecondaryFontSize; }
    public void setReadoutSecondaryFontSize(double v) { readoutSecondaryFontSize = clamp(v, 10.0, 20.0); }

    private static String safeHex(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v.trim();
    }

    private static double clamp(double v, double min, double max) {
        if (!Double.isFinite(v)) return min;
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChartAppearance that)) return false;
        return Double.compare(that.gridOpacity, gridOpacity) == 0
            && Double.compare(that.btWidth, btWidth) == 0
            && Double.compare(that.etWidth, etWidth) == 0
            && Double.compare(that.rorBtWidth, rorBtWidth) == 0
            && Double.compare(that.rorEtWidth, rorEtWidth) == 0
            && Double.compare(that.gasWidth, gasWidth) == 0
            && Double.compare(that.drumWidth, drumWidth) == 0
            && Double.compare(that.gasFillOpacity, gasFillOpacity) == 0
            && Double.compare(that.axisFontSize, axisFontSize) == 0
            && Double.compare(that.annotationFontSize, annotationFontSize) == 0
            && Double.compare(that.readoutMainFontSize, readoutMainFontSize) == 0
            && Double.compare(that.readoutSecondaryFontSize, readoutSecondaryFontSize) == 0
            && Objects.equals(backgroundMain, that.backgroundMain)
            && Objects.equals(backgroundBottom, that.backgroundBottom)
            && Objects.equals(gridColor, that.gridColor)
            && Objects.equals(axisFontFamily, that.axisFontFamily)
            && Objects.equals(axisFontColor, that.axisFontColor)
            && Objects.equals(btColor, that.btColor)
            && Objects.equals(etColor, that.etColor)
            && Objects.equals(rorBtColor, that.rorBtColor)
            && Objects.equals(rorEtColor, that.rorEtColor)
            && Objects.equals(gasColor, that.gasColor)
            && Objects.equals(drumColor, that.drumColor)
            && Objects.equals(eventLineColor, that.eventLineColor)
            && Objects.equals(eventDotColor, that.eventDotColor)
            && btLineStyle == that.btLineStyle
            && etLineStyle == that.etLineStyle
            && rorBtLineStyle == that.rorBtLineStyle
            && rorEtLineStyle == that.rorEtLineStyle
            && gasLineStyle == that.gasLineStyle
            && drumLineStyle == that.drumLineStyle
            && legendPosition == that.legendPosition
            && Objects.equals(annotationBoxBg, that.annotationBoxBg)
            && Objects.equals(annotationTextColor, that.annotationTextColor)
            && Objects.equals(readoutBtColor, that.readoutBtColor)
            && Objects.equals(readoutEtColor, that.readoutEtColor)
            && Objects.equals(readoutRorBtColor, that.readoutRorBtColor)
            && Objects.equals(readoutRorEtColor, that.readoutRorEtColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backgroundMain, backgroundBottom, gridColor, gridOpacity,
            axisFontFamily, axisFontSize, axisFontColor,
            btColor, etColor, rorBtColor, rorEtColor, gasColor, drumColor, eventLineColor, eventDotColor,
            btWidth, etWidth, rorBtWidth, rorEtWidth, gasWidth, drumWidth,
            btLineStyle, etLineStyle, rorBtLineStyle, rorEtLineStyle, gasLineStyle, drumLineStyle,
            gasFillOpacity, legendPosition, annotationBoxBg, annotationTextColor, annotationFontSize,
            readoutBtColor, readoutEtColor, readoutRorBtColor, readoutRorEtColor,
            readoutMainFontSize, readoutSecondaryFontSize);
    }
}
