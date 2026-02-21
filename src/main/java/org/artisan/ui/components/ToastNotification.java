package org.artisan.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Toast notifications sliding in from bottom-right. Types: INFO, WARNING, SUCCESS, ERROR.
 * Auto-dismiss 4s; click to dismiss early. Stacks up to 4 visible.
 */
public final class ToastNotification {

    public enum ToastType {
        INFO("toast-info"),
        WARNING("toast-warning"),
        SUCCESS("toast-success"),
        ERROR("toast-error");

        final String styleClass;
        ToastType(String styleClass) { this.styleClass = styleClass; }
    }

    private static final int MAX_VISIBLE = 4;
    private static final int AUTO_DISMISS_MS = 4000;
    private static final int TOAST_WIDTH = 320;

    private static VBox toastStack;
    private static Pane overlayContainer;
    private static int visibleCount;

    /**
     * Show a toast. Call from any thread; runs on FX thread.
     * If owner is a Stage, finds the scene root and adds overlay there.
     */
    public static void show(Stage owner, ToastType type, String message) {
        Platform.runLater(() -> {
            if (owner == null) return;
            Node anchor = owner.getScene() != null ? owner.getScene().getRoot() : null;
            show(anchor, type, message);
        });
    }

    /**
     * Show a toast anchored to the given node (e.g. main root Pane).
     */
    public static void show(Node anchor, ToastType type, String message) {
        Platform.runLater(() -> {
            if (anchor == null) return;
            ensureStack(anchor);
            while (visibleCount >= MAX_VISIBLE && toastStack.getChildren().size() > 0) {
                toastStack.getChildren().remove(0);
                visibleCount--;
            }
            Label card = new Label(message);
            card.getStyleClass().addAll("toast-card", type.styleClass);
            card.setWrapText(true);
            card.setMaxWidth(TOAST_WIDTH);
            card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> dismissToast(card));
            toastStack.getChildren().add(card);
            visibleCount++;

            TranslateTransition tt = new TranslateTransition(Duration.millis(180), card);
            tt.setFromY(60);
            tt.setToY(0);
            tt.play();

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(AUTO_DISMISS_MS));
            pause.setOnFinished(e -> dismissToast(card));
            pause.play();
        });
    }

    private static void ensureStack(Node anchor) {
        if (toastStack != null && overlayContainer != null) return;
        toastStack = new VBox(8);
        toastStack.getStyleClass().add("toast-stack");
        StackPane wrapper = new StackPane(toastStack);
        wrapper.setAlignment(Pos.BOTTOM_RIGHT);
        wrapper.setMouseTransparent(true);
        wrapper.setPickOnBounds(false);
        StackPane.setMargin(toastStack, new Insets(0, 16, 16, 0));

        Pane parent = findPane(anchor);
        if (parent != null) {
            parent.getChildren().add(wrapper);
            overlayContainer = parent;
        }
    }

    private static Pane findPane(Node n) {
        if (n instanceof Pane) return (Pane) n;
        if (n instanceof javafx.scene.layout.BorderPane) return (Pane) n;
        if (n.getParent() != null) return findPane(n.getParent());
        return null;
    }

    private static void dismissToast(Label card) {
        if (toastStack == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(150), card);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            toastStack.getChildren().remove(card);
            visibleCount = Math.max(0, visibleCount - 1);
        });
        ft.play();
    }
}
