package org.artisan.ui;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.artisan.controller.AppController;
import org.artisan.controller.DisplaySettings;
import org.artisan.ui.screens.PreRoastScreen;
import org.artisan.ui.state.LayoutState;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for Cropster RI5 UI: Pre-Roast screen, layout persistence, PreferencesStore.
 */
@ExtendWith(ApplicationExtension.class)
class UISmokeTest extends ApplicationTest {

    private Stage testStage;

    @Override
    public void start(Stage stage) {
        testStage = stage;
        stage.show();
    }

    @Test
    void preRoastScreen_buildsWithoutError() {
        interact(() -> {
            PreRoastScreen screen = new PreRoastScreen(null, new UIPreferences(), new PreferencesStore());
            Pane root = screen.getRoot();
            assertNotNull(root);
            assertFalse(root.getChildren().isEmpty());
        });
    }

    @Test
    void preRoastScreen_withPreferences_buildsCorrectly() {
        interact(() -> {
            UIPreferences prefs = new UIPreferences();
            prefs.setDensity(UIPreferences.Density.COMPACT);
            prefs.setVisibleBT(false);
            PreRoastScreen screen = new PreRoastScreen(null, prefs, new PreferencesStore());
            assertNotNull(screen.getRoot());
        });
    }

    @Test
    void layoutPersistence_saveAndRestore(@TempDir Path dir) throws Exception {
        Path fakeHome = dir.resolve("home");
        Files.createDirectories(fakeHome);
        String prev = System.getProperty("user.home");
        try {
            System.setProperty("user.home", fakeHome.toString());
            PreferencesStore store = new PreferencesStore();
            UIPreferences prefs = new UIPreferences();
            prefs.getLayoutState().setDockWidth(320);
            prefs.getLayoutState().setPanelCollapsed(LayoutState.PANEL_LEGEND, true);
            prefs.getLayoutState().setControlsVisible(false);
            prefs.setMainDividerPosition(0.7);
            store.save(prefs);

            UIPreferences loaded = store.load();
            assertEquals(320.0, loaded.getLayoutState().getDockWidth());
            assertTrue(loaded.getLayoutState().isPanelCollapsed(LayoutState.PANEL_LEGEND));
            assertFalse(loaded.getLayoutState().isControlsVisible());
            assertEquals(0.7, loaded.getMainDividerPosition());
        } finally {
            if (prev != null) System.setProperty("user.home", prev);
        }
    }

    @Test
    void layoutPersistence_resetThenRestore(@TempDir Path dir) throws Exception {
        Path fakeHome = dir.resolve("home");
        Files.createDirectories(fakeHome);
        String prev = System.getProperty("user.home");
        try {
            System.setProperty("user.home", fakeHome.toString());
            PreferencesStore store = new PreferencesStore();
            UIPreferences prefs = new UIPreferences();
            prefs.getLayoutState().setDockWidth(400);
            prefs.getLayoutState().setPanelDetached(LayoutState.PANEL_READOUTS, true);
            store.resetLayout(prefs);
            store.save(prefs);

            UIPreferences loaded = store.load();
            assertEquals(LayoutState.DEFAULT_DOCK_WIDTH, loaded.getLayoutState().getDockWidth());
            assertEquals(0.75, loaded.getMainDividerPosition());
        } finally {
            if (prev != null) System.setProperty("user.home", prev);
        }
    }

    @Test
    void preRoastScreen_refreshDoesNotThrow() {
        interact(() -> {
            PreRoastScreen screen = new PreRoastScreen(null, new UIPreferences(), new PreferencesStore());
            assertDoesNotThrow(screen::refresh);
        });
    }

    @Test
    void roastLiveScreen_applyLayoutFromPreferences_doesNotThrow() {
        interact(() -> {
            org.artisan.controller.RoastSession session = new org.artisan.controller.RoastSession();
            org.artisan.model.ArtisanTime time = new org.artisan.model.ArtisanTime();
            org.artisan.model.Sampling sampling = new org.artisan.model.Sampling(time);
            org.artisan.device.StubDevice device = new org.artisan.device.StubDevice();
            org.artisan.model.ColorConfig colorConfig = new org.artisan.model.ColorConfig(org.artisan.model.ColorConfig.Theme.DARK);
            org.artisan.model.AxisConfig axisConfig = new org.artisan.model.AxisConfig();
            org.artisan.model.CurveSet curveSet = org.artisan.model.CurveSet.createDefault();
            org.artisan.view.RoastChartController chartController = new org.artisan.view.RoastChartController(
                session.getCanvasData(), colorConfig, axisConfig, new DisplaySettings());
            AppController appController = new AppController(
                session, sampling, device, chartController, axisConfig, colorConfig, curveSet);
            UIPreferences prefs = new UIPreferences();
            org.artisan.ui.screens.RoastLiveScreen screen = new org.artisan.ui.screens.RoastLiveScreen(
                testStage, appController, chartController, new DisplaySettings(), prefs, new PreferencesStore());
            assertDoesNotThrow(screen::applyLayoutFromPreferences);
        });
    }
}
