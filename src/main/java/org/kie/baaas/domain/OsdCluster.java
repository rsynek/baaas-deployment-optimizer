package org.kie.baaas.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
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
