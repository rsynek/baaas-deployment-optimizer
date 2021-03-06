package org.kie.baaas.optimizer.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.ResourceCapacity;
import org.kie.baaas.optimizer.domain.ResourceRequirement;
import org.kie.baaas.optimizer.domain.Service;
import org.kie.baaas.optimizer.generator.DataSet;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class DataSetIO {

    private static final String DATA_FOLDER = "data";

    private final ObjectMapper objectMapper;

    @Inject
    public DataSetIO(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DataSet read(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("The data set file (" + file.getAbsolutePath() + ") does not exist.");
        }

        DataSet dataSet;
        try {
            dataSet = objectMapper.readValue(file, DataSet.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed reading a data set file (" + file.getAbsolutePath() + ").", e);
        }

        // Establish bi-directional references.
        Map<Service, List<ResourceRequirement>> requirementsPerService = dataSet.getServiceDeploymentSchedule()
                .getResourceRequirements().stream()
                .collect(Collectors.groupingBy(ResourceRequirement::getService));
        for (Service service : dataSet.getServiceDeploymentSchedule().getServices()) {
            List<ResourceRequirement> resourceRequirements = requirementsPerService.get(service);
            Collections.sort(resourceRequirements);
            service.setResourceRequirements(resourceRequirements);
        }

        Map<OsdCluster, List<ResourceCapacity>> capacitiesPerCluster = dataSet.getServiceDeploymentSchedule()
                .getResourceCapacities().stream()
                .collect(Collectors.groupingBy(ResourceCapacity::getOsdCluster));
        for (OsdCluster osdCluster : dataSet.getServiceDeploymentSchedule().getOsdClusters()) {
            List<ResourceCapacity> resourceCapacities = capacitiesPerCluster.get(osdCluster);
            osdCluster.setResourceCapacities(resourceCapacities);
        }
        return dataSet;
    }

    public void write(String filename, DataSet dataSet) {
        Path dataFolderPath = Path.of(DATA_FOLDER);
        if (!Files.exists(dataFolderPath)) {
            try {
                Files.createDirectories(dataFolderPath);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create a " + DATA_FOLDER
                        + " folder to write a data set to a file (" + filename + ").", e);
            }
        }
        Path outputFilePath = Path.of(DATA_FOLDER, Objects.requireNonNull(filename));
        write(outputFilePath.toFile(), dataSet);
    }

    public void write(File file, DataSet dataSet) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(dataSet);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, dataSet);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write a data set to a file (" + file.getAbsolutePath() + ").", e);
        }
    }
}
