package org.artisan.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSessionTest {

    private FileSession session;

    @AfterEach
    void tearDown() {
        if (session != null) {
            session.clearRecentFiles();
        }
    }

    @Test
    void newSession_isNotDirty() {
        session = new FileSession();
        assertFalse(session.isDirty());
        assertNull(session.getCurrentFilePath());
    }

    @Test
    void markDirty_isDirty() {
        session = new FileSession();
        session.markDirty();
        assertTrue(session.isDirty());
    }

    @Test
    void markSaved_clearsFlag_setsPath() {
        session = new FileSession();
        session.markDirty();
        Path path = Path.of("some", "file.alog").normalize();
        session.markSaved(path);
        assertFalse(session.isDirty());
        assertEquals(path, session.getCurrentFilePath());
    }

    @Test
    void markNew_clearsPathAndDirty() {
        session = new FileSession();
        session.markSaved(Path.of("x.alog"));
        session.markDirty();
        session.markNew();
        assertFalse(session.isDirty());
        assertNull(session.getCurrentFilePath());
    }

    @Test
    void pushRecentFile_storesUpToTen() {
        session = new FileSession();
        for (int i = 0; i < 11; i++) {
            session.pushRecentFile(Path.of("file" + i + ".alog"));
        }
        var recent = session.getRecentFiles();
        assertEquals(10, recent.size());
        assertEquals("file10.alog", recent.get(0).getFileName().toString());
        assertEquals("file1.alog", recent.get(9).getFileName().toString());
    }

    @Test
    void clearRecentFiles_returnsEmpty() {
        session = new FileSession();
        session.pushRecentFile(Path.of("a.alog"));
        session.pushRecentFile(Path.of("b.alog"));
        session.clearRecentFiles();
        assertTrue(session.getRecentFiles().isEmpty());
    }
}
