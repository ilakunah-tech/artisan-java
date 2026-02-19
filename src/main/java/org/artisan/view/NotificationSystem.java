package org.artisan.view;

import java.util.logging.Logger;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.util.Duration;

/**
 * Lightweight toast notification overlay for MainWindow.
 * Shows a semi-transparent label at bottom-center of anchor, auto-dismisses after 4 seconds.
 */
public final class NotificationSystem {

    private static final Logger LOG = Logger.getLogger(NotificationSystem.class.getName());
    private static final int DISMISS_MS = 4000;
    private static final int FADE_MS = 200;
    private static final String TEXT_COLOR = "white";

    private NotificationSystem() {}

    /**
     * Shows a toast notification on the anchor node (e.g. main root).
     * Appears at bottom-center, slides in/out with fade, auto-dismisses after 4 seconds.
     *
     * @param anchor  the node to overlay (e.g. root BorderPane or StackPane)
     * @param message text to show
     * @param level   INFO, WARNING, or ERROR (determines background color)
     */
    public static void show(Node anchor, String message, NotificationLevel level) {
        if (anchor == null || message == null) return;
        NotificationLevel lvl = level != null ? level : NotificationLevel.INFO;
        String bg = lvl.getHexColor();
        Label label = new Label(message);
        label.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-padding: 10 16; -fx-font-size: 14px; -fx-background-radius: 4px;",
            bg, TEXT_COLOR));
        label.setMaxWidth(Double.MAX_VALUE);

        StackPane overlay = new StackPane(label);
        overlay.setAlignment(Pos.BOTTOM_CENTER);
        overlay.setMouseTransparent(true);
        overlay.setPickOnBounds(false);
        StackPane.setMargin(label, new javafx.geometry.Insets(0, 0, 24, 0));

        StackPane container;
        if (anchor instanceof StackPane) {
            container = (StackPane) anchor;
        } else if (anchor.getScene() != null) {
            Scene scene = anchor.getScene();
            Node root = scene.getRoot();
            if (root instanceof StackPane) {
                container = (StackPane) root;
            } else {
                LOG.warning("NotificationSystem: anchor is not StackPane and scene root is not StackPane; cannot show notification");
                return;
            }
        } else {
            LOG.warning("NotificationSystem: anchor has no scene; cannot show notification");
            return;
        }
        container.getChildren().add(overlay);
        showThenRemove(overlay, container);
    }

    private static void showThenRemove(StackPane overlay, StackPane container) {
        overlay.setOpacity(0);
        FadeTransition in = new FadeTransition(Duration.millis(FADE_MS), overlay);
        in.setToValue(0.9);
        in.setOnFinished(e -> {
            Timeline dismiss = new Timeline(new KeyFrame(Duration.millis(DISMISS_MS), ev -> {
                FadeTransition out = new FadeTransition(Duration.millis(FADE_MS), overlay);
                out.setToValue(0);
                out.setOnFinished(e2 -> {
                    if (container != null && container.getChildren().contains(overlay)) {
                        container.getChildren().remove(overlay);
                    }
                });
                out.play();
            }));
            dismiss.play();
        });
        in.play();
    }
}
