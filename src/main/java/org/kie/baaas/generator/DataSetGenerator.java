package org.kie.baaas.generator;

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

import org.kie.baaas.domain.Node;
import org.kie.baaas.domain.OsdCluster;
import org.kie.baaas.domain.Pod;
import org.kie.baaas.domain.Resource;
import org.kie.baaas.domain.ResourceCapacity;
import org.kie.baaas.domain.ResourceRequirement;
import org.kie.baaas.domain.Service;
import org.kie.baaas.domain.ServiceDeploymentSchedule;

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
    private final PodSummaryGenerator podSummaryGenerator = new PodSummaryGenerator(random);
    private final Resource cpuResource = new Resource(IdGenerator.nextId(), RESOURCE_SAFE_CAPACITY_RATIO);
    private final Resource memoryResource = new Resource(IdGenerator.nextId(), RESOURCE_SAFE_CAPACITY_RATIO);

    /**
     * Generates a single {@link DataSet} instance given the input parameters.
     *
     * @param clusterCount         the number of OSD cluster instances
     * @param minClusterSize       a minimal number of worker nodes in a single OSD cluster
     * @param maxClusterSize       a maximal number of worker nodes in a single OSD cluster
     * @param baseUtilizationRatio a resource utilization ratio not counting the generated services. This parameter
     *                             artificially decreases the resource capacities to simulate resource consumption by
     *                             processes not directly related to Decision Services. A double between 0.0 and 1.0.
     * @param maxUtilizationRatio  a total resource utilization ratio that must not be exceeded. A double between 0.0 and 1.0
     */
    public DataSet generateDataSet(int clusterCount, int minClusterSize, int maxClusterSize, double baseUtilizationRatio, double maxUtilizationRatio) {
        assertNonNegativeInteger(clusterCount, "clusterCount");
        assertNonNegativeInteger(minClusterSize, "minClusterSize");
        assertNonNegativeInteger(maxClusterSize, "maxClusterSize");
        assertRatio(baseUtilizationRatio, "baseUtilizationRatio");
        assertRatio(maxUtilizationRatio, "maxUtilizationRatio");
        if (minClusterSize > maxClusterSize) {
            throw new IllegalArgumentException("The minClusterSize (" + minClusterSize + ") must be lesser or equal to maxClusterSize (" + maxClusterSize + ")");
        }

        List<OpenShiftCluster> openShiftClusters = generateClusters(clusterCount, minClusterSize, maxClusterSize);
        Map<Node, List<ResourceCapacity>> nodesWithResourceCapacities = createNodes(openShiftClusters, baseUtilizationRatio);
        List<Node> nodes = new ArrayList<>(nodesWithResourceCapacities.keySet());
        List<OsdCluster> osdClusters = nodes.stream()
                .map(Node::getOsdCluster)
                .distinct()
                .collect(Collectors.toList());
        List<ResourceCapacity> resourceCapacities = nodesWithResourceCapacities.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        //  60 - 10 = 50% utilization can be used by services
        final double remainingResourceRatio = maxUtilizationRatio - baseUtilizationRatio;
        Map<Resource, Long> remainingCapacityPerResource = resourceCapacities.stream()
                .collect(Collectors.groupingBy(ResourceCapacity::getResource,
                        Collectors.summingLong(ResourceCapacity::getCapacity)));
        for (Map.Entry<Resource, Long> capacityEntry : remainingCapacityPerResource.entrySet()) {
            capacityEntry.setValue((long) (capacityEntry.getValue() * remainingResourceRatio));
        }

        Map<Pod, List<ResourceRequirement>> podsWithResourceRequirements = generatePods(remainingCapacityPerResource);
        List<Pod> pods = new ArrayList<>(podsWithResourceRequirements.keySet());
        List<Service> services = pods.stream()
                .map(Pod::getService)
                .distinct()
                .collect(Collectors.toList());
        List<ResourceRequirement> resourceRequirements = podsWithResourceRequirements.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        ServiceDeploymentSchedule serviceDeploymentSchedule = new ServiceDeploymentSchedule(osdClusters, nodes,
                services, pods, Arrays.asList(cpuResource, memoryResource), resourceCapacities, resourceRequirements);
        return new DataSet(openShiftClusters, serviceDeploymentSchedule);
    }

    private void assertNonNegativeInteger(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("The " + name + " (" + value + ") is a non-negative integer.");
        }
    }

    private void assertRatio(double value, String name) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + "(" + value + ") is a double between 0.0 and 1.0.");
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

    private Map<Pod, List<ResourceRequirement>> generatePods(Map<Resource, Long> remainingCapacityPerResource) {
        Map<Pod, List<ResourceRequirement>> podsWithResourceRequirements = new HashMap<>();

        boolean isAnyResourceDepleted = false;
        while (!isAnyResourceDepleted) {
            PodSummary podSummary = podSummaryGenerator.generatePod();
            // TODO: Generate services with multiple running pods.
            Service service = new Service(IdGenerator.nextId());
            Pod pod = new Pod(IdGenerator.nextId(), podSummary.getName(), service);
            List<ResourceRequirement> resourceRequirements = createResourceRequirements(podSummary, pod);
            for (ResourceRequirement resourceRequirement : resourceRequirements) {
                Long remainingCapacity = remainingCapacityPerResource.computeIfPresent(resourceRequirement.getResource(),
                        (resource, value) -> value - resourceRequirement.getAmount());
                if (remainingCapacity < 0) {
                    isAnyResourceDepleted = true;
                }
            }

            if (!isAnyResourceDepleted) {
                podsWithResourceRequirements.put(pod, resourceRequirements);
            }
        }

        return podsWithResourceRequirements;
    }

    private List<ResourceRequirement> createResourceRequirements(PodSummary podSummary, Pod pod) {
        ResourceRequirement cpuRequirement = new ResourceRequirement(IdGenerator.nextId(), pod, cpuResource, podSummary.getCpuNanoCoresUsage());
        ResourceRequirement memoryRequirement = new ResourceRequirement(IdGenerator.nextId(), pod, memoryResource, podSummary.getMemoryBytesUsage());
        return Arrays.asList(cpuRequirement, memoryRequirement);
    }

    private Map<Node, List<ResourceCapacity>> createNodes(List<OpenShiftCluster> openShiftClusters, double baseUtilizationRatio) {
        Map<Node, List<ResourceCapacity>> nodesWithResourceCapacities = new HashMap<>(openShiftClusters.size());
        openShiftClusters.forEach(openShiftCluster -> {
            OsdCluster osdCluster = createOsdCluster(openShiftCluster);
            for (OpenShiftNode openShiftNode : openShiftCluster.getOpenShiftNodes()) {
                Node node = new Node(IdGenerator.nextId(), osdCluster);
                List<ResourceCapacity> resourceCapacities = createResourceCapacities(openShiftNode, node, baseUtilizationRatio);
                nodesWithResourceCapacities.put(node, resourceCapacities);
            }
        });
        return nodesWithResourceCapacities;
    }

    private OsdCluster createOsdCluster(OpenShiftCluster openShiftCluster) {
        return new OsdCluster(IdGenerator.nextId(), (long) (openShiftCluster.getCost() * 1000_000L));
    }

    private List<ResourceCapacity> createResourceCapacities(OpenShiftNode openShiftNode, Node node, double baseUtilizationRatio) {
        ResourceCapacity cpuCapacity = new ResourceCapacity(IdGenerator.nextId(), node, cpuResource,
                computeTotalAvailableCapacity(openShiftNode.getCloudNode().getCpuCores() * CPU_CORES_MULTIPLIER, baseUtilizationRatio));
        ResourceCapacity memoryCapacity = new ResourceCapacity(IdGenerator.nextId(), node, memoryResource,
                computeTotalAvailableCapacity(openShiftNode.getCloudNode().getMemoryGiBs() * MEMORY_MULTIPLIER, baseUtilizationRatio));
        return Arrays.asList(cpuCapacity, memoryCapacity);
    }

    private long computeTotalAvailableCapacity(long totalCapacity, double baseUtilizationRatio) {
        return (long) (totalCapacity * (1.0 - baseUtilizationRatio));
    }

    private static final class IdGenerator {
        private static final AtomicLong NEXT_ID = new AtomicLong(1);

        static long nextId() {
            return NEXT_ID.getAndIncrement();
        }
    }
}
