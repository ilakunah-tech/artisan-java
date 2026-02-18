package org.artisan.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ColorConfigTest {

    @Test
    void allEventColorsAreSetAndNotNull() {
        ColorConfig light = new ColorConfig(ColorConfig.Theme.LIGHT);
        for (EventType e : EventType.values()) {
            Color c = light.getEventColor(e);
            assertNotNull(c, "Event color for " + e + " must not be null");
        }
        ColorConfig dark = new ColorConfig(ColorConfig.Theme.DARK);
        for (EventType e : EventType.values()) {
            Color c = dark.getEventColor(e);
            assertNotNull(c, "Event color for " + e + " must not be null");
        }
    }

    @Test
    void darkThemeDiffersFromLightTheme() {
        ColorConfig light = new ColorConfig(ColorConfig.Theme.LIGHT);
        ColorConfig dark = new ColorConfig(ColorConfig.Theme.DARK);
        assertNotEquals(light.getCurveBT(), dark.getCurveBT(), "curve BT should differ between themes");
        assertNotEquals(light.getCurveET(), dark.getCurveET(), "curve ET should differ between themes");
        assertNotEquals(light.getEventColor(EventType.CHARGE), dark.getEventColor(EventType.CHARGE), "CHARGE event color should differ between themes");
    }

    @Test
    void hexStringParsedToColor() {
        Color c = ColorConfig.fromHex("#ff0000");
        assertNotNull(c);
        assertEquals(1.0, c.getRed(), 0.01);
        assertEquals(0.0, c.getGreen(), 0.01);
        assertEquals(0.0, c.getBlue(), 0.01);
        Color c2 = ColorConfig.fromHex("#0a5c90");
        assertNotNull(c2);
        assertEquals(0.04, c2.getRed(), 0.01);
        assertEquals(0.36, c2.getGreen(), 0.01);
        assertEquals(0.56, c2.getBlue(), 0.01);
    }

    @Test
    void namedColorDarkredMapsToColor() {
        Color c = ColorConfig.fromHex("darkred");
        assertNotNull(c);
        assertEquals(Color.DARKRED.getRed(), c.getRed(), 0.01);
        assertEquals(Color.DARKRED.getGreen(), c.getGreen(), 0.01);
        assertEquals(Color.DARKRED.getBlue(), c.getBlue(), 0.01);
    }

    @Test
    void curveColorsAreSet() {
        ColorConfig config = new ColorConfig();
        assertNotNull(config.getCurveBT());
        assertNotNull(config.getCurveET());
        assertNotNull(config.getCurveRoR());
        assertNotNull(config.getCurveDeltaBT());
        assertNotNull(config.getCurveDeltaET());
    }

    @Test
    void setThemeUpdatesColors() {
        ColorConfig config = new ColorConfig(ColorConfig.Theme.LIGHT);
        Color etLight = config.getCurveET();
        config.setTheme(ColorConfig.Theme.DARK);
        assertNotEquals(etLight, config.getCurveET());
        assertEquals(ColorConfig.Theme.DARK, config.getTheme());
    }
}
