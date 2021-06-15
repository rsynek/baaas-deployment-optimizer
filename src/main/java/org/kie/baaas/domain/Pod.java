package org.kie.baaas.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@PlanningEntity
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Pod extends AbstractIdentifiable {

    private String name;
    @JsonIdentityReference(alwaysAsId=true)
    private Service service;
    @JsonIdentityReference(alwaysAsId=true)
    private Node node;
    @JsonIdentityReference(alwaysAsId=true)
    private Node originalNode;

    public Pod() {
    }

    public Pod(long id, String name, Service service) {
        super(id);
        this.name = name;
        this.service = service;
    }

    public Pod(long id, String name, Service service, Node originalNode) {
        super(id);
        this.name = name;
        this.service = service;
        this.originalNode = originalNode;
    }

    public String getName() {
        return name;
    }

    public Service getService() {
        return service;
    }

    @PlanningVariable(valueRangeProviderRefs = "nodesRange")
    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getOriginalNode() {
        return originalNode;
    }
}
