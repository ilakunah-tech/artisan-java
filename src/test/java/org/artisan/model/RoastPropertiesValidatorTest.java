package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoastPropertiesValidatorTest {

    private static RoastProperties validProps() {
        return RoastProperties.builder()
                .beanName("Ethiopian")
                .roastDate(LocalDate.now())
                .weightInGrams(500.0)
                .weightOutGrams(425.0)
                .moisturePercent(12.0)
                .densityGramsPerLiter(380.0)
                .build();
    }

    @Test
    void validObjectReturnsEmptyErrorList() {
        List<String> errors = RoastPropertiesValidator.validate(validProps());
        assertTrue(errors.isEmpty());
    }

    @Test
    void negativeWeightInReturnsError() {
        RoastProperties p = RoastProperties.builder()
                .beanName("B")
                .weightInGrams(-1.0)
                .weightOutGrams(100.0)
                .build();
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertTrue(errors.stream().anyMatch(e -> e.toLowerCase().contains("weight in")));
    }

    @Test
    void weightOutGreaterThanWeightInReturnsError() {
        RoastProperties p = RoastProperties.builder()
                .beanName("B")
                .weightInGrams(400.0)
                .weightOutGrams(450.0)
                .build();
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("weight out"));
    }

    @Test
    void moistureOutOfRangeReturnsError() {
        RoastProperties p = RoastProperties.builder()
                .beanName("B")
                .weightInGrams(500.0)
                .weightOutGrams(425.0)
                .moisturePercent(150.0)
                .build();
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("moisture"));

        p = RoastProperties.builder()
                .beanName("B")
                .weightInGrams(500.0)
                .weightOutGrams(425.0)
                .moisturePercent(-1.0)
                .build();
        errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
    }

    @Test
    void emptyBeanNameReturnsError() {
        RoastProperties p = RoastProperties.builder()
                .beanName("")
                .weightInGrams(500.0)
                .weightOutGrams(425.0)
                .build();
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("bean"));

        p = RoastProperties.builder()
                .beanName("   ")
                .weightInGrams(500.0)
                .weightOutGrams(425.0)
                .build();
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
        RoastProperties p = RoastProperties.builder()
                .beanName("B")
                .weightInGrams(500.0)
                .weightOutGrams(425.0)
                .densityGramsPerLiter(-1.0)
                .build();
        List<String> errors = RoastPropertiesValidator.validate(p);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).toLowerCase().contains("density"));
    }
}
