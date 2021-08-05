package org.kie.baaas.optimizer.cli;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.Service;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.kie.baaas.optimizer.generator.DataSetGenerator;

public final class PrintingUtil {

    private PrintingUtil() {
        throw new UnsupportedOperationException("This class should not be instantiated.");
    }

    public static void printSolutionStatistics(ServiceDeploymentSchedule solution) {
        Map<OsdCluster, List<Service>> servicesByCluster = solution.getServices().stream()
                .filter(service -> service.getOsdCluster() != null)
                .collect(Collectors.groupingBy(Service::getOsdCluster));

        int clusterCount = servicesByCluster.size();
        double cost = ((double) servicesByCluster.keySet().stream()
                .collect(Collectors.summingLong(OsdCluster::getCostPerHour))) / DataSetGenerator.COST_PRECISION_MULTIPLIER;

        System.out.println("The solution uses " + clusterCount + " active OSD clusters that cost " + cost + " USD per hour.");
    }
}
