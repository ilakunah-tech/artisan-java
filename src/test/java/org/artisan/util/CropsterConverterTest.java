package org.artisan.util;

import org.artisan.model.ProfileData;
import org.artisan.model.RoastProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CropsterConverterTest {

    @TempDir
    Path tempDir;

    @Test
    void exportToCropster_createsFile() throws Exception {
        ProfileData pd = simpleProfile();
        Path out = tempDir.resolve("out.csv");
        CropsterConverter.exportToCropster(pd, new RoastProperties(), out);
        assertTrue(Files.exists(out));
        List<String> lines = Files.readAllLines(out, StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty());
        assertEquals("Time,BT,ET,ROR_BT,Gas,Inlet", lines.get(0));
    }

    @Test
    void exportToCropster_rowCount() throws Exception {
        ProfileData pd = simpleProfile();
        Path out = tempDir.resolve("out.csv");
        CropsterConverter.exportToCropster(pd, new RoastProperties(), out);
        List<String> lines = Files.readAllLines(out, StandardCharsets.UTF_8);
        assertEquals(pd.getTimex().size() + 1, lines.size());
    }

    @Test
    void importFromCropster_parsesTimeMMSS() throws Exception {
        Path in = tempDir.resolve("in.csv");
        Files.writeString(in, "Time,BT\n01:30,200\n", StandardCharsets.UTF_8);
        ProfileData pd = CropsterConverter.importFromCropster(in);
        assertNotNull(pd);
        assertEquals(90.0, pd.getTimex().get(0), 1e-9);
    }

    @Test
    void importFromCropster_parsesTimeSeconds() throws Exception {
        Path in = tempDir.resolve("in.csv");
        Files.writeString(in, "Time,BT\n90,200\n", StandardCharsets.UTF_8);
        ProfileData pd = CropsterConverter.importFromCropster(in);
        assertNotNull(pd);
        assertEquals(90.0, pd.getTimex().get(0), 1e-9);
    }

    @Test
    void importFromCropster_returnsNullOnEmpty() throws Exception {
        Path in = tempDir.resolve("empty.csv");
        Files.writeString(in, "", StandardCharsets.UTF_8);
        assertNull(CropsterConverter.importFromCropster(in));
    }

    @Test
    void importFromCropster_populatesBT() throws Exception {
        Path in = tempDir.resolve("in.csv");
        Files.writeString(in, "Time,BT\n0,200\n1,201\n", StandardCharsets.UTF_8);
        ProfileData pd = CropsterConverter.importFromCropster(in);
        assertNotNull(pd);
        assertNotNull(pd.getTemp2());
        assertFalse(pd.getTemp2().isEmpty());
    }

    private static ProfileData simpleProfile() {
        ProfileData pd = new ProfileData();
        List<Double> tx = new ArrayList<>();
        List<Double> bt = new ArrayList<>();
        List<Double> et = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tx.add((double) i);
            bt.add(200.0 + i);
            et.add(220.0 + i);
        }
        pd.setTimex(tx);
        pd.setTemp2(bt);
        pd.setTemp1(et);
        return pd;
    }
}

