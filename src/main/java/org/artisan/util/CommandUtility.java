package org.artisan.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility for running external shell commands (used by future integrations).
 */
public final class CommandUtility {

    private static final ExecutorService EXEC = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "CommandUtility-worker");
        t.setDaemon(true);
        return t;
    });

    private CommandUtility() {}

    /**
     * Runs the command asynchronously, captures stdout and stderr, returns CommandResult.
     */
    public static CompletableFuture<CommandResult> runAsync(String... command) {
        return CompletableFuture.supplyAsync(() -> {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            try {
                Process p = pb.start();
                String stdout = readFully(p.getInputStream());
                String stderr = readFully(p.getErrorStream());
                int exitCode = p.waitFor();
                return new CommandResult(exitCode, stdout, stderr);
            } catch (Exception e) {
                return new CommandResult(-1, "", e.getMessage() != null ? e.getMessage() : "");
            }
        }, EXEC);
    }

    private static String readFully(java.io.InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(line);
            }
        } catch (Exception ignored) {}
        return sb.toString();
    }

    /**
     * Checks if an executable is on PATH; returns its full path if found.
     * Uses "which" on Linux/Mac, "where" on Windows.
     */
    public static Optional<String> which(String executable) {
        String os = System.getProperty("os.name", "").toLowerCase();
        String[] cmd = os.contains("win")
                ? new String[] { "where", executable }
                : new String[] { "which", executable };
        try {
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String out = readFully(p.getInputStream());
            p.waitFor();
            if (p.exitValue() != 0 || out == null || out.isBlank()) return Optional.empty();
            String first = out.lines().findFirst().orElse("").trim();
            return first.isEmpty() ? Optional.empty() : Optional.of(first);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Result of a run: exit code, stdout, stderr. isSuccess() is true when exitCode == 0.
     */
    public record CommandResult(int exitCode, String stdout, String stderr) {
        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
}
