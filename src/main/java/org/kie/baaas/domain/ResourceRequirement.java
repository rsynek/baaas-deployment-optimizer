package org.kie.baaas.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class ResourceRequirement extends AbstractIdentifiable {

    @JsonIdentityReference(alwaysAsId=true)
    private Pod pod;
    @JsonIdentityReference(alwaysAsId=true)
    private Resource resource;
    private long amount;

    public ResourceRequirement() {
        // Required by Jackson.
    }

    public ResourceRequirement(Pod pod, Resource resource, long amount) {
        this.pod = pod;
        this.resource = resource;
        this.amount = amount;
    }

    public ResourceRequirement(long id, Pod pod, Resource resource, long amount) {
        super(id);
        this.pod = pod;
        this.resource = resource;
        this.amount = amount;
    }

    public Pod getPod() {
        return pod;
    }

    public Resource getResource() {
        return resource;
    }

    public long getAmount() {
        return amount;
    }
}
