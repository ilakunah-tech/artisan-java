package org.artisan.view;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for ArtisanComboBox: selected value.
 */
@ExtendWith(ApplicationExtension.class)
class ArtisanComboBoxTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        // JavaFX toolkit initialized
    }

    @Test
    void returnsSelectedValue() {
        ArtisanComboBox<String> box = new ArtisanComboBox<>();
        box.getItems().setAll("A", "B", "C");
        box.setValue("B");
        assertEquals("B", box.getSelectedValue());
    }

    @Test
    void setSelectedValueUpdatesSelection() {
        ArtisanComboBox<String> box = new ArtisanComboBox<>();
        box.getItems().setAll("X", "Y", "Z");
        box.setSelectedValue("Y");
        assertEquals("Y", box.getValue());
    }

    @Test
    void emptySelectionReturnsNull() {
        ArtisanComboBox<String> box = new ArtisanComboBox<>();
        box.getItems().setAll("Only");
        assertNull(box.getSelectedValue());
    }
}
