package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.artisan.model.PhaseResult;

/**
 * HBox (36px) with three phase blocks: Drying, Maillard, Development.
 * Each shows phase name + elapsed time; Development also shows DTR%.
 *
 * @Disabled — not rendered in current layout version
 */
public final class PhaseTimerStrip extends HBox {

    private static final String DASH = "—";

    private final Label dryingLabel;
    private final Label maillardLabel;
    private final Label developmentLabel;
    private final HBox dryingBlock;
    private final HBox maillardBlock;
    private final HBox developmentBlock;

    public PhaseTimerStrip() {
        setSpacing(0);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0));
        setMinHeight(36);
        setPrefHeight(36);
        setMaxHeight(36);
        getStyleClass().add("phase-timer-strip");

        dryingBlock = block("Drying", "phase-block-drying");
        dryingLabel = new Label(DASH);
        dryingBlock.getChildren().add(dryingLabel);

        maillardBlock = block("Maillard", "phase-block-maillard");
        maillardLabel = new Label(DASH);
        maillardBlock.getChildren().add(maillardLabel);

        developmentBlock = block("Development", "phase-block-development");
        developmentLabel = new Label(DASH);
        developmentBlock.getChildren().add(developmentLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(dryingBlock, maillardBlock, developmentBlock, spacer);
    }

    private static HBox block(String title, String styleClass) {
        HBox b = new HBox(6);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(0, 12, 0, 12));
        b.setMinWidth(60);
        b.getStyleClass().addAll("phase-block", styleClass);
        Label t = new Label(title + ":");
        t.setStyle("-fx-font-weight: bold;");
        b.getChildren().add(t);
        return b;
    }

    /** Update from PhaseResult and total elapsed seconds. */
    public void refresh(PhaseResult result, double elapsedSec, double totalRoastSec) {
        if (result == null || result.isInvalid()) {
            dryingLabel.setText(DASH);
            maillardLabel.setText(DASH);
            developmentLabel.setText(DASH);
            setBlockState(dryingBlock, false, false);
            setBlockState(maillardBlock, false, false);
            setBlockState(developmentBlock, false, false);
            return;
        }

        double drying = result.getDryingTimeSec();
        double maillard = result.getMaillardTimeSec();
        double development = result.getDevelopmentTimeSec();

        String dryingStr = formatDuration(drying);
        String maillardStr = formatDuration(maillard);
        String devStr = formatDuration(development);

        double dtrPercent = totalRoastSec > 0 && development >= 0
            ? (development / totalRoastSec) * 100 : Double.NaN;
        if (Double.isFinite(dtrPercent)) {
            devStr += " (" + String.format("%.1f%%", dtrPercent) + ")";
        }

        dryingLabel.setText(dryingStr);
        maillardLabel.setText(maillardStr);
        developmentLabel.setText(devStr);

        boolean dryingComplete = elapsedSec > drying && drying >= 0;
        boolean maillardComplete = elapsedSec > drying + maillard && maillard >= 0;
        boolean devComplete = elapsedSec > drying + maillard + development && development >= 0;

        boolean dryingActive = !dryingComplete && elapsedSec <= drying;
        boolean maillardActive = dryingComplete && !maillardComplete && elapsedSec <= drying + maillard;
        boolean devActive = maillardComplete && !devComplete && elapsedSec <= drying + maillard + development;

        setBlockState(dryingBlock, dryingActive, dryingComplete);
        setBlockState(maillardBlock, maillardActive, maillardComplete);
        setBlockState(developmentBlock, devActive, devComplete);
    }

    private static void setBlockState(HBox block, boolean active, boolean completed) {
        block.getStyleClass().removeAll("active", "inactive");
        if (active) block.getStyleClass().add("active");
        else if (completed) block.getStyleClass().add("inactive");
    }

    private static String formatDuration(double sec) {
        if (!Double.isFinite(sec) || sec < 0) return DASH;
        int m = (int) (sec / 60);
        int s = (int) (sec % 60);
        return String.format("%d:%02d", m, s);
    }
}
