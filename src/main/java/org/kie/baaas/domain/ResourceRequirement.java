package org.kie.baaas.domain;

public class ResourceRequirement {

    private Service service;
    private Resource resource;
    private long amount;

    public ResourceRequirement(Service service, Resource resource, long amount) {
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
