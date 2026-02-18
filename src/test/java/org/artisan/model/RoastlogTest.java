package org.artisan.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Roastlog: save/load roast profile to/from .alog (JSON) files.
 */
class RoastlogTest {

    @TempDir
    Path tempDir;

    private static ProfileData fullProfile() {
        ProfileData p = new ProfileData();
        p.setTitle("Test Roast");
        p.setSamplingInterval(1.0);
        // BT (temp1) and ET (temp2) curves
        p.setTimex(Arrays.asList(0.0, 60.0, 120.0, 180.0, 240.0));
        p.setTemp1(Arrays.asList(20.0, 80.0, 140.0, 200.0, 210.0));   // BT
        p.setTemp2(Arrays.asList(22.0, 90.0, 160.0, 220.0, 230.0)); // ET
        // Events: index, type, value
        p.setSpecialevents(Arrays.asList(1, 3));
        p.setSpecialeventstype(Arrays.asList(1, 2));
        p.setSpecialeventsvalue(Arrays.asList(0.0, 0.0));
        // Metadata: computed
        ComputedProfileInformation comp = new ComputedProfileInformation();
        comp.setChargeBt(20.0);
        comp.setChargeEt(22.0);
        comp.setDropTime(240.0);
        comp.setDropBt(210.0);
        comp.setDropEt(230.0);
        comp.setTotalTime(240.0);
        p.setComputed(comp);
        return p;
    }

    @Test
    void saveProfileToAlogFile() throws Exception {
        ProfileData profile = fullProfile();
        Path alog = tempDir.resolve("roast.alog");
        Roastlog.save(profile, alog);
        assertNotNull(Files.readAllBytes(alog));
        String content = Files.readString(alog);
        // JSON contains key fields
        assertEquals(true, content.contains("\"title\""));
        assertEquals(true, content.contains("Test Roast"));
        assertEquals(true, content.contains("\"timex\""));
        assertEquals(true, content.contains("\"temp1\""));
        assertEquals(true, content.contains("\"temp2\""));
        assertEquals(true, content.contains("\"specialevents\""));
        assertEquals(true, content.contains("\"computed\""));
    }

    @Test
    void loadProfileFromAlogFile() throws Exception {
        ProfileData original = fullProfile();
        Path alog = tempDir.resolve("load.alog");
        Roastlog.save(original, alog);
        ProfileData loaded = Roastlog.load(alog);
        assertNotNull(loaded);
        assertEquals(original.getTitle(), loaded.getTitle());
        assertEquals(original.getSamplingInterval(), loaded.getSamplingInterval());
        assertEquals(original.getTimex(), loaded.getTimex());
        assertEquals(original.getTemp1(), loaded.getTemp1());
        assertEquals(original.getTemp2(), loaded.getTemp2());
        assertEquals(original.getSpecialevents(), loaded.getSpecialevents());
        assertEquals(original.getSpecialeventstype(), loaded.getSpecialeventstype());
        assertEquals(original.getSpecialeventsvalue(), loaded.getSpecialeventsvalue());
        assertNotNull(loaded.getComputed());
        assertEquals(original.getComputed().getChargeBt(), loaded.getComputed().getChargeBt());
        assertEquals(original.getComputed().getDropTime(), loaded.getComputed().getDropTime());
    }

    @Test
    void allFieldsSerializeAndDeserialize() throws Exception {
        ProfileData p = fullProfile();
        Path alog = tempDir.resolve("roundtrip.alog");
        Roastlog.save(p, alog);
        ProfileData q = Roastlog.load(alog);
        assertNotNull(q);
        assertEquals(p.getTimex(), q.getTimex());
        assertEquals(p.getTemp1(), q.getTemp1());
        assertEquals(p.getTemp2(), q.getTemp2());
        assertEquals(p.getSpecialevents(), q.getSpecialevents());
        assertEquals(p.getSpecialeventstype(), q.getSpecialeventstype());
        assertEquals(p.getSpecialeventsvalue(), q.getSpecialeventsvalue());
        assertEquals(p.getTitle(), q.getTitle());
        assertEquals(p.getSamplingInterval(), q.getSamplingInterval());
        assertEquals(p.getComputed().getChargeBt(), q.getComputed().getChargeBt());
        assertEquals(p.getComputed().getChargeEt(), q.getComputed().getChargeEt());
        assertEquals(p.getComputed().getDropTime(), q.getComputed().getDropTime());
        assertEquals(p.getComputed().getDropBt(), q.getComputed().getDropBt());
        assertEquals(p.getComputed().getDropEt(), q.getComputed().getDropEt());
        assertEquals(p.getComputed().getTotalTime(), q.getComputed().getTotalTime());
    }

    @Test
    void corruptedJsonReturnsNull() throws Exception {
        Path bad = tempDir.resolve("bad.alog");
        Files.writeString(bad, "{ \"timex\": [1, 2, broken }");
        ProfileData loaded = Roastlog.load(bad);
        assertNull(loaded);
    }

    @Test
    void emptyFileReturnsNull() throws Exception {
        Path empty = tempDir.resolve("empty.alog");
        Files.writeString(empty, "");
        ProfileData loaded = Roastlog.load(empty);
        assertNull(loaded);
    }

    @Test
    void notJsonReturnsNull() throws Exception {
        Path text = tempDir.resolve("text.alog");
        Files.writeString(text, "not json at all");
        ProfileData loaded = Roastlog.load(text);
        assertNull(loaded);
    }

    @Test
    void loadMissingFileReturnsNull() {
        Path missing = tempDir.resolve("nonexistent.alog");
        ProfileData loaded = Roastlog.load(missing);
        assertNull(loaded);
    }

    @Test
    void saveNullProfileThrows() {
        Path p = tempDir.resolve("x.alog");
        assertThrows(IllegalArgumentException.class, () -> Roastlog.save(null, p));
    }

    @Test
    void saveNullPathThrows() {
        assertThrows(IllegalArgumentException.class, () -> Roastlog.save(fullProfile(), null));
    }

    @Test
    void loadOrThrowInvalidFileThrows() throws Exception {
        Path bad = tempDir.resolve("bad2.alog");
        Files.writeString(bad, "}{");
        assertThrows(Exception.class, () -> Roastlog.loadOrThrow(bad));
    }

    @Test
    void loadOrThrowMissingFileThrows() throws Exception {
        Path missing = tempDir.resolve("missing.alog");
        assertThrows(IOException.class, () -> Roastlog.loadOrThrow(missing));
    }
}
