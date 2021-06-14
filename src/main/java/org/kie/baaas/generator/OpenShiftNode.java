package org.kie.baaas.generator;

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
}
