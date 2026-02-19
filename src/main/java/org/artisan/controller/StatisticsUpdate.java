package org.artisan.controller;

import org.artisan.model.PhaseResult;
import org.artisan.model.RoastStats;

/**
 * DTO for statistics panel update: computed stats, phase, DTR, AUC, and optional AUC base temp for tooltip.
 */
public final class StatisticsUpdate {

    private final RoastStats stats;
    private final PhaseResult phase;
    private final double dtr;
    private final double auc;
    private final double aucBaseTempC;

    public StatisticsUpdate(RoastStats stats, PhaseResult phase, double dtr, double auc, double aucBaseTempC) {
        this.stats = stats;
        this.phase = phase;
        this.dtr = dtr;
        this.auc = auc;
        this.aucBaseTempC = aucBaseTempC;
    }

    public RoastStats getStats() { return stats; }
    public PhaseResult getPhase() { return phase; }
    public double getDtr() { return dtr; }
    public double getAuc() { return auc; }
    public double getAucBaseTempC() { return aucBaseTempC; }
}
