package org.kie.baaas.optimizer.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.baaas.optimizer.generator.DataSet;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DataSetIOTest {

    private static final String TEST_DATASET = "test_dataset.json";

    @Inject
    DataSetIO dataSetIO;

    @Test
    void readDataSet() {
        File file = new File(getClass().getResource(TEST_DATASET).getFile());
        Path testDataSetPath = file.toPath();

        DataSet dataSet = dataSetIO.read(testDataSetPath.toFile());
        int expectedClusterCount = 2;
        Assertions.assertThat(dataSet.getOpenShiftClusters()).hasSize(expectedClusterCount);
        assertThat(dataSet.getServiceDeploymentSchedule().getOsdClusters()).hasSize(expectedClusterCount);
        assertThat(dataSet.getServiceDeploymentSchedule().getServices()).hasSize(12);
        List<DataSet.ResourceUtilization> resourceUtilizationList = dataSet.statistics().getResourceUtilizationList();
        assertThat(resourceUtilizationList).hasSize(2);
        assertThat(resourceUtilizationList.get(0).utilization()).isBetween(0.0, 0.3);
        assertThat(resourceUtilizationList.get(1).utilization()).isBetween(0.0, 0.3);
    }
}
