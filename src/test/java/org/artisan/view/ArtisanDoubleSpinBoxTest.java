package org.artisan.view;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for ArtisanDoubleSpinBox: value clamped to range.
 */
@ExtendWith(ApplicationExtension.class)
class ArtisanDoubleSpinBoxTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        // JavaFX toolkit initialized
    }

    @Test
    void clampsToMin() {
        ArtisanDoubleSpinBox box = new ArtisanDoubleSpinBox(0.0, 10.0, 5.0);
        box.setDoubleValue(-1.0);
        assertEquals(0.0, box.getDoubleValue(), 1e-9);
    }

    @Test
    void clampsToMax() {
        ArtisanDoubleSpinBox box = new ArtisanDoubleSpinBox(0.0, 10.0, 5.0);
        box.setDoubleValue(100.0);
        assertEquals(10.0, box.getDoubleValue(), 1e-9);
    }

    @Test
    void getDoubleValueReturnsInRange() {
        ArtisanDoubleSpinBox box = new ArtisanDoubleSpinBox(1.0, 2.0, 1.5);
        assertEquals(1.5, box.getDoubleValue(), 1e-9);
        box.setDoubleValue(1.8);
        assertEquals(1.8, box.getDoubleValue(), 1e-9);
    }
}
