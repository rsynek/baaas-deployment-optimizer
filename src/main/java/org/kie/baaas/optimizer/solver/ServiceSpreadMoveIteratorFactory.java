package org.kie.baaas.optimizer.solver;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.Service;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;

public class ServiceSpreadMoveIteratorFactory implements MoveIteratorFactory<ServiceDeploymentSchedule, ServiceSpreadMove> {

    @Override
    public long getSize(ScoreDirector<ServiceDeploymentSchedule> scoreDirector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<ServiceSpreadMove> createOriginalMoveIterator(ScoreDirector<ServiceDeploymentSchedule> scoreDirector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<ServiceSpreadMove> createRandomMoveIterator(ScoreDirector<ServiceDeploymentSchedule> scoreDirector, Random workingRandom) {
        Map<OsdCluster, List<Service>> servicesByCluster = scoreDirector.getWorkingSolution().getServices()
                .stream()
                .collect(Collectors.groupingBy(Service::getOsdCluster));
        return new SpreadServiceMoveIterator(servicesByCluster, scoreDirector.getWorkingSolution().getOsdClusters(), workingRandom);
    }

    private static final class SpreadServiceMoveIterator implements Iterator<ServiceSpreadMove> {
        private final Map<OsdCluster, List<Service>> servicesByCluster;
        private final List<OsdCluster> osdClusters;
        private final Random workingRandom;

        public SpreadServiceMoveIterator(Map<OsdCluster, List<Service>> servicesByCluster, List<OsdCluster> osdClusters,
                                         Random workingRandom) {
            this.servicesByCluster = servicesByCluster;
            this.osdClusters = osdClusters;
            this.workingRandom = workingRandom;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public ServiceSpreadMove next() {
            OsdCluster randomCluster = osdClusters.get(workingRandom.nextInt(osdClusters.size()));
            List<Service> servicesInSelectedCluster = servicesByCluster.get(randomCluster);
            if (servicesInSelectedCluster == null || servicesInSelectedCluster.isEmpty()) {
                return new ServiceSpreadMove(new Service[]{}, new OsdCluster[] {});
            }
            final int clustersSize = osdClusters.size();
            final int servicesSize = servicesInSelectedCluster.size();
            final int selectedServicesSize = workingRandom.nextInt(servicesSize);
            Service[] selectedServices = new Service[selectedServicesSize];
            OsdCluster[] selectedClusters = new OsdCluster[selectedServicesSize];
            for (int i = 0; i < selectedServicesSize; i++) {
                selectedServices[i] = servicesInSelectedCluster.get(workingRandom.nextInt(servicesSize));
                selectedClusters[i] = osdClusters.get(workingRandom.nextInt(clustersSize));
            }
            return new ServiceSpreadMove(selectedServices, selectedClusters);
        }
    }
}
