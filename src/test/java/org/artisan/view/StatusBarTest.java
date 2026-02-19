package org.artisan.view;

import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.artisan.controller.AppController.DisplayState;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Logic-only tests for StatusBar (instantiation, updateSample sets BT label).
 * Uses TestFX so JavaFX toolkit is initialized.
 */
@ExtendWith(ApplicationExtension.class)
class StatusBarTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        stage.show();
    }

    @Test
    void canBeInstantiated() {
        StatusBar bar = new StatusBar();
        assertNotNull(bar);
    }

    @Test
    void updateSample_setsBtLabel() {
        StatusBar bar = new StatusBar();
        interact(() -> {
            bar.updateSample(190.5, 210.0, 5.0, 4.0, 120.0);
            // BT label is first child; should show "190.5 °C"
            Label btLabel = (Label) bar.getChildren().get(0);
            String text = btLabel.getText();
            assertTrue(text.contains("190.5") || text.contains("190"), "BT label should show 190.5: " + text);
        });
    }

    @Test
    void setState_doesNotThrow() {
        StatusBar bar = new StatusBar();
        assertDoesNotThrow(() -> bar.setState(DisplayState.IDLE));
        assertDoesNotThrow(() -> bar.setState(DisplayState.SAMPLING));
        assertDoesNotThrow(() -> bar.setState(DisplayState.ERROR));
    }
}
