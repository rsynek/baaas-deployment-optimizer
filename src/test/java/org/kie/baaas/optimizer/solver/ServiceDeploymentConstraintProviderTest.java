package org.kie.baaas.optimizer.solver;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.Region;
import org.kie.baaas.optimizer.domain.Resource;
import org.kie.baaas.optimizer.domain.ResourceCapacity;
import org.kie.baaas.optimizer.domain.ResourceRequirement;
import org.kie.baaas.optimizer.domain.Service;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ServiceDeploymentConstraintProviderTest {

    @Inject
    ConstraintVerifier<ServiceDeploymentConstraintProvider, ServiceDeploymentSchedule> constraintVerifier;

    @Test
    void resourceCapacity() {
        Resource cpu = new Resource(1L, 0.8);
        Resource memory = new Resource(2L, 0.8);

        OsdCluster cluster1 = new OsdCluster(1L, 5, null);
        OsdCluster cluster2 = new OsdCluster(2L, 5, null);

        ResourceCapacity cpuCapacityCluster1 = new ResourceCapacity(cluster1, cpu, 10L);
        ResourceCapacity memoryCapacityCluster1 = new ResourceCapacity(cluster1, memory, 10L);
        ResourceCapacity cpuCapacityCluster2 = new ResourceCapacity(cluster2, cpu, 10L);
        ResourceCapacity memoryCapacityCluster2 = new ResourceCapacity(cluster2, memory, 10L);

        Service service1 = new Service(1L, "decision1", null);
        Service service2 = new Service(2L, "decision2", null);
        Service service3 = new Service(3L, "decision3", null);

        service1.setOsdCluster(cluster1);
        service2.setOsdCluster(cluster2);
        service3.setOsdCluster(cluster2);

        ResourceRequirement cpuRequirementservice1 = new ResourceRequirement(service1, cpu, 3L);
        ResourceRequirement memoryRequirementservice1 = new ResourceRequirement(service1, memory, 3L);
        ResourceRequirement cpuRequirementservice2 = new ResourceRequirement(service2, cpu, 30L);
        ResourceRequirement memoryRequirementservice2 = new ResourceRequirement(service2, memory, 30L);
        ResourceRequirement cpuRequirementservice3 = new ResourceRequirement(service3, cpu, 300L);
        ResourceRequirement memoryRequirementservice3 = new ResourceRequirement(service3, memory, 300L);

        /*
           cluster1: (10 * 0.8 - 3) + (10 * 0.8 - 3)  = no penalty
           cluster2: (10 * 0.8 - 300 - 30) + (10 * 0.8 - 300 - 30) = -644
         */
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::safeResourceCapacity)
                .given(cpu, memory, cluster1, cluster2, cpuCapacityCluster1,
                        memoryCapacityCluster1, cpuCapacityCluster2, memoryCapacityCluster2, service1, service2, service3,
                        cpuRequirementservice1, memoryRequirementservice1, cpuRequirementservice2,
                        memoryRequirementservice2, cpuRequirementservice3, memoryRequirementservice3)
                .penalizesBy(644L);
    }

    @Test
    void clusterCost() {
        OsdCluster cluster1 = new OsdCluster(1L, 1, null);
        OsdCluster cluster2 = new OsdCluster(2L, 10, null);
        OsdCluster cluster3 = new OsdCluster(3L, 100, null);

        Service service1 = new Service(1L, "decision1", null);
        Service service2 = new Service(2L, "decision2", null);
        service1.setOsdCluster(cluster1);
        service2.setOsdCluster(cluster2);

        // There are services running only on the clusters 1 and 2.
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::clusterCost)
                .given(cluster1, cluster2, cluster3, service1, service2)
                .penalizesBy(1 + 10);
    }

    @Test
    void serviceMoveCost() {
        OsdCluster cluster1 = new OsdCluster(1L, 1, null);
        OsdCluster cluster2 = new OsdCluster(2L, 1, null);

        Service service1 = new Service(1L, "decision1", null, cluster1);
        Service service2 = new Service(2L, "decision2", null, cluster1);
        Service service3 = new Service(3L, "decision3", null, cluster1);
        service1.setOsdCluster(cluster1);
        service2.setOsdCluster(cluster2);
        service3.setOsdCluster(cluster2);

        // The service2 moves to a different cluster.
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::serviceMoveCost)
                .given(cluster1, cluster2, service1, service2, service3)
                .penalizesBy(2);
    }

    @Test
    void matchingRegion() {
        Region region1 = new Region(1L, "us-west");
        Region region2 = new Region(2L, "us-east");
        OsdCluster cluster1 = new OsdCluster(1L, 1, region1);
        OsdCluster cluster2 = new OsdCluster(2L, 1, region2);

        Service service1 = new Service(1L, "decision1", region1);
        Service service2 = new Service(2L, "decision2", region1);
        service1.setOsdCluster(cluster1);
        service2.setOsdCluster(cluster2);

        // The service2 should be deployed to the cluster1 instead of the cluster2 due to a matching region.
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::matchingRegion)
                .given(cluster1, cluster2, service1, service2)
                .penalizesBy(1);
    }
}
