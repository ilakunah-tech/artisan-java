package org.artisan.ui.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PreferencesStore: load defaults, save and reload, reset layout.
 */
class PreferencesStoreTest {

    @Test
    void load_returnsValidPreferences() {
        PreferencesStore store = new PreferencesStore();
        UIPreferences prefs = store.load();
        assertNotNull(prefs);
        assertTrue(prefs.getSchemaVersion() >= 1);
        assertNotNull(prefs.getTheme());
        assertNotNull(prefs.getDensity());
        assertNotNull(prefs.getLayoutState());
        assertFalse(prefs.getLayoutState().getPanelOrder().isEmpty());
    }

    @Test
    void saveAndLoad_roundTrips(@TempDir Path dir) throws Exception {
        Path fakeHome = dir.resolve("home");
        Files.createDirectories(fakeHome);
        String prev = System.getProperty("user.home");
        try {
            System.setProperty("user.home", fakeHome.toString());
            PreferencesStore store = new PreferencesStore();
            UIPreferences prefs = new UIPreferences();
            prefs.setTheme("light");
            prefs.setDensity(UIPreferences.Density.COMPACT);
            prefs.setVisibleBT(false);
            prefs.getLayoutState().setDockWidth(300);
            store.save(prefs);
            assertTrue(Files.isRegularFile(store.getPath()));
            UIPreferences loaded = store.load();
            assertEquals("light", loaded.getTheme());
            assertEquals(UIPreferences.Density.COMPACT, loaded.getDensity());
            assertFalse(loaded.isVisibleBT());
            assertEquals(300.0, loaded.getLayoutState().getDockWidth());
        } finally {
            if (prev != null) System.setProperty("user.home", prev);
        }
    }

    @Test
    void resetLayout_clearsLayoutState() {
        UIPreferences prefs = new UIPreferences();
        prefs.getLayoutState().setDockWidth(400);
        prefs.getLayoutState().setPanelCollapsed(LayoutState.PANEL_LEGEND, true);
        PreferencesStore store = new PreferencesStore();
        store.resetLayout(prefs);
        LayoutState layout = prefs.getLayoutState();
        assertEquals(LayoutState.DEFAULT_DOCK_WIDTH, layout.getDockWidth());
    }

    @Test
    void shortcuts_roundTrip(@TempDir Path dir) throws Exception {
        Path fakeHome = dir.resolve("home");
        Files.createDirectories(fakeHome);
        String prev = System.getProperty("user.home");
        try {
            System.setProperty("user.home", fakeHome.toString());
            PreferencesStore store = new PreferencesStore();
            UIPreferences prefs = new UIPreferences();
            Map<String, String> shortcuts = new LinkedHashMap<>(UIPreferences.DEFAULT_SHORTCUTS);
            shortcuts.put("addEvent", "SPACE");
            shortcuts.put("toggleControls", "Ctrl+C");
            prefs.setShortcuts(shortcuts);
            store.save(prefs);
            assertTrue(Files.isRegularFile(store.getPath()));
            UIPreferences loaded = store.load();
            Map<String, String> loadedShortcuts = loaded.getShortcuts();
            assertNotNull(loadedShortcuts);
            assertEquals("SPACE", loadedShortcuts.get("addEvent"));
            assertEquals("Ctrl+C", loadedShortcuts.get("toggleControls"));
            assertEquals(UIPreferences.DEFAULT_SHORTCUTS.get("help"), loadedShortcuts.get("help"));
        } finally {
            if (prev != null) System.setProperty("user.home", prev);
        }
    }

    @Test
    void load_withMissingShortcuts_returnsDefaults() {
        PreferencesStore store = new PreferencesStore();
        UIPreferences prefs = store.load();
        Map<String, String> shortcuts = prefs.getShortcuts();
        assertNotNull(shortcuts);
        assertEquals(UIPreferences.DEFAULT_SHORTCUTS.get("addEvent"), shortcuts.get("addEvent"));
        assertEquals(UIPreferences.DEFAULT_SHORTCUTS.get("help"), shortcuts.get("help"));
    }
}
