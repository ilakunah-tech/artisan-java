package org.artisan.util;

import org.artisan.model.ProfileData;
import org.artisan.model.RoastProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Import/Export Cropster CSV format.
 */
public final class CropsterConverter {

    private static final Logger LOG = Logger.getLogger(CropsterConverter.class.getName());

    private CropsterConverter() {}

    public static void exportToCropster(ProfileData pd, RoastProperties props, Path outputPath) throws IOException {
        if (pd == null || outputPath == null) return;
        List<Double> timex = pd.getTimex();
        List<Double> bt = pd.getTemp2();
        List<Double> et = pd.getTemp1();
        List<Double> rorBt = pd.getDelta2();
        int n = timex != null ? timex.size() : 0;
        if (n <= 0) return;

        try (BufferedWriter w = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            w.write("Time,BT,ET,ROR_BT,Gas,Inlet");
            w.newLine();
            for (int i = 0; i < n; i++) {
                double t = timex.get(i) != null ? timex.get(i) : 0.0;
                String timeStr = formatTimeMMSS(t);
                double btV = (bt != null && i < bt.size() && bt.get(i) != null) ? bt.get(i) : 0.0;
                double etV = (et != null && i < et.size() && et.get(i) != null) ? et.get(i) : 0.0;
                double ror = (rorBt != null && i < rorBt.size() && rorBt.get(i) != null) ? rorBt.get(i) : 0.0;
                w.write(String.format(Locale.US, "%s,%.1f,%.1f,%.1f,%.1f,%.1f",
                    timeStr, btV, etV, ror, 0.0, 0.0));
                w.newLine();
            }
        }
    }

    public static ProfileData importFromCropster(Path inputPath) {
        if (inputPath == null) return null;
        if (!Files.isRegularFile(inputPath)) return null;

        try (BufferedReader r = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            String header = r.readLine();
            if (header == null || header.isBlank()) return null;

            String[] cols = splitCsvLine(header);
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < cols.length; i++) {
                idx.put(normalize(cols[i]), i);
            }
            Integer timeIdx = idx.get("TIME");
            Integer btIdx = idx.get("BT");
            if (timeIdx == null || btIdx == null) return null;
            Integer etIdx = idx.get("ET");
            Integer rorIdx = idx.get("ROR_BT");

            List<Double> timex = new ArrayList<>();
            List<Double> bt = new ArrayList<>();
            List<Double> et = etIdx != null ? new ArrayList<>() : null;
            List<Double> delta2 = rorIdx != null ? new ArrayList<>() : null;

            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] row = splitCsvLine(line);
                if (timeIdx >= row.length || btIdx >= row.length) continue;
                Double t = parseTimeSeconds(row[timeIdx]);
                if (t == null) { continue; }
                Double btV = parseDouble(row[btIdx]);
                if (btV == null) { continue; }
                timex.add(t);
                bt.add(btV);
                if (et != null) {
                    Double etV = etIdx < row.length ? parseDouble(row[etIdx]) : null;
                    et.add(etV != null ? etV : 0.0);
                }
                if (delta2 != null) {
                    Double d = rorIdx < row.length ? parseDouble(row[rorIdx]) : null;
                    delta2.add(d != null ? d : 0.0);
                }
            }

            if (timex.isEmpty()) return null;
            ProfileData pd = new ProfileData();
            pd.setTimex(timex);
            pd.setTemp2(bt);
            if (et != null) pd.setTemp1(et);
            if (delta2 != null) pd.setDelta2(delta2);
            return pd;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Cropster import failed: {0}", ex.getMessage());
            return null;
        }
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().replace(" ", "_").toUpperCase(Locale.ROOT);
    }

    private static String[] splitCsvLine(String line) {
        // Minimal CSV support: this project’s exports are unquoted. Cropster exports are typically simple too.
        return line != null ? line.split("\\s*,\\s*", -1) : new String[0];
    }

    private static String formatTimeMMSS(double seconds) {
        int total = (int) Math.round(seconds);
        if (total < 0) total = 0;
        int mm = total / 60;
        int ss = total % 60;
        return String.format(Locale.US, "%02d:%02d", mm, ss);
    }

    private static Double parseTimeSeconds(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        try {
            if (v.contains(":")) {
                String[] parts = v.split(":");
                if (parts.length != 2) return null;
                double mm = Double.parseDouble(parts[0].trim());
                double ss = Double.parseDouble(parts[1].trim());
                return mm * 60.0 + ss;
            }
            return Double.parseDouble(v);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Double parseDouble(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

