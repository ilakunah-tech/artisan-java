package org.artisan.controller;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.artisan.device.DeviceChannel;
import org.artisan.device.DeviceException;
import org.artisan.device.SampleResult;

import javafx.application.Platform;

import java.util.function.Consumer;

/**
 * Manages the active DeviceChannel and the sampling loop. Dispatches onSample and
 * onError on the JavaFX thread via Platform.runLater.
 */
public final class CommController {

    private static final Logger LOG = Logger.getLogger(CommController.class.getName());
    private static final int MAX_CONSECUTIVE_ERRORS = 3;
    private static final int STOP_AWAIT_SECONDS = 2;

    private volatile DeviceChannel activeChannel;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> future;
    private Consumer<SampleResult> onSample;
    private Runnable onError;
    private volatile int consecutiveErrors;
    private volatile long startTimeMs;

    public CommController() {
        this.activeChannel = null;
        this.scheduler = null;
        this.future = null;
        this.onSample = null;
        this.onError = null;
        this.consecutiveErrors = 0;
        this.startTimeMs = 0;
    }

    public void setChannel(DeviceChannel ch) {
        this.activeChannel = ch;
    }

    public DeviceChannel getActiveChannel() {
        return activeChannel;
    }

    public void setOnSample(Consumer<SampleResult> onSample) {
        this.onSample = onSample;
    }

    public void setOnError(Runnable onError) {
        this.onError = onError;
    }

    /**
     * Starts the sampling loop at the given interval (seconds). Uses a daemon
     * ScheduledExecutorService. Each tick: reads activeChannel; on success
     * dispatches onSample on FX thread and resets error count; on DeviceException
     * logs WARN and after MAX_CONSECUTIVE_ERRORS calls onError on FX thread and stops.
     */
    public synchronized void start(double intervalSeconds) {
        if (activeChannel == null) {
            LOG.warning("CommController.start: no active channel set");
            return;
        }
        if (future != null && !future.isCancelled()) {
            return;
        }
        if (!activeChannel.isOpen()) {
            try {
                activeChannel.open();
            } catch (DeviceException e) {
                LOG.log(Level.WARNING, "Failed to open channel: {0}", e.getMessage());
                if (onError != null) {
                    Platform.runLater(onError);
                }
                return;
            }
        }
        consecutiveErrors = 0;
        startTimeMs = System.currentTimeMillis();
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "artisan-comm");
                t.setDaemon(true);
                return t;
            });
        }
        long periodMs = Math.max(1, (long) (intervalSeconds * 1000));
        Consumer<SampleResult> consumer = this.onSample;
        Runnable errCallback = this.onError;
        future = scheduler.scheduleAtFixedRate(() -> {
            DeviceChannel ch = activeChannel;
            if (ch == null || !ch.isOpen()) return;
            try {
                SampleResult result = ch.read();
                consecutiveErrors = 0;
                if (consumer != null) {
                    SampleResult r = result;
                    Platform.runLater(() -> consumer.accept(r));
                }
            } catch (DeviceException e) {
                LOG.log(Level.WARNING, "Device read error: {0}", e.getMessage());
                int n = ++consecutiveErrors;
                if (n >= MAX_CONSECUTIVE_ERRORS && errCallback != null) {
                    Platform.runLater(() -> {
                        errCallback.run();
                        stop();
                    });
                }
            }
        }, periodMs, periodMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the sampling loop and shuts down the scheduler (await up to 2 seconds).
     */
    public synchronized void stop() {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(STOP_AWAIT_SECONDS, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
    }

    public synchronized boolean isRunning() {
        return future != null && !future.isCancelled();
    }

    /**
     * Returns the elapsed time in milliseconds since start() was called (0 if not started).
     */
    public long getElapsedMs() {
        long start = startTimeMs;
        return start > 0 ? System.currentTimeMillis() - start : 0;
    }
}
