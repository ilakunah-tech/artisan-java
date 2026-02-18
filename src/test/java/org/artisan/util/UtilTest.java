package org.artisan.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Util}.
 */
class UtilTest {

    @Test
    void applicationConstants() {
        assertEquals("AQ+", Util.APPLICATION_NAME);
        assertEquals("AQ+ Viewer", Util.APPLICATION_VIEWER_NAME);
        assertEquals("artisan-scope", Util.APPLICATION_ORGANIZATION_NAME);
        assertNotNull(Util.DELTA_LABEL_PREFIX);
        assertNotNull(Util.WEIGHT_UNITS);
        assertEquals(4, Util.WEIGHT_UNITS.length);
    }

    @Test
    void uchr() {
        assertEquals("A", Util.uchr(65));
        assertEquals("", Util.uchr(-1));
        assertEquals("\u0394", Util.uchr(916));
    }

    @Test
    void hex2int() {
        assertEquals(0x0102, Util.hex2int(1, 2));
        assertEquals(1, Util.hex2int(1, null));
    }

    @Test
    void str2cmdAndCmd2str() {
        byte[] bytes = Util.str2cmd("Hello");
        assertEquals("Hello", Util.cmd2str(bytes));
        // Non-ASCII stripped; "café" -> "caf"
        byte[] cafe = Util.str2cmd("café");
        assertEquals(3, cafe.length);
        assertEquals("caf", Util.cmd2str(cafe));
    }

    @Test
    void stringfromseconds() {
        assertEquals("00:30", Util.stringfromseconds(30, true));
        assertEquals("01:00", Util.stringfromseconds(60, true));
        assertEquals("1:00", Util.stringfromseconds(60, false));
        assertEquals("-01:00", Util.stringfromseconds(-60, true));
        // Exactly 3600 stays as mm:ss ("60:00"); over 3600 uses "h" (e.g. 7200 -> "02h00")
        assertEquals("60:00", Util.stringfromseconds(3600.0, true));
        assertEquals("02h00", Util.stringfromseconds(7200.0, true));
    }

    @Test
    void stringtoseconds() {
        assertEquals(90, Util.stringtoseconds("01:30"));
        assertEquals(90, Util.stringtoseconds("1:30"));
        assertEquals(5400, Util.stringtoseconds("01h30")); // 1h 30min = 90 min = 5400 s
        assertEquals(-90, Util.stringtoseconds("-01:30"));
        assertThrows(IllegalArgumentException.class, () -> Util.stringtoseconds("invalid"));
        assertThrows(IllegalArgumentException.class, () -> Util.stringtoseconds(""));
    }

    @Test
    void fromFtoCstrictAndFromCtoFstrict() {
        assertEquals(0.0, Util.fromFtoCstrict(32.0), 1e-6);
        assertEquals(100.0, Util.fromCtoFstrict(37.777777), 0.1);
        assertEquals(-1, Util.fromFtoCstrict(-1), 0);
        assertEquals(-1, Util.fromCtoFstrict(-1), 0);
    }

    @Test
    void fromFtoCAndFromCtoF() {
        assertNull(Util.fromFtoC(null));
        assertEquals(-1, Util.fromFtoC(-1.0));
        assertNull(Util.fromCtoF(null));
        assertEquals(89.6, Util.fromCtoF(32.0), 0.1);
    }

    @Test
    void RoRConversions() {
        assertEquals(9.0, Util.RoRfromCtoFstrict(5.0), 1e-6);
        assertEquals(25.0 / 9.0, Util.RoRfromFtoCstrict(5.0), 1e-6);
        // 5 C/min -> 9 F/min
        assertEquals(9.0, Util.convertRoRstrict(5.0, 'C', 'F'), 0.01);
    }

    @Test
    void convertTemp() {
        assertEquals(32.0, Util.convertTemp(0, "C", "F"), 0.01);
        assertEquals(0.0, Util.convertTemp(32, "F", "C"), 0.01);
        assertEquals(100.0, Util.convertTemp(100, "C", "C"));
    }

    @Test
    void toInt() {
        assertEquals(0, Util.toInt(null));
        assertEquals(42, Util.toInt(42));
        assertEquals(43, Util.toInt(42.7)); // round
        assertEquals(42, Util.toInt("42"));
        assertEquals(0, Util.toInt("x"));
    }

    @Test
    void toFloat() {
        assertEquals(0.0, Util.toFloat(null));
        assertEquals(42.5, Util.toFloat(42.5));
        assertEquals(42.5, Util.toFloat("42.5"));
    }

    @Test
    void toBool() {
        assertFalse(Util.toBool(null));
        assertTrue(Util.toBool(true));
        assertTrue(Util.toBool("yes"));
        assertTrue(Util.toBool("true"));
        assertFalse(Util.toBool("no"));
        assertFalse(Util.toBool("false"));
    }

    @Test
    void isProperTemp() {
        assertFalse(Util.isProperTemp(null));
        assertFalse(Util.isProperTemp(Double.NaN));
        assertFalse(Util.isProperTemp(-1.0));
        assertFalse(Util.isProperTemp(0.0));
        assertTrue(Util.isProperTemp(100.0));
    }

    @Test
    void abbrevString() {
        assertEquals("Hi", Util.abbrevString("Hi", 5));
        assertEquals("Hel\u2026", Util.abbrevString("Hello", 4));
    }

    @Test
    void comma2dot() {
        assertEquals("1.5", Util.comma2dot("1,5"));
        assertEquals("1.5", Util.comma2dot("1.5"));
        assertEquals("1234.56", Util.comma2dot("1,234.56"));
    }

    @Test
    void scaleFloat2String() {
        assertEquals("0", Util.scaleFloat2String(0));
        assertEquals("1.5", Util.scaleFloat2String(1.5));
        assertEquals("999.4", Util.scaleFloat2String(999.4)); // abs < 1000 -> one decimal
        assertEquals("1000", Util.scaleFloat2String(1000.0));
    }
}
