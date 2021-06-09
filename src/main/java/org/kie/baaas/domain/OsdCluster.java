package org.kie.baaas.domain;

import java.util.List;

import org.optaplanner.core.api.domain.lookup.PlanningId;

public class OsdCluster {

    @PlanningId
    private long id;

    private double costPerHour;

    private List<ResourceCapacity> resourceCapacities;

    public OsdCluster(long id, double costPerHour) {
        this.id = id;
        this.costPerHour = costPerHour;
    }

    public double getCostPerHour() {
        return costPerHour;
    }

    public long getId() {
        return id;
    }
}
