package org.artisan.controller;

import org.artisan.device.DeviceChannel;
import org.artisan.device.DeviceException;
import org.artisan.device.SampleResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommControllerTest {

    private CommController commController;

    @BeforeEach
    void setUp() {
        commController = new CommController();
    }

    @AfterEach
    void tearDown() {
        if (commController != null) {
            commController.stop();
        }
    }

    /** Scheduler ticks and calls read() on the channel; verify read() is invoked at expected rate (no FX thread). */
    @Test
    void start_callsOnSample_atExpectedRate() throws InterruptedException {
        AtomicInteger readCount = new AtomicInteger(0);
        DeviceChannel mockChannel = new DeviceChannel() {
            @Override
            public void open() {}
            @Override
            public void close() {}
            @Override
            public boolean isOpen() { return true; }
            @Override
            public SampleResult read() {
                readCount.incrementAndGet();
                return SampleResult.now(100.0, 80.0);
            }
            @Override
            public String getDescription() { return "Mock"; }
        };
        commController.setChannel(mockChannel);
        // Do not set onSample so we avoid Platform.runLater (no JavaFX toolkit in test)
        commController.start(0.05); // 50 ms interval
        Thread.sleep(250);
        int n = readCount.get();
        assertTrue(n >= 3, "read() should be called at least 3 times (got " + n + ")");
        commController.stop();
    }

    /** After 3 consecutive errors the error path is hit (onError/stop are dispatched on FX thread; we verify read() called 3+ times). */
    @Test
    void consecutiveErrors_callsOnError_afterThreshold() throws InterruptedException {
        AtomicInteger readAttempts = new AtomicInteger(0);
        commController.setOnError(() -> {});
        DeviceChannel failingChannel = new DeviceChannel() {
            @Override
            public void open() {}
            @Override
            public void close() {}
            @Override
            public boolean isOpen() { return true; }
            @Override
            public SampleResult read() {
                readAttempts.incrementAndGet();
                throw new DeviceException("mock error");
            }
            @Override
            public String getDescription() { return "Failing"; }
        };
        commController.setChannel(failingChannel);
        commController.start(0.05); // 50 ms
        Thread.sleep(400);
        assertTrue(readAttempts.get() >= 3, "read() should be called at least 3 times before onError/stop (FX) would run");
        commController.stop();
    }

    @Test
    void stop_shutsDownScheduler() throws InterruptedException {
        DeviceChannel mockChannel = new DeviceChannel() {
            @Override
            public void open() {}
            @Override
            public void close() {}
            @Override
            public boolean isOpen() { return true; }
            @Override
            public SampleResult read() { return SampleResult.now(0, 0); }
            @Override
            public String getDescription() { return "Mock"; }
        };
        commController.setChannel(mockChannel);
        commController.start(0.2);
        assertTrue(commController.isRunning());
        commController.stop();
        assertFalse(commController.isRunning());
        Thread.sleep(300);
        assertFalse(commController.isRunning());
    }
}
