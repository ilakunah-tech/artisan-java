package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoastPropertiesValidatorTest {

    private static RoastProperties validProps() {
        RoastProperties p = new RoastProperties();
        p.setTitle("Ethiopian");
        p.setGreenWeight(500.0);
        p.setRoastedWeight(425.0);
        p.setMoisture(12.0);
        p.setDensity(380.0);
        return p;
    }

    @Test
    void validObjectReturnsEmptyErrorList() {
        List<String> errors = RoastPropertiesValidator.validate(validProps());
        assertTrue(errors.isEmpty());
    }

    @Test
    void zeroWeightInReturnsError() {
        RoastProperties p = new RoastProperties();
        p.setTitle("B");
        p.setGreenWeight(0.0);
        p.setRoastedWeight(100.0);
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("weight in")));
    }

    @Test
    void weightOutGreaterThanWeightInReturnsError() {
        RoastProperties p = new RoastProperties();
        p.setTitle("B");
        p.setGreenWeight(400.0);
        p.setRoastedWeight(450.0);
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("weight out"));
    }

    @Test
    void moistureOutOfRangeReturnsError() {
        RoastProperties p = new RoastProperties();
        p.setTitle("B");
        p.setGreenWeight(500.0);
        p.setRoastedWeight(425.0);
        p.setMoisture(150.0);
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("moisture"));

        p = new RoastProperties();
        p.setTitle("B");
        p.setGreenWeight(500.0);
        p.setRoastedWeight(425.0);
        p.setMoisture(-1.0);
        errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
    }

    @Test
    void emptyBeanNameReturnsError() {
        RoastProperties p = new RoastProperties();
        p.setTitle("");
        p.setBeanOrigin("");
        p.setGreenWeight(500.0);
        p.setRoastedWeight(425.0);
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("bean"));

        p = new RoastProperties();
        p.setTitle("   ");
        p.setGreenWeight(500.0);
        p.setRoastedWeight(425.0);
        errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
    }

    @Test
    void nullRoastPropertiesReturnsError() {
        List<String> errors = RoastPropertiesValidator.validate(null);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("null"));
    }

    @Test
    void negativeDensityReturnsError() {
        RoastProperties p = new RoastProperties();
        p.setTitle("B");
        p.setGreenWeight(500.0);
        p.setRoastedWeight(425.0);
        p.setDensity(-1.0);
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("density"));
    }
}
