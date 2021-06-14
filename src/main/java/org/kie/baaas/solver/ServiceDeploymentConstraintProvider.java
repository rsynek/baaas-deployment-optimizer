package org.kie.baaas.solver;

import org.kie.baaas.domain.ResourceCapacity;
import org.kie.baaas.domain.Service;
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
                maximumCapacity(constraintFactory)
                //cost(constraintFactory)
        };
    }

    Constraint maximumCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Service.class)
                .join(ResourceCapacity.class, Joiners.equal(Service::getOsdCluster, ResourceCapacity::getOsdCluster))
                .join(ResourceRequirement.class,
                        Joiners.equal((service, resourceCapacity) -> service, ResourceRequirement::getService),
                        Joiners.equal((service, resourceCapacity) -> resourceCapacity.getResource(), ResourceRequirement::getResource)
                )
                .groupBy(
                        (service, resourceCapacity, resourceRequirement) -> service.getOsdCluster(),
                        (service, resourceCapacity, resourceRequirement) -> resourceCapacity,
                        sumLong((service, resourceCapacity, resourceRequirement) -> resourceRequirement.getAmount()))
                .filter((cluster, resourceCapacity, usagePerCluster) -> usagePerCluster > resourceCapacity.getCapacity())
                .penalizeLong("resourceCapacity", HardSoftLongScore.ONE_HARD,
                        (cluster, resourceCapacity, usagePerCluster) -> usagePerCluster - resourceCapacity.getCapacity());

    }
/*
    return factory.from(MrMachineCapacity.class)
            .join(MrProcessAssignment.class,
                  equal(MrMachineCapacity::getMachine, MrProcessAssignment::getMachine))
            .groupBy((machineCapacity, processAssignment) -> machineCapacity.getMachine(),
            (machineCapacity, processAssignment) -> machineCapacity,
    sumLong((machineCapacity, processAssignment) -> processAssignment
            .getUsage(machineCapacity.getResource())))
            .filter(((machine, machineCapacity, usage) -> machineCapacity.getMaximumCapacity() < usage))
            .penalizeLong(MrConstraints.MAXIMUM_CAPACITY, HardSoftLongScore.ONE_HARD,
                        (machine, machineCapacity, usage) -> usage - machineCapacity.getMaximumCapacity());
   */

    Constraint cost(ConstraintFactory constraintFactory) {
        throw new UnsupportedOperationException();
    }
}
