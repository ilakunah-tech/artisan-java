package org.artisan.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Record-like class representing a single roast batch for BatchesDialog and production reports.
 */
public final class Batch {

    private int batchNumber;
    private String title;
    private String date;
    private double greenWeight;
    private double roastedWeight;
    private double totalRoastTimeSec;
    private String profilePath;
    private String notes;
    private boolean exported;
    private int roastColor;

    public Batch() {
        this.batchNumber = 0;
        this.title = "";
        this.date = "";
        this.greenWeight = 0.0;
        this.roastedWeight = 0.0;
        this.totalRoastTimeSec = 0.0;
        this.profilePath = null;
        this.notes = "";
        this.exported = false;
        this.roastColor = 0;
    }

    public int getBatchNumber() { return batchNumber; }
    public void setBatchNumber(int batchNumber) { this.batchNumber = batchNumber; }

    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(String title) { this.title = title != null ? title : ""; }

    public String getDate() { return date != null ? date : ""; }
    public void setDate(String date) { this.date = date != null ? date : ""; }

    public double getGreenWeight() { return greenWeight; }
    public void setGreenWeight(double greenWeight) { this.greenWeight = Math.max(0, greenWeight); }

    public double getRoastedWeight() { return roastedWeight; }
    public void setRoastedWeight(double roastedWeight) { this.roastedWeight = Math.max(0, roastedWeight); }

    public double getTotalRoastTimeSec() { return totalRoastTimeSec; }
    public void setTotalRoastTimeSec(double totalRoastTimeSec) { this.totalRoastTimeSec = Math.max(0, totalRoastTimeSec); }

    public String getProfilePath() { return profilePath; }
    public void setProfilePath(String profilePath) { this.profilePath = profilePath; }

    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes != null ? notes : ""; }

    public boolean isExported() { return exported; }
    public void setExported(boolean exported) { this.exported = exported; }

    /** Roast color (0–100, proxy for quality). Used by ranking report. */
    public int getRoastColor() { return roastColor; }
    public void setRoastColor(int roastColor) { this.roastColor = Math.max(0, Math.min(100, roastColor)); }

    /** Weight loss %: (green - roasted) / green * 100. Returns 0 if greenWeight <= 0. */
    public double weightLossPercent() {
        if (greenWeight <= 0) return 0.0;
        return (greenWeight - roastedWeight) / greenWeight * 100.0;
    }

    /** Map for CSV export; all keys present. */
    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("batchNumber", String.valueOf(batchNumber));
        m.put("title", getTitle());
        m.put("date", getDate());
        m.put("greenWeight", String.valueOf(greenWeight));
        m.put("roastedWeight", String.valueOf(roastedWeight));
        m.put("totalRoastTimeSec", String.valueOf(totalRoastTimeSec));
        m.put("profilePath", profilePath != null ? profilePath : "");
        m.put("notes", getNotes());
        m.put("exported", String.valueOf(exported));
        m.put("roastColor", String.valueOf(roastColor));
        return m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Batch batch = (Batch) o;
        return batchNumber == batch.batchNumber
                && Double.compare(batch.greenWeight, greenWeight) == 0
                && Double.compare(batch.roastedWeight, roastedWeight) == 0
                && Double.compare(batch.totalRoastTimeSec, totalRoastTimeSec) == 0
                && exported == batch.exported
                && roastColor == batch.roastColor
                && Objects.equals(title, batch.title)
                && Objects.equals(date, batch.date)
                && Objects.equals(profilePath, batch.profilePath)
                && Objects.equals(notes, batch.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchNumber, title, date, greenWeight, roastedWeight, totalRoastTimeSec, profilePath, notes, exported, roastColor);
    }
}
