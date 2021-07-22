package org.kie.baaas.optimizer.domain;

import java.util.List;
import java.util.Objects;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Service extends AbstractIdentifiable {

    private String name;
    @JsonIdentityReference(alwaysAsId = true)
    private OsdCluster osdCluster;
    @JsonIdentityReference(alwaysAsId = true)
    private OsdCluster originalOsdCluster;
    @JsonIdentityReference(alwaysAsId = true)
    private Region region;
    @JsonIdentityReference(alwaysAsId = true)
    private Customer customer;
    @JsonIgnore
    private List<ResourceRequirement> resourceRequirements;

    public Service() {
        // Required by Jackson and OptaPlanner.
    }

    public Service(long id, String name, Region region) {
        super(id);
        this.region = region;
        this.name = name;
    }

    public Service(long id, String name, Region region, Customer customer) {
        super(id);
        this.region = region;
        this.name = name;
        this.customer = customer;
    }

    public Service(long id, String name, Region region, OsdCluster originalOsdCluster) {
        super(id);
        this.name = name;
        this.region = region;
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

    public Region getRegion() {
        return region;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setOriginalOsdCluster(OsdCluster originalOsdCluster) {
        this.originalOsdCluster = originalOsdCluster;
    }

    public OsdCluster getOriginalOsdCluster() {
        return originalOsdCluster;
    }

    public void setResourceRequirements(List<ResourceRequirement> resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public long getUsage(Resource resource) {
        return resource.getIndex() >= resourceRequirements.size() ?
                0L : resourceRequirements.get(resource.getIndex()).getAmount();
    }

    @JsonIgnore
    public boolean isMoved() {
        if (osdCluster == null) {
            return false;
        }
        return !Objects.equals(originalOsdCluster, osdCluster);
    }
}
