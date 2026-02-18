package org.artisan.model;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Sampling timer: interval (delay) in milliseconds and a scheduled task that runs at that interval.
 * Ported from Artisan Python: main.setSamplingRate, qmc.delay / min_delay / default_delay,
 * and the sampling loop replaced by ScheduledExecutorService (no Thread.sleep).
 */
public final class Sampling {

    /** Default delay between samples (ms), matches Python qmc.default_delay. */
    public static final int DEFAULT_DELAY_MS = 2000;
    /** Minimum allowed delay (ms), matches Python qmc.min_delay. */
    public static final int MIN_DELAY_MS = 100;

    private final ArtisanTime timeclock;
    private volatile int delayMs;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> future;

    /**
     * Creates a sampling timer with default delay, using the given time source for elapsed since start.
     *
     * @param timeclock time source (e.g. for elapsed since start); not null
     */
    public Sampling(ArtisanTime timeclock) {
        this.timeclock = Objects.requireNonNull(timeclock, "timeclock");
        this.delayMs = DEFAULT_DELAY_MS;
    }

    /** Current sampling interval in milliseconds. */
    public int getDelayMs() {
        return delayMs;
    }

    /**
     * Sets the sampling rate (interval between samples). Clamped to {@link #MIN_DELAY_MS}.
     * Matches Python setSamplingRate(rate): qmc.delay = max(min_delay, rate).
     *
     * @param rateMs desired interval in milliseconds
     */
    public void setSamplingRate(int rateMs) {
        this.delayMs = Math.max(MIN_DELAY_MS, rateMs);
    }

    /** Elapsed time since last start() in milliseconds (0 if not started or after reset). */
    public double getElapsedMs() {
        return timeclock.elapsed();
    }

    /**
     * Starts the sampling loop: runs the given task at fixed rate with the current delay.
     * If already running, this method does nothing.
     *
     * @param onSample task to run on each sample tick; not null
     */
    public synchronized void start(Runnable onSample) {
        Objects.requireNonNull(onSample, "onSample");
        if (future != null && !future.isCancelled()) {
            return;
        }
        timeclock.start();
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "artisan-sampling");
                t.setDaemon(true);
                return t;
            });
        }
        long periodMs = Math.max(1, delayMs);
        future = executor.scheduleAtFixedRate(
                onSample,
                periodMs,
                periodMs,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the sampling loop. After this call, the scheduled task will not run again.
     */
    public synchronized void stop() {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            executor = null;
        }
    }

    /**
     * Resets the timer: stops the loop (if running) and restores the default delay.
     */
    public synchronized void reset() {
        stop();
        this.delayMs = DEFAULT_DELAY_MS;
    }

    /** Returns true if the sampling loop is currently running. */
    public synchronized boolean isRunning() {
        return future != null && !future.isCancelled();
    }
}
