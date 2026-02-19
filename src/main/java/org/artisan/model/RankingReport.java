package org.artisan.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generates a ranking table comparing multiple batches by quality score (roast color / weight loss).
 */
public final class RankingReport {

    private RankingReport() {}

    /**
     * Returns plain-text report: header, ranked table (Rank | # | Title | Date | Loss% | Color), footer.
     * Sorted by roastColor descending (proxy for quality); if roastColor == 0, by weightLossPercent ascending.
     */
    public static String generate(List<Batch> batches, BatchManager bm) {
        if (batches == null) batches = List.of();
        List<Batch> sorted = new ArrayList<>(batches);
        sorted.sort(
                Comparator.comparingInt(Batch::getRoastColor).reversed()
                        .thenComparing(Comparator.comparingDouble(Batch::weightLossPercent)));

        StringBuilder sb = new StringBuilder();
        sb.append("=== RANKING REPORT ===\n\n");
        sb.append(String.format("%-5s %-4s %-20s %-12s %8s %6s%n", "Rank", "#", "Title", "Date", "Loss%", "Color"));
        sb.append("--------------------------------------------------------------------------------\n");

        int rank = 1;
        for (Batch b : sorted) {
            sb.append(String.format("%-5d %-4d %-20s %-12s %7.1f%% %6d%n",
                    rank++,
                    b.getBatchNumber(),
                    truncate(b.getTitle(), 20),
                    b.getDate() != null ? b.getDate() : "",
                    b.weightLossPercent(),
                    b.getRoastColor()));
        }

        sb.append("\nTotal batches: ").append(sorted.size()).append("\n");
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }
}
