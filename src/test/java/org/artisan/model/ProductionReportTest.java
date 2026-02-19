package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionReportTest {

    @Test
    void generate_filtersByDateRange() {
        BatchManager bm = new BatchManager();
        Batch b1 = new Batch();
        b1.setBatchNumber(1);
        b1.setTitle("Jan");
        b1.setDate("2025-01-15");
        b1.setGreenWeight(500);
        b1.setRoastedWeight(420);
        bm.addBatch(b1);
        Batch b2 = new Batch();
        b2.setBatchNumber(2);
        b2.setTitle("Feb");
        b2.setDate("2025-02-20");
        b2.setGreenWeight(600);
        b2.setRoastedWeight(510);
        bm.addBatch(b2);
        Batch b3 = new Batch();
        b3.setBatchNumber(3);
        b3.setTitle("Mar");
        b3.setDate("2025-03-10");
        b3.setGreenWeight(550);
        b3.setRoastedWeight(465);
        bm.addBatch(b3);

        String result = ProductionReport.generate(bm, "2025-02-01", "2025-02-28");
        assertTrue(result.contains("Feb"));
        assertTrue(result.contains("2025-02-20"));
        assertFalse(result.contains("Jan"));
        assertFalse(result.contains("Mar"));
    }

    @Test
    void generate_containsTotals() {
        BatchManager bm = new BatchManager();
        Batch b = new Batch();
        b.setBatchNumber(1);
        b.setTitle("One");
        b.setDate("2025-01-01");
        b.setGreenWeight(500);
        b.setRoastedWeight(450);
        bm.addBatch(b);
        String result = ProductionReport.generate(bm, "2025-01-01", "2025-01-31");
        assertTrue(result.contains("Total"));
    }

    @Test
    void generate_emptyRange_noRows() {
        BatchManager bm = new BatchManager();
        Batch b = new Batch();
        b.setBatchNumber(1);
        b.setTitle("One");
        b.setDate("2025-06-15");
        bm.addBatch(b);
        String result = ProductionReport.generate(bm, "2025-01-01", "2025-01-31");
        assertTrue(result.contains("No batches in date range"));
    }
}
