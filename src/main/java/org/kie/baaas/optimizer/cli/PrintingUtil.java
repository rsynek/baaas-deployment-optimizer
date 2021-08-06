package org.kie.baaas.optimizer.cli;

import org.kie.baaas.optimizer.generator.DataSet;

public final class PrintingUtil {

    private PrintingUtil() {
        throw new UnsupportedOperationException("This class should not be instantiated.");
    }

    public static void printSolutionStatistics(DataSet dataSet) {
        int clusterCount = dataSet.statistics().activeClusters();
        double cost = dataSet.statistics().costPerHour();

        System.out.println("The solution uses " + clusterCount + " active OSD clusters that cost " + cost + " USD per hour.");
    }
}
