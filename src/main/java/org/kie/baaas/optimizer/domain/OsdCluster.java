package org.kie.baaas.optimizer.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class OsdCluster extends AbstractIdentifiable {

    /**
     * Artificial {@link OsdCluster} instance to enable over-constrained planning.
     */
    public static final OsdCluster SINK = new OsdCluster(9999L);

    private long costPerHour;
    @JsonIdentityReference(alwaysAsId = true)
    private Region region;

    @JsonIgnore
    private List<ResourceCapacity> resourceCapacities;

    public OsdCluster() {
        // Required by Jackson.
    }

    public OsdCluster(long id) {
        super(id);
    }

    public OsdCluster(long id, long costPerHour, Region region) {
        super(id);
        this.costPerHour = costPerHour;
        this.region = region;
    }

    public long getCostPerHour() {
        return costPerHour;
    }

    public Region getRegion() {
        return region;
    }

    public void setResourceCapacities(List<ResourceCapacity> resourceCapacities) {
        this.resourceCapacities = resourceCapacities;
    }

    public ResourceCapacity getMachineCapacity(Resource resource) {
        return resourceCapacities.get(resource.getIndex());
    }
}
