package org.artisan.view;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TestFX / unit tests for ArtisanDialog: open/close, button labels, result.
 */
@ExtendWith(ApplicationExtension.class)
class ArtisanDialogTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        stage.show();
    }

    @Test
    void dialogOpensAndClosesWithCancel() {
        interact(() -> {
            ArtisanDialog d = new ArtisanDialog(null, false, false) {
                @Override
                protected Node buildContent() {
                    return new Pane();
                }
            };
            assertFalse(d.isResultOk());
            d.show();
            assertTrue(d.getStage().isShowing());
            d.getCancelButton().fire();
            assertFalse(d.getStage().isShowing());
            assertFalse(d.isResultOk());
        });
    }

    @Test
    void dialogClosesWithOk() {
        interact(() -> {
            ArtisanDialog d = new ArtisanDialog(null, false, false) {
                @Override
                protected Node buildContent() {
                    return new Pane();
                }
            };
            d.show();
            d.getOkButton().fire();
            assertFalse(d.getStage().isShowing());
            assertTrue(d.isResultOk());
        });
    }

    @Test
    void buttonLabelsFromConstants() {
        interact(() -> {
            ArtisanDialog d = new ArtisanDialog(null, false, false) {
                @Override
                protected Node buildContent() {
                    return new Pane();
                }
            };
            assertEquals(ViewStrings.BUTTON_OK, d.getOkButton().getText());
            assertEquals(ViewStrings.BUTTON_CANCEL, d.getCancelButton().getText());
        });
    }
}
