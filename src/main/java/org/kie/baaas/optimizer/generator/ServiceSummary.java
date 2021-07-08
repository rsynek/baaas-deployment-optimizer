package org.kie.baaas.optimizer.generator;

public class ServiceSummary {
    private String name;
    private long cpuNanoCoresUsage;
    private long memoryBytesUsage;

    public ServiceSummary(String name, long cpuNanoCoresUsage, long memoryBytesUsage) {
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
