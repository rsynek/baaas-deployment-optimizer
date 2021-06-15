package org.kie.baaas.solver;

import org.kie.baaas.domain.Pod;
import org.kie.baaas.domain.ResourceCapacity;
import org.kie.baaas.domain.ResourceRequirement;
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
                resourceCapacity(constraintFactory)
                //cost(constraintFactory)
        };
    }

    Constraint resourceCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Pod.class)
                .join(ResourceCapacity.class, Joiners.equal(Pod::getNode, ResourceCapacity::getNode))
                .join(ResourceRequirement.class,
                        Joiners.equal((pod, resourceCapacity) -> pod, ResourceRequirement::getPod),
                        Joiners.equal((pod, resourceCapacity) -> resourceCapacity.getResource(), ResourceRequirement::getResource)
                )
                .groupBy(
                        (pod, resourceCapacity, resourceRequirement) -> pod.getNode(),
                        (pod, resourceCapacity, resourceRequirement) -> resourceCapacity,
                        sumLong((service, resourceCapacity, resourceRequirement) -> resourceRequirement.getAmount()))
                .filter((cluster, resourceCapacity, usagePerCluster) -> usagePerCluster > resourceCapacity.getCapacity())
                .penalizeLong("resourceCapacity", HardSoftLongScore.ONE_HARD,
                        (cluster, resourceCapacity, usagePerCluster) -> usagePerCluster - resourceCapacity.getCapacity());
    }

    Constraint serviceBelongsToSingleCluster(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUniquePair(Pod.class, Joiners.equal(Pod::getService))
                .filter((pod1, pod2) -> pod1.getNode().getOsdCluster() != pod2.getNode().getOsdCluster())
                .penalize("serviceBelongsToSingleCluster", HardSoftLongScore.ONE_HARD);
    }

    Constraint serviceSpansOverMultipleNodes(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUniquePair(Pod.class,
                Joiners.equal(Pod::getService),
                Joiners.equal(Pod::getNode))
                .penalize("serviceSpansOverMultipleNodes", HardSoftLongScore.ONE_HARD);
    }

    Constraint clusterMaintenanceCost(ConstraintFactory constraintFactory) {
        throw new UnsupportedOperationException();
    }

    // Avoid moving services between clusters if not necessary
    Constraint serviceMoveCost(ConstraintFactory constraintFactory) {
        throw new UnsupportedOperationException();
    }

    // Avoid moving pods between nodes if not necessary
    Constraint processMoveCost(ConstraintFactory constraintFactory) {
        throw new UnsupportedOperationException();
    }
}
