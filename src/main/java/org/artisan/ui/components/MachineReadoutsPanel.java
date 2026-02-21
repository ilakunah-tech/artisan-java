package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Machine readouts panel: draggable tiles for extra sensors.
 * Order persisted in LayoutState.machineReadoutsOrder.
 */
public final class MachineReadoutsPanel extends VBox {

    public MachineReadoutsPanel() {
        setSpacing(8);
        setPadding(new Insets(8));
        getStyleClass().add("ri5-dock-panel");
        FlowPane tiles = new FlowPane(8, 8);
        tiles.setPrefWrapLength(280);
        tiles.getChildren().addAll(
            readoutTile("Exhaust", "—"),
            readoutTile("Env", "—"),
            readoutTile("Pressure", "—")
        );
        getChildren().addAll(new Label("Machine"), tiles);
    }

    private static VBox readoutTile(String name, String value) {
        Label title = new Label(name);
        Label val = new Label(value);
        val.getStyleClass().add("ri5-readout-value");
        VBox tile = new VBox(2, title, val);
        tile.getStyleClass().addAll("ri5-readout-tile", "tile-s");
        return tile;
    }
}
