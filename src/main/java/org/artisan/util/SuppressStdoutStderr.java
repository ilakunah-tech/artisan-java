package org.artisan.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * A context-like helper to suppress {@code System.out} and {@code System.err}.
 *
 * <p>Python reference: {@code artisanlib.suppress_errors.suppress_stdout_stderr}.
 *
 * <p>Note: unlike the Python implementation that can suppress native writes to file descriptors,
 * this implementation only redirects Java-level {@link System#out} and {@link System#err}.
 */
public final class SuppressStdoutStderr implements AutoCloseable {

    private final PrintStream originalOut;
    private final PrintStream originalErr;

    public SuppressStdoutStderr() {
        this.originalOut = System.out;
        this.originalErr = System.err;

        PrintStream nullStream =
                new PrintStream(OutputStream.nullOutputStream(), false, StandardCharsets.UTF_8);

        System.setOut(nullStream);
        System.setErr(nullStream);
    }

    @Override
    public void close() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}

