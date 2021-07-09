package org.kie.baaas.optimizer.cli;

import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.kie.baaas.optimizer.generator.DataSet;
import org.kie.baaas.optimizer.io.DataSetIO;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;

import picocli.CommandLine;

@CommandLine.Command(name = "solve", description = "Solves either unsolved or initialized data sets.")
public class SolverOptimizationCommand implements Runnable {

    private final SolverManager<ServiceDeploymentSchedule, Long> solverManager;
    private final DataSetIO dataSetIO;

    @CommandLine.Parameters
    File datasetFileName;

    @Inject
    public SolverOptimizationCommand(SolverManager<ServiceDeploymentSchedule, Long> solverManager, DataSetIO dataSetIO) {
        this.solverManager = solverManager;
        this.dataSetIO = dataSetIO;
    }

    @Override
    public void run() {
        DataSet dataSet = dataSetIO.read(datasetFileName);

        long problemId = 1L;
        SolverJob<ServiceDeploymentSchedule, Long> solverJob = solverManager.solve(problemId, dataSet.getServiceDeploymentSchedule());

        ServiceDeploymentSchedule solution;
        try {
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted waiting for a solution.");
        } catch (ExecutionException e) {
            throw new IllegalStateException("Solver run has failed.", e.getCause());
        }

        dataSet.setServiceDeploymentSchedule(solution);
        dataSetIO.write(createOutputFile(datasetFileName.getName()), dataSet);
    }

    private String createOutputFile(String inputFileName) {
        int lastDotIndex = inputFileName.lastIndexOf('.');
        return inputFileName.substring(0, lastDotIndex) + "_solved" + inputFileName.substring(lastDotIndex);
    }
}
