package org.kie.baaas.optimizer.solver;

import java.io.File;
import java.net.URISyntaxException;

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

    private final static String TEST_DATA_SET = "dataset_10_3_10_0.20_10_0.10.json";

    @Inject
    SolverFactory<ServiceDeploymentSchedule> solverFactory;

    @Inject
    DataSetIO dataSetIO;

    @Test
    void solverTest() throws URISyntaxException {
        DataSet dataSet = dataSetIO.read(new File(getClass().getResource(TEST_DATA_SET).toURI()));

        Solver<ServiceDeploymentSchedule> solver = solverFactory.buildSolver();
        ServiceDeploymentSchedule solution = solver.solve(dataSet.getServiceDeploymentSchedule());

        Assertions.assertThat(solution.getScore().isFeasible()).isTrue();
    }
}
