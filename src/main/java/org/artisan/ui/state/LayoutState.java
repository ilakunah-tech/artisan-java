package org.artisan.ui.state;

import java.util.ArrayList;
import java.util.List;

/**
 * Persisted layout state: panel order, collapsed state, dock width,
 * detached state and last window position/size per panel.
 */
public final class LayoutState {

    public static final double DEFAULT_DOCK_WIDTH = 340.0;
    public static final double MIN_DOCK_WIDTH = 280.0;
    public static final double MAX_DOCK_WIDTH = 640.0;

    /** Panel IDs for right dock (order preserved). */
    public static final String PANEL_LEGEND = "legend";
    public static final String PANEL_READOUTS = "readouts";
    public static final String PANEL_CONTROLS = "controls";
    public static final String PANEL_EVENT_LOG = "eventLog";

    private List<String> panelOrder;
    private double dockWidth;
    private boolean controlsVisible;

    /** For each panel ID: is it collapsed (accordion-style)? */
    private java.util.Map<String, Boolean> collapsed;
    /** For each panel ID: is it detached (in its own Stage)? */
    private java.util.Map<String, Boolean> detached;
    /** For detached panels: last window x, y, width, height. */
    private java.util.Map<String, WindowBounds> detachedBounds;

    public LayoutState() {
        panelOrder = new ArrayList<>();
        panelOrder.add(PANEL_LEGEND);
        panelOrder.add(PANEL_READOUTS);
        panelOrder.add(PANEL_CONTROLS);
        panelOrder.add(PANEL_EVENT_LOG);
        dockWidth = DEFAULT_DOCK_WIDTH;
        controlsVisible = true;
        collapsed = new java.util.HashMap<>();
        detached = new java.util.HashMap<>();
        detachedBounds = new java.util.HashMap<>();
    }

    public List<String> getPanelOrder() {
        return panelOrder != null ? new ArrayList<>(panelOrder) : defaultPanelOrder();
    }

    public void setPanelOrder(List<String> order) {
        this.panelOrder = order != null ? new ArrayList<>(order) : defaultPanelOrder();
    }

    private static List<String> defaultPanelOrder() {
        List<String> d = new ArrayList<>();
        d.add(PANEL_LEGEND);
        d.add(PANEL_READOUTS);
        d.add(PANEL_CONTROLS);
        d.add(PANEL_EVENT_LOG);
        return d;
    }

    public double getDockWidth() {
        return dockWidth <= 0 ? DEFAULT_DOCK_WIDTH : Math.min(MAX_DOCK_WIDTH, Math.max(MIN_DOCK_WIDTH, dockWidth));
    }

    public void setDockWidth(double dockWidth) {
        this.dockWidth = Math.min(MAX_DOCK_WIDTH, Math.max(MIN_DOCK_WIDTH, dockWidth));
    }

    public boolean isControlsVisible() {
        return controlsVisible;
    }

    public void setControlsVisible(boolean controlsVisible) {
        this.controlsVisible = controlsVisible;
    }

    public boolean isPanelCollapsed(String panelId) {
        return collapsed != null && Boolean.TRUE.equals(collapsed.get(panelId));
    }

    public void setPanelCollapsed(String panelId, boolean collapsed) {
        if (this.collapsed == null) this.collapsed = new java.util.HashMap<>();
        this.collapsed.put(panelId, collapsed);
    }

    public boolean isPanelDetached(String panelId) {
        return detached != null && Boolean.TRUE.equals(detached.get(panelId));
    }

    public void setPanelDetached(String panelId, boolean detached) {
        if (this.detached == null) this.detached = new java.util.HashMap<>();
        this.detached.put(panelId, detached);
    }

    public WindowBounds getDetachedBounds(String panelId) {
        return detachedBounds != null ? detachedBounds.get(panelId) : null;
    }

    public void setDetachedBounds(String panelId, WindowBounds bounds) {
        if (this.detachedBounds == null) this.detachedBounds = new java.util.HashMap<>();
        this.detachedBounds.put(panelId, bounds);
    }

    /** Last position/size of a detached panel window. */
    public static final class WindowBounds {
        public double x, y, width, height;

        public WindowBounds() {}

        public WindowBounds(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
