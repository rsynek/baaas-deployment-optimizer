package org.kie.baaas.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class ResourceCapacity extends AbstractIdentifiable {

    @JsonIdentityReference(alwaysAsId=true)
    private OsdCluster osdCluster;
    @JsonIdentityReference(alwaysAsId=true)
    private Resource resource;
    private long capacity;

    public ResourceCapacity() {
        // Required by Jackson.
    }

    public ResourceCapacity(OsdCluster osdCluster, Resource resource, long capacity) {
        this.osdCluster = osdCluster;
        this.resource = resource;
        this.capacity = capacity;
    }

    public ResourceCapacity(long id, OsdCluster osdCluster, Resource resource, long capacity) {
        super(id);
        this.osdCluster = osdCluster;
        this.resource = resource;
        this.capacity = capacity;
    }

    public OsdCluster getOsdCluster() {
        return osdCluster;
    }

    public Resource getResource() {
        return resource;
    }

    public long getCapacity() {
        return capacity;
    }
}
