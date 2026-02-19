package org.artisan.controller;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.media.AudioClip;

import org.artisan.model.Alarm;
import org.artisan.model.AlarmAction;
import org.artisan.model.AlarmCondition;
import org.artisan.model.AlarmList;
import org.artisan.model.PhasesConfig;
import org.artisan.model.ProfileData;

/**
 * Evaluates alarms on each sample and executes actions (IF-THEN style, matching Python Artisan).
 * Call evaluate() from the JavaFX Application Thread (e.g. from AppController.afterSample).
 */
public final class AlarmEngine {

    private static final Logger LOG = Logger.getLogger(AlarmEngine.class.getName());

    private final AlarmList alarms;
    private final Runnable onRedraw;
    private Consumer<String> markEventCallback;
    private Consumer<Double> burnerCallback;

    public AlarmEngine(AlarmList alarms, Runnable onRedraw) {
        this.alarms = alarms != null ? alarms : new AlarmList();
        this.onRedraw = onRedraw != null ? onRedraw : () -> {};
    }

    /**
     * Clears all "triggered" flags. Call on CHARGE.
     */
    public void reset() {
        alarms.resetAll();
    }

    /**
     * Sets the callback for MARK_EVENT (event name/label). MainWindow wires this to mark event on chart.
     */
    public void setMarkEventCallback(Consumer<String> markEventCallback) {
        this.markEventCallback = markEventCallback;
    }

    /**
     * Sets the callback for SET_BURNER (percentage 0–100). MainWindow wires this to slider or status.
     */
    public void setBurnerCallback(Consumer<Double> burnerCallback) {
        this.burnerCallback = burnerCallback;
    }

    /**
     * Evaluates all enabled alarms and runs actions for those that fire.
     * Called from JavaFX thread (e.g. afterSample).
     */
    public void evaluate(double timeSec, double bt, double et, double rorBt,
                         ProfileData profile, PhasesConfig phases) {
        if (profile == null) return;
        double chargeTimeSec = chargeTimeSeconds(profile);
        int n = alarms.size();
        for (int i = 0; i < n; i++) {
            Alarm a = alarms.get(i);
            if (!a.isEnabled()) continue;
            if (a.isTriggered()) continue;
            int guard = a.getGuardAlarmIndex();
            if (guard >= 0 && guard < n && !alarms.get(guard).isTriggered()) continue;
            if (!conditionMet(a, timeSec, bt, et, rorBt, chargeTimeSec)) continue;
            a.markTriggered();
            executeAction(a);
        }
        onRedraw.run();
    }

    /**
     * Evaluates condition for one alarm.
     */
    private boolean conditionMet(Alarm a, double timeSec, double bt, double et, double rorBt, double chargeTimeSec) {
        double th = a.getThreshold();
        switch (a.getCondition()) {
            case BT_RISES_ABOVE:
                return bt >= th;
            case BT_FALLS_BELOW:
                return bt <= th;
            case ET_RISES_ABOVE:
                return et >= th;
            case ET_FALLS_BELOW:
                return et <= th;
            case ROR_RISES_ABOVE:
                return rorBt >= th;
            case ROR_FALLS_BELOW:
                return rorBt <= th;
            case TIME_AFTER_CHARGE:
                return (timeSec - chargeTimeSec) >= th;
            case TIME_AFTER_EVENT:
                return timeSec >= th;
            default:
                return false;
        }
    }

    private static double chargeTimeSeconds(ProfileData profile) {
        List<Integer> ti = profile.getTimeindex();
        List<Double> tx = profile.getTimex();
        if (ti == null || tx == null || ti.isEmpty()) return 0.0;
        int idx = ti.get(0);
        if (idx < 0 || idx >= tx.size()) return 0.0;
        return tx.get(idx);
    }

    private void executeAction(Alarm a) {
        AlarmAction action = a.getAction();
        String param = a.getActionParam() != null ? a.getActionParam() : "";
        switch (action) {
            case POPUP_MESSAGE:
                Platform.runLater(() -> showPopup(param));
                break;
            case PLAY_SOUND:
                Platform.runLater(() -> playSound(param));
                break;
            case MARK_EVENT:
                if (markEventCallback != null) {
                    markEventCallback.accept(param);
                }
                break;
            case SET_BURNER:
                if (burnerCallback != null) {
                    double pct = parsePercent(param);
                    burnerCallback.accept(pct);
                }
                break;
            case CALL_PROGRAM:
                runProgram(param);
                break;
            default:
                break;
        }
    }

    private void showPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alarm");
        alert.setHeaderText(null);
        alert.setContentText(message != null && !message.isEmpty() ? message : "Alarm triggered");
        alert.initOwner(null);
        alert.show();
    }

    private void playSound(String path) {
        if (path == null || path.isEmpty()) return;
        File f = new File(path);
        if (!f.exists()) return;
        try {
            AudioClip clip = new AudioClip(f.toURI().toString());
            clip.play();
        } catch (Exception e) {
            LOG.log(Level.FINE, "Could not play sound: " + path, e);
        }
    }

    private static double parsePercent(String s) {
        if (s == null || s.isEmpty()) return 0.0;
        try {
            double v = Double.parseDouble(s.trim());
            if (v < 0) return 0.0;
            if (v > 100) return 100.0;
            return v;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void runProgram(String command) {
        if (command == null || command.isEmpty()) return;
        Thread t = new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec(command);
                p.waitFor();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Alarm CALL_PROGRAM failed: " + command, e);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Fires the given alarm's action immediately (for Test button). Does not check condition or mark triggered.
     */
    public void testAlarm(Alarm a) {
        if (a == null) return;
        executeAction(a);
    }
}
