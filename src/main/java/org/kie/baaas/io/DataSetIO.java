package org.kie.baaas.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.baaas.generator.DataSet;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class DataSetIO {

    private static final String DATA_FOLDER = "data";

    private final ObjectMapper objectMapper;

    @Inject
    public DataSetIO(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DataSet read(String inputFileName) {
        Path inputFilePath = Path.of(DATA_FOLDER, "input", Objects.requireNonNull(inputFileName));
        return read(inputFilePath);
    }

    public DataSet read(Path inputFilePath) {
        if (!Files.exists(inputFilePath)) {
            throw new IllegalArgumentException("The input file (" + inputFilePath + ") does not exist.");
        }

        try {
            return objectMapper.readValue(inputFilePath.toFile(), DataSet.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed reading inputSolutionFile (" + inputFilePath + ").", e);
        }
    }

    public void write(long problemId, DataSet dataSet) {
        String fileName = "solution_" + problemId + ".json";
        Path outputFilePath = Path.of(DATA_FOLDER, "output", fileName);
        write(outputFilePath, dataSet);
    }

    public void write(Path outputFilePath, DataSet dataSet) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFilePath.toFile(), dataSet);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to write a data set to file (" + outputFilePath + ").", e);
        }
    }
}
