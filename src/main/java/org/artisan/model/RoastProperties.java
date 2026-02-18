package org.artisan.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Roast metadata DTO (from atypes / roast_properties).
 */
public final class RoastProperties {

    private final String beanName;
    private final LocalDate roastDate;
    private final double weightInGrams;
    private final double weightOutGrams;
    private final double moisturePercent;
    private final double densityGramsPerLiter;
    private final int colorWholeBean;
    private final int colorGround;
    private final String batchPrefix;
    private final int batchNumber;
    private final String roastNotes;
    private final String cuppingNotes;

    private RoastProperties(Builder b) {
        this.beanName = b.beanName;
        this.roastDate = b.roastDate;
        this.weightInGrams = b.weightInGrams;
        this.weightOutGrams = b.weightOutGrams;
        this.moisturePercent = b.moisturePercent;
        this.densityGramsPerLiter = b.densityGramsPerLiter;
        this.colorWholeBean = b.colorWholeBean;
        this.colorGround = b.colorGround;
        this.batchPrefix = b.batchPrefix;
        this.batchNumber = b.batchNumber;
        this.roastNotes = b.roastNotes;
        this.cuppingNotes = b.cuppingNotes;
    }

    public String getBeanName() {
        return beanName;
    }

    public LocalDate getRoastDate() {
        return roastDate;
    }

    public double getWeightInGrams() {
        return weightInGrams;
    }

    public double getWeightOutGrams() {
        return weightOutGrams;
    }

    public double getMoisturePercent() {
        return moisturePercent;
    }

    public double getDensityGramsPerLiter() {
        return densityGramsPerLiter;
    }

    public int getColorWholeBean() {
        return colorWholeBean;
    }

    public int getColorGround() {
        return colorGround;
    }

    public String getBatchPrefix() {
        return batchPrefix;
    }

    public int getBatchNumber() {
        return batchNumber;
    }

    public String getRoastNotes() {
        return roastNotes;
    }

    public String getCuppingNotes() {
        return cuppingNotes;
    }

    /**
     * Weight loss percentage: (weightIn - weightOut) / weightIn * 100.
     * Returns 0 if weightInGrams is 0.
     */
    public double weightLossPercent() {
        if (weightInGrams <= 0) {
            return 0.0;
        }
        return (weightInGrams - weightOutGrams) / weightInGrams * 100.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoastProperties that = (RoastProperties) o;
        return Double.compare(that.weightInGrams, weightInGrams) == 0
                && Double.compare(that.weightOutGrams, weightOutGrams) == 0
                && Double.compare(that.moisturePercent, moisturePercent) == 0
                && Double.compare(that.densityGramsPerLiter, densityGramsPerLiter) == 0
                && colorWholeBean == that.colorWholeBean
                && colorGround == that.colorGround
                && batchNumber == that.batchNumber
                && Objects.equals(beanName, that.beanName)
                && Objects.equals(roastDate, that.roastDate)
                && Objects.equals(batchPrefix, that.batchPrefix)
                && Objects.equals(roastNotes, that.roastNotes)
                && Objects.equals(cuppingNotes, that.cuppingNotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanName, roastDate, weightInGrams, weightOutGrams, moisturePercent,
                densityGramsPerLiter, colorWholeBean, colorGround, batchPrefix, batchNumber, roastNotes, cuppingNotes);
    }

    public static final class Builder {
        private String beanName = "";
        private LocalDate roastDate;
        private double weightInGrams;
        private double weightOutGrams;
        private double moisturePercent;
        private double densityGramsPerLiter;
        private int colorWholeBean;
        private int colorGround;
        private String batchPrefix = "";
        private int batchNumber;
        private String roastNotes = "";
        private String cuppingNotes = "";

        private Builder() {
        }

        public Builder beanName(String beanName) {
            this.beanName = beanName != null ? beanName : "";
            return this;
        }

        public Builder roastDate(LocalDate roastDate) {
            this.roastDate = roastDate;
            return this;
        }

        public Builder weightInGrams(double weightInGrams) {
            this.weightInGrams = weightInGrams;
            return this;
        }

        public Builder weightOutGrams(double weightOutGrams) {
            this.weightOutGrams = weightOutGrams;
            return this;
        }

        public Builder moisturePercent(double moisturePercent) {
            this.moisturePercent = moisturePercent;
            return this;
        }

        public Builder densityGramsPerLiter(double densityGramsPerLiter) {
            this.densityGramsPerLiter = densityGramsPerLiter;
            return this;
        }

        public Builder colorWholeBean(int colorWholeBean) {
            this.colorWholeBean = colorWholeBean;
            return this;
        }

        public Builder colorGround(int colorGround) {
            this.colorGround = colorGround;
            return this;
        }

        public Builder batchPrefix(String batchPrefix) {
            this.batchPrefix = batchPrefix != null ? batchPrefix : "";
            return this;
        }

        public Builder batchNumber(int batchNumber) {
            this.batchNumber = batchNumber;
            return this;
        }

        public Builder roastNotes(String roastNotes) {
            this.roastNotes = roastNotes != null ? roastNotes : "";
            return this;
        }

        public Builder cuppingNotes(String cuppingNotes) {
            this.cuppingNotes = cuppingNotes != null ? cuppingNotes : "";
            return this;
        }

        public RoastProperties build() {
            return new RoastProperties(this);
        }
    }
}
