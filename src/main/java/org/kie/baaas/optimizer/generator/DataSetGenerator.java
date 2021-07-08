package org.kie.baaas.optimizer.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.Resource;
import org.kie.baaas.optimizer.domain.ResourceCapacity;
import org.kie.baaas.optimizer.domain.ResourceRequirement;
import org.kie.baaas.optimizer.domain.Service;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;

@ApplicationScoped
public class DataSetGenerator {

    // TODO: generate some portion of general, memory-optimized and cpu-optimized clusters, according to best prices.
    //       general - Google, memory-optimized - Azure, cpu-optimized - AWS (brings also enough memory)

    // Using nano cores to avoid losing precision.
    private static final long CPU_CORES_MULTIPLIER = 1000_000_000L;
    private static final long MEMORY_MULTIPLIER = 1024L * 1024L * 1024L;
    private static final double RESOURCE_SAFE_CAPACITY_RATIO = 0.8;
    private final Random random = new Random();
    private final ClusterGenerator clusterGenerator = new ClusterGenerator(random);
    private final ServiceSummaryGenerator serviceSummaryGenerator = new ServiceSummaryGenerator(random);
    private final Resource cpuResource = new Resource(IdGenerator.nextId(), RESOURCE_SAFE_CAPACITY_RATIO);
    private final Resource memoryResource = new Resource(IdGenerator.nextId(), RESOURCE_SAFE_CAPACITY_RATIO);

    /**
     * Generates a single {@link DataSet} instance given the input parameters.
     *
     * @param clusterCount        the number of OSD cluster instances
     * @param minClusterSize      a minimal number of worker nodes in a single OSD cluster
     * @param maxClusterSize      a maximal number of worker nodes in a single OSD cluster
     * @param maxUtilizationRatio a total resource utilization ratio that must not be exceeded. A double between 0.0 and 1.0
     */
    public DataSet generateDataSet(int clusterCount, int minClusterSize, int maxClusterSize, double maxUtilizationRatio) {
        assertNonNegativeInteger(clusterCount, "clusterCount");
        assertNonNegativeInteger(minClusterSize, "minClusterSize");
        assertNonNegativeInteger(maxClusterSize, "maxClusterSize");
        if (maxUtilizationRatio < 0.0 || maxUtilizationRatio > 1.0) {
            throw new IllegalArgumentException("maxUtilizationRatio" + "(" + maxUtilizationRatio + ") is a double between 0.0 and 1.0.");
        }
        if (minClusterSize > maxClusterSize) {
            throw new IllegalArgumentException("The minClusterSize (" + minClusterSize + ") must be lesser or equal to maxClusterSize (" + maxClusterSize + ")");
        }

        List<OpenShiftCluster> openShiftClusters = generateClusters(clusterCount, minClusterSize, maxClusterSize);
        Map<OsdCluster, List<ResourceCapacity>> createOsdClustersWithResources = createOsdClustersWithResources(openShiftClusters);
        List<OsdCluster> osdClusters = new ArrayList<>(createOsdClustersWithResources.keySet());
        List<ResourceCapacity> resourceCapacities = createOsdClustersWithResources.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Map<Resource, Long> remainingCapacityPerResource = resourceCapacities.stream()
                .collect(Collectors.groupingBy(ResourceCapacity::getResource,
                        Collectors.summingLong(ResourceCapacity::getCapacity)));
        for (Map.Entry<Resource, Long> capacityEntry : remainingCapacityPerResource.entrySet()) {
            capacityEntry.setValue((long) (capacityEntry.getValue() * maxUtilizationRatio));
        }

        Map<Service, List<ResourceRequirement>> servicesWithResourceRequirements = generateServices(remainingCapacityPerResource);
        List<Service> services = new ArrayList<>(servicesWithResourceRequirements.keySet());
        List<ResourceRequirement> resourceRequirements = servicesWithResourceRequirements.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        ServiceDeploymentSchedule serviceDeploymentSchedule = new ServiceDeploymentSchedule(osdClusters, services,
                Arrays.asList(cpuResource, memoryResource), resourceCapacities, resourceRequirements);
        return new DataSet(openShiftClusters, serviceDeploymentSchedule);
    }

    private void assertNonNegativeInteger(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("The " + name + " (" + value + ") is a non-negative integer.");
        }
    }

    private List<OpenShiftCluster> generateClusters(int clusters, int minClusterSize, int maxClusterSize) {
        List<OpenShiftCluster> openShiftClusters = new ArrayList<>(clusters);
        for (int i = 0; i < clusters; i++) {
            // For simplicity, use only general instance types.
            openShiftClusters.add(clusterGenerator.generateCluster(CloudProvider.GOOGLE, nextIntBetween(minClusterSize, maxClusterSize), CloudInstanceType.GENERAL));
        }
        return openShiftClusters;
    }

    private int nextIntBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private Map<Service, List<ResourceRequirement>> generateServices(Map<Resource, Long> remainingCapacityPerResource) {
        Map<Service, List<ResourceRequirement>> podsWithResourceRequirements = new HashMap<>();

        boolean isAnyResourceDepleted = false;
        while (!isAnyResourceDepleted) {
            ServiceSummary serviceSummary = serviceSummaryGenerator.generateService();
            Service service = new Service(IdGenerator.nextId(), serviceSummary.getName());
            List<ResourceRequirement> resourceRequirements = createResourceRequirements(serviceSummary, service);
            for (ResourceRequirement resourceRequirement : resourceRequirements) {
                Long remainingCapacity = remainingCapacityPerResource.computeIfPresent(resourceRequirement.getResource(),
                        (resource, value) -> value - resourceRequirement.getAmount());
                if (remainingCapacity < 0) {
                    isAnyResourceDepleted = true;
                }
            }

            if (!isAnyResourceDepleted) {
                podsWithResourceRequirements.put(service, resourceRequirements);
            }
        }

        return podsWithResourceRequirements;
    }

    private List<ResourceRequirement> createResourceRequirements(ServiceSummary serviceSummary, Service service) {
        ResourceRequirement cpuRequirement = new ResourceRequirement(IdGenerator.nextId(), service, cpuResource, serviceSummary.getCpuNanoCoresUsage());
        ResourceRequirement memoryRequirement = new ResourceRequirement(IdGenerator.nextId(), service, memoryResource, serviceSummary.getMemoryBytesUsage());
        return Arrays.asList(cpuRequirement, memoryRequirement);
    }

    private Map<OsdCluster, List<ResourceCapacity>> createOsdClustersWithResources(List<OpenShiftCluster> openShiftClusters) {
        Map<OsdCluster, List<ResourceCapacity>> osdClustersWithResources = new HashMap<>(openShiftClusters.size());
        openShiftClusters.forEach(openShiftCluster -> {
            OsdCluster osdCluster = createOsdCluster(openShiftCluster);
            List<OpenShiftNode> workerNodes = openShiftCluster.getOpenShiftNodes().stream()
                    .filter(OpenShiftNode::isWorkerNode)
                    .collect(Collectors.toList());
            List<ResourceCapacity> resourceCapacities = createResourceCapacities(workerNodes, osdCluster);
            osdClustersWithResources.put(osdCluster, resourceCapacities);
        });

        return osdClustersWithResources;
    }

    private OsdCluster createOsdCluster(OpenShiftCluster openShiftCluster) {
        return new OsdCluster(IdGenerator.nextId(), (long) (openShiftCluster.getCost() * 1000_000L));
    }

    private List<ResourceCapacity> createResourceCapacities(List<OpenShiftNode> openShiftNodes, OsdCluster osdCluster) {
        int totalCpuCores = 0;
        int totalMemoryGiBs = 0;
        for (OpenShiftNode node : openShiftNodes) {
            totalCpuCores += node.getCloudNode().getCpuCores();
            totalMemoryGiBs += node.getCloudNode().getMemoryGiBs();
        }
        ResourceCapacity cpuCapacity = new ResourceCapacity(IdGenerator.nextId(), osdCluster, cpuResource, totalCpuCores * CPU_CORES_MULTIPLIER);
        ResourceCapacity memoryCapacity = new ResourceCapacity(IdGenerator.nextId(), osdCluster, memoryResource, totalMemoryGiBs * MEMORY_MULTIPLIER);
        return Arrays.asList(cpuCapacity, memoryCapacity);
    }

    private static final class IdGenerator {
        private static final AtomicLong NEXT_ID = new AtomicLong(1);

        static long nextId() {
            return NEXT_ID.getAndIncrement();
        }
    }
}
