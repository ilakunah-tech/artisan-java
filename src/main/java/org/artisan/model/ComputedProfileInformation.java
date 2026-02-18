package org.artisan.model;

import java.util.Objects;

/**
 * Computed profile information (Python: ComputedProfileInformation TypedDict).
 * All fields optional; used for CHARGE_ET/BT, DRY, FCs/FCe, DROP, COOL, phase times, RoR, etc.
 */
public class ComputedProfileInformation {

    private Double chargeEt;
    private Double chargeBt;
    private Double dryTime;
    private Double dryEt;
    private Double dryBt;
    private Double fcsTime;
    private Double fceTime;
    private Double dropTime;
    private Double dropEt;
    private Double dropBt;
    private Double totalTime;
    private Double totalRor;

    public ComputedProfileInformation() {}

    public Double getChargeEt() { return chargeEt; }
    public void setChargeEt(Double chargeEt) { this.chargeEt = chargeEt; }
    public Double getChargeBt() { return chargeBt; }
    public void setChargeBt(Double chargeBt) { this.chargeBt = chargeBt; }
    public Double getDryTime() { return dryTime; }
    public void setDryTime(Double dryTime) { this.dryTime = dryTime; }
    public Double getDryEt() { return dryEt; }
    public void setDryEt(Double dryEt) { this.dryEt = dryEt; }
    public Double getDryBt() { return dryBt; }
    public void setDryBt(Double dryBt) { this.dryBt = dryBt; }
    public Double getFcsTime() { return fcsTime; }
    public void setFcsTime(Double fcsTime) { this.fcsTime = fcsTime; }
    public Double getFceTime() { return fceTime; }
    public void setFceTime(Double fceTime) { this.fceTime = fceTime; }
    public Double getDropTime() { return dropTime; }
    public void setDropTime(Double dropTime) { this.dropTime = dropTime; }
    public Double getDropEt() { return dropEt; }
    public void setDropEt(Double dropEt) { this.dropEt = dropEt; }
    public Double getDropBt() { return dropBt; }
    public void setDropBt(Double dropBt) { this.dropBt = dropBt; }
    public Double getTotalTime() { return totalTime; }
    public void setTotalTime(Double totalTime) { this.totalTime = totalTime; }
    public Double getTotalRor() { return totalRor; }
    public void setTotalRor(Double totalRor) { this.totalRor = totalRor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
