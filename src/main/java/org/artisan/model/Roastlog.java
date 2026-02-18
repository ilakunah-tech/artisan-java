package org.artisan.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Saves and loads roast profiles to/from .alog files (JSON format).
 * Migrated from Artisan Python serialize/deserialize (util.py); Java uses JSON via Jackson.
 */
public final class Roastlog {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private Roastlog() {}

    /**
     * Saves the given profile to a .alog (JSON) file.
     *
     * @param profile profile to save
     * @param path    target file path (typically .alog)
     * @throws IOException if writing fails
     */
    public static void save(ProfileData profile, Path path) throws IOException {
        if (profile == null || path == null) {
            throw new IllegalArgumentException("profile and path must be non-null");
        }
        try (OutputStream out = Files.newOutputStream(path)) {
            MAPPER.writeValue(out, profile);
        }
    }

    /**
     * Loads a profile from a .alog (JSON) file.
     *
     * @param path file path (typically .alog)
     * @return loaded profile, or null if file is empty/corrupted or invalid JSON
     */
    public static ProfileData load(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return null;
        }
        try (InputStream in = Files.newInputStream(path)) {
            return MAPPER.readValue(in, ProfileData.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Loads a profile from a .alog (JSON) file; throws on IO or parse error.
     *
     * @param path file path (typically .alog)
     * @return loaded profile
     * @throws IOException if reading or parsing fails
     */
    public static ProfileData loadOrThrow(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IOException("Missing or not a file: " + path);
        }
        try (InputStream in = Files.newInputStream(path)) {
            return MAPPER.readValue(in, ProfileData.class);
        }
    }
}
