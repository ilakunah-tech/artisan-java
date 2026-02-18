package org.artisan.view;

import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for ArtisanSpinBox: value stays within min/max.
 */
@ExtendWith(ApplicationExtension.class)
class ArtisanSpinBoxTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        // JavaFX toolkit initialized
    }

    @Test
    void doesNotAcceptValueBelowMin() {
        ArtisanSpinBox box = new ArtisanSpinBox(0, 100, 50);
        box.setIntValue(-10);
        assertEquals(0, box.getIntValue());
        assertEquals(0, box.getValue());
    }

    @Test
    void doesNotAcceptValueAboveMax() {
        ArtisanSpinBox box = new ArtisanSpinBox(0, 100, 50);
        box.setIntValue(200);
        assertEquals(100, box.getIntValue());
        assertEquals(100, box.getValue());
    }

    @Test
    void getIntValueClampsToRange() {
        ArtisanSpinBox box = new ArtisanSpinBox(10, 20, 15);
        assertEquals(15, box.getIntValue());
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) box.getValueFactory()).setValue(25);
        assertEquals(20, box.getIntValue());
    }

    @Test
    void minMaxPreserved() {
        ArtisanSpinBox box = new ArtisanSpinBox(5, 99, 50);
        assertEquals(5, box.getMin());
        assertEquals(99, box.getMax());
    }
}
