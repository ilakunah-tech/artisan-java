package org.artisan.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.scene.image.WritableImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for QrCodeGenerator (ZXing-based QR generation).
 */
class QrCodeGeneratorTest {

    @Test
    void generateQrFx_returnsImage() {
        WritableImage img = QrCodeGenerator.generateQrFx("https://example.com", 200);
        assertNotNull(img);
        assertEquals(200, (int) img.getWidth());
        assertEquals(200, (int) img.getHeight());
    }

    @Test
    void generateQrFx_emptyString() {
        WritableImage img = QrCodeGenerator.generateQrFx("", 100);
        assertNull(img);
    }

    @Test
    void saveQrPng_createsFile(@TempDir Path dir) throws IOException {
        Path out = dir.resolve("qrtest.png");
        QrCodeGenerator.saveQrPng("https://example.com", 150, out);
        assertTrue(Files.isRegularFile(out));
        assertTrue(Files.size(out) > 0);
    }
}
