package org.artisan.view;

import javafx.scene.paint.Color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies NotificationLevel enum has INFO, WARNING, ERROR and getColor() returns non-null Color.
 */
class NotificationLevelTest {

    @Test
    void enumValues() {
        assertEquals(3, NotificationLevel.values().length);
        assertNotNull(NotificationLevel.INFO);
        assertNotNull(NotificationLevel.WARNING);
        assertNotNull(NotificationLevel.ERROR);
    }

    @Test
    void getColor_info() {
        Color c = NotificationLevel.INFO.getColor();
        assertNotNull(c);
        assertEquals(0.129, c.getRed(), 0.01);
        assertEquals(0.588, c.getGreen(), 0.01);
        assertEquals(0.953, c.getBlue(), 0.01);
    }

    @Test
    void getColor_warning() {
        Color c = NotificationLevel.WARNING.getColor();
        assertNotNull(c);
    }

    @Test
    void getColor_error() {
        Color c = NotificationLevel.ERROR.getColor();
        assertNotNull(c);
    }
}
