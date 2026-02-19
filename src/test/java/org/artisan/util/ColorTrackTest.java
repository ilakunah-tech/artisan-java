package org.artisan.util;

import javafx.scene.paint.Color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorTrackTest {

    @Test
    void fromAgtron0_returnsVeryDarkColor() {
        Color c = ColorTrack.fromAgtron(0);
        assertNotNull(c);
        assertTrue(c.getRed() < 0.2, "red should be dark");
    }

    @Test
    void fromAgtron100_returnsLightColor() {
        Color c = ColorTrack.fromAgtron(100);
        assertNotNull(c);
        assertTrue(c.getRed() > 0.7, "red should be light");
    }

    @Test
    void fromAgtron50_returnsMidBrown() {
        Color c = ColorTrack.fromAgtron(50);
        assertNotNull(c);
    }

    @Test
    void fromAgtronNegative_clampsTo0Range() {
        assertDoesNotThrow(() -> {
            Color c = ColorTrack.fromAgtron(-10);
            assertNotNull(c);
        });
    }

    @Test
    void toHex_returns7CharStringStartingWithHash() {
        Color c = Color.web("#FF0000");
        String hex = ColorTrack.toHex(c);
        assertNotNull(hex);
        assertTrue(hex.startsWith("#"));
        assertEquals(7, hex.length());
    }

    @Test
    void toHex_nullReturnsDefault() {
        String hex = ColorTrack.toHex(null);
        assertNotNull(hex);
        assertTrue(hex.startsWith("#"));
    }

    @Test
    void clamp() {
        assertEquals(0.0, ColorTrack.clamp(-5, 0, 100), 1e-9);
        assertEquals(100.0, ColorTrack.clamp(150, 0, 100), 1e-9);
        assertEquals(50.0, ColorTrack.clamp(50, 0, 100), 1e-9);
    }
}
