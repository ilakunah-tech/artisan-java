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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Giesen CSV roast profile importer — parity with Python artisanlib/giesen.py.
 *
 * <p>Columns: air (ET), beans (BT), ror, power, speed, pressure.
 * Power (type 3) and speed (type 1) changes are captured as special events
 * with fluctuation suppression.
 */
public final class GiesenImporter {

    private static final Logger LOG = Logger.getLogger(GiesenImporter.class.getName());

    private GiesenImporter() {}

    /**
     * Parse a Giesen CSV file and return a {@link ProfileData}.
     *
     * @param file path to the .csv file
     * @return populated ProfileData
     * @throws IOException on read / parse failure
     */
    public static ProfileData importFile(Path file) throws IOException {
        ProfileData res = new ProfileData();
        res.setSamplingInterval(1.0);

        try (InputStream is = Files.newInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) return res;
            String[] headerCols = headerLine.split(",", -1);
            for (int h = 0; h < headerCols.length; h++) headerCols[h] = headerCols[h].trim();

            List<Double> timex  = new ArrayList<>();
            List<Double> temp1  = new ArrayList<>();
            List<Double> temp2  = new ArrayList<>();
            List<Double> extra1 = new ArrayList<>(); // ror
            List<Double> extra2 = new ArrayList<>(); // power
            List<Double> extra3 = new ArrayList<>(); // speed
            List<Double> extra4 = new ArrayList<>(); // pressure
            List<Integer> timeindex = new ArrayList<>(List.of(-1, 0, 0, 0, 0, 0, 0, 0));
            List<Integer> specialevents      = new ArrayList<>();
            List<Integer> specialeventstype  = new ArrayList<>();
            List<Double>  specialeventsvalue = new ArrayList<>();
            List<String>  specialeventsStrings = new ArrayList<>();

            Double speed = null, speedLast = null, power = null, powerLast = null;
            boolean speedEvent = false, powerEvent = false;
            int i = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",", -1);
                Map<String, String> item = new HashMap<>();
                for (int k = 0; k < Math.min(headerCols.length, cols.length); k++) {
                    item.put(headerCols[k], cols[k].trim());
                }

                i++;
                timex.add((double) i);
                temp1.add(parseDouble(item.get("air"), -1.0));
                temp2.add(parseDouble(item.get("beans"), -1.0));
                extra1.add(parseDouble(item.get("ror"), -1.0));
                extra2.add(parseDouble(item.get("power"), -1.0));
                extra3.add(parseDouble(item.get("speed"), -1.0));
                extra4.add(parseDouble(item.get("pressure"), -1.0));

                if (timeindex.get(0) <= -1) timeindex.set(0, i);

                // Speed event (drum, type 1)
                String speedStr = item.get("speed");
                if (speedStr != null && !speedStr.isEmpty()) {
                    try {
                        double v = Double.parseDouble(speedStr);
                        if (speed == null || v != speed) {
                            if (speedLast != null && v == speedLast) {
                                int idx = lastEventOfType(specialeventstype, 1);
                                if (idx >= 0) removeAt(specialevents, specialeventstype, specialeventsvalue, specialeventsStrings, idx);
                                speed = speedLast;
                                speedLast = null;
                            } else {
                                speedLast = speed;
                                speed = v;
                                speedEvent = true;
                                specialevents.add(i);
                                specialeventstype.add(1);
                                specialeventsvalue.add(v / 10.0);
                                specialeventsStrings.add(String.format("%.1f%%", v));
                            }
                        } else {
                            speedLast = null;
                        }
                    } catch (NumberFormatException e) { LOG.log(Level.FINE, "speed parse", e); }
                }

                // Power event (heat, type 3)
                String powerStr = item.get("power");
                if (powerStr != null && !powerStr.isEmpty()) {
                    try {
                        double v = Double.parseDouble(powerStr);
                        if (power == null || v != power) {
                            if (powerLast != null && v == powerLast) {
                                int idx = lastEventOfType(specialeventstype, 3);
                                if (idx >= 0) removeAt(specialevents, specialeventstype, specialeventsvalue, specialeventsStrings, idx);
                                power = powerLast;
                                powerLast = null;
                            } else {
                                powerLast = power;
                                power = v;
                                powerEvent = true;
                                specialevents.add(i);
                                specialeventstype.add(3);
                                specialeventsvalue.add(v / 10.0);
                                specialeventsStrings.add(String.format("%.0f%%", v));
                            }
                        } else {
                            powerLast = null;
                        }
                    } catch (NumberFormatException e) { LOG.log(Level.FINE, "power parse", e); }
                }
            }

            res.setTimex(timex);
            res.setTemp1(temp1);
            res.setTemp2(temp2);
            res.setTimeindex(timeindex);

            res.setExtradevices(List.of(25, 25));
            res.setExtratimex(List.of(new ArrayList<>(timex), new ArrayList<>(timex)));
            res.setExtraname1(List.of("ror", "speed"));
            res.setExtratemp1(List.of(extra1, extra3));
            res.setExtramathexpression1(List.of("", ""));
            res.setExtraname2(List.of("power", "pressure"));
            res.setExtratemp2(List.of(extra2, extra4));
            res.setExtramathexpression2(List.of("", ""));

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

    private static int lastEventOfType(List<Integer> types, int type) {
        for (int k = types.size() - 1; k >= 0; k--) {
            if (types.get(k) == type) return k;
        }
        return -1;
    }

    private static void removeAt(List<Integer> ev, List<Integer> et, List<Double> evv, List<String> evs, int idx) {
        ev.remove(idx); et.remove(idx); evv.remove(idx); evs.remove(idx);
    }
}
