package org.kie.baaas.solver;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.baaas.domain.Node;
import org.kie.baaas.domain.OsdCluster;
import org.kie.baaas.domain.Pod;
import org.kie.baaas.domain.Resource;
import org.kie.baaas.domain.ResourceCapacity;
import org.kie.baaas.domain.ResourceRequirement;
import org.kie.baaas.domain.Service;
import org.kie.baaas.domain.ServiceDeploymentSchedule;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ServiceDeploymentConstraintProviderTest {

    @Inject
    ConstraintVerifier<ServiceDeploymentConstraintProvider, ServiceDeploymentSchedule> constraintVerifier;

    @Test
    void resourceCapacity() {
        Resource cpu = new Resource(1L, 0.8);
        Resource memory = new Resource(2L ,0.8);

        OsdCluster cluster1 = new OsdCluster(1L, 5);
        Node node1 = new Node(1L, cluster1);
        OsdCluster cluster2 = new OsdCluster(2L, 5);
        Node node2 = new Node(2L, cluster2);

        ResourceCapacity cpuCapacityNode1 = new ResourceCapacity(node1, cpu, 10L);
        ResourceCapacity memoryCapacityNode1 = new ResourceCapacity(node1, cpu, 10L);
        ResourceCapacity cpuCapacityNode2 = new ResourceCapacity(node2, cpu, 10L);
        ResourceCapacity memoryCapacityNode2 = new ResourceCapacity(node2, cpu, 10L);

        Service service1 = new Service(1L);
        Pod pod1 = new Pod(1L, "pod-1", service1);
        Service service2 = new Service(2L);
        Pod pod2 = new Pod(2L, "pod-2", service2);
        Service service3 = new Service(3L);
        Pod pod3 = new Pod(3L, "pod-3", service3);
        pod1.setNode(node1);
        pod2.setNode(node2);
        pod3.setNode(node2);

        ResourceRequirement cpuRequirementPod1 = new ResourceRequirement(pod1, cpu, 3L);
        ResourceRequirement memoryRequirementPod1 = new ResourceRequirement(pod1, memory, 3L);
        ResourceRequirement cpuRequirementPod2 = new ResourceRequirement(pod2, cpu, 30L);
        ResourceRequirement memoryRequirementPod2 = new ResourceRequirement(pod2, memory, 30L);
        ResourceRequirement cpuRequirementPod3 = new ResourceRequirement(pod3, cpu, 300L);
        ResourceRequirement memoryRequirementPod3 = new ResourceRequirement(pod3, memory, 300L);

        /*
           node1: (10 * 0.8 - 3) + (10 * 0.8 - 3)  = no penalty
           node2: (10 * 0.8 - 300 - 30) + (10 * 0.8 - 300 - 30) = -644
         */
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::safeResourceCapacity)
                .given(cpu, memory, cluster1, cluster2, node1, node2, cpuCapacityNode1,
                        memoryCapacityNode1, cpuCapacityNode2, memoryCapacityNode2, pod1, pod2, pod3, service1,
                        service2, service3, cpuRequirementPod1, memoryRequirementPod1, cpuRequirementPod2,
                        memoryRequirementPod2, cpuRequirementPod3, memoryRequirementPod3)
                .penalizesBy(644L);
    }

    @Test
    void singleClusterPerService() {
        OsdCluster cluster1 = new OsdCluster(1L, 5);
        Node node1Cluster1 = new Node(1L, cluster1);
        OsdCluster cluster2 = new OsdCluster(2L, 5);
        Node node2Cluster2 = new Node(2L, cluster2);

        Service service1 = new Service(1L);
        Pod pod1 = new Pod(1L, "pod-1", service1);
        Pod pod2 = new Pod(2L, "pod-2", service1);
        Service service2 = new Service(2L);
        Pod pod3 = new Pod(3L, "pod-3", service2);

        pod1.setNode(node1Cluster1);
        pod2.setNode(node2Cluster2);
        pod3.setNode(node2Cluster2);

        // The service1 spans over two clusters.
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::singleClusterPerService)
                .given(cluster1, cluster2, node1Cluster1, node2Cluster2, service1, service2, pod1, pod2, pod3)
                .penalizesBy(1);
    }

    @Test
    void serviceSpansOverMultipleNodes() {
        OsdCluster cluster1 = new OsdCluster(1L, 5);
        Node node1Cluster1 = new Node(1L, cluster1);
        OsdCluster cluster2 = new OsdCluster(2L, 5);
        Node node2Cluster2 = new Node(1L, cluster2);

        Service service1 = new Service(1L);
        Pod pod1 = new Pod(1L, "pod-1", service1);
        Pod pod2 = new Pod(2L, "pod-2", service1);
        Service service2 = new Service(2L);
        Pod pod3 = new Pod(3L, "pod-3", service2);
        Pod pod4 = new Pod(4L, "pod-4", service2);

        pod1.setNode(node1Cluster1);
        pod2.setNode(node2Cluster2);
        pod3.setNode(node2Cluster2);
        pod4.setNode(node2Cluster2);

        // The service2 has all the pods on a single machine.
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::serviceSpansOverMultipleNodes)
                .given(cluster1, cluster2, node1Cluster1, node2Cluster2, service1, service2, pod1, pod2, pod3, pod4)
                .penalizesBy(1);
    }

    @Test
    void clusterCost() {
        OsdCluster cluster1 = new OsdCluster(1L, 1);
        Node node1Cluster1 = new Node(1L, cluster1);
        OsdCluster cluster2 = new OsdCluster(2L, 10);
        Node node2Cluster2 = new Node(2L, cluster2);
        OsdCluster cluster3 = new OsdCluster(3L, 100);
        Node node3Cluster3 = new Node(3L, cluster3);

        Service service1 = new Service(1L);
        Pod pod1 = new Pod(1L, "pod-1", service1);
        Service service2 = new Service(2L);
        Pod pod2 = new Pod(2L, "pod-2", service2);
        pod1.setNode(node1Cluster1);
        pod2.setNode(node2Cluster2);

        // There are pods running only on the clusters 1 and 2.
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::clusterCost)
                .given(cluster1, cluster2, cluster3, node1Cluster1, node2Cluster2, node3Cluster3, service1, service2,
                        pod1, pod2)
                .penalizesBy(1 + 10);
    }

    @Test
    void serviceMoveCost() {
        OsdCluster cluster1 = new OsdCluster(1L, 1);
        Node node1Cluster1 = new Node(1L, cluster1);
        OsdCluster cluster2 = new OsdCluster(2L, 1);
        Node node2Cluster2 = new Node(2L, cluster2);

        Service service1 = new Service(1L);
        Pod pod1 = new Pod(1L, "pod-1", service1, node1Cluster1);
        Service service2 = new Service(2L);
        Pod pod2 = new Pod(2L, "pod-2", service2, node1Cluster1);
        Pod pod3 = new Pod(3L, "pod-3", service2, node1Cluster1);
        pod1.setNode(node1Cluster1);
        pod2.setNode(node2Cluster2);
        pod3.setNode(node2Cluster2);

        // The service2 moves to a different cluster.
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::serviceMoveCost)
                .given(cluster1, cluster2, node1Cluster1, node2Cluster2, service1, service2, pod1, pod2, pod3)
                .penalizesBy(2);
    }

    @Test
    void podMoveCost() {
        OsdCluster cluster1 = new OsdCluster(1L, 1);
        Node node1 = new Node(1L, cluster1);
        Node node2 = new Node(2L, cluster1);

        Service service1 = new Service(1L);
        Pod pod1 = new Pod(1L, "pod-1", service1, node1);
        Pod pod2 = new Pod(2L, "pod-2", service1, node1);
        Pod pod3 = new Pod(3L, "pod-3", service1, node1);
        pod1.setNode(node1);
        pod2.setNode(node1);
        pod3.setNode(node2);

        // The pod3 moves to a different node.
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::podMoveCost)
                .given(cluster1, node1, node2, service1, pod1, pod2, pod3)
                .penalizesBy(1);
    }
}
