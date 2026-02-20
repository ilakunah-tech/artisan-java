package org.artisan.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.nio.file.Path;
import java.util.function.Consumer;
import org.artisan.controller.AppController;
import org.artisan.controller.CommController;
import org.artisan.ui.screens.PreRoastScreen;
import org.artisan.ui.screens.RoastLiveScreen;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;

/**
 * Main window layout: top bar (app name, nav Pre-Roast | Roast Live, connection, settings),
 * center = current screen content.
 */
public final class AppShell {

    private final BorderPane root;
    private final HBox topBar;
    private final StackPane contentArea;
    private final ToggleButton preRoastNav;
    private final ToggleButton roastLiveNav;
    private final Label machineLabel;
    private final Label connectionLabel;
    private final PreRoastScreen preRoastScreen;
    private final RoastLiveScreen roastLiveScreen;
    private final AppController appController;
    private final UIPreferences uiPreferences;
    private final PreferencesStore preferencesStore;
    private Runnable onSettings;
    private Runnable onResetLayout;
    private Runnable onCurveVisibilitySync;
    private Runnable onOpenReplay;
    private Consumer<Path> onOpenRecent;
    private DemoRunner demoRunner;

    public AppShell(Stage primaryStage, AppController appController,
                    org.artisan.view.RoastChartController chartController,
                    org.artisan.controller.DisplaySettings displaySettings,
                    UIPreferences uiPreferences, PreferencesStore preferencesStore) {
        this.appController = appController;
        this.uiPreferences = uiPreferences != null ? uiPreferences : new UIPreferences();
        this.preferencesStore = preferencesStore != null ? preferencesStore : new PreferencesStore();

        root = new BorderPane();
        root.getStyleClass().add("ri5-root");

        topBar = new HBox(12);
        topBar.getStyleClass().add("ri5-topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 12, 8, 12));

        Label appName = new Label("Artisan Java");
        appName.getStyleClass().add("app-name");

        machineLabel = new Label("—");
        machineLabel.getStyleClass().add("machine-indicator");
        machineLabel.setTooltip(new Tooltip("Configured device (Settings → Device)"));

        preRoastNav = new ToggleButton("Pre-Roast");
        preRoastNav.setSelected(true);
        preRoastNav.setTooltip(new Tooltip("Setup profile and batch"));
        roastLiveNav = new ToggleButton("Roast (Live)");
        roastLiveNav.setSelected(false);
        roastLiveNav.setTooltip(new Tooltip("Live roast chart and controls"));
        javafx.scene.control.ToggleGroup navGroup = new javafx.scene.control.ToggleGroup();
        preRoastNav.setToggleGroup(navGroup);
        roastLiveNav.setToggleGroup(navGroup);

        connectionLabel = new Label("Disconnected");
        connectionLabel.getStyleClass().add("connection-status");

        MenuButton settingsBtn = new MenuButton("Settings");
        MenuItem devicesItem = new MenuItem("Devices...");
        devicesItem.setOnAction(e -> {
            if (onSettings != null) onSettings.run();
        });
        MenuItem resetLayoutItem = new MenuItem("Reset Layout...");
        resetLayoutItem.setOnAction(e -> {
            if (onResetLayout != null) onResetLayout.run();
        });
        settingsBtn.getItems().addAll(devicesItem, resetLayoutItem);

        topBar.getChildren().addAll(appName, machineLabel, preRoastNav, roastLiveNav, connectionLabel, settingsBtn);
        HBox.setMargin(machineLabel, new Insets(0, 0, 0, 16));
        HBox.setMargin(connectionLabel, new Insets(0, 0, 0, 24));

        root.setTop(topBar);
        contentArea = new StackPane();
        contentArea.setMinSize(0, 0);
        preRoastScreen = new PreRoastScreen(appController, this.uiPreferences, this.preferencesStore);
        roastLiveScreen = new RoastLiveScreen(primaryStage, appController, chartController,
            displaySettings, this.uiPreferences, this.preferencesStore);

        contentArea.getChildren().add(preRoastScreen.getRoot());
        contentArea.getChildren().add(roastLiveScreen.getRoot());
        javafx.scene.layout.StackPane.setAlignment(preRoastScreen.getRoot(), javafx.geometry.Pos.TOP_LEFT);
        javafx.scene.layout.StackPane.setAlignment(roastLiveScreen.getRoot(), javafx.geometry.Pos.TOP_LEFT);
        roastLiveScreen.getRoot().setVisible(false);

        preRoastNav.setOnAction(e -> {
            if (preRoastNav.isSelected()) switchToPreRoast();
        });
        roastLiveNav.setOnAction(e -> {
            if (roastLiveNav.isSelected()) switchToRoastLive();
        });

        preRoastScreen.setOnStartRoast(() -> {
            appController.startSampling();
            updateRoastLiveEnabled();
            switchToRoastLive();
        });
        preRoastScreen.setOnDemoMode(() -> {
            if (demoRunner == null) demoRunner = new DemoRunner(appController);
            demoRunner.start();
            updateRoastLiveEnabled();
            switchToRoastLive();
        });
        root.setCenter(contentArea);

        updateConnectionStatus();
        updateRoastLiveEnabled();
    }

    public BorderPane getRoot() {
        return root;
    }

    /** Prepends a node to the top bar (e.g. menu overflow button). */
    public void addLeadingToTopBar(javafx.scene.Node node) {
        if (topBar != null) topBar.getChildren().add(0, node);
    }

    public void setOnSettings(Runnable onSettings) {
        this.onSettings = onSettings;
    }

    public void setOnResetLayout(Runnable onResetLayout) {
        this.onResetLayout = onResetLayout;
    }

    /** Syncs curve visibility from UI prefs to DisplaySettings when Pre-Roast quick settings change. */
    public void setOnCurveVisibilitySync(Runnable onCurveVisibilitySync) {
        this.onCurveVisibilitySync = onCurveVisibilitySync;
        if (preRoastScreen != null) preRoastScreen.setOnCurveVisibilitySync(onCurveVisibilitySync);
    }

    public void switchToPreRoast() {
        preRoastScreen.getRoot().setVisible(true);
        roastLiveScreen.getRoot().setVisible(false);
        preRoastNav.setSelected(true);
        roastLiveNav.setSelected(false);
        preRoastScreen.refresh();
    }

    public void switchToRoastLive() {
        preRoastScreen.getRoot().setVisible(false);
        roastLiveScreen.getRoot().setVisible(true);
        preRoastNav.setSelected(false);
        roastLiveNav.setSelected(true);
        roastLiveScreen.onScreenShown();
    }

    public void updateConnectionStatus() {
        CommController comm = appController != null ? appController.getCommController() : null;
        boolean running = comm != null && comm.isRunning();
        String desc = (comm != null && comm.getActiveChannel() != null)
            ? comm.getActiveChannel().getDescription() : "Disconnected";
        connectionLabel.setText(running ? desc : "Disconnected");
    }

    public void updateRoastLiveEnabled() {
        boolean active = appController != null && (appController.getSession().isActive()
            || (demoRunner != null && demoRunner.isRunning()));
        roastLiveNav.setDisable(!active);
        if (active && !roastLiveNav.isSelected()) {
            roastLiveNav.setTooltip(new Tooltip("Switch to live roast view"));
        }
    }

    public RoastLiveScreen getRoastLiveScreen() {
        return roastLiveScreen;
    }

    public DemoRunner getDemoRunner() {
        return demoRunner;
    }

    public void setDemoRunner(DemoRunner demoRunner) {
        this.demoRunner = demoRunner;
    }

    /** Updates the machine indicator (e.g. device type name). Call after device config changes. */
    public void setMachineName(String name) {
        String value = name != null && !name.isBlank() ? name : "—";
        if (machineLabel != null) machineLabel.setText(value);
        preRoastScreen.setMachineName(value);
    }

    public void setOnOpenReplay(Runnable onOpenReplay) {
        this.onOpenReplay = onOpenReplay;
        if (preRoastScreen != null) preRoastScreen.setOnOpenReplay(onOpenReplay);
    }

    public void setOnOpenRecent(Consumer<Path> onOpenRecent) {
        this.onOpenRecent = onOpenRecent;
        if (preRoastScreen != null) preRoastScreen.setOnOpenRecent(onOpenRecent);
    }

    public PreRoastScreen getPreRoastScreen() {
        return preRoastScreen;
    }

    /** Refreshes connection status and Roast Live button. Call after Ports/Devices dialogs close. */
    public void refreshTopBar() {
        updateConnectionStatus();
        updateRoastLiveEnabled();
        preRoastScreen.refresh();
    }
}
