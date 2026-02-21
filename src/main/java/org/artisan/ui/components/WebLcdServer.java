package org.artisan.ui.components;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embedded HTTP server for external Web LCD displays — parity with Python artisanlib/weblcds.py.
 *
 * <p>Python weblcds.py uses aiohttp + WebSocket to serve live roast data to browser clients.
 * This Java implementation uses the JDK built-in {@code com.sun.net.httpserver.HttpServer}
 * with Server-Sent Events (SSE) which provides compatible real-time push without
 * an external dependency.
 *
 * <p>Usage:
 * <pre>
 *   WebLcdServer server = new WebLcdServer(8080);
 *   server.start();
 *   server.broadcast("{\"bt\":200.5,\"et\":220.3,\"ror\":10.2}");
 *   ...
 *   server.stop();
 * </pre>
 *
 * <p>Clients connect to {@code http://host:port/lcd} and receive SSE events.
 * The {@code /} root serves a minimal LCD HTML page.
 */
@SuppressWarnings("restriction")
public final class WebLcdServer {

    private static final Logger LOG = Logger.getLogger(WebLcdServer.class.getName());

    private static final int KEEPALIVE_INTERVAL_SECONDS = 15;
    private static final double MIN_SEND_INTERVAL_SECONDS = 0.03; // parity with Python _min_send_interval

    private final int port;
    private volatile HttpServer httpServer;
    private volatile ScheduledExecutorService keepaliveExecutor;
    private final Set<OutputStream> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private volatile String lastMessage = null;
    private volatile long lastSendNanos = 0;

    public WebLcdServer(int port) {
        this.port = port;
    }

    /** Starts the HTTP server. Non-blocking. */
    public void start() {
        if (httpServer != null) return;
        try {
            HttpServer srv = HttpServer.create(new InetSocketAddress(port), 10);
            srv.createContext("/", this::handleRoot);
            srv.createContext("/lcd", this::handleSse);
            srv.setExecutor(Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "WebLcdServer-http");
                t.setDaemon(true);
                return t;
            }));
            srv.start();
            httpServer = srv;
            LOG.info("WebLcdServer started on port " + port);

            keepaliveExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "WebLcdServer-keepalive");
                t.setDaemon(true);
                return t;
            });
            keepaliveExecutor.scheduleAtFixedRate(
                    this::sendKeepalive, KEEPALIVE_INTERVAL_SECONDS, KEEPALIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "WebLcdServer failed to start", e);
        }
    }

    /** Stops the HTTP server and disconnects all clients. */
    public void stop() {
        if (keepaliveExecutor != null) {
            keepaliveExecutor.shutdownNow();
            keepaliveExecutor = null;
        }
        for (OutputStream os : clients) {
            try { os.close(); } catch (IOException ignored) {}
        }
        clients.clear();
        if (httpServer != null) {
            httpServer.stop(1);
            httpServer = null;
            LOG.info("WebLcdServer stopped");
        }
    }

    public boolean isRunning() {
        return httpServer != null;
    }

    /**
     * Sends a JSON message to all connected SSE clients (rate-limited to MIN_SEND_INTERVAL_SECONDS).
     * Parity with Python WebView.send() / _last_send / _min_send_interval.
     *
     * @param jsonMessage JSON string, e.g. {@code {"bt":200.5,"et":220.0,"ror":10.2}}
     */
    public void broadcast(String jsonMessage) {
        if (jsonMessage == null) return;
        long now = System.nanoTime();
        double elapsed = (now - lastSendNanos) / 1e9;
        if (elapsed < MIN_SEND_INTERVAL_SECONDS) return;
        lastSendNanos = now;
        lastMessage = jsonMessage;
        String sseFrame = "data: " + jsonMessage + "\n\n";
        byte[] bytes = sseFrame.getBytes(StandardCharsets.UTF_8);
        Set<OutputStream> dead = ConcurrentHashMap.newKeySet();
        for (OutputStream os : clients) {
            try {
                os.write(bytes);
                os.flush();
            } catch (IOException e) {
                dead.add(os);
            }
        }
        clients.removeAll(dead);
    }

    // ── HTTP handlers ──────────────────────────────────────────────────────────

    private void handleRoot(HttpExchange exchange) throws IOException {
        String html = buildLcdHtml();
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void handleSse(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        clients.add(os);
        // Send last known state to newly connected client (parity with Python _last_message)
        String last = lastMessage;
        if (last != null) {
            try {
                String sseFrame = "data: " + last + "\n\n";
                os.write(sseFrame.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException e) {
                clients.remove(os);
                return;
            }
        }
        // Block until client disconnects or server stops
        while (clients.contains(os)) {
            try {
                Thread.sleep(500);
                // SSE keepalive comment
                os.write(": keepalive\n\n".getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException e) {
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        clients.remove(os);
        try { os.close(); } catch (IOException ignored) {}
    }

    private void sendKeepalive() {
        byte[] ping = ": ping\n\n".getBytes(StandardCharsets.UTF_8);
        Set<OutputStream> dead = ConcurrentHashMap.newKeySet();
        for (OutputStream os : clients) {
            try {
                os.write(ping);
                os.flush();
            } catch (IOException e) {
                dead.add(os);
            }
        }
        clients.removeAll(dead);
    }

    // ── LCD HTML page ──────────────────────────────────────────────────────────

    private String buildLcdHtml() {
        return "<!DOCTYPE html>\n<html>\n<head>\n" +
            "<meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "<title>Artisan Web LCD</title>\n" +
            "<style>\n" +
            "  body{background:#1a1a2e;color:#00d4ff;font-family:monospace;text-align:center;padding:20px;}\n" +
            "  .lcd{display:inline-block;background:#0a0a1a;border:2px solid #00d4ff;border-radius:8px;\n" +
            "       padding:20px 40px;margin:10px;min-width:150px;}\n" +
            "  .label{font-size:14px;color:#888;margin-bottom:4px;}\n" +
            "  .value{font-size:48px;font-weight:bold;}\n" +
            "  .unit{font-size:16px;color:#888;}\n" +
            "</style>\n</head>\n<body>\n" +
            "<h2 style=\"color:#00d4ff\">Artisan Web LCD</h2>\n" +
            "<div class=\"lcd\"><div class=\"label\">BT</div><div class=\"value\" id=\"bt\">--.-</div><div class=\"unit\">°C</div></div>\n" +
            "<div class=\"lcd\"><div class=\"label\">ET</div><div class=\"value\" id=\"et\">--.-</div><div class=\"unit\">°C</div></div>\n" +
            "<div class=\"lcd\"><div class=\"label\">RoR</div><div class=\"value\" id=\"ror\">--.-</div><div class=\"unit\">°C/min</div></div>\n" +
            "<script>\n" +
            "const src = new EventSource('/lcd');\n" +
            "src.onmessage = function(e) {\n" +
            "  try {\n" +
            "    const d = JSON.parse(e.data);\n" +
            "    if(d.bt !== undefined) document.getElementById('bt').textContent = Number(d.bt).toFixed(1);\n" +
            "    if(d.et !== undefined) document.getElementById('et').textContent = Number(d.et).toFixed(1);\n" +
            "    if(d.ror !== undefined) document.getElementById('ror').textContent = Number(d.ror).toFixed(1);\n" +
            "  } catch(ex) {}\n" +
            "};\n" +
            "</script>\n</body>\n</html>\n";
    }
}
