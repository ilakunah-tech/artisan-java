package org.artisan.controller;

/**
 * Cropster RI5-style pre-start → auto-CHARGE state machine.
 *
 * Transitions:
 *   IDLE ──[onStartPressed]──► PRE_ROAST ──[doCharge]──► ROASTING
 *                                 │
 *                         [onStartPressed again] (manual CHARGE)
 *                         [BT drop >= threshold, sustained] (auto CHARGE)
 */
public final class RoastStateMachine {

    public enum State { IDLE, PRE_ROAST, ROASTING }

    // ── Config (all mutable) ───────────────────────────────────────
    private double autoChargeTempDropDeg    = 5.0;
    private double autoChargeDropSustainSec = 20.0;
    private double preRoastTimeoutSec       = 300.0;

    // ── Live state ─────────────────────────────────────────────────
    private State   state                 = State.IDLE;
    private double  wallClockPreRoastStart;
    private double  preRoastFirstTimeSec  = Double.NaN;
    private double  maxBtSincePreStart    = Double.NEGATIVE_INFINITY;
    private double  dropCandidateStartSec = Double.NaN;
    private boolean dropCandidateActive   = false;
    private boolean timeoutFired          = false;

    // ── Callbacks ──────────────────────────────────────────────────
    private Runnable onAutoCharge;
    private Runnable onPreRoastTimeout;

    public void setOnAutoCharge(Runnable r)      { this.onAutoCharge      = r; }
    public void setOnPreRoastTimeout(Runnable r) { this.onPreRoastTimeout = r; }

    // ── API ────────────────────────────────────────────────────────

    public synchronized void onStartPressed() {
        if (state == State.IDLE) {
            state = State.PRE_ROAST;
            wallClockPreRoastStart = nowSec();
            maxBtSincePreStart     = Double.NEGATIVE_INFINITY;
            dropCandidateActive    = false;
            timeoutFired           = false;
            preRoastFirstTimeSec   = Double.NaN;
        } else if (state == State.PRE_ROAST) {
            doCharge(); // second press = manual CHARGE
        }
    }

    public synchronized void onSample(double timeSec, double bt) {
        if (state != State.PRE_ROAST) return;
        if (!Double.isFinite(preRoastFirstTimeSec)) preRoastFirstTimeSec = timeSec;

        if (bt > maxBtSincePreStart) {
            maxBtSincePreStart  = bt;
            dropCandidateActive = false; // new peak — reset candidate
        }

        double drop = maxBtSincePreStart - bt;
        if (drop >= autoChargeTempDropDeg) {
            if (!dropCandidateActive) {
                dropCandidateActive   = true;
                dropCandidateStartSec = timeSec;
            } else if (timeSec - dropCandidateStartSec >= autoChargeDropSustainSec) {
                doCharge();
                return;
            }
        } else if (drop < autoChargeTempDropDeg * 0.5) {
            dropCandidateActive = false; // false alarm — temp recovered
        }

        if (!timeoutFired && (nowSec() - wallClockPreRoastStart) >= preRoastTimeoutSec) {
            timeoutFired = true;
            if (onPreRoastTimeout != null) onPreRoastTimeout.run();
        }
    }

    public synchronized void onDrop()  { state = State.IDLE; }

    public synchronized void reset() {
        state               = State.IDLE;
        maxBtSincePreStart  = Double.NEGATIVE_INFINITY;
        dropCandidateActive = false;
        timeoutFired        = false;
        preRoastFirstTimeSec = Double.NaN;
    }

    public synchronized State  getState()                { return state; }
    public synchronized double getPreRoastFirstTimeSec() { return preRoastFirstTimeSec; }

    // config
    public double getAutoChargeTempDropDeg()            { return autoChargeTempDropDeg; }
    public void   setAutoChargeTempDropDeg(double v)    { autoChargeTempDropDeg = v; }
    public double getAutoChargeDropSustainSec()         { return autoChargeDropSustainSec; }
    public void   setAutoChargeDropSustainSec(double v) { autoChargeDropSustainSec = v; }
    public double getPreRoastTimeoutSec()               { return preRoastTimeoutSec; }
    public void   setPreRoastTimeoutSec(double v)       { preRoastTimeoutSec = v; }

    private void doCharge() {
        state = State.ROASTING;
        dropCandidateActive = false;
        if (onAutoCharge != null) onAutoCharge.run();
    }

    private static double nowSec() { return System.currentTimeMillis() / 1000.0; }
}
