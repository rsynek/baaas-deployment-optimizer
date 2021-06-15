package org.kie.baaas.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Node extends AbstractIdentifiable {

    @JsonIdentityReference(alwaysAsId=true)
    private OsdCluster osdCluster;

    public Node() {
    }

    public Node(long id, OsdCluster osdCluster) {
        super(id);
        this.osdCluster = osdCluster;
    }

    public OsdCluster getOsdCluster() {
        return osdCluster;
    }
}
