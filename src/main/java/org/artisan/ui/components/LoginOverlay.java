package org.artisan.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * Login overlay card shown on top of the main layout.
 * Semi-transparent background dismisses on click; Escape key dismisses the card.
 */
public final class LoginOverlay extends StackPane {

    public LoginOverlay() {
        setVisible(false);
        setManaged(false);
        setPickOnBounds(false);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Pane bgPane = new Pane();
        bgPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        bgPane.setStyle("-fx-background-color: rgba(0,0,0,0.08);");
        bgPane.setOnMouseClicked(e -> hide());

        VBox card = buildCard();
        card.setMaxWidth(320);
        StackPane.setAlignment(card, Pos.TOP_LEFT);
        StackPane.setMargin(card, new Insets(40, 0, 0, 36));
        card.setOnMouseClicked(Event::consume);

        getChildren().addAll(bgPane, card);
    }

    private VBox buildCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("login-card");
        card.setPadding(new Insets(24, 20, 20, 20));

        Label title = new Label("Welcome Back!");
        title.getStyleClass().add("login-title");

        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add("login-field-label");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email address");
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("login-field-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        Button loginBtn = new Button("Log in");
        loginBtn.getStyleClass().add("btn-login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            System.out.println("Login: " + emailField.getText());
            hide();
        });

        Hyperlink forgotLink = new Hyperlink("Forgot your password?");
        forgotLink.setStyle("-fx-text-fill: #9CA3AF;");
        forgotLink.setMaxWidth(Double.MAX_VALUE);
        forgotLink.setAlignment(Pos.CENTER);
        forgotLink.setOnAction(e -> System.out.println("Forgot password"));

        card.getChildren().addAll(title, emailLabel, emailField, passLabel, passwordField, loginBtn, forgotLink);
        return card;
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
