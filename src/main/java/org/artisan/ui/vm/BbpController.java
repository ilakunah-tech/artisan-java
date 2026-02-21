package org.artisan.ui.vm;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;

/**
 * Logic for Bean Batch Percentage (BBP) timer after DROP.
 * Sets vm.bbtActive=true on DROP; counts BBP elapsed; ends on next Charge.
 */
public final class BbpController {

    private final RoastViewModel vm;
    private AnimationTimer timer;
    private long bbpStartNanos;
    private boolean paused;
    private long totalPausedMs;

    public BbpController(RoastViewModel vm) {
        this.vm = vm;
    }

    /** Call after DROP event. */
    public void startBbp() {
        if (timer != null) timer.stop();
        vm.setBbtActive(true);
        bbpStartNanos = System.nanoTime();
        totalPausedMs = 0;
        paused = false;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (paused) return;
                long elapsedMs = (now - bbpStartNanos) / 1_000_000 - totalPausedMs;
                Platform.runLater(() -> vm.setBbpElapsedSeconds(Math.max(0, elapsedMs / 1000)));
            }
        };
        timer.start();
    }

    /** Pause or resume BBP timer. */
    public void togglePause() {
        if (timer == null) return;
        long now = System.nanoTime();
        if (paused) {
            bbpStartNanos = now;
        } else {
            totalPausedMs += (now - bbpStartNanos) / 1_000_000;
        }
        paused = !paused;
    }

    /** Call on next Charge (new roast). */
    public void endBbp() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        vm.setBbtActive(false);
    }
}
