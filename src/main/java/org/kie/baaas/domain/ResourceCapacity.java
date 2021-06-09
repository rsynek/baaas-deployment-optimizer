package org.kie.baaas.domain;

public class ResourceCapacity {

    private OsdCluster osdCluster;
    private Resource resource;
    private long capacity;

    public ResourceCapacity(OsdCluster osdCluster, Resource resource, long capacity) {
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
