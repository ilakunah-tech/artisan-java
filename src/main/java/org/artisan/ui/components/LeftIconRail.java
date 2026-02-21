package org.artisan.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/**
 * Narrow left icon rail (28px wide) with three action buttons:
 * cloud (account), flame (roast properties), cup (settings).
 */
public final class LeftIconRail extends VBox {

    private final Button cloudBtn;
    private final Button flameBtn;
    private final Button cupBtn;

    public LeftIconRail(Runnable onCloud, Runnable onFlame, Runnable onCup) {
        getStyleClass().add("left-icon-rail");
        setAlignment(Pos.TOP_CENTER);
        setPadding(new javafx.geometry.Insets(8, 0, 0, 0));
        setSpacing(4);
        setPrefWidth(30);
        setMinWidth(30);
        setMaxWidth(30);

        cloudBtn = createIconButton("\u2601", "Website");
        flameBtn = createIconButton("\uD83D\uDD25", "Roast Properties");
        cupBtn   = createIconButton("\u2615", "Settings");

        cloudBtn.setOnAction(e -> { if (onCloud != null) onCloud.run(); });
        flameBtn.setOnAction(e -> { if (onFlame != null) onFlame.run(); });
        cupBtn.setOnAction(e -> { if (onCup != null) onCup.run(); });

        getChildren().addAll(cloudBtn, flameBtn, cupBtn);
    }

    private Button createIconButton(String symbol, String tooltipText) {
        Button btn = new Button(symbol);
        btn.getStyleClass().add("icon-rail-btn");
        btn.setTooltip(new Tooltip(tooltipText));
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    public void setCloudActive(boolean active) {
        setActive(cloudBtn, active);
    }

    public void setFlameActive(boolean active) {
        setActive(flameBtn, active);
    }

    public void setCupActive(boolean active) {
        setActive(cupBtn, active);
    }

    private void setActive(Button btn, boolean active) {
        if (active) {
            if (!btn.getStyleClass().contains("active")) btn.getStyleClass().add("active");
        } else {
            btn.getStyleClass().remove("active");
        }
    }
}
