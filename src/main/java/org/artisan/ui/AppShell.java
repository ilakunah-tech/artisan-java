package org.artisan.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.file.Path;
import java.util.function.Consumer;

import org.artisan.controller.AppController;
import org.artisan.controller.CommController;
import org.artisan.model.ReferenceProfile;
import org.artisan.ui.components.CustomTitleBar;
import org.artisan.ui.components.LeftDrawer;
import org.artisan.ui.components.LeftIconRail;
import org.artisan.ui.components.LoginOverlay;
import org.artisan.ui.components.RightReadoutPanel;
import org.artisan.ui.components.SettingsOverlay;
import org.artisan.ui.components.WebOverlay;
import org.artisan.ui.screens.PreRoastScreen;
import org.artisan.ui.screens.RoastLiveScreen;
import org.artisan.ui.state.PreferencesStore;
import org.artisan.ui.state.UIPreferences;

/**
 * Main application shell: custom title bar + main content.
 */
public final class AppShell {

    private final StackPane shellRoot;
    private final VBox rootVBox;
    private final CustomTitleBar titleBar;
    private final BorderPane mainLayout;
    private LeftIconRail leftIconRail;
    private final RightReadoutPanel rightReadoutPanel;
    private final LeftDrawer leftDrawer;
    private final LoginOverlay loginOverlay;
    private final SettingsOverlay settingsOverlay;
    private final WebOverlay webOverlay;
    private final RoastLiveScreen roastLiveScreen;
    private final AppController appController;
    private final UIPreferences uiPreferences;
    private final PreferencesStore preferencesStore;
    private final Pane drawerOverlay;

    private Runnable onSettings;
    private Runnable onResetLayout;
    private Runnable onCurveVisibilitySync;
    private Runnable onOpenReplay;
    private Consumer<Path> onOpenRecent;
    private DemoRunner demoRunner;

    // Kept alive for tests/compatibility; not displayed in new layout
    private final PreRoastScreen preRoastScreen;

    public AppShell(Stage primaryStage, AppController appController,
                    org.artisan.view.RoastChartController chartController,
                    org.artisan.controller.DisplaySettings displaySettings,
                    UIPreferences uiPreferences, PreferencesStore preferencesStore) {
        this.appController = appController;
        this.uiPreferences = uiPreferences != null ? uiPreferences : new UIPreferences();
        this.preferencesStore = preferencesStore != null ? preferencesStore : new PreferencesStore();

        // STEP 1 — RoastLiveScreen
        roastLiveScreen = new RoastLiveScreen(primaryStage, appController, chartController,
            displaySettings, this.uiPreferences, this.preferencesStore);

        // STEP 2 — rightReadoutPanel
        rightReadoutPanel = new RightReadoutPanel(roastLiveScreen.getViewModel());
        rightReadoutPanel.setAppController(appController);

        // STEP 3 — titleBar
        titleBar = new CustomTitleBar(primaryStage);
        roastLiveScreen.getViewModel().connectionStatusProperty()
            .addListener((obs, old, val) -> Platform.runLater(() ->
                titleBar.setConnected(val != null && !val.equals("Disconnected"))));

        // STEP 4 — overlays
        leftDrawer = new LeftDrawer();
        loginOverlay = new LoginOverlay();
        settingsOverlay = new SettingsOverlay();
        webOverlay = new WebOverlay();

        drawerOverlay = new Pane();
        drawerOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.08);");
        drawerOverlay.setVisible(false);
        drawerOverlay.setManaged(false);
        drawerOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        drawerOverlay.setOnMouseClicked(e -> {
            leftDrawer.close();
            leftIconRail.setFlameActive(false);
            setDrawerOverlayVisible(false);
        });

        // STEP 5 — leftIconRail
        leftIconRail = new LeftIconRail(
            () -> {
                webOverlay.toggle();
                leftIconRail.setCloudActive(webOverlay.isVisible());
            },
            () -> {
                if (leftDrawer.isOpen()) {
                    leftDrawer.close();
                    setDrawerOverlayVisible(false);
                } else {
                    leftDrawer.open();
                    setDrawerOverlayVisible(true);
                }
                leftIconRail.setFlameActive(leftDrawer.isOpen());
            },
            () -> {
                settingsOverlay.toggle();
                leftIconRail.setCupActive(settingsOverlay.isVisible());
            }
        );
        webOverlay.visibleProperty().addListener((obs, oldVal, newVal) ->
            leftIconRail.setCloudActive(newVal));
        settingsOverlay.visibleProperty().addListener((obs, oldVal, newVal) ->
            leftIconRail.setCupActive(newVal));

        // STEP 6 — mainLayout
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #EBEBEB;");
        mainLayout.setLeft(leftIconRail);
        mainLayout.setCenter(roastLiveScreen.getRoot());
        mainLayout.setRight(rightReadoutPanel);

        // STEP 7 — rootVBox
        rootVBox = new VBox(0, titleBar, mainLayout);
        rootVBox.setStyle("-fx-background-color: #EBEBEB;");
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        // STEP 8 — leftDrawer wiring
        leftDrawer.setOnReferenceSelected(name -> {
            ReferenceProfile rp = ReferenceProfile.createTestProfile();
            rightReadoutPanel.getRefBox().setReferenceProfile(rp);
            rightReadoutPanel.getModulationTimeline().setActions(rp.getModulationActions());
            roastLiveScreen.setPhaseStripProfile(rp);
        });

        // STEP 9 — shellRoot
        shellRoot = new StackPane();
        shellRoot.setStyle("-fx-background-color: #EBEBEB;");
        StackPane.setAlignment(leftDrawer, Pos.TOP_LEFT);
        StackPane.setMargin(leftDrawer, new Insets(28, 0, 0, 30));
        StackPane.setAlignment(loginOverlay, Pos.TOP_LEFT);
        StackPane.setAlignment(settingsOverlay, Pos.CENTER);
        titleBar.setOnConnect(() -> loginOverlay.toggle());

        shellRoot.getChildren().addAll(
            rootVBox, drawerOverlay, leftDrawer, loginOverlay, settingsOverlay, webOverlay);
        drawerOverlay.prefWidthProperty().bind(shellRoot.widthProperty());
        drawerOverlay.prefHeightProperty().bind(shellRoot.heightProperty());

        // STEP 10 — Start button wiring
        roastLiveScreen.setOnStart(() -> {
            if (appController != null) appController.startSampling();
            roastLiveScreen.onScreenShown();
            leftDrawer.close();
            leftIconRail.setFlameActive(false);
            setDrawerOverlayVisible(false);
        });

        // STEP 11 — ModulationTimeline ref
        roastLiveScreen.setModulationTimelineRef(rightReadoutPanel.getModulationTimeline());

        // STEP 12 — Open drawer on first launch
        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.millis(400));
            delay.setOnFinished(e -> {
                leftDrawer.open();
                leftIconRail.setFlameActive(true);
                setDrawerOverlayVisible(true);
            });
            delay.play();
        });

        // Kept for API compatibility (not shown)
        preRoastScreen = new PreRoastScreen(appController, this.uiPreferences, this.preferencesStore);
    }

    private void setDrawerOverlayVisible(boolean visible) {
        drawerOverlay.setVisible(visible);
        drawerOverlay.setManaged(visible);
        if (visible) {
            drawerOverlay.toFront();
            leftDrawer.toFront();
        }
    }

    public StackPane getRoot() { return shellRoot; }

    public RoastLiveScreen getRoastLiveScreen() { return roastLiveScreen; }

    public DemoRunner getDemoRunner() { return demoRunner; }

    public void setDemoRunner(DemoRunner demoRunner) {
        this.demoRunner = demoRunner;
    }

    public void setOnSettings(Runnable onSettings) {
        this.onSettings = onSettings;
    }

    public void setOnResetLayout(Runnable onResetLayout) {
        this.onResetLayout = onResetLayout;
    }

    public void setOnCurveVisibilitySync(Runnable onCurveVisibilitySync) {
        this.onCurveVisibilitySync = onCurveVisibilitySync;
        if (preRoastScreen != null) preRoastScreen.setOnCurveVisibilitySync(onCurveVisibilitySync);
    }

    public void setOnOpenReplay(Runnable onOpenReplay) {
        this.onOpenReplay = onOpenReplay;
        if (preRoastScreen != null) preRoastScreen.setOnOpenReplay(onOpenReplay);
    }

    public void setOnOpenRecent(Consumer<Path> onOpenRecent) {
        this.onOpenRecent = onOpenRecent;
        if (preRoastScreen != null) preRoastScreen.setOnOpenRecent(onOpenRecent);
    }

    public void setMachineName(String name) {
        if (preRoastScreen != null)
            preRoastScreen.setMachineName(name != null && !name.isBlank() ? name : "\u2014");
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

    /** Kept for API compatibility — no top nav bar. */
    public void switchToPreRoast() {
        leftDrawer.open();
        leftIconRail.setFlameActive(true);
        setDrawerOverlayVisible(true);
    }

    /** Kept for API compatibility — RoastLiveScreen is always visible. */
    public void switchToRoastLive() {
        leftDrawer.close();
        setDrawerOverlayVisible(false);
        leftIconRail.setFlameActive(false);
        roastLiveScreen.onScreenShown();
    }

    public void updateRoastLiveEnabled() { /* no-op */ }

    public void refreshTopBar() {
        updateConnectionStatus();
        if (preRoastScreen != null) preRoastScreen.refresh();
    }

    /** Kept for API compatibility (was addLeadingToTopBar). No-op since top bar removed. */
    public void addLeadingToTopBar(javafx.scene.Node node) { /* no-op */ }

    public PreRoastScreen getPreRoastScreen() { return preRoastScreen; }
}
