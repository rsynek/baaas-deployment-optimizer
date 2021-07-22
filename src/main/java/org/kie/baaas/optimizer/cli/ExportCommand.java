package org.kie.baaas.optimizer.cli;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.kie.baaas.optimizer.domain.OsdCluster;
import org.kie.baaas.optimizer.domain.Resource;
import org.kie.baaas.optimizer.domain.ResourceCapacity;
import org.kie.baaas.optimizer.domain.ResourceRequirement;
import org.kie.baaas.optimizer.domain.Service;
import org.kie.baaas.optimizer.domain.ServiceDeploymentSchedule;
import org.kie.baaas.optimizer.generator.DataSet;
import org.kie.baaas.optimizer.io.DataSetIO;

import picocli.CommandLine;

@CommandLine.Command(name = "export", description = "Exports a data set to a different file format.")
public class ExportCommand implements Runnable {

    @CommandLine.Parameters(index = "0")
    File inputFile;

    @CommandLine.Option(names = {"-o", "--output-file"}, description = "Output file to write results to.")
    File outputFile;

    private final DataSetIO dataSetIO;

    @Inject
    public ExportCommand(DataSetIO dataSetIO) {
        this.dataSetIO = dataSetIO;
    }

    @Override
    public void run() {
        Objects.requireNonNull(inputFile);
        DataSet dataSet = dataSetIO.read(inputFile);
        ServiceDeploymentSchedule schedule = Objects.requireNonNull(dataSet.getServiceDeploymentSchedule());

        if (outputFile == null) {
            outputFile = new File(inputFile.getParentFile(), createDefaultOutputFileName(inputFile.getName()));
        }
        try {
            Files.writeString(outputFile.toPath(), toCsvString(schedule));
        } catch (IOException e) {
            throw new UncheckedIOException("Exporting a deployment schedule to a file (" + outputFile.getAbsolutePath() + ") has failed.", e);
        }
    }

    public String toCsvString(ServiceDeploymentSchedule schedule) {
        Map<OsdCluster, List<ResourceCapacity>> capacitiesPerCluster = schedule.getResourceCapacities().stream()
                .collect(Collectors.groupingBy(ResourceCapacity::getOsdCluster));
        Map<OsdCluster, List<Service>> servicesPerCluster = schedule.getServices()
                .stream()
                .filter(service -> service.getOsdCluster() != null)
                .collect(Collectors.groupingBy(Service::getOsdCluster));
        Map<Long, List<ResourceRequirement>> requirementsPerServiceId = schedule.getResourceRequirements()
                .stream()
                .collect(Collectors.groupingBy(resourceRequirement -> resourceRequirement.getService().getId()));
        StringBuilder sb = new StringBuilder();
        for (OsdCluster osdCluster : schedule.getOsdClusters()) {
            sb.append("Cluster ").append(osdCluster.getId());
            Map<Resource, Long> resourceCapacities = capacitiesPerCluster.get(osdCluster).stream()
                    .collect(Collectors.groupingBy(ResourceCapacity::getResource, Collectors.summingLong(ResourceCapacity::getCapacity)));

            Map<Resource, Long> usagePerResource = schedule.getResourceRequirements().stream()
                    .filter(resourceRequirement -> resourceRequirement.getService().getOsdCluster() != null)
                    .filter(resourceRequirement -> resourceRequirement.getService().getOsdCluster().getId().equals(osdCluster.getId()))
                    .collect(Collectors.groupingBy(ResourceRequirement::getResource, Collectors.summingLong(ResourceRequirement::getAmount)));
            List<Service> services = servicesPerCluster.get(osdCluster);

            String usageSummary = schedule.getResources().stream()
                    .map(resource -> {
                        Long usage = usagePerResource.get(resource);
                        if (usage == null) {
                            usage = 0L;
                        }
                        return resource.getId()
                                + ": "
                                + printResourceValue(usage)
                                + " / "
                                + printResourceValue(resourceCapacities.get(resource));

                    })
                    .collect(Collectors.joining(" "));
            sb.append(" [ ").append(usageSummary).append(" ], ");
            sb.append(printServices(services, requirementsPerServiceId));
            sb.append("\n");
        }

        return sb.toString();
    }

    private String printServices(List<Service> services, Map<Long, List<ResourceRequirement>> requirementsPerPodId) {
        StringBuilder sb = new StringBuilder();
        if (services != null) {
            String podSummary = services.stream().map(pod -> {
                Map<Resource, Long> resourceRequirements = requirementsPerPodId.get(pod.getId()).stream()
                        .collect(Collectors.groupingBy(ResourceRequirement::getResource, Collectors.summingLong(ResourceRequirement::getAmount)));
                return pod.getName() + " " + printResources(resourceRequirements);
            }).collect(Collectors.joining(","));
            sb.append(podSummary);
        }
        return sb.toString();
    }

    private String printResources(Map<Resource, Long> resources) {
        String resourcesSummary = resources.entrySet().stream().map(resourceValueEntry ->
                resourceValueEntry.getKey().getId() + ": " + printResourceValue(resourceValueEntry.getValue()))
                .collect(Collectors.joining(" "));
        return "[ " + resourcesSummary + " ]";
    }

    private String printResourceValue(long value) {
        return String.format("%.2f", (double) value / 1_000_000_000L);
    }

    private String createDefaultOutputFileName(String inputFileName) {
        int lastDotIndex = inputFileName.lastIndexOf('.');
        return inputFileName.substring(0, lastDotIndex) + ".csv";
    }
}
