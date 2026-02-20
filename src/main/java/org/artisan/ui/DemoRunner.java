package org.artisan.ui;

import javafx.animation.AnimationTimer;
import org.artisan.controller.AppController;

/**
 * Simple demo data generator: feeds synthetic BT/ET samples to simulate a roast.
 * Uses AnimationTimer to push samples on FX thread. Call start() then stop() when done.
 */
public final class DemoRunner {

    private final AppController appController;
    private AnimationTimer timer;
    private double startTimeSec;
    private boolean running;

    public DemoRunner(AppController appController) {
        this.appController = appController;
    }

    /** Starts demo: session.start() and synthetic samples (ramp up BT/ET). */
    public void start() {
        if (appController == null) return;
        appController.getSession().start();
        startTimeSec = 0;
        running = true;
        timer = new AnimationTimer() {
            private long lastNanos = -1;
            private double nextSampleAt = 0;

            @Override
            public void handle(long now) {
                if (lastNanos < 0) lastNanos = now;
                double elapsedSec = (now - lastNanos) / 1_000_000_000.0;
                lastNanos = now;
                startTimeSec += elapsedSec;
                if (startTimeSec < nextSampleAt) return;
                nextSampleAt = startTimeSec + 1.0;
                double t = startTimeSec;
                double bt = 20 + t * 2.5 + Math.sin(t * 0.1) * 3;
                double et = 80 + t * 1.8 + Math.sin(t * 0.08) * 5;
                if (bt > 230) bt = 230;
                if (et > 250) et = 250;
                if (appController != null)
                    appController.acceptSampleFromComm(t, bt, et);
            }
        };
        timer.start();
    }

    public void stop() {
        running = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public boolean isRunning() {
        return running && timer != null;
    }
}
