package org.artisan.model.importer;

import org.artisan.model.ProfileData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Petroncini CSV roast profile importer — parity with Python artisanlib/petroncini.py.
 *
 * <p>Petroncini CSV columns: Date, Time, BT, ET, Gas (or Burner), Fan, Drum.
 */
public final class PetronciniImporter {

    private static final Logger LOG = Logger.getLogger(PetronciniImporter.class.getName());

    private PetronciniImporter() {}

    /**
     * Parse a Petroncini CSV file and return a {@link ProfileData}.
     *
     * @param file path to the .csv file
     * @return populated ProfileData
     * @throws IOException on read / parse failure
     */
    public static ProfileData importFile(Path file) throws IOException {
        ProfileData res = new ProfileData();
        res.setRoastertype("Petroncini");
        res.setSamplingInterval(1.0);

        try (InputStream is = Files.newInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) return res;
            String[] headerCols = headerLine.split("[,;]", -1);
            for (int h = 0; h < headerCols.length; h++) headerCols[h] = headerCols[h].trim();

            List<Double>  timex    = new ArrayList<>();
            List<Double>  temp1    = new ArrayList<>();
            List<Double>  temp2    = new ArrayList<>();
            List<Integer> timeindex = new ArrayList<>(List.of(-1, 0, 0, 0, 0, 0, 0, 0));
            List<Integer> specialevents      = new ArrayList<>();
            List<Integer> specialeventstype  = new ArrayList<>();
            List<Double>  specialeventsvalue = new ArrayList<>();
            List<String>  specialeventsStrings = new ArrayList<>();

            int i = 0;
            Double lastFan = null, lastGas = null, lastDrum = null;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split("[,;]", -1);
                Map<String, String> item = new HashMap<>();
                for (int k = 0; k < Math.min(headerCols.length, cols.length); k++) {
                    item.put(headerCols[k], cols[k].trim());
                }

                timex.add((double) i);
                temp1.add(parseDouble(item.get("ET"), parseDouble(item.get("Air"), -1.0)));
                temp2.add(parseDouble(item.get("BT"), parseDouble(item.get("Bean"), -1.0)));

                if (timeindex.get(0) <= -1) timeindex.set(0, i);

                // Gas/Burner event (type 3)
                String gasStr = item.getOrDefault("Gas", item.get("Burner"));
                if (gasStr != null && !gasStr.isEmpty()) {
                    try {
                        double v = Double.parseDouble(gasStr);
                        if (lastGas == null || v != lastGas) {
                            lastGas = v;
                            specialevents.add(i);
                            specialeventstype.add(3);
                            specialeventsvalue.add(v / 10.0);
                            specialeventsStrings.add(String.format("%.0f%%", v));
                        }
                    } catch (NumberFormatException ignored) {}
                }

                // Fan event (type 0)
                String fanStr = item.get("Fan");
                if (fanStr != null && !fanStr.isEmpty()) {
                    try {
                        double v = Double.parseDouble(fanStr);
                        if (lastFan == null || v != lastFan) {
                            lastFan = v;
                            specialevents.add(i);
                            specialeventstype.add(0);
                            specialeventsvalue.add(v / 10.0);
                            specialeventsStrings.add(String.format("%.0f%%", v));
                        }
                    } catch (NumberFormatException ignored) {}
                }

                // Drum event (type 1)
                String drumStr = item.get("Drum");
                if (drumStr != null && !drumStr.isEmpty()) {
                    try {
                        double v = Double.parseDouble(drumStr);
                        if (lastDrum == null || v != lastDrum) {
                            lastDrum = v;
                            specialevents.add(i);
                            specialeventstype.add(1);
                            specialeventsvalue.add(v / 10.0);
                            specialeventsStrings.add(String.format("%.0f%%", v));
                        }
                    } catch (NumberFormatException ignored) {}
                }

                i++;
            }

            if (timeindex.get(6) == 0 && !timex.isEmpty()) {
                timeindex.set(6, timex.size() - 1);
            }

            res.setMode("C");
            res.setTimex(timex);
            res.setTemp1(temp1);
            res.setTemp2(temp2);
            res.setTimeindex(timeindex);

            if (!specialevents.isEmpty()) {
                res.setSpecialevents(specialevents);
                res.setSpecialeventstype(specialeventstype);
                res.setSpecialeventsvalue(specialeventsvalue);
                res.setSpecialeventsStrings(specialeventsStrings);
            }
        }

        String stem = file.getFileName().toString();
        int dot = stem.lastIndexOf('.');
        res.setTitle(dot > 0 ? stem.substring(0, dot) : stem);
        return res;
    }

    private static double parseDouble(String s, double fallback) {
        if (s == null || s.isEmpty()) return fallback;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return fallback; }
    }
}
