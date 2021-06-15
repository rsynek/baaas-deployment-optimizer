package org.kie.baaas.domain;

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
    private List<Node> nodes;
    private List<Service> services;
    private List<Pod> pods;
    private List<Resource> resources;
    private List<ResourceCapacity> resourceCapacities;
    private List<ResourceRequirement> resourceRequirements;
    private HardSoftLongScore score;

    public ServiceDeploymentSchedule() {
        // Required by Jackson.
    }

    public ServiceDeploymentSchedule(List<OsdCluster> osdClusters, List<Node> nodes, List<Service> services,
                                     List<Pod> pods, List<Resource> resources, List<ResourceCapacity> resourceCapacities,
                                     List<ResourceRequirement> resourceRequirements) {
        this.osdClusters = osdClusters;
        this.nodes = nodes;
        this.services = services;
        this.pods = pods;
        this.resources = resources;
        this.resourceCapacities = resourceCapacities;
        this.resourceRequirements = resourceRequirements;
    }

    @ProblemFactCollectionProperty
    public List<OsdCluster> getOsdClusters() {
        return osdClusters;
    }

    @ValueRangeProvider(id = "nodesRange")
    @ProblemFactCollectionProperty
    public List<Node> getNodes() {
        return nodes;
    }

    @ProblemFactCollectionProperty
    public List<Service> getServices() {
        return services;
    }

    @PlanningEntityCollectionProperty
    public List<Pod> getPods() {
        return pods;
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
