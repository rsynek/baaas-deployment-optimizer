package org.kie.baaas.generator;

public class PodSummary {
    private String name;
    private long cpuNanoCoresUsage;
    private long memoryBytesUsage;

    public PodSummary(String name, long cpuNanoCoresUsage, long memoryBytesUsage) {
        this.name = name;
        this.cpuNanoCoresUsage = cpuNanoCoresUsage;
        this.memoryBytesUsage = memoryBytesUsage;
    }

    public String getName() {
        return name;
    }

    public long getCpuNanoCoresUsage() {
        return cpuNanoCoresUsage;
    }

    public long getMemoryBytesUsage() {
        return memoryBytesUsage;
    }
}
