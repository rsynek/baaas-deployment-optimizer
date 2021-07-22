package org.kie.baaas.optimizer.generator;

import java.util.Random;
import java.util.UUID;

class ServiceSummaryGenerator {

    /*
        The original data gathered during a load test:

                      Pod name               |         CPU         |   Memory   | Net received | Net transmitted
        face-mask-1-68fbb445d8-wrxwc         | 0.09443372246981484 |  368830464 |         2315 |           6923
        hospitals-1-7c848655-bfxfl           | 0.12638245970740736 |  387784704 |         1850 |           6679
        status-service-1-c9b458f87-6gnqr     | 0.06472619953148148 |  375760896 |         2645 |           4303
        traffic-violation-1-75c8bcc4dc-nk2wr | 0.06819738478074791 |  295792640 |          935 |           3693
        loan-1-6c578569c4-mptqb              | 0.20683128819074081 |  333385728 |         4694 |          18580
     */

    // From 0.01 to 1 CPU
    private static final ResourceValueDescriptor CPU = new ResourceValueDescriptor(10_000_000L, 1_000_000_000L);
    // From 200 to 600 MB
    private static final ResourceValueDescriptor MEMORY = new ResourceValueDescriptor(200_000_000L, 600_000_000L);

    // TODO: Use network-related resources when the network capacity of a cluster can be calculated.
    // From 0 to 100 kB
    private static final ResourceValueDescriptor NETWORK_IN = new ResourceValueDescriptor(0L, 100_000L);
    // From 0 to 500 kB
    private static final ResourceValueDescriptor NETWORK_OUT = new ResourceValueDescriptor(0L, 500_000L);

    private final Random random;

    ServiceSummaryGenerator(Random random) {
        this.random = random;
    }

    ServiceSummary generateService() {
        long cpuNanoCores = nextLong(CPU.minValue, CPU.maxValue);
        long memoryBytes = nextLong(MEMORY.minValue, MEMORY.maxValue);
        return new ServiceSummary(generateServiceName(), cpuNanoCores, memoryBytes);
    }

    private String generateServiceName() {
        return "decision-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private long nextLong(long min, long max) {
        return min + (long) (random.nextDouble() * (max - min));
    }

    private static final class ResourceValueDescriptor {
        final long minValue;
        final long maxValue;

        public ResourceValueDescriptor(long minValue, long maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }
    }
}
