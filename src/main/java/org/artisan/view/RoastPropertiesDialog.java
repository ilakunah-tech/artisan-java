package org.artisan.view;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.artisan.controller.AppController;
import org.artisan.model.RoastProperties;
import org.artisan.model.RoastPropertiesValidator;

/**
 * Roast → Properties... dialog. Two tabs: Roast Info, Bean Info.
 * OK/Apply: validate via RoastPropertiesValidator.showErrors, save, call appController.onRoastPropertiesChanged.
 */
public final class RoastPropertiesDialog extends ArtisanDialog {

    private final AppController appController;
    private final Runnable onApplied;

    private TextField titleField;
    private TextField operatorField;
    private DatePicker roastDatePicker;
    private Spinner<Double> greenWeightSpinner;
    private Spinner<Double> roastedWeightSpinner;
    private Label weightLossLabel;
    private Spinner<Integer> roastColorSpinner;
    private TextArea notesArea;

    private TextField originField;
    private TextField varietyField;
    private TextField processField;
    private TextField gradeField;
    private Spinner<Double> moistureSpinner;
    private Spinner<Double> densitySpinner;
    private ListView<String> customLabelsList;
    private javafx.collections.ObservableList<String> customLabelsItems;

    public RoastPropertiesDialog(Window owner, AppController appController, Runnable onApplied) {
        super(owner, true, true);
        this.appController = appController;
        this.onApplied = onApplied != null ? onApplied : () -> {};
        getStage().setTitle("Roast Properties");
        getApplyButton().setOnAction(e -> apply(false));
    }

    @Override
    protected Node buildContent() {
        RoastProperties props = appController.getRoastProperties();

        titleField = new TextField(props.getTitle());
        titleField.setPromptText("Roast title");
        operatorField = new TextField(props.getOperator());
        operatorField.setPromptText("Operator");
        roastDatePicker = new DatePicker();
        try {
            if (props.getRoastDate() != null && !props.getRoastDate().isBlank()) {
                roastDatePicker.setValue(LocalDate.parse(props.getRoastDate()));
            }
        } catch (DateTimeParseException ignored) {}
        greenWeightSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0, 10_000.0, props.getGreenWeight(), 0.1));
        greenWeightSpinner.setEditable(true);
        roastedWeightSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0, 10_000.0, props.getRoastedWeight(), 0.1));
        roastedWeightSpinner.setEditable(true);
        weightLossLabel = new Label("Weight loss: — %");
        roastColorSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                0, 100, props.getRoastColor(), 1));
        roastColorSpinner.setEditable(true);
        notesArea = new TextArea(props.getNotes());
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        Runnable updateWeightLoss = () -> {
            double g = greenWeightSpinner.getValue();
            double r = roastedWeightSpinner.getValue();
            double pct = g > 0 ? (g - r) / g * 100.0 : 0.0;
            weightLossLabel.setText(String.format("Weight loss: %.1f%%", pct));
        };
        greenWeightSpinner.getValueFactory().valueProperty().addListener((a, b, c) -> updateWeightLoss.run());
        roastedWeightSpinner.getValueFactory().valueProperty().addListener((a, b, c) -> updateWeightLoss.run());
        updateWeightLoss.run();

        GridPane roastGrid = new GridPane();
        roastGrid.setHgap(10);
        roastGrid.setVgap(8);
        int r = 0;
        roastGrid.add(new Label("Title:"), 0, r);
        roastGrid.add(titleField, 1, r++);
        roastGrid.add(new Label("Operator:"), 0, r);
        roastGrid.add(operatorField, 1, r++);
        roastGrid.add(new Label("Roast date:"), 0, r);
        roastGrid.add(roastDatePicker, 1, r++);
        roastGrid.add(new Label("Green weight (g):"), 0, r);
        roastGrid.add(greenWeightSpinner, 1, r++);
        roastGrid.add(new Label("Roasted weight (g):"), 0, r);
        roastGrid.add(roastedWeightSpinner, 1, r++);
        roastGrid.add(weightLossLabel, 0, r++, 2, 1);
        roastGrid.add(new Label("Roast color (0–100):"), 0, r);
        roastGrid.add(roastColorSpinner, 1, r++);
        roastGrid.add(new Label("Notes:"), 0, r);
        roastGrid.add(notesArea, 1, r++);
        Tab roastInfoTab = new Tab("Roast Info", roastGrid);

        originField = new TextField(props.getBeanOrigin());
        varietyField = new TextField(props.getBeanVariety());
        processField = new TextField(props.getBeanProcess());
        gradeField = new TextField(props.getBeanGrade());
        moistureSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0, 100.0, props.getMoisture(), 0.1));
        moistureSpinner.setEditable(true);
        densitySpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0, 2000.0, props.getDensity(), 1.0));
        densitySpinner.setEditable(true);
        customLabelsItems = javafx.collections.FXCollections.observableArrayList(props.getCustomLabels());
        customLabelsList = new ListView<>(customLabelsItems);
        customLabelsList.setPrefHeight(120);

        javafx.scene.control.Button addLabelBtn = new javafx.scene.control.Button("Add");
        javafx.scene.control.Button editLabelBtn = new javafx.scene.control.Button("Edit");
        javafx.scene.control.Button removeLabelBtn = new javafx.scene.control.Button("Remove");
        addLabelBtn.setOnAction(e -> {
            String line = promptKeyValue("Add custom label", "key=value", "");
            if (line != null && !line.isBlank()) customLabelsItems.add(line);
        });
        editLabelBtn.setOnAction(e -> {
            String sel = customLabelsList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String line = promptKeyValue("Edit custom label", "key=value", sel);
            if (line != null && !line.isBlank()) {
                int i = customLabelsList.getSelectionModel().getSelectedIndex();
                customLabelsItems.set(i, line);
            }
        });
        removeLabelBtn.setOnAction(e -> {
            int i = customLabelsList.getSelectionModel().getSelectedIndex();
            if (i >= 0) customLabelsItems.remove(i);
        });
        HBox labelButtons = new HBox(6, addLabelBtn, editLabelBtn, removeLabelBtn);

        GridPane beanGrid = new GridPane();
        beanGrid.setHgap(10);
        beanGrid.setVgap(8);
        int br = 0;
        beanGrid.add(new Label("Origin:"), 0, br);
        beanGrid.add(originField, 1, br++);
        beanGrid.add(new Label("Variety:"), 0, br);
        beanGrid.add(varietyField, 1, br++);
        beanGrid.add(new Label("Process:"), 0, br);
        beanGrid.add(processField, 1, br++);
        beanGrid.add(new Label("Grade:"), 0, br);
        beanGrid.add(gradeField, 1, br++);
        beanGrid.add(new Label("Moisture (%):"), 0, br);
        beanGrid.add(moistureSpinner, 1, br++);
        beanGrid.add(new Label("Density (g/L):"), 0, br);
        beanGrid.add(densitySpinner, 1, br++);
        beanGrid.add(new Label("Custom labels (key=value):"), 0, br++);
        beanGrid.add(new VBox(4, customLabelsList, labelButtons), 0, br++, 2, 1);
        Tab beanInfoTab = new Tab("Bean Info", beanGrid);

        TabPane tabs = new TabPane(roastInfoTab, beanInfoTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox root = new VBox(10, tabs);
        root.setPadding(new Insets(10));
        return root;
    }

    private String promptKeyValue(String title, String prompt, String initial) {
        javafx.scene.control.TextInputDialog d = new javafx.scene.control.TextInputDialog(initial);
        d.setTitle(title);
        d.setHeaderText(prompt);
        d.getEditor().setPromptText("key=value");
        return d.showAndWait().orElse(null);
    }

    private RoastProperties copyFromUi() {
        RoastProperties p = new RoastProperties();
        p.setTitle(titleField.getText());
        p.setOperator(operatorField.getText());
        if (roastDatePicker.getValue() != null) {
            p.setRoastDate(roastDatePicker.getValue().toString());
        } else {
            p.setRoastDate("");
        }
        p.setGreenWeight(greenWeightSpinner.getValue());
        p.setRoastedWeight(roastedWeightSpinner.getValue());
        p.setRoastColor(roastColorSpinner.getValue());
        p.setNotes(notesArea.getText());
        p.setBeanOrigin(originField.getText());
        p.setBeanVariety(varietyField.getText());
        p.setBeanProcess(processField.getText());
        p.setBeanGrade(gradeField.getText());
        p.setMoisture(moistureSpinner.getValue());
        p.setDensity(densitySpinner.getValue());
        p.setCustomLabels(new ArrayList<>(customLabelsItems));
        return p;
    }

    private void copyToController(RoastProperties p) {
        RoastProperties current = appController.getRoastProperties();
        current.setTitle(p.getTitle());
        current.setNotes(p.getNotes());
        current.setRoastDate(p.getRoastDate());
        current.setBeanOrigin(p.getBeanOrigin());
        current.setBeanVariety(p.getBeanVariety());
        current.setBeanProcess(p.getBeanProcess());
        current.setBeanGrade(p.getBeanGrade());
        current.setGreenWeight(p.getGreenWeight());
        current.setRoastedWeight(p.getRoastedWeight());
        current.setMoisture(p.getMoisture());
        current.setDensity(p.getDensity());
        current.setRoastColor(p.getRoastColor());
        current.setOperator(p.getOperator());
        current.setCustomLabels(p.getCustomLabels());
    }

    private void apply(boolean close) {
        RoastProperties p = copyFromUi();
        if (!RoastPropertiesValidator.showErrors(getStage(), p)) return;
        copyToController(p);
        p.save();
        appController.onRoastPropertiesChanged(appController.getRoastProperties());
        onApplied.run();
        if (close) getStage().close();
    }

    @Override
    protected void onOk(javafx.event.ActionEvent e) {
        apply(true);
        super.onOk(e);
    }
}
