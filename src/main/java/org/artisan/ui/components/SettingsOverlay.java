package org.artisan.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Settings overlay panel shown in the center area when the settings icon is active.
 */
public final class SettingsOverlay extends StackPane {

    private final StackPane contentArea;
    private final ToggleGroup tabGroup;

    private static final String[] TAB_NAMES = {"General", "Device", "Colors", "Events", "Logging", "Advanced"};

    public SettingsOverlay() {
        setVisible(false);
        setManaged(false);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        Pane bgPane = new Pane();
        bgPane.setStyle("-fx-background-color: rgba(0,0,0,0.15);");
        bgPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        bgPane.prefWidthProperty().bind(widthProperty());
        bgPane.prefHeightProperty().bind(heightProperty());
        bgPane.setOnMouseClicked(e -> hide());

        VBox card = new VBox(12);
        card.getStyleClass().add("settings-card");
        card.setMaxWidth(720);
        card.setMaxHeight(520);
        card.setOnMouseClicked(Event::consume);

        Label header = new Label("Setting:");
        header.getStyleClass().add("settings-header");

        tabGroup = new ToggleGroup();
        HBox tabRow = new HBox(8);
        tabRow.setPadding(new Insets(0, 0, 8, 0));

        VBox[] tabPanes = new VBox[TAB_NAMES.length];
        for (int i = 0; i < TAB_NAMES.length; i++) {
            ToggleButton tab = new ToggleButton(TAB_NAMES[i]);
            tab.getStyleClass().add("settings-tab");
            tab.setToggleGroup(tabGroup);
            tabRow.getChildren().add(tab);
            tabPanes[i] = buildTabContent(TAB_NAMES[i]);
            if (i == 0) tab.setSelected(true);
        }

        contentArea = new StackPane();
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        for (int i = 0; i < tabPanes.length; i++) {
            tabPanes[i].setVisible(i == 0);
            tabPanes[i].setManaged(i == 0);
            contentArea.getChildren().add(tabPanes[i]);
        }

        tabGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                if (oldToggle != null) oldToggle.setSelected(true);
                return;
            }
            int selectedIdx = tabRow.getChildren().indexOf((Node) newToggle);
            for (int i = 0; i < tabPanes.length; i++) {
                tabPanes[i].setVisible(i == selectedIdx);
                tabPanes[i].setManaged(i == selectedIdx);
            }
        });

        card.getChildren().addAll(header, tabRow, contentArea);
        StackPane.setAlignment(card, Pos.CENTER);

        getChildren().addAll(bgPane, card);
    }

    private VBox buildTabContent(String tabName) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(12));
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        if ("Device".equals(tabName)) {
            Label portLabel = new Label("Serial Port:");
            TextField portField = new TextField();
            portField.setPromptText("COM3");
            portField.setMaxWidth(Double.MAX_VALUE);

            Label baudLabel = new Label("Baud Rate:");
            ComboBox<String> baudCombo = new ComboBox<>();
            baudCombo.getItems().addAll("9600", "19200", "38400", "57600", "115200");
            baudCombo.setMaxWidth(Double.MAX_VALUE);

            pane.getChildren().addAll(portLabel, portField, baudLabel, baudCombo);
        } else {
            pane.getChildren().add(new Label("Coming soon\u2026"));
        }
        return pane;
    }

    public void show() {
        setOpacity(0);
        setVisible(true);
        setManaged(true);
        toFront();
        FadeTransition ft = new FadeTransition(Duration.millis(200), this);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    public void hide() {
        FadeTransition ft = new FadeTransition(Duration.millis(160), this);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setInterpolator(Interpolator.EASE_IN);
        ft.setOnFinished(e -> {
            setVisible(false);
            setManaged(false);
        });
        ft.play();
    }

    public void toggle() {
        if (isVisible()) hide(); else show();
    }
}
