package org.kie.baaas.solver;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.baaas.domain.OsdCluster;
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
    void maximumCapacity() {
        Resource cpu = new Resource(1L);
        Resource memory = new Resource(2L);

        OsdCluster cluster1 = new OsdCluster(1L, 0.5);
        OsdCluster cluster2 = new OsdCluster(2L, 0.5);

        ResourceCapacity cpuCapacityCluster1 = new ResourceCapacity(cluster1, cpu, 10L);
        ResourceCapacity memoryCapacityCluster1 = new ResourceCapacity(cluster1, cpu, 10L);
        ResourceCapacity cpuCapacityCluster2 = new ResourceCapacity(cluster2, cpu, 10L);
        ResourceCapacity memoryCapacityCluster2 = new ResourceCapacity(cluster2, cpu, 10L);

        Service service1 = new Service(1L,"service-1");
        Service service2 = new Service(2L, "service-2");
        Service service3 = new Service(3L, "service-3");
        service1.setOsdCluster(cluster1);
        service2.setOsdCluster(cluster2);
        service3.setOsdCluster(cluster2);

        ResourceRequirement cpuRequirementService1 = new ResourceRequirement(service1, cpu, 3L);
        ResourceRequirement memoryRequirementService1 = new ResourceRequirement(service1, memory, 3L);
        ResourceRequirement cpuRequirementService2 = new ResourceRequirement(service2, cpu, 30L);
        ResourceRequirement memoryRequirementService2 = new ResourceRequirement(service2, memory, 30L);
        ResourceRequirement cpuRequirementService3 = new ResourceRequirement(service3, cpu, 300L);
        ResourceRequirement memoryRequirementService3 = new ResourceRequirement(service3, memory, 300L);

        /*
           cluster1: (10 - 3) + (10 - 3)  = no penalty
           cluster2: (10 - 300 - 30) + (10 - 300 - 30) = -640
         */
        constraintVerifier.verifyThat(ServiceDeploymentConstraintProvider::maximumCapacity)
                .given(cpu, memory, cluster1, cluster2, cpuCapacityCluster1, memoryCapacityCluster1, cpuCapacityCluster2,
                        memoryCapacityCluster2, service1, service2, service3, cpuRequirementService1, memoryRequirementService1,
                        cpuRequirementService2, memoryRequirementService2, cpuRequirementService3, memoryRequirementService3)
                .penalizesBy(640L);
    }
}
