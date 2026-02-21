package org.artisan.ui.components;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Slide-out left drawer for roast properties (name, stock, blend, reference, comment, weights).
 * Width 320px, slides from the left.
 */
public final class LeftDrawer extends VBox {

    private static final double DRAWER_WIDTH  = 300.0;
    private static final double LEFT_OFFSET = 30.0;
    private static final double HIDE_PADDING = 10.0;
    private static final Duration SLIDE_DURATION = Duration.millis(250);

    private final TranslateTransition openAnim;
    private final TranslateTransition closeAnim;
    private boolean isOpen = false;

    private Consumer<String> onReferenceSelected;

    public LeftDrawer() {
        getStyleClass().add("left-drawer");
        setPrefWidth(DRAWER_WIDTH);
        setMaxWidth(DRAWER_WIDTH);
        setPrefHeight(Region.USE_COMPUTED_SIZE);
        setMaxHeight(560);
        setTranslateX(-(DRAWER_WIDTH + LEFT_OFFSET + HIDE_PADDING));

        buildContent();

        openAnim = new TranslateTransition(SLIDE_DURATION, this);
        openAnim.setToX(0);
        openAnim.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        closeAnim = new TranslateTransition(SLIDE_DURATION, this);
        closeAnim.setToX(-DRAWER_WIDTH);
        closeAnim.setInterpolator(javafx.animation.Interpolator.EASE_IN);
    }

    private void buildContent() {
        Label drawerTitle = new Label("Roast Properties");
        drawerTitle.setStyle(
            "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1A3A5C;" +
                "-fx-padding: 0 0 6 0;"
        );

        // 1. Roast name
        TextField roastNameField = new TextField();
        roastNameField.setPromptText("#Name Roast");
        roastNameField.setStyle(
            "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: rgba(255,255,255,0.85);" +
                "-fx-border-color: rgba(255,255,255,0.9);" +
                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                "-fx-padding: 10 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);"
        );
        roastNameField.setMaxWidth(Double.MAX_VALUE);

        // 2. Stock combo
        ComboBox<String> stockCombo = new ComboBox<>();
        stockCombo.setPromptText("Stock");
        stockCombo.setMaxWidth(Double.MAX_VALUE);
        stockCombo.setStyle(
            "-fx-background-color: rgba(255,255,255,0.75);" +
                "-fx-border-color: rgba(255,255,255,0.8);" +
                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 6 12;" +
                "-fx-pref-height: 36px;"
        );

        // 3. Blend combo
        ComboBox<String> blendCombo = new ComboBox<>();
        blendCombo.setPromptText("Blend");
        blendCombo.setMaxWidth(Double.MAX_VALUE);
        blendCombo.setStyle(
            "-fx-background-color: rgba(255,255,255,0.75);" +
                "-fx-border-color: rgba(255,255,255,0.8);" +
                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 6 12;" +
                "-fx-pref-height: 36px;"
        );

        // 4. Reference combo
        ComboBox<String> referenceCombo = new ComboBox<>();
        referenceCombo.setPromptText("Reference");
        referenceCombo.setMaxWidth(Double.MAX_VALUE);
        referenceCombo.setStyle(
            "-fx-background-color: rgba(255,255,255,0.75);" +
                "-fx-border-color: rgba(255,255,255,0.8);" +
                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 6 12;" +
                "-fx-pref-height: 36px;"
        );
        referenceCombo.getItems().addAll(
            "Ethiopia Natural 200g",
            "Brazil Cerrado 250g"
        );
        referenceCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && onReferenceSelected != null) {
                onReferenceSelected.accept(newVal);
            }
        });

        Separator sectionDivider = new Separator();
        sectionDivider.setStyle("-fx-background-color: rgba(255,255,255,0.35);");

        // 5. Comment area
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Comment batch");
        commentArea.setMaxWidth(Double.MAX_VALUE);
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);
        commentArea.setStyle(
            "-fx-background-color: rgba(255,255,255,0.7);" +
                "-fx-border-color: rgba(255,255,255,0.75);" +
                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                "-fx-font-size: 11px;"
        );

        // 6. Weight row
        TextField weightGreen = new TextField();
        weightGreen.setPromptText("138");
        weightGreen.getStyleClass().add("weight-field");

        TextField weightRoasted = new TextField();
        weightRoasted.setPromptText("138");
        weightRoasted.getStyleClass().add("weight-field");

        Label weightLabel = new Label("Weight:");
        weightLabel.setStyle("-fx-text-fill: #1A3A5C; -fx-font-weight: bold;");
        Label greenUnit = new Label("g");
        Label roastedUnit = new Label("g");

        HBox weightsRow = new HBox(6, weightLabel, weightGreen, greenUnit, weightRoasted, roastedUnit);
        weightsRow.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(
            drawerTitle,
            roastNameField,
            stockCombo,
            blendCombo,
            referenceCombo,
            sectionDivider,
            commentArea,
            weightsRow
        );
    }

    public void toggle() {
        if (isOpen) close(); else open();
    }

    public void open() {
        if (isOpen) return;
        isOpen = true;
        setVisible(true);
        setManaged(true);
        closeAnim.stop();
        openAnim.setToX(0);
        openAnim.setOnFinished(null);
        openAnim.playFromStart();
    }

    public void close() {
        if (!isOpen) return;
        isOpen = false;
        openAnim.stop();
        double hideX = -(getPrefWidth() + LEFT_OFFSET + HIDE_PADDING);
        closeAnim.setToX(hideX);
        closeAnim.setOnFinished(e -> {
            setVisible(false);
            setManaged(false);
        });
        closeAnim.playFromStart();
    }

    public boolean isOpen() { return isOpen; }

    public void setOnReferenceSelected(Consumer<String> handler) { this.onReferenceSelected = handler; }
}
