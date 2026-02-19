package org.artisan.model;

import java.util.List;

/**
 * Generates a summary report for a date range from BatchManager.
 */
public final class ProductionReport {

    private ProductionReport() {}

    /**
     * Filters batches by date range (ISO-8601 string comparison) and returns plain-text report
     * with date range header, per-batch rows, totals row, and batch count.
     */
    public static String generate(BatchManager bm, String fromDate, String toDate) {
        if (bm == null) return "No batch manager.\n";
        List<Batch> all = bm.getBatches();
        List<Batch> filtered = all.stream()
                .filter(b -> inRange(b.getDate(), fromDate, toDate))
                .sorted((a, c) -> {
                    int cmp = (a.getDate() != null ? a.getDate() : "").compareTo(c.getDate() != null ? c.getDate() : "");
                    if (cmp != 0) return cmp;
                    return Integer.compare(a.getBatchNumber(), c.getBatchNumber());
                })
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("=== PRODUCTION REPORT ===\n");
        sb.append(String.format("From: %s  To: %s%n", fromDate != null ? fromDate : "", toDate != null ? toDate : ""));
        sb.append("\n");

        if (filtered.isEmpty()) {
            sb.append("No batches in date range.\n");
            return sb.toString();
        }

        sb.append(String.format("%-4s %-20s %-12s %8s %8s %8s%n",
                "#", "Title", "Date", "Green(g)", "Roasted(g)", "Loss%"));
        sb.append("--------------------------------------------------------------------------------\n");

        double totalGreenG = 0.0;
        double totalRoastedG = 0.0;
        double sumLossPct = 0.0;
        int count = 0;

        for (Batch b : filtered) {
            double loss = b.weightLossPercent();
            sb.append(String.format("%-4d %-20s %-12s %8.0f %8.0f %7.1f%%%n",
                    b.getBatchNumber(),
                    truncate(b.getTitle(), 20),
                    b.getDate() != null ? b.getDate() : "",
                    b.getGreenWeight(),
                    b.getRoastedWeight(),
                    loss));
            totalGreenG += b.getGreenWeight();
            totalRoastedG += b.getRoastedWeight();
            sumLossPct += loss;
            count++;
        }

        sb.append("--------------------------------------------------------------------------------\n");
        double avgLoss = count > 0 ? sumLossPct / count : 0.0;
        sb.append(String.format("%-4s %-20s %-12s %8.0f %8.0f %7.1f%%%n",
                "", "Total", "",
                totalGreenG, totalRoastedG, avgLoss));
        sb.append(String.format("%nBatch count: %d%n", count));
        return sb.toString();
    }

    private static boolean inRange(String date, String from, String to) {
        if (date == null || date.isBlank()) return false;
        if (from != null && !from.isBlank() && date.compareTo(from) < 0) return false;
        if (to != null && !to.isBlank() && date.compareTo(to) > 0) return false;
        return true;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }
}
