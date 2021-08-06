package org.kie.baaas.optimizer.generator;

public class ServiceSummary {
    private String name;
    private long cpuMicroCoresUsage;
    private long memoryKBytesUsage;

    public ServiceSummary(String name, long cpuMicroCoresUsage, long memoryKBytesUsage) {
        this.name = name;
        this.cpuMicroCoresUsage = cpuMicroCoresUsage;
        this.memoryKBytesUsage = memoryKBytesUsage;
    }

    public String getName() {
        return name;
    }

    public long getCpuMicroCoresUsage() {
        return cpuMicroCoresUsage;
    }

    public long getMemoryKBytesUsage() {
        return memoryKBytesUsage;
    }
}
