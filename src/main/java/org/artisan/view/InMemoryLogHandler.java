package org.artisan.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * java.util.logging Handler that keeps the last N log records in a circular buffer (LinkedList).
 * Used by LogViewer to show recent application log entries.
 */
public final class InMemoryLogHandler extends Handler {

    private static final int DEFAULT_CAPACITY = 500;
    private static volatile InMemoryLogHandler instance;

    private final int capacity;
    private final LinkedList<String> records = new LinkedList<>();
    private final Formatter formatter = new java.util.logging.SimpleFormatter();

    public InMemoryLogHandler() {
        this(DEFAULT_CAPACITY);
    }

    public InMemoryLogHandler(int capacity) {
        this.capacity = Math.max(1, capacity);
        setLevel(Level.ALL);
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null) return;
        String line = formatter.format(record);
        synchronized (records) {
            records.add(line);
            while (records.size() > capacity) {
                records.removeFirst();
            }
        }
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}

    /**
     * Returns a copy of the current log records (safe to modify; does not affect the handler).
     */
    public List<String> getRecords() {
        synchronized (records) {
            return new ArrayList<>(records);
        }
    }

    /**
     * Clears the in-memory buffer.
     */
    public void clear() {
        synchronized (records) {
            records.clear();
        }
    }

    /**
     * Installs a global InMemoryLogHandler on the root logger and returns the instance.
     * Call at application startup (e.g. from Launcher).
     */
    public static InMemoryLogHandler install() {
        if (instance == null) {
            synchronized (InMemoryLogHandler.class) {
                if (instance == null) {
                    instance = new InMemoryLogHandler();
                    java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
                    root.addHandler(instance);
                }
            }
        }
        return instance;
    }

    /** Returns the singleton instance after install(), or null if not yet installed. */
    public static InMemoryLogHandler getInstance() {
        return instance;
    }
}
