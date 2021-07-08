package org.kie.baaas.optimizer.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

    public DataSet read(String filename) {
        Path inputFilePath = Path.of(DATA_FOLDER, Objects.requireNonNull(filename));
        return read(inputFilePath.toFile());
    }

    public DataSet read(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("The data set file (" + file.getAbsolutePath() + ") does not exist.");
        }

        try {
            return objectMapper.readValue(file, DataSet.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed reading data set file (" + file.getAbsolutePath() + ").", e);
        }
    }

    public void write(String filename, DataSet dataSet) {
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
            throw new IllegalStateException("Failed to write a data set to file (" + file.getAbsolutePath() + ").", e);
        }
    }
}
