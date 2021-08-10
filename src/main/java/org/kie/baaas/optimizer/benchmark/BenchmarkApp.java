package org.kie.baaas.optimizer.benchmark;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.kie.baaas.optimizer.io.DataSetIO;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.persistence.jackson.api.OptaPlannerJacksonModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.runtime.QuarkusApplication;

public class BenchmarkApp implements QuarkusApplication {

    private static final String DATA_FOLDER = "data/uniform";
    private static final String DATA_SET_100_20 = "dataset_100_3_20_0.20_1_0.10.json";
    private static final String DATA_SET_100_40 = "dataset_100_3_20_0.40_1_0.10.json";
    private static final String DATA_SET_100_60 = "dataset_100_3_20_0.60_1_0.10.json";

    public static void main(String[] args) {
        new BenchmarkApp().run(args);
    }

    @Override
    public int run(String... args) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(OptaPlannerJacksonModule.createModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());

        DataSetIO dataSetIO = new DataSetIO(objectMapper);
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(
                "solverBenchmarkConfig.xml", getClass().getClassLoader());

        List<ServiceDeploymentSchedule> dataSets = Arrays.asList(DATA_SET_100_20, DATA_SET_100_40, DATA_SET_100_60)
                .stream()
                .map(dataSetName -> dataSetIO.read(new File(DATA_FOLDER, dataSetName)))
                .map(dataSet -> dataSet.getServiceDeploymentSchedule())
                .collect(Collectors.toList());

        PlannerBenchmark plannerBenchmark = benchmarkFactory.buildPlannerBenchmark(dataSets);
        plannerBenchmark.benchmark();
        return 0;
    }
}
