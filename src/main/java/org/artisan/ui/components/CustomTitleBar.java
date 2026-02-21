package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Custom title bar for undecorated windows.
 */
public final class CustomTitleBar extends HBox {

    private final Label connectDot;
    private final Label connectLabel;
    private Runnable onConnect;
    private double xOffset;
    private double yOffset;

    public CustomTitleBar(Stage stage) {
        getStyleClass().add("custom-title-bar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(6);
        setPadding(new Insets(0, 8, 0, 8));
        setMinHeight(28);
        setPrefHeight(28);
        setMaxHeight(28);

        connectDot = new Label("\u25cf");
        connectDot.getStyleClass().add("title-bar-connect-dot");
        setConnected(false);

        connectLabel = new Label("Connect");
        connectLabel.getStyleClass().add("title-bar-connect-label");

        Tooltip connectTooltip = new Tooltip("Account / Login");
        connectDot.setTooltip(connectTooltip);
        connectLabel.setTooltip(connectTooltip);
        connectDot.setCursor(Cursor.HAND);
        connectLabel.setCursor(Cursor.HAND);
        connectDot.setOnMouseClicked(e -> { if (onConnect != null) onConnect.run(); });
        connectLabel.setOnMouseClicked(e -> { if (onConnect != null) onConnect.run(); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnMin = new Button("\u2014");
        Button btnMax = new Button("\u25a1");
        Button btnClose = new Button("\u00d7");

        btnMin.getStyleClass().add("title-bar-btn");
        btnMax.getStyleClass().add("title-bar-btn");
        btnClose.getStyleClass().addAll("title-bar-btn", "title-bar-btn-close");

        btnMin.setOnAction(e -> stage.setIconified(true));
        btnMax.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        btnClose.setOnAction(e -> stage.close());

        setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        });

        getChildren().addAll(connectDot, connectLabel, spacer, btnMin, btnMax, btnClose);
    }

    public void setConnected(boolean connected) {
        connectDot.setStyle("-fx-text-fill: " + (connected ? "#43A047" : "#E53935") + ";");
    }

    public void setOnConnect(Runnable onConnect) {
        this.onConnect = onConnect;
    }
}
