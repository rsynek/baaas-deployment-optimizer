package org.kie.baaas.optimizer.solver;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.kie.baaas.optimizer.generator.DataSet;
import org.kie.baaas.optimizer.io.DataSetIO;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ServiceDeploymentOptimizationTest {

    private final static String TEST_DATA_SET = "dataset_10_3_10_0.50_initialized.json";

    @Inject
    SolverFactory<ServiceDeploymentSchedule> solverFactory;

    @Inject
    DataSetIO dataSetIO;

   // @Test
    void solverTest() throws URISyntaxException {
        DataSet dataSet = dataSetIO.read(new File(getClass().getResource(TEST_DATA_SET).toURI()));
        printSchedule(dataSet.getServiceDeploymentSchedule());

        Solver<ServiceDeploymentSchedule> solver = solverFactory.buildSolver();
        ServiceDeploymentSchedule solution = solver.solve(dataSet.getServiceDeploymentSchedule());

        printSchedule(solution);

        //Assertions.assertThat(solution.getScore().isFeasible()).isTrue();
    }

    private void printSchedule(ServiceDeploymentSchedule schedule) {
        schedule.getServices().forEach(service -> {
            String cluster = service.getOsdCluster() == null ? "null" : service.getOsdCluster().toString();
            System.out.println(service + " -> " + cluster);
        });
    }

}
