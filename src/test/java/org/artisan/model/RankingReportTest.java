package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingReportTest {

    @Test
    void generate_sortsByColorDesc() {
        BatchManager bm = new BatchManager();
        Batch b80 = new Batch();
        b80.setBatchNumber(1);
        b80.setTitle("A");
        b80.setRoastColor(80);
        b80.setGreenWeight(500);
        b80.setRoastedWeight(450);
        bm.addBatch(b80);
        Batch b60 = new Batch();
        b60.setBatchNumber(2);
        b60.setTitle("B");
        b60.setRoastColor(60);
        b60.setGreenWeight(500);
        b60.setRoastedWeight(450);
        bm.addBatch(b60);
        Batch b90 = new Batch();
        b90.setBatchNumber(3);
        b90.setTitle("C");
        b90.setRoastColor(90);
        b90.setGreenWeight(500);
        b90.setRoastedWeight(450);
        bm.addBatch(b90);

        List<Batch> list = new ArrayList<>(bm.getBatches());
        String result = RankingReport.generate(list, bm);
        assertNotNull(result);
        int idx90 = result.indexOf("90");
        int idx80 = result.indexOf("80");
        int idx60 = result.indexOf("60");
        assertTrue(idx90 >= 0 && idx80 >= 0 && idx60 >= 0);
        assertTrue(idx90 < idx80 && idx80 < idx60, "Order should be 90, 80, 60 (color desc)");
    }

    @Test
    void generate_containsHeader() {
        BatchManager bm = new BatchManager();
        String result = RankingReport.generate(bm.getBatches(), bm);
        assertNotNull(result);
        assertTrue(result.contains("RANKING REPORT"));
    }

    @Test
    void generate_containsTotalBatches() {
        BatchManager bm = new BatchManager();
        Batch b = new Batch();
        b.setBatchNumber(1);
        b.setTitle("One");
        bm.addBatch(b);
        String result = RankingReport.generate(bm.getBatches(), bm);
        assertNotNull(result);
        assertTrue(result.contains("Total batches"));
    }

    @Test
    void generate_emptyList() {
        String result = RankingReport.generate(List.of(), new BatchManager());
        assertNotNull(result);
        assertTrue(result.contains("RANKING REPORT"));
    }
}
