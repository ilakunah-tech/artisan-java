package org.artisan.ui.vm;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.stage.Stage;
import org.artisan.ui.components.ToastNotification;
import org.artisan.ui.model.AlertRule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Evaluates alert rules against current vm values. Fires toast and/or beep when threshold met.
 */
public final class AlertsManager {

    private final RoastViewModel vm;
    private final Supplier<List<AlertRule>> rulesSupplier;
    private Stage stage;
    private boolean attached;

    public AlertsManager(RoastViewModel vm, Supplier<List<AlertRule>> rulesSupplier) {
        this.vm = vm;
        this.rulesSupplier = rulesSupplier != null ? rulesSupplier : ArrayList::new;
    }

    public void setStage(Stage stage) { this.stage = stage; }

    public void attach() {
        if (attached) return;
        attached = true;
        ChangeListener<Number> eval = (o, ov, nv) -> evaluate();
        vm.btProperty().addListener(eval);
        vm.etProperty().addListener(eval);
        vm.rorBTProperty().addListener(eval);
        vm.gasValueProperty().addListener(eval);
        vm.airValueProperty().addListener(eval);
        vm.drumValueProperty().addListener(eval);
    }

    public void detach() {
        attached = false;
    }

    private void evaluate() {
        List<AlertRule> rules = rulesSupplier.get();
        if (rules == null) return;
        for (AlertRule r : rules) {
            double val = valueForCurve(r.getCurveName());
            if (evaluateRule(r, val)) {
                fireAlert(r);
            }
        }
    }

    private double valueForCurve(String curve) {
        if (curve == null) return Double.NaN;
        return switch (curve.toUpperCase()) {
            case "BT" -> vm.getBt();
            case "ET" -> vm.getEt();
            case "ROR" -> vm.getRorBT();
            case "GAS" -> vm.getGasValue();
            case "AIR" -> vm.getAirValue();
            case "DRUM" -> vm.getDrumValue();
            default -> Double.NaN;
        };
    }

    private boolean evaluateRule(AlertRule r, double val) {
        if (!Double.isFinite(val)) return false;
        double th = r.getThreshold();
        return switch (r.getOperator()) {
            case ">" -> val > th;
            case ">=" -> val >= th;
            case "<" -> val < th;
            case "<=" -> val <= th;
            case "=" -> Math.abs(val - th) < 0.01;
            default -> false;
        };
    }

    private void fireAlert(AlertRule r) {
        ToastNotification.ToastType type = ToastNotification.ToastType.INFO;
        if (r.getType() == AlertRule.NotificationType.VISUAL || r.getType() == AlertRule.NotificationType.BOTH) {
            Stage s = stage;
            Platform.runLater(() -> {
                if (s != null) ToastNotification.show(s, type, r.getMessage());
            });
        }
    }
}
