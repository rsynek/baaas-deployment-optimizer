package org.kie.baaas.optimizer.solver;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.Resource;
import org.kie.baaas.optimizer.domain.ResourceCapacity;
import org.kie.baaas.optimizer.domain.ResourceRequirement;
import org.kie.baaas.optimizer.domain.Service;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ServiceDeploymentOptimizationTest {

    @Inject
    SolverFactory<ServiceDeploymentSchedule> solverFactory;

    @Test
    void one_cluster_redundant() {
        Resource cpu = new Resource(1L, 0.8);
        Resource memory = new Resource(2L, 0.7);

        // This cluster is redundant.
        OsdCluster cluster1 = new OsdCluster(1L, 100);

        // This cluster provides enough capacity for both services.
        OsdCluster cluster2 = new OsdCluster(2L, 50);

        ResourceCapacity cpuCluster1 = new ResourceCapacity(1L, cluster1, cpu, 8);
        ResourceCapacity cpuCluster2 = new ResourceCapacity(2L, cluster2, cpu, 4);
        ResourceCapacity memCluster1 = new ResourceCapacity(3L, cluster1, memory, 32);
        ResourceCapacity memCluster2 = new ResourceCapacity(4L, cluster2, memory, 16);

        Service service1 = new Service(1L, "decision1", cluster1);
        Service service2 = new Service(2L, "decision2", cluster2);

        ResourceRequirement cpuService1 = new ResourceRequirement(1L, service1, cpu, 1);
        ResourceRequirement cpuService2 = new ResourceRequirement(2L, service2, cpu, 1);
        ResourceRequirement memService1 = new ResourceRequirement(3L, service1, memory, 1);
        ResourceRequirement memService2 = new ResourceRequirement(4L, service2, memory, 1);

        List<OsdCluster> clusters = Arrays.asList(cluster1, cluster2);
        List<Resource> resources = Arrays.asList(cpu, memory);
        List<ResourceCapacity> resourceCapacities = Arrays.asList(cpuCluster1, cpuCluster2, memCluster1, memCluster2);
        List<ResourceRequirement> resourceRequirements = Arrays.asList(cpuService1, cpuService2, memService1, memService2);
        List<Service> services = Arrays.asList(service1, service2);

        ServiceDeploymentSchedule problem =
                new ServiceDeploymentSchedule(clusters, services, resources, resourceCapacities, resourceRequirements);

        Solver<ServiceDeploymentSchedule> solver = solverFactory.buildSolver();
        ServiceDeploymentSchedule solution = solver.solve(problem);

        Assertions.assertThat(solution.getScore().isFeasible()).isTrue();
        // The service1 has moved and only the cluster2 is in use.
        Assertions.assertThat(solution.getScore().getSoftScore()).isEqualTo(-51L);
    }

}
