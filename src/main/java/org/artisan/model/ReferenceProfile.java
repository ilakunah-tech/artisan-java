package org.artisan.model;

import java.util.ArrayList;
import java.util.List;

public class ReferenceProfile {
    private String name;
    private double chargeTempC;
    private double tpTempC;
    private double dropTempC;
    private double tpTimeSec;
    private double dryEndTimeSec;
    private double fcStartTimeSec;
    private double fcEndTimeSec;
    private double dropTimeSec;
    private double dryPct;
    private double maillardPct;
    private double dtPct;
    private List<ModulationAction> modulationActions = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getChargeTempC() { return chargeTempC; }
    public void setChargeTempC(double chargeTempC) { this.chargeTempC = chargeTempC; }

    public double getTpTempC() { return tpTempC; }
    public void setTpTempC(double tpTempC) { this.tpTempC = tpTempC; }

    public double getDropTempC() { return dropTempC; }
    public void setDropTempC(double dropTempC) { this.dropTempC = dropTempC; }

    public double getTpTimeSec() { return tpTimeSec; }
    public void setTpTimeSec(double tpTimeSec) { this.tpTimeSec = tpTimeSec; }

    public double getDryEndTimeSec() { return dryEndTimeSec; }
    public void setDryEndTimeSec(double dryEndTimeSec) { this.dryEndTimeSec = dryEndTimeSec; }

    public double getFcStartTimeSec() { return fcStartTimeSec; }
    public void setFcStartTimeSec(double fcStartTimeSec) { this.fcStartTimeSec = fcStartTimeSec; }

    public double getFcEndTimeSec() { return fcEndTimeSec; }
    public void setFcEndTimeSec(double fcEndTimeSec) { this.fcEndTimeSec = fcEndTimeSec; }

    public double getDropTimeSec() { return dropTimeSec; }
    public void setDropTimeSec(double dropTimeSec) { this.dropTimeSec = dropTimeSec; }

    public double getDryPct() { return dryPct; }
    public void setDryPct(double dryPct) { this.dryPct = dryPct; }

    public double getMaillardPct() { return maillardPct; }
    public void setMaillardPct(double maillardPct) { this.maillardPct = maillardPct; }

    public double getDtPct() { return dtPct; }
    public void setDtPct(double dtPct) { this.dtPct = dtPct; }

    public List<ModulationAction> getModulationActions() { return modulationActions; }
    public void setModulationActions(List<ModulationAction> modulationActions) {
        this.modulationActions = modulationActions != null ? modulationActions : new ArrayList<>();
    }

    public static ReferenceProfile createTestProfile() {
        ReferenceProfile rp = new ReferenceProfile();
        rp.setName("Ethiopia Natural 200g");
        rp.setChargeTempC(185);
        rp.setTpTempC(93);
        rp.setDropTempC(212);
        rp.setTpTimeSec(135);
        rp.setDryEndTimeSec(297);
        rp.setFcStartTimeSec(420);
        rp.setFcEndTimeSec(465);
        rp.setDropTimeSec(540);
        rp.setDryPct(55);
        rp.setMaillardPct(25);
        rp.setDtPct(20);

        List<ModulationAction> actions = new ArrayList<>();
        actions.add(new ModulationAction(ModulationAction.ActionType.GAS,      0,    80, "Gas \u2192 80%",  "@ 0:00"));
        actions.add(new ModulationAction(ModulationAction.ActionType.CHARGE,   0,     0, "Charge",          "@ 0:00"));
        actions.add(new ModulationAction(ModulationAction.ActionType.AIR,      30,   30, "Air \u2192 30%",  "@ 0:30"));
        actions.add(new ModulationAction(ModulationAction.ActionType.GAS,     120,   70, "Gas \u2192 70%",  "@ 2:00"));
        actions.add(new ModulationAction(ModulationAction.ActionType.TP,      135,    0, "Turning Pt",      "@ 2:15"));
        actions.add(new ModulationAction(ModulationAction.ActionType.AIR,     180,   50, "Air \u2192 50%",  "@ 3:00"));
        actions.add(new ModulationAction(ModulationAction.ActionType.GAS,     240,   60, "Gas \u2192 60%",  "@ 4:00"));
        actions.add(new ModulationAction(ModulationAction.ActionType.DRY_END, 297,    0, "Dry End",         "@ 4:57"));
        actions.add(new ModulationAction(ModulationAction.ActionType.AIR,     360,   65, "Air \u2192 65%",  "@ 6:00"));
        actions.add(new ModulationAction(ModulationAction.ActionType.FC_START, 420,   0, "FC Start",        "@ 7:00"));
        actions.add(new ModulationAction(ModulationAction.ActionType.GAS,     450,   40, "Gas \u2192 40%",  "@ 7:30"));
        actions.add(new ModulationAction(ModulationAction.ActionType.DROP,    540,    0, "Drop",            "@ 9:00"));
        rp.setModulationActions(actions);
        return rp;
    }
}
