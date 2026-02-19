package org.artisan.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.artisan.model.ProfileData;
import org.artisan.model.Roastlog;

/**
 * Auto-save behavior mirroring Python Artisan autosave.py.
 * All settings persisted in Preferences under "autosave.*".
 * Scheduler uses daemon threads so it does not block JVM exit.
 */
public final class AutoSave {

    private static final String PREF_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "autosave.";
    private static final String KEY_ENABLED = PREFIX + "enabled";
    private static final String KEY_INTERVAL = PREFIX + "intervalMinutes";
    private static final String KEY_SAVE_PATH = PREFIX + "savePath";
    private static final String KEY_PREFIX = PREFIX + "prefix";
    private static final String KEY_ADD_TIMESTAMP = PREFIX + "addTimestamp";
    private static final String KEY_SAVE_ON_DROP = PREFIX + "saveOnDrop";

    private static final int DEFAULT_INTERVAL = 5;
    private static final int MIN_INTERVAL = 1;
    private static final int MAX_INTERVAL = 60;
    private static final String DEFAULT_PREFIX = "autosave";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final Logger LOG = Logger.getLogger(AutoSave.class.getName());

    private boolean enabled;
    private int intervalMinutes;
    private String savePath;
    private String prefix;
    private boolean addTimestamp;
    private boolean saveOnDrop;

    private volatile ScheduledExecutorService scheduler;
    private volatile Supplier<ProfileData> profileDataSupplier;
    private volatile Supplier<String> titleSupplier;

    public AutoSave() {
        load();
    }

    public void load() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        enabled = prefs.getBoolean(KEY_ENABLED, false);
        intervalMinutes = clampInterval(prefs.getInt(KEY_INTERVAL, DEFAULT_INTERVAL));
        savePath = prefs.get(KEY_SAVE_PATH, "");
        prefix = prefs.get(KEY_PREFIX, DEFAULT_PREFIX);
        if (prefix == null) prefix = DEFAULT_PREFIX;
        addTimestamp = prefs.getBoolean(KEY_ADD_TIMESTAMP, true);
        saveOnDrop = prefs.getBoolean(KEY_SAVE_ON_DROP, true);
    }

    public void save() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.putBoolean(KEY_ENABLED, enabled);
        prefs.putInt(KEY_INTERVAL, intervalMinutes);
        prefs.put(KEY_SAVE_PATH, savePath != null ? savePath : "");
        prefs.put(KEY_PREFIX, prefix != null ? prefix : DEFAULT_PREFIX);
        prefs.putBoolean(KEY_ADD_TIMESTAMP, addTimestamp);
        prefs.putBoolean(KEY_SAVE_ON_DROP, saveOnDrop);
    }

    private static int clampInterval(int v) {
        if (v < MIN_INTERVAL) return MIN_INTERVAL;
        if (v > MAX_INTERVAL) return MAX_INTERVAL;
        return v;
    }

    /**
     * Starts the auto-save scheduler. Call when recording begins (after CHARGE).
     * Fires every intervalMinutes; if enabled and profile has at least CHARGE event,
     * saves to savePath with filename &lt;prefix&gt;_&lt;title&gt;_&lt;yyyyMMdd_HHmmss&gt;.alog
     * (timestamp omitted if addTimestamp is false).
     */
    public void start(Supplier<ProfileData> profileDataSupplier, Supplier<String> titleSupplier) {
        stop();
        this.profileDataSupplier = profileDataSupplier;
        this.titleSupplier = titleSupplier;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoSave");
            t.setDaemon(true);
            return t;
        });
        long periodMinutes = Math.max(1, intervalMinutes);
        scheduler.scheduleAtFixedRate(this::tick, periodMinutes, periodMinutes, TimeUnit.MINUTES);
    }

    /** For tests: run one tick without waiting for the scheduler. */
    void runTickForTest() {
        tick();
    }

    /**
     * Stops the scheduler. Call when recording ends (DROP or manual stop).
     */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
    }

    private void tick() {
        if (!enabled) return;
        Supplier<ProfileData> sup = profileDataSupplier;
        Supplier<String> titleSup = titleSupplier;
        if (sup == null || titleSup == null) return;
        ProfileData profile = sup.get();
        if (profile == null || !hasChargeEvent(profile)) return;
        String dir = savePath != null ? savePath.trim() : "";
        if (dir.isEmpty()) return;
        Path dirPath = Path.of(dir);
        if (!Files.isDirectory(dirPath)) return;
        String filename = buildFilename(titleSup.get());
        Path path = dirPath.resolve(filename);
        try {
            Roastlog.save(profile, path);
            LOG.log(Level.INFO, "Autosave: {0}", path);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Autosave failed: " + path, e);
        }
    }

    /**
     * Called when DROP event fires. If saveOnDrop is true, performs immediate save
     * with same naming convention.
     */
    public void onDrop() {
        if (!saveOnDrop || !enabled) return;
        Supplier<ProfileData> sup = profileDataSupplier;
        Supplier<String> titleSup = titleSupplier;
        if (sup == null || titleSup == null) return;
        ProfileData profile = sup.get();
        if (profile == null || !hasChargeEvent(profile)) return;
        String dir = savePath != null ? savePath.trim() : "";
        if (dir.isEmpty()) return;
        Path dirPath = Path.of(dir);
        if (!Files.isDirectory(dirPath)) return;
        String filename = buildFilename(titleSup.get());
        Path path = dirPath.resolve(filename);
        try {
            Roastlog.save(profile, path);
            LOG.log(Level.INFO, "Autosave on DROP: {0}", path);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Autosave on DROP failed: " + path, e);
        }
    }

    private static boolean hasChargeEvent(ProfileData profile) {
        var ti = profile.getTimeindex();
        return ti != null && ti.size() > 0 && ti.get(0) != null && ti.get(0) >= 0;
    }

    /**
     * Builds filename: prefix_title_yyyyMMdd_HHmmss.alog, or prefix_title.alog if addTimestamp is false.
     * Title is sanitized for filesystem (replace invalid chars with underscore).
     */
    String buildFilename(String title) {
        String safeTitle = sanitizeFilename(title != null ? title : "roast");
        String base = prefix + "_" + safeTitle;
        if (addTimestamp) {
            base += "_" + LocalDateTime.now().format(TIMESTAMP_FORMAT);
        }
        return base + ".alog";
    }

    private static String sanitizeFilename(String s) {
        if (s == null) return "roast";
        return s.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    // --- Getters/setters (used by AutoSaveDialog and tests) ---

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(int intervalMinutes) { this.intervalMinutes = clampInterval(intervalMinutes); }

    public String getSavePath() { return savePath != null ? savePath : ""; }
    public void setSavePath(String savePath) { this.savePath = savePath; }

    public String getPrefix() { return prefix != null ? prefix : DEFAULT_PREFIX; }
    public void setPrefix(String prefix) { this.prefix = prefix != null ? prefix : DEFAULT_PREFIX; }

    public boolean isAddTimestamp() { return addTimestamp; }
    public void setAddTimestamp(boolean addTimestamp) { this.addTimestamp = addTimestamp; }

    public boolean isSaveOnDrop() { return saveOnDrop; }
    public void setSaveOnDrop(boolean saveOnDrop) { this.saveOnDrop = saveOnDrop; }

    /** Restore default values (in-memory only; call save() to persist). */
    public void restoreDefaults() {
        enabled = false;
        intervalMinutes = DEFAULT_INTERVAL;
        savePath = "";
        prefix = DEFAULT_PREFIX;
        addTimestamp = true;
        saveOnDrop = true;
    }
}
