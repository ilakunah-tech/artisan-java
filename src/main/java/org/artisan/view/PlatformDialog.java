package org.artisan.view;

import java.io.InputStream;
import java.util.Properties;
import java.awt.Desktop;
import java.net.URI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Help → About... modal. Shows app name, version, Java/JavaFX/OS, repo link (opens in browser).
 */
public final class PlatformDialog {

    private static final String REPO_URL = "https://github.com/ilakunah-tech/artisan-java";

    private final Stage stage;

    public PlatformDialog(Window owner) {
        stage = new Stage();
        stage.setTitle("About Artisan Java");
        stage.initOwner(owner);
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        String version = "?";
        try (InputStream in = getClass().getResourceAsStream("/version.properties")) {
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                version = p.getProperty("version", "?");
            }
        } catch (Exception ignored) {}

        Label appName = new Label("Artisan Java");
        appName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label versionLabel = new Label("Version: " + version);
        Label javaLabel = new Label("Java version: " + System.getProperty("java.version", "?"));
        Label javafxLabel = new Label("JavaFX version: " + System.getProperty("javafx.version", "?"));
        Label osLabel = new Label("OS: " + System.getProperty("os.name", "?") + " " + System.getProperty("os.version", ""));

        Label repoLabel = new Label(REPO_URL);
        repoLabel.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-cursor: hand;");
        repoLabel.setOnMouseClicked(e -> {
            try {
                Desktop.getDesktop().browse(URI.create(REPO_URL));
            } catch (Exception ex) {
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                        "Could not open URL: " + ex.getMessage()).showAndWait();
            }
        });

        VBox content = new VBox(8, appName, versionLabel, javaLabel, javafxLabel, osLabel, repoLabel);
        content.setPadding(new Insets(20));

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setOnAction(ev -> stage.close());

        BorderPane root = new BorderPane();
        root.setCenter(content);
        root.setBottom(okButton);
        BorderPane.setAlignment(okButton, Pos.CENTER_RIGHT);
        BorderPane.setMargin(okButton, new Insets(10));

        stage.setScene(new javafx.scene.Scene(root));
        stage.setResizable(false);
    }

    public void showAndWait() {
        stage.showAndWait();
    }
}
