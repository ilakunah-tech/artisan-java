package org.artisan.view;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.artisan.model.ProfileData;
import org.artisan.model.RoastComparator;
import org.artisan.model.Roastlog;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.scene.paint.Color;

/**
 * Tools » Comparator: non-modal window with list of loaded profiles and BT chart.
 * Per-profile time offset; preferences comparator.* for position/size.
 */
public final class ComparatorView {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "comparator.";
    private static final int MAX_PROFILES = 8;
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

    private final Stage stage;
    private final RoastComparator comparator = new RoastComparator();
    private final ObservableList<Double> timeOffsets = FXCollections.observableArrayList();
    private final ObservableList<String> fileNames = FXCollections.observableArrayList();
    private final ListView<String> listView;
    private final XYChart chart;
    private final List<DoubleDataSet> dataSets = new ArrayList<>();

    public ComparatorView(Window owner) {
        stage = new Stage();
        stage.setTitle("Roast Comparator");
        stage.initOwner(owner);

        listView = new ListView<>(fileNames);
        listView.setCellFactory(this::createCell);
        listView.setPrefWidth(280);
        listView.getSelectionModel().selectedIndexProperty().addListener((a, b, c) -> {});

        Button addBtn = new Button("Add Profile");
        addBtn.setOnAction(e -> addProfile());
        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(e -> removeSelected());
        Button clearBtn = new Button("Clear All");
        clearBtn.setOnAction(e -> clearAll());
        VBox leftButtons = new VBox(6, addBtn, removeBtn, clearBtn);

        VBox leftPanel = new VBox(8, listView, leftButtons);
        leftPanel.setPadding(new Insets(8));
        leftPanel.setMinWidth(200);

        DefaultNumericAxis xAxis = new DefaultNumericAxis("Time (s)");
        xAxis.setSide(Side.BOTTOM);
        DefaultNumericAxis yAxis = new DefaultNumericAxis("BT (°C)");
        yAxis.setSide(Side.LEFT);
        yAxis.setMin(0);
        yAxis.setMax(250);
        chart = new XYChart(xAxis, yAxis);
        chart.setLegendVisible(true);
        for (int i = 0; i < MAX_PROFILES; i++) {
            DoubleDataSet ds = new DoubleDataSet("P" + (i + 1));
            ds.setStyle("-fx-stroke: " + toHex(PALETTE[i % PALETTE.length]) + "; -fx-stroke-width: 2px;");
            dataSets.add(ds);
            chart.getDatasets().add(ds);
        }

        BorderPane chartPane = new BorderPane(chart);
        chartPane.setPadding(new Insets(8));

        ToolBar toolbar = new ToolBar();
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> stage.hide());
        toolbar.getItems().add(closeBtn);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setLeft(leftPanel);
        root.setCenter(chartPane);
        BorderPane.setMargin(chartPane, new Insets(0, 8, 8, 8));
        HBox.setHgrow(chartPane, Priority.ALWAYS);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 500);
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
        for (DoubleDataSet ds : dataSets) {
            ds.clearData();
        }
        List<ProfileData> profiles = comparator.getProfiles();
        for (int i = 0; i < profiles.size() && i < dataSets.size(); i++) {
            double offset = i < timeOffsets.size() ? timeOffsets.get(i) : 0;
            double[] time = comparator.getAlignedTime(i, offset);
            double[] bt = comparator.getAlignedBT(i, offset);
            if (time.length > 0 && bt.length == time.length) {
                dataSets.get(i).set(time, bt);
                dataSets.get(i).setName(comparator.getFilename(i));
            }
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
            if (Double.isFinite(x) && Double.isFinite(y)) stage.setX(x);
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
