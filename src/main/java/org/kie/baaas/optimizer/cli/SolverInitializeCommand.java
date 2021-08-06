package org.kie.baaas.optimizer.cli;

import java.io.File;
import java.util.Collections;
import java.util.Objects;

import javax.inject.Inject;

import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.Service;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.kie.baaas.optimizer.generator.DataSet;
import org.kie.baaas.optimizer.io.DataSetIO;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.solver.SolverConfig;

import picocli.CommandLine;

@CommandLine.Command(name = "init", description = "Initializes the solution by running a Construction Heuristic.")
public class SolverInitializeCommand implements Runnable {

    @CommandLine.Parameters(index = "0")
    File inputFile;

    @CommandLine.Option(names = {"-o", "--output-file"}, description = "Output file to write results to.")
    File outputFile;

    private final SolverConfig solverConfig;
    private final DataSetIO dataSetIO;

    @Inject
    public SolverInitializeCommand(SolverConfig solverConfig, DataSetIO dataSetIO) {
        this.solverConfig = solverConfig;
        this.dataSetIO = dataSetIO;
    }

    @Override
    public void run() {
        Objects.requireNonNull(inputFile);
        DataSet dataSet = dataSetIO.read(inputFile);
        ServiceDeploymentSchedule problem = Objects.requireNonNull(dataSet.getServiceDeploymentSchedule());
        Solver<ServiceDeploymentSchedule> solver = buildSolverWithConstructionHeuristicOnly();
        ServiceDeploymentSchedule solution = solver.solve(problem);
        setOriginalClusterAssignments(solution);
        dataSet.setServiceDeploymentSchedule(solution);

        if (outputFile != null) {
            dataSetIO.write(outputFile, dataSet);
        } else {
            dataSetIO.write(createDefaultOutputFile(inputFile.getName(), dataSet), dataSet);
        }

        PrintingUtil.printSolutionStatistics(dataSet);
    }

    private void setOriginalClusterAssignments(ServiceDeploymentSchedule solution) {
        for (Service service : solution.getServices()) {
            // Remove the SINK reference.
            if (service.getOsdCluster().getId().equals(OsdCluster.SINK.getId())) {
                service.setOriginalOsdCluster(null);
            }
            service.setOriginalOsdCluster(service.getOsdCluster());
        }
    }

    private Solver<ServiceDeploymentSchedule> buildSolverWithConstructionHeuristicOnly() {
        PhaseConfig<?> chPhaseConfig = new ConstructionHeuristicPhaseConfig();
        solverConfig.setPhaseConfigList(Collections.singletonList(chPhaseConfig));
        solverConfig.setTerminationConfig(null);
        SolverFactory<ServiceDeploymentSchedule> solverFactory = SolverFactory.create(solverConfig);
        return solverFactory.buildSolver();
    }

    private String createDefaultOutputFile(String inputFileName, DataSet dataSet) {
        String suffix = String.format("_%d_%.2f", dataSet.statistics().activeClusters(), dataSet.statistics().costPerHour());
        int lastDotIndex = inputFileName.lastIndexOf('.');
        return inputFileName.substring(0, lastDotIndex) + "_simple" + suffix + inputFileName.substring(lastDotIndex);
    }
}
