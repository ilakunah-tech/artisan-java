package org.artisan.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.artisan.model.ModulationAction;
import org.artisan.model.ModulationAction.ActionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a 4-slot scrolling view of upcoming / recent modulation actions.
 * Slots: [past, next, soon1, soon2].
 */
public final class ModulationTimeline extends VBox {

    private List<ModulationAction> actions = new ArrayList<>();
    private int currentIndex = 0;

    private final SlotBar pastSlot;
    private final SlotBar nextSlot;
    private final SlotBar soon1Slot;
    private final SlotBar soon2Slot;

    private static final class SlotBar extends HBox {
        private final Label iconLabel;
        private final Label mainLabel;
        private final Label timeLabel;

        SlotBar(String styleClass) {
            getStyleClass().addAll("modulation-slot", styleClass);
            setMaxWidth(Double.MAX_VALUE);
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(8);
            setMinHeight(26);
            setPrefHeight(26);
            setMaxHeight(26);
            setPadding(new Insets(4, 10, 4, 10));

            iconLabel = new Label("\u2014");
            iconLabel.getStyleClass().add("slot-label");
            iconLabel.setStyle("-fx-font-size: 13px;");

            mainLabel = new Label("");
            mainLabel.getStyleClass().add("slot-label");
            mainLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            HBox.setHgrow(mainLabel, Priority.ALWAYS);

            timeLabel = new Label("");
            timeLabel.getStyleClass().add("slot-time");
            timeLabel.setStyle("-fx-font-size: 10px; -fx-opacity: 0.85;");

            getChildren().addAll(iconLabel, mainLabel, timeLabel);
        }

        void setAction(ModulationAction a) {
            if (a == null) {
                iconLabel.setText("\u2014");
                mainLabel.setText("");
                timeLabel.setText("");
                setOpacity(0.35);
                return;
            }
            setOpacity(1.0);
            iconLabel.setText(iconFor(a.getType()));
            mainLabel.setText(a.getLabel());
            timeLabel.setText(a.getTimeStr());
        }

        private String iconFor(ActionType t) {
            if (t == null) return "\u25cf";
            return switch (t) {
                case GAS      -> "\u26fd";
                case AIR      -> "\uD83D\uDCA8";
                case DRUM     -> "\u2699";
                case CHARGE   -> "\u25bc";
                case TP       -> "\u21a9";
                case DRY_END  -> "\u25cb";
                case FC_START -> "\u2605";
                case FC_END   -> "\u2606";
                case DROP     -> "\u25b2";
                default       -> "\u25cf";
            };
        }
    }

    public ModulationTimeline() {
        setSpacing(4);
        setPadding(new Insets(0));
        setMaxWidth(Double.MAX_VALUE);

        pastSlot  = new SlotBar("modulation-slot-past");
        nextSlot  = new SlotBar("modulation-slot-next");
        soon1Slot = new SlotBar("modulation-slot-soon1");
        soon2Slot = new SlotBar("modulation-slot-soon2");

        getChildren().addAll(pastSlot, nextSlot, soon1Slot, soon2Slot);

        pastSlot.setAction(null);
        nextSlot.setAction(null);
        soon1Slot.setAction(null);
        soon2Slot.setAction(null);
    }

    public void setActions(List<ModulationAction> list) {
        this.actions = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.currentIndex = 0;
        update(0);
    }

    public void update(double currentTimeSec) {
        currentIndex = 0;
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).getTriggerTimeSec() <= currentTimeSec) {
                currentIndex = i + 1;
            } else {
                break;
            }
        }

        pastSlot.setAction(currentIndex > 0
            ? actions.get(currentIndex - 1) : null);
        nextSlot.setAction(currentIndex < actions.size()
            ? actions.get(currentIndex) : null);
        soon1Slot.setAction(currentIndex + 1 < actions.size()
            ? actions.get(currentIndex + 1) : null);
        soon2Slot.setAction(currentIndex + 2 < actions.size()
            ? actions.get(currentIndex + 2) : null);
    }
}
