package org.artisan.ui.components;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.artisan.ui.vm.RoastViewModel;

/**
 * Minimal top strip (36px): hamburger | logo | phase pills + timer | connection dot + Settings.
 */
public final class RoastTopBar extends HBox {

    private final Label timerLabel;
    private final Label bbpBadge;
    private final Button bbpPauseBtn;
    private final Label connectionLabel;
    private Runnable onHamburger;
    private Runnable onSettings;
    private Runnable onResetLayout;
    private Runnable onKeyboardShortcuts;
    private Runnable onAbout;

    public RoastTopBar(RoastViewModel vm, Runnable onBbpPauseToggle) {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0, 12, 0, 8));
        setMinHeight(36);
        setPrefHeight(36);
        setMaxHeight(36);
        getStyleClass().add("top-strip");

        Button hamburgerBtn = new Button("\u2630");
        hamburgerBtn.getStyleClass().add("top-strip-icon-btn");
        hamburgerBtn.setMinSize(36, 36);
        hamburgerBtn.setPrefSize(36, 36);
        hamburgerBtn.setOnAction(e -> { if (onHamburger != null) onHamburger.run(); });

        Label logoLabel = new Label("AJ");
        logoLabel.getStyleClass().add("top-strip-logo");
        logoLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: #e8e8f0;");

        Region leftSpacer = new Region();
        leftSpacer.setMinWidth(4);
        leftSpacer.setMaxWidth(4);

        Label phaseLabel = new Label("");
        phaseLabel.getStyleClass().add("top-strip-phase");
        phaseLabel.textProperty().bind(vm.currentPhaseNameProperty());
        phaseLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaacc;");

        Region centerSpacer = new Region();
        HBox.setHgrow(centerSpacer, Priority.ALWAYS);

        timerLabel = new Label("00:00");
        timerLabel.getStyleClass().add("top-strip-timer");
        timerLabel.textProperty().bind(Bindings.createStringBinding(
            () -> {
                long sec = vm.isBbtActive() ? vm.getBbpElapsedSeconds() : vm.getElapsedSeconds();
                return String.format("%02d:%02d", sec / 60, sec % 60);
            },
            vm.elapsedSecondsProperty(), vm.bbpElapsedSecondsProperty(), vm.bbtActiveProperty()));
        timerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;");

        bbpBadge = new Label("BBP");
        bbpBadge.getStyleClass().add("bbp-badge");
        bbpBadge.visibleProperty().bind(vm.bbtActiveProperty());
        bbpBadge.managedProperty().bind(vm.bbtActiveProperty());

        bbpPauseBtn = new Button("\u23F8");
        bbpPauseBtn.getStyleClass().add("bbp-pause-btn");
        bbpPauseBtn.visibleProperty().bind(vm.bbtActiveProperty());
        bbpPauseBtn.managedProperty().bind(vm.bbtActiveProperty());
        bbpPauseBtn.setOnAction(e -> { if (onBbpPauseToggle != null) onBbpPauseToggle.run(); });

        Region rightSpacer = new Region();
        rightSpacer.setMinWidth(8);
        rightSpacer.setMaxWidth(8);

        connectionLabel = new Label("\u25CF Disconnected");
        connectionLabel.getStyleClass().add("top-strip-connection");
        connectionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8888a0;");
        connectionLabel.textProperty().bind(Bindings.createStringBinding(
            () -> "\u25CF " + vm.connectionStatusProperty().get(),
            vm.connectionStatusProperty()));

        MenuButton settingsBtn = new MenuButton("\u2699");
        settingsBtn.getStyleClass().add("top-strip-icon-btn");
        settingsBtn.setStyle("-fx-font-size: 16px;");

        MenuItem preferencesItem = new MenuItem("Preferences (Ctrl+,)");
        preferencesItem.setOnAction(e -> { if (onSettings != null) onSettings.run(); });

        MenuItem resetLayoutItem = new MenuItem("Reset Layout (Ctrl+Shift+R)");
        resetLayoutItem.setOnAction(e -> { if (onResetLayout != null) onResetLayout.run(); });

        MenuItem shortcutsItem = new MenuItem("Keyboard Shortcuts (F1)");
        shortcutsItem.setOnAction(e -> { if (onKeyboardShortcuts != null) onKeyboardShortcuts.run(); });

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> { if (onAbout != null) onAbout.run(); });

        settingsBtn.getItems().addAll(preferencesItem, resetLayoutItem, shortcutsItem, aboutItem);

        getChildren().addAll(hamburgerBtn, logoLabel, leftSpacer, phaseLabel,
                centerSpacer, timerLabel, bbpBadge, bbpPauseBtn, rightSpacer,
                connectionLabel, settingsBtn);
    }

    public RoastTopBar(RoastViewModel vm) {
        this(vm, null);
    }

    public void setOnHamburger(Runnable onHamburger) {
        this.onHamburger = onHamburger;
    }

    public void setOnSettings(Runnable onSettings) {
        this.onSettings = onSettings;
    }

    public void setOnResetLayout(Runnable onResetLayout) {
        this.onResetLayout = onResetLayout;
    }

    public void setOnKeyboardShortcuts(Runnable onKeyboardShortcuts) {
        this.onKeyboardShortcuts = onKeyboardShortcuts;
    }

    public void setOnAbout(Runnable onAbout) {
        this.onAbout = onAbout;
    }

    public void setConnectionStatus(String status) {
        // handled via vm binding; kept for compatibility
    }
}
