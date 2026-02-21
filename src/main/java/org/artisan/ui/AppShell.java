package org.artisan.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.function.Consumer;
import org.artisan.controller.AppController;
import org.artisan.controller.CommController;
import org.artisan.ui.components.LeftDrawer;
import org.artisan.ui.components.ShortcutHelpDialog;
import org.artisan.ui.screens.PreRoastScreen;
import org.artisan.ui.screens.RoastLiveScreen;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;

/**
 * Main window layout: StackPane with RoastLiveScreen always visible and LeftDrawer overlay.
 * Pre-Roast setup lives inside LeftDrawer; there is no top-level nav bar.
 */
public final class AppShell {

    private final StackPane root;
    private final RoastLiveScreen roastLiveScreen;
    private final LeftDrawer leftDrawer;
    private final PreRoastScreen preRoastScreen;
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

        roastLiveScreen = new RoastLiveScreen(primaryStage, appController, chartController,
            displaySettings, this.uiPreferences, this.preferencesStore);

        leftDrawer = new LeftDrawer();
        leftDrawer.setOnDemoMode(() -> {
            if (demoRunner == null) demoRunner = new DemoRunner(appController);
            if (roastLiveScreen != null) demoRunner.setViewModel(roastLiveScreen.getViewModel());
            demoRunner.start();
            roastLiveScreen.onScreenShown();
        });
        leftDrawer.setOnStartRoast(() -> {
            if (appController != null) appController.startSampling();
            roastLiveScreen.onScreenShown();
        });

        roastLiveScreen.setOnHamburger(leftDrawer::toggle);
        roastLiveScreen.setOnTopBarSettings(() -> { if (onSettings != null) onSettings.run(); });
        roastLiveScreen.setOnTopBarResetLayout(() -> { if (onResetLayout != null) onResetLayout.run(); });
        roastLiveScreen.setOnTopBarKeyboardShortcuts(() -> {
            org.artisan.ui.components.ShortcutHelpDialog.show(primaryStage);
        });

        root = new StackPane();
        root.getStyleClass().add("ri5-root");
        root.setAlignment(Pos.TOP_LEFT);

        StackPane.setAlignment(roastLiveScreen.getRoot(), Pos.TOP_LEFT);
        StackPane.setAlignment(leftDrawer, Pos.TOP_LEFT);

        root.getChildren().addAll(roastLiveScreen.getRoot(), leftDrawer);

        // PreRoastScreen kept alive for tests; not shown in layout
        preRoastScreen = new PreRoastScreen(appController, this.uiPreferences, this.preferencesStore);

        // Open drawer on first launch
        if (!this.uiPreferences.isTourCompleted()) {
            javafx.application.Platform.runLater(() -> {
                javafx.animation.PauseTransition delay =
                    new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
                delay.setOnFinished(e -> leftDrawer.open());
                delay.play();
            });
        }
    }

    public StackPane getRoot() {
        return root;
    }

    public void setOnSettings(Runnable onSettings) {
        this.onSettings = onSettings;
        roastLiveScreen.setOnTopBarSettings(() -> { if (onSettings != null) onSettings.run(); });
    }

    public void setOnResetLayout(Runnable onResetLayout) {
        this.onResetLayout = onResetLayout;
        roastLiveScreen.setOnTopBarResetLayout(() -> { if (onResetLayout != null) onResetLayout.run(); });
    }

    public void setOnCurveVisibilitySync(Runnable onCurveVisibilitySync) {
        this.onCurveVisibilitySync = onCurveVisibilitySync;
        if (preRoastScreen != null) preRoastScreen.setOnCurveVisibilitySync(onCurveVisibilitySync);
    }

    /** No longer used (no nav bar), kept for API compatibility. */
    public void switchToPreRoast() {
        leftDrawer.open();
    }

    /** No longer needed; RoastLiveScreen is always visible. */
    public void switchToRoastLive() {
        leftDrawer.close();
        roastLiveScreen.onScreenShown();
    }

    public void updateConnectionStatus() {
        CommController comm = appController != null ? appController.getCommController() : null;
        String desc = (comm != null && comm.getActiveChannel() != null)
            ? comm.getActiveChannel().getDescription() : "Disconnected";
        if (roastLiveScreen != null) {
            roastLiveScreen.getViewModel().setConnectionStatus(
                (comm != null && comm.isRunning()) ? desc : "Disconnected");
        }
    }

    public void updateRoastLiveEnabled() {
        // No nav button to enable/disable; no-op.
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

    /** Updates the machine indicator. */
    public void setMachineName(String name) {
        // Machine name shown inside LeftDrawer or status bar; no top-bar label needed.
        if (preRoastScreen != null) preRoastScreen.setMachineName(name != null && !name.isBlank() ? name : "—");
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

    /** Refreshes connection status. Call after Ports/Devices dialogs close. */
    public void refreshTopBar() {
        updateConnectionStatus();
        if (preRoastScreen != null) preRoastScreen.refresh();
    }

    /** Kept for API compatibility (was addLeadingToTopBar). No-op since top bar removed. */
    public void addLeadingToTopBar(javafx.scene.Node node) {
        // no-op: top nav bar removed in P6
    }
}
