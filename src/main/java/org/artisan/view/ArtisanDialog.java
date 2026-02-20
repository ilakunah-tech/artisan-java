package org.artisan.view;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.lang.reflect.Method;

/**
 * Base dialog using JavaFX Stage + Scene.
 * Analog of ArtisanDialog from dialogs.py: standard OK/Cancel/Apply buttons,
 * modal or non-modal, ESC/close triggers cancel.
 */
public abstract class ArtisanDialog {

    private final Stage stage;
    private final Button okButton;
    private final Button cancelButton;
    private final Button applyButton;
    private final BorderPane root;
    private final boolean hasApply;
    private boolean resultOk;

    /**
     * Creates a base dialog with OK and Cancel. Optionally add Apply.
     *
     * @param owner  owner window (can be null)
     * @param modal  true for modal (blocking), false for non-modal
     * @param withApply true to show Apply button
     */
    protected ArtisanDialog(Window owner, boolean modal, boolean withApply) {
        this.hasApply = withApply;
        this.resultOk = false;

        root = new BorderPane();
        root.getStyleClass().add("artisan-dialog-root");

        ButtonBar buttonBar = new ButtonBar();
        okButton = new Button(ViewStrings.BUTTON_OK);
        okButton.setDefaultButton(true);
        okButton.setOnAction(this::onOk);
        cancelButton = new Button(ViewStrings.BUTTON_CANCEL);
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(this::onCancel);
        applyButton = new Button(ViewStrings.BUTTON_APPLY);
        applyButton.setOnAction(this::onApply);

        buttonBar.getButtons().addAll(cancelButton, okButton);
        if (withApply) {
            buttonBar.getButtons().add(applyButton);
        }
        root.setBottom(buttonBar);

        Scene scene = new Scene(root);
        applyTheme();
        applyBrandStylesheets(scene);
        stage = new Stage();
        stage.setScene(scene);
        stage.initOwner(owner);
        stage.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);
        stage.setOnCloseRequest(this::onCloseRequest);
        scene.setOnKeyPressed(this::onKeyPressed);
    }

    /** Default: modal, no Apply (OK + Cancel only). */
    protected ArtisanDialog(Window owner, boolean modal) {
        this(owner, modal, false);
    }

    private static void applyTheme() {
        try {
            Class<?> themeClass = Class.forName("io.github.mkpaz.atlantafx.base.theme.PrimerLight");
            Object theme = themeClass.getDeclaredConstructor().newInstance();
            Method m = themeClass.getMethod("getUserAgentStylesheet");
            String css = (String) m.invoke(theme);
            Application.setUserAgentStylesheet(css);
        } catch (Throwable t) {
            // AtlantaFX optional; Modena fallback
        }
    }

    private static void applyBrandStylesheets(Scene scene) {
        try {
            scene.getStylesheets().add(ArtisanDialog.class.getResource("/org/artisan/ui/theme/tokens.css").toExternalForm());
            scene.getStylesheets().add(ArtisanDialog.class.getResource("/org/artisan/ui/theme/light-brand.css").toExternalForm());
        } catch (Exception ignored) {}
    }

    /** Content to show in center of the dialog. */
    protected abstract Node buildContent();

    /** Called after layout is ready. Override to add content. */
    protected void initLayout() {
        Node content = buildContent();
        if (content != null) {
            root.setCenter(content instanceof Pane ? content : new BorderPane(content));
        }
    }

    /** Show Apply button (call before show). */
    protected void setShowApply(boolean show) {
        if (show && !buttonBar().getButtons().contains(applyButton)) {
            buttonBar().getButtons().add(applyButton);
        } else if (!show) {
            buttonBar().getButtons().remove(applyButton);
        }
    }

    private ButtonBar buttonBar() {
        return (ButtonBar) root.getBottom();
    }

    public Stage getStage() {
        return stage;
    }

    public Button getOkButton() {
        return okButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    /** Whether the dialog was closed with OK. */
    public boolean isResultOk() {
        return resultOk;
    }

    /** Override to run logic on OK; then call super.onOk(e) to close with result true. */
    protected void onOk(ActionEvent e) {
        resultOk = true;
        stage.close();
    }

    /** Override to run logic on Cancel. Default closes with result false. */
    protected void onCancel(ActionEvent e) {
        resultOk = false;
        stage.close();
    }

    /** Override to run logic on Apply (no close). Default does nothing. */
    protected void onApply(ActionEvent e) {
        // subclasses may override
    }

    private void onCloseRequest(WindowEvent e) {
        resultOk = false;
        // allow close
    }

    private void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE) {
            onCancel(null);
            e.consume();
        }
    }

    /** Show dialog and wait. Returns true if user chose OK. */
    public boolean showAndWait() {
        initLayout();
        stage.showAndWait();
        return resultOk;
    }

    /** Show dialog without blocking. */
    public void show() {
        initLayout();
        stage.show();
    }

    /** Hide the dialog. */
    public void hide() {
        stage.hide();
    }
}
