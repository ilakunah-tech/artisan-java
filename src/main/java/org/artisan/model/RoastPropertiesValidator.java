package org.artisan.model;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.stage.Window;

/**
 * Validates a RoastProperties instance.
 */
public final class RoastPropertiesValidator {

    private RoastPropertiesValidator() {
    }

    /**
     * Validates the given RoastProperties and, if there are errors, shows an alert on the given window.
     *
     * @param owner the owner window for the alert (can be null)
     * @param p     the instance to validate
     * @return true if valid (no errors); false if errors were shown
     */
    public static boolean showErrors(Window owner, RoastProperties p) {
        List<String> errors = validate(p);
        if (errors.isEmpty()) return true;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Roast Properties");
        alert.setHeaderText("Please correct the following:");
        alert.setContentText(String.join("\n", errors));
        if (owner != null) alert.initOwner(owner);
        alert.showAndWait();
        return false;
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
