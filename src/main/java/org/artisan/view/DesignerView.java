package org.artisan.view;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.ParameterMeasurements;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.chartfx.ui.geometry.Side;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.artisan.controller.AppController;
import org.artisan.model.DesignerPoint;
import org.artisan.model.ProfileData;
import org.artisan.model.ProfileDesigner;

import java.util.List;
import java.util.prefs.Preferences;

/**
 * Tools » Designer: interactive profile designer using control points + spline interpolation.
 */
public final class DesignerView {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "designer.";

    private final AppController appController;
    private final ProfileDesigner profileDesigner;
    private final Stage stage;

    private XYChart chart;
    private DoubleDataSet dataBT;
    private DoubleDataSet dataET;
    private Pane pointsLayer;
    private Label btCountLabel;
    private Label etCountLabel;
    private Spinner<Double> samplingSpinner;

    private SelectedPoint selected;

    private enum CurveType { BT, ET }

    private record SelectedPoint(CurveType type, int index, Circle circle) {}

    public DesignerView(Window owner, AppController appController) {
        this.appController = appController;
        this.profileDesigner = new ProfileDesigner();
        this.stage = new Stage();
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Profile Designer");
        stage.setScene(new Scene(buildRoot(), 900, 540));
        restoreWindowState();
        stage.setOnHiding(e -> saveWindowState());
    }

    public Stage getStage() {
        return stage;
    }

    public void show() {
        stage.show();
        stage.toFront();
        refresh();
    }

    private BorderPane buildRoot() {
        DefaultNumericAxis xAxis = new DefaultNumericAxis("Time (s)");
        xAxis.setSide(Side.BOTTOM);
        DefaultNumericAxis yAxis = new DefaultNumericAxis("Temperature (°C)");
        yAxis.setSide(Side.LEFT);
        yAxis.setAutoRanging(false);
        yAxis.setMin(100);
        yAxis.setMax(280);

        chart = new XYChart(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.getPlugins().addAll(new Zoomer(), new ParameterMeasurements());

        dataBT = new DoubleDataSet("BT");
        dataBT.setStyle("-fx-stroke: #FF6600; -fx-stroke-width: 2px;");
        dataET = new DoubleDataSet("ET");
        dataET.setStyle("-fx-stroke: #FF0000; -fx-stroke-width: 2px;");
        chart.getDatasets().addAll(dataBT, dataET);

        pointsLayer = new Pane();
        pointsLayer.setPickOnBounds(false);

        StackPane chartStack = new StackPane(chart, pointsLayer);
        VBox.setVgrow(chartStack, Priority.ALWAYS);

        chartStack.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        chartStack.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        chartStack.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        chartStack.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);

        VBox rightPanel = buildRightPanel();
        rightPanel.setPrefWidth(200);
        rightPanel.setMinWidth(200);
        rightPanel.setMaxWidth(200);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setCenter(chartStack);
        root.setRight(rightPanel);
        BorderPane.setMargin(rightPanel, new Insets(0, 0, 0, 10));
        return root;
    }

    private VBox buildRightPanel() {
        Label title = new Label("Designer");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        btCountLabel = new Label("BT Points: 0");
        etCountLabel = new Label("ET Points: 0");

        Button defaultsBtn = new Button("Add Default Profile");
        defaultsBtn.setMaxWidth(Double.MAX_VALUE);
        defaultsBtn.setOnAction(e -> {
            profileDesigner.loadDefaults();
            refresh();
        });

        Button clearBtn = new Button("Clear All Points");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            profileDesigner.clear();
            refresh();
        });

        Button loadBtn = new Button("Load as Profile");
        loadBtn.setMaxWidth(Double.MAX_VALUE);
        loadBtn.setOnAction(e -> {
            ProfileData pd = profileDesigner.generate();
            if (pd != null && appController != null) {
                appController.loadSimulatedProfile(pd);
                stage.close();
            }
        });

        Button closeBtn = new Button("Close");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setOnAction(e -> stage.close());

        samplingSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 5.0, 1.0, 0.5));
        samplingSpinner.setEditable(true);
        samplingSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                profileDesigner.setSamplingInterval(newV);
                refresh();
            }
        });

        VBox box = new VBox(10,
            title,
            new Separator(),
            btCountLabel,
            etCountLabel,
            defaultsBtn,
            clearBtn,
            new Separator(),
            loadBtn,
            closeBtn,
            new Separator(),
            new Label("Sampling interval:"),
            samplingSpinner
        );
        box.setPadding(new Insets(0));
        VBox.setVgrow(defaultsBtn, Priority.NEVER);
        return box;
    }

    private void refresh() {
        btCountLabel.setText("BT Points: " + profileDesigner.getBtPoints().size());
        etCountLabel.setText("ET Points: " + profileDesigner.getEtPoints().size());
        samplingSpinner.getValueFactory().setValue(profileDesigner.getSamplingInterval());

        ProfileData pd = profileDesigner.generate();
        if (pd != null) {
            setSeries(dataBT, pd.getTimex(), pd.getTemp2());
            setSeries(dataET, pd.getTimex(), pd.getTemp1());
            DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
            xAxis.setAutoRanging(false);
            xAxis.setMin(0);
            xAxis.setMax(pd.getTimex().isEmpty() ? 600 : pd.getTimex().get(pd.getTimex().size() - 1));
        } else {
            dataBT.clearData();
            dataET.clearData();
        }
        redrawControlPoints();
    }

    private static void setSeries(DoubleDataSet ds, List<Double> xList, List<Double> yList) {
        if (xList == null || yList == null) {
            ds.clearData();
            return;
        }
        int n = Math.min(xList.size(), yList.size());
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = xList.get(i) != null ? xList.get(i) : 0.0;
            y[i] = yList.get(i) != null ? yList.get(i) : 0.0;
        }
        ds.set(x, y);
    }

    private void redrawControlPoints() {
        pointsLayer.getChildren().clear();
        drawPoints(profileDesigner.getBtPoints(), CurveType.BT, Color.web("#FF6600"));
        drawPoints(profileDesigner.getEtPoints(), CurveType.ET, Color.web("#FF0000"));
    }

    private void drawPoints(List<DesignerPoint> pts, CurveType type, Color color) {
        if (pts == null) return;
        for (int i = 0; i < pts.size(); i++) {
            DesignerPoint p = pts.get(i);
            Circle c = new Circle(5);
            c.setFill(color);
            c.setStroke(Color.color(0, 0, 0, 0.35));
            c.setStrokeWidth(1);
            positionCircle(c, p.getTimeSec(), p.getTemp());
            int idx = i;
            c.setOnContextMenuRequested(e -> {
                ContextMenu menu = new ContextMenu();
                MenuItem del = new MenuItem("Delete point");
                del.setOnAction(ae -> {
                    if (type == CurveType.BT) profileDesigner.removeBtPoint(idx);
                    else profileDesigner.removeEtPoint(idx);
                    refresh();
                });
                menu.getItems().add(del);
                menu.show(c, e.getScreenX(), e.getScreenY());
                e.consume();
            });
            pointsLayer.getChildren().add(c);
        }
    }

    private void positionCircle(Circle c, double timeSec, double temp) {
        double w = pointsLayer.getWidth() > 0 ? pointsLayer.getWidth() : chart.getWidth();
        double h = pointsLayer.getHeight() > 0 ? pointsLayer.getHeight() : chart.getHeight();
        if (w <= 0 || h <= 0) return;
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
        double xMin = xAxis.getMin();
        double xMax = xAxis.getMax();
        double yMin = yAxis.getMin();
        double yMax = yAxis.getMax();
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;
        if (xRange <= 0 || yRange <= 0) return;
        double xPx = (timeSec - xMin) / xRange * w;
        double yPx = (yMax - temp) / yRange * h;
        c.setCenterX(xPx);
        c.setCenterY(yPx);
    }

    private void onMousePressed(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        SelectedPoint sp = findNearestPoint(e.getX(), e.getY(), 12);
        selected = sp;
    }

    private void onMouseDragged(MouseEvent e) {
        if (selected == null) return;
        double[] data = pixelToData(e.getX(), e.getY());
        double t = data[0];
        double temp = data[1];
        t = Math.max(0, t);
        temp = Math.max(100, Math.min(280, temp));
        if (selected.type() == CurveType.BT) {
            profileDesigner.moveBtPoint(selected.index(), t, temp);
        } else {
            profileDesigner.moveEtPoint(selected.index(), t, temp);
        }
        refresh();
    }

    private void onMouseReleased(MouseEvent e) {
        selected = null;
    }

    private void onMouseClicked(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
            // double-click on empty chart area: add new BT point
            SelectedPoint near = findNearestPoint(e.getX(), e.getY(), 12);
            if (near != null) return;
            double[] data = pixelToData(e.getX(), e.getY());
            double t = Math.max(0, data[0]);
            double temp = Math.max(100, Math.min(280, data[1]));
            profileDesigner.addBtPoint(t, temp);
            refresh();
        }
    }

    private SelectedPoint findNearestPoint(double xPx, double yPx, double maxDistPx) {
        SelectedPoint best = null;
        double bestDist = Double.POSITIVE_INFINITY;

        List<DesignerPoint> bt = profileDesigner.getBtPoints();
        for (int i = 0; i < bt.size(); i++) {
            double[] p = dataToPixel(bt.get(i).getTimeSec(), bt.get(i).getTemp());
            double d = dist(xPx, yPx, p[0], p[1]);
            if (d < bestDist) {
                bestDist = d;
                best = new SelectedPoint(CurveType.BT, i, null);
            }
        }
        List<DesignerPoint> et = profileDesigner.getEtPoints();
        for (int i = 0; i < et.size(); i++) {
            double[] p = dataToPixel(et.get(i).getTimeSec(), et.get(i).getTemp());
            double d = dist(xPx, yPx, p[0], p[1]);
            if (d < bestDist) {
                bestDist = d;
                best = new SelectedPoint(CurveType.ET, i, null);
            }
        }

        return bestDist <= maxDistPx ? best : null;
    }

    private double[] dataToPixel(double timeSec, double temp) {
        double w = pointsLayer.getWidth() > 0 ? pointsLayer.getWidth() : chart.getWidth();
        double h = pointsLayer.getHeight() > 0 ? pointsLayer.getHeight() : chart.getHeight();
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
        double xMin = xAxis.getMin();
        double xMax = xAxis.getMax();
        double yMin = yAxis.getMin();
        double yMax = yAxis.getMax();
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;
        if (w <= 0 || h <= 0 || xRange <= 0 || yRange <= 0) return new double[] { 0, 0 };
        double xPx = (timeSec - xMin) / xRange * w;
        double yPx = (yMax - temp) / yRange * h;
        return new double[] { xPx, yPx };
    }

    private double[] pixelToData(double xPx, double yPx) {
        double w = pointsLayer.getWidth() > 0 ? pointsLayer.getWidth() : chart.getWidth();
        double h = pointsLayer.getHeight() > 0 ? pointsLayer.getHeight() : chart.getHeight();
        DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
        DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
        double xMin = xAxis.getMin();
        double xMax = xAxis.getMax();
        double yMin = yAxis.getMin();
        double yMax = yAxis.getMax();
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;
        if (w <= 0 || h <= 0 || xRange <= 0 || yRange <= 0) return new double[] { 0, 0 };
        double timeSec = xMin + (xPx / w) * xRange;
        double temp = yMax - (yPx / h) * yRange;
        return new double[] { timeSec, temp };
    }

    private static double dist(double x0, double y0, double x1, double y1) {
        double dx = x0 - x1;
        double dy = y0 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void restoreWindowState() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            double x = p.getDouble(PREFIX + "x", Double.NaN);
            double y = p.getDouble(PREFIX + "y", Double.NaN);
            double w = p.getDouble(PREFIX + "w", Double.NaN);
            double h = p.getDouble(PREFIX + "h", Double.NaN);
            if (Double.isFinite(w) && Double.isFinite(h) && w > 200 && h > 200) {
                stage.setWidth(w);
                stage.setHeight(h);
            }
            if (Double.isFinite(x) && Double.isFinite(y)) {
                stage.setX(x);
                stage.setY(y);
            }
        } catch (Exception ignored) {}
    }

    private void saveWindowState() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            p.putDouble(PREFIX + "x", stage.getX());
            p.putDouble(PREFIX + "y", stage.getY());
            p.putDouble(PREFIX + "w", stage.getWidth());
            p.putDouble(PREFIX + "h", stage.getHeight());
        } catch (Exception ignored) {}
    }
}

