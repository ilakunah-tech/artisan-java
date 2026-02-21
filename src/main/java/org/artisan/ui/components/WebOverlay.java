package org.artisan.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.Duration;

/**
 * Simple WebView overlay panel.
 */
public final class WebOverlay extends StackPane {

    public WebOverlay() {
        setVisible(false);
        setManaged(false);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Pane bgPane = new Pane();
        bgPane.setStyle("-fx-background-color: rgba(0,0,0,0.08);");
        bgPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        bgPane.setOnMouseClicked(e -> hide());

        VBox card = new VBox();
        StackPane.setAlignment(card, Pos.TOP_LEFT);
        StackPane.setMargin(card, new Insets(40, 0, 0, 36));
        card.setMaxWidth(380);
        card.setPrefHeight(520);
        card.setStyle(
            "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #E5E7EB;" +
                "-fx-border-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 3);"
        );

        WebView webView = new WebView();
        webView.setPrefSize(380, 480);
        webView.getEngine().load("https://placeholder.com");
        card.getChildren().add(webView);
        card.setOnMouseClicked(e -> e.consume());

        getChildren().addAll(bgPane, card);
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
