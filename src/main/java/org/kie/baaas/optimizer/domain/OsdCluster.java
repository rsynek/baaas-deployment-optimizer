package org.kie.baaas.optimizer.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class OsdCluster extends AbstractIdentifiable {

    public static final OsdCluster SINK = new OsdCluster(9999L);

    private long costPerHour;
    @JsonIdentityReference(alwaysAsId = true)
    private Region region;

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
}
