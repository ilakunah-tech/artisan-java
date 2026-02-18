package org.artisan.util;

import org.junit.jupiter.api.Test;

import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SuppressStdoutStderrTest {

    @Test
    void redirectsAndRestoresStreams() {
        PrintStream outBefore = System.out;
        PrintStream errBefore = System.err;

        try (SuppressStdoutStderr ignored = new SuppressStdoutStderr()) {
            assertNotEquals(outBefore, System.out);
            assertNotEquals(errBefore, System.err);
            System.out.println("suppressed");
            System.err.println("suppressed");
        }

        assertEquals(outBefore, System.out);
        assertEquals(errBefore, System.err);
    }
}

