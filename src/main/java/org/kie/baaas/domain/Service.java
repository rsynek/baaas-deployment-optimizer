package org.kie.baaas.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@PlanningEntity
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Service extends AbstractIdentifiable {

    private String name;
    @JsonIdentityReference(alwaysAsId=true)
    private OsdCluster osdCluster;
    @JsonIdentityReference(alwaysAsId=true)
    private OsdCluster originalOsdCluster;

    public Service() {
        // Required by Jackson and OptaPlanner.
    }

    public Service(String name) {
        this.name = name;
    }

    public Service(String name, OsdCluster originalOsdCluster) {
        this.name = name;
        this.originalOsdCluster = originalOsdCluster;
    }

    public Service(long id, String name) {
        super(id);
        this.name = name;
    }

    public Service(long id, String name, OsdCluster osdCluster, OsdCluster originalOsdCluster) {
        super(id);
        this.name = name;
        this.osdCluster = osdCluster;
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
}
