package org.artisan.view;

import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.model.Calculator;
import org.artisan.model.ProfileData;

/**
 * Tools » Calculator: DTR, AUC, and unit conversions.
 */
public final class CalculatorDialog extends ArtisanDialog {

    private final AppController appController;

    // DTR
    private Spinner<Double> totalRoastTimeSpinner;
    private Spinner<Double> developmentTimeSpinner;
    private Label dtrLabel;
    private Label dtrStatusLabel;

    // AUC
    private Spinner<Double> baseTempSpinner;
    private Spinner<Double> startTimeSpinner;
    private Spinner<Double> endTimeSpinner;
    private Button calcAucBtn;
    private Label aucLabel;

    // Conversions
    private TextField celsiusField;
    private TextField fahrenheitField;
    private ComboBox<String> weightUnitCombo;
    private Spinner<Double> weightSpinner;
    private Label weightG;
    private Label weightKg;
    private Label weightLb;
    private Label weightOz;
    private boolean updatingTemp;

    public CalculatorDialog(Window owner, AppController appController) {
        super(owner, true, false);
        this.appController = appController;
        getStage().setTitle("Tools » Calculator");
        getOkButton().setVisible(false);
        getCancelButton().setText("Close");
    }

    @Override
    protected Node buildContent() {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(buildDTRTab());
        tabs.getTabs().add(buildAUCTab());
        tabs.getTabs().add(buildConversionsTab());
        VBox root = new VBox(10, tabs);
        root.setPadding(new Insets(10));
        return root;
    }

    private Tab buildDTRTab() {
        totalRoastTimeSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 7200, 600, 1));
        totalRoastTimeSpinner.setEditable(true);
        developmentTimeSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 3600, 120, 1));
        developmentTimeSpinner.setEditable(true);
        totalRoastTimeSpinner.valueProperty().addListener((a, b, c) -> updateDTR());
        developmentTimeSpinner.valueProperty().addListener((a, b, c) -> updateDTR());

        dtrLabel = new Label("DTR: —%");
        dtrStatusLabel = new Label("Status: —");
        updateDTR();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(new Label("Total roast time (s):"), 0, row);
        grid.add(totalRoastTimeSpinner, 1, row++);
        grid.add(new Label("Development time (s, FC to drop):"), 0, row);
        grid.add(developmentTimeSpinner, 1, row++);
        grid.add(dtrLabel, 0, row);
        grid.add(dtrStatusLabel, 1, row++);

        VBox box = new VBox(10, grid);
        box.setPadding(new Insets(8));
        Tab tab = new Tab("DTR", box);
        tab.setClosable(false);
        return tab;
    }

    private void updateDTR() {
        double total = totalRoastTimeSpinner.getValue();
        double dev = developmentTimeSpinner.getValue();
        double dtr = total > 0 ? 100.0 * dev / total : 0;
        dtrLabel.setText(String.format("DTR: %.1f%%", dtr));
        String status;
        String style;
        if (dtr >= 20 && dtr <= 25) {
            status = "Good (20–25%)";
            style = "-fx-text-fill: green;";
        } else if ((dtr >= 15 && dtr < 20) || (dtr > 25 && dtr <= 30)) {
            status = "Caution (15–20% or 25–30%)";
            style = "-fx-text-fill: orange;";
        } else {
            status = "Outside typical range";
            style = "-fx-text-fill: red;";
        }
        dtrStatusLabel.setText("Status: " + status);
        dtrStatusLabel.setStyle(style);
    }

    private Tab buildAUCTab() {
        baseTempSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 300, 100, 1));
        baseTempSpinner.setEditable(true);
        startTimeSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 3600, 0, 10));
        startTimeSpinner.setEditable(true);
        endTimeSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 3600, 600, 10));
        endTimeSpinner.setEditable(true);
        calcAucBtn = new Button("Calculate from current profile");
        aucLabel = new Label("AUC: — °C·min");
        calcAucBtn.setOnAction(e -> doCalcAUC());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(new Label("Base temp (°C):"), 0, row);
        grid.add(baseTempSpinner, 1, row++);
        grid.add(new Label("Start time (s):"), 0, row);
        grid.add(startTimeSpinner, 1, row++);
        grid.add(new Label("End time (s):"), 0, row);
        grid.add(endTimeSpinner, 1, row++);
        grid.add(calcAucBtn, 0, row, 2, 1);
        grid.add(aucLabel, 0, row + 1, 2, 1);

        VBox box = new VBox(10, grid);
        box.setPadding(new Insets(8));
        Tab tab = new Tab("AUC", box);
        tab.setClosable(false);
        return tab;
    }

    private void doCalcAUC() {
        ProfileData profile = appController != null ? appController.getCurrentProfileData() : null;
        if (profile == null || profile.getTimex() == null || profile.getTemp2() == null) {
            aucLabel.setText("AUC: (no profile)");
            return;
        }
        List<Double> timex = profile.getTimex();
        List<Double> bt = profile.getTemp2();
        double base = baseTempSpinner.getValue();
        double start = startTimeSpinner.getValue();
        double end = endTimeSpinner.getValue();
        double auc = Calculator.computeAUC(timex, bt, base, start, end);
        aucLabel.setText(String.format("AUC: %.1f °C·min", auc));
    }

    private Tab buildConversionsTab() {
        celsiusField = new TextField();
        celsiusField.setPromptText("°C");
        fahrenheitField = new TextField();
        fahrenheitField.setPromptText("°F");
        celsiusField.textProperty().addListener((a, b, c) -> { if (!updatingTemp) celsiusToFahrenheit(); });
        fahrenheitField.textProperty().addListener((a, b, c) -> { if (!updatingTemp) fahrenheitToCelsius(); });

        weightUnitCombo = new ComboBox<>();
        weightUnitCombo.getItems().addAll("g", "kg", "lb", "oz");
        weightUnitCombo.getSelectionModel().selectFirst();
        weightSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 250, 1));
        weightSpinner.setEditable(true);
        weightG = new Label("— g");
        weightKg = new Label("— kg");
        weightLb = new Label("— lb");
        weightOz = new Label("— oz");
        weightUnitCombo.valueProperty().addListener((a, b, c) -> updateWeightConversions());
        weightSpinner.valueProperty().addListener((a, b, c) -> updateWeightConversions());
        updateWeightConversions();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        int row = 0;
        grid.add(new Label("Celsius:"), 0, row);
        grid.add(celsiusField, 1, row++);
        grid.add(new Label("Fahrenheit:"), 0, row);
        grid.add(fahrenheitField, 1, row++);
        grid.add(new Label("Weight unit:"), 0, row);
        grid.add(weightUnitCombo, 1, row++);
        grid.add(new Label("Value:"), 0, row);
        grid.add(weightSpinner, 1, row++);
        grid.add(weightG, 0, row);
        grid.add(weightKg, 1, row++);
        grid.add(weightLb, 0, row);
        grid.add(weightOz, 1, row++);

        VBox box = new VBox(10, grid);
        box.setPadding(new Insets(8));
        Tab tab = new Tab("Conversions", box);
        tab.setClosable(false);
        return tab;
    }

    private void celsiusToFahrenheit() {
        String s = celsiusField.getText();
        if (s == null || s.isBlank()) return;
        try {
            double c = Double.parseDouble(s.trim());
            double f = c * 9 / 5 + 32;
            updatingTemp = true;
            fahrenheitField.setText(String.format("%.2f", f));
            updatingTemp = false;
        } catch (NumberFormatException ignored) {}
    }

    private void fahrenheitToCelsius() {
        String s = fahrenheitField.getText();
        if (s == null || s.isBlank()) return;
        try {
            double f = Double.parseDouble(s.trim());
            double c = (f - 32) * 5 / 9;
            updatingTemp = true;
            celsiusField.setText(String.format("%.2f", c));
            updatingTemp = false;
        } catch (NumberFormatException ignored) {}
    }

    private void updateWeightConversions() {
        double val = weightSpinner.getValue();
        String unit = weightUnitCombo.getValue();
        if (unit == null) unit = "g";
        double g, kg, lb, oz;
        switch (unit) {
            case "kg":
                kg = val;
                g = val * 1000;
                lb = val * 2.20462;
                oz = val * 35.274;
                break;
            case "lb":
                lb = val;
                g = val * 453.592;
                kg = val * 0.453592;
                oz = val * 16;
                break;
            case "oz":
                oz = val;
                g = val * 28.3495;
                kg = val * 0.0283495;
                lb = val / 16;
                break;
            default:
                g = val;
                kg = val / 1000;
                lb = val / 453.592;
                oz = val / 28.3495;
                break;
        }
        weightG.setText(String.format("%.2f g", g));
        weightKg.setText(String.format("%.4f kg", kg));
        weightLb.setText(String.format("%.4f lb", lb));
        weightOz.setText(String.format("%.2f oz", oz));
    }
}
