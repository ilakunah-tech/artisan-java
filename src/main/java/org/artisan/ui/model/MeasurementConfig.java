package org.artisan.ui.model;

import java.util.Objects;

/**
 * Configuration for a machine measurement tile (visible, name, size).
 */
public final class MeasurementConfig {

    public enum Size { S, L }

    private String name;
    private boolean visible;
    private Size size;

    public MeasurementConfig() {
        this("", true, Size.S);
    }

    public MeasurementConfig(String name, boolean visible, Size size) {
        this.name = name != null ? name : "";
        this.visible = visible;
        this.size = size != null ? size : Size.S;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name != null ? name : ""; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size != null ? size : Size.S; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeasurementConfig that = (MeasurementConfig) o;
        return visible == that.visible
                && Objects.equals(name, that.name)
                && size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, visible, size);
    }
}
