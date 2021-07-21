package org.kie.baaas.optimizer.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

@PlanningSolution
public class ServiceDeploymentSchedule extends AbstractIdentifiable {
    private List<OsdCluster> osdClusters;
    private List<Service> services;
    private List<Resource> resources;
    private List<ResourceCapacity> resourceCapacities;
    private List<ResourceRequirement> resourceRequirements;
    private List<Region> regions;
    private List<Customer> customers;
    private HardMediumSoftLongScore score;

    public ServiceDeploymentSchedule() {
        // Required by Jackson.
    }

    public ServiceDeploymentSchedule(List<OsdCluster> osdClusters, List<Service> services, List<Resource> resources,
                                     List<ResourceCapacity> resourceCapacities, List<ResourceRequirement> resourceRequirements,
                                     List<Region> regions, List<Customer> customers) {
        this.osdClusters = osdClusters;
        this.services = services;
        this.resources = resources;
        this.resourceCapacities = resourceCapacities;
        this.resourceRequirements = resourceRequirements;
        this.regions = regions;
        this.customers = customers;
    }

    @ValueRangeProvider(id = "clustersRange")
    @ProblemFactCollectionProperty
    public List<OsdCluster> getOsdClusters() {
        List<OsdCluster> clustersWithVirtualValue = new ArrayList<>(osdClusters);
        clustersWithVirtualValue.add(OsdCluster.SINK);
        return clustersWithVirtualValue;
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

    @ProblemFactCollectionProperty
    public List<Region> getRegions() {
        return regions;
    }

    @ProblemFactCollectionProperty
    public List<Customer> getCustomers() {
        return customers;
    }

    @PlanningScore
    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }
}
