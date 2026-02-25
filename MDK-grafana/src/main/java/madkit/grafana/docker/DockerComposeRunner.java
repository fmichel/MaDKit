package madkit.grafana.docker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Encapsulates all interactions with the {@code docker compose} CLI.
 * <p>
 * This class is intentionally stateless: it receives the compose file path
 * on each call, making it easy to test and reuse.
 */
public class DockerComposeRunner {

    private static final Logger LOGGER = Logger.getLogger(DockerComposeRunner.class.getName());

    /**
     * Runs a {@code docker compose} command with the given arguments.
     *
     * @param composeFile absolute path to the docker-compose.yml file
     * @param args        docker compose sub-command and flags (e.g., "up", "-d")
     * @throws IOException          if the process cannot be started
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws IllegalStateException if the process exits with a non-zero code
     */
    public void run(Path composeFile, String... args) throws IOException, InterruptedException {
        List<String> command = buildCommand(composeFile, args);
        LOGGER.fine(() -> "Running: " + String.join(" ", command));
        int exitCode = new ProcessBuilder(command).inheritIO().start().waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("docker compose exited with code " + exitCode);
        }
    }

    /**
     * Checks whether the {@code docker} CLI is available on the system PATH.
     *
     * @return true if {@code docker --version} exits successfully
     */
    public boolean isDockerAvailable() {
        try {
            int exit = new ProcessBuilder("docker", "--version")
                    .redirectErrorStream(true).start().waitFor();
            return exit == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Builds the full command list for a docker compose invocation.
     *
     * @param composeFile the path to the docker-compose.yml file
     * @param args        additional docker compose arguments
     * @return the complete command list
     */
    private List<String> buildCommand(Path composeFile, String... args) {
        var command = new ArrayList<>(List.of("docker", "compose", "-f", composeFile.toString()));
        command.addAll(List.of(args));
        return command;
    }
}
