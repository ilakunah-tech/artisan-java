package org.artisan.controller;

import org.artisan.model.ProfileData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoSaveTest {

    private static ProfileData profileWithCharge() {
        ProfileData p = new ProfileData();
        p.setTimex(List.of(0.0, 60.0));
        p.setTemp1(List.of(200.0, 205.0));
        p.setTemp2(List.of(100.0, 150.0));
        List<Integer> ti = new ArrayList<>();
        ti.add(0);  // CHARGE at index 0
        for (int i = 1; i < 8; i++) ti.add(-1);
        p.setTimeindex(ti);
        return p;
    }

    @Test
    void buildFilename_withTimestamp_matchesPattern() {
        AutoSave autoSave = new AutoSave();
        autoSave.setPrefix("autosave");
        autoSave.setAddTimestamp(true);
        String name = autoSave.buildFilename("MyRoast");
        assertTrue(name.startsWith("autosave_MyRoast_"));
        assertTrue(name.endsWith(".alog"));
        String middle = name.substring("autosave_MyRoast_".length(), name.length() - 5);
        assertTrue(middle.matches("\\d{8}_\\d{6}"));
    }

    @Test
    void buildFilename_withoutTimestamp_noDateInName() {
        AutoSave autoSave = new AutoSave();
        autoSave.setPrefix("autosave");
        autoSave.setAddTimestamp(false);
        String name = autoSave.buildFilename("Roast");
        assertEquals("autosave_Roast.alog", name);
    }

    @Test
    void onDrop_savesFile_whenSaveOnDropEnabled(@TempDir Path dir) throws Exception {
        AutoSave autoSave = new AutoSave();
        autoSave.setEnabled(true);
        autoSave.setSaveOnDrop(true);
        autoSave.setSavePath(dir.toString());
        autoSave.setPrefix("autosave");
        ProfileData profile = profileWithCharge();
        autoSave.start(() -> profile, () -> "test");
        autoSave.onDrop();
        autoSave.stop();
        long count = Files.list(dir).filter(p -> p.getFileName().toString().startsWith("autosave_") && p.getFileName().toString().endsWith(".alog")).count();
        assertTrue(count >= 1, "Expected at least one autosave file");
    }

    @Test
    void onDrop_doesNotSave_whenSaveOnDropDisabled(@TempDir Path dir) {
        AutoSave autoSave = new AutoSave();
        autoSave.setEnabled(true);
        autoSave.setSaveOnDrop(false);
        autoSave.setSavePath(dir.toString());
        ProfileData profile = profileWithCharge();
        autoSave.start(() -> profile, () -> "test");
        autoSave.onDrop();
        autoSave.stop();
        try {
            long count = Files.list(dir).count();
            assertEquals(0, count, "No file should be created when saveOnDrop is disabled");
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void disabled_doesNotSave_onTick(@TempDir Path dir) {
        AutoSave autoSave = new AutoSave();
        autoSave.setEnabled(false);
        autoSave.setSavePath(dir.toString());
        ProfileData profile = profileWithCharge();
        autoSave.start(() -> profile, () -> "roast");
        autoSave.runTickForTest();
        autoSave.stop();
        try {
            long count = Files.list(dir).count();
            assertEquals(0, count, "No file should be created when disabled");
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
