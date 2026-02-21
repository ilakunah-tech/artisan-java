package org.artisan.ui.vm;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.artisan.model.EventEntry;
import org.artisan.model.EventType;
import org.artisan.ui.model.RoastEvent;

import java.util.List;

/**
 * View-model exposing roast state as JavaFX properties for UI binding.
 * Updated from AppController / RoastSession (on FX thread).
 */
public final class RoastViewModel {

    private static String eventTypeAbbrev(EventType type) {
        if (type == null) return "?";
        return switch (type) {
            case CHARGE -> "CH";
            case DRY_END -> "DE";
            case FC_START -> "FC↑";
            case FC_END -> "FC↓";
            case DROP -> "DR";
            case COOL_END -> "CMT";
            default -> type.name().length() > 2 ? type.name().substring(0, 2) : type.name();
        };
    }

    private final DoubleProperty bt = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty et = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty rorBT = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty rorET = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty deltaBT = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty elapsedSec = new SimpleDoubleProperty(0.0);
    private final LongProperty elapsedSeconds = new SimpleLongProperty(0);
    private final DoubleProperty devTimeSec = new SimpleDoubleProperty(Double.NaN);
    private final StringProperty phaseName = new SimpleStringProperty("");
    private final StringProperty connectionStatus = new SimpleStringProperty("Disconnected");
    private final BooleanProperty samplingActive = new SimpleBooleanProperty(false);
    private final DoubleProperty gasPercent = new SimpleDoubleProperty(0.0);
    private final DoubleProperty airPercent = new SimpleDoubleProperty(0.0);
    private final DoubleProperty drumPercent = new SimpleDoubleProperty(0.0);
    private final BooleanProperty smartPredictionsEnabled = new SimpleBooleanProperty(true);
    private final BooleanProperty bbtActive = new SimpleBooleanProperty(false);
    private final LongProperty bbpElapsedSeconds = new SimpleLongProperty(0);
    private final BooleanProperty replayMode = new SimpleBooleanProperty(false);
    private final ObservableList<EventEntry> events = FXCollections.observableArrayList();
    private final ObservableList<RoastEvent> roastEvents = FXCollections.observableArrayList();

    public RoastViewModel() {
        elapsedSec.addListener((o, oldV, newV) ->
            elapsedSeconds.set(newV != null && newV.doubleValue() >= 0 ? (long) newV.doubleValue() : 0));
    }

    public DoubleProperty btProperty() { return bt; }
    public DoubleProperty etProperty() { return et; }
    public DoubleProperty rorBTProperty() { return rorBT; }
    public DoubleProperty rorProperty() { return rorBT; }
    public DoubleProperty rorETProperty() { return rorET; }
    public DoubleProperty deltaBTProperty() { return deltaBT; }
    public DoubleProperty elapsedSecProperty() { return elapsedSec; }
    public LongProperty elapsedSecondsProperty() { return elapsedSeconds; }
    public DoubleProperty devTimeSecProperty() { return devTimeSec; }
    public StringProperty phaseNameProperty() { return phaseName; }
    public StringProperty currentPhaseNameProperty() { return phaseName; }
    public StringProperty connectionStatusProperty() { return connectionStatus; }
    public BooleanProperty samplingActiveProperty() { return samplingActive; }
    public DoubleProperty gasPercentProperty() { return gasPercent; }
    public DoubleProperty gasValueProperty() { return gasPercent; }
    public DoubleProperty airPercentProperty() { return airPercent; }
    public DoubleProperty airValueProperty() { return airPercent; }
    public DoubleProperty drumPercentProperty() { return drumPercent; }
    public DoubleProperty drumValueProperty() { return drumPercent; }
    public BooleanProperty smartPredictionsEnabledProperty() { return smartPredictionsEnabled; }
    public BooleanProperty bbtActiveProperty() { return bbtActive; }
    public LongProperty bbpElapsedSecondsProperty() { return bbpElapsedSeconds; }
    public BooleanProperty replayModeProperty() { return replayMode; }
    public ObservableList<EventEntry> getEvents() { return events; }
    public ObservableList<RoastEvent> getRoastEvents() { return roastEvents; }

    public double getBt() { return bt.get(); }
    public double getEt() { return et.get(); }
    public double getRorBT() { return rorBT.get(); }
    public double getRorET() { return rorET.get(); }
    public double getDeltaBT() { return deltaBT.get(); }
    public double getElapsedSec() { return elapsedSec.get(); }
    public long getElapsedSeconds() { return elapsedSeconds.get(); }
    public double getDevTimeSec() { return devTimeSec.get(); }
    public String getPhaseName() { return phaseName.get(); }
    public String getConnectionStatus() { return connectionStatus.get(); }
    public boolean isSamplingActive() { return samplingActive.get(); }
    public double getGasPercent() { return gasPercent.get(); }
    public double getGasValue() { return gasPercent.get(); }
    public double getAirPercent() { return airPercent.get(); }
    public double getAirValue() { return airPercent.get(); }
    public double getDrumPercent() { return drumPercent.get(); }
    public double getDrumValue() { return drumPercent.get(); }
    public boolean isSmartPredictionsEnabled() { return smartPredictionsEnabled.get(); }
    public boolean isBbtActive() { return bbtActive.get(); }
    public long getBbpElapsedSeconds() { return bbpElapsedSeconds.get(); }
    public boolean isReplayMode() { return replayMode.get(); }

    public void setBt(double v) { bt.set(v); }
    public void setEt(double v) { et.set(v); }
    public void setRorBT(double v) { rorBT.set(v); }
    public void setRorET(double v) { rorET.set(v); }
    public void setDeltaBT(double v) { deltaBT.set(v); }
    public void setElapsedSec(double v) { elapsedSec.set(v); }
    public void setElapsedSeconds(long v) { elapsedSeconds.set(v); }
    public void setDevTimeSec(double v) { devTimeSec.set(v); }
    public void setPhaseName(String v) { phaseName.set(v != null ? v : ""); }
    public void setConnectionStatus(String v) { connectionStatus.set(v != null ? v : ""); }
    public void setSamplingActive(boolean v) { samplingActive.set(v); }
    public void setGasPercent(double v) { gasPercent.set(v); }
    public void setGasValue(double v) { gasPercent.set(v); }
    public void setAirPercent(double v) { airPercent.set(v); }
    public void setAirValue(double v) { airPercent.set(v); }
    public void setDrumPercent(double v) { drumPercent.set(v); }
    public void setDrumValue(double v) { drumPercent.set(v); }
    public void setSmartPredictionsEnabled(boolean v) { smartPredictionsEnabled.set(v); }
    public void setBbtActive(boolean v) { bbtActive.set(v); }
    public void setBbpElapsedSeconds(long v) { bbpElapsedSeconds.set(v); }
    public void setReplayMode(boolean v) { replayMode.set(v); }

    /** Replace events list with current snapshot from session. */
    public void syncEvents(List<EventEntry> list) {
        syncEvents(list, null);
    }

    /** Replace events list with current snapshot; timex used to convert timeIndex to timeSeconds. */
    public void syncEvents(List<EventEntry> list, List<Double> timex) {
        events.clear();
        roastEvents.clear();
        if (list == null) return;
        events.addAll(list);
        for (EventEntry e : list) {
            Double t = timex != null && e.getTimeIndex() >= 0 && e.getTimeIndex() < timex.size()
                ? timex.get(e.getTimeIndex()) : null;
            long timeSec = t != null ? (long) t.doubleValue() : e.getTimeIndex();
            String type = eventTypeAbbrev(e.getType());
            String note = e.getLabel() != null ? e.getLabel() : "";
            roastEvents.add(new RoastEvent(timeSec, type, note));
        }
    }
}
