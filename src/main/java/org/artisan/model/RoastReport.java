package org.artisan.model;

import java.util.List;
import java.util.Map;

/**
 * Generates a human-readable plain-text report for a single roast (current session).
 */
public final class RoastReport {

    private RoastReport() {}

    /**
     * Generates a plain-text formatted report with sections: ROAST INFO, PROFILE, PHASES, CUP PROFILE, STATISTICS.
     */
    public static String generate(
            ProfileData pd,
            RoastProperties props,
            CupProfile cup,
            PhaseResult phases,
            RoastStats stats) {
        StringBuilder sb = new StringBuilder();

        // ROAST INFO
        sb.append("=== ROAST INFO ===\n");
        sb.append(String.format("  %-12s %s%n", "Title:", nullToEmpty(props.getTitle())));
        sb.append(String.format("  %-12s %s%n", "Date:", nullToEmpty(props.getRoastDate())));
        sb.append(String.format("  %-12s %s%n", "Operator:", nullToEmpty(props.getOperator())));
        sb.append(String.format("  %-12s %s%n", "Origin:", nullToEmpty(props.getBeanOrigin())));
        sb.append(String.format("  %-12s %s%n", "Variety:", nullToEmpty(props.getBeanVariety())));
        sb.append(String.format("  %-12s %.1f g%n", "Green weight:", props.getGreenWeight()));
        sb.append(String.format("  %-12s %.1f g%n", "Roasted weight:", props.getRoastedWeight()));
        sb.append(String.format("  %-12s %d%n", "Color:", props.getRoastColor()));
        sb.append("\n");

        // PROFILE
        sb.append("=== PROFILE ===\n");
        double totalTime = stats != null && !stats.isEmpty() ? stats.getTotalTimeSec() : 0.0;
        sb.append(String.format("  %-12s %.1f s%n", "Total time:", totalTime));
        double chargeTemp = tempAtIndex(pd, 0);
        double dropTemp = getDropTemp(pd);
        if (Double.isFinite(chargeTemp)) sb.append(String.format("  %-12s %.1f °C%n", "Charge temp:", chargeTemp));
        if (Double.isFinite(dropTemp)) sb.append(String.format("  %-12s %.1f °C%n", "Drop temp:", dropTemp));
        double rorPeak = rorPeakFromProfile(pd);
        if (Double.isFinite(rorPeak)) sb.append(String.format("  %-12s %.1f °C/min%n", "RoR peak:", rorPeak));
        sb.append("\n");

        // PHASES
        sb.append("=== PHASES ===\n");
        if (phases != null && !phases.isInvalid()) {
            sb.append(String.format("  %-12s %.1f%%%n", "Drying:", phases.getDryingPercent()));
            sb.append(String.format("  %-12s %.1f%%%n", "Maillard:", phases.getMaillardPercent()));
            sb.append(String.format("  %-12s %.1f%%%n", "Development:", phases.getDevelopmentPercent()));
            double dtrPct = phases.getTotalTimeSec() > 0
                    ? (phases.getDevelopmentTimeSec() / phases.getTotalTimeSec()) * 100.0
                    : 0.0;
            sb.append(String.format("  %-12s %.1f%%%n", "DTR:", dtrPct));
        } else {
            sb.append("  (invalid or no phase data)\n");
        }
        sb.append("\n");

        // CUP PROFILE
        sb.append("=== CUP PROFILE ===\n");
        if (cup != null) {
            for (Map.Entry<String, Double> e : cup.getScores().entrySet()) {
                sb.append(String.format("  %-20s %.1f%n", e.getKey() + ":", e.getValue() != null ? e.getValue() : 0.0));
            }
            sb.append(String.format("  %-20s %.1f%n", "Total SCA score:", cup.getTotal()));
        } else {
            sb.append("  (no cup profile)\n");
        }
        sb.append("\n");

        // STATISTICS
        sb.append("=== STATISTICS ===\n");
        if (stats != null && !stats.isEmpty()) {
            sb.append(String.format("  %-12s %.1f °C%n", "Mean BT:", stats.getMeanBt()));
            sb.append(String.format("  %-12s %.1f °C%n", "Mean ET:", stats.getMeanEt()));
            sb.append(String.format("  %-12s %.1f °C·s%n", "AUC:",
                    Calculator.computeAUC(pd != null ? pd.getTimex() : null,
                            pd != null ? pd.getTemp2() : null,
                            100.0, 0.0, stats.getTotalTimeSec())));
        } else {
            sb.append("  (no statistics)\n");
        }

        return sb.toString();
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static double getDropTemp(ProfileData pd) {
        double fromIndex = tempAtIndex(pd, 6);
        if (Double.isFinite(fromIndex)) return fromIndex;
        if (pd == null) return Double.NaN;
        List<Double> temp2 = pd.getTemp2();
        if (temp2 == null || temp2.isEmpty()) return Double.NaN;
        Double last = temp2.get(temp2.size() - 1);
        return last != null ? last : Double.NaN;
    }

    private static double tempAtIndex(ProfileData pd, int timeindexSlot) {
        if (pd == null) return Double.NaN;
        List<Integer> ti = pd.getTimeindex();
        List<Double> temp2 = pd.getTemp2();
        if (ti == null || ti.size() <= timeindexSlot || temp2 == null) return Double.NaN;
        int idx = ti.get(timeindexSlot);
        if (idx < 0 || idx >= temp2.size()) return Double.NaN;
        return temp2.get(idx);
    }

    private static double rorPeakFromProfile(ProfileData pd) {
        if (pd == null) return Double.NaN;
        List<Double> delta2 = pd.getDelta2();
        if (delta2 == null || delta2.isEmpty()) return Double.NaN;
        double max = Double.NEGATIVE_INFINITY;
        for (Double d : delta2) {
            if (d != null && Double.isFinite(d) && d > max) max = d;
        }
        return max == Double.NEGATIVE_INFINITY ? Double.NaN : max;
    }
}
