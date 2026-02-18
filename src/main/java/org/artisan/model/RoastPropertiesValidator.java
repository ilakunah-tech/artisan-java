package org.artisan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a RoastProperties instance.
 */
public final class RoastPropertiesValidator {

    private RoastPropertiesValidator() {
    }

    /**
     * Validates the given RoastProperties.
     *
     * @param p the instance to validate (may be null)
     * @return list of validation error messages; empty list means valid
     */
    public static List<String> validate(RoastProperties p) {
        List<String> errors = new ArrayList<>();
        if (p == null) {
            errors.add("RoastProperties must not be null");
            return errors;
        }

        if (p.getBeanName() == null || p.getBeanName().trim().isEmpty()) {
            errors.add("Bean name must not be null or empty");
        }
        if (p.getWeightInGrams() <= 0) {
            errors.add("Weight in (grams) must be greater than 0");
        }
        if (p.getWeightOutGrams() <= 0) {
            errors.add("Weight out (grams) must be greater than 0");
        }
        if (p.getWeightOutGrams() >= p.getWeightInGrams()) {
            errors.add("Weight out must be less than weight in");
        }
        if (p.getMoisturePercent() < 0 || p.getMoisturePercent() > 100) {
            errors.add("Moisture percent must be between 0 and 100");
        }
        if (p.getDensityGramsPerLiter() < 0) {
            errors.add("Density (g/L) must be >= 0");
        }

        return errors;
    }
}
