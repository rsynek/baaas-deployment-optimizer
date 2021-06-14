package org.kie.baaas.generator;

public class CloudNode {
    private String name;
    private CloudProvider cloudProvider;
    private int cpuCores;
    private int memoryGiBs;
    private CloudInstanceType cloudInstanceType;
    private double cost;

    public CloudNode() {
        // Required by Jackson.
    }

    public CloudNode(String name, CloudProvider cloudProvider, CloudInstanceType cloudInstanceType, int cpuCores, int memoryGiBs, double cost) {
        this.name = name;
        this.cloudProvider = cloudProvider;
        this.cloudInstanceType = cloudInstanceType;
        this.cpuCores = cpuCores;
        this.memoryGiBs = memoryGiBs;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public CloudProvider getCloudProvider() {
        return cloudProvider;
    }

    public CloudInstanceType getCloudInstanceType() {
        return cloudInstanceType;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public int getMemoryGiBs() {
        return memoryGiBs;
    }

    public double getCost() {
        return cost;
    }
}
