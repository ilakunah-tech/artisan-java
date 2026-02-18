package org.artisan.util;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

/**
 * Artisan utility functions ported from Python artisanlib.util.
 * Constants, conversions, and pure helpers (no UI dependencies).
 */
public final class Util {

    private Util() {}

    // --- Application constants (from Python) ---
    public static final String APPLICATION_NAME = "AQ+";
    public static final String APPLICATION_VIEWER_NAME = "AQ+ Viewer";
    public static final String APPLICATION_ORGANIZATION_NAME = "artisan-scope";
    public static final String APPLICATION_ORGANIZATION_DOMAIN = "artisan-scope.org";
    public static final String APPLICATION_DESKTOP_FILE_NAME = "org.artisan_scope.artisan";

    /** Delta label prefix for HTML labels (Delta ET/BT). */
    public static final String DELTA_LABEL_PREFIX = "<html>&Delta;&thinsp;</html>";
    /** Delta for non-HTML widgets; Linux uses "Delta", others use Unicode delta + thin space. */
    public static final String DELTA_LABEL_UTF8 =
            isLinux() ? "Delta" : "\u0394\u2009";
    public static final String DELTA_LABEL_BIG_PREFIX = "<big><b>&Delta;</b></big>&thinsp;<big><b>";
    public static final String DELTA_LABEL_MATH_PREFIX = "$\\Delta\\/$";

    /** Weight units: g, Kg, lb, oz. */
    public static final String[] WEIGHT_UNITS = {"g", "Kg", "lb", "oz"};
    public static final String[] WEIGHT_UNITS_LOWER = {"g", "kg", "lb", "oz"};

    // --- Platform / frozen ---

    /**
     * Returns true if the application is running as a frozen executable (e.g. PyInstaller).
     * In Java we approximate: we consider "frozen" when running from a JAR with no class path
     * pointing to source (heuristic: no "target/classes" or "build/classes" in classpath).
     */
    public static boolean appFrozen() {
        String cp = System.getProperty("java.class.path", "");
        return !cp.contains("target" + System.getProperty("file.separator") + "classes")
                && !cp.contains("build" + System.getProperty("file.separator") + "classes");
    }

    private static boolean isLinux() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase(Locale.ROOT).contains("linux");
    }

    // --- Character / encoding ---

    /** Returns the character for the given code point, or empty string if invalid. */
    public static String uchr(int codePoint) {
        if (!Character.isValidCodePoint(codePoint)) {
            return "";
        }
        try {
            return String.valueOf(Character.toChars(codePoint));
        } catch (Exception e) {
            return "";
        }
    }

    /** Combines two bytes as (h1*256 + h2) or returns h1 if h2 is null. */
    public static int hex2int(int h1, Integer h2) {
        if (h2 != null) {
            return (h1 * 256) + h2;
        }
        return h1;
    }

    /** Converts string to ASCII bytes for device communication; non-ASCII ignored. */
    public static byte[] str2cmd(String s) {
        if (s == null) {
            return new byte[0];
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 128) {
                sb.append(c);
            }
        }
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /** Converts bytes from device to string (Latin-1). */
    public static String cmd2str(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    // --- Time formatting ---

    /**
     * Converts seconds to string "mm:ss" or "hh:mm" with "h" separator if over 1 hour.
     *
     * @param secondsRaw time in seconds (or minutes if over 1 hour, then separator is "h")
     * @param leadingZero whether to zero-pad the first segment
     */
    public static String stringfromseconds(double secondsRaw, boolean leadingZero) {
        String sep = ":";
        if (Math.abs(secondsRaw) > 3600) {
            secondsRaw /= 60;
            sep = "h";
        }
        int seconds = (int) Math.floor(secondsRaw + 0.5);
        if (seconds >= 0) {
            int d = seconds / 60;
            int m = seconds % 60;
            if (leadingZero) {
                return String.format("%02d%s%02d", d, sep, m);
            }
            return String.format("%d%s%02d", d, sep, m);
        }
        int neg = Math.abs(seconds);
        int d = neg / 60;
        int m = neg % 60;
        if (leadingZero) {
            return String.format("-%02d%s%02d", d, sep, m);
        }
        return String.format("-%d%s%02d", d, sep, m);
    }

    public static String stringfromseconds(double secondsRaw) {
        return stringfromseconds(secondsRaw, true);
    }

    /**
     * Parses time string "mm:ss" or "hh:mm" (with "h") or "-mm:ss" / "-hh:mm" into seconds.
     *
     * @throws IllegalArgumentException if format is invalid
     */
    public static int stringtoseconds(String string) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException("Time string is null or blank");
        }
        String s = string.trim();
        String[] parts = s.split(":");
        boolean hours = false;
        if (parts.length != 2) {
            parts = s.split("h");
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Time string must be xx:xx or -xx:xx or xxhxx or -xxhxx: " + string);
            }
            hours = true;
        }
        if (parts[0].startsWith("-")) {
            int first = Integer.parseInt(parts[0].trim());
            int second = Integer.parseInt(parts[1].trim());
            int sec = first * 60 - second;
            if (hours) {
                sec *= 60;
            }
            return sec;
        }
        int first = Integer.parseInt(parts[0].trim());
        int second = Integer.parseInt(parts[1].trim());
        int sec = second + first * 60;
        if (hours) {
            sec *= 60;
        }
        return sec;
    }

    // --- Temperature and RoR conversions ---

    public static double fromFtoCstrict(double f) {
        if (f == -1) {
            return f;
        }
        return (f - 32.0) * (5.0 / 9.0);
    }

    public static Double fromFtoC(Double f) {
        if (f == null || f == -1 || Double.isNaN(f)) {
            return f;
        }
        return fromFtoCstrict(f);
    }

    public static double fromCtoFstrict(double c) {
        if (c == -1) {
            return c;
        }
        return (c * 9.0 / 5.0) + 32.0;
    }

    public static Double fromCtoF(Double c) {
        if (c == null || c == -1 || Double.isNaN(c)) {
            return c;
        }
        return fromCtoFstrict(c);
    }

    public static double RoRfromCtoFstrict(double cRoR) {
        if (cRoR == -1) {
            return cRoR;
        }
        return cRoR * 9.0 / 5.0;
    }

    public static Double RoRfromCtoF(Double cRoR) {
        if (cRoR == null || cRoR == -1 || Double.isNaN(cRoR)) {
            return cRoR;
        }
        return RoRfromCtoFstrict(cRoR);
    }

    public static double RoRfromFtoCstrict(double fRoR) {
        if (fRoR == -1) {
            return fRoR;
        }
        return fRoR * (5.0 / 9.0);
    }

    public static Double RoRfromFtoC(Double fRoR) {
        if (fRoR == null || fRoR == -1 || Double.isNaN(fRoR)) {
            return fRoR;
        }
        return RoRfromFtoCstrict(fRoR);
    }

    public static Double convertRoR(Double r, char sourceUnit, char targetUnit) {
        if (sourceUnit == targetUnit) {
            return r;
        }
        if (sourceUnit == 'C') {
            return RoRfromCtoF(r);
        }
        return RoRfromFtoC(r);
    }

    public static double convertRoRstrict(double r, char sourceUnit, char targetUnit) {
        if (sourceUnit == targetUnit) {
            return r;
        }
        if (sourceUnit == 'C') {
            return RoRfromCtoFstrict(r);
        }
        return RoRfromFtoCstrict(r);
    }

    public static double convertTemp(double t, String sourceUnit, String targetUnit) {
        if (sourceUnit == null || sourceUnit.isEmpty() || sourceUnit.equals(targetUnit) || targetUnit == null || targetUnit.isEmpty()) {
            return t;
        }
        if ("C".equals(sourceUnit)) {
            return fromCtoFstrict(t);
        }
        return fromFtoCstrict(t);
    }

    // --- Type conversions (toInt, toFloat, toBool, etc.) ---

    public static int toInt(Object x) {
        if (x == null) {
            return 0;
        }
        try {
            if (x instanceof Number) {
                return (int) Math.round(((Number) x).doubleValue());
            }
            return (int) Math.round(Double.parseDouble(Objects.toString(x).trim()));
        } catch (Exception e) {
            return 0;
        }
    }

    public static double toFloat(Object x) {
        if (x == null) {
            return 0.0;
        }
        try {
            if (x instanceof Number) {
                return ((Number) x).doubleValue();
            }
            return Double.parseDouble(Objects.toString(x).trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static boolean toBool(Object x) {
        if (x == null) {
            return false;
        }
        if (x instanceof String) {
            String s = ((String) x).trim().toLowerCase(Locale.ROOT);
            if (s.equals("yes") || s.equals("true") || s.equals("t") || s.equals("1")) {
                return true;
            }
            if (s.equals("no") || s.equals("false") || s.equals("f") || s.equals("0")) {
                return false;
            }
        }
        if (x instanceof Boolean) {
            return (Boolean) x;
        }
        if (x instanceof Number) {
            return ((Number) x).doubleValue() != 0;
        }
        return Boolean.parseBoolean(Objects.toString(x));
    }

    /** Returns true if value is a valid temperature (not null, not NaN, not -1 or 0 or infinity). */
    public static boolean isProperTemp(Double x) {
        if (x == null || Double.isNaN(x)) {
            return false;
        }
        return x != 0 && x != -1 && x != Double.NEGATIVE_INFINITY && x != Double.POSITIVE_INFINITY;
    }

    /** Abbreviates string to max length ll, appending Unicode ellipsis. */
    public static String abbrevString(String s, int ll) {
        if (s == null) {
            return "";
        }
        if (s.length() > ll) {
            return s.substring(0, Math.max(0, ll - 1)) + "\u2026";
        }
        return s;
    }

    /**
     * Normalizes decimal separator: last comma or dot becomes decimal separator;
     * others removed. Trailing dot/comma removed.
     */
    public static String comma2dot(String s) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        int lastDot = s.lastIndexOf('.');
        int lastComma = s.lastIndexOf(',');
        if (lastDot >= 0 && (lastComma < 0 || lastDot > lastComma)) {
            if (lastDot + 1 == s.length()) {
                return s.replace(",", "").replace(".", "");
            }
            String before = s.substring(0, lastDot).replace(",", "").replace(".", "");
            String after = s.substring(lastDot).replace(",", "").replaceAll("0+$", "").replaceAll("\\.$", "");
            return before + after;
        }
        if (lastComma >= 0) {
            if (lastComma + 1 == s.length()) {
                return s.replace(",", "").replace(".", "");
            }
            String before = s.substring(0, lastComma).replace(",", "").replace(".", "");
            String after = s.substring(lastComma + 1).replaceAll("0+$", "").replaceAll("\\.$", "");
            return before + "." + after;
        }
        return s;
    }

    /**
     * Formats number with adaptive decimal places: 0, 0.999, 9.99, 99.9, 999.
     */
    public static String scaleFloat2String(double num) {
        double n = num;
        if (n == 0) {
            return "0";
        }
        double abs = Math.abs(n);
        if (abs < 10) {
            String f = String.format(Locale.ROOT, "%.3f", n);
            return f.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        if (abs >= 1000) {
            return String.format(Locale.ROOT, "%.0f", n);
        }
        if (abs >= 100) {
            String f = String.format(Locale.ROOT, "%.1f", n);
            return f.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        String f = String.format(Locale.ROOT, "%.2f", n);
        return f.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public static String scaleFloat2String(Object num) {
        return scaleFloat2String(toFloat(num));
    }
}
