package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Panel with title bar and icons: collapse, detach, close.
 * Content is the provided Node; collapse hides/show content; detach moves to separate Stage.
 */
public final class DockPanel extends VBox {

    private static final String STYLE_DOCK = "ri5-dock-panel";
    private static final String STYLE_TITLE_BAR = "title-bar";
    private static final double TITLE_HEIGHT = 28;

    private final String panelId;
    private final Label titleLabel;
    private final VBox contentBox;
    private final Button collapseBtn;
    private final Button detachBtn;
    private boolean collapsed;
    private boolean detached;
    private Runnable onCollapseToggle;
    private Runnable onDetach;
    private Runnable onRedock;

    public DockPanel(String panelId, String title, Node content) {
        this.panelId = panelId;
        getStyleClass().add(STYLE_DOCK);

        titleLabel = new Label(title != null ? title : panelId);
        titleLabel.getStyleClass().add("title-label");

        collapseBtn = new Button("\u2212");
        collapseBtn.setTooltip(new Tooltip("Collapse"));
        collapseBtn.getStyleClass().add("icon-button");
        collapseBtn.setMaxSize(24, 24);
        collapseBtn.setMinSize(24, 24);

        detachBtn = new Button("\u29C9");
        detachBtn.setTooltip(new Tooltip("Detach"));
        detachBtn.getStyleClass().add("icon-button");
        detachBtn.setMaxSize(24, 24);
        detachBtn.setMinSize(24, 24);

        HBox titleBar = new HBox(6);
        titleBar.getStyleClass().add(STYLE_TITLE_BAR);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setMinHeight(TITLE_HEIGHT);
        titleBar.setPadding(new Insets(0, 8, 0, 8));
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleBar.getChildren().addAll(titleLabel, collapseBtn, detachBtn);

        contentBox = new VBox(content);
        contentBox.getStyleClass().add("content");
        contentBox.setPadding(new Insets(8));
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        getChildren().addAll(titleBar, contentBox);

        collapsed = false;
        detached = false;

        collapseBtn.setOnAction(e -> {
            collapsed = !collapsed;
            contentBox.setManaged(!collapsed);
            contentBox.setVisible(!collapsed);
            collapseBtn.setText(collapsed ? "+" : "\u2212");
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
        contentBox.setManaged(!collapsed);
        contentBox.setVisible(!collapsed);
        collapseBtn.setText(collapsed ? "+" : "\u2212");
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
}
