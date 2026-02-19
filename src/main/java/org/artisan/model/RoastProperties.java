package org.artisan.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * Data model for a single roast's metadata.
 * Persistence: Preferences under roast.* keys.
 */
public final class RoastProperties {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "roast.";

    public static final double MIN_WEIGHT_G = 0.0;
    public static final double MAX_WEIGHT_G = 10_000.0;
    public static final int ROAST_COLOR_MIN = 0;
    public static final int ROAST_COLOR_MAX = 100;

    private String title;
    private String notes;
    private String roastDate; // ISO-8601 date string
    private String beanOrigin;
    private String beanVariety;
    private String beanProcess;
    private String beanGrade;
    private double greenWeight;
    private double roastedWeight;
    private double moisture;
    private double density;
    private int roastColor;
    private String operator;
    private List<String> customLabels;

    public RoastProperties() {
        this.title = "";
        this.notes = "";
        this.roastDate = "";
        this.beanOrigin = "";
        this.beanVariety = "";
        this.beanProcess = "";
        this.beanGrade = "";
        this.greenWeight = 0.0;
        this.roastedWeight = 0.0;
        this.moisture = 0.0;
        this.density = 0.0;
        this.roastColor = 0;
        this.operator = "";
        this.customLabels = new ArrayList<>();
    }

    /** Default instance with empty/zero values. */
    public static RoastProperties defaults() {
        return new RoastProperties();
    }

    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(String title) { this.title = title != null ? title : ""; }

    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes != null ? notes : ""; }

    public String getRoastDate() { return roastDate != null ? roastDate : ""; }
    public void setRoastDate(String roastDate) { this.roastDate = roastDate != null ? roastDate : ""; }

    public String getBeanOrigin() { return beanOrigin != null ? beanOrigin : ""; }
    public void setBeanOrigin(String beanOrigin) { this.beanOrigin = beanOrigin != null ? beanOrigin : ""; }

    public String getBeanVariety() { return beanVariety != null ? beanVariety : ""; }
    public void setBeanVariety(String beanVariety) { this.beanVariety = beanVariety != null ? beanVariety : ""; }

    public String getBeanProcess() { return beanProcess != null ? beanProcess : ""; }
    public void setBeanProcess(String beanProcess) { this.beanProcess = beanProcess != null ? beanProcess : ""; }

    public String getBeanGrade() { return beanGrade != null ? beanGrade : ""; }
    public void setBeanGrade(String beanGrade) { this.beanGrade = beanGrade != null ? beanGrade : ""; }

    public double getGreenWeight() { return greenWeight; }
    public void setGreenWeight(double greenWeight) { this.greenWeight = Math.max(MIN_WEIGHT_G, Math.min(MAX_WEIGHT_G, greenWeight)); }

    public double getRoastedWeight() { return roastedWeight; }
    public void setRoastedWeight(double roastedWeight) { this.roastedWeight = Math.max(MIN_WEIGHT_G, Math.min(MAX_WEIGHT_G, roastedWeight)); }

    public double getMoisture() { return moisture; }
    public void setMoisture(double moisture) { this.moisture = moisture; }

    public double getDensity() { return density; }
    public void setDensity(double density) { this.density = density; }

    public int getRoastColor() { return roastColor; }
    public void setRoastColor(int roastColor) { this.roastColor = Math.max(ROAST_COLOR_MIN, Math.min(ROAST_COLOR_MAX, roastColor)); }

    public String getOperator() { return operator != null ? operator : ""; }
    public void setOperator(String operator) { this.operator = operator != null ? operator : ""; }

    public List<String> getCustomLabels() {
        if (customLabels == null) customLabels = new ArrayList<>();
        return customLabels;
    }
    public void setCustomLabels(List<String> customLabels) {
        this.customLabels = customLabels != null ? new ArrayList<>(customLabels) : new ArrayList<>();
    }

    /** For RoastPropertiesValidator: bean name must not be empty. Use title or beanOrigin. */
    public String getBeanName() {
        if (title != null && !title.isBlank()) return title;
        return beanOrigin != null ? beanOrigin : "";
    }

    public double getWeightInGrams() { return greenWeight; }
    public double getWeightOutGrams() { return roastedWeight; }
    public double getMoisturePercent() { return moisture; }
    public double getDensityGramsPerLiter() { return density; }

    /** Weight loss %: (green - roasted) / green * 100. Returns 0 if greenWeight <= 0. */
    public double weightLossPercent() {
        if (greenWeight <= 0) return 0.0;
        return (greenWeight - roastedWeight) / greenWeight * 100.0;
    }

    /** Map for CSV export; all keys present. */
    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("title", getTitle());
        m.put("notes", getNotes());
        m.put("roastDate", getRoastDate());
        m.put("beanOrigin", getBeanOrigin());
        m.put("beanVariety", getBeanVariety());
        m.put("beanProcess", getBeanProcess());
        m.put("beanGrade", getBeanGrade());
        m.put("greenWeight", String.valueOf(greenWeight));
        m.put("roastedWeight", String.valueOf(roastedWeight));
        m.put("moisture", String.valueOf(moisture));
        m.put("density", String.valueOf(density));
        m.put("roastColor", String.valueOf(roastColor));
        m.put("operator", getOperator());
        for (int i = 0; i < getCustomLabels().size(); i++) {
            m.put("customLabel_" + i, getCustomLabels().get(i));
        }
        return m;
    }

    public void load() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        setTitle(p.get(PREFIX + "title", ""));
        setNotes(p.get(PREFIX + "notes", ""));
        setRoastDate(p.get(PREFIX + "roastDate", ""));
        setBeanOrigin(p.get(PREFIX + "beanOrigin", ""));
        setBeanVariety(p.get(PREFIX + "beanVariety", ""));
        setBeanProcess(p.get(PREFIX + "beanProcess", ""));
        setBeanGrade(p.get(PREFIX + "beanGrade", ""));
        setGreenWeight(p.getDouble(PREFIX + "greenWeight", 0.0));
        setRoastedWeight(p.getDouble(PREFIX + "roastedWeight", 0.0));
        setMoisture(p.getDouble(PREFIX + "moisture", 0.0));
        setDensity(p.getDouble(PREFIX + "density", 0.0));
        setRoastColor(p.getInt(PREFIX + "roastColor", 0));
        setOperator(p.get(PREFIX + "operator", ""));
        int n = p.getInt(PREFIX + "customLabelsSize", 0);
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            labels.add(p.get(PREFIX + "customLabel_" + i, ""));
        }
        setCustomLabels(labels);
    }

    public void save() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        p.put(PREFIX + "title", getTitle());
        p.put(PREFIX + "notes", getNotes());
        p.put(PREFIX + "roastDate", getRoastDate());
        p.put(PREFIX + "beanOrigin", getBeanOrigin());
        p.put(PREFIX + "beanVariety", getBeanVariety());
        p.put(PREFIX + "beanProcess", getBeanProcess());
        p.put(PREFIX + "beanGrade", getBeanGrade());
        p.putDouble(PREFIX + "greenWeight", greenWeight);
        p.putDouble(PREFIX + "roastedWeight", roastedWeight);
        p.putDouble(PREFIX + "moisture", moisture);
        p.putDouble(PREFIX + "density", density);
        p.putInt(PREFIX + "roastColor", roastColor);
        p.put(PREFIX + "operator", getOperator());
        List<String> labels = getCustomLabels();
        p.putInt(PREFIX + "customLabelsSize", labels.size());
        for (int i = 0; i < labels.size(); i++) {
            p.put(PREFIX + "customLabel_" + i, labels.get(i));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoastProperties that = (RoastProperties) o;
        return Double.compare(that.greenWeight, greenWeight) == 0
                && Double.compare(that.roastedWeight, roastedWeight) == 0
                && Double.compare(that.moisture, moisture) == 0
                && Double.compare(that.density, density) == 0
                && roastColor == that.roastColor
                && Objects.equals(title, that.title)
                && Objects.equals(notes, that.notes)
                && Objects.equals(roastDate, that.roastDate)
                && Objects.equals(beanOrigin, that.beanOrigin)
                && Objects.equals(beanVariety, that.beanVariety)
                && Objects.equals(beanProcess, that.beanProcess)
                && Objects.equals(beanGrade, that.beanGrade)
                && Objects.equals(operator, that.operator)
                && Objects.equals(getCustomLabels(), that.getCustomLabels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, notes, roastDate, beanOrigin, beanVariety, beanProcess, beanGrade,
                greenWeight, roastedWeight, moisture, density, roastColor, operator, getCustomLabels());
    }

    @Override
    public String toString() {
        return "RoastProperties{title='" + getTitle() + "', roastDate='" + getRoastDate()
                + "', greenWeight=" + greenWeight + ", roastedWeight=" + roastedWeight + "}";
    }
}
