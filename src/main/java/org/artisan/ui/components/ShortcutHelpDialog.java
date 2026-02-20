package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * In-app shortcuts help dialog. Lists keyboard shortcuts for Roast Live screen.
 */
public final class ShortcutHelpDialog {

    private static final String[][] SHORTCUTS = {
        { "Ctrl+Shift+R", "Reset layout to defaults" },
        { "Space", "Add event marker at current time (quick note)" },
        { "1", "Charge" },
        { "2", "Yellow / Dry end" },
        { "3", "First crack start" },
        { "4", "First crack end" },
        { "5", "Drop" },
        { "C", "Toggle Controls panel visibility" },
        { "L", "Toggle Curve Legend panel" },
        { "E", "Focus Event Log" },
        { "?", "Show this help" },
    };

    public static void show(Window owner) {
        Dialog<Void> d = new Dialog<>();
        d.initOwner(owner);
        d.initModality(Modality.NONE);
        d.setTitle("Keyboard Shortcuts");
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        int row = 0;
        for (String[] rowData : SHORTCUTS) {
            Label key = new Label(rowData[0]);
            key.setStyle("-fx-font-weight: bold; -fx-min-width: 80;");
            Label desc = new Label(rowData[1]);
            grid.add(key, 0, row);
            grid.add(desc, 1, row);
            row++;
        }

        VBox content = new VBox(
            new Label("Roast Live screen shortcuts:"),
            grid
        );
        content.setSpacing(12);
        content.setPadding(new Insets(8));
        d.getDialogPane().setContent(content);
        d.showAndWait();
    }
}
