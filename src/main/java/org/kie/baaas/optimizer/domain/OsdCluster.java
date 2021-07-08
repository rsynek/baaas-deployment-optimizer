package org.kie.baaas.optimizer.domain;

public class OsdCluster extends AbstractIdentifiable {

    private long costPerHour;

    public OsdCluster() {
        // Required by Jackson.
    }

    public OsdCluster(long id, long costPerHour) {
        super(id);
        this.costPerHour = costPerHour;
    }

    public long getCostPerHour() {
        return costPerHour;
    }
}
