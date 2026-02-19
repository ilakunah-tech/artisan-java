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

import javafx.scene.paint.Color;

import org.artisan.model.ColorConfig;
import org.artisan.model.EventButtonConfig;
import org.artisan.model.EventType;

/**
 * Saves and loads List&lt;EventButtonConfig&gt; (up to 4) to/from ~/.artisan/eventbuttons.json using Gson.
 */
public final class EventButtonConfigPersistence {

    private static final String DIR_NAME = ".artisan";
    private static final String FILE_NAME = "eventbuttons.json";
    private static final int MAX_BUTTONS = 4;

    private static Path eventButtonsPath() {
        String home = System.getProperty("user.home");
        return Paths.get(home, DIR_NAME, FILE_NAME);
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final TypeToken<List<EventButtonConfigDto>> LIST_TYPE = new TypeToken<List<EventButtonConfigDto>>() {};

    /**
     * Saves the list to ~/.artisan/eventbuttons.json. Truncates to MAX_BUTTONS. Creates directory if needed.
     */
    public static void save(List<EventButtonConfig> list) throws IOException {
        save(list, eventButtonsPath());
    }

    /**
     * Saves to the given path. Used for tests.
     */
    public static void save(List<EventButtonConfig> list, Path path) throws IOException {
        if (list == null || path == null) return;
        Files.createDirectories(path.getParent());
        List<EventButtonConfigDto> dtos = new ArrayList<>();
        int n = Math.min(list.size(), MAX_BUTTONS);
        for (int i = 0; i < n; i++) {
            dtos.add(EventButtonConfigDto.from(list.get(i)));
        }
        String json = GSON.toJson(dtos);
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    /**
     * Loads from ~/.artisan/eventbuttons.json. Returns empty list if file is missing or invalid.
     */
    public static List<EventButtonConfig> load() {
        return load(eventButtonsPath());
    }

    /**
     * Loads from the given path. Returns empty list if file is missing or invalid. Used for tests.
     */
    public static List<EventButtonConfig> load(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return new ArrayList<>();
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            List<EventButtonConfigDto> dtos = GSON.fromJson(json, LIST_TYPE.getType());
            if (dtos == null) return new ArrayList<>();
            List<EventButtonConfig> list = new ArrayList<>();
            for (EventButtonConfigDto dto : dtos) {
                EventButtonConfig c = dto.toConfig();
                if (c != null) list.add(c);
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static class EventButtonConfigDto {
        String label;
        String description;
        String type;
        double value;
        String color;
        boolean visible;
        String action;
        String actionParam;

        static EventButtonConfigDto from(EventButtonConfig c) {
            EventButtonConfigDto d = new EventButtonConfigDto();
            d.label = c.getLabel();
            d.description = c.getDescription();
            d.type = c.getType().name();
            d.value = c.getValue();
            d.color = toHex(c.getColor());
            d.visible = c.isVisible();
            d.action = c.getAction();
            d.actionParam = c.getActionParam();
            return d;
        }

        EventButtonConfig toConfig() {
            EventType t;
            try {
                t = EventType.valueOf(type != null ? type : "CUSTOM");
            } catch (Exception e) {
                t = EventType.CUSTOM;
            }
            Color col = color != null && !color.isBlank() ? ColorConfig.fromHex(color) : Color.GRAY;
            return new EventButtonConfig(
                    label != null ? label : "",
                    description != null ? description : "",
                    t, value, col, visible,
                    action != null ? action : "",
                    actionParam != null ? actionParam : "");
        }

        private static String toHex(Color c) {
            if (c == null) return "#808080";
            int r = (int) Math.round(c.getRed() * 255);
            int g = (int) Math.round(c.getGreen() * 255);
            int b = (int) Math.round(c.getBlue() * 255);
            return String.format("#%02x%02x%02x", r, g, b);
        }
    }
}
