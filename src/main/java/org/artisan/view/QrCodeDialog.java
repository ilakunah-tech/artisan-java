package org.artisan.view;

import java.nio.file.Path;
import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.util.QrCodeGenerator;

import javafx.scene.image.WritableImage;

/**
 * Help → QR Code... non-modal dialog. Generates QR from current file URL or editable text.
 * Save PNG, Copy to Clipboard, Close. Preferences: qr.* for window position.
 */
public final class QrCodeDialog {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "qr.";
    private static final String DEFAULT_URL = "https://github.com/ilakunah-tech/artisan-java";
    private static final int QR_SIZE = 300;

    private final Stage stage;
    private final AppController appController;
    private final TextField textField;
    private final ImageView imageView;
    private WritableImage currentImage;

    public QrCodeDialog(Window owner, AppController appController) {
        this.appController = appController;
        stage = new Stage();
        stage.setTitle("QR Code");
        stage.initOwner(owner);
        stage.initModality(javafx.stage.Modality.NONE);

        String initialContent = contentFromController();
        textField = new TextField(initialContent);
        textField.setPromptText("URL or text to encode");
        textField.setMinWidth(320);

        Button generateBtn = new Button("Generate");
        generateBtn.setOnAction(e -> generate());

        imageView = new ImageView();
        imageView.setFitWidth(QR_SIZE);
        imageView.setFitHeight(QR_SIZE);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Button savePngBtn = new Button("Save PNG...");
        savePngBtn.setOnAction(e -> savePng());
        Button copyBtn = new Button("Copy to Clipboard");
        copyBtn.setOnAction(e -> copyToClipboard());
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> stage.hide());

        HBox buttons = new HBox(10, savePngBtn, copyBtn, closeBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.getChildren().addAll(textField, generateBtn, imageView, buttons);
        VBox.setMargin(imageView, new Insets(8, 0, 0, 0));

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 360, 480);
        stage.setScene(scene);

        loadPreferences();
        stage.setOnCloseRequest(e -> savePreferences());
    }

    private String contentFromController() {
        if (appController == null) return DEFAULT_URL;
        String path = appController.getCurrentFilePath();
        if (path != null && !path.isBlank()) {
            try {
                return Path.of(path).toUri().toString();
            } catch (Exception ignored) {}
        }
        return DEFAULT_URL;
    }

    private void generate() {
        String text = textField.getText();
        if (text == null) text = "";
        currentImage = QrCodeGenerator.generateQrFx(text, QR_SIZE);
        imageView.setImage(currentImage);
    }

    private void savePng() {
        String text = textField.getText();
        if (text == null) text = "";
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG (*.png)", "*.png"));
        java.io.File file = chooser.showSaveDialog(stage);
        if (file != null) {
            java.nio.file.Path path = file.toPath();
            String name = path.getFileName().toString();
            if (name.isEmpty() || !name.toLowerCase().endsWith(".png")) {
                path = path.getParent() != null ? path.getParent().resolve(name + ".png") : java.nio.file.Path.of(name + ".png");
            }
            QrCodeGenerator.saveQrPng(text, QR_SIZE, path);
        }
    }

    private void copyToClipboard() {
        if (currentImage == null) return;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putImage(currentImage);
        clipboard.setContent(content);
    }

    private void loadPreferences() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            double x = p.getDouble(PREFIX + "x", Double.NaN);
            double y = p.getDouble(PREFIX + "y", Double.NaN);
            double w = p.getDouble(PREFIX + "width", Double.NaN);
            double h = p.getDouble(PREFIX + "height", Double.NaN);
            if (Double.isFinite(x)) stage.setX(x);
            if (Double.isFinite(y)) stage.setY(y);
            if (Double.isFinite(w) && w > 100) stage.setWidth(w);
            if (Double.isFinite(h) && h > 100) stage.setHeight(h);
        } catch (Exception ignored) {}
    }

    private void savePreferences() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            p.putDouble(PREFIX + "x", stage.getX());
            p.putDouble(PREFIX + "y", stage.getY());
            p.putDouble(PREFIX + "width", stage.getWidth());
            p.putDouble(PREFIX + "height", stage.getHeight());
        } catch (Exception ignored) {}
    }

    public void show() {
        textField.setText(contentFromController());
        generate();
        loadPreferences();
        stage.show();
        stage.toFront();
    }
}
