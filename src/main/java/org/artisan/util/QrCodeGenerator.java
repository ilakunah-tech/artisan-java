package org.artisan.util;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * Utility for generating QR codes via ZXing.
 * Supports JavaFX WritableImage and PNG file output.
 */
public final class QrCodeGenerator {

    private static final Logger LOG = Logger.getLogger(QrCodeGenerator.class.getName());

    private static final int ON_PIXEL = 0xFF_00_00_00;  // black
    private static final int OFF_PIXEL = 0xFF_FF_FF_FF; // white

    private QrCodeGenerator() {}

    /**
     * Encodes content as a QR code and renders to a JavaFX WritableImage of the given size.
     * Pixels: black (0xFF000000) for 1-bits, white (0xFFFFFFFF) for 0-bits.
     *
     * @param content text to encode (empty string causes ZXing to reject and returns null)
     * @param size    width and height in pixels
     * @return WritableImage of size×size, or null on failure (logs warning)
     */
    public static WritableImage generateQrFx(String content, int size) {
        if (content == null) content = "";
        if (size <= 0) {
            LOG.warning("generateQrFx: size must be positive");
            return null;
        }
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            int w = matrix.getWidth();
            int h = matrix.getHeight();
            WritableImage image = new WritableImage(w, h);
            PixelWriter pw = image.getPixelWriter();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    pw.setArgb(x, y, matrix.get(x, y) ? ON_PIXEL : OFF_PIXEL);
                }
            }
            return image;
        } catch (WriterException e) {
            LOG.log(Level.WARNING, "QR encode failed: " + e.getMessage(), e);
            return null;
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "QR encode failed: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generates a QR code for the content and writes a PNG file to the given path.
     * Does nothing on failure (logs warning).
     *
     * @param content    text to encode
     * @param size       image width/height in pixels
     * @param outputPath path for the PNG file
     */
    public static void saveQrPng(String content, int size, Path outputPath) {
        if (content == null) content = "";
        if (size <= 0 || outputPath == null) {
            LOG.warning("saveQrPng: size must be positive and outputPath non-null");
            return;
        }
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            MatrixToImageWriter.writeToPath(matrix, "PNG", outputPath);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "QR save PNG failed: " + e.getMessage(), e);
        }
    }
}
