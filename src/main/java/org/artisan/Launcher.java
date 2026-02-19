package org.artisan;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javafx.application.Application;
import org.artisan.view.InMemoryLogHandler;
import org.artisan.view.MainWindow;

/**
 * Artisan JavaFX application entry point.
 * Single-instance via file lock on ~/.artisan/artisan.lock.
 */
public class Launcher {

    private static final String LOCK_DIR = ".artisan";
    private static final String LOCK_FILE = "artisan.lock";

    private static FileLock lock;
    private static FileChannel lockChannel;
    private static Path lockPath;

    /**
     * Tries to acquire an exclusive lock on ~/.artisan/artisan.lock.
     * Call from MainWindow.start() on JavaFX thread. Returns false if already running.
     */
    public static synchronized boolean tryAcquireLock() {
        if (lock != null) return true;
        String home = System.getProperty("user.home");
        lockPath = Paths.get(home, LOCK_DIR, LOCK_FILE);
        try {
            Path parent = lockPath.getParent();
            if (parent != null) Files.createDirectories(parent);
            lockChannel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            lock = lockChannel.tryLock();
            return lock != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Releases the lock and deletes the lock file. Call on normal exit (close request or Application.stop()).
     */
    public static synchronized void releaseLock() {
        try {
            if (lock != null) {
                lock.close();
                lock = null;
            }
            if (lockChannel != null) {
                lockChannel.close();
                lockChannel = null;
            }
            if (lockPath != null && Files.exists(lockPath)) {
                Files.delete(lockPath);
            }
        } catch (IOException ignored) {}
        lockPath = null;
    }

    public static void main(String[] args) {
        InMemoryLogHandler.install();
        if (!tryAcquireLock()) {
            // JavaFX not yet started — use Swing or plain stderr
            javax.swing.JOptionPane.showMessageDialog(null,
                "Artisan Java is already running.",
                "Already Running",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(Launcher::releaseLock));
        Application.launch(MainWindow.class, args);
    }
}
