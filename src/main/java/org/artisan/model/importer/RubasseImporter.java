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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rubasse CSV roast profile importer — parity with Python rubasse.py.
 *
 * <p>Column order (0-based):
 * time, BT, Fan, Heater, RoR, Drum, Humidity, ET, Pressure, DT,
 * timeB, BTB, FanB, HeaterB, RoRB, DrumB, HumidityB, ETB, PressureB, DTB (+ more header cols)
 *
 * <p>Header row columns 19 and 21 carry FCs and SCs timeindex values.
 */
public final class RubasseImporter {

    private static final Logger LOG = Logger.getLogger(RubasseImporter.class.getName());

    private static final String[] HEADER = {
        "time", "BT", "Fan", "Heater", "RoR", "Drum", "Humidity", "ET", "Pressure",
        "DT", "timeB", "BTB", "FanB", "HeaterB", "RoRB", "DrumB", "HumidityB", "ETB", "PressureB", "DTB"
    };

    private static final List<String> DEFAULT_ETYPES = List.of("Air", "Drum", "TS", "Power");

    private RubasseImporter() {}

    /**
     * Parse a Rubasse CSV file and return a {@link ProfileData}.
     *
     * @param file path to the .csv file
     * @return populated ProfileData
     * @throws IOException on read / parse failure
     */
    public static ProfileData importFile(Path file) throws IOException {
        ProfileData res = new ProfileData();

        res.setSamplingInterval(1.0);
        res.setTitle(file.getFileName().toString());
        res.setRoastertype("Rubasse");
        res.setRoasterheating(3); // electric

        try (InputStream is = Files.newInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) return res;

            String[] headerRow = headerLine.split(",", -1);

            List<Double> timex  = new ArrayList<>();
            List<Double> temp1  = new ArrayList<>();
            List<Double> temp2  = new ArrayList<>();
            List<Double> extra1 = new ArrayList<>(); // Heater
            List<Double> extra2 = new ArrayList<>(); // Fan
            List<Double> extra3 = new ArrayList<>(); // Humidity
            List<Double> extra4 = new ArrayList<>(); // Pressure
            List<Double> extra5 = new ArrayList<>(); // Drum
            List<Double> extra6 = new ArrayList<>(); // DT

            List<Integer> timeindex = new ArrayList<>(List.of(-1, 0, 0, 0, 0, 0, 0, 0));
            List<Integer> specialevents      = new ArrayList<>();
            List<Integer> specialeventstype  = new ArrayList<>();
            List<Double>  specialeventsvalue = new ArrayList<>();
            List<String>  specialeventsStrings = new ArrayList<>();

            Double fan          = null;
            Double fanLast      = null;
            boolean fanEvent    = false;
            Double heater       = null;
            Double heaterLast   = null;
            boolean heaterEvent = false;

            int i = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",", -1);
                Map<String, String> item = new HashMap<>();
                for (int k = 0; k < Math.min(HEADER.length, cols.length); k++) {
                    item.put(HEADER[k], cols[k].trim());
                }

                timex.add((double) i);
                temp1.add(parseDouble(item.get("ET"), -1.0));
                temp2.add(-1.0); // BT not present in this format
                extra1.add(parseDouble(item.get("Heater"), -1.0));
                extra2.add(parseDouble(item.get("Fan"), -1.0));
                extra3.add(parseDouble(item.get("Humidity"), -1.0));
                extra4.add(parseDouble(item.get("Pressure"), -1.0));
                extra5.add(parseDouble(item.get("Drum"), -1.0));
                extra6.add(parseDouble(item.get("DT"), -1.0));

                // Fan event with fluctuation suppression
                String fanStr = item.get("Fan");
                if (fanStr != null && !fanStr.isEmpty()) {
                    try {
                        double v = Double.parseDouble(fanStr);
                        if (fan == null || v != fan) {
                            if (fanLast != null && v == fanLast) {
                                // fluctuation — remove the last fan event
                                int lastFanIdx = lastEventOfType(specialeventstype, 0);
                                if (lastFanIdx >= 0) {
                                    specialevents.remove(lastFanIdx);
                                    specialeventstype.remove(lastFanIdx);
                                    specialeventsvalue.remove(lastFanIdx);
                                    specialeventsStrings.remove(lastFanIdx);
                                }
                                fan = fanLast;
                                fanLast = null;
                            } else {
                                fanLast = fan;
                                fan = v;
                                fanEvent = true;
                                specialevents.add(i);
                                specialeventstype.add(0);
                                specialeventsvalue.add(externalToInternal((int) Math.round(v)));
                                specialeventsStrings.add(v + "%");
                            }
                        } else {
                            fanLast = null;
                        }
                    } catch (NumberFormatException e) {
                        LOG.log(Level.FINE, "Fan parse error", e);
                    }
                }

                // Heater event with fluctuation suppression
                String heaterStr = item.get("Heater");
                if (heaterStr != null && !heaterStr.isEmpty()) {
                    try {
                        double v = Double.parseDouble(heaterStr);
                        if (heater == null || v != heater) {
                            if (heaterLast != null && v == heaterLast) {
                                int lastHeaterIdx = lastEventOfType(specialeventstype, 3);
                                if (lastHeaterIdx >= 0) {
                                    specialevents.remove(lastHeaterIdx);
                                    specialeventstype.remove(lastHeaterIdx);
                                    specialeventsvalue.remove(lastHeaterIdx);
                                    specialeventsStrings.remove(lastHeaterIdx);
                                }
                                heater = heaterLast;
                                heaterLast = null;
                            } else {
                                heaterLast = heater;
                                heater = v;
                                heaterEvent = true;
                                specialevents.add(i);
                                specialeventstype.add(3);
                                specialeventsvalue.add(externalToInternal((int) Math.round(v)));
                                specialeventsStrings.add(v + "%");
                            }
                        } else {
                            heaterLast = null;
                        }
                    } catch (NumberFormatException e) {
                        LOG.log(Level.FINE, "Heater parse error", e);
                    }
                }

                i++;
            }

            // Mark CHARGE
            if (timeindex.get(0) == -1) timeindex.set(0, 0);

            // Mark FCs (header column 19)
            try {
                if (headerRow.length > 19) {
                    int fcs = Math.max(0, Integer.parseInt(headerRow[19].trim()));
                    timeindex.set(2, fcs);
                }
            } catch (Exception e) { /* ignore */ }

            // Mark SCs (header column 21)
            try {
                if (headerRow.length > 21) {
                    int scs = Math.max(0, Integer.parseInt(headerRow[21].trim()));
                    timeindex.set(4, scs);
                }
            } catch (Exception e) { /* ignore */ }

            // Mark DROP at last data point if not set
            if (timeindex.get(6) == 0) {
                timeindex.set(6, Math.max(0, timex.size() - 1));
            }

            res.setMode("C");
            res.setTimex(timex);
            res.setTemp1(temp1);
            res.setTemp2(temp2);
            res.setTimeindex(timeindex);

            // Extra devices (device 25 = "none" / virtual)
            res.setExtradevices(List.of(25, 25, 25));
            res.setExtratimex(List.of(new ArrayList<>(timex), new ArrayList<>(timex), new ArrayList<>(timex)));

            res.setExtraname1(List.of("{3}", "Moisture", "{1}"));
            res.setExtratemp1(List.of(extra1, extra3, extra5));
            res.setExtramathexpression1(List.of("", "", ""));

            res.setExtraname2(List.of("{0}", "Pressure", "DT"));
            res.setExtratemp2(List.of(extra2, extra4, extra6));
            res.setExtramathexpression2(List.of("", "", ""));

            res.setExtraCurveVisibility1(List.of(false, true, false, false, true, true, true, true, true, true));
            res.setExtraCurveVisibility2(List.of(false, false, false, false, true, true, true, true, true, true));
            res.setExtraDelta1(List.of(false, false, false));
            res.setExtraDelta2(List.of(false, false, false));
            res.setExtraNoneTempHint1(List.of(true, true, true));
            res.setExtraNoneTempHint2(List.of(true, true, true));

            if (!specialevents.isEmpty()) {
                res.setSpecialevents(specialevents);
                res.setSpecialeventstype(specialeventstype);
                res.setSpecialeventsvalue(specialeventsvalue);
                res.setSpecialeventsStrings(specialeventsStrings);
                if (heaterEvent || fanEvent) {
                    res.setEtypes(new ArrayList<>(DEFAULT_ETYPES));
                }
            }
        }

        return res;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static double parseDouble(String s, double fallback) {
        if (s == null || s.isEmpty()) return fallback;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return fallback; }
    }

    private static double externalToInternal(int pct) {
        return pct / 10.0;
    }

    /** Returns the last index in specialeventstype with the given type, or -1. */
    private static int lastEventOfType(List<Integer> types, int type) {
        for (int k = types.size() - 1; k >= 0; k--) {
            if (types.get(k) == type) return k;
        }
        return -1;
    }
}
