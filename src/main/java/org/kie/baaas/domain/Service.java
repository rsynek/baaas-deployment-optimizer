package org.kie.baaas.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Service {

    private String name;
    private OsdCluster osdCluster;
    private OsdCluster originalOsdCluster;

    public Service(String name) {
        this.name = name;
    }

    public Service(String name, OsdCluster originalOsdCluster) {
        this.name = name;
        this.originalOsdCluster = originalOsdCluster;
    }

    public String getName() {
        return name;
    }

    @PlanningVariable(valueRangeProviderRefs = "clustersRange")
    public OsdCluster getOsdCluster() {
        return osdCluster;
    }

    public OsdCluster getOriginalOsdCluster() {
        return originalOsdCluster;
    }

    public void setOsdCluster(OsdCluster osdCluster) {
        this.osdCluster = osdCluster;
    }

    //    public long getUsage(Resource resource) {
//
//    }
}
