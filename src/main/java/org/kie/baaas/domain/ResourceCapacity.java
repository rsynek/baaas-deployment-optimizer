package org.kie.baaas.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class ResourceCapacity extends AbstractIdentifiable {

    @JsonIdentityReference(alwaysAsId=true)
    private Node node;
    @JsonIdentityReference(alwaysAsId=true)
    private Resource resource;
    private long capacity;

    public ResourceCapacity() {
        // Required by Jackson.
    }

    public ResourceCapacity(Node node, Resource resource, long capacity) {
        this.node = node;
        this.resource = resource;
        this.capacity = capacity;
    }

    public ResourceCapacity(long id, Node node, Resource resource, long capacity) {
        super(id);
        this.node = node;
        this.resource = resource;
        this.capacity = capacity;
    }

    public Node getNode() {
        return node;
    }

    public Resource getResource() {
        return resource;
    }

    public long getCapacity() {
        return capacity;
    }
}
