package org.artisan.view;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.artisan.controller.PhasesSettings;
import org.artisan.model.CanvasData;
import org.artisan.model.PhaseDisplayMode;
import org.artisan.model.PhaseResult;
import org.artisan.model.Phases;
import org.artisan.model.PhasesConfig;

/**
 * Three small LCD-style panels for Drying, Maillard, Finishing (Artisan Phases LCDs).
 * Right-click cycles display mode: TIME → PERCENTAGE → TEMPERATURE (persisted in PhasesSettings).
 * During roast shows info relative to current progress; when no data or no phase boundaries shows "—".
 */
public final class PhasesLCD extends HBox {

    private static final String DASH = "—";
    private static final int IDX_CHARGE = 0;
    private static final int IDX_DRY_END = 1;
    private static final int IDX_FC_START = 2;
    private static final int IDX_DROP = 6;

    private final Label dryingLabel;
    private final Label maillardLabel;
    private final Label finishingLabel;
    private final Label dryingTitle;
    private final Label maillardTitle;
    private final Label finishingTitle;
    private final PhasesSettings phasesSettings;

    public PhasesLCD(PhasesSettings phasesSettings) {
        this.phasesSettings = phasesSettings != null ? phasesSettings : PhasesSettings.load();
        dryingTitle = new Label("Drying");
        dryingLabel = new Label(DASH);
        maillardTitle = new Label("Maillard");
        maillardLabel = new Label(DASH);
        finishingTitle = new Label("Finishing");
        finishingLabel = new Label(DASH);

        stylePanel(dryingTitle, dryingLabel);
        stylePanel(maillardTitle, maillardLabel);
        stylePanel(finishingTitle, finishingLabel);

        VBox dryingBox = new VBox(2, dryingTitle, dryingLabel);
        VBox maillardBox = new VBox(2, maillardTitle, maillardLabel);
        VBox finishingBox = new VBox(2, finishingTitle, finishingLabel);
        dryingBox.getStyleClass().add("phases-lcd-panel");
        maillardBox.getStyleClass().add("phases-lcd-panel");
        finishingBox.getStyleClass().add("phases-lcd-panel");

        getChildren().addAll(dryingBox, gap(), maillardBox, gap(), finishingBox);
        setSpacing(8);
        getStyleClass().add("phases-lcd");

        ContextMenu menu = new ContextMenu();
        javafx.scene.control.MenuItem timeItem = new javafx.scene.control.MenuItem("Mode: Time");
        javafx.scene.control.MenuItem pctItem = new javafx.scene.control.MenuItem("Mode: Percentage");
        javafx.scene.control.MenuItem tempItem = new javafx.scene.control.MenuItem("Mode: Temperature");
        menu.getItems().addAll(timeItem, pctItem, tempItem);
        timeItem.setOnAction(e -> setMode(PhaseDisplayMode.TIME));
        pctItem.setOnAction(e -> setMode(PhaseDisplayMode.PERCENTAGE));
        tempItem.setOnAction(e -> setMode(PhaseDisplayMode.TEMPERATURE));
        setOnContextMenuRequested(ev -> menu.show(this, ev.getScreenX(), ev.getScreenY()));
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                cycleMode();
            }
        });
    }

    private static Label gap() {
        Label l = new Label(" ");
        l.setMinWidth(12);
        return l;
    }

    private void stylePanel(Label title, Label value) {
        title.getStyleClass().add("phases-lcd-title");
        value.getStyleClass().add("phases-lcd-value");
        value.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
    }

    private void setMode(PhaseDisplayMode mode) {
        phasesSettings.setLcdMode(mode);
    }

    private void cycleMode() {
        PhaseDisplayMode current = phasesSettings.getLcdMode();
        PhaseDisplayMode next = current == PhaseDisplayMode.TIME ? PhaseDisplayMode.PERCENTAGE
                : current == PhaseDisplayMode.PERCENTAGE ? PhaseDisplayMode.TEMPERATURE : PhaseDisplayMode.TIME;
        phasesSettings.setLcdMode(next);
    }

    /**
     * Updates the three LCD panels from current canvas data and phases config.
     * Call from main timer or when profile/events change.
     */
    public void update(CanvasData canvasData, PhasesConfig config) {
        if (canvasData == null || canvasData.getTimex().isEmpty()) {
            dryingLabel.setText(DASH);
            maillardLabel.setText(DASH);
            finishingLabel.setText(DASH);
            return;
        }
        var timex = canvasData.getTimex();
        var temp2 = canvasData.getTemp2();
        var ti = Phases.timeindexFromIndices(
                canvasData.getChargeIndex(),
                canvasData.getDryEndIndex(),
                canvasData.getFcStartIndex(),
                canvasData.getDropIndex());
        var effective = Phases.getEffectiveTimeindex(timex, temp2, ti, config);
        PhaseResult result = Phases.compute(timex, temp2, effective);
        int currentIdx = timex.size() - 1;
        double currentTime = timex.get(currentIdx);
        double currentBt = currentIdx < temp2.size() ? temp2.get(currentIdx) : 0;

        int chargeIdx = indexAt(effective, IDX_CHARGE);
        int dryEndIdx = indexAt(effective, IDX_DRY_END);
        int fcStartIdx = indexAt(effective, IDX_FC_START);
        int dropIdx = indexAt(effective, IDX_DROP);
        double startTime = chargeIdx >= 0 && chargeIdx < timex.size() ? timex.get(chargeIdx) : 0;

        PhaseDisplayMode mode = config != null ? config.getLcdMode() : PhaseDisplayMode.TIME;
        boolean finishingShowAll = config != null && config.isFinishingShowAllLcds();

        String dryingText = formatPhase(mode, result, 0, startTime, currentTime, currentBt,
                chargeIdx, dryEndIdx, fcStartIdx, dropIdx, timex, temp2, 0);
        String maillardText = formatPhase(mode, result, 1, startTime, currentTime, currentBt,
                chargeIdx, dryEndIdx, fcStartIdx, dropIdx, timex, temp2, 1);
        String finishingText = formatPhase(mode, result, 2, startTime, currentTime, currentBt,
                chargeIdx, dryEndIdx, fcStartIdx, dropIdx, timex, temp2, 2);

        if (finishingShowAll && currentIdx >= fcStartIdx && fcStartIdx > 0) {
            String timePart = formatTime(result.getDevelopmentTimeSec());
            String pctPart = String.format("%.0f%%", result.getDevelopmentPercent());
            double btAtFcs = fcStartIdx < temp2.size() ? temp2.get(fcStartIdx) : 0;
            String tempPart = String.format("%+.1f°C", currentBt - btAtFcs);
            finishingLabel.setText(timePart + " | " + pctPart + " | " + tempPart);
        } else {
            finishingLabel.setText(finishingText);
        }
        dryingLabel.setText(dryingText);
        maillardLabel.setText(maillardText);
    }

    private static int indexAt(java.util.List<Integer> ti, int slot) {
        if (slot >= ti.size()) return -1;
        Integer v = ti.get(slot);
        if (v == null) return -1;
        return (slot == IDX_CHARGE && v >= 0) || (slot != IDX_CHARGE && v > 0) ? v : -1;
    }

    private static String formatPhase(PhaseDisplayMode mode, PhaseResult result,
            int phaseIndex, double startTime, double currentTime, double currentBt,
            int chargeIdx, int dryEndIdx, int fcStartIdx, int dropIdx,
            java.util.List<Double> timex, java.util.List<Double> temp2, int whichPhase) {
        switch (mode) {
            case TIME:
                return formatPhaseTime(whichPhase, startTime, currentTime, timex, temp2, chargeIdx, dryEndIdx, fcStartIdx, dropIdx);
            case PERCENTAGE:
                if (whichPhase == 0) return String.format("%.0f%%", result.getDryingPercent());
                if (whichPhase == 1) return String.format("%.0f%%", result.getMaillardPercent());
                return String.format("%.0f%%", result.getDevelopmentPercent());
            case TEMPERATURE:
                return formatPhaseTemp(whichPhase, currentBt, timex, temp2, chargeIdx, dryEndIdx, fcStartIdx, dropIdx);
            default:
                return DASH;
        }
    }

    private static String formatPhaseTime(int whichPhase, double startTime, double currentTime,
            java.util.List<Double> timex, java.util.List<Double> temp2,
            int chargeIdx, int dryEndIdx, int fcStartIdx, int dropIdx) {
        if (whichPhase == 0) {
            if (dryEndIdx <= 0) return "—";
            double phaseStart = chargeIdx >= 0 && chargeIdx < timex.size() ? timex.get(chargeIdx) : 0;
            if (currentTime < (dryEndIdx < timex.size() ? timex.get(dryEndIdx) : 0)) {
                return formatTime(currentTime - phaseStart);
            }
            return formatTime((dryEndIdx < timex.size() ? timex.get(dryEndIdx) : 0) - phaseStart);
        }
        if (whichPhase == 1) {
            if (fcStartIdx <= 0 || dryEndIdx <= 0) return DASH;
            double dryTime = dryEndIdx < timex.size() ? timex.get(dryEndIdx) : 0;
            if (currentTime < (fcStartIdx < timex.size() ? timex.get(fcStartIdx) : 0)) {
                return formatTime(currentTime - dryTime);
            }
            return formatTime((fcStartIdx < timex.size() ? timex.get(fcStartIdx) : 0) - dryTime);
        }
        if (whichPhase == 2) {
            if (fcStartIdx <= 0) return DASH;
            double fcsTime = fcStartIdx < timex.size() ? timex.get(fcStartIdx) : 0;
            if (currentTime < fcsTime) return DASH;
            if (dropIdx > 0 && dropIdx < timex.size() && currentTime >= timex.get(dropIdx)) {
                return formatTime(timex.get(dropIdx) - fcsTime);
            }
            return formatTime(currentTime - fcsTime);
        }
        return DASH;
    }

    private static String formatPhaseTemp(int whichPhase, double currentBt,
            java.util.List<Double> timex, java.util.List<Double> temp2,
            int chargeIdx, int dryEndIdx, int fcStartIdx, int dropIdx) {
        if (whichPhase == 0) {
            if (chargeIdx < 0 || chargeIdx >= temp2.size()) return DASH;
            return String.format("%+.1f°C", currentBt - temp2.get(chargeIdx));
        }
        if (whichPhase == 1) {
            if (dryEndIdx <= 0 || dryEndIdx >= temp2.size()) return DASH;
            return String.format("%+.1f°C", currentBt - temp2.get(dryEndIdx));
        }
        if (whichPhase == 2) {
            if (fcStartIdx <= 0 || fcStartIdx >= temp2.size()) return DASH;
            return String.format("%+.1f°C", currentBt - temp2.get(fcStartIdx));
        }
        return DASH;
    }

    private static String formatTime(double sec) {
        int s = (int) Math.round(sec);
        int m = s / 60;
        s = s % 60;
        return String.format("%d:%02d", m, s);
    }
}
