package org.artisan.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Flavor score model for cupping. SCA-style total: sum(scores) - defects + 36, clamped to [0, 100].
 */
public final class CupProfile {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "cup.";
    private static final double TOTAL_OFFSET = 36.0;
    private static final double MIN_TOTAL = 0.0;
    private static final double MAX_TOTAL = 100.0;

    /** Default flavor attributes (10). */
    public static final String[] DEFAULT_ATTRIBUTES = {
        "Fragrance/Aroma", "Flavor", "Aftertaste", "Acidity", "Body",
        "Balance", "Uniformity", "Clean Cup", "Sweetness", "Overall"
    };

    private java.util.Map<String, Double> scores;
    private double defects;
    private String cupNotes;

    public CupProfile() {
        this.scores = new LinkedHashMap<>();
        for (String attr : DEFAULT_ATTRIBUTES) {
            scores.put(attr, 0.0);
        }
        this.defects = 0.0;
        this.cupNotes = "";
    }

    /** Default instance with all scores 0. */
    public static CupProfile defaults() {
        CupProfile p = new CupProfile();
        p.scores = new LinkedHashMap<>();
        for (String attr : DEFAULT_ATTRIBUTES) {
            p.scores.put(attr, 0.0);
        }
        p.defects = 0.0;
        p.cupNotes = "";
        return p;
    }

    /** Scores map (flavor attribute → score 0.0–10.0). */
    public Map<String, Double> getScores() {
        if (scores == null) {
            scores = new LinkedHashMap<>();
            for (String attr : DEFAULT_ATTRIBUTES) {
                scores.put(attr, 0.0);
            }
        }
        return scores;
    }

    public void setScores(Map<String, Double> scores) {
        this.scores = scores != null ? new LinkedHashMap<>(scores) : new LinkedHashMap<>();
    }

    public double getDefects() { return defects; }
    public void setDefects(double defects) { this.defects = Math.max(0, Math.min(100, defects)); }

    public String getCupNotes() { return cupNotes != null ? cupNotes : ""; }
    public void setCupNotes(String cupNotes) { this.cupNotes = cupNotes != null ? cupNotes : ""; }

    /**
     * SCA-style total: sum(scores) - defects + 36, clamped to [0, 100].
     */
    public double getTotal() {
        double sum = 0.0;
        for (Double v : getScores().values()) {
            sum += v != null ? v : 0.0;
        }
        double total = sum - defects + TOTAL_OFFSET;
        return Math.max(MIN_TOTAL, Math.min(MAX_TOTAL, total));
    }

    public void load() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        for (String attr : DEFAULT_ATTRIBUTES) {
            String key = PREFIX + "score." + attr.replace("/", "_");
            getScores().put(attr, p.getDouble(key, 0.0));
        }
        setDefects(p.getDouble(PREFIX + "defects", 0.0));
        setCupNotes(p.get(PREFIX + "notes", ""));
    }

    public void save() {
        Preferences p = Preferences.userRoot().node(PREFS_NODE);
        for (Map.Entry<String, Double> e : getScores().entrySet()) {
            String key = PREFIX + "score." + e.getKey().replace("/", "_");
            p.putDouble(key, e.getValue() != null ? e.getValue() : 0.0);
        }
        p.putDouble(PREFIX + "defects", defects);
        p.put(PREFIX + "notes", getCupNotes());
    }

    /** Map for export; all score keys and defects, notes. */
    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : getScores().entrySet()) {
            m.put(e.getKey(), String.valueOf(e.getValue() != null ? e.getValue() : 0.0));
        }
        m.put("defects", String.valueOf(defects));
        m.put("cupNotes", getCupNotes());
        return m;
    }
}
