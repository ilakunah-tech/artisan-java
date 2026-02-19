package org.artisan;

import javafx.application.Application;
import org.artisan.view.InMemoryLogHandler;
import org.artisan.view.MainWindow;

/**
 * Artisan JavaFX application entry point.
 * Delegates to MainWindow (toolbar, chart, status bar).
 */
public class Launcher {

    public static void main(String[] args) {
        InMemoryLogHandler.install();
        Application.launch(MainWindow.class, args);
    }
}
