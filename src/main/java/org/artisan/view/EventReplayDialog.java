package org.artisan.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import org.artisan.controller.EventReplay;

/**
 * Config » Replay... dialog: enable replay, offset (-60..60 s), replay from background checkbox.
 * OK saves to Preferences (replay.*).
 */
public final class EventReplayDialog extends ArtisanDialog {

    private final EventReplay replay;
    private CheckBox enableCheck;
    private Spinner<Double> offsetSpinner;
    private CheckBox replayBackgroundCheck;

    public EventReplayDialog(Window owner, EventReplay replay) {
        super(owner, true, false);
        this.replay = replay != null ? replay : new EventReplay();
        getStage().setTitle("Config » Replay");
    }

    @Override
    protected Node buildContent() {
        enableCheck = new CheckBox("Enable event replay");
        enableCheck.setSelected(replay.isEnabled());

        offsetSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-60.0, 60.0, replay.getOffsetSeconds(), 1.0));
        offsetSpinner.setEditable(true);
        offsetSpinner.setPrefWidth(120);

        replayBackgroundCheck = new CheckBox("Replay from background profile");
        replayBackgroundCheck.setSelected(replay.isReplayBackground());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Replay recorded custom events during live roast when time is reached."), 0, 0, 2, 1);
        grid.add(enableCheck, 0, 1, 2, 1);
        grid.add(new Label("Offset (seconds):"), 0, 2);
        grid.add(offsetSpinner, 1, 2);
        grid.add(replayBackgroundCheck, 0, 3, 2, 1);
        return grid;
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        replay.setEnabled(enableCheck.isSelected());
        try {
            replay.setOffsetSeconds(offsetSpinner.getValue());
        } catch (Exception ignored) {
            replay.setOffsetSeconds(0.0);
        }
        replay.setReplayBackground(replayBackgroundCheck.isSelected());
        replay.save();
        super.onOk(e);
    }
}
