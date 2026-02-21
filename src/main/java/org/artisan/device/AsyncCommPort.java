package org.artisan.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic async communication port (TCP or serial).
 * Uses a dedicated executor thread for the connection loop and a shared pool for read/write.
 * Equivalent to Python artisanlib.async_comm.AsyncComm.
 */
public abstract class AsyncCommPort {

    private static final Logger LOG = Logger.getLogger(AsyncCommPort.class.getName());

    private final String host;
    private final int port;
    private final SerialSettings serialSettings;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<byte[]> writeQueue = new LinkedBlockingQueue<>();
    private volatile ExecutorService loopExecutor;
    private volatile ExecutorService ioExecutor;
    private volatile Socket socket;
    private volatile InputStream inputStream;
    private volatile OutputStream outputStream;

    private Runnable connectedHandler;
    private Runnable disconnectedHandler;
    private boolean verifyCrc = true;
    private boolean logging = false;

    protected AsyncCommPort(String host, int port) {
        this.host = host;
        this.port = port;
        this.serialSettings = null;
    }

    protected AsyncCommPort(SerialSettings serialSettings) {
        this.host = null;
        this.port = -1;
        this.serialSettings = serialSettings;
    }

    public void setVerifyCRC(boolean b) {
        this.verifyCrc = b;
    }

    public void setLogging(boolean b) {
        this.logging = b;
    }

    public void setConnectedHandler(Runnable connectedHandler) {
        this.connectedHandler = connectedHandler;
    }

    public void setDisconnectedHandler(Runnable disconnectedHandler) {
        this.disconnectedHandler = disconnectedHandler;
    }

    public boolean isRunning() {
        return running.get();
    }

    /**
     * Hook for subclasses to reset internal readings. Default no-op.
     */
    protected void resetReadings() {
    }

    /**
     * Hook for subclasses to handle one message from the stream. Default no-op.
     */
    protected void readMsg(InputStream stream) throws IOException {
    }

    /**
     * Starts the connection loop in a background thread. Connects to host:port or serial.
     */
    public void start(double connectTimeoutSeconds) {
        if (running.getAndSet(true)) {
            return;
        }
        loopExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "AsyncCommPort-loop");
            t.setDaemon(true);
            return t;
        });
        ioExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "AsyncCommPort-io");
            t.setDaemon(true);
            return t;
        });
        final int connectTimeoutMs = (int) (connectTimeoutSeconds * 1000);
        loopExecutor.submit(() -> connectLoop(connectTimeoutMs));
    }

    /**
     * Stops the connection loop and disconnects.
     */
    public void stop() {
        running.set(false);
        send(new byte[0]);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        loopExecutor = shutdown(loopExecutor);
        ioExecutor = shutdown(ioExecutor);
        writeQueue.clear();
        resetReadings();
    }

    private static ExecutorService shutdown(ExecutorService es) {
        if (es == null) return null;
        es.shutdown();
        try {
            if (!es.awaitTermination(2, TimeUnit.SECONDS)) {
                es.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            es.shutdownNow();
        }
        return null;
    }

    private void connectLoop(int connectTimeoutMs) {
        while (running.get()) {
            try {
                if (serialSettings != null) {
                    connectSerial();
                } else {
                    connectTcp(connectTimeoutMs);
                }
            } catch (IOException e) {
                if (logging) {
                    LOG.log(Level.FINE, "AsyncCommPort connection error", e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            resetReadings();
            if (disconnectedHandler != null) {
                try {
                    disconnectedHandler.run();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (!running.get()) break;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void connectTcp(int connectTimeoutMs) throws IOException, InterruptedException {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), connectTimeoutMs);
            this.socket = s;
            this.inputStream = s.getInputStream();
            this.outputStream = s.getOutputStream();
            if (connectedHandler != null) {
                try {
                    connectedHandler.run();
                } catch (Exception e) {
                    // ignore
                }
            }
            CompletableFuture<?> readFuture = CompletableFuture.runAsync(this::readLoop, ioExecutor);
            CompletableFuture<?> writeFuture = CompletableFuture.runAsync(this::writeLoop, ioExecutor);
            CompletableFuture.anyOf(readFuture, writeFuture).join();
            readFuture.cancel(true);
            writeFuture.cancel(true);
        } finally {
            closeStreams();
        }
    }

    private void connectSerial() throws IOException, InterruptedException {
        if (serialSettings == null) return;
        com.fazecast.jSerialComm.SerialPort serialPort = com.fazecast.jSerialComm.SerialPort.getCommPort(serialSettings.getPort());
        serialPort.setBaudRate(serialSettings.getBaudrate());
        serialPort.setNumDataBits(serialSettings.getBytesize());
        int stop = serialSettings.getStopbits();
        serialPort.setNumStopBits(stop == 2 ? com.fazecast.jSerialComm.SerialPort.TWO_STOP_BITS : com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT);
        String p = serialSettings.getParity();
        int parity = com.fazecast.jSerialComm.SerialPort.NO_PARITY;
        if (p != null && !p.isEmpty()) {
            switch (p.toUpperCase().charAt(0)) {
                case 'E': parity = com.fazecast.jSerialComm.SerialPort.EVEN_PARITY; break;
                case 'O': parity = com.fazecast.jSerialComm.SerialPort.ODD_PARITY; break;
                default: break;
            }
        }
        serialPort.setParity(parity);
        serialPort.openPort();
        try {
            this.inputStream = serialPort.getInputStream();
            this.outputStream = serialPort.getOutputStream();
            if (connectedHandler != null) {
                try {
                    connectedHandler.run();
                } catch (Exception e) {
                    // ignore
                }
            }
            CompletableFuture<?> readFuture = CompletableFuture.runAsync(this::readLoop, ioExecutor);
            CompletableFuture<?> writeFuture = CompletableFuture.runAsync(this::writeLoop, ioExecutor);
            CompletableFuture.anyOf(readFuture, writeFuture).join();
            readFuture.cancel(true);
            writeFuture.cancel(true);
        } finally {
            serialPort.closePort();
            closeStreams();
        }
    }

    private void closeStreams() {
        this.socket = null;
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ignored) {
        }
        inputStream = null;
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException ignored) {
        }
        outputStream = null;
    }

    private void readLoop() {
        try {
            InputStream in = this.inputStream;
            while (in != null && running.get()) {
                readMsg(in);
            }
        } catch (IOException e) {
            if (logging && running.get()) {
                LOG.log(Level.FINE, "AsyncCommPort read error", e);
            }
        }
    }

    private void writeLoop() {
        try {
            OutputStream out = this.outputStream;
            while (out != null && running.get()) {
                byte[] message = writeQueue.take();
                if (message.length == 0) {
                    break;
                }
                if (logging) {
                    LOG.fine("AsyncCommPort write: " + Arrays.toString(message));
                }
                out.write(message);
                out.flush();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            if (logging) {
                LOG.log(Level.FINE, "AsyncCommPort write error", e);
            }
        }
    }

    /**
     * Sends a message on the write queue. Safe to call from any thread.
     * Sending an empty array signals the writer to close the connection.
     */
    public void send(byte[] message) {
        if (message == null) {
            message = new byte[0];
        }
        writeQueue.offer(message);
    }

    public boolean isVerifyCrc() {
        return verifyCrc;
    }

    public boolean isLogging() {
        return logging;
    }
}
