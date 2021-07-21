package org.kie.baaas.optimizer.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class ClusterGenerator {

    private static final int MASTER_NODE_COUNT = 3;

    private static final CloudNode[] AWS_INSTANCE_TYPES = {
            // AWS general instances.
            new CloudNode("m5.large", CloudProvider.AWS, CloudInstanceType.GENERAL, 2, 8, 0.107),
            new CloudNode("m5.xlarge", CloudProvider.AWS, CloudInstanceType.GENERAL, 4, 16, 0.214),
            new CloudNode("m5.2xlarge", CloudProvider.AWS, CloudInstanceType.GENERAL, 8, 32, 0.428),
            // AWS memory-optimized instances.
            new CloudNode("x2gd.xlarge", CloudProvider.AWS, CloudInstanceType.MEMORY_OPTIMIZED, 4, 64, 0.40),
            new CloudNode("x2gd.2xlarge", CloudProvider.AWS, CloudInstanceType.MEMORY_OPTIMIZED, 8, 128, 0.80),
            // AWS cpu-optimized instances.
            new CloudNode("c6g.2xlarge", CloudProvider.AWS, CloudInstanceType.CPU_OPTIMIZED, 8, 16, 0.3072),
            new CloudNode("c6gd.4xlarge", CloudProvider.AWS, CloudInstanceType.CPU_OPTIMIZED, 16, 32, 0.6976),
    };

    private static final CloudNode[] AZURE_INSTANCE_TYPES = {
            // Azure general instances.
            new CloudNode("D2s v4", CloudProvider.AZURE, CloudInstanceType.GENERAL, 2, 8, 0.116),
            new CloudNode("D4s v4", CloudProvider.AZURE, CloudInstanceType.GENERAL, 4, 16, 0.232),
            new CloudNode("D8s v4", CloudProvider.AZURE, CloudInstanceType.GENERAL, 8, 32, 0.465),
            // Azure memory-optimized instances.
            new CloudNode("E8 v5", CloudProvider.AZURE, CloudInstanceType.MEMORY_OPTIMIZED, 8, 64, 0.306),
            new CloudNode("E16 v5", CloudProvider.AZURE, CloudInstanceType.MEMORY_OPTIMIZED, 16, 128, 0.612),
            // Azure cpu-optimized instances.
            new CloudNode("F8s v2", CloudProvider.AZURE, CloudInstanceType.CPU_OPTIMIZED, 8, 16, 0.405),
            new CloudNode("F16s v2", CloudProvider.AZURE, CloudInstanceType.CPU_OPTIMIZED, 16, 32, 0.809),
    };

    private static final CloudNode[] GOOGLE_INSTANCE_TYPES = {
            // Google general instances.
            new CloudNode("e2-standard-2", CloudProvider.GOOGLE, CloudInstanceType.GENERAL, 2, 8, 0.086334),
            new CloudNode("e2-standard-4", CloudProvider.GOOGLE, CloudInstanceType.GENERAL, 4, 16, 0.172668),
            new CloudNode("e2-standard-8", CloudProvider.GOOGLE, CloudInstanceType.GENERAL, 8, 32, 0.345336),
            // Google memory-optimized instances.
            new CloudNode("e2-highmem-8", CloudProvider.GOOGLE, CloudInstanceType.MEMORY_OPTIMIZED, 8, 64, 0.465848),
            new CloudNode("e2-highmem-16", CloudProvider.GOOGLE, CloudInstanceType.MEMORY_OPTIMIZED, 16, 128, 0.931696),
            // Google cpu-optimized instances.
            new CloudNode("e2-highcpu-8", CloudProvider.GOOGLE, CloudInstanceType.CPU_OPTIMIZED, 8, 8, 0.254952),
            new CloudNode("e2-highcpu-16", CloudProvider.GOOGLE, CloudInstanceType.CPU_OPTIMIZED, 16, 16, 0.509904)
    };

    private final Random random;
    private final Map<CloudProvider, CloudNode[]> instanceTypesPerCloudProvider = new HashMap<>();

    ClusterGenerator(Random random) {
        this.random = random;
        instanceTypesPerCloudProvider.put(CloudProvider.AWS, AWS_INSTANCE_TYPES);
        instanceTypesPerCloudProvider.put(CloudProvider.AZURE, AZURE_INSTANCE_TYPES);
        instanceTypesPerCloudProvider.put(CloudProvider.GOOGLE, GOOGLE_INSTANCE_TYPES);
    }

    private CloudNode generateCloudNode(CloudProvider cloudProvider, CloudInstanceType cloudInstanceType) {
        CloudNode[] cloudNodes = Arrays.stream(instanceTypesPerCloudProvider.get(cloudProvider))
                .filter(cloudNode -> cloudNode.getCloudInstanceType() == cloudInstanceType)
                .toArray(CloudNode[]::new);
        return cloudNodes[random.nextInt(cloudNodes.length)];
    }

    private OpenShiftNode generateOpenShiftNode(CloudProvider cloudProvider, OpenShiftNodeType openShiftNodeType,
                                                CloudInstanceType cloudInstanceType) {
        return new OpenShiftNode(openShiftNodeType, generateCloudNode(cloudProvider, cloudInstanceType));
    }

    private List<OpenShiftNode> generateOpenShiftNodes(CloudProvider cloudProvider, OpenShiftNodeType openShiftNodeType,
                                                       CloudInstanceType cloudInstanceType, int count) {
        List<OpenShiftNode> openShiftNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            openShiftNodes.add(generateOpenShiftNode(cloudProvider, openShiftNodeType, cloudInstanceType));
        }
        return openShiftNodes;
    }

    /**
     * See the sizing recommendations for more details:
     * https://docs.openshift.com/container-platform/4.7/scalability_and_performance/recommended-host-practices.html
     */
    private OpenShiftNode generateOpenShiftMasterNode(CloudProvider cloudProvider, int workerNodeCount) {
        final int midWorkerNodes = 25;
        final int maxWorkerNodes = 100;

        if (workerNodeCount > maxWorkerNodes) {
            throw new IllegalArgumentException("Unsupported cluster size ("
                    + workerNodeCount + "). Please specify no more than " + maxWorkerNodes + " worker nodes.");
        }

        final int requiredCpu = workerNodeCount <= midWorkerNodes ? 4 : 8;
        final int requiredMemoryGiBs = workerNodeCount <= midWorkerNodes ? 16 : 32;

        CloudNode masterCloudNode = Arrays.stream(instanceTypesPerCloudProvider.get(cloudProvider))
                .filter(cloudNode -> cloudNode.getCloudInstanceType() == CloudInstanceType.GENERAL
                        && cloudNode.getCpuCores() == requiredCpu
                        && cloudNode.getMemoryGiBs() == requiredMemoryGiBs)
                .findFirst().orElseThrow(() -> new IllegalStateException("No suitable CloudNode found for a provider (" + cloudProvider + ")."));
        return new OpenShiftNode(OpenShiftNodeType.MASTER, masterCloudNode);
    }

    /**
     * A cluster consists of three master nodes and a number of worker nodes. All these nodes must come from a single
     * cloud provider.
     * @param cloudProvider cloud provider (AWS, Google, Azure)
     * @param workerNodeCount number of worker nodes
     * @param workerNodeInstanceType instance type of worker nodes. A cluster may contain nodes of difference instance
     *                               type, but the generator does not support that at the moment
     * @return OpenShiftCluster instance
     */
    OpenShiftCluster generateCluster(CloudProvider cloudProvider, int workerNodeCount, CloudInstanceType workerNodeInstanceType) {
        List<OpenShiftNode> openShiftNodes = new ArrayList<>();

        for (int i = 0; i < MASTER_NODE_COUNT; i++) {
            openShiftNodes.add(generateOpenShiftMasterNode(cloudProvider, workerNodeCount));
        }

        openShiftNodes.addAll(generateOpenShiftNodes(cloudProvider, OpenShiftNodeType.WORKER, workerNodeInstanceType, workerNodeCount));

        OpenShiftCluster openShiftCluster = new OpenShiftCluster(cloudProvider, openShiftNodes);
        return openShiftCluster;
    }
}
