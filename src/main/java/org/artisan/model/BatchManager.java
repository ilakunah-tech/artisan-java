package org.artisan.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Manages a list of batches. Persistence: ~/.artisan/batches.json (Gson).
 */
public final class BatchManager {

    private static final String DIR_NAME = ".artisan";
    private static final String FILE_NAME = "batches.json";

    private static Path batchesPath() {
        String home = System.getProperty("user.home");
        return Paths.get(home, DIR_NAME, FILE_NAME);
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final TypeToken<List<Batch>> LIST_TYPE = new TypeToken<List<Batch>>() {};

    private final List<Batch> batches = new ArrayList<>();

    /** Loads from ~/.artisan/batches.json; silently empty if missing. */
    public void load() {
        load(batchesPath());
    }

    /** Loads from the given path. Used for tests. */
    public void load(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            batches.clear();
            return;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            List<Batch> loaded = GSON.fromJson(json, LIST_TYPE.getType());
            batches.clear();
            if (loaded != null) {
                for (Batch b : loaded) {
                    if (b != null) batches.add(b);
                }
            }
        } catch (Exception e) {
            batches.clear();
        }
    }

    /** Persists to ~/.artisan/batches.json. Creates directory if needed. */
    public void save() throws IOException {
        save(batchesPath());
    }

    /** Saves to the given path. Used for tests. */
    public void save(Path path) throws IOException {
        if (path == null) return;
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
        String json = GSON.toJson(batches);
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    /** Auto-assigns batchNumber (max existing + 1) and adds the batch. */
    public void addBatch(Batch b) {
        if (b == null) return;
        if (b.getBatchNumber() <= 0) {
            b.setBatchNumber(getNextBatchNumber());
        }
        batches.add(b);
    }

    /** Removes the batch with the given batchNumber. Returns true if found and removed. */
    public boolean removeBatch(int batchNumber) {
        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).getBatchNumber() == batchNumber) {
                batches.remove(i);
                return true;
            }
        }
        return false;
    }

    public Optional<Batch> getBatch(int batchNumber) {
        return batches.stream()
                .filter(b -> b.getBatchNumber() == batchNumber)
                .findFirst();
    }

    /** Unmodifiable list sorted by batchNumber. */
    public List<Batch> getBatches() {
        List<Batch> copy = new ArrayList<>(batches);
        copy.sort(Comparator.comparingInt(Batch::getBatchNumber));
        return Collections.unmodifiableList(copy);
    }

    /** Next batch number (max existing + 1). */
    public int getNextBatchNumber() {
        int max = 0;
        for (Batch b : batches) {
            if (b.getBatchNumber() > max) max = b.getBatchNumber();
        }
        return max + 1;
    }

    /** Writes all batches as CSV (header + rows) using toMap() fields. Standard Java only. */
    public void exportCsv(Path outputPath) throws IOException {
        if (outputPath == null) return;
        List<Batch> list = getBatches();
        if (list.isEmpty()) {
            Files.writeString(outputPath, "", StandardCharsets.UTF_8);
            return;
        }
        Map<String, String> first = list.get(0).toMap();
        List<String> keys = new ArrayList<>(first.keySet());
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", keys)).append("\n");
        for (Batch b : list) {
            Map<String, String> row = b.toMap();
            List<String> values = new ArrayList<>();
            for (String key : keys) {
                String v = row.get(key);
                if (v != null && (v.contains(",") || v.contains("\"") || v.contains("\n"))) {
                    v = "\"" + v.replace("\"", "\"\"") + "\"";
                }
                values.add(v != null ? v : "");
            }
            sb.append(String.join(",", values)).append("\n");
        }
        Files.writeString(outputPath, sb.toString(), StandardCharsets.UTF_8);
    }

    /** Removes all batches. Does NOT save. */
    public void clear() {
        batches.clear();
    }
}
