package org.kie.baaas.optimizer.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private List<SolverJob<ServiceDeploymentSchedule, Long>> solverJobs;

    @CommandLine.Parameters
    File[] datasetFileNames;

    @Inject
    public SolverOptimizationCommand(SolverManager<ServiceDeploymentSchedule, Long> solverManager, DataSetIO dataSetIO) {
        this.solverManager = solverManager;
        this.dataSetIO = dataSetIO;
    }

    @Override
    public void run() {
        CountDownLatch allJobsFinished = new CountDownLatch(datasetFileNames.length);
        solverJobs = new ArrayList<>(datasetFileNames.length);
        long problemId = 1L;
        for (File datasetFileName : datasetFileNames) {
            DataSet dataSet = dataSetIO.read(datasetFileName);
            SolverJob<ServiceDeploymentSchedule, Long> solverJob
                    = solverManager.solve(problemId, dataSet.getServiceDeploymentSchedule(), serviceDeploymentSchedule -> {
                dataSet.setServiceDeploymentSchedule(serviceDeploymentSchedule);
                dataSetIO.write(createOutputFile(datasetFileName.getName()), dataSet);
                allJobsFinished.countDown();
            }, (problemIdErr, throwable) -> {
                throwable.printStackTrace();
                allJobsFinished.countDown();
            });
            solverJobs.add(solverJob);
        }

        try {
            allJobsFinished.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted waiting for optimization to complete.");
        }
    }

    private String createOutputFile(String inputFileName) {
        int lastDotIndex = inputFileName.lastIndexOf('.');
        return inputFileName.substring(0, lastDotIndex) + "_solved" + inputFileName.substring(lastDotIndex);
    }
}
