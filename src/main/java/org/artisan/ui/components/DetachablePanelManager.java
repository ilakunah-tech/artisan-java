package org.artisan.ui.components;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.artisan.ui.state.LayoutState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages detach/redock of panels: moves content Node into a new Stage or back to dock container.
 * Persists detached state and window bounds; restores on next startup.
 */
public final class DetachablePanelManager {

    private final Map<String, DockPanel> panels = new HashMap<>();
    private final Map<String, Stage> detachedStages = new HashMap<>();
    private final Map<String, javafx.scene.layout.VBox> dockContainers = new HashMap<>();
    private LayoutState layoutState;
    private Window primaryWindow;

    public void setLayoutState(LayoutState layoutState) {
        this.layoutState = layoutState;
    }

    public void setPrimaryWindow(Window primaryWindow) {
        this.primaryWindow = primaryWindow;
    }

    public void registerPanel(DockPanel panel, javafx.scene.layout.VBox dockContainer) {
        if (panel == null) return;
        String id = panel.getPanelId();
        panels.put(id, panel);
        if (dockContainer != null) dockContainers.put(id, dockContainer);

        panel.setOnDetach(() -> detach(id));
        panel.setOnRedock(() -> redock(id));
    }

    /** Detach panel into its own Stage. */
    public void detach(String panelId) {
        DockPanel panel = panels.get(panelId);
        if (panel == null || panel.isDetached()) return;

        javafx.scene.layout.VBox container = dockContainers.get(panelId);
        if (container != null) container.getChildren().remove(panel);

        Node contentNode = panel.takeContent();
        if (contentNode == null) return;

        Stage stage = new Stage();
        stage.setTitle(panel.getTitle());
        stage.initOwner(primaryWindow != null ? primaryWindow : null);
        javafx.scene.Scene scene = new javafx.scene.Scene(new javafx.scene.layout.StackPane(contentNode));
        scene.getStylesheets().addAll(primaryWindow != null && primaryWindow.getScene() != null
            ? primaryWindow.getScene().getStylesheets() : java.util.Collections.emptyList());
        stage.setScene(scene);

        LayoutState.WindowBounds bounds = layoutState != null ? layoutState.getDetachedBounds(panelId) : null;
        if (bounds != null && bounds.width > 0 && bounds.height > 0) {
            stage.setX(bounds.x);
            stage.setY(bounds.y);
            stage.setWidth(bounds.width);
            stage.setHeight(bounds.height);
            if (!isOnScreen(bounds)) {
                centerOnPrimary(stage);
            }
        } else {
            stage.setWidth(320);
            stage.setHeight(280);
            centerOnPrimary(stage);
        }

        stage.setOnCloseRequest(e -> redock(panelId));
        stage.xProperty().addListener((a, b, c) -> saveBounds(panelId, stage));
        stage.yProperty().addListener((a, b, c) -> saveBounds(panelId, stage));
        stage.widthProperty().addListener((a, b, c) -> saveBounds(panelId, stage));
        stage.heightProperty().addListener((a, b, c) -> saveBounds(panelId, stage));

        detachedStages.put(panelId, stage);
        panel.setDetached(true);
        if (layoutState != null) layoutState.setPanelDetached(panelId, true);
        stage.show();
    }

    /** Redock panel from its Stage back into the dock. */
    public void redock(String panelId) {
        Stage stage = detachedStages.remove(panelId);
        DockPanel panel = panels.get(panelId);
        if (panel == null) return;
        if (stage != null && stage.getScene() != null && stage.getScene().getRoot() != null) {
            javafx.scene.layout.Pane root = (javafx.scene.layout.Pane) stage.getScene().getRoot();
            if (!root.getChildren().isEmpty()) {
                Node content = root.getChildren().remove(0);
                panel.setContent(content);
            }
            stage.close();
        }
        panel.setDetached(false);
        javafx.scene.layout.VBox container = dockContainers.get(panelId);
        if (container != null && !container.getChildren().contains(panel)) {
            int idx = indexForPanel(panelId, container);
            container.getChildren().add(idx, panel);
        }
        if (layoutState != null) layoutState.setPanelDetached(panelId, false);
    }

    private int indexForPanel(String panelId, javafx.scene.layout.VBox container) {
        java.util.List<String> order = layoutState != null ? layoutState.getPanelOrder() : null;
        if (order == null) return container.getChildren().size();
        int targetIdx = order.indexOf(panelId);
        if (targetIdx < 0) return container.getChildren().size();
        for (int i = 0; i < container.getChildren().size(); i++) {
            Node n = container.getChildren().get(i);
            if (n instanceof DockPanel) {
                int o = order.indexOf(((DockPanel) n).getPanelId());
                if (o > targetIdx) return i;
            }
        }
        return container.getChildren().size();
    }

    private void saveBounds(String panelId, Stage stage) {
        if (layoutState != null && stage.isShowing()) {
            LayoutState.WindowBounds b = new LayoutState.WindowBounds(
                stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            layoutState.setDetachedBounds(panelId, b);
        }
    }

    private boolean isOnScreen(LayoutState.WindowBounds b) {
        for (Screen s : Screen.getScreens()) {
            Rectangle2D bounds = s.getVisualBounds();
            if (b.x >= bounds.getMinX() && b.x + b.width <= bounds.getMaxX()
                && b.y >= bounds.getMinY() && b.y + b.height <= bounds.getMaxY())
                return true;
        }
        return false;
    }

    private void centerOnPrimary(Stage stage) {
        if (primaryWindow != null && primaryWindow instanceof javafx.stage.Stage) {
            javafx.stage.Stage primary = (javafx.stage.Stage) primaryWindow;
            stage.setX(primary.getX() + (primary.getWidth() - stage.getWidth()) / 2);
            stage.setY(primary.getY() + (primary.getHeight() - stage.getHeight()) / 2);
        } else {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2);
            stage.setY(bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2);
        }
    }

    public boolean isDetached(String panelId) {
        return detachedStages.containsKey(panelId);
    }

    public void closeAllDetached() {
        for (String id : new HashMap<>(detachedStages).keySet()) {
            redock(id);
        }
    }

    /**
     * Writes current bounds of all detached stages to LayoutState (for persist on exit).
     */
    public void syncDetachedBoundsToLayoutState() {
        if (layoutState == null) return;
        for (Map.Entry<String, Stage> e : detachedStages.entrySet()) {
            Stage stage = e.getValue();
            if (stage != null && stage.isShowing()) {
                LayoutState.WindowBounds b = new LayoutState.WindowBounds(
                    stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
                layoutState.setDetachedBounds(e.getKey(), b);
            }
        }
    }

    /**
     * Restores panels that were saved as detached: opens each in its own Stage.
     * Call after main window is shown (e.g. Platform.runLater after stage.show()).
     */
    public void restoreDetachedPanels() {
        if (layoutState == null) return;
        List<String> order = layoutState.getPanelOrder();
        if (order == null) return;
        for (String panelId : order) {
            if (layoutState.isPanelDetached(panelId) && panels.containsKey(panelId)) {
                DockPanel panel = panels.get(panelId);
                if (panel != null && !panel.isDetached()) {
                    detach(panelId);
                }
            }
        }
    }
}
