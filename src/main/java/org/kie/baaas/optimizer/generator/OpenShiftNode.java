package org.kie.baaas.optimizer.generator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class OpenShiftNode {
    private OpenShiftNodeType nodeType;
    private CloudNode cloudNode;

    public OpenShiftNode() {
        // Required by Jackson.
    }

    public OpenShiftNode(OpenShiftNodeType nodeType, CloudNode cloudNode) {
        this.nodeType = nodeType;
        this.cloudNode = cloudNode;
    }

    public OpenShiftNodeType getNodeType() {
        return nodeType;
    }

    public CloudNode getCloudNode() {
        return cloudNode;
    }

    public double getCost() {
        return cloudNode.getCost();
    }

    public boolean isWorkerNode() {
        return nodeType == OpenShiftNodeType.WORKER;
    }
}
