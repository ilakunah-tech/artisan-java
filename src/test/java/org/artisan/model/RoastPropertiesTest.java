package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoastPropertiesTest {

    @Test
    void builderConstruction() {
        LocalDate date = LocalDate.of(2025, 2, 18);
        RoastProperties p = RoastProperties.builder()
                .beanName("Ethiopian Yirgacheffe")
                .roastDate(date)
                .weightInGrams(500.0)
                .weightOutGrams(425.0)
                .moisturePercent(12.5)
                .densityGramsPerLiter(380.0)
                .colorWholeBean(65)
                .colorGround(72)
                .batchPrefix("R-")
                .batchNumber(101)
                .roastNotes("Light roast")
                .cuppingNotes("Floral, citrus")
                .build();

        assertEquals("Ethiopian Yirgacheffe", p.getBeanName());
        assertEquals(date, p.getRoastDate());
        assertEquals(500.0, p.getWeightInGrams());
        assertEquals(425.0, p.getWeightOutGrams());
        assertEquals(12.5, p.getMoisturePercent());
        assertEquals(380.0, p.getDensityGramsPerLiter());
        assertEquals(65, p.getColorWholeBean());
        assertEquals(72, p.getColorGround());
        assertEquals("R-", p.getBatchPrefix());
        assertEquals(101, p.getBatchNumber());
        assertEquals("Light roast", p.getRoastNotes());
        assertEquals("Floral, citrus", p.getCuppingNotes());
    }

    @Test
    void weightLossPercentCalculation() {
        RoastProperties p = RoastProperties.builder()
                .beanName("B")
                .weightInGrams(500.0)
                .weightOutGrams(425.0)
                .build();
        double loss = p.weightLossPercent();
        // (500 - 425) / 500 * 100 = 15.0
        assertEquals(15.0, loss, 0.001);
    }

    @Test
    void weightLossPercentZeroWhenWeightInZero() {
        RoastProperties p = RoastProperties.builder()
                .beanName("B")
                .weightInGrams(0.0)
                .weightOutGrams(0.0)
                .build();
        assertEquals(0.0, p.weightLossPercent(), 0.001);
    }

    @Test
    void nullSafetyInBuilder() {
        RoastProperties p = RoastProperties.builder()
                .beanName(null)
                .batchPrefix(null)
                .roastNotes(null)
                .cuppingNotes(null)
                .weightInGrams(100.0)
                .weightOutGrams(85.0)
                .build();
        assertNotNull(p.getBeanName());
        assertEquals("", p.getBeanName());
        assertEquals("", p.getBatchPrefix());
        assertEquals("", p.getRoastNotes());
        assertEquals("", p.getCuppingNotes());
    }
}
