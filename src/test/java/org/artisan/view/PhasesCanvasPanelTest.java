package org.artisan.view;

import org.artisan.model.PhaseResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless-safe tests for PhasesCanvasPanel (model/refresh logic only, no rendering).
 */
class PhasesCanvasPanelTest {

    @Test
    void canBeInstantiatedWithValidPhaseResult() {
        PhaseResult result = new PhaseResult(600, 240, 240, 120, 40, 40, 20, false);
        PhasesCanvasPanel panel = new PhasesCanvasPanel(result);
        assertNotNull(panel);
        assertNotNull(panel.getPhasesCanvas());
    }

    @Test
    void refreshWithNullDoesNotThrow() {
        PhasesCanvasPanel panel = new PhasesCanvasPanel(PhaseResult.INVALID);
        assertDoesNotThrow(() -> panel.refresh(null));
    }

    @Test
    void refreshWithValidResultDoesNotThrow() {
        PhaseResult result = new PhaseResult(600, 240, 240, 120, 40, 40, 20, false);
        PhasesCanvasPanel panel = new PhasesCanvasPanel(result);
        assertDoesNotThrow(() -> panel.refresh(result));
    }

    @Test
    void instantiateWithPhaseResult_drying40Maillard40Dev20() {
        PhaseResult result = new PhaseResult(600, 240, 240, 120, 40, 40, 20, false);
        assertEquals(40.0, result.getDryingPercent(), 1e-9);
        assertEquals(40.0, result.getMaillardPercent(), 1e-9);
        assertEquals(20.0, result.getDevelopmentPercent(), 1e-9);
        PhasesCanvasPanel panel = new PhasesCanvasPanel(result);
        assertDoesNotThrow(() -> panel.refresh(result));
    }
}
