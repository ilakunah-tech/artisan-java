package org.artisan.model.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.artisan.model.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * HiBean JSON roast profile importer — parity with Python hibean.py.
 *
 * <p>Maps HiBean dataList → Artisan timex/temp1/temp2/timeindex and
 * extracts fan (FC), heat (HP), drum (RC) and steam (TS) events
 * as specialevents.
 */
public final class HiBeanImporter {

    private static final Logger LOG = Logger.getLogger(HiBeanImporter.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * HiBean event number → Artisan timeindex slot.
     * Index is the HiBean event number; value is the Artisan timeindex slot,
     * or -1 if there is no correspondence.
     * Mapping from Python: [None, 0, None, 1, 2, 3, 4, 5, 6]
     */
    private static final int[] EVENT2TIMEINDEX = {-1, 0, -1, 1, 2, 3, 4, 5, 6};

    private static final List<String> DEFAULT_ETYPES = List.of("Air", "Drum", "TS", "Power");

    private HiBeanImporter() {}

    /**
     * Parse a HiBean JSON file and return a {@link ProfileData}.
     *
     * @param file path to the .json file
     * @return populated ProfileData
     * @throws IOException on read / parse failure
     */
    public static ProfileData importFile(Path file) throws IOException {
        ProfileData res = new ProfileData();

        try (InputStream in = Files.newInputStream(file)) {
            JsonNode root = MAPPER.readTree(in);

            res.setSamplingInterval(root.path("sampleInterval").asDouble(1.0));
            String unit = root.path("temperatureUnit").asText("C");
            res.setMode(unit);

            // ── Date / time ─────────────────────────────────────────────────
            String dateTimeStr = root.path("dateTime").asText(null);
            if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse(dateTimeStr,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    Instant instant = ldt.toInstant(ZoneOffset.UTC);
                    res.setRoastepoch(instant.getEpochSecond());
                    res.setRoastdate(ldt.toLocalDate().toString());
                    res.setRoastisodate(ldt.toLocalDate().format(DateTimeFormatter.ISO_DATE));
                    res.setRoasttime(ldt.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
                } catch (Exception e) {
                    LOG.warning("HiBean: could not parse dateTime: " + dateTimeStr);
                }
            }

            res.setRoastingnotes(root.path("notes").asText(""));

            // ── Roast context ────────────────────────────────────────────────
            JsonNode ctx = root.path("roastContext");
            if (!ctx.isMissingNode()) {
                res.setTitle(ctx.path("name").asText(""));

                JsonNode gw = ctx.path("greenBeanWeight");
                JsonNode rw = ctx.path("roastedBeanWeight");
                List<Object> weight = new ArrayList<>();
                weight.add(gw.isMissingNode() ? 0.0 : gw.path("value").asDouble(0.0));
                weight.add(rw.isMissingNode() ? 0.0 : rw.path("value").asDouble(0.0));
                String wu = gw.isMissingNode() ? "g" : gw.path("unit").asText("g").toLowerCase();
                if ("lbs".equals(wu)) wu = "lb";
                weight.add(wu);
                res.setWeight(weight);

                JsonNode envTemp = ctx.path("envTemp");
                res.setAmbientTemp(envTemp.isMissingNode() ? 0.0 : envTemp.path("value").asDouble(0.0));
                res.setAmbientHumidity(ctx.path("envHumidity").asDouble(0.0));
                res.setAmbientPressure(ctx.path("pressure").asDouble(0.0));
            }

            // ── Device info ──────────────────────────────────────────────────
            JsonNode devInfo = root.path("deviceInfo");
            if (!devInfo.isMissingNode()) {
                res.setRoastertype(devInfo.path("name").asText(""));
            }

            // ── Curve data ───────────────────────────────────────────────────
            List<Double> timex = new ArrayList<>();
            List<Double> temp1 = new ArrayList<>();
            List<Double> temp2 = new ArrayList<>();
            List<Integer> timeindex = new ArrayList<>(List.of(-1, 0, 0, 0, 0, 0, 0, 0));
            List<Integer> specialevents = new ArrayList<>();
            List<Integer> specialeventstype = new ArrayList<>();
            List<Double> specialeventsvalue = new ArrayList<>();
            List<String> specialeventsStrings = new ArrayList<>();

            Integer lastFan = null;
            Integer lastHeat = null;
            Integer lastDrum = null;
            Integer lastTs = null;

            JsonNode dataList = root.path("dataList");
            for (int idx = 0; idx < dataList.size(); idx++) {
                JsonNode dp = dataList.get(idx);
                timex.add(dp.path("duration").asDouble(0.0));
                temp1.add(dp.path("et").asDouble(0.0));
                temp2.add(dp.path("bt").asDouble(0.0));

                // timeindex events
                if (dp.has("event")) {
                    try {
                        int evNum = dp.path("event").asInt();
                        if (evNum >= 0 && evNum < EVENT2TIMEINDEX.length) {
                            int slot = EVENT2TIMEINDEX[evNum];
                            if (slot >= 0) {
                                timeindex.set(slot, idx);
                            }
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }

                // roaster param events
                JsonNode params = dp.path("roasterParams");
                if (params.isArray()) {
                    Integer fan = extractIntParam(params, "FC");
                    Integer heat = extractIntParam(params, "HP");
                    Integer drum = extractIntParam(params, "RC");
                    Integer ts = extractIntParam(params, "TS");

                    if (fan != null && !fan.equals(lastFan)) {
                        lastFan = fan;
                        specialevents.add(idx);
                        specialeventstype.add(0);
                        specialeventsvalue.add(externalToInternal(fan));
                        specialeventsStrings.add(fan + "%");
                    }
                    if (heat != null && !heat.equals(lastHeat)) {
                        lastHeat = heat;
                        specialevents.add(idx);
                        specialeventstype.add(3);
                        specialeventsvalue.add(externalToInternal(heat));
                        specialeventsStrings.add(heat + "%");
                    }
                    if (drum != null && !drum.equals(lastDrum)) {
                        lastDrum = drum;
                        specialevents.add(idx);
                        specialeventstype.add(1);
                        specialeventsvalue.add(externalToInternal(drum));
                        specialeventsStrings.add(drum + "%");
                    }
                    if (ts != null && !ts.equals(lastTs)) {
                        lastTs = ts;
                        specialevents.add(idx);
                        specialeventstype.add(2);
                        specialeventsvalue.add(externalToInternal(ts));
                        specialeventsStrings.add(String.valueOf(ts));
                    }
                }
            }

            res.setTimex(timex);
            res.setTemp1(temp1);
            res.setTemp2(temp2);
            res.setTimeindex(timeindex);

            if (!specialevents.isEmpty()) {
                res.setSpecialevents(specialevents);
                res.setSpecialeventstype(specialeventstype);
                res.setSpecialeventsvalue(specialeventsvalue);
                res.setSpecialeventsStrings(specialeventsStrings);
                res.setEtypes(new ArrayList<>(DEFAULT_ETYPES));
            }
        }

        return res;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Integer extractIntParam(JsonNode params, String key) {
        for (JsonNode p : params) {
            if (key.equals(p.path("key").asText(null))) {
                try {
                    return (int) Math.round(p.path("value").asDouble());
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    /** Converts a 0-100 percentage to Artisan internal event value (0.0-10.0 range). */
    private static double externalToInternal(int pct) {
        return pct / 10.0;
    }
}
