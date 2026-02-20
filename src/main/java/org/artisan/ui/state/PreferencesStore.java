package org.artisan.ui.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists UI preferences to a JSON file in user home: ~/.artisan-java/ui-preferences.json
 * Schema versioning with defaults for missing keys.
 */
public final class PreferencesStore {

    private static final String DIR_NAME = ".artisan-java";
    private static final String FILE_NAME = "ui-preferences.json";

    private final Path path;
    private final ObjectMapper mapper = new ObjectMapper();

    public PreferencesStore() {
        String home = System.getProperty("user.home");
        path = Paths.get(home, DIR_NAME, FILE_NAME);
    }

    public Path getPath() {
        return path;
    }

    /** Loads preferences; returns defaults if file missing or invalid. Schema versioning: unknown keys ignored; future versions get defaults for new fields. */
    public UIPreferences load() {
        UIPreferences prefs = new UIPreferences();
        if (!Files.isRegularFile(path)) {
            return prefs;
        }
        try {
            String json = Files.readString(path);
            JsonNode root = mapper.readTree(json);
            if (root == null || !root.isObject()) return prefs;

            ObjectNode obj = (ObjectNode) root;
            int fileVersion = obj.path("schemaVersion").asInt(UIPreferences.SCHEMA_VERSION);
            prefs.setSchemaVersion(Math.min(fileVersion, UIPreferences.SCHEMA_VERSION));
            prefs.setTheme(obj.path("theme").asText("light"));
            String dens = obj.path("density").asText("COMFORTABLE");
            try {
                prefs.setDensity(UIPreferences.Density.valueOf(dens));
            } catch (Exception e) {
                prefs.setDensity(UIPreferences.Density.COMFORTABLE);
            }
            String readout = obj.path("readoutSize").asText("M");
            try {
                prefs.setReadoutSize(UIPreferences.ReadoutSize.valueOf(readout));
            } catch (Exception e) {
                prefs.setReadoutSize(UIPreferences.ReadoutSize.M);
            }
            prefs.setVisibleBT(obj.path("visibleBT").asBoolean(true));
            prefs.setVisibleET(obj.path("visibleET").asBoolean(true));
            prefs.setVisibleDeltaBT(obj.path("visibleDeltaBT").asBoolean(true));
            prefs.setVisibleDeltaET(obj.path("visibleDeltaET").asBoolean(true));
            prefs.setMainDividerPosition(obj.path("mainDividerPosition").asDouble(0.75));

            LayoutState layout = new LayoutState();
            JsonNode layoutNode = obj.get("layout");
            if (layoutNode != null && layoutNode.isObject()) {
                ObjectNode layoutObj = (ObjectNode) layoutNode;
                layout.setDockWidth(layoutObj.path("dockWidth").asDouble(LayoutState.DEFAULT_DOCK_WIDTH));
                layout.setControlsVisible(layoutObj.path("controlsVisible").asBoolean(true));
                JsonNode orderNode = layoutObj.get("panelOrder");
                if (orderNode != null && orderNode.isArray()) {
                    List<String> order = new ArrayList<>();
                    for (JsonNode n : orderNode) {
                        if (n.isTextual()) order.add(n.asText());
                    }
                    if (!order.isEmpty()) layout.setPanelOrder(order);
                }
                JsonNode collapsedNode = layoutObj.get("collapsed");
                if (collapsedNode != null && collapsedNode.isObject()) {
                    collapsedNode.fields().forEachRemaining(e ->
                        layout.setPanelCollapsed(e.getKey(), e.getValue().asBoolean(false)));
                }
                JsonNode detachedNode = layoutObj.get("detached");
                if (detachedNode != null && detachedNode.isObject()) {
                    detachedNode.fields().forEachRemaining(e ->
                        layout.setPanelDetached(e.getKey(), e.getValue().asBoolean(false)));
                }
                JsonNode boundsNode = layoutObj.get("detachedBounds");
                if (boundsNode != null && boundsNode.isObject()) {
                    boundsNode.fields().forEachRemaining(e -> {
                        JsonNode b = e.getValue();
                        if (b.isObject()) {
                            LayoutState.WindowBounds wb = new LayoutState.WindowBounds(
                                b.path("x").asDouble(100),
                                b.path("y").asDouble(100),
                                b.path("width").asDouble(300),
                                b.path("height").asDouble(250));
                            layout.setDetachedBounds(e.getKey(), wb);
                        }
                    });
                }
            }
            prefs.setLayoutState(layout);

            JsonNode shortcutsNode = obj.get("shortcuts");
            if (shortcutsNode != null && shortcutsNode.isObject()) {
                Map<String, String> shortcuts = new LinkedHashMap<>(UIPreferences.DEFAULT_SHORTCUTS);
                shortcutsNode.fields().forEachRemaining(e -> {
                    if (e.getValue().isTextual()) shortcuts.put(e.getKey(), e.getValue().asText());
                });
                prefs.setShortcuts(shortcuts);
            }
        } catch (IOException e) {
            // return defaults
        }
        return prefs;
    }

    /** Saves preferences; creates parent directory if needed. */
    public void save(UIPreferences prefs) {
        if (prefs == null) return;
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);

            ObjectNode root = mapper.createObjectNode();
            root.put("schemaVersion", prefs.getSchemaVersion());
            root.put("theme", prefs.getTheme());
            root.put("density", prefs.getDensity().name());
            root.put("readoutSize", prefs.getReadoutSize().name());
            root.put("visibleBT", prefs.isVisibleBT());
            root.put("visibleET", prefs.isVisibleET());
            root.put("visibleDeltaBT", prefs.isVisibleDeltaBT());
            root.put("visibleDeltaET", prefs.isVisibleDeltaET());
            root.put("mainDividerPosition", prefs.getMainDividerPosition());

            LayoutState layout = prefs.getLayoutState();
            ObjectNode layoutObj = mapper.createObjectNode();
            layoutObj.put("dockWidth", layout.getDockWidth());
            layoutObj.put("controlsVisible", layout.isControlsVisible());
            ArrayNode orderArr = layoutObj.putArray("panelOrder");
            for (String id : layout.getPanelOrder()) orderArr.add(id);
            ObjectNode collapsedObj = layoutObj.putObject("collapsed");
            for (String id : layout.getPanelOrder()) {
                collapsedObj.put(id, layout.isPanelCollapsed(id));
            }
            ObjectNode detachedObj = layoutObj.putObject("detached");
            for (String id : layout.getPanelOrder()) {
                detachedObj.put(id, layout.isPanelDetached(id));
            }
            ObjectNode boundsObj = layoutObj.putObject("detachedBounds");
            for (String id : layout.getPanelOrder()) {
                LayoutState.WindowBounds wb = layout.getDetachedBounds(id);
                if (wb != null) {
                    ObjectNode b = boundsObj.putObject(id);
                    b.put("x", wb.x);
                    b.put("y", wb.y);
                    b.put("width", wb.width);
                    b.put("height", wb.height);
                }
            }
            root.set("layout", layoutObj);

            ObjectNode shortcutsObj = root.putObject("shortcuts");
            for (Map.Entry<String, String> e : prefs.getShortcuts().entrySet()) {
                shortcutsObj.put(e.getKey(), e.getValue());
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
        } catch (IOException e) {
            // log and ignore
        }
    }

    /** Reset layout to defaults (panel order, not collapsed, not detached, default width). */
    public void resetLayout(UIPreferences prefs) {
        if (prefs == null) return;
        prefs.setLayoutState(new LayoutState());
        prefs.setMainDividerPosition(0.75);
    }
}
