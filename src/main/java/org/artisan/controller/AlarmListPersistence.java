package org.artisan.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.artisan.model.Alarm;
import org.artisan.model.AlarmAction;
import org.artisan.model.AlarmCondition;
import org.artisan.model.AlarmList;

/**
 * Saves and loads AlarmList to/from ~/.artisan/alarms.json using Gson.
 */
public final class AlarmListPersistence {

    private static final String DIR_NAME = ".artisan";
    private static final String FILE_NAME = "alarms.json";

    private static Path alarmsPath() {
        String home = System.getProperty("user.home");
        return Paths.get(home, DIR_NAME, FILE_NAME);
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final TypeToken<List<AlarmDto>> LIST_TYPE = new TypeToken<List<AlarmDto>>() {};

    /**
     * Saves the alarm list to ~/.artisan/alarms.json. Creates directory if needed.
     */
    public static void save(AlarmList list) throws IOException {
        save(list, alarmsPath());
    }

    /**
     * Saves the alarm list to the given path. Creates directory if needed. Used for tests.
     */
    public static void save(AlarmList list, Path path) throws IOException {
        if (list == null || path == null) return;
        Files.createDirectories(path.getParent());
        List<AlarmDto> dtos = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Alarm a = list.get(i);
            dtos.add(AlarmDto.from(a));
        }
        String json = GSON.toJson(dtos);
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    /**
     * Loads alarm list from ~/.artisan/alarms.json. Returns empty list if file is missing or invalid.
     */
    public static AlarmList load() {
        return load(alarmsPath());
    }

    /**
     * Loads alarm list from the given path. Returns empty list if file is missing or invalid. Used for tests.
     */
    public static AlarmList load(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return new AlarmList();
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            List<AlarmDto> dtos = GSON.fromJson(json, LIST_TYPE.getType());
            if (dtos == null) return new AlarmList();
            AlarmList list = new AlarmList();
            for (AlarmDto dto : dtos) {
                Alarm a = dto.toAlarm();
                if (a != null) list.add(a);
            }
            return list;
        } catch (Exception e) {
            return new AlarmList();
        }
    }

    /** DTO for JSON (no triggered state; loaded alarms start re-armed). */
    private static class AlarmDto {
        boolean enabled;
        String description;
        String condition;
        double threshold;
        String action;
        String actionParam;
        boolean triggerOnce;
        int guardAlarmIndex;

        static AlarmDto from(Alarm a) {
            AlarmDto d = new AlarmDto();
            d.enabled = a.isEnabled();
            d.description = a.getDescription();
            d.condition = a.getCondition().name();
            d.threshold = a.getThreshold();
            d.action = a.getAction().name();
            d.actionParam = a.getActionParam();
            d.triggerOnce = a.isTriggerOnce();
            d.guardAlarmIndex = a.getGuardAlarmIndex();
            return d;
        }

        Alarm toAlarm() {
            AlarmCondition c;
            try {
                c = AlarmCondition.valueOf(condition);
            } catch (Exception e) {
                c = AlarmCondition.BT_RISES_ABOVE;
            }
            AlarmAction ac;
            try {
                ac = AlarmAction.valueOf(action);
            } catch (Exception e) {
                ac = AlarmAction.POPUP_MESSAGE;
            }
            return new Alarm(enabled, description != null ? description : "",
                    c, threshold, ac, actionParam != null ? actionParam : "",
                    triggerOnce, guardAlarmIndex, false);
        }
    }
}
