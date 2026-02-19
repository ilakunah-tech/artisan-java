package org.artisan.controller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Lightweight helper for current file path, dirty state, and recent files.
 * Used by MainWindow and AppController. Recent files persisted in Preferences
 * under keys "recent.file.0" … "recent.file.9".
 */
public final class FileSession {

    private static final String PREF_NODE = "org/artisan/artisan-java";
    private static final String RECENT_PREFIX = "recent.file.";
    private static final int MAX_RECENT = 10;

    private Path currentFilePath;
    private boolean dirty;
    private Runnable onStateChange;

    public FileSession() {
        this.currentFilePath = null;
        this.dirty = false;
    }

    /** Optional callback when dirty or path changes (e.g. to update window title). */
    public void setOnStateChange(Runnable onStateChange) {
        this.onStateChange = onStateChange;
    }

    private void notifyStateChange() {
        if (onStateChange != null) onStateChange.run();
    }

    public void markDirty() {
        this.dirty = true;
        notifyStateChange();
    }

    public void markSaved(Path path) {
        this.currentFilePath = path != null ? path.normalize() : null;
        this.dirty = false;
        if (path != null) {
            pushRecentFile(path);
        }
        notifyStateChange();
    }

    public void markNew() {
        this.currentFilePath = null;
        this.dirty = false;
        notifyStateChange();
    }

    public boolean isDirty() {
        return dirty;
    }

    public Path getCurrentFilePath() {
        return currentFilePath;
    }

    /**
     * Adds path to recent list (max 10). Most recent first. Persists to Preferences.
     */
    public void pushRecentFile(Path p) {
        if (p == null) return;
        Path normalized = p.normalize().toAbsolutePath();
        String pathStr = normalized.toString();
        List<String> recent = new ArrayList<>(getRecentFilesPaths());
        recent.remove(pathStr);
        recent.add(0, pathStr);
        while (recent.size() > MAX_RECENT) {
            recent.remove(recent.size() - 1);
        }
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        for (int i = 0; i < MAX_RECENT; i++) {
            if (i < recent.size()) {
                prefs.put(RECENT_PREFIX + i, recent.get(i));
            } else {
                prefs.remove(RECENT_PREFIX + i);
            }
        }
    }

    /**
     * Returns recent file paths (most recent first), from Preferences.
     */
    public List<Path> getRecentFiles() {
        List<String> paths = getRecentFilesPaths();
        List<Path> result = new ArrayList<>(paths.size());
        for (String s : paths) {
            if (s != null && !s.isBlank()) {
                result.add(Path.of(s));
            }
        }
        return Collections.unmodifiableList(result);
    }

    private List<String> getRecentFilesPaths() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        List<String> list = new LinkedList<>();
        for (int i = 0; i < MAX_RECENT; i++) {
            String v = prefs.get(RECENT_PREFIX + i, "");
            if (v != null && !v.isBlank()) {
                list.add(v);
            }
        }
        return list;
    }

    public void clearRecentFiles() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        for (int i = 0; i < MAX_RECENT; i++) {
            prefs.remove(RECENT_PREFIX + i);
        }
    }
}
