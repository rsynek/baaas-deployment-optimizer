package org.kie.baaas.optimizer.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class ResourceRequirement extends AbstractIdentifiable implements Comparable<ResourceRequirement> {

    @JsonIdentityReference(alwaysAsId = true)
    private Service service;
    @JsonIdentityReference(alwaysAsId = true)
    private Resource resource;
    private long amount;

    public ResourceRequirement() {
        // Required by Jackson.
    }

    public ResourceRequirement(Service service, Resource resource, long amount) {
        this.service = service;
        this.resource = resource;
        this.amount = amount;
    }

    public ResourceRequirement(long id, Service service, Resource resource, long amount) {
        super(id);
        this.service = service;
        this.resource = resource;
        this.amount = amount;
    }

    public Service getService() {
        return service;
    }

    public Resource getResource() {
        return resource;
    }

    public long getAmount() {
        return amount;
    }

    @Override
    public int compareTo(ResourceRequirement other) {
        return getResource().getIndex() - other.getResource().getIndex();
    }
}
