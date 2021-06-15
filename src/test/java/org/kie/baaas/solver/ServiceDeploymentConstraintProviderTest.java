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
        Resource cpu = new Resource(1L);
        Resource memory = new Resource(2L);

        OsdCluster cluster1 = new OsdCluster(1L, 0.5);
        Node node1Cluster1 = new Node(1L, cluster1);
        OsdCluster cluster2 = new OsdCluster(2L, 0.5);
        Node node2Cluster2 = new Node(1L, cluster2);

        ResourceCapacity cpuCapacityCluster1 = new ResourceCapacity(node1Cluster1, cpu, 10L);
        ResourceCapacity memoryCapacityCluster1 = new ResourceCapacity(node1Cluster1, cpu, 10L);
        ResourceCapacity cpuCapacityCluster2 = new ResourceCapacity(node2Cluster2, cpu, 10L);
        ResourceCapacity memoryCapacityCluster2 = new ResourceCapacity(node2Cluster2, cpu, 10L);

        Service service1 = new Service(1L);
        Pod pod1 = new Pod(1L, "pod-1", service1);
        Service service2 = new Service(2L);
        Pod pod2 = new Pod(2L, "pod-2", service2);
        Service service3 = new Service(3L);
        Pod pod3 = new Pod(3L, "pod-3", service3);
        pod1.setNode(node1Cluster1);
        pod2.setNode(node1Cluster1);
        pod3.setNode(node2Cluster2);

        ResourceRequirement cpuRequirementService1 = new ResourceRequirement(pod1, cpu, 3L);
        ResourceRequirement memoryRequirementService1 = new ResourceRequirement(pod1, memory, 3L);
        ResourceRequirement cpuRequirementService2 = new ResourceRequirement(pod2, cpu, 30L);
        ResourceRequirement memoryRequirementService2 = new ResourceRequirement(pod2, memory, 30L);
        ResourceRequirement cpuRequirementService3 = new ResourceRequirement(pod3, cpu, 300L);
        ResourceRequirement memoryRequirementService3 = new ResourceRequirement(pod3, memory, 300L);

        /*
           cluster1: (10 - 3) + (10 - 3)  = no penalty
           cluster2: (10 - 300 - 30) + (10 - 300 - 30) = -640
         */
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::resourceCapacity)
                .given(cpu, memory, cluster1, cluster2, cpuCapacityCluster1, memoryCapacityCluster1, cpuCapacityCluster2,
                        memoryCapacityCluster2, service1, service2, service3, cpuRequirementService1, memoryRequirementService1,
                        cpuRequirementService2, memoryRequirementService2, cpuRequirementService3, memoryRequirementService3)
                .penalizesBy(640L);
    }
}
