package org.kie.baaas.optimizer.solver;

import static org.kie.baaas.optimizer.domain.OsdCluster.SINK;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sumLong;

import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.ResourceBalance;
import org.kie.baaas.optimizer.domain.ResourceCapacity;
import org.kie.baaas.optimizer.domain.ResourceRequirement;
import org.kie.baaas.optimizer.domain.Service;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

public class ServiceDeploymentConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                safeResourceCapacity(constraintFactory),
                serviceMoveCost(constraintFactory),
                clusterCost(constraintFactory),
                antiLoadBalancing(constraintFactory),
                matchingRegion(constraintFactory),
                exclusiveCluster(constraintFactory),
                balanceCost(constraintFactory),
                assignServices(constraintFactory)
        };
    }

    Constraint safeResourceCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .filter(service -> service.getOsdCluster() != SINK)
                .join(ResourceCapacity.class, Joiners.equal(Service::getOsdCluster, ResourceCapacity::getOsdCluster))
                .join(ResourceRequirement.class,
                        Joiners.equal((service, resourceCapacity) -> service.getId(), resourceRequirement -> resourceRequirement.getService().getId()),
                        Joiners.equal((service, resourceCapacity) -> resourceCapacity.getResource(), ResourceRequirement::getResource)
                )
                .groupBy(
                        (service, resourceCapacity, resourceRequirement) -> service.getOsdCluster(),
                        (service, resourceCapacity, resourceRequirement) -> resourceCapacity,
                        sumLong((pod, resourceCapacity, resourceRequirement) -> resourceRequirement.getAmount()))
                .filter((cluster, resourceCapacity, usagePerCluster) -> usagePerCluster > resourceCapacity.getSafeCapacity())
                .penalizeLong("safeResourceCapacity", HardMediumSoftLongScore.ONE_HARD,
                        (cluster, resourceCapacity, usagePerCluster) -> usagePerCluster - resourceCapacity.getSafeCapacity());
    }

    Constraint clusterCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .filter(service -> service.getOsdCluster() != SINK)
                .groupBy(Service::getOsdCluster)
                .penalizeLong("clusterCost", HardMediumSoftLongScore.ONE_SOFT, OsdCluster::getCostPerHour);
    }

    Constraint serviceMoveCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .filter(service -> service.getOsdCluster() != SINK)
                .filter(Service::isMoved)
                .penalize("serviceMoveCost", HardMediumSoftLongScore.ONE_SOFT);
    }

    Constraint matchingRegion(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .filter(service -> service.getOsdCluster() != SINK)
                .filter(service -> service.getRegion() != service.getOsdCluster().getRegion())
                .penalize("matchingRegion", HardMediumSoftLongScore.ONE_HARD);
    }

    Constraint exclusiveCluster(ConstraintFactory constraintFactory) {
        // Penalize for every service running on an exclusive cluster that does not belong to the customer owning the cluster.
        return constraintFactory.from(Service.class)
                .filter(service -> service.getOsdCluster() != SINK)
                .ifExists(Service.class,
                        Joiners.equal(Service::getOsdCluster),
                        Joiners.filtering((serviceA, serviceB) -> serviceB.getCustomer().isExclusive()
                                && serviceA.getCustomer() != serviceB.getCustomer()
                        )
                )
                .penalize("exclusiveCluster", HardMediumSoftLongScore.ONE_HARD);
    }

    Constraint antiLoadBalancing(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .filter(service -> service.getOsdCluster() != SINK)
                .join(ResourceCapacity.class, Joiners.equal(Service::getOsdCluster, ResourceCapacity::getOsdCluster))
                .join(ResourceRequirement.class,
                        Joiners.equal((service, resourceCapacity) -> service.getId(), resourceRequirement -> resourceRequirement.getService().getId()),
                        Joiners.equal((service, resourceCapacity) -> resourceCapacity.getResource(), ResourceRequirement::getResource)
                )
                .groupBy(
                        (service, resourceCapacity, resourceRequirement) -> service.getOsdCluster(),
                        (service, resourceCapacity, resourceRequirement) -> resourceCapacity,
                        sumLong((pod, resourceCapacity, resourceRequirement) -> resourceRequirement.getAmount()))
                .filter((cluster, resourceCapacity, usagePerCluster) -> usagePerCluster < resourceCapacity.getSafeCapacity())
                .penalizeLong("antiLoadBalancing", HardMediumSoftLongScore.ONE_SOFT,
                        (osdCluster, resourceCapacity, usagePerCluster) -> resourceCapacity.getSafeCapacity() - usagePerCluster);
    }

    Constraint balanceCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ResourceBalance.class)
                .join(constraintFactory.from(Service.class)
                        .filter(service -> service.getOsdCluster() != SINK)
                )
                .groupBy((resourceBalance, service) -> resourceBalance,
                        (resourceBalance, service) -> service.getOsdCluster(),
                        sumLong((resourceBalance, service) -> service.getUsage(resourceBalance.getOriginResource())),
                        sumLong((resourceBalance, service) -> service.getUsage(resourceBalance.getTargetResource())))
                .penalizeLong("balanceCost", HardMediumSoftLongScore.ONE_SOFT, this::balanceCost);
    }

    private long balanceCost(ResourceBalance resourceBalance, OsdCluster osdCluster, long originUsage, long targetUsage) {
        long originalAvailability = osdCluster.getMachineCapacity(resourceBalance.getOriginResource()).getSafeCapacity()
                - originUsage;
        long targetAvailability = osdCluster.getMachineCapacity(resourceBalance.getTargetResource()).getSafeCapacity() - targetUsage;
        long lackingAvailability = (resourceBalance.getMultiplicand() * originalAvailability) - targetAvailability;
        return Math.max(lackingAvailability, 0L);
    }

    Constraint assignServices(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .filter(service -> service.getOsdCluster() == SINK)
                .penalize("assignServices", HardMediumSoftLongScore.ONE_MEDIUM);
    }
}
