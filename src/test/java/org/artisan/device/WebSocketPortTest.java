package org.artisan.device;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link WebSocketPort} with mock (no real WebSocket server required for basic tests).
 */
class WebSocketPortTest {

    private WebSocketPort port;

    @AfterEach
    void tearDown() {
        if (port != null && port.isRunning()) {
            port.stop();
        }
    }

    @Test
    void stopWithoutStartDoesNotThrow() {
        port = new WebSocketPort("127.0.0.1", 80);
        port.stop();
        assertFalse(port.isRunning());
    }

    @Test
    void isConnectedFalseWhenNotStarted() {
        port = new WebSocketPort("127.0.0.1", 8080);
        assertFalse(port.isConnected());
        assertFalse(port.isRunning());
    }

    @Test
    void sendWhenNotConnectedBlockReturnsNull() {
        port = new WebSocketPort("127.0.0.1", 80);
        JsonNode result = port.send(Map.of("command", "getData"), true);
        assertNull(result);
    }

    @Test
    void startSetsRunning() {
        port = new WebSocketPort("127.0.0.1", 65535);
        port.start();
        assertTrue(port.isRunning());
        port.stop();
        assertFalse(port.isRunning());
    }
}
