package org.kie.baaas.solver;

import org.kie.baaas.domain.OsdCluster;
import org.kie.baaas.domain.Pod;
import org.kie.baaas.domain.ResourceCapacity;
import org.kie.baaas.domain.ResourceRequirement;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sumLong;

import java.util.function.Function;

public class ServiceDeploymentConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                resourceCapacity(constraintFactory),
                singleClusterPerService(constraintFactory),
                serviceSpansOverMultipleNodes(constraintFactory),
                clusterCost(constraintFactory)
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

    Constraint singleClusterPerService(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUniquePair(Pod.class, Joiners.equal(Pod::getService))
                .filter((pod1, pod2) -> pod1.getNode().getOsdCluster() != pod2.getNode().getOsdCluster())
                .penalize("singleClusterPerService", HardSoftLongScore.ONE_HARD);
    }

    // TODO: maybe too strict? Could we allow 2 pods of a service running on the same node provided there are other pods
    //  running on different machines(s)?
    Constraint serviceSpansOverMultipleNodes(ConstraintFactory constraintFactory) {
        return constraintFactory.fromUniquePair(Pod.class,
                Joiners.equal(Pod::getService),
                Joiners.equal(Pod::getNode))
                .penalize("serviceSpansOverMultipleNodes", HardSoftLongScore.ONE_HARD);
    }

    Constraint clusterCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(OsdCluster.class)
                .ifExists(Pod.class, Joiners.equal(Function.identity(), pod -> pod.getNode().getOsdCluster()))
                .penalizeLong("clusterCost", HardSoftLongScore.ONE_SOFT, OsdCluster::getCostPerHour);
    }

    // Avoid moving services between clusters if not necessary.
    Constraint serviceMoveCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Pod.class)
                .filter(pod -> pod.isMoved()
                        && pod.getNode().getOsdCluster() != pod.getOriginalNode().getOsdCluster())
                .groupBy(Pod::getService, ConstraintCollectors.count())
                .penalize("serviceMoveCost", HardSoftLongScore.ONE_SOFT, (service, podCount) -> podCount);
    }

    // Avoid moving pods between nodes if not necessary.
    Constraint podMoveCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Pod.class)
                .filter(Pod::isMoved)
                .penalize("podMoveCost", HardSoftLongScore.ONE_SOFT);
    }
}
