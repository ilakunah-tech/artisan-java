package org.artisan.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Computed profile information — full parity with Python ComputedProfileInformation TypedDict.
 * All fields optional (nullable).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputedProfileInformation {

    // ── Charge ──────────────────────────────────────────────────────────────
    @JsonProperty("CHARGE_ET") private Double chargeEt;
    @JsonProperty("CHARGE_BT") private Double chargeBt;

    // ── Turning Point ────────────────────────────────────────────────────────
    @JsonProperty("TP_idx")  private Integer tpIdx;
    @JsonProperty("TP_time") private Double  tpTime;
    @JsonProperty("TP_ET")   private Double  tpEt;
    @JsonProperty("TP_BT")   private Double  tpBt;

    // ── MET (maximum ET during roast) ────────────────────────────────────────
    @JsonProperty("MET") private Double met;

    // ── Dry End ──────────────────────────────────────────────────────────────
    @JsonProperty("DRY_time") private Double dryTime;
    @JsonProperty("DRY_ET")   private Double dryEt;
    @JsonProperty("DRY_BT")   private Double dryBt;

    // ── First Crack Start ────────────────────────────────────────────────────
    @JsonProperty("FCs_time") private Double fcsTime;
    @JsonProperty("FCs_ET")   private Double fcsEt;
    @JsonProperty("FCs_BT")   private Double fcsBt;

    // ── First Crack End ──────────────────────────────────────────────────────
    @JsonProperty("FCe_time") private Double fceTime;
    @JsonProperty("FCe_ET")   private Double fceEt;
    @JsonProperty("FCe_BT")   private Double fceBt;

    // ── Second Crack Start ───────────────────────────────────────────────────
    @JsonProperty("SCs_time") private Double scsTime;
    @JsonProperty("SCs_ET")   private Double scsEt;
    @JsonProperty("SCs_BT")   private Double scsBt;

    // ── Second Crack End ─────────────────────────────────────────────────────
    @JsonProperty("SCe_time") private Double sceTime;
    @JsonProperty("SCe_ET")   private Double sceEt;
    @JsonProperty("SCe_BT")   private Double sceBt;

    // ── Drop ─────────────────────────────────────────────────────────────────
    @JsonProperty("DROP_time") private Double dropTime;
    @JsonProperty("DROP_ET")   private Double dropEt;
    @JsonProperty("DROP_BT")   private Double dropBt;

    // ── Cool End ─────────────────────────────────────────────────────────────
    @JsonProperty("COOL_time") private Double coolTime;
    @JsonProperty("COOL_ET")   private Double coolEt;
    @JsonProperty("COOL_BT")   private Double coolBt;

    // ── Phase durations ──────────────────────────────────────────────────────
    @JsonProperty("totaltime")       private Double totalTime;
    @JsonProperty("dryphasetime")    private Double dryPhaseTime;
    @JsonProperty("midphasetime")    private Double midPhaseTime;
    @JsonProperty("finishphasetime") private Double finishPhaseTime;
    @JsonProperty("coolphasetime")   private Double coolPhaseTime;

    // ── Phase RoR ────────────────────────────────────────────────────────────
    @JsonProperty("dry_phase_ror")    private Double dryPhaseRor;
    @JsonProperty("mid_phase_ror")    private Double midPhaseRor;
    @JsonProperty("finish_phase_ror") private Double finishPhaseRor;
    @JsonProperty("total_ror")        private Double totalRor;
    @JsonProperty("fcs_ror")          private Double fcsRor;

    // ── Phase delta temps ────────────────────────────────────────────────────
    @JsonProperty("dry_phase_delta_temp")    private Double dryPhaseDeltaTemp;
    @JsonProperty("mid_phase_delta_temp")    private Double midPhaseDeltaTemp;
    @JsonProperty("finish_phase_delta_temp") private Double finishPhaseDeltaTemp;

    // ── Total samples ────────────────────────────────────────────────────────
    @JsonProperty("total_ts")    private Integer totalTs;
    @JsonProperty("total_ts_ET") private Integer totalTsEt;
    @JsonProperty("total_ts_BT") private Integer totalTsBt;

    // ── AUC ──────────────────────────────────────────────────────────────────
    @JsonProperty("AUC")              private Integer auc;
    @JsonProperty("AUCbegin")         private String  aucBegin;
    @JsonProperty("AUCbase")          private Double  aucBase;
    @JsonProperty("AUCfromeventflag") private Integer aucFromEventFlag;
    @JsonProperty("dry_phase_AUC")    private Integer dryPhaseAuc;
    @JsonProperty("mid_phase_AUC")    private Integer midPhaseAuc;
    @JsonProperty("finish_phase_AUC") private Integer finishPhaseAuc;

    // ── Weight / Volume ──────────────────────────────────────────────────────
    @JsonProperty("weight_loss")          private Double weightLoss;
    @JsonProperty("roast_defects_loss")   private Double roastDefectsLoss;
    @JsonProperty("total_loss")           private Double totalLoss;
    @JsonProperty("volume_gain")          private Double volumeGain;
    @JsonProperty("moisture_loss")        private Double moistureLoss;
    @JsonProperty("organic_loss")         private Double organicLoss;
    @JsonProperty("volumein")             private Double volumeIn;
    @JsonProperty("volumeout")            private Double volumeOut;
    @JsonProperty("weightin")             private Double weightIn;
    @JsonProperty("weightout")            private Double weightOut;
    @JsonProperty("roast_defects_weight") private Double roastDefectsWeight;
    @JsonProperty("total_yield")          private Double totalYield;

    // ── Density / Moisture ───────────────────────────────────────────────────
    @JsonProperty("green_density")    private Double greenDensity;
    @JsonProperty("roasted_density")  private Double roastedDensity;
    @JsonProperty("set_density")      private Double setDensity;
    @JsonProperty("moisture_greens")  private Double moistureGreens;
    @JsonProperty("moisture_roasted") private Double moistureRoasted;

    // ── Ambient conditions ───────────────────────────────────────────────────
    @JsonProperty("ambient_humidity")    private Double ambientHumidity;
    @JsonProperty("ambient_pressure")    private Double ambientPressure;
    @JsonProperty("ambient_temperature") private Double ambientTemperature;

    // ── Delta ET / BT ────────────────────────────────────────────────────────
    @JsonProperty("det") private Double det;
    @JsonProperty("dbt") private Double dbt;

    // ── Energy (BTU / CO2) ───────────────────────────────────────────────────
    @JsonProperty("BTU_preheat")             private Double btuPreheat;
    @JsonProperty("CO2_preheat")             private Double co2Preheat;
    @JsonProperty("BTU_bbp")                 private Double btuBbp;
    @JsonProperty("CO2_bbp")                 private Double co2Bbp;
    @JsonProperty("BTU_cooling")             private Double btuCooling;
    @JsonProperty("CO2_cooling")             private Double co2Cooling;
    @JsonProperty("BTU_LPG")                 private Double btuLpg;
    @JsonProperty("BTU_NG")                  private Double btuNg;
    @JsonProperty("BTU_ELEC")                private Double btuElec;
    @JsonProperty("BTU_batch")               private Double btuBatch;
    @JsonProperty("BTU_batch_per_green_kg")  private Double btuBatchPerGreenKg;
    @JsonProperty("BTU_roast")               private Double btuRoast;
    @JsonProperty("BTU_roast_per_green_kg")  private Double btuRoastPerGreenKg;
    @JsonProperty("CO2_batch")               private Double co2Batch;
    @JsonProperty("CO2_batch_per_green_kg")  private Double co2BatchPerGreenKg;
    @JsonProperty("CO2_roast")               private Double co2Roast;
    @JsonProperty("CO2_roast_per_green_kg")  private Double co2RoastPerGreenKg;
    @JsonProperty("KWH_batch_per_green_kg")  private Double kwhBatchPerGreenKg;
    @JsonProperty("KWH_roast_per_green_kg")  private Double kwhRoastPerGreenKg;

    // ── Between-batch preheat (BBP) ──────────────────────────────────────────
    @JsonProperty("bbp_total_time")              private Double bbpTotalTime;
    @JsonProperty("bbp_bottom_temp")             private Double bbpBottomTemp;
    @JsonProperty("bbp_begin_to_bottom_time")    private Double bbpBeginToBottomTime;
    @JsonProperty("bbp_bottom_to_charge_time")   private Double bbpBottomToChargeTime;
    @JsonProperty("bbp_begin_to_bottom_ror")     private Double bbpBeginToBottomRor;
    @JsonProperty("bbp_bottom_to_charge_ror")    private Double bbpBottomToChargeRor;

    public ComputedProfileInformation() {}

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Double getChargeEt() { return chargeEt; }
    public void setChargeEt(Double v) { this.chargeEt = v; }

    public Double getChargeBt() { return chargeBt; }
    public void setChargeBt(Double v) { this.chargeBt = v; }

    public Integer getTpIdx() { return tpIdx; }
    public void setTpIdx(Integer v) { this.tpIdx = v; }

    public Double getTpTime() { return tpTime; }
    public void setTpTime(Double v) { this.tpTime = v; }

    public Double getTpEt() { return tpEt; }
    public void setTpEt(Double v) { this.tpEt = v; }

    public Double getTpBt() { return tpBt; }
    public void setTpBt(Double v) { this.tpBt = v; }

    public Double getMet() { return met; }
    public void setMet(Double v) { this.met = v; }

    public Double getDryTime() { return dryTime; }
    public void setDryTime(Double v) { this.dryTime = v; }

    public Double getDryEt() { return dryEt; }
    public void setDryEt(Double v) { this.dryEt = v; }

    public Double getDryBt() { return dryBt; }
    public void setDryBt(Double v) { this.dryBt = v; }

    public Double getFcsTime() { return fcsTime; }
    public void setFcsTime(Double v) { this.fcsTime = v; }

    public Double getFcsEt() { return fcsEt; }
    public void setFcsEt(Double v) { this.fcsEt = v; }

    public Double getFcsBt() { return fcsBt; }
    public void setFcsBt(Double v) { this.fcsBt = v; }

    public Double getFceTime() { return fceTime; }
    public void setFceTime(Double v) { this.fceTime = v; }

    public Double getFceEt() { return fceEt; }
    public void setFceEt(Double v) { this.fceEt = v; }

    public Double getFceBt() { return fceBt; }
    public void setFceBt(Double v) { this.fceBt = v; }

    public Double getScsTime() { return scsTime; }
    public void setScsTime(Double v) { this.scsTime = v; }

    public Double getScsEt() { return scsEt; }
    public void setScsEt(Double v) { this.scsEt = v; }

    public Double getScsBt() { return scsBt; }
    public void setScsBt(Double v) { this.scsBt = v; }

    public Double getSceTime() { return sceTime; }
    public void setSceTime(Double v) { this.sceTime = v; }

    public Double getSceEt() { return sceEt; }
    public void setSceEt(Double v) { this.sceEt = v; }

    public Double getSceBt() { return sceBt; }
    public void setSceBt(Double v) { this.sceBt = v; }

    public Double getDropTime() { return dropTime; }
    public void setDropTime(Double v) { this.dropTime = v; }

    public Double getDropEt() { return dropEt; }
    public void setDropEt(Double v) { this.dropEt = v; }

    public Double getDropBt() { return dropBt; }
    public void setDropBt(Double v) { this.dropBt = v; }

    public Double getCoolTime() { return coolTime; }
    public void setCoolTime(Double v) { this.coolTime = v; }

    public Double getCoolEt() { return coolEt; }
    public void setCoolEt(Double v) { this.coolEt = v; }

    public Double getCoolBt() { return coolBt; }
    public void setCoolBt(Double v) { this.coolBt = v; }

    public Double getTotalTime() { return totalTime; }
    public void setTotalTime(Double v) { this.totalTime = v; }

    public Double getDryPhaseTime() { return dryPhaseTime; }
    public void setDryPhaseTime(Double v) { this.dryPhaseTime = v; }

    public Double getMidPhaseTime() { return midPhaseTime; }
    public void setMidPhaseTime(Double v) { this.midPhaseTime = v; }

    public Double getFinishPhaseTime() { return finishPhaseTime; }
    public void setFinishPhaseTime(Double v) { this.finishPhaseTime = v; }

    public Double getCoolPhaseTime() { return coolPhaseTime; }
    public void setCoolPhaseTime(Double v) { this.coolPhaseTime = v; }

    public Double getDryPhaseRor() { return dryPhaseRor; }
    public void setDryPhaseRor(Double v) { this.dryPhaseRor = v; }

    public Double getMidPhaseRor() { return midPhaseRor; }
    public void setMidPhaseRor(Double v) { this.midPhaseRor = v; }

    public Double getFinishPhaseRor() { return finishPhaseRor; }
    public void setFinishPhaseRor(Double v) { this.finishPhaseRor = v; }

    public Double getTotalRor() { return totalRor; }
    public void setTotalRor(Double v) { this.totalRor = v; }

    public Double getFcsRor() { return fcsRor; }
    public void setFcsRor(Double v) { this.fcsRor = v; }

    public Double getDryPhaseDeltaTemp() { return dryPhaseDeltaTemp; }
    public void setDryPhaseDeltaTemp(Double v) { this.dryPhaseDeltaTemp = v; }

    public Double getMidPhaseDeltaTemp() { return midPhaseDeltaTemp; }
    public void setMidPhaseDeltaTemp(Double v) { this.midPhaseDeltaTemp = v; }

    public Double getFinishPhaseDeltaTemp() { return finishPhaseDeltaTemp; }
    public void setFinishPhaseDeltaTemp(Double v) { this.finishPhaseDeltaTemp = v; }

    public Integer getTotalTs() { return totalTs; }
    public void setTotalTs(Integer v) { this.totalTs = v; }

    public Integer getTotalTsEt() { return totalTsEt; }
    public void setTotalTsEt(Integer v) { this.totalTsEt = v; }

    public Integer getTotalTsBt() { return totalTsBt; }
    public void setTotalTsBt(Integer v) { this.totalTsBt = v; }

    public Integer getAuc() { return auc; }
    public void setAuc(Integer v) { this.auc = v; }

    public String getAucBegin() { return aucBegin; }
    public void setAucBegin(String v) { this.aucBegin = v; }

    public Double getAucBase() { return aucBase; }
    public void setAucBase(Double v) { this.aucBase = v; }

    public Integer getAucFromEventFlag() { return aucFromEventFlag; }
    public void setAucFromEventFlag(Integer v) { this.aucFromEventFlag = v; }

    public Integer getDryPhaseAuc() { return dryPhaseAuc; }
    public void setDryPhaseAuc(Integer v) { this.dryPhaseAuc = v; }

    public Integer getMidPhaseAuc() { return midPhaseAuc; }
    public void setMidPhaseAuc(Integer v) { this.midPhaseAuc = v; }

    public Integer getFinishPhaseAuc() { return finishPhaseAuc; }
    public void setFinishPhaseAuc(Integer v) { this.finishPhaseAuc = v; }

    public Double getWeightLoss() { return weightLoss; }
    public void setWeightLoss(Double v) { this.weightLoss = v; }

    public Double getRoastDefectsLoss() { return roastDefectsLoss; }
    public void setRoastDefectsLoss(Double v) { this.roastDefectsLoss = v; }

    public Double getTotalLoss() { return totalLoss; }
    public void setTotalLoss(Double v) { this.totalLoss = v; }

    public Double getVolumeGain() { return volumeGain; }
    public void setVolumeGain(Double v) { this.volumeGain = v; }

    public Double getMoistureLoss() { return moistureLoss; }
    public void setMoistureLoss(Double v) { this.moistureLoss = v; }

    public Double getOrganicLoss() { return organicLoss; }
    public void setOrganicLoss(Double v) { this.organicLoss = v; }

    public Double getVolumeIn() { return volumeIn; }
    public void setVolumeIn(Double v) { this.volumeIn = v; }

    public Double getVolumeOut() { return volumeOut; }
    public void setVolumeOut(Double v) { this.volumeOut = v; }

    public Double getWeightIn() { return weightIn; }
    public void setWeightIn(Double v) { this.weightIn = v; }

    public Double getWeightOut() { return weightOut; }
    public void setWeightOut(Double v) { this.weightOut = v; }

    public Double getRoastDefectsWeight() { return roastDefectsWeight; }
    public void setRoastDefectsWeight(Double v) { this.roastDefectsWeight = v; }

    public Double getTotalYield() { return totalYield; }
    public void setTotalYield(Double v) { this.totalYield = v; }

    public Double getGreenDensity() { return greenDensity; }
    public void setGreenDensity(Double v) { this.greenDensity = v; }

    public Double getRoastedDensity() { return roastedDensity; }
    public void setRoastedDensity(Double v) { this.roastedDensity = v; }

    public Double getSetDensity() { return setDensity; }
    public void setSetDensity(Double v) { this.setDensity = v; }

    public Double getMoistureGreens() { return moistureGreens; }
    public void setMoistureGreens(Double v) { this.moistureGreens = v; }

    public Double getMoistureRoasted() { return moistureRoasted; }
    public void setMoistureRoasted(Double v) { this.moistureRoasted = v; }

    public Double getAmbientHumidity() { return ambientHumidity; }
    public void setAmbientHumidity(Double v) { this.ambientHumidity = v; }

    public Double getAmbientPressure() { return ambientPressure; }
    public void setAmbientPressure(Double v) { this.ambientPressure = v; }

    public Double getAmbientTemperature() { return ambientTemperature; }
    public void setAmbientTemperature(Double v) { this.ambientTemperature = v; }

    public Double getDet() { return det; }
    public void setDet(Double v) { this.det = v; }

    public Double getDbt() { return dbt; }
    public void setDbt(Double v) { this.dbt = v; }

    public Double getBtuPreheat() { return btuPreheat; }
    public void setBtuPreheat(Double v) { this.btuPreheat = v; }

    public Double getCo2Preheat() { return co2Preheat; }
    public void setCo2Preheat(Double v) { this.co2Preheat = v; }

    public Double getBtuBbp() { return btuBbp; }
    public void setBtuBbp(Double v) { this.btuBbp = v; }

    public Double getCo2Bbp() { return co2Bbp; }
    public void setCo2Bbp(Double v) { this.co2Bbp = v; }

    public Double getBtuCooling() { return btuCooling; }
    public void setBtuCooling(Double v) { this.btuCooling = v; }

    public Double getCo2Cooling() { return co2Cooling; }
    public void setCo2Cooling(Double v) { this.co2Cooling = v; }

    public Double getBtuLpg() { return btuLpg; }
    public void setBtuLpg(Double v) { this.btuLpg = v; }

    public Double getBtuNg() { return btuNg; }
    public void setBtuNg(Double v) { this.btuNg = v; }

    public Double getBtuElec() { return btuElec; }
    public void setBtuElec(Double v) { this.btuElec = v; }

    public Double getBtuBatch() { return btuBatch; }
    public void setBtuBatch(Double v) { this.btuBatch = v; }

    public Double getBtuBatchPerGreenKg() { return btuBatchPerGreenKg; }
    public void setBtuBatchPerGreenKg(Double v) { this.btuBatchPerGreenKg = v; }

    public Double getBtuRoast() { return btuRoast; }
    public void setBtuRoast(Double v) { this.btuRoast = v; }

    public Double getBtuRoastPerGreenKg() { return btuRoastPerGreenKg; }
    public void setBtuRoastPerGreenKg(Double v) { this.btuRoastPerGreenKg = v; }

    public Double getCo2Batch() { return co2Batch; }
    public void setCo2Batch(Double v) { this.co2Batch = v; }

    public Double getCo2BatchPerGreenKg() { return co2BatchPerGreenKg; }
    public void setCo2BatchPerGreenKg(Double v) { this.co2BatchPerGreenKg = v; }

    public Double getCo2Roast() { return co2Roast; }
    public void setCo2Roast(Double v) { this.co2Roast = v; }

    public Double getCo2RoastPerGreenKg() { return co2RoastPerGreenKg; }
    public void setCo2RoastPerGreenKg(Double v) { this.co2RoastPerGreenKg = v; }

    public Double getKwhBatchPerGreenKg() { return kwhBatchPerGreenKg; }
    public void setKwhBatchPerGreenKg(Double v) { this.kwhBatchPerGreenKg = v; }

    public Double getKwhRoastPerGreenKg() { return kwhRoastPerGreenKg; }
    public void setKwhRoastPerGreenKg(Double v) { this.kwhRoastPerGreenKg = v; }

    public Double getBbpTotalTime() { return bbpTotalTime; }
    public void setBbpTotalTime(Double v) { this.bbpTotalTime = v; }

    public Double getBbpBottomTemp() { return bbpBottomTemp; }
    public void setBbpBottomTemp(Double v) { this.bbpBottomTemp = v; }

    public Double getBbpBeginToBottomTime() { return bbpBeginToBottomTime; }
    public void setBbpBeginToBottomTime(Double v) { this.bbpBeginToBottomTime = v; }

    public Double getBbpBottomToChargeTime() { return bbpBottomToChargeTime; }
    public void setBbpBottomToChargeTime(Double v) { this.bbpBottomToChargeTime = v; }

    public Double getBbpBeginToBottomRor() { return bbpBeginToBottomRor; }
    public void setBbpBeginToBottomRor(Double v) { this.bbpBeginToBottomRor = v; }

    public Double getBbpBottomToChargeRor() { return bbpBottomToChargeRor; }
    public void setBbpBottomToChargeRor(Double v) { this.bbpBottomToChargeRor = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComputedProfileInformation)) return false;
        ComputedProfileInformation that = (ComputedProfileInformation) o;
        return Objects.equals(chargeEt, that.chargeEt)
                && Objects.equals(chargeBt, that.chargeBt)
                && Objects.equals(dryTime, that.dryTime)
                && Objects.equals(dropTime, that.dropTime)
                && Objects.equals(totalTime, that.totalTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chargeEt, chargeBt, dryTime, dropTime, totalTime);
    }
}
