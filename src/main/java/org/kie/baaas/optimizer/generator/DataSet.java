package org.kie.baaas.optimizer.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kie.baaas.optimizer.domain.Resource;
import org.kie.baaas.optimizer.domain.ResourceCapacity;
import org.kie.baaas.optimizer.domain.ResourceRequirement;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DataSet {

    private List<OpenShiftCluster> openShiftClusters;
    private ServiceDeploymentSchedule serviceDeploymentSchedule;

    public DataSet() {
        // Required by Jackson.
    }

    public DataSet(List<OpenShiftCluster> openShiftClusters, ServiceDeploymentSchedule serviceDeploymentSchedule) {
        this.openShiftClusters = openShiftClusters;
        this.serviceDeploymentSchedule = serviceDeploymentSchedule;
    }

    @JsonIgnore
    public List<ResourceUtilization> getResourceUtilizationList() {
        Map<Resource, Long> totalCapacityPerResourceId = serviceDeploymentSchedule.getResourceCapacities().stream()
                .collect(Collectors.groupingBy(ResourceCapacity::getResource,
                        Collectors.summingLong(ResourceCapacity::getCapacity)));

        Map<Resource, Long> totalRequirementPerResourceId = serviceDeploymentSchedule.getResourceRequirements().stream()
                .collect(Collectors.groupingBy(ResourceRequirement::getResource,
                        Collectors.summingLong(ResourceRequirement::getAmount)));

        if (totalCapacityPerResourceId.size() != totalRequirementPerResourceId.size()) {
            throw new IllegalStateException("The number of resources in resource requirements ("
                    + totalRequirementPerResourceId.size()
                    + ") does not match the number of resource in resource capacities ("
                    + totalCapacityPerResourceId.size() + ").");
        }

        List<ResourceUtilization> resourceUtilizationList = new ArrayList<>();
        for (Map.Entry<Resource, Long> requirementEntry : totalRequirementPerResourceId.entrySet()) {
            Resource resource = requirementEntry.getKey();
            Long totalCapacity = totalCapacityPerResourceId.get(resource);
            if (totalCapacity == null) {
                throw new IllegalStateException("No capacity entry found for a resource (" + resource + ").");
            }
            resourceUtilizationList.add(new ResourceUtilization(resource, totalCapacity, requirementEntry.getValue()));
        }
        return resourceUtilizationList;
    }

    public static final class ResourceUtilization {
        private final Resource resource;
        private final long capacity;
        private final long requirement;

        private ResourceUtilization(Resource resource, long capacity, long requirement) {
            this.resource = resource;
            this.capacity = capacity;
            this.requirement = requirement;
        }

        public double utilization() {
            return (double) requirement / (double) capacity;
        }

        public Resource getResource() {
            return this.resource;
        }
    }

    public List<OpenShiftCluster> getOpenShiftClusters() {
        return openShiftClusters;
    }

    public ServiceDeploymentSchedule getServiceDeploymentSchedule() {
        return serviceDeploymentSchedule;
    }

    public void setServiceDeploymentSchedule(ServiceDeploymentSchedule serviceDeploymentSchedule) {
        this.serviceDeploymentSchedule = serviceDeploymentSchedule;
    }
}
