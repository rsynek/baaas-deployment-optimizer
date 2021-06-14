package org.kie.baaas.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class ResourceRequirement extends AbstractIdentifiable {

    @JsonIdentityReference(alwaysAsId=true)
    private Service service;
    @JsonIdentityReference(alwaysAsId=true)
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
}
