package org.kie.baaas.optimizer.solver;

import java.util.Arrays;

import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.Service;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;

public class ServiceSpreadMove extends AbstractMove<ServiceDeploymentSchedule> {

    private Service[] services;
    private OsdCluster originalCluster;
    private OsdCluster[] targetClusters;

    public ServiceSpreadMove(Service[] services, OsdCluster[] targetClusters) {
        if (services.length != targetClusters.length) {
            throw new IllegalArgumentException("The number of cluster ("
                    + targetClusters.length
                    + ") must match the number of services ("
                    + services.length
                    + ").");
        }
        this.services = services;
        this.originalCluster = services.length == 0 ? null : services[0].getOsdCluster();
        this.targetClusters = targetClusters;
    }

    @Override
    protected AbstractMove<ServiceDeploymentSchedule> createUndoMove(ScoreDirector<ServiceDeploymentSchedule> scoreDirector) {
        OsdCluster [] clusters = new OsdCluster[services.length];
        Arrays.fill(clusters, originalCluster);
        return new ServiceSpreadMove(services, clusters);
    }

    @Override
    protected void doMoveOnGenuineVariables(ScoreDirector<ServiceDeploymentSchedule> scoreDirector) {
        for (int i = 0; i < services.length; i++) {
            Service service = services[i];
            scoreDirector.beforeVariableChanged(service, "osdCluster");
            service.setOsdCluster(targetClusters[i]);
            scoreDirector.afterVariableChanged(service, "osdCluster");
        }
    }

    /**
     * The move is doable if the targetClusters array contains at least a single cluster other than the originalCluster.
     */
    @Override
    public boolean isMoveDoable(ScoreDirector<ServiceDeploymentSchedule> scoreDirector) {
        if (services.length <= 0) {
            return false;
        }
        for (OsdCluster osdCluster : targetClusters) {
            if (osdCluster != originalCluster) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ServiceSpreadMove{" +
                "services=" + Arrays.toString(services) +
                ", originalCluster=" + originalCluster +
                ", targetClusters=" + Arrays.toString(targetClusters) +
                '}';
    }
}
