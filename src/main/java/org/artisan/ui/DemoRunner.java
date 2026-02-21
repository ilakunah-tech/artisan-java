package org.artisan.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import org.artisan.controller.AppController;
import org.artisan.ui.vm.RoastViewModel;

/**
 * Simple demo data generator: feeds synthetic BT/ET samples to simulate a roast.
 * Uses AnimationTimer to push samples on FX thread. Call start() then stop() when done.
 */
public final class DemoRunner {

    private final AppController appController;
    private RoastViewModel viewModel;
    private AnimationTimer timer;
    private double startTimeSec;
    private boolean running;

    public DemoRunner(AppController appController) {
        this.appController = appController;
    }

    public void setViewModel(RoastViewModel vm) {
        this.viewModel = vm;
    }

    /** Synthetic gas/air/drum for modulation chart. Gas: 70->40 at 5min, 60 at 8min. */
    private static void pushModulation(RoastViewModel vm, double t) {
        if (vm == null) return;
        Platform.runLater(() -> {
            double gas = 70;
            if (t > 300) gas = 40 + (t - 300) * 0.1;
            if (t > 480) gas = 60;
            gas = Math.max(0, Math.min(100, gas));
            double air = 30 + Math.min(t * 0.05, 40);
            air = Math.max(0, Math.min(100, air));
            double drum = 50 + Math.sin(t * 0.02) * 10;
            drum = Math.max(0, Math.min(100, drum));
            vm.setGasValue(gas);
            vm.setAirValue(air);
            vm.setDrumValue(drum);
        });
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
                if (viewModel != null) {
                    double elapsedTimeSec = t;
                    Platform.runLater(() -> viewModel.setElapsedSec(elapsedTimeSec));
                }
                pushModulation(viewModel, t);
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
