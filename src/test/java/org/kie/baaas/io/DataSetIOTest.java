package org.kie.baaas.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.baaas.generator.DataSet;

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

        DataSet dataSet = dataSetIO.read(testDataSetPath);
        int expectedClusterCount = 4;
        assertThat(dataSet.getOpenShiftClusters()).hasSize(expectedClusterCount);
        assertThat(dataSet.getServiceDeploymentSchedule().getOsdClusters()).hasSize(expectedClusterCount);
        List<DataSet.ResourceUtilization> resourceUtilizationList = dataSet.getResourceUtilizationList();
        assertThat(resourceUtilizationList).hasSize(2);
        assertThat(resourceUtilizationList.get(0).utilization()).isBetween(0.0, 0.6);
        assertThat(resourceUtilizationList.get(1).utilization()).isBetween(0.0, 0.6);
    }
}
