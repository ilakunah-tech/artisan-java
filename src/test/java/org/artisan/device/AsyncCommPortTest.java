package org.artisan.device;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AsyncCommPort} with mocked connection (in-process TCP server).
 */
class AsyncCommPortTest {

    private AsyncCommPort port;
    private ServerSocket mockServer;

    @AfterEach
    void tearDown() throws Exception {
        if (port != null && port.isRunning()) {
            port.stop();
        }
        if (mockServer != null && !mockServer.isClosed()) {
            mockServer.close();
        }
    }

    @Test
    void startStopDoesNotThrow() {
        port = new TestableAsyncCommPort("127.0.0.1", 0);
        port.start(1.0);
        assertTrue(port.isRunning());
        port.stop();
        assertFalse(port.isRunning());
    }

    @Test
    void sendEmptyStopsWriter() throws InterruptedException {
        port = new TestableAsyncCommPort("127.0.0.1", 0);
        port.start(0.5);
        port.send(new byte[0]);
        Thread.sleep(400);
        port.stop();
        assertFalse(port.isRunning());
    }

    @Test
    void setVerifyCrcAndLogging() {
        port = new TestableAsyncCommPort("127.0.0.1", 8080);
        port.setVerifyCRC(false);
        port.setLogging(true);
        assertFalse(port.isVerifyCrc());
        assertTrue(port.isLogging());
    }

    @Test
    void connectedHandlerInvokedWhenConnected() throws Exception {
        mockServer = new ServerSocket(0);
        int portNum = mockServer.getLocalPort();
        CountDownLatch accepted = new CountDownLatch(1);
        Thread serverThread = new Thread(() -> {
            try {
                Socket client = mockServer.accept();
                accepted.countDown();
                client.getInputStream().read();
                client.close();
            } catch (IOException e) {
                // ignore
            }
        }, "mock-server");
        serverThread.setDaemon(true);
        serverThread.start();

        CountDownLatch connected = new CountDownLatch(1);
        port = new TestableAsyncCommPort("127.0.0.1", portNum);
        port.setConnectedHandler(connected::countDown);
        port.start(2.0);
        assertTrue(connected.await(3, TimeUnit.SECONDS));
        assertTrue(accepted.await(1, TimeUnit.SECONDS));
        port.send(new byte[0]);
        Thread.sleep(400);
        port.stop();
    }

    /** Concrete subclass that reads one byte then EOF to end read loop. */
    private static final class TestableAsyncCommPort extends AsyncCommPort {
        TestableAsyncCommPort(String host, int port) {
            super(host, port);
        }

        @Override
        protected void readMsg(InputStream stream) throws IOException {
            int b = stream.read();
            if (b < 0) throw new IOException("eof");
        }
    }
}
