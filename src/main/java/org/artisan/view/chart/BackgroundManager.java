package org.artisan.view.chart;

import io.fair_acc.dataset.spi.DoubleDataSet;
import org.artisan.controller.BackgroundSettings;
import org.artisan.controller.DisplaySettings;
import org.artisan.model.*;
import org.artisan.util.CurveSmoothing;

import java.util.List;

/**
 * Manages background profile datasets and their styles. Extracts the background
 * chart logic from the old monolithic RoastChartController. Handles loading bg data
 * into datasets, applying dashed styles, and managing bg event markers.
 */
public final class BackgroundManager {

    private static final int DEFAULT_ROR_SMOOTHING = 5;

    private final ChartFactory chartFactory;
    private final RorCalculator rorCalculator = new RorCalculator();

    private BackgroundSettings backgroundSettings;
    private BackgroundProfile backgroundProfile;
    private DisplaySettings displaySettings;
    private ColorConfig colorConfig;

    public BackgroundManager(ChartFactory chartFactory) {
        this.chartFactory = chartFactory;
    }

    public void setBackgroundSettings(BackgroundSettings bs)  { this.backgroundSettings = bs; }
    public void setBackgroundProfile(BackgroundProfile bp)    { this.backgroundProfile = bp; }
    public void setDisplaySettings(DisplaySettings ds)        { this.displaySettings = ds; }
    public void setColorConfig(ColorConfig cfg)               { this.colorConfig = cfg; }

    public BackgroundSettings getBackgroundSettings()         { return backgroundSettings; }
    public BackgroundProfile getBackgroundProfile()           { return backgroundProfile; }

    public boolean isEnabled() {
        if (backgroundSettings != null && !backgroundSettings.isEnabled()) return false;
        return backgroundProfile != null && backgroundProfile.isVisible() && !backgroundProfile.isEmpty();
    }

    public boolean shouldShowBgET() {
        if (!isEnabled()) return false;
        boolean setting = backgroundSettings == null || backgroundSettings.isShowBgET();
        boolean visible = displaySettings == null || displaySettings.isVisibleET();
        return setting && visible;
    }

    public boolean shouldShowBgBT() {
        if (!isEnabled()) return false;
        boolean setting = backgroundSettings == null || backgroundSettings.isShowBgBT();
        boolean visible = displaySettings == null || displaySettings.isVisibleBT();
        return setting && visible;
    }

    public boolean shouldShowBgDeltaET() {
        if (!isEnabled()) return false;
        boolean setting = backgroundSettings != null && backgroundSettings.isShowBgDeltaET();
        boolean visible = displaySettings == null || displaySettings.isVisibleDeltaET();
        return setting && visible;
    }

    public boolean shouldShowBgDeltaBT() {
        if (!isEnabled()) return false;
        boolean setting = backgroundSettings != null && backgroundSettings.isShowBgDeltaBT();
        boolean visible = displaySettings == null || displaySettings.isVisibleDeltaBT();
        return setting && visible;
    }

    /**
     * Synchronizes background datasets: adds/removes them from chart renderers
     * and updates legend labels.
     */
    public void syncDatasetsInChart() {
        chartFactory.removeBackgroundDatasets();
        if (!isEnabled()) return;
        chartFactory.addBackgroundDatasets(
                shouldShowBgET(), shouldShowBgBT(),
                shouldShowBgDeltaET(), shouldShowBgDeltaBT());

        String title = backgroundProfile != null ? backgroundProfile.getTitle() : "";
        String suffix = (title != null && !title.isBlank()) ? " (" + title + ")" : "";
        chartFactory.getDataBgET().setName("BG ET" + suffix);
        chartFactory.getDataBgBT().setName("BG BT" + suffix);
        chartFactory.getDataBgDeltaET().setName("BG ΔET" + suffix);
        chartFactory.getDataBgDeltaBT().setName("BG ΔBT" + suffix);
    }

    /**
     * Applies dashed background styles to bg datasets.
     */
    public void applyStyles() {
        if (!isEnabled() || colorConfig == null) return;
        double alpha = displaySettings != null ? displaySettings.getBackgroundAlpha() : 0.2;
        alpha = Math.max(0, Math.min(1, alpha));
        int wBT = displaySettings != null ? displaySettings.getLineWidthBT() : 2;
        int wET = displaySettings != null ? displaySettings.getLineWidthET() : 2;
        int wDeltaBT = displaySettings != null ? displaySettings.getLineWidthDeltaBT() : 1;
        int wDeltaET = displaySettings != null ? displaySettings.getLineWidthDeltaET() : 1;

        chartFactory.getDataBgET().setStyle(
                ChartFactory.styleForDashed(shouldShowBgET(), colorConfig.getPaletteColor("backgroundmetcolor"), alpha, wET));
        chartFactory.getDataBgBT().setStyle(
                ChartFactory.styleForDashed(shouldShowBgBT(), colorConfig.getPaletteColor("backgroundbtcolor"), alpha, wBT));
        chartFactory.getDataBgDeltaET().setStyle(
                ChartFactory.styleForDashed(shouldShowBgDeltaET(), colorConfig.getPaletteColor("backgrounddeltaetcolor"), alpha, wDeltaET));
        chartFactory.getDataBgDeltaBT().setStyle(
                ChartFactory.styleForDashed(shouldShowBgDeltaBT(), colorConfig.getPaletteColor("backgrounddeltabtcolor"), alpha, wDeltaBT));
    }

    /**
     * Updates background datasets from the background profile data.
     */
    public void updateData() {
        DoubleDataSet bgET = chartFactory.getDataBgET();
        DoubleDataSet bgBT = chartFactory.getDataBgBT();
        DoubleDataSet bgDeltaET = chartFactory.getDataBgDeltaET();
        DoubleDataSet bgDeltaBT = chartFactory.getDataBgDeltaBT();

        if (!isEnabled() || backgroundProfile.getProfileData() == null) {
            bgET.clearData(); bgBT.clearData(); bgDeltaET.clearData(); bgDeltaBT.clearData();
            return;
        }
        ProfileData pd = backgroundProfile.getProfileData();
        List<Double> timex = pd.getTimex();
        List<Double> etRaw = pd.getTemp1();
        List<Double> btRaw = pd.getTemp2();
        if (timex == null || etRaw == null || btRaw == null) {
            bgET.clearData(); bgBT.clearData(); bgDeltaET.clearData(); bgDeltaBT.clearData();
            return;
        }
        int n = Math.min(timex.size(), Math.min(etRaw.size(), btRaw.size()));
        if (n <= 0) {
            bgET.clearData(); bgBT.clearData(); bgDeltaET.clearData(); bgDeltaBT.clearData();
            return;
        }

        List<Double> etList = etRaw, btList = btRaw;
        if (displaySettings != null) {
            int smoothET = displaySettings.getSmoothingET();
            int smoothBT = displaySettings.getSmoothingBT();
            if (smoothET > 1) etList = CurveSmoothing.smooth(etRaw, smoothET);
            if (smoothBT > 1) btList = CurveSmoothing.smooth(btRaw, smoothBT);
        }

        double offset = backgroundProfile.getAlignOffset();
        double[] x = new double[n];
        double[] et = new double[n];
        double[] bt = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = timex.get(i) + offset;
            et[i] = i < etList.size() ? etList.get(i) : 0.0;
            bt[i] = i < btList.size() ? btList.get(i) : 0.0;
        }
        bgET.set(x, et);
        bgBT.set(x, bt);

        int smoothDelta = displaySettings != null ? displaySettings.getSmoothingDelta() : DEFAULT_ROR_SMOOTHING;
        var dEt = rorCalculator.computeRoRSmoothed(timex, etRaw, smoothDelta);
        var dBt = rorCalculator.computeRoRSmoothed(timex, btRaw, smoothDelta);
        RorCalculator.clampRoR(dEt, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
        RorCalculator.clampRoR(dBt, RorCalculator.DEFAULT_MIN_ROR, RorCalculator.DEFAULT_MAX_ROR);
        double[] det = new double[n];
        double[] dbt = new double[n];
        for (int i = 0; i < n; i++) {
            det[i] = i < dEt.size() ? dEt.get(i) : 0.0;
            dbt[i] = i < dBt.size() ? dBt.get(i) : 0.0;
        }
        bgDeltaET.set(x, det);
        bgDeltaBT.set(x, dbt);
    }

    public void clearData() {
        chartFactory.getDataBgET().clearData();
        chartFactory.getDataBgBT().clearData();
        chartFactory.getDataBgDeltaET().clearData();
        chartFactory.getDataBgDeltaBT().clearData();
    }
}
