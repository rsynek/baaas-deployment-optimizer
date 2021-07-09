package org.kie.baaas.optimizer.generator;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class OpenShiftCluster {

    private CloudProvider cloudProvider;
    private List<OpenShiftNode> openShiftNodes;

    public OpenShiftCluster() {
        // Required by Jackson.
    }

    public OpenShiftCluster(CloudProvider cloudProvider, List<OpenShiftNode> openShiftNodes) {
        this.cloudProvider = cloudProvider;
        this.openShiftNodes = openShiftNodes;
    }

    public CloudProvider getCloudProvider() {
        return cloudProvider;
    }

    public List<OpenShiftNode> getOpenShiftNodes() {
        return openShiftNodes;
    }

    public double getCost() {
        return openShiftNodes.stream().collect(Collectors.summingDouble(OpenShiftNode::getCost));
    }

    public int getTotalCpuCores() {
        return openShiftNodes.stream()
                .filter(openShiftNode -> openShiftNode.getNodeType() == OpenShiftNodeType.WORKER)
                .map(OpenShiftNode::getCloudNode)
                .collect(Collectors.summingInt(CloudNode::getCpuCores));
    }

    public int getTotalMemoryGiBs() {
        return openShiftNodes.stream()
                .filter(openShiftNode -> openShiftNode.getNodeType() == OpenShiftNodeType.WORKER)
                .map(OpenShiftNode::getCloudNode)
                .collect(Collectors.summingInt(CloudNode::getMemoryGiBs));
    }
}
