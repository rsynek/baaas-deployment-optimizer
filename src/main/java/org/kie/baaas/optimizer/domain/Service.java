package org.kie.baaas.optimizer.domain;

import java.util.Objects;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

@PlanningEntity
public class Service extends AbstractIdentifiable {

    private String name;
    @JsonIdentityReference(alwaysAsId = true)
    private OsdCluster osdCluster;
    @JsonIdentityReference(alwaysAsId = true)
    private OsdCluster originalOsdCluster;

    public Service() {
        // Required by Jackson and OptaPlanner.
    }

    public Service(long id, String name) {
        super(id);
        this.name = name;
    }

    public Service(long id, String name, OsdCluster originalOsdCluster) {
        super(id);
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

    public void setOsdCluster(OsdCluster osdCluster) {
        this.osdCluster = osdCluster;
    }

    public void setOriginalOsdCluster(OsdCluster originalOsdCluster) {
        this.originalOsdCluster = originalOsdCluster;
    }

    public OsdCluster getOriginalOsdCluster() {
        return originalOsdCluster;
    }

    public boolean isMoved() {
        if (osdCluster == null) {
            return false;
        }
        return !Objects.equals(originalOsdCluster, osdCluster);
    }
}
