package org.artisan.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.artisan.util.IirFilterDesign;
import org.artisan.util.LiveSosFilter;

/**
 * PID controller (ported from Python artisanlib.pid).
 * Supports P/I/D terms, derivative-on-measurement, integral windup prevention,
 * output/derivative filtering, and gain scheduling.
 */
public class PID {

    private final ReentrantLock lock = new ReentrantLock();

    private int outMin = 0;
    private int outMax = 100;
    private int dutySteps = 1;
    private int dutyMin = 0;
    private int dutyMax = 100;
    private Consumer<Double> control = v -> {};

    private double kp = 2.0;
    private double ki = 0.03;
    private double kd = 0.0;
    private double beta = 1.0;
    private double gamma = 1.0;
    private double samplingRate = 1.0;

    private boolean gainScheduling;
    private boolean gainSchedulingOnSV = true;
    private boolean gainSchedulingQuadratic;
    private double kp1 = 2.0, ki1 = 0.03, kd1 = 0.0;
    private double kp2 = 2.0, ki2 = 0.03, kd2 = 0.0;
    private double schedule0, schedule1, schedule2;

    private double pterm, iterm, dterm;
    private double errSum = 0.0;
    private Double lastError;
    private Double lastInput;
    private Double lastOutput;
    private Double lastTime;
    private double target = 0.0;
    private boolean active;

    private int outputFilterLevel = 0;
    private LiveSosFilter outputFilter;
    private int derivativeFilterLevel = 0;
    private LiveSosFilter derivativeFilter;

    private int forceDuty = 3;
    private int iterationsSinceDuty = 0;

    private double lastTarget = 0.0;
    private double derivativeLimit = 100.0;
    private final List<Double> measurementHistory = new ArrayList<>();
    private static final int MAX_LEN_MEASUREMENT_HISTORY = 5;
    private boolean setpointChangedSignificantly;
    private double significantSetupChangeLimit = 15.0;

    private boolean integralWindupPrevention = true;
    private double integralLimitFactor = 1.0;
    private double setpointChangeThreshold = 25.0;
    private boolean integralResetOnSetpointChange;
    private double backCalculationFactor = 0.5;
    private boolean integralJustReset = true;

    public PID() {
        this(v -> {});
    }

    public PID(Consumer<Double> control) {
        this(control, 2.0, 0.03, 0.0, 1.0, 1.0, 1.0);
    }

    public PID(Consumer<Double> control, double p, double i, double d,
               double beta, double gamma, double samplingRate) {
        this.control = control != null ? control : v -> {};
        this.kp = p;
        this.ki = i;
        this.kd = d;
        this.beta = Math.max(0, beta);
        this.gamma = Math.max(0, gamma);
        this.samplingRate = samplingRate > 0 ? samplingRate : 1.0;
        this.outputFilter = outputFilter(this.samplingRate);
        this.derivativeFilter = derivativeFilter(this.samplingRate);
    }

    private double smoothOutput(double output) {
        if (outputFilterLevel > 0) {
            return outputFilter.process(output);
        }
        return output;
    }

    private boolean detectMeasurementDiscontinuity(double currentInput) {
        if (measurementHistory.size() < 2) {
            return false;
        }
        List<Double> recent = measurementHistory;
        double sum = 0;
        int count = 0;
        for (int i = 1; i < Math.min(recent.size(), MAX_LEN_MEASUREMENT_HISTORY - 1); i++) {
            sum += Math.abs(recent.get(recent.size() - i) - recent.get(recent.size() - i - 1));
            count++;
        }
        if (count == 0) {
            return false;
        }
        double avgRecentChange = sum / count;
        double currentChange = Math.abs(currentInput - recent.get(recent.size() - 1));
        return currentChange > 2.5 * avgRecentChange && currentChange > 1.0;
    }

    private double calculateDerivative(double currentInput, double dt) {
        if (lastInput == null) {
            return 0.0;
        }
        double error = gamma * target - currentInput;
        double lastErrorVal = gamma * lastTarget - lastInput;
        double derror = (error - lastErrorVal) / dt;
        if (derivativeFilterLevel > 0) {
            derror = derivativeFilter.process(derror);
        }
        if (Math.abs(derror) > derivativeLimit) {
            derror = derror > 0 ? derivativeLimit : -derivativeLimit;
        }
        if (setpointChangedSignificantly) {
            derror *= 1.0 - Math.max(0, Math.min(1, gamma)) / 2;
        }
        if (detectMeasurementDiscontinuity(currentInput)) {
            derror *= 0.3;
        }
        return getKd(currentInput) * derror;
    }

    private void updateMeasurementHistory(double currentInput) {
        measurementHistory.add(currentInput);
        while (measurementHistory.size() > MAX_LEN_MEASUREMENT_HISTORY) {
            measurementHistory.remove(0);
        }
    }

    private boolean shouldIntegrate(double error, double outputBeforeClamp) {
        if (!integralWindupPrevention) {
            return true;
        }
        if (integralJustReset) {
            return false;
        }
        return !(outputBeforeClamp > outMax && error > 0)
                && !(outputBeforeClamp < outMin && error < 0);
    }

    private void handleSetpointChangeIntegral(double setpointChange) {
        if (!integralResetOnSetpointChange) {
            return;
        }
        if (Math.abs(setpointChange) > setpointChangeThreshold) {
            iterm = 0.0;
            integralJustReset = true;
        } else if (Math.abs(setpointChange) > setpointChangeThreshold * 0.5) {
            iterm *= 0.5;
        }
    }

    private void backCalculateIntegral(double pv, double outputBeforeClamp, double outputAfterClamp) {
        if (!integralWindupPrevention) {
            return;
        }
        if (Math.abs(outputBeforeClamp - outputAfterClamp) > 0.001) {
            double excess = outputBeforeClamp - outputAfterClamp;
            if (getKi(pv) != 0) {
                double integralAdjustment = excess * backCalculationFactor;
                iterm -= integralAdjustment;
                iterm = applyIntegralLimits(pv, iterm);
            }
        }
    }

    /** Integral limits from output range and factor (symmetric/positive/negative). */
    private static double[] calculateIntegralLimits(int outMin, int outMax, double integralLimitFactor) {
        double outputRange = outMax - outMin;
        double integralRange = outputRange * integralLimitFactor;
        double integralMin;
        double integralMax;
        if (outMin >= 0) {
            integralMin = 0.0;
            integralMax = integralRange;
        } else if (outMax <= 0) {
            integralMin = -integralRange;
            integralMax = 0.0;
        } else {
            integralMax = integralRange / 2;
            integralMin = -integralMax;
        }
        return new double[] { integralMin, integralMax };
    }

    public double applyIntegralLimits(double pv, double itermVal) {
        double[] lim = calculateIntegralLimits(outMin, outMax, integralLimitFactor);
        double integralMin = lim[0];
        double integralMax = lim[1];
        integralMax += getKp(pv) * (1 - beta) * target;
        return Math.max(integralMin, Math.min(integralMax, itermVal));
    }

    private double getParameter(double pv, double y1, double y2, double y3) {
        if (!gainScheduling) {
            return y1;
        }
        double x = gainSchedulingOnSV ? target : pv;
        try {
            if (gainSchedulingQuadratic) {
                double[] coeffs = quadraticFit(schedule0, schedule1, schedule2, y1, y2, y3);
                if (coeffs != null) {
                    double mapped = coeffs[0] * x * x + coeffs[1] * x + coeffs[2];
                    double maxP = Math.max(y1, Math.max(y2, y3));
                    double minP = Math.min(y1, Math.min(y2, y3));
                    return Math.max(minP, Math.min(maxP, mapped));
                }
            } else {
                double[] coeffs = linearFit(schedule0, schedule1, y1, y2);
                if (coeffs != null) {
                    double mapped = coeffs[0] * x + coeffs[1];
                    double maxP = Math.max(y1, y2);
                    double minP = Math.min(y1, y2);
                    return Math.max(minP, Math.min(maxP, mapped));
                }
            }
        } catch (Exception ignored) {
            // fallback to y1
        }
        return y1;
    }

    private static double[] linearFit(double x1, double x2, double y1, double y2) {
        if (Math.abs(x2 - x1) < 1e-15) {
            return null;
        }
        double m = (y2 - y1) / (x2 - x1);
        double c = y1 - m * x1;
        return new double[] { m, c };
    }

    private static double[] quadraticFit(double x1, double x2, double x3, double y1, double y2, double y3) {
        // Solve for a,b,c in y = a*x^2 + b*x + c
        double d21 = y2 - y1, d31 = y3 - y1;
        double x21 = x2 - x1, x31 = x3 - x1;
        double x21s = x2 * x2 - x1 * x1, x31s = x3 * x3 - x1 * x1;
        double det = x21s * x31 - x31s * x21;
        if (Math.abs(det) < 1e-15) {
            return null;
        }
        double a = (d21 * x31 - d31 * x21) / det;
        double b = (x21s * d31 - x31s * d21) / det;
        double c = y1 - a * x1 * x1 - b * x1;
        return new double[] { a, b, c };
    }

    public double getKp(double pv) {
        return getParameter(pv, kp, kp1, kp2);
    }

    public double getKi(double pv) {
        return getParameter(pv, ki, ki1, ki2);
    }

    public double getKd(double pv) {
        return getParameter(pv, kd, kd1, kd2);
    }

    public void on() {
        lock.lock();
        try {
            lastOutput = null;
            active = true;
        } finally {
            lock.unlock();
        }
    }

    public void off() {
        lock.lock();
        try {
            active = false;
        } finally {
            lock.unlock();
        }
    }

    public boolean isActive() {
        lock.lock();
        try {
            return active;
        } catch (Exception e) {
            return false;
        } finally {
            lock.unlock();
        }
    }

    /** Update with process value (PV). Rejects null and -1. */
    public void update(Double i) {
        Consumer<Double> controlFunc = null;
        Double outputValue = null;
        if (i != null && !isActive()) {
            target = i;
        }
        if (i == null || i == -1) {
            return;
        }
        lock.lock();
        try {
            double now = System.currentTimeMillis() * 0.001;
            double err = target - i;
            if (lastError == null || lastTime == null) {
                lastTime = now;
                lastError = err;
                lastInput = i;
                return;
            }
            double dt = now - lastTime;
            if (dt < 0.05) {
                return;
            }

            double setpointChange = target - lastTarget;
            if (Math.abs(setpointChange) > 0.001) {
                setpointChangedSignificantly =
                        Math.abs(setpointChange) > significantSetupChangeLimit && significantSetupChangeLimit > 0;
                handleSetpointChangeIntegral(setpointChange);
                lastTarget = target;
            } else {
                setpointChangedSignificantly = false;
            }

            pterm = getKp(i) * (beta * target - i);
            double outputBeforeIntegration = pterm + iterm;
            if (shouldIntegrate(err, outputBeforeIntegration)) {
                iterm += getKi(i) * err * dt;
                iterm = applyIntegralLimits(i, iterm);
            }
            integralJustReset = false;

            dterm = calculateDerivative(i, dt);
            updateMeasurementHistory(i);

            lastTime = now;
            lastError = err;
            lastInput = i;
            double output = pterm + iterm + dterm;
            output = smoothOutput(output);

            double outputBeforeClamp = output;
            if (output > outMax) {
                output = outMax;
            } else if (output < outMin) {
                output = outMin;
            }
            backCalculateIntegral(i, outputBeforeClamp, output);

            double finalOutput = Math.min((double) dutyMax, Math.max((double) dutyMin, output));
            if (lastOutput == null
                    || iterationsSinceDuty >= forceDuty
                    || finalOutput >= lastOutput + dutySteps
                    || finalOutput <= lastOutput - dutySteps) {
                if (active) {
                    controlFunc = control;
                    outputValue = finalOutput;
                }
                lastOutput = output;
            }
            iterationsSinceDuty++;
        } finally {
            lock.unlock();
        }
        if (controlFunc != null && outputValue != null) {
            try {
                controlFunc.accept(outputValue);
                iterationsSinceDuty = 0;
            } catch (Exception e) {
                // log and continue
            }
        }
    }

    public void reset() {
        init(true, samplingRate);
    }

    public void init(boolean doLock, double samplingRateArg) {
        if (doLock) {
            lock.lock();
        }
        try {
            derivativeFilter = derivativeFilter(samplingRateArg);
            outputFilter = outputFilter(this.samplingRate);
            errSum = 0.0;
            lastError = 0.0;
            lastInput = null;
            lastTime = null;
            pterm = 0.0;
            iterm = 0.0;
            lastOutput = null;
            lastTarget = target;
            measurementHistory.clear();
            setpointChangedSignificantly = false;
            integralJustReset = false;
        } finally {
            if (doLock) {
                lock.unlock();
            }
        }
    }

    public void setTarget(double targetVal, boolean doInit) {
        lock.lock();
        try {
            target = targetVal;
            if (doInit) {
                init(false, samplingRate);
            }
        } finally {
            lock.unlock();
        }
    }

    public double getTarget() {
        lock.lock();
        try {
            return target;
        } finally {
            lock.unlock();
        }
    }

    public void setPID(double p, double i, double d) {
        lock.lock();
        try {
            kp = Math.max(p, 0);
            ki = Math.max(i, 0);
            kd = Math.max(d, 0);
        } finally {
            lock.unlock();
        }
    }

    public void setWeights(Double betaVal, Double gammaVal) {
        lock.lock();
        try {
            if (betaVal != null) {
                beta = Math.max(betaVal, 0);
            }
            if (gammaVal != null) {
                gamma = Math.max(gammaVal, 0);
            }
        } finally {
            lock.unlock();
        }
    }

    public void setLimits(int minOut, int maxOut) {
        lock.lock();
        try {
            outMin = minOut;
            outMax = maxOut;
        } finally {
            lock.unlock();
        }
    }

    public void setDutySteps(int steps) {
        lock.lock();
        try {
            dutySteps = steps;
        } finally {
            lock.unlock();
        }
    }

    public void setDutyMin(int m) {
        lock.lock();
        try {
            dutyMin = m;
        } finally {
            lock.unlock();
        }
    }

    public void setDutyMax(int m) {
        lock.lock();
        try {
            dutyMax = m;
        } finally {
            lock.unlock();
        }
    }

    public void setControl(Consumer<Double> f) {
        lock.lock();
        try {
            control = f != null ? f : v -> {};
        } finally {
            lock.unlock();
        }
    }

    public Double getDuty() {
        lock.lock();
        try {
            if (lastOutput == null) {
                return null;
            }
            return Math.min((double) dutyMax, Math.max((double) dutyMin, lastOutput));
        } finally {
            lock.unlock();
        }
    }

    public double getPterm() {
        lock.lock();
        try {
            return pterm;
        } finally {
            lock.unlock();
        }
    }

    public double getIterm() {
        lock.lock();
        try {
            return iterm;
        } finally {
            lock.unlock();
        }
    }

    public double getDterm() {
        lock.lock();
        try {
            return dterm;
        } finally {
            lock.unlock();
        }
    }

    public double getError() {
        lock.lock();
        try {
            return lastError != null ? lastError : 0.0;
        } finally {
            lock.unlock();
        }
    }

    private static LiveSosFilter iirFilter(double samplingRate, double wn) {
        double[][] sos = IirFilterDesign.butter1LowpassSos(samplingRate, wn);
        return new LiveSosFilter(sos);
    }

    private static LiveSosFilter derivativeFilter(double samplingRate) {
        return iirFilter(samplingRate, 0.1);
    }

    private static LiveSosFilter outputFilter(double samplingRate) {
        return iirFilter(samplingRate, 0.35);
    }

    public void setOutputFilterLevel(int v, boolean reset) {
        lock.lock();
        try {
            outputFilterLevel = v;
            if (reset) {
                outputFilter = outputFilter(samplingRate);
            }
        } finally {
            lock.unlock();
        }
    }

    public void setDerivativeFilterLevel(int v, boolean reset) {
        lock.lock();
        try {
            derivativeFilterLevel = v;
            if (reset) {
                derivativeFilter = derivativeFilter(samplingRate);
            }
        } finally {
            lock.unlock();
        }
    }

    public void setDerivativeLimit(double limit) {
        lock.lock();
        try {
            derivativeLimit = Math.max(0.0, limit);
        } finally {
            lock.unlock();
        }
    }

    public double getDerivativeLimit() {
        lock.lock();
        try {
            return derivativeLimit;
        } finally {
            lock.unlock();
        }
    }

    public void setIntegralWindupPrevention(boolean enabled) {
        lock.lock();
        try {
            integralWindupPrevention = enabled;
        } finally {
            lock.unlock();
        }
    }

    public boolean getIntegralWindupPrevention() {
        lock.lock();
        try {
            return integralWindupPrevention;
        } finally {
            lock.unlock();
        }
    }

    public void setIntegralResetOnSP(boolean enabled) {
        lock.lock();
        try {
            integralResetOnSetpointChange = enabled;
        } finally {
            lock.unlock();
        }
    }

    public boolean getIntegralResetOnSP() {
        lock.lock();
        try {
            return integralResetOnSetpointChange;
        } finally {
            lock.unlock();
        }
    }

    public void setIntegralLimitFactor(double factor) {
        lock.lock();
        try {
            integralLimitFactor = Math.max(0.0, Math.min(1.0, factor));
        } finally {
            lock.unlock();
        }
    }

    public double getIntegralLimitFactor() {
        lock.lock();
        try {
            return integralLimitFactor;
        } finally {
            lock.unlock();
        }
    }

    public void setSetpointChangeThreshold(double threshold) {
        lock.lock();
        try {
            setpointChangeThreshold = Math.max(0.0, threshold);
        } finally {
            lock.unlock();
        }
    }

    public double getSetpointChangeThreshold() {
        lock.lock();
        try {
            return setpointChangeThreshold;
        } finally {
            lock.unlock();
        }
    }

    public void setBackCalculationFactor(double factor) {
        lock.lock();
        try {
            backCalculationFactor = Math.max(0.0, Math.min(1.0, factor));
        } finally {
            lock.unlock();
        }
    }

    public void setSamplingRate(double rate) {
        lock.lock();
        try {
            if (rate > 0) {
                samplingRate = rate;
            }
        } finally {
            lock.unlock();
        }
    }

    public void setGainScheduleState(boolean state) {
        lock.lock();
        try {
            gainScheduling = state;
        } finally {
            lock.unlock();
        }
    }

    public void setGainScheduleOnSV(boolean state) {
        lock.lock();
        try {
            gainSchedulingOnSV = state;
        } finally {
            lock.unlock();
        }
    }

    public void setGainScheduleQuadratic(boolean state) {
        lock.lock();
        try {
            gainSchedulingQuadratic = state;
        } finally {
            lock.unlock();
        }
    }

    public void setGainSchedule(double kp1v, double ki1v, double kd1v, double kp2v, double ki2v, double kd2v,
                               double s0, double s1, double s2) {
        lock.lock();
        try {
            kp1 = kp1v;
            ki1 = ki1v;
            kd1 = kd1v;
            kp2 = kp2v;
            ki2 = ki2v;
            kd2 = kd2v;
            schedule0 = s0;
            schedule1 = s1;
            schedule2 = s2;
        } finally {
            lock.unlock();
        }
    }

    // Package-private for tests (mirror Python behaviour)
    boolean shouldIntegrateForTest(double error, double outputBeforeClamp) {
        return shouldIntegrate(error, outputBeforeClamp);
    }

    void handleSetpointChangeIntegralForTest(double setpointChange) {
        handleSetpointChangeIntegral(setpointChange);
    }

    void backCalculateIntegralForTest(double pv, double outputBeforeClamp, double outputAfterClamp) {
        backCalculateIntegral(pv, outputBeforeClamp, outputAfterClamp);
    }

    double calculateDerivativeForTest(double currentInput, double dt) {
        return calculateDerivative(currentInput, dt);
    }

    boolean detectMeasurementDiscontinuityForTest(double currentInput) {
        return detectMeasurementDiscontinuity(currentInput);
    }

    void updateMeasurementHistoryForTest(double currentInput) {
        updateMeasurementHistory(currentInput);
    }

    /** Returns last computed output (before duty min/max clamp) for tests. */
    Double getLastOutputInternal() {
        return lastOutput;
    }

    /** Allows tests to drive time: call updateWithTime(pv, timeSeconds). */
    public void updateWithTime(double pv, double nowSeconds) {
        if (!isActive()) {
            target = pv;
        }
        lock.lock();
        try {
            double err = target - pv;
            if (lastError == null || lastTime == null) {
                lastTime = nowSeconds;
                lastError = err;
                lastInput = pv;
                return;
            }
            double dt = nowSeconds - lastTime;
            if (dt < 0.05) {
                return;
            }
            double setpointChange = target - lastTarget;
            if (Math.abs(setpointChange) > 0.001) {
                setpointChangedSignificantly =
                        Math.abs(setpointChange) > significantSetupChangeLimit && significantSetupChangeLimit > 0;
                handleSetpointChangeIntegral(setpointChange);
                lastTarget = target;
            } else {
                setpointChangedSignificantly = false;
            }
            pterm = getKp(pv) * (beta * target - pv);
            double outputBeforeIntegration = pterm + iterm;
            if (shouldIntegrate(err, outputBeforeIntegration)) {
                iterm += getKi(pv) * err * dt;
                iterm = applyIntegralLimits(pv, iterm);
            }
            integralJustReset = false;
            dterm = calculateDerivative(pv, dt);
            updateMeasurementHistory(pv);
            lastTime = nowSeconds;
            lastError = err;
            lastInput = pv;
            double output = pterm + iterm + dterm;
            output = smoothOutput(output);
            double outputBeforeClamp = output;
            if (output > outMax) {
                output = outMax;
            } else if (output < outMin) {
                output = outMin;
            }
            backCalculateIntegral(pv, outputBeforeClamp, output);
            lastOutput = output;
            iterationsSinceDuty++;
        } finally {
            lock.unlock();
        }
    }
}
