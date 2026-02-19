package org.artisan.view;

import java.util.List;
import java.util.prefs.Preferences;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.model.ProfileData;
import org.artisan.model.Transposer;
import org.artisan.model.TransposerParams;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.spi.DoubleDataSet;

/**
 * Tools » Transposer: time/temp shift and scale; preview and apply to current profile.
 */
public final class TransposerDialog extends ArtisanDialog {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "transposer.";

    private final AppController appController;
    private Spinner<Double> timeShiftSpinner;
    private Spinner<Double> tempShiftSpinner;
    private Spinner<Double> tempScaleSpinner;
    private Label sourceLabel;
    private XYChart previewChart;
    private DoubleDataSet dataBefore;
    private DoubleDataSet dataAfter;

    public TransposerDialog(Window owner, AppController appController) {
        super(owner, true, false);
        this.appController = appController;
        getStage().setTitle("Tools » Transposer");
    }

    @Override
    protected void onOk(ActionEvent e) {
        applyAndClose();
        super.onOk(e);
    }

    @Override
    protected Node buildContent() {
        ProfileData current = appController != null ? appController.getCurrentProfileData() : null;
        String title = current != null && current.getTitle() != null ? current.getTitle() : "(no profile)";
        sourceLabel = new Label("Source: " + title);

        double timeShift = loadDouble(PREFIX + "timeShift", 0);
        double tempShift = loadDouble(PREFIX + "tempShift", 0);
        double tempScale = loadDouble(PREFIX + "tempScale", 1.0);

        timeShiftSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-600, 600, timeShift, 1));
        timeShiftSpinner.setEditable(true);
        tempShiftSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-50, 50, tempShift, 1));
        tempShiftSpinner.setEditable(true);
        tempScaleSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 2.0, tempScale, 0.01));
        tempScaleSpinner.setEditable(true);

        Button previewBtn = new Button("Preview");
        previewBtn.setOnAction(e -> doPreview());

        DefaultNumericAxis xAxis = new DefaultNumericAxis("Time (s)");
        xAxis.setSide(Side.BOTTOM);
        DefaultNumericAxis yAxis = new DefaultNumericAxis("BT (°C)");
        yAxis.setSide(Side.LEFT);
        previewChart = new XYChart(xAxis, yAxis);
        previewChart.setLegendVisible(true);
        previewChart.setPrefSize(400, 200);
        dataBefore = new DoubleDataSet("Before");
        dataBefore.setStyle("-fx-stroke: #0a5c90; -fx-stroke-width: 2px;");
        dataAfter = new DoubleDataSet("After");
        dataAfter.setStyle("-fx-stroke: #cc0f50; -fx-stroke-width: 2px;");
        previewChart.getDatasets().addAll(dataBefore, dataAfter);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(sourceLabel, 0, row++, 2, 1);
        grid.add(new Label("Time shift (s):"), 0, row);
        grid.add(timeShiftSpinner, 1, row++);
        grid.add(new Label("Temp shift (°C):"), 0, row);
        grid.add(tempShiftSpinner, 1, row++);
        grid.add(new Label("Temp scale:"), 0, row);
        grid.add(tempScaleSpinner, 1, row++);
        grid.add(previewBtn, 0, row++, 2, 1);

        VBox root = new VBox(10, grid, previewChart);
        root.setPadding(new Insets(10));
        return root;
    }

    private void doPreview() {
        ProfileData current = appController != null ? appController.getCurrentProfileData() : null;
        if (current == null || current.getTimex() == null || current.getTimex().isEmpty()) return;
        TransposerParams params = getParams();
        ProfileData after = Transposer.apply(current, params);
        if (after == null) return;
        List<Double> tx = current.getTimex();
        List<Double> btBefore = current.getTemp2();
        List<Double> txAfter = after.getTimex();
        List<Double> btAfter = after.getTemp2();
        if (tx != null && btBefore != null) {
            int n = Math.min(tx.size(), btBefore.size());
            double[] x = new double[n], y = new double[n];
            for (int i = 0; i < n; i++) {
                x[i] = tx.get(i);
                y[i] = btBefore.get(i) != null ? btBefore.get(i) : 0;
            }
            dataBefore.set(x, y);
        }
        if (txAfter != null && btAfter != null) {
            int n = Math.min(txAfter.size(), btAfter.size());
            double[] x = new double[n], y = new double[n];
            for (int i = 0; i < n; i++) {
                x[i] = txAfter.get(i);
                y[i] = btAfter.get(i) != null ? btAfter.get(i) : 0;
            }
            dataAfter.set(x, y);
        }
    }

    private void applyAndClose() {
        ProfileData current = appController != null ? appController.getCurrentProfileData() : null;
        if (current == null) return;
        savePreferences();
        TransposerParams params = getParams();
        ProfileData after = Transposer.apply(current, params);
        if (after != null && appController != null) {
            appController.loadSimulatedProfile(after);
        }
    }

    private TransposerParams getParams() {
        double timeShift = timeShiftSpinner.getValue();
        double tempShift = tempShiftSpinner.getValue();
        double tempScale = tempScaleSpinner.getValue();
        return new TransposerParams(timeShift, tempShift, tempScale);
    }

    private static double loadDouble(String key, double def) {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            return p.getDouble(key, def);
        } catch (Exception e) {
            return def;
        }
    }

    private void savePreferences() {
        try {
            Preferences p = Preferences.userRoot().node(PREFS_NODE);
            p.putDouble(PREFIX + "timeShift", timeShiftSpinner.getValue());
            p.putDouble(PREFIX + "tempShift", tempShiftSpinner.getValue());
            p.putDouble(PREFIX + "tempScale", tempScaleSpinner.getValue());
        } catch (Exception ignored) {}
    }
}
