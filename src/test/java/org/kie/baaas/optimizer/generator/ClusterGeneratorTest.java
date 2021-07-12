package org.kie.baaas.optimizer.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Random;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ClusterGeneratorTest {

    @Test
    void generateCluster() {
        Random randomMock = Mockito.mock(Random.class);
        when(randomMock.nextInt(anyInt())).thenReturn(1);
        ClusterGenerator clusterGenerator = new ClusterGenerator(randomMock);
        final int workerNodeCount = 2;
        OpenShiftCluster openShiftCluster = clusterGenerator.generateCluster(CloudProvider.AWS, workerNodeCount, CloudInstanceType.GENERAL);

        assertThat(openShiftCluster.getOpenShiftNodes()).hasSize(5);
        assertThat(openShiftCluster.getOpenShiftNodes().stream().filter(OpenShiftNode::isWorkerNode)).hasSize(workerNodeCount);
        assertThat(openShiftCluster.getCost()).isEqualTo(0.214 * 5, Offset.offset(0.1));
        assertThat(openShiftCluster.getTotalCpuCores()).isEqualTo(8);
        assertThat(openShiftCluster.getTotalMemoryGiBs()).isEqualTo(32);
        assertThat(openShiftCluster.getCloudProvider()).isEqualTo(CloudProvider.AWS);
    }
}
