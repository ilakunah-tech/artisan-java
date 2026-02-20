package org.artisan.ui.vm;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.artisan.model.EventEntry;

/**
 * View-model exposing roast state as JavaFX properties for UI binding.
 * Updated from AppController / RoastSession (on FX thread).
 */
public final class RoastViewModel {

    private final DoubleProperty bt = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty et = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty rorBT = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty rorET = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty deltaBT = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty elapsedSec = new SimpleDoubleProperty(0.0);
    private final StringProperty phaseName = new SimpleStringProperty("");
    private final StringProperty connectionStatus = new SimpleStringProperty("Disconnected");
    private final BooleanProperty samplingActive = new SimpleBooleanProperty(false);
    private final DoubleProperty gasPercent = new SimpleDoubleProperty(0.0);
    private final DoubleProperty airPercent = new SimpleDoubleProperty(0.0);
    private final DoubleProperty drumPercent = new SimpleDoubleProperty(0.0);
    private final ObservableList<EventEntry> events = FXCollections.observableArrayList();

    public DoubleProperty btProperty() { return bt; }
    public DoubleProperty etProperty() { return et; }
    public DoubleProperty rorBTProperty() { return rorBT; }
    public DoubleProperty rorETProperty() { return rorET; }
    public DoubleProperty deltaBTProperty() { return deltaBT; }
    public DoubleProperty elapsedSecProperty() { return elapsedSec; }
    public StringProperty phaseNameProperty() { return phaseName; }
    public StringProperty connectionStatusProperty() { return connectionStatus; }
    public BooleanProperty samplingActiveProperty() { return samplingActive; }
    public DoubleProperty gasPercentProperty() { return gasPercent; }
    public DoubleProperty airPercentProperty() { return airPercent; }
    public DoubleProperty drumPercentProperty() { return drumPercent; }
    public ObservableList<EventEntry> getEvents() { return events; }

    public double getBt() { return bt.get(); }
    public double getEt() { return et.get(); }
    public double getRorBT() { return rorBT.get(); }
    public double getRorET() { return rorET.get(); }
    public double getDeltaBT() { return deltaBT.get(); }
    public double getElapsedSec() { return elapsedSec.get(); }
    public String getPhaseName() { return phaseName.get(); }
    public String getConnectionStatus() { return connectionStatus.get(); }
    public boolean isSamplingActive() { return samplingActive.get(); }
    public double getGasPercent() { return gasPercent.get(); }
    public double getAirPercent() { return airPercent.get(); }
    public double getDrumPercent() { return drumPercent.get(); }

    public void setBt(double v) { bt.set(v); }
    public void setEt(double v) { et.set(v); }
    public void setRorBT(double v) { rorBT.set(v); }
    public void setRorET(double v) { rorET.set(v); }
    public void setDeltaBT(double v) { deltaBT.set(v); }
    public void setElapsedSec(double v) { elapsedSec.set(v); }
    public void setPhaseName(String v) { phaseName.set(v != null ? v : ""); }
    public void setConnectionStatus(String v) { connectionStatus.set(v != null ? v : ""); }
    public void setSamplingActive(boolean v) { samplingActive.set(v); }
    public void setGasPercent(double v) { gasPercent.set(v); }
    public void setAirPercent(double v) { airPercent.set(v); }
    public void setDrumPercent(double v) { drumPercent.set(v); }

    /** Replace events list with current snapshot from session. */
    public void syncEvents(java.util.List<EventEntry> list) {
        events.clear();
        if (list != null) events.addAll(list);
    }
}
