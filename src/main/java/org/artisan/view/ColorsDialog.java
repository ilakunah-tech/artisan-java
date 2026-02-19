package org.artisan.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import org.artisan.controller.DisplaySettings;
import org.artisan.model.ColorConfig;

/**
 * Colors and display settings dialog (Python Artisan Colors dialog).
 * Tabs: Curves (profile/background colors, line widths, smoothing, visibility),
 * Graph (palette keys), LCDs (foreground/background per LCD). Restore Defaults and Grey.
 */
public final class ColorsDialog extends ArtisanDialog {

    private static final String DELTA = "\u0394"; // Δ

    private final DisplaySettings displaySettings;
    private final ColorConfig colorConfig;
    private final Runnable onApply;

    // Curves tab
    private Button etColorBtn, btColorBtn, deltaEtColorBtn, deltaBtColorBtn;
    private Button bgEtColorBtn, bgBtColorBtn, bgDeltaEtColorBtn, bgDeltaBtColorBtn;
    private Spinner<Integer> backgroundAlphaSpinner;
    private Spinner<Integer> lineWidthETSpinner, lineWidthBTSpinner, lineWidthDeltaETSpinner, lineWidthDeltaBTSpinner;
    private Spinner<Integer> smoothingETSpinner, smoothingBTSpinner, smoothingDeltaSpinner;
    private CheckBox visibleETCheck, visibleBTCheck, visibleDeltaETCheck, visibleDeltaBTCheck;
    private Spinner<Double> timeGuideSpinner;

    public ColorsDialog(Window owner, DisplaySettings displaySettings, ColorConfig colorConfig, Runnable onApply) {
        super(owner, true, false);
        this.displaySettings = displaySettings;
        this.colorConfig = colorConfig;
        this.onApply = onApply != null ? onApply : () -> {};
        getStage().setTitle("Colors");
        getCancelButton().setVisible(false);
    }

    @Override
    protected Node buildContent() {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(buildCurvesTab());
        tabs.getTabs().add(buildGraphTab());
        tabs.getTabs().add(buildLcdsTab());

        Button restoreBtn = new Button("Restore Defaults");
        restoreBtn.setOnAction(e -> { displaySettings.restoreDefaults(); syncFromDisplaySettings(); applyAndRedraw(); });
        Button greyBtn = new Button("Grey");
        greyBtn.setOnAction(e -> { displaySettings.setGrey(); syncFromDisplaySettings(); applyAndRedraw(); });

        HBox bottomButtons = new HBox(10, restoreBtn, greyBtn);
        bottomButtons.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox(8, tabs, bottomButtons);
        root.setPadding(new Insets(10));
        return root;
    }

    private Tab buildCurvesTab() {
        etColorBtn = colorButton(displaySettings.getPaletteCurveET());
        btColorBtn = colorButton(displaySettings.getPaletteCurveBT());
        deltaEtColorBtn = colorButton(displaySettings.getPaletteCurveDeltaET());
        deltaBtColorBtn = colorButton(displaySettings.getPaletteCurveDeltaBT());
        bgEtColorBtn = colorButton(displaySettings.getPaletteBackgroundET());
        bgBtColorBtn = colorButton(displaySettings.getPaletteBackgroundBT());
        bgDeltaEtColorBtn = colorButton(displaySettings.getPaletteBackgroundDeltaET());
        bgDeltaBtColorBtn = colorButton(displaySettings.getPaletteBackgroundDeltaBT());

        backgroundAlphaSpinner = new Spinner<>(0, 10, (int) Math.round(displaySettings.getBackgroundAlpha() * 10));
        backgroundAlphaSpinner.setEditable(true);
        backgroundAlphaSpinner.valueProperty().addListener((a, b, v) -> {
            if (v != null) { displaySettings.setBackgroundAlpha(v / 10.0); applyAndRedraw(); }
        });

        lineWidthETSpinner = spinner(1, 10, displaySettings.getLineWidthET(), v -> { displaySettings.setLineWidthET(v); applyAndRedraw(); });
        lineWidthBTSpinner = spinner(1, 10, displaySettings.getLineWidthBT(), v -> { displaySettings.setLineWidthBT(v); applyAndRedraw(); });
        lineWidthDeltaETSpinner = spinner(1, 10, displaySettings.getLineWidthDeltaET(), v -> { displaySettings.setLineWidthDeltaET(v); applyAndRedraw(); });
        lineWidthDeltaBTSpinner = spinner(1, 10, displaySettings.getLineWidthDeltaBT(), v -> { displaySettings.setLineWidthDeltaBT(v); applyAndRedraw(); });

        smoothingETSpinner = spinner(1, 50, (displaySettings.getSmoothingET() + 1) / 2, v -> {
            displaySettings.setSmoothingET(2 * v - 1);
            applyAndRedraw();
        });
        smoothingBTSpinner = spinner(1, 50, (displaySettings.getSmoothingBT() + 1) / 2, v -> {
            displaySettings.setSmoothingBT(2 * v - 1);
            applyAndRedraw();
        });
        smoothingDeltaSpinner = spinner(1, 50, (displaySettings.getSmoothingDelta() + 1) / 2, v -> {
            displaySettings.setSmoothingDelta(2 * v - 1);
            applyAndRedraw();
        });

        visibleETCheck = new CheckBox("Show ET");
        visibleETCheck.setSelected(displaySettings.isVisibleET());
        visibleETCheck.setOnAction(e -> { displaySettings.setVisibleET(visibleETCheck.isSelected()); applyAndRedraw(); });
        visibleBTCheck = new CheckBox("Show BT");
        visibleBTCheck.setSelected(displaySettings.isVisibleBT());
        visibleBTCheck.setOnAction(e -> { displaySettings.setVisibleBT(visibleBTCheck.isSelected()); applyAndRedraw(); });
        visibleDeltaETCheck = new CheckBox("Show " + DELTA + "ET");
        visibleDeltaETCheck.setSelected(displaySettings.isVisibleDeltaET());
        visibleDeltaETCheck.setOnAction(e -> { displaySettings.setVisibleDeltaET(visibleDeltaETCheck.isSelected()); applyAndRedraw(); });
        visibleDeltaBTCheck = new CheckBox("Show " + DELTA + "BT");
        visibleDeltaBTCheck.setSelected(displaySettings.isVisibleDeltaBT());
        visibleDeltaBTCheck.setOnAction(e -> { displaySettings.setVisibleDeltaBT(visibleDeltaBTCheck.isSelected()); applyAndRedraw(); });

        wireCurveColorButton(etColorBtn, "et", displaySettings::getPaletteCurveET, displaySettings::setPaletteCurveET);
        wireCurveColorButton(btColorBtn, "bt", displaySettings::getPaletteCurveBT, displaySettings::setPaletteCurveBT);
        wireCurveColorButton(deltaEtColorBtn, "deltaet", displaySettings::getPaletteCurveDeltaET, displaySettings::setPaletteCurveDeltaET);
        wireCurveColorButton(deltaBtColorBtn, "deltabt", displaySettings::getPaletteCurveDeltaBT, displaySettings::setPaletteCurveDeltaBT);
        wireCurveColorButton(bgEtColorBtn, "backgroundmetcolor", displaySettings::getPaletteBackgroundET, displaySettings::setPaletteBackgroundET);
        wireCurveColorButton(bgBtColorBtn, "backgroundbtcolor", displaySettings::getPaletteBackgroundBT, displaySettings::setPaletteBackgroundBT);
        wireCurveColorButton(bgDeltaEtColorBtn, "backgrounddeltaetcolor", displaySettings::getPaletteBackgroundDeltaET, displaySettings::setPaletteBackgroundDeltaET);
        wireCurveColorButton(bgDeltaBtColorBtn, "backgrounddeltabtcolor", displaySettings::getPaletteBackgroundDeltaBT, displaySettings::setPaletteBackgroundDeltaBT);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        int row = 0;
        grid.add(new Label("Profile colors"), 0, row++);
        grid.add(new Label("ET"), 0, row); grid.add(etColorBtn, 1, row++);
        grid.add(new Label("BT"), 0, row); grid.add(btColorBtn, 1, row++);
        grid.add(new Label(DELTA + "ET"), 0, row); grid.add(deltaEtColorBtn, 1, row++);
        grid.add(new Label(DELTA + "BT"), 0, row); grid.add(deltaBtColorBtn, 1, row++);
        row++;
        grid.add(new Label("Background profile"), 0, row++);
        grid.add(new Label("ET"), 0, row); grid.add(bgEtColorBtn, 1, row++);
        grid.add(new Label("BT"), 0, row); grid.add(bgBtColorBtn, 1, row++);
        grid.add(new Label(DELTA + "ET"), 0, row); grid.add(bgDeltaEtColorBtn, 1, row++);
        grid.add(new Label(DELTA + "BT"), 0, row); grid.add(bgDeltaBtColorBtn, 1, row++);
        grid.add(new Label("Background opacity (0–10)"), 0, row); grid.add(backgroundAlphaSpinner, 1, row++);
        row++;
        grid.add(new Label("Line width (px)"), 0, row++);
        grid.add(new Label("ET"), 0, row); grid.add(lineWidthETSpinner, 1, row++);
        grid.add(new Label("BT"), 0, row); grid.add(lineWidthBTSpinner, 1, row++);
        grid.add(new Label(DELTA + "ET"), 0, row); grid.add(lineWidthDeltaETSpinner, 1, row++);
        grid.add(new Label(DELTA + "BT"), 0, row); grid.add(lineWidthDeltaBTSpinner, 1, row++);
        row++;
        grid.add(new Label("Smoothing (window)"), 0, row++);
        grid.add(new Label("ET"), 0, row); grid.add(smoothingETSpinner, 1, row++);
        grid.add(new Label("BT"), 0, row); grid.add(smoothingBTSpinner, 1, row++);
        grid.add(new Label("Delta"), 0, row); grid.add(smoothingDeltaSpinner, 1, row++);
        row++;
        grid.add(new Label("Visible"), 0, row++);
        grid.add(visibleETCheck, 0, row++);
        grid.add(visibleBTCheck, 0, row++);
        grid.add(visibleDeltaETCheck, 0, row++);
        grid.add(visibleDeltaBTCheck, 0, row++);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        return new Tab("Curves", scroll);
    }

    private Tab buildGraphTab() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(4);
        String[] graphKeys = {
            "background", "canvas", "grid", "title", "ylabel", "xlabel", "text", "markers", "watermarks",
            "timeguide", "aucguide", "aucarea", "legendbg", "legendborder",
            "rect1", "rect2", "rect3", "rect4", "rect5",
            "specialeventbox", "specialeventtext", "bgeventmarker", "bgeventtext", "metbox", "mettext",
            "analysismask", "statsanalysisbkgnd"
        };
        String[] labels = {
            "Background", "Canvas", "Grid", "Title", "Y Label", "X Label", "Text", "Markers", "Watermarks",
            "Time Guide", "AUC Guide", "AUC Area", "Legend bg", "Legend border",
            "Drying Phase", "Maillard Phase", "Finishing Phase", "Cooling Phase", "Bars Bkgnd",
            "Special Event Box", "Special Event Text", "Bkgd Event Marker", "Bkgd Event Text", "MET Box", "MET Text",
            "Analysis Mask", "Stats/Analysis Bkgnd"
        };
        int row = 0;
        for (int i = 0; i < graphKeys.length; i++) {
            String key = graphKeys[i];
            String label = labels[i];
            Button btn = colorButton(displaySettings.getPalette(key));
            wireGraphColorButton(btn, key);
            grid.add(new Label(label), 0, row);
            Node right = btn;
            if (key.equals("legendbg") || key.equals("analysismask") || key.equals("statsanalysisbkgnd")) {
                Spinner<Integer> alphaSpinner = new Spinner<>(1, 10, (int) Math.round(getAlphaForKey(key) * 10));
                alphaSpinner.setEditable(true);
                alphaSpinner.valueProperty().addListener((a, b, v) -> {
                    if (v != null) setAlphaForKey(key, v / 10.0);
                });
                HBox h = new HBox(8, btn, alphaSpinner);
                right = h;
            }
            grid.add(right, 1, row++);
        }
        row++;
        Label timeGuideLabel = new Label("Time Guide (s)");
        timeGuideSpinner = new Spinner<>();
        timeGuideSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 3600.0, displaySettings.getTimeguideSec(), 30.0));
        timeGuideSpinner.setEditable(true);
        timeGuideSpinner.valueProperty().addListener((a, b, v) -> {
            if (v != null) {
                displaySettings.setTimeguideSec(v);
                applyAndRedraw();
            }
        });
        grid.add(timeGuideLabel, 0, row);
        grid.add(timeGuideSpinner, 1, row++);
        row++;
        Label aucBaseLabel = new Label("AUC base temp (°C)");
        Spinner<Double> aucBaseSpinner = new Spinner<>();
        aucBaseSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 300.0, displaySettings.getAucBaseTemp(), 1.0));
        aucBaseSpinner.setEditable(true);
        aucBaseSpinner.valueProperty().addListener((a, b, v) -> {
            if (v != null) {
                displaySettings.setAucBaseTemp(v);
                applyAndRedraw();
            }
        });
        grid.add(aucBaseLabel, 0, row);
        grid.add(aucBaseSpinner, 1, row++);
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        return new Tab("Graph", scroll);
    }

    private double getAlphaForKey(String key) {
        if ("legendbg".equals(key)) return displaySettings.getAlphaLegendBg();
        if ("analysismask".equals(key)) return displaySettings.getAlphaAnalysismask();
        if ("statsanalysisbkgnd".equals(key)) return displaySettings.getAlphaStatsanalysisbkgnd();
        return 0.8;
    }
    private void setAlphaForKey(String key, double v) {
        if ("legendbg".equals(key)) displaySettings.setAlphaLegendBg(v);
        else if ("analysismask".equals(key)) displaySettings.setAlphaAnalysismask(v);
        else if ("statsanalysisbkgnd".equals(key)) displaySettings.setAlphaStatsanalysisbkgnd(v);
        applyAndRedraw();
    }

    private Tab buildLcdsTab() {
        String[] lcdKeys = DisplaySettings.getLcdKeys();
        String[] labels = { "Timer", "ET", "BT", DELTA + "ET", DELTA + "BT", "PID SV", "Ramp/Soak Timer", "Slow Cooling Timer" };
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        for (int i = 0; i < lcdKeys.length; i++) {
            String key = lcdKeys[i];
            Button fg = colorButton(displaySettings.getLcdForeground(key));
            Button bg = colorButton(displaySettings.getLcdBackground(key));
            wireLcdButton(fg, key, true);
            wireLcdButton(bg, key, false);
            grid.add(new Label(labels[i]), 0, i);
            grid.add(new Label("Digits"), 1, i);
            grid.add(fg, 2, i);
            grid.add(new Label("Background"), 3, i);
            grid.add(bg, 4, i);
        }
        Button bwBtn = new Button("B/W");
        bwBtn.setOnAction(e -> {
            for (String key : lcdKeys) {
                displaySettings.setLcdForeground(key, "#000000");
                displaySettings.setLcdBackground(key, "#ffffff");
            }
            syncFromDisplaySettings();
            applyAndRedraw();
        });
        VBox root = new VBox(10, new ScrollPane(grid), bwBtn);
        return new Tab("LCDs", root);
    }

    private Button colorButton(String hex) {
        Button b = new Button();
        b.setMinWidth(80);
        b.setStyle("-fx-background-color: " + (hex != null && !hex.isEmpty() ? hex : "#000000") + "; -fx-border-color: #666;");
        return b;
    }

    private void wireCurveColorButton(Button btn, String paletteKey,
            java.util.function.Supplier<String> getter, java.util.function.Consumer<String> setter) {
        btn.setOnAction(e -> {
            Color c = pickColor(btn, ColorConfig.fromHex(getter.get()));
            if (c == null) return;
            String hex = toHex(c);
            setter.accept(hex);
            colorConfig.setPaletteColor(paletteKey, c);
            btn.setStyle("-fx-background-color: " + hex + "; -fx-border-color: #666;");
            applyAndRedraw();
        });
    }

    private void wireGraphColorButton(Button btn, String key) {
        btn.setOnAction(e -> {
            Color c = pickColor(btn, ColorConfig.fromHex(displaySettings.getPalette(key)));
            if (c == null) return;
            String hex = toHex(c);
            displaySettings.setPalette(key, hex);
            colorConfig.setPaletteColor(key, c);
            btn.setStyle("-fx-background-color: " + hex + "; -fx-border-color: #666;");
            applyAndRedraw();
        });
    }

    private void wireLcdButton(Button btn, String lcdKey, boolean foreground) {
        btn.setOnAction(e -> {
            String hex = foreground ? displaySettings.getLcdForeground(lcdKey) : displaySettings.getLcdBackground(lcdKey);
            Color c = pickColor(btn, ColorConfig.fromHex(hex));
            if (c == null) return;
            String newHex = toHex(c);
            if (foreground) displaySettings.setLcdForeground(lcdKey, newHex);
            else displaySettings.setLcdBackground(lcdKey, newHex);
            btn.setStyle("-fx-background-color: " + newHex + "; -fx-border-color: #666;");
            applyAndRedraw();
        });
    }

    private static Color pickColor(Button owner, Color initial) {
        ColorPicker picker = new ColorPicker(initial != null ? initial : Color.BLACK);
        Button ok = new Button("OK");
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initOwner(owner.getScene() != null ? owner.getScene().getWindow() : null);
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        final Color[] result = { null };
        ok.setOnAction(e2 -> {
            result[0] = picker.getValue();
            popup.close();
        });
        VBox v = new VBox(10, picker, ok);
        v.setPadding(new Insets(10));
        popup.setScene(new javafx.scene.Scene(v));
        popup.showAndWait();
        return result[0];
    }

    private static String toHex(Color c) {
        if (c == null) return "#000000";
        return String.format("#%02x%02x%02x",
            (int) Math.round(c.getRed() * 255),
            (int) Math.round(c.getGreen() * 255),
            (int) Math.round(c.getBlue() * 255));
    }

    private static Spinner<Integer> spinner(int min, int max, int initial, java.util.function.Consumer<Integer> onChange) {
        Spinner<Integer> s = new Spinner<>(min, max, initial);
        s.setEditable(true);
        s.valueProperty().addListener((a, b, v) -> { if (v != null) onChange.accept(v); });
        return s;
    }

    private void syncFromDisplaySettings() {
        colorConfig.setPaletteColor("et", ColorConfig.fromHex(displaySettings.getPaletteCurveET()));
        colorConfig.setPaletteColor("bt", ColorConfig.fromHex(displaySettings.getPaletteCurveBT()));
        colorConfig.setPaletteColor("deltaet", ColorConfig.fromHex(displaySettings.getPaletteCurveDeltaET()));
        colorConfig.setPaletteColor("deltabt", ColorConfig.fromHex(displaySettings.getPaletteCurveDeltaBT()));
        for (String k : new String[] { "background", "canvas", "grid", "title", "ylabel", "xlabel", "text", "markers", "watermarks",
                "timeguide", "aucguide", "aucarea", "legendbg", "legendborder", "rect1", "rect2", "rect3", "rect4", "rect5",
                "specialeventbox", "specialeventtext", "bgeventmarker", "bgeventtext", "metbox", "mettext", "analysismask", "statsanalysisbkgnd" }) {
            colorConfig.setPaletteColor(k, ColorConfig.fromHex(displaySettings.getPalette(k)));
        }
        colorConfig.setPaletteAlpha("legendbg", displaySettings.getAlphaLegendBg());
        colorConfig.setPaletteAlpha("analysismask", displaySettings.getAlphaAnalysismask());
        colorConfig.setPaletteAlpha("statsanalysisbkgnd", displaySettings.getAlphaStatsanalysisbkgnd());
        if (etColorBtn != null) etColorBtn.setStyle("-fx-background-color: " + displaySettings.getPaletteCurveET() + "; -fx-border-color: #666;");
        if (btColorBtn != null) btColorBtn.setStyle("-fx-background-color: " + displaySettings.getPaletteCurveBT() + "; -fx-border-color: #666;");
        if (deltaEtColorBtn != null) deltaEtColorBtn.setStyle("-fx-background-color: " + displaySettings.getPaletteCurveDeltaET() + "; -fx-border-color: #666;");
        if (deltaBtColorBtn != null) deltaBtColorBtn.setStyle("-fx-background-color: " + displaySettings.getPaletteCurveDeltaBT() + "; -fx-border-color: #666;");
        if (bgEtColorBtn != null) bgEtColorBtn.setStyle("-fx-background-color: " + displaySettings.getPaletteBackgroundET() + "; -fx-border-color: #666;");
        if (bgBtColorBtn != null) bgBtColorBtn.setStyle("-fx-background-color: " + displaySettings.getPaletteBackgroundBT() + "; -fx-border-color: #666;");
        if (bgDeltaEtColorBtn != null) bgDeltaEtColorBtn.setStyle("-fx-background-color: " + displaySettings.getPaletteBackgroundDeltaET() + "; -fx-border-color: #666;");
        if (bgDeltaBtColorBtn != null) bgDeltaBtColorBtn.setStyle("-fx-background-color: " + displaySettings.getPaletteBackgroundDeltaBT() + "; -fx-border-color: #666;");
        if (backgroundAlphaSpinner != null) backgroundAlphaSpinner.getValueFactory().setValue((int) Math.round(displaySettings.getBackgroundAlpha() * 10));
        if (lineWidthETSpinner != null) lineWidthETSpinner.getValueFactory().setValue(displaySettings.getLineWidthET());
        if (lineWidthBTSpinner != null) lineWidthBTSpinner.getValueFactory().setValue(displaySettings.getLineWidthBT());
        if (lineWidthDeltaETSpinner != null) lineWidthDeltaETSpinner.getValueFactory().setValue(displaySettings.getLineWidthDeltaET());
        if (lineWidthDeltaBTSpinner != null) lineWidthDeltaBTSpinner.getValueFactory().setValue(displaySettings.getLineWidthDeltaBT());
        if (smoothingETSpinner != null) smoothingETSpinner.getValueFactory().setValue((displaySettings.getSmoothingET() + 1) / 2);
        if (smoothingBTSpinner != null) smoothingBTSpinner.getValueFactory().setValue((displaySettings.getSmoothingBT() + 1) / 2);
        if (smoothingDeltaSpinner != null) smoothingDeltaSpinner.getValueFactory().setValue((displaySettings.getSmoothingDelta() + 1) / 2);
        if (visibleETCheck != null) visibleETCheck.setSelected(displaySettings.isVisibleET());
        if (visibleBTCheck != null) visibleBTCheck.setSelected(displaySettings.isVisibleBT());
        if (visibleDeltaETCheck != null) visibleDeltaETCheck.setSelected(displaySettings.isVisibleDeltaET());
        if (visibleDeltaBTCheck != null) visibleDeltaBTCheck.setSelected(displaySettings.isVisibleDeltaBT());
        if (timeGuideSpinner != null) timeGuideSpinner.getValueFactory().setValue(displaySettings.getTimeguideSec());
    }

    private void applyAndRedraw() {
        onApply.run();
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        applyAndRedraw();
        super.onOk(e);
    }
}
