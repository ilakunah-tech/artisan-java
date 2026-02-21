package org.artisan.ui.model;

import java.util.Objects;

/**
 * Item in the production plan list (Pre-Roast screen).
 */
public final class ProductionItem {

    private String name;
    private double weight;
    private String machine;

    public ProductionItem() {
        this("", 0, "");
    }

    public ProductionItem(String name, double weight, String machine) {
        this.name = name != null ? name : "";
        this.weight = weight;
        this.machine = machine != null ? machine : "";
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name != null ? name : ""; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getMachine() { return machine; }
    public void setMachine(String machine) { this.machine = machine != null ? machine : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductionItem that = (ProductionItem) o;
        return Double.compare(that.weight, weight) == 0
                && Objects.equals(name, that.name)
                && Objects.equals(machine, that.machine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weight, machine);
    }
}
