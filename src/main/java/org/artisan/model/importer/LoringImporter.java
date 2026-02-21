package org.artisan.model.importer;

import org.artisan.model.ProfileData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loring CSV roast profile importer — parity with Python artisanlib/loring.py.
 *
 * <p>Loring CSV columns include: Time, RoastTimeSeconds, RoastingOnOff,
 * InletTemp (ET), BeanTemp (BT), BurnerPercent, InletAir, Stack, RoR.
 * Heater (power) events are captured; temperature mode is detected automatically.
 */
public final class LoringImporter {

    private static final Logger LOG = Logger.getLogger(LoringImporter.class.getName());

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");

    private LoringImporter() {}

    /**
     * Parse a Loring CSV file and return a {@link ProfileData}.
     *
     * @param file path to the .csv file
     * @return populated ProfileData
     * @throws IOException on read / parse failure
     */
    public static ProfileData importFile(Path file) throws IOException {
        ProfileData res = new ProfileData();
        res.setRoastertype("Loring");
        res.setRoasterheating(2); // NG

        try (InputStream is = Files.newInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) return res;
            String[] headerCols = headerLine.split(",", -1);
            for (int h = 0; h < headerCols.length; h++) headerCols[h] = headerCols[h].trim();

            List<Double>  timex   = new ArrayList<>();
            List<Double>  temp1   = new ArrayList<>();
            List<Double>  temp2   = new ArrayList<>();
            List<Double>  extra1  = new ArrayList<>(); // burner
            List<Double>  extra2  = new ArrayList<>(); // inlet air
            List<Double>  extra3  = new ArrayList<>(); // stack
            List<Double>  extra4  = new ArrayList<>(); // ror
            List<Integer> timeindex = new ArrayList<>(List.of(-1, 0, 0, 0, 0, 0, 0, 0));
            List<Integer> specialevents      = new ArrayList<>();
            List<Integer> specialeventstype  = new ArrayList<>();
            List<Double>  specialeventsvalue = new ArrayList<>();
            List<String>  specialeventsStrings = new ArrayList<>();

            Double power = null, powerLast = null;
            boolean powerEvent = false;
            LocalDateTime startDt = null;
            double samplingInterval = 6;
            double lastSecs = -1;
            int i = 0;
            String mode = "C";

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",", -1);
                Map<String, String> item = new HashMap<>();
                for (int k = 0; k < Math.min(headerCols.length, cols.length); k++) {
                    item.put(headerCols[k], cols[k].trim());
                }

                if (!item.containsKey("Time") || !item.containsKey("RoastingOnOff")
                        || item.get("RoastingOnOff").isEmpty()) continue;

                LocalDateTime dt = null;
                try {
                    dt = LocalDateTime.parse(item.get("Time"), TIME_FMT);
                } catch (DateTimeParseException e) {
                    LOG.log(Level.FINE, "Loring time parse error", e);
                    continue;
                }

                if (startDt == null) {
                    startDt = dt;
                    res.setRoastdate(dt.toLocalDate().toString());
                    res.setRoastisodate(dt.toLocalDate().toString());
                    res.setRoasttime(dt.toLocalTime().toString());
                }

                double secs = java.time.Duration.between(startDt, dt).getSeconds();
                if (lastSecs >= 0) {
                    double diff = secs - lastSecs;
                    if (diff > 0) samplingInterval = diff;
                }
                lastSecs = secs;

                timex.add(secs);
                temp1.add(parseDouble(item.get("InletTemp"), -1.0));
                temp2.add(parseDouble(item.get("BeanTemp"), -1.0));
                extra1.add(parseDouble(item.get("BurnerPercent"), -1.0));
                extra2.add(parseDouble(item.get("InletAir"), -1.0));
                extra3.add(parseDouble(item.get("Stack"), -1.0));
                extra4.add(parseDouble(item.get("RoR"), -1.0));

                if (timeindex.get(0) <= -1) timeindex.set(0, i);

                // Power / burner event (type 3)
                String burnerStr = item.get("BurnerPercent");
                if (burnerStr != null && !burnerStr.isEmpty()) {
                    try {
                        double v = Double.parseDouble(burnerStr);
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
                i++;
            }

            res.setSamplingInterval(samplingInterval);
            res.setMode(mode);
            res.setTimex(timex);
            res.setTemp1(temp1);
            res.setTemp2(temp2);
            res.setTimeindex(timeindex);

            res.setExtradevices(List.of(25, 25));
            res.setExtratimex(List.of(new ArrayList<>(timex), new ArrayList<>(timex)));
            res.setExtraname1(List.of("Burner", "Inlet"));
            res.setExtratemp1(List.of(extra1, extra2));
            res.setExtramathexpression1(List.of("", ""));
            res.setExtraname2(List.of("Stack", "RoR"));
            res.setExtratemp2(List.of(extra3, extra4));
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
