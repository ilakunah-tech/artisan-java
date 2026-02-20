package org.artisan.view;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.artisan.model.ProfileData;
import org.artisan.model.RoastComparator;
import org.artisan.model.Roastlog;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.chartfx.ui.geometry.Side;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.scene.paint.Color;

/**
 * Tools - Comparator: non-modal window with list of loaded profiles, dual-axis chart
 * showing BT + ET on left axis and RoR on right axis, per-profile event markers
 * and phase shading. Per-profile time offset; preferences comparator.* for position/size.
 */
public final class ComparatorView {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "comparator.";
    private static final int MAX_PROFILES = 8;
    private static final int ROR_SMOOTH_WINDOW = 5;
    private static final Color[] PALETTE = {
        Color.web("#0a5c90"),
        Color.web("#cc0f50"),
        Color.web("#49b160"),
        Color.web("#b86dd4"),
        Color.web("#d03050"),
        Color.web("#e69800"),
        Color.web("#008080"),
        Color.web("#804080")
    };
    private static final String[] EVENT_NAMES = {"CHARGE", "DRY", "FCs", "FCe", "SCs", "SCe", "DROP", "COOL"};

    private final Stage stage;
    private final RoastComparator comparator = new RoastComparator();
    private final ObservableList<Double> timeOffsets = FXCollections.observableArrayList();
    private final ObservableList<String> fileNames = FXCollections.observableArrayList();
    private final ListView<String> listView;

    private final XYChart chart;
    private final DefaultNumericAxis xAxis;
    private final DefaultNumericAxis tempAxis;
    private final DefaultNumericAxis rorAxis;
    private final ErrorDataSetRenderer tempRenderer;
    private final ErrorDataSetRenderer rorRenderer;
    private final Pane overlayPane = new Pane();

    private final List<DoubleDataSet> btSets = new ArrayList<>();
    private final List<DoubleDataSet> etSets = new ArrayList<>();
    private final List<DoubleDataSet> rorSets = new ArrayList<>();

    private boolean showET = true;
    private boolean showRoR = true;
    private boolean showEvents = true;

    public ComparatorView(Window owner) {
        stage = new Stage();
        stage.setTitle("Roast Comparator");
        stage.initOwner(owner);

        listView = new ListView<>(fileNames);
        listView.setCellFactory(this::createCell);
        listView.setPrefWidth(280);

        Button addBtn = new Button("Add Profile");
        addBtn.setOnAction(e -> addProfile());
        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(e -> removeSelected());
        Button clearBtn = new Button("Clear All");
        clearBtn.setOnAction(e -> clearAll());

        CheckBox etCheck = new CheckBox("Show ET");
        etCheck.setSelected(showET);
        etCheck.selectedProperty().addListener((a, b, c) -> { showET = c; refreshChart(); });
        CheckBox rorCheck = new CheckBox("Show RoR");
        rorCheck.setSelected(showRoR);
        rorCheck.selectedProperty().addListener((a, b, c) -> { showRoR = c; refreshChart(); });
        CheckBox evtCheck = new CheckBox("Events");
        evtCheck.setSelected(showEvents);
        evtCheck.selectedProperty().addListener((a, b, c) -> { showEvents = c; refreshChart(); });

        VBox leftPanel = new VBox(8, listView, addBtn, removeBtn, clearBtn,
                new Separator(), etCheck, rorCheck, evtCheck);
        leftPanel.setPadding(new Insets(8));
        leftPanel.setMinWidth(200);

        xAxis = new DefaultNumericAxis("Time (s)");
        xAxis.setSide(Side.BOTTOM);
        tempAxis = new DefaultNumericAxis("Temp (°C)");
        tempAxis.setSide(Side.LEFT);
        tempAxis.setMin(0);
        tempAxis.setMax(250);

        rorAxis = new DefaultNumericAxis("RoR (°C/min)");
        rorAxis.setSide(Side.RIGHT);
        rorAxis.setMin(-20);
        rorAxis.setMax(50);

        chart = new XYChart(xAxis, tempAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(true);

        tempRenderer = new ErrorDataSetRenderer();
        tempRenderer.getAxes().add(tempAxis);
        rorRenderer = new ErrorDataSetRenderer();
        rorRenderer.getAxes().add(rorAxis);

        chart.getRenderers().clear();
        chart.getRenderers().addAll(tempRenderer, rorRenderer);

        for (int i = 0; i < MAX_PROFILES; i++) {
            Color c = PALETTE[i % PALETTE.length];
            String hex = toHex(c);

            DoubleDataSet btDs = new DoubleDataSet("BT P" + (i + 1));
            btDs.setStyle("-fx-stroke: " + hex + "; -fx-stroke-width: 2px;");
            btSets.add(btDs);
            tempRenderer.getDatasets().add(btDs);

            DoubleDataSet etDs = new DoubleDataSet("ET P" + (i + 1));
            Color lighter = c.deriveColor(0, 0.7, 1.3, 0.7);
            etDs.setStyle("-fx-stroke: " + toHex(lighter) + "; -fx-stroke-width: 1px; -fx-stroke-dash-array: 6 4;");
            etSets.add(etDs);
            tempRenderer.getDatasets().add(etDs);

            DoubleDataSet rorDs = new DoubleDataSet("ΔBT P" + (i + 1));
            rorDs.setStyle("-fx-stroke: " + hex + "; -fx-stroke-width: 1px; -fx-stroke-dash-array: 2 3;");
            rorSets.add(rorDs);
            rorRenderer.getDatasets().add(rorDs);
        }

        overlayPane.setMouseTransparent(true);
        StackPane chartStack = new StackPane(chart, overlayPane);

        BorderPane chartPane = new BorderPane(chartStack);
        chartPane.setPadding(new Insets(8));

        ToolBar toolbar = new ToolBar();
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> stage.hide());
        toolbar.getItems().add(closeBtn);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setLeft(leftPanel);
        root.setCenter(chartPane);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 560);
        stage.setScene(scene);

        loadPreferences();
        stage.setOnCloseRequest(e -> savePreferences());
    }

    private ListCell<String> createCell(ListView<String> view) {
        return new ListCell<>() {
            private final Spinner<Double> offsetSpinner = new Spinner<>(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(-600, 600, 0, 1));
            private final HBox box = new HBox(8);
            {
                offsetSpinner.setEditable(true);
                offsetSpinner.setPrefWidth(80);
                offsetSpinner.valueProperty().addListener((a, b, c) -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < timeOffsets.size()) {
                        timeOffsets.set(idx, offsetSpinner.getValue());
                        refreshChart();
                    }
                });
                box.getChildren().add(offsetSpinner);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    int idx = getIndex();
                    if (idx >= 0 && idx < timeOffsets.size()) {
                        offsetSpinner.getValueFactory().setValue(timeOffsets.get(idx));
                    }
                    setGraphic(box);
                }
            }
        };
    }

    private void addProfile() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Artisan log (*.alog)", "*.alog"));
        java.io.File file = chooser.showOpenDialog(stage);
        if (file == null) return;
        Path path = file.toPath();
        ProfileData pd = Roastlog.load(path);
        if (pd == null || pd.getTimex() == null || pd.getTimex().isEmpty()) return;
        if (comparator.getProfiles().size() >= MAX_PROFILES) return;
        String filename = path.getFileName() != null ? path.getFileName().toString() : path.toString();
        comparator.addProfile(pd, filename);
        timeOffsets.add(0.0);
        fileNames.add(filename);
        refreshChart();
    }

    private void removeSelected() {
        int idx = listView.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;
        comparator.removeProfile(idx);
        timeOffsets.remove(idx);
        fileNames.remove(idx);
        refreshChart();
    }

    private void clearAll() {
        comparator.clear();
        timeOffsets.clear();
        fileNames.clear();
        refreshChart();
    }

    private void refreshChart() {
        for (int i = 0; i < MAX_PROFILES; i++) {
            btSets.get(i).clearData();
            etSets.get(i).clearData();
            rorSets.get(i).clearData();
        }
        overlayPane.getChildren().clear();

        List<ProfileData> profiles = comparator.getProfiles();
        for (int i = 0; i < profiles.size() && i < MAX_PROFILES; i++) {
            double offset = i < timeOffsets.size() ? timeOffsets.get(i) : 0;
            double[] time = comparator.getAlignedTime(i, offset);
            double[] bt = comparator.getAlignedBT(i, offset);
            double[] et = comparator.getAlignedET(i, offset);
            double[] ror = comparator.getAlignedRoRBT(i, offset, ROR_SMOOTH_WINDOW);

            String name = comparator.getFilename(i);
            if (time.length > 0 && bt.length == time.length) {
                btSets.get(i).set(time, bt);
                btSets.get(i).setName("BT " + name);
            }
            if (showET && time.length > 0 && et.length == time.length) {
                etSets.get(i).set(time, et);
                etSets.get(i).setName("ET " + name);
            }
            if (showRoR && time.length > 0 && ror.length == time.length) {
                rorSets.get(i).set(time, ror);
                rorSets.get(i).setName("ΔBT " + name);
            }

            if (showEvents) {
                drawEventMarkers(i, offset);
            }
        }
    }

    private void drawEventMarkers(int profileIndex, double offset) {
        List<Integer> ti = comparator.getEventTimeindex(profileIndex);
        if (ti == null || ti.isEmpty()) return;
        ProfileData pd = comparator.getProfiles().get(profileIndex);
        List<Double> timex = pd.getTimex();
        if (timex == null || timex.isEmpty()) return;

        Color color = PALETTE[profileIndex % PALETTE.length];
        Color markerColor = Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.5);

        double w = overlayPane.getWidth();
        double h = overlayPane.getHeight();
        if (w <= 0 || h <= 0) return;
        double xMin = xAxis.getMin(), xMax = xAxis.getMax();
        double xRange = xMax - xMin;
        if (xRange <= 0) return;

        for (int slot = 0; slot < Math.min(ti.size(), EVENT_NAMES.length); slot++) {
            Integer idx = ti.get(slot);
            if (idx == null || idx <= 0 || idx >= timex.size()) continue;
            double tSec = timex.get(idx) + offset;
            double xPx = (tSec - xMin) / xRange * w;
            if (xPx < 0 || xPx > w) continue;
            Line line = new Line(xPx, 0, xPx, h);
            line.setStroke(markerColor);
            line.getStrokeDashArray().addAll(4.0, 6.0);
            line.setMouseTransparent(true);
            overlayPane.getChildren().add(line);

            Text label = new Text(EVENT_NAMES[slot]);
            label.setFill(markerColor);
            label.setStyle("-fx-font-size: 8px;");
            label.setX(xPx + 2);
            label.setY(12 + profileIndex * 10);
            label.setMouseTransparent(true);
            overlayPane.getChildren().add(label);
        }
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

    public Stage getStage() {
        return stage;
    }

    public RoastComparator getComparator() {
        return comparator;
    }

    private static String toHex(Color c) {
        if (c == null) return "#000000";
        return String.format("#%02x%02x%02x",
            (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }

    private void loadPreferences() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            double x = p.getDouble(PREFIX + "x", Double.NaN);
            double y = p.getDouble(PREFIX + "y", Double.NaN);
            double w = p.getDouble(PREFIX + "width", Double.NaN);
            double h = p.getDouble(PREFIX + "height", Double.NaN);
            if (Double.isFinite(x)) stage.setX(x);
            if (Double.isFinite(y)) stage.setY(y);
            if (Double.isFinite(w) && w > 100) stage.setWidth(w);
            if (Double.isFinite(h) && h > 100) stage.setHeight(h);
        } catch (Exception ignored) {}
    }

    private void savePreferences() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            p.putDouble(PREFIX + "x", stage.getX());
            p.putDouble(PREFIX + "y", stage.getY());
            p.putDouble(PREFIX + "width", stage.getWidth());
            p.putDouble(PREFIX + "height", stage.getHeight());
        } catch (Exception ignored) {}
    }
}
