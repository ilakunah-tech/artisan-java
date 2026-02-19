package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoastPropertiesTest {

    @Test
    void defaultValues() {
        RoastProperties p = RoastProperties.defaults();
        assertNotNull(p);
        assertEquals("", p.getTitle());
        assertEquals("", p.getNotes());
        assertEquals("", p.getRoastDate());
        assertEquals("", p.getBeanOrigin());
        assertEquals("", p.getOperator());
        assertEquals(0.0, p.getGreenWeight());
        assertEquals(0.0, p.getRoastedWeight());
        assertEquals(0.0, p.getMoisture());
        assertEquals(0.0, p.getDensity());
        assertEquals(0, p.getRoastColor());
        assertNotNull(p.getCustomLabels());
        assertTrue(p.getCustomLabels().isEmpty());
    }

    @Test
    void saveLoadRoundtrip() {
        RoastProperties p = new RoastProperties();
        p.setTitle("My Roast");
        p.setNotes("Tasty");
        p.setRoastDate("2025-02-19");
        p.setBeanOrigin("Ethiopia");
        p.setBeanVariety("Heirloom");
        p.setBeanProcess("Washed");
        p.setBeanGrade("Grade 1");
        p.setGreenWeight(500.0);
        p.setRoastedWeight(420.0);
        p.setMoisture(12.0);
        p.setDensity(380.0);
        p.setRoastColor(65);
        p.setOperator("Jane");
        p.getCustomLabels().add("lot=123");
        p.getCustomLabels().add("farm=Yirg");
        p.save();
        RoastProperties loaded = new RoastProperties();
        loaded.load();
        assertEquals(p.getTitle(), loaded.getTitle());
        assertEquals(p.getNotes(), loaded.getNotes());
        assertEquals(p.getRoastDate(), loaded.getRoastDate());
        assertEquals(p.getBeanOrigin(), loaded.getBeanOrigin());
        assertEquals(p.getBeanVariety(), loaded.getBeanVariety());
        assertEquals(p.getBeanProcess(), loaded.getBeanProcess());
        assertEquals(p.getBeanGrade(), loaded.getBeanGrade());
        assertEquals(p.getGreenWeight(), loaded.getGreenWeight());
        assertEquals(p.getRoastedWeight(), loaded.getRoastedWeight());
        assertEquals(p.getMoisture(), loaded.getMoisture());
        assertEquals(p.getDensity(), loaded.getDensity());
        assertEquals(p.getRoastColor(), loaded.getRoastColor());
        assertEquals(p.getOperator(), loaded.getOperator());
        assertEquals(p.getCustomLabels(), loaded.getCustomLabels());
    }

    @Test
    void toMapContainsAllKeys() {
        RoastProperties p = new RoastProperties();
        p.setTitle("T");
        p.getCustomLabels().add("a=b");
        Map<String, String> m = p.toMap();
        assertTrue(m.containsKey("title"));
        assertTrue(m.containsKey("notes"));
        assertTrue(m.containsKey("roastDate"));
        assertTrue(m.containsKey("beanOrigin"));
        assertTrue(m.containsKey("beanVariety"));
        assertTrue(m.containsKey("beanProcess"));
        assertTrue(m.containsKey("beanGrade"));
        assertTrue(m.containsKey("greenWeight"));
        assertTrue(m.containsKey("roastedWeight"));
        assertTrue(m.containsKey("moisture"));
        assertTrue(m.containsKey("density"));
        assertTrue(m.containsKey("roastColor"));
        assertTrue(m.containsKey("operator"));
        assertTrue(m.containsKey("customLabel_0"));
    }

    @Test
    void weightLossCalculation() {
        RoastProperties p = new RoastProperties();
        p.setGreenWeight(500.0);
        p.setRoastedWeight(400.0);
        double loss = p.weightLossPercent();
        // (500 - 400) / 500 * 100 = 20.0
        assertEquals(20.0, loss, 0.001);
    }

    @Test
    void getBeanNameReturnsTitleOrOrigin() {
        RoastProperties p = new RoastProperties();
        assertEquals("", p.getBeanName());
        p.setTitle("My Roast");
        assertEquals("My Roast", p.getBeanName());
        p.setTitle("");
        p.setBeanOrigin("Ethiopia");
        assertEquals("Ethiopia", p.getBeanName());
    }
}
