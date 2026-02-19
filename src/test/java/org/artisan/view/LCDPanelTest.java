package org.artisan.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests LCDPanel value formatting logic via ValueFormatter (headless-safe, no JavaFX rendering).
 */
class LCDPanelTest {

    @Test
    void format_oneDecimal() {
        assertEquals("123.5", ValueFormatter.format(123.456, 1));
    }

    @Test
    void format_zero() {
        assertEquals("0.0", ValueFormatter.format(0.0, 1));
    }

    @Test
    void format_nan() {
        assertEquals("–––", ValueFormatter.format(Double.NaN, 1));
    }

    @Test
    void format_positiveInfinity() {
        assertEquals("–––", ValueFormatter.format(Double.POSITIVE_INFINITY, 1));
    }

    @Test
    void format_negativeInfinity() {
        assertEquals("–––", ValueFormatter.format(Double.NEGATIVE_INFINITY, 1));
    }

    @Test
    void format_twoDecimals() {
        assertEquals("123.46", ValueFormatter.format(123.456, 2));
    }
}
