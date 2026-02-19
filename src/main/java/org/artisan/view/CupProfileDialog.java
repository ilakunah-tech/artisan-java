package org.artisan.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.model.CupProfile;

/**
 * Roast → Cup Profile... dialog. FlavorWheelView (320×320) + sliders for 10 attributes,
 * defects, cup notes, Total label. OK/Apply: save CupProfile, call appController.onCupProfileChanged.
 */
public final class CupProfileDialog extends ArtisanDialog {

    private final AppController appController;
    private final FlavorWheelView flavorWheelView;
    private final Label totalLabel;
    private final Slider[] sliders;
    private final Spinner<Double> defectsSpinner;
    private final TextArea cupNotesArea;

    public CupProfileDialog(Window owner, AppController appController) {
        super(owner, true, true);
        this.appController = appController;
        this.flavorWheelView = new FlavorWheelView();
        this.totalLabel = new Label("Total: 0.0");
        this.sliders = new Slider[CupProfile.DEFAULT_ATTRIBUTES.length];
        this.defectsSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 0.0, 0.5));
        this.defectsSpinner.setEditable(true);
        this.cupNotesArea = new TextArea();
        this.cupNotesArea.setPrefRowCount(2);
        this.cupNotesArea.setWrapText(true);
        getStage().setTitle("Cup Profile");
        getApplyButton().setOnAction(e -> apply(false));
    }

    @Override
    protected Node buildContent() {
        CupProfile profile = appController.getCupProfile();
        profile.load();
        flavorWheelView.setCupProfile(profile);

        VBox left = new VBox(flavorWheelView);
        left.setAlignment(Pos.CENTER);
        left.setMinWidth(320);
        left.setMaxWidth(320);

        GridPane right = new GridPane();
        right.setHgap(8);
        right.setVgap(6);
        int row = 0;
        for (int i = 0; i < CupProfile.DEFAULT_ATTRIBUTES.length; i++) {
            String attr = CupProfile.DEFAULT_ATTRIBUTES[i];
            Slider sl = new Slider(0, 10, profile.getScores().getOrDefault(attr, 0.0));
            sl.setBlockIncrement(0.5);
            sl.setMajorTickUnit(2);
            sl.setMinorTickCount(3);
            sl.setShowTickLabels(false);
            sl.setShowTickMarks(false);
            final int idx = i;
            sl.valueProperty().addListener((a, b, c) -> {
                profile.getScores().put(CupProfile.DEFAULT_ATTRIBUTES[idx], sl.getValue());
                flavorWheelView.draw();
                updateTotalLabel();
            });
            sliders[i] = sl;
            right.add(new Label(attr + ":"), 0, row);
            right.add(sl, 1, row++);
        }
        right.add(new Label("Defects:"), 0, row);
        right.add(defectsSpinner, 1, row++);
        defectsSpinner.getValueFactory().setValue(profile.getDefects());
        defectsSpinner.getValueFactory().valueProperty().addListener((a, b, c) -> {
            appController.getCupProfile().setDefects(defectsSpinner.getValue());
            updateTotalLabel();
        });
        cupNotesArea.setText(profile.getCupNotes());
        right.add(new Label("Notes:"), 0, row);
        right.add(cupNotesArea, 1, row++);
        right.add(totalLabel, 0, row++, 2, 1);

        updateTotalLabel();

        HBox main = new HBox(15, left, right);
        main.setPadding(new Insets(10));
        return main;
    }

    private void updateTotalLabel() {
        CupProfile p = appController.getCupProfile();
        totalLabel.setText(String.format("Total: %.1f", p.getTotal()));
    }

    private void syncToProfile() {
        CupProfile p = appController.getCupProfile();
        for (int i = 0; i < sliders.length; i++) {
            p.getScores().put(CupProfile.DEFAULT_ATTRIBUTES[i], sliders[i].getValue());
        }
        p.setDefects(defectsSpinner.getValue());
        p.setCupNotes(cupNotesArea.getText());
    }

    private void apply(boolean close) {
        syncToProfile();
        appController.getCupProfile().save();
        appController.onCupProfileChanged(appController.getCupProfile());
        if (close) getStage().close();
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        apply(true);
        super.onOk(e);
    }
}
