package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

/**
 * Reference info panel with collapsible sections: Profile, Times, Temperatures, Weight.
 */
public final class ReferenceInfoPanel extends VBox {

    public ReferenceInfoPanel() {
        setSpacing(4);
        setPadding(new Insets(0));
        getStyleClass().add("ri5-reference-info");

        TitledPane profileSection = new TitledPane("Profile", new VBox(4,
            new Label("Name: —"),
            new Label("Green: —"),
            new Label("Roast level: —")));
        profileSection.setAnimated(true);
        profileSection.setExpanded(true);

        TitledPane timesSection = new TitledPane("Times", new VBox(4,
            new Label("Total: —"),
            new Label("Drying: —"),
            new Label("Maillard: —"),
            new Label("Development: —")));
        timesSection.setAnimated(true);

        TitledPane tempsSection = new TitledPane("Temperatures", new VBox(4,
            new Label("Charge: —"),
            new Label("DE: —"),
            new Label("FC: —"),
            new Label("Drop: —")));
        tempsSection.setAnimated(true);

        TitledPane weightSection = new TitledPane("Weight", new VBox(4,
            new Label("Green: — kg"),
            new Label("Roasted: — kg"),
            new Label("Loss: — %")));
        weightSection.setAnimated(true);

        getChildren().addAll(profileSection, timesSection, tempsSection, weightSection);
    }
}
