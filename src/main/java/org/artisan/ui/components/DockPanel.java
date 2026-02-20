package org.artisan.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Panel with title bar and icons: collapse, detach, close.
 * Content is the provided Node; collapse hides/show content; detach moves to separate Stage.
 */
public final class DockPanel extends VBox {

    private static final String STYLE_DOCK = "ri5-dock-panel";
    private static final String STYLE_TITLE_BAR = "title-bar";
    private static final double TITLE_HEIGHT = 42;
    private static final double DEFAULT_EXPANDED_BODY_HEIGHT = 260;
    private static final Duration COLLAPSE_ANIMATION = Duration.millis(180);

    private final String panelId;
    private final Label titleLabel;
    private final VBox contentBox;
    private final StackPane contentWrapper;
    private final ScrollPane contentScroll;
    private final Button collapseBtn;
    private final Button menuBtn;
    private final Button detachBtn;
    private boolean collapsed;
    private boolean detached;
    private Runnable onCollapseToggle;
    private Runnable onDetach;
    private Runnable onRedock;

    public DockPanel(String panelId, String title, Node content) {
        this(panelId, title, content, (Node[]) null);
    }

    public DockPanel(String panelId, String title, Node content, Node... titleBarExtras) {
        this.panelId = panelId;
        getStyleClass().add(STYLE_DOCK);

        titleLabel = new Label(title != null ? title : panelId);
        titleLabel.getStyleClass().add("title-label");

        collapseBtn = new Button("\u25BE");
        collapseBtn.setTooltip(new Tooltip("Collapse"));
        collapseBtn.getStyleClass().add("icon-button");
        collapseBtn.setMaxSize(24, 24);
        collapseBtn.setMinSize(24, 24);

        menuBtn = new Button("\u22EF");
        menuBtn.setTooltip(new Tooltip("More actions"));
        menuBtn.getStyleClass().add("icon-button");
        menuBtn.setMaxSize(24, 24);
        menuBtn.setMinSize(24, 24);
        menuBtn.setDisable(true);
        menuBtn.setFocusTraversable(false);

        detachBtn = new Button("\u29C9");
        detachBtn.setTooltip(new Tooltip("Detach"));
        detachBtn.getStyleClass().add("icon-button");
        detachBtn.setMaxSize(24, 24);
        detachBtn.setMinSize(24, 24);

        HBox titleBar = new HBox(6);
        titleBar.getStyleClass().add(STYLE_TITLE_BAR);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setMinHeight(TITLE_HEIGHT);
        titleBar.setPrefHeight(TITLE_HEIGHT);
        titleBar.setMaxHeight(TITLE_HEIGHT);
        titleBar.setPadding(new Insets(0, 10, 0, 12));
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleBar.getChildren().add(titleLabel);
        if (titleBarExtras != null) {
            for (Node n : titleBarExtras) {
                if (n != null) titleBar.getChildren().add(n);
            }
        }
        titleBar.getChildren().addAll(collapseBtn, menuBtn, detachBtn);

        contentBox = new VBox(content);
        contentBox.setPadding(Insets.EMPTY);
        contentBox.setFillWidth(true);
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        contentScroll = new ScrollPane(contentBox);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setPannable(true);
        contentScroll.setMinHeight(0);
        contentScroll.getStyleClass().add("dock-card-body-scroll");

        contentWrapper = new StackPane(contentScroll);
        contentWrapper.getStyleClass().add("content");
        contentWrapper.setMinHeight(0);
        contentWrapper.setPrefHeight(Region.USE_COMPUTED_SIZE);
        contentWrapper.setMaxHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(contentWrapper, Priority.NEVER);

        getChildren().addAll(titleBar, contentWrapper);
        setFillWidth(true);

        collapsed = false;
        detached = false;

        collapseBtn.setOnAction(e -> {
            setCollapsed(!collapsed);
            if (onCollapseToggle != null) onCollapseToggle.run();
        });

        detachBtn.setOnAction(e -> {
            if (detached && onRedock != null) {
                onRedock.run();
                detached = false;
                detachBtn.setTooltip(new Tooltip("Detach"));
                detachBtn.setText("\u29C9");
            } else if (!detached && onDetach != null) {
                onDetach.run();
                detached = true;
                detachBtn.setTooltip(new Tooltip("Re-dock"));
                detachBtn.setText("\u25C4");
            }
        });
    }

    public String getPanelId() {
        return panelId;
    }

    public Node getContentNode() {
        return contentBox.getChildren().isEmpty() ? null : contentBox.getChildren().get(0);
    }

    public void setCollapsed(boolean collapsed) {
        if (this.collapsed == collapsed) return;
        this.collapsed = collapsed;
        if (collapsed) {
            animateCollapse();
        } else {
            animateExpand();
        }
        collapseBtn.setText(collapsed ? "\u25B8" : "\u25BE");
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setDetached(boolean detached) {
        this.detached = detached;
        detachBtn.setTooltip(new Tooltip(detached ? "Re-dock" : "Detach"));
        detachBtn.setText(detached ? "\u25C4" : "\u29C9");
    }

    public boolean isDetached() {
        return detached;
    }

    public void setOnCollapseToggle(Runnable onCollapseToggle) {
        this.onCollapseToggle = onCollapseToggle;
    }

    public void setOnDetach(Runnable onDetach) {
        this.onDetach = onDetach;
    }

    public void setOnRedock(Runnable onRedock) {
        this.onRedock = onRedock;
    }

    public void setTitle(String title) {
        titleLabel.setText(title != null ? title : panelId);
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    /** Removes and returns the content node (for detach). */
    public Node takeContent() {
        if (contentBox.getChildren().isEmpty()) return null;
        Node n = contentBox.getChildren().remove(0);
        return n;
    }

    /** Sets content back (for redock). */
    public void setContent(Node content) {
        contentBox.getChildren().clear();
        if (content != null) contentBox.getChildren().add(content);
    }

    private void animateCollapse() {
        double start = Math.max(contentWrapper.getHeight(), DEFAULT_EXPANDED_BODY_HEIGHT);
        contentWrapper.setManaged(true);
        contentWrapper.setVisible(true);
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(contentWrapper.prefHeightProperty(), start),
                new KeyValue(contentWrapper.maxHeightProperty(), start),
                new KeyValue(contentWrapper.opacityProperty(), 1.0)
            ),
            new KeyFrame(COLLAPSE_ANIMATION,
                new KeyValue(contentWrapper.prefHeightProperty(), 0),
                new KeyValue(contentWrapper.maxHeightProperty(), 0),
                new KeyValue(contentWrapper.opacityProperty(), 0.0)
            )
        );
        timeline.setOnFinished(e -> {
            contentWrapper.setVisible(false);
            contentWrapper.setManaged(false);
        });
        timeline.play();
    }

    private void animateExpand() {
        double target = computeExpandedHeight();
        contentWrapper.setManaged(true);
        contentWrapper.setVisible(true);
        contentWrapper.setPrefHeight(0);
        contentWrapper.setMaxHeight(0);
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(contentWrapper.prefHeightProperty(), 0),
                new KeyValue(contentWrapper.maxHeightProperty(), 0),
                new KeyValue(contentWrapper.opacityProperty(), 0.0)
            ),
            new KeyFrame(COLLAPSE_ANIMATION,
                new KeyValue(contentWrapper.prefHeightProperty(), target),
                new KeyValue(contentWrapper.maxHeightProperty(), target),
                new KeyValue(contentWrapper.opacityProperty(), 1.0)
            )
        );
        timeline.setOnFinished(e -> {
            contentWrapper.setPrefHeight(Region.USE_COMPUTED_SIZE);
            contentWrapper.setMaxHeight(Region.USE_COMPUTED_SIZE);
        });
        timeline.play();
    }

    private double computeExpandedHeight() {
        applyCss();
        layout();
        double availableWidth = Math.max(180, getWidth() - snappedLeftInset() - snappedRightInset());
        double preferred = contentBox.prefHeight(Math.max(120, availableWidth - 24));
        if (!Double.isFinite(preferred) || preferred <= 0) preferred = DEFAULT_EXPANDED_BODY_HEIGHT;
        return Math.max(120, Math.min(420, preferred + 16));
    }
}
