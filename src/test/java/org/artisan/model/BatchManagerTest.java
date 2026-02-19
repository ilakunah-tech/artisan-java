package org.artisan.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void addBatch_assignsNumber() {
        BatchManager bm = new BatchManager();
        Batch b = new Batch();
        b.setTitle("First");
        bm.addBatch(b);
        assertEquals(1, b.getBatchNumber());
        assertEquals(1, bm.getBatches().size());
    }

    @Test
    void addBatch_incrementsNumber() {
        BatchManager bm = new BatchManager();
        Batch b1 = new Batch();
        b1.setTitle("First");
        bm.addBatch(b1);
        Batch b2 = new Batch();
        b2.setTitle("Second");
        bm.addBatch(b2);
        assertEquals(1, b1.getBatchNumber());
        assertEquals(2, b2.getBatchNumber());
        assertEquals(2, bm.getBatches().size());
    }

    @Test
    void removeBatch_returnsTrueIfFound() {
        BatchManager bm = new BatchManager();
        Batch b = new Batch();
        b.setTitle("One");
        bm.addBatch(b);
        assertTrue(bm.removeBatch(1));
        assertEquals(0, bm.getBatches().size());
    }

    @Test
    void removeBatch_returnsFalseIfNotFound() {
        BatchManager bm = new BatchManager();
        assertFalse(bm.removeBatch(99));
    }

    @Test
    void getBatches_sortedByNumber() {
        BatchManager bm = new BatchManager();
        Batch b3 = new Batch();
        b3.setTitle("Third");
        bm.addBatch(b3);
        Batch b1 = new Batch();
        b1.setTitle("First");
        bm.addBatch(b1);
        Batch b2 = new Batch();
        b2.setTitle("Second");
        bm.addBatch(b2);
        List<Batch> list = bm.getBatches();
        assertEquals(3, list.size());
        assertEquals(1, list.get(0).getBatchNumber());
        assertEquals(2, list.get(1).getBatchNumber());
        assertEquals(3, list.get(2).getBatchNumber());
    }

    @Test
    void getNextBatchNumber_afterAdd() {
        BatchManager bm = new BatchManager();
        assertEquals(1, bm.getNextBatchNumber());
        bm.addBatch(new Batch());
        assertEquals(2, bm.getNextBatchNumber());
        bm.addBatch(new Batch());
        assertEquals(3, bm.getNextBatchNumber());
    }

    @Test
    void exportCsv_createsFile() throws IOException {
        Path csv = tempDir.resolve("batches.csv");
        BatchManager bm = new BatchManager();
        Batch b = new Batch();
        b.setTitle("Export Test");
        b.setDate("2025-01-15");
        bm.addBatch(b);
        bm.exportCsv(csv);
        assertTrue(Files.isRegularFile(csv));
        String content = Files.readString(csv);
        assertTrue(content.contains("batchNumber"));
        assertTrue(content.contains("title"));
        long lineCount = content.lines().count();
        assertEquals(2, lineCount); // header + 1 row
    }
}
