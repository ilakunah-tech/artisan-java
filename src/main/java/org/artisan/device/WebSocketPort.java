package org.artisan.device;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CompletionStage;

/**
 * WebSocket client port using Java 11+ HttpClient WebSocket API.
 * Equivalent to Python artisanlib.wsport.
 */
public class WebSocketPort {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 80;
    private static final String DEFAULT_PATH = "WebSocket";
    private static final String ID_NODE = "id";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final String host;
    private final int port;
    private final String path;
    private final long connectTimeoutSeconds;
    private final long requestTimeoutSeconds;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<Integer, CompletableFuture<JsonNode>> pendingRequests = new ConcurrentHashMap<>();
    private final AtomicInteger messageIdGen = new AtomicInteger(1);

    private volatile WebSocket webSocket;
    private volatile ExecutorService executor;
    private HttpClient httpClient;

    public WebSocketPort(String host, int port, String path, double connectTimeoutSeconds, double requestTimeoutSeconds) {
        this.host = host;
        this.port = port;
        this.path = path.startsWith("/") ? path : "/" + path;
        this.connectTimeoutSeconds = (long) Math.ceil(connectTimeoutSeconds);
        this.requestTimeoutSeconds = (long) Math.ceil(requestTimeoutSeconds * 1000);
    }

    public WebSocketPort(String host, int port) {
        this(host, port, DEFAULT_PATH, 4.0, 0.5);
    }

    public void start() {
        if (running.getAndSet(true)) {
            return;
        }
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
        executor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "WebSocketPort-worker");
            t.setDaemon(true);
            return t;
        });
        executor.submit(this::connectLoop);
        executor.submit(this::producerLoop);
    }

    public void stop() {
        running.set(false);
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "stop");
            webSocket = null;
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
            executor = null;
        }
        writeQueue.clear();
        pendingRequests.values().forEach(f -> f.cancel(false));
        pendingRequests.clear();
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isConnected() {
        return webSocket != null && !webSocket.isOutputClosed();
    }

    /**
     * Sends a request as JSON. If block is true, waits for a response with matching id (up to requestTimeout).
     *
     * @param request request map (will be augmented with "id" and optionally "roasterID")
     * @param block   if true, block until response or timeout
     * @return response as JsonNode, or null on timeout/error
     */
    public JsonNode send(Map<String, Object> request, boolean block) {
        if (!running.get()) {
            start();
        }
        if (block && (webSocket == null || webSocket.isOutputClosed())) {
            return null;
        }
        try {
            int messageId = messageIdGen.getAndIncrement();
            @SuppressWarnings("unchecked")
            Map<String, Object> mutable = request instanceof ObjectNode
                    ? JSON.convertValue(request, Map.class)
                    : new java.util.HashMap<>(request);
            mutable.put(ID_NODE, messageId);

            String json = JSON.writeValueAsString(mutable);
            CompletableFuture<JsonNode> future = null;
            if (block) {
                future = new CompletableFuture<>();
                pendingRequests.put(messageId, future);
            }
            writeQueue.offer(json);
            if (block && future != null) {
                try {
                    return future.get(requestTimeoutSeconds, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    pendingRequests.remove(messageId);
                    return null;
                }
            }
        } catch (JsonProcessingException e) {
            throw new CommException("WebSocket send JSON failed", e);
        }
        return null;
    }

    /**
     * Sends text message (no response wait).
     */
    public void sendText(String text) {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendText(text, true);
        }
    }

    private void connectLoop() {
        while (running.get()) {
            try {
                String authority = (port == 80 || port == 443) ? host : host + ":" + port;
                String scheme = port == 443 ? "wss" : "ws";
                URI uri = URI.create(scheme + "://" + authority + path);
                CompletableFuture<WebSocket> cf = httpClient.newWebSocketBuilder()
                        .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                        .buildAsync(uri, new Listener());
                webSocket = cf.get(connectTimeoutSeconds + 2, TimeUnit.SECONDS);
            } catch (Exception e) {
                webSocket = null;
            }
            if (!running.get()) break;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void producerLoop() {
        try {
            while (running.get()) {
                String msg = writeQueue.poll(100, TimeUnit.MILLISECONDS);
                if (msg != null && webSocket != null && !webSocket.isOutputClosed()) {
                    webSocket.sendText(msg, true);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void onMessage(String text) {
        try {
            JsonNode node = JSON.readTree(text);
            JsonNode idNode = node.get(ID_NODE);
            if (idNode != null && idNode.isInt()) {
                int id = idNode.asInt();
                CompletableFuture<JsonNode> f = pendingRequests.remove(id);
                if (f != null) {
                    f.complete(node);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private class Listener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            onMessage(data.toString());
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
        }
    }
}
