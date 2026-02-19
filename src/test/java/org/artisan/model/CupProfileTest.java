package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CupProfileTest {

    @Test
    void defaultAttributes_allPresent() {
        CupProfile p = CupProfile.defaults();
        assertNotNull(p.getScores());
        assertEquals(10, p.getScores().size());
        for (String attr : CupProfile.DEFAULT_ATTRIBUTES) {
            assertTrue(p.getScores().containsKey(attr), "missing: " + attr);
            assertEquals(0.0, p.getScores().get(attr), 0.001);
        }
    }

    @Test
    void getTotalCalculation() {
        CupProfile p = new CupProfile();
        // sum = 80 (8 per attribute * 10), defects = 0, total = 80 + 36 = 116 → clamp to 100
        for (String attr : CupProfile.DEFAULT_ATTRIBUTES) {
            p.getScores().put(attr, 8.0);
        }
        p.setDefects(0.0);
        double total = p.getTotal();
        assertEquals(100.0, total, 0.001);
    }

    @Test
    void getTotalWithDefects() {
        CupProfile p = new CupProfile();
        // sum = 50, defects = 5, total = 50 - 5 + 36 = 81
        for (String attr : CupProfile.DEFAULT_ATTRIBUTES) {
            p.getScores().put(attr, 5.0);
        }
        p.setDefects(5.0);
        double total = p.getTotal();
        assertEquals(81.0, total, 0.001);
    }

    @Test
    void saveLoadRoundtrip() {
        CupProfile p = new CupProfile();
        p.getScores().put("Fragrance/Aroma", 7.5);
        p.getScores().put("Flavor", 8.0);
        p.setDefects(2.0);
        p.setCupNotes("Bright, fruity");
        p.save();
        CupProfile loaded = new CupProfile();
        loaded.load();
        assertEquals(7.5, loaded.getScores().get("Fragrance/Aroma"), 0.001);
        assertEquals(8.0, loaded.getScores().get("Flavor"), 0.001);
        assertEquals(2.0, loaded.getDefects(), 0.001);
        assertEquals("Bright, fruity", loaded.getCupNotes());
    }

    @Test
    void defaultsResetsAllScores() {
        CupProfile p = new CupProfile();
        p.getScores().put("Flavor", 9.0);
        p.setDefects(5.0);
        p = CupProfile.defaults();
        assertEquals(0.0, p.getScores().get("Flavor"), 0.001);
        assertEquals(0.0, p.getDefects(), 0.001);
    }

    @Test
    void toMapContainsScoresAndDefectsAndNotes() {
        CupProfile p = new CupProfile();
        p.getScores().put("Overall", 7.0);
        p.setDefects(1.0);
        p.setCupNotes("Good");
        Map<String, String> m = p.toMap();
        assertTrue(m.containsKey("Overall"));
        assertEquals("7.0", m.get("Overall"));
        assertTrue(m.containsKey("defects"));
        assertEquals("1.0", m.get("defects"));
        assertTrue(m.containsKey("cupNotes"));
        assertEquals("Good", m.get("cupNotes"));
    }
}
