package org.kie.baaas.optimizer.cli;

import javax.inject.Inject;

import org.kie.baaas.optimizer.generator.DataSet;
import org.kie.baaas.optimizer.generator.DataSetGenerator;
import org.kie.baaas.optimizer.io.DataSetIO;

import picocli.CommandLine;

@CommandLine.Command(name = "generate", description = "Generates an unsolved data set.")
public class GeneratorCommand implements Runnable {

    @CommandLine.Option(names = {"--cluster-count"}, required = true, description = "Number of OSD clusters to generate.")
    int clusterCount;

    @CommandLine.Option(names = {"--min-size"}, required = true, description = "Minimal size of every OSD cluster (worker node count).")
    int minSize;

    @CommandLine.Option(names = {"--max-size"}, required = true, description = "Maximal size of every OSD cluster (worker node count).")
    int maxSize;

    @CommandLine.Option(names = {"--max-utilization"}, defaultValue = "0.5", description = "Maximal utilization of every OSD cluster.")
    double maxUtilization;

    private final DataSetGenerator dataSetGenerator;

    private final DataSetIO dataSetIO;

    @Inject
    public GeneratorCommand(DataSetGenerator dataSetGenerator, DataSetIO dataSetIO) {
        this.dataSetGenerator = dataSetGenerator;
        this.dataSetIO = dataSetIO;
    }

    @Override
    public void run() {
        DataSet dataSet = dataSetGenerator.generateDataSet(clusterCount, minSize, maxSize, maxUtilization);
        String filename = String.format("dataset_%d_%d_%d_%.2f.json", clusterCount, minSize, maxSize, maxUtilization);
        dataSetIO.write(filename, dataSet);
    }
}
