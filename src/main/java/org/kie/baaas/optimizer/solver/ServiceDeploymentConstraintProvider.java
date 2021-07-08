package org.kie.baaas.optimizer.solver;

import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.ResourceCapacity;
import org.kie.baaas.optimizer.domain.ResourceRequirement;
import org.kie.baaas.optimizer.domain.Service;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sumLong;

public class ServiceDeploymentConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                safeResourceCapacity(constraintFactory),
                serviceMoveCost(constraintFactory),
                clusterCost(constraintFactory)
        };
    }

    Constraint safeResourceCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .join(ResourceCapacity.class, Joiners.equal(Service::getOsdCluster, ResourceCapacity::getOsdCluster))
                .join(ResourceRequirement.class,
                        Joiners.equal((service, resourceCapacity) -> service.getId(), resourceRequirement -> resourceRequirement.getService().getId()),
                        Joiners.equal((service, resourceCapacity) -> resourceCapacity.getResource(), ResourceRequirement::getResource)
                )
                .groupBy(
                        (service, resourceCapacity, resourceRequirement) -> service.getOsdCluster(),
                        (service, resourceCapacity, resourceRequirement) -> resourceCapacity,
                        sumLong((pod, resourceCapacity, resourceRequirement) -> resourceRequirement.getAmount()))
                .filter((cluster, resourceCapacity, usagePerNode) -> usagePerNode > resourceCapacity.getSafeCapacity())
                .penalizeLong("safeResourceCapacity", HardSoftLongScore.ONE_HARD,
                        (cluster, resourceCapacity, usagePerNode) -> usagePerNode - resourceCapacity.getSafeCapacity());
    }

    Constraint clusterCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .groupBy(service -> service.getOsdCluster())
                .penalizeLong("clusterCost", HardSoftLongScore.ONE_SOFT, OsdCluster::getCostPerHour);
    }

    // Avoid moving services between clusters if not necessary.
    Constraint serviceMoveCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .filter(service -> service.isMoved())
                .penalize("serviceMoveCost", HardSoftLongScore.ONE_SOFT);
    }
}
