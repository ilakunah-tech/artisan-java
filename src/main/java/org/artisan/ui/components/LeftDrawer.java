package org.artisan.ui.components;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Slide-out left drawer overlay. Covers the full scene; contains nav links and
 * the Pre-Roast setup form. Toggle via {@link #toggle()}, open/close via
 * {@link #open()} and {@link #close()}.
 *
 * <p>Usage: add as layer [1] in a StackPane over RoastLiveScreen.
 */
public final class LeftDrawer extends StackPane {

    private static final double DRAWER_WIDTH = 380.0;
    private static final Duration SLIDE_DURATION = Duration.millis(250);

    private final VBox drawer;
    private final TranslateTransition openAnim;
    private final TranslateTransition closeAnim;
    private boolean isOpen = false;

    private Runnable onDemoMode;
    private Runnable onStartRoast;
    private Runnable onSimulator;

    public LeftDrawer() {
        setPickOnBounds(false);
        setMouseTransparent(true);

        Pane backdrop = new Pane();
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        backdrop.setVisible(false);
        backdrop.setMouseTransparent(false);
        backdrop.setOnMouseClicked(e -> close());

        drawer = new VBox(0);
        drawer.setPrefWidth(DRAWER_WIDTH);
        drawer.setMaxWidth(DRAWER_WIDTH);
        drawer.setMinWidth(DRAWER_WIDTH);
        drawer.setStyle("-fx-background-color: #FFFFFF;");
        drawer.setEffect(new DropShadow(20, 4, 0, Color.color(0, 0, 0, 0.4)));
        drawer.setTranslateX(-DRAWER_WIDTH);
        drawer.setMaxHeight(Double.MAX_VALUE);

        StackPane.setAlignment(drawer, Pos.CENTER_LEFT);
        StackPane.setAlignment(backdrop, Pos.CENTER);

        getChildren().addAll(backdrop, drawer);

        openAnim = new TranslateTransition(SLIDE_DURATION, drawer);
        openAnim.setToX(0);
        openAnim.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        closeAnim = new TranslateTransition(SLIDE_DURATION, drawer);
        closeAnim.setToX(-DRAWER_WIDTH);
        closeAnim.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        closeAnim.setOnFinished(e -> {
            setMouseTransparent(true);
            backdrop.setVisible(false);
        });

        drawer.getChildren().addAll(
            buildHeader(),
            new Separator(),
            buildNav(),
            new Separator(),
            buildPreRoastForm()
        );
    }

    private HBox buildHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 12, 10, 12));
        header.setStyle("-fx-background-color: #0e0e1a;");
        header.setMinHeight(48);

        Button closeBtn = new Button("\u2716");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8888a0; -fx-font-size: 16px; -fx-padding: 0 8 0 0;");
        closeBtn.setOnAction(e -> close());

        Label logo = new Label("AJ");
        logo.setStyle("-fx-font-weight: 700; -fx-font-size: 16px; -fx-text-fill: #e8e8f0; -fx-background-color: #5680E9; -fx-background-radius: 4; -fx-padding: 2 6 2 6;");

        Label title = new Label("Artisan Java");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #e8e8f0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(closeBtn, logo, title, spacer);
        return header;
    }

    private VBox buildNav() {
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(8, 0, 8, 0));

        nav.getChildren().addAll(
            navItem("\uD83D\uDDA5 Simulator", () -> { if (onSimulator != null) onSimulator.run(); }),
            navItem("\u2699 Pre-Roast Setup", null),
            navItem("\uD83D\uDCC8 Roast (Live)", this::close)
        );
        return nav;
    }

    private Button navItem(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-font-size: 13px; -fx-text-fill: #1F2937; -fx-padding: 10 16 10 16; -fx-background-radius: 0;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #F3F4F6; -fx-font-size: 13px; -fx-text-fill: #1F2937; -fx-padding: 10 16 10 16; -fx-background-radius: 0;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-font-size: 13px; -fx-text-fill: #1F2937; -fx-padding: 10 16 10 16; -fx-background-radius: 0;"));
        if (action != null) btn.setOnAction(e -> action.run());
        return btn;
    }

    private VBox buildPreRoastForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(16));
        VBox.setVgrow(form, Priority.ALWAYS);

        Label sectionTitle = new Label("Pre-Roast Setup");
        sectionTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #1F2937;");

        Label profileLbl = new Label("Roast Profile");
        profileLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        ComboBox<String> profileCombo = new ComboBox<>();
        profileCombo.getItems().addAll("(Default)", "Light", "Medium", "Dark", "City", "Full City", "Vienna", "French");
        profileCombo.getSelectionModel().select(0);
        profileCombo.setMaxWidth(Double.MAX_VALUE);

        Label batchLbl = new Label("Batch Size (g)");
        batchLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        TextField batchField = new TextField();
        batchField.setPromptText("e.g. 300");
        batchField.setMaxWidth(Double.MAX_VALUE);

        Label refLbl = new Label("Reference Curve");
        refLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        ComboBox<String> refCombo = new ComboBox<>();
        refCombo.getItems().addAll("(None)", "Last roast", "Reference 1", "Reference 2");
        refCombo.getSelectionModel().select(0);
        refCombo.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button demoBtn = new Button("Demo Mode");
        demoBtn.setMaxWidth(Double.MAX_VALUE);
        demoBtn.setStyle("-fx-background-color: #1e1e30; -fx-text-fill: #e8e8f0; -fx-border-color: #2a2a3e; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 16 8 16; -fx-font-size: 13px;");
        demoBtn.setOnAction(e -> {
            close();
            if (onDemoMode != null) onDemoMode.run();
        });

        Button startBtn = new Button("Start Roast");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 10 16 10 16; -fx-font-size: 14px;");
        startBtn.setOnAction(e -> {
            close();
            if (onStartRoast != null) onStartRoast.run();
        });

        form.getChildren().addAll(sectionTitle,
            profileLbl, profileCombo,
            batchLbl, batchField,
            refLbl, refCombo,
            spacer, demoBtn, startBtn);
        return form;
    }

    public void toggle() {
        if (isOpen) close(); else open();
    }

    public void open() {
        if (isOpen) return;
        isOpen = true;
        setMouseTransparent(false);
        Pane bd = (Pane) getChildren().get(0);
        bd.setVisible(true);
        closeAnim.stop();
        openAnim.playFromStart();
    }

    public void close() {
        if (!isOpen) return;
        isOpen = false;
        closeAnim.stop();
        closeAnim.playFromStart();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOnDemoMode(Runnable onDemoMode) {
        this.onDemoMode = onDemoMode;
    }

    public void setOnStartRoast(Runnable onStartRoast) {
        this.onStartRoast = onStartRoast;
    }

    public void setOnSimulator(Runnable onSimulator) {
        this.onSimulator = onSimulator;
    }
}
