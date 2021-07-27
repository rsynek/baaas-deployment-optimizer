package org.kie.baaas.optimizer.generator;

public class ServiceSummary {
    private String name;
    private long cpuMilliCoresUsage;
    private long memoryKBytesUsage;

    public ServiceSummary(String name, long cpuMilliCoresUsage, long memoryKBytesUsage) {
        this.name = name;
        this.cpuMilliCoresUsage = cpuMilliCoresUsage;
        this.memoryKBytesUsage = memoryKBytesUsage;
    }

    public String getName() {
        return name;
    }

    public long getCpuMilliCoresUsage() {
        return cpuMilliCoresUsage;
    }

    public long getMemoryKBytesUsage() {
        return memoryKBytesUsage;
    }
}
