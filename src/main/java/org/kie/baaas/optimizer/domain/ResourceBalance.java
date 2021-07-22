package org.kie.baaas.optimizer.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class ResourceBalance extends AbstractIdentifiable {
    @JsonIdentityReference(alwaysAsId=true)
    private Resource originResource;
    @JsonIdentityReference(alwaysAsId=true)
    private Resource targetResource;
    private int multiplicand;

    public ResourceBalance() {
        // Required by Jackson.
    }

    public ResourceBalance(long id, Resource originResource, Resource targetResource, int multiplicand) {
        super(id);
        this.originResource = originResource;
        this.targetResource = targetResource;
        this.multiplicand = multiplicand;
    }

    public Resource getOriginResource() {
        return originResource;
    }

    public Resource getTargetResource() {
        return targetResource;
    }

    public int getMultiplicand() {
        return multiplicand;
    }
}
