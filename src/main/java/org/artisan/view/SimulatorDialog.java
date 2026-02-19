package org.artisan.view;

import java.util.List;
import java.util.prefs.Preferences;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.device.SimulatorConfig;
import org.artisan.model.ProfileData;
import org.artisan.model.RoastSimulator;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.spi.DoubleDataSet;

/**
 * Tools » Simulator: generate synthetic roast profile; preview and load as profile.
 */
public final class SimulatorDialog extends ArtisanDialog {

    private static final String PREFS_NODE = "org/artisan/artisan-java";
    private static final String PREFIX = "simulator_tool.";

    private final AppController appController;
    private Spinner<Double> chargeTempSpinner;
    private Spinner<Double> dropTempSpinner;
    private Spinner<Integer> totalTimeSpinner;
    private Spinner<Double> rorPeakSpinner;
    private Spinner<Integer> rorPeakTimeSpinner;
    private Spinner<Double> etOffsetSpinner;
    private Spinner<Double> noiseAmplitudeSpinner;
    private XYChart previewChart;
    private DoubleDataSet dataBT;

    public SimulatorDialog(Window owner, AppController appController) {
        super(owner, true, false);
        this.appController = appController;
        getStage().setTitle("Tools » Simulator");
        getOkButton().setVisible(false);
        getCancelButton().setText("Close");
    }

    @Override
    protected Node buildContent() {
        double chargeTemp = loadDouble(PREFIX + "chargeTemp", 200);
        double dropTemp = loadDouble(PREFIX + "dropTemp", 210);
        int totalTime = (int) loadDouble(PREFIX + "totalTime", 600);
        double rorPeak = loadDouble(PREFIX + "rorPeak", 15);
        int rorPeakTime = (int) loadDouble(PREFIX + "rorPeakTime", 90);
        double etOffset = loadDouble(PREFIX + "etOffset", 20);
        double noiseAmplitude = loadDouble(PREFIX + "noiseAmplitude", 0.5);

        chargeTempSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(50, 300, chargeTemp, 1));
        chargeTempSpinner.setEditable(true);
        dropTempSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(150, 300, dropTemp, 1));
        dropTempSpinner.setEditable(true);
        totalTimeSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(300, 1800, totalTime, 30));
        totalTimeSpinner.setEditable(true);
        rorPeakSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(5, 30, rorPeak, 0.5));
        rorPeakSpinner.setEditable(true);
        rorPeakTimeSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 600, rorPeakTime, 10));
        rorPeakTimeSpinner.setEditable(true);
        etOffsetSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 60, etOffset, 1));
        etOffsetSpinner.setEditable(true);
        noiseAmplitudeSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 5, noiseAmplitude, 0.1));
        noiseAmplitudeSpinner.setEditable(true);

        Button previewBtn = new Button("Preview");
        previewBtn.setOnAction(e -> doPreview());
        Button loadBtn = new Button("Load as Profile");
        loadBtn.setOnAction(e -> doLoadAndClose());

        DefaultNumericAxis xAxis = new DefaultNumericAxis("Time (s)");
        xAxis.setSide(Side.BOTTOM);
        DefaultNumericAxis yAxis = new DefaultNumericAxis("BT (°C)");
        yAxis.setSide(Side.LEFT);
        previewChart = new XYChart(xAxis, yAxis);
        previewChart.setLegendVisible(false);
        previewChart.setPrefSize(450, 220);
        dataBT = new DoubleDataSet("BT");
        dataBT.setStyle("-fx-stroke: #0a5c90; -fx-stroke-width: 2px;");
        previewChart.getDatasets().add(dataBT);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        int row = 0;
        grid.add(new Label("Charge temp (°C):"), 0, row);
        grid.add(chargeTempSpinner, 1, row++);
        grid.add(new Label("Drop temp (°C):"), 0, row);
        grid.add(dropTempSpinner, 1, row++);
        grid.add(new Label("Total time (s):"), 0, row);
        grid.add(totalTimeSpinner, 1, row++);
        grid.add(new Label("RoR peak (°C/min):"), 0, row);
        grid.add(rorPeakSpinner, 1, row++);
        grid.add(new Label("RoR peak time (s):"), 0, row);
        grid.add(rorPeakTimeSpinner, 1, row++);
        grid.add(new Label("ET offset (°C):"), 0, row);
        grid.add(etOffsetSpinner, 1, row++);
        grid.add(new Label("Noise amplitude (°C):"), 0, row);
        grid.add(noiseAmplitudeSpinner, 1, row++);
        grid.add(previewBtn, 0, row);
        grid.add(loadBtn, 1, row++);

        VBox root = new VBox(10, grid, previewChart);
        root.setPadding(new Insets(10));
        return root;
    }

    private void doPreview() {
        RoastSimulator sim = buildSimulator();
        ProfileData pd = sim.generate();
        if (pd == null || pd.getTimex() == null || pd.getTemp2() == null) return;
        List<Double> tx = pd.getTimex();
        List<Double> bt = pd.getTemp2();
        int n = Math.min(tx.size(), bt.size());
        double[] x = new double[n], y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = tx.get(i);
            y[i] = bt.get(i) != null ? bt.get(i) : 0;
        }
        dataBT.set(x, y);
    }

    private void doLoadAndClose() {
        savePreferences();
        RoastSimulator sim = buildSimulator();
        ProfileData pd = sim.generate();
        if (pd != null && appController != null) {
            appController.loadSimulatedProfile(pd);
        }
        getStage().close();
    }

    private RoastSimulator buildSimulator() {
        RoastSimulator sim = new RoastSimulator();
        sim.setChargeTemp(chargeTempSpinner.getValue());
        sim.setDropTemp(dropTempSpinner.getValue());
        sim.setTotalTimeSeconds(totalTimeSpinner.getValue());
        sim.setRorPeak(rorPeakSpinner.getValue());
        sim.setRorPeakTime(rorPeakTimeSpinner.getValue());
        SimulatorConfig cfg = new SimulatorConfig();
        cfg.setEtOffset(etOffsetSpinner.getValue());
        cfg.setNoiseAmplitude(noiseAmplitudeSpinner.getValue());
        cfg.setSpeedMultiplier(1.0);
        sim.setConfig(cfg);
        return sim;
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
            p.putDouble(PREFIX + "chargeTemp", chargeTempSpinner.getValue());
            p.putDouble(PREFIX + "dropTemp", dropTempSpinner.getValue());
            p.putDouble(PREFIX + "totalTime", totalTimeSpinner.getValue());
            p.putDouble(PREFIX + "rorPeak", rorPeakSpinner.getValue());
            p.putDouble(PREFIX + "rorPeakTime", rorPeakTimeSpinner.getValue());
            p.putDouble(PREFIX + "etOffset", etOffsetSpinner.getValue());
            p.putDouble(PREFIX + "noiseAmplitude", noiseAmplitudeSpinner.getValue());
        } catch (Exception ignored) {}
    }
}
