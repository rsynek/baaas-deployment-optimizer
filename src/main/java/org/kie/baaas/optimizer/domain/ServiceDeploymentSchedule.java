package org.kie.baaas.optimizer.domain;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

@PlanningSolution
public class ServiceDeploymentSchedule {

    private List<OsdCluster> osdClusters;
    private List<Service> services;
    private List<Resource> resources;
    private List<ResourceCapacity> resourceCapacities;
    private List<ResourceRequirement> resourceRequirements;
    private HardSoftLongScore score;

    public ServiceDeploymentSchedule() {
        // Required by Jackson.
    }

    public ServiceDeploymentSchedule(List<OsdCluster> osdClusters, List<Service> services, List<Resource> resources,
                                     List<ResourceCapacity> resourceCapacities, List<ResourceRequirement> resourceRequirements) {
        this.osdClusters = osdClusters;
        this.services = services;
        this.resources = resources;
        this.resourceCapacities = resourceCapacities;
        this.resourceRequirements = resourceRequirements;
    }

    @ValueRangeProvider(id = "clustersRange")
    @ProblemFactCollectionProperty
    public List<OsdCluster> getOsdClusters() {
        return osdClusters;
    }

    @PlanningEntityCollectionProperty
    public List<Service> getServices() {
        return services;
    }

    @ProblemFactCollectionProperty
    public List<Resource> getResources() {
        return resources;
    }

    @ProblemFactCollectionProperty
    public List<ResourceCapacity> getResourceCapacities() {
        return resourceCapacities;
    }

    @ProblemFactCollectionProperty
    public List<ResourceRequirement> getResourceRequirements() {
        return resourceRequirements;
    }

    @PlanningScore
    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }
}
