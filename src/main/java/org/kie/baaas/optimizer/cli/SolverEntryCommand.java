package org.kie.baaas.optimizer.cli;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true,
        subcommands = { GeneratorCommand.class, SolverInitializeCommand.class, SolverOptimizationCommand.class, ExportCommand.class })
public class SolverEntryCommand {

}
