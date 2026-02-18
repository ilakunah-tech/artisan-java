package org.artisan;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Artisan JavaFX application entry point.
 */
public class Launcher extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("Artisan (Java)");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 200);
        stage.setScene(scene);
        stage.setTitle("Artisan");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
