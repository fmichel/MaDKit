package madkit.grafana.docker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.logging.Logger;

import madkit.grafana.GrafanaConfig;
import madkit.grafana.dashboard.GrafanaConnection;
import madkit.grafana.metrics.CsvWriter;
import madkit.grafana.metrics.InfluxDbWriter;
import madkit.grafana.metrics.SimuMetrics;

/**
 * Orchestrates the Grafana + InfluxDB Docker Compose stack lifecycle.
 * <p>
 * Delegates Docker CLI calls to {@link DockerComposeRunner} and
 * health checking to {@link HealthChecker}. Resource extraction
 * is the only I/O this class performs directly.
 */
public class GrafanaInfrastructure {

    private static final Logger LOGGER = Logger.getLogger(GrafanaInfrastructure.class.getName());
    /** Maximum time to wait for each service to become healthy. */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    /** Classpath resources bundled in the JAR that must be extracted before starting Docker. */
    private static final String[] BUNDLED_RESOURCES = {
        "docker-compose.yml",
        "grafana/provisioning/datasources/influxdb.yml"
    };

    /** Working directory where Docker Compose files are extracted. */
    private final Path workDir;
    /** Host port for the Grafana UI container. */
    private final int grafanaPort;
    /** Host port for the InfluxDB API container. */
    private final int influxDbPort;
    /** Delegate for executing docker compose commands. */
    private final DockerComposeRunner dockerRunner;
    /** Delegate for polling HTTP health endpoints. */
    private final HealthChecker healthChecker;

    /**
     * Creates a new infrastructure manager with the given ports.
     *
     * @param workDir      working directory for extracted resources
     * @param grafanaPort  host port for Grafana
     * @param influxDbPort host port for InfluxDB
     */
    public GrafanaInfrastructure(Path workDir, int grafanaPort, int influxDbPort) {
        this(workDir, grafanaPort, influxDbPort, new DockerComposeRunner(), new HealthChecker());
    }

    /**
     * Constructor with injectable dependencies for testing.
     *
     * @param workDir       working directory for extracted resources
     * @param grafanaPort   host port for Grafana
     * @param influxDbPort  host port for InfluxDB
     * @param dockerRunner  the Docker Compose CLI runner
     * @param healthChecker the HTTP health checker
     */
    GrafanaInfrastructure(Path workDir, int grafanaPort, int influxDbPort,
                          DockerComposeRunner dockerRunner, HealthChecker healthChecker) {
        this.workDir = workDir;
        this.grafanaPort = grafanaPort;
        this.influxDbPort = influxDbPort;
        this.dockerRunner = dockerRunner;
        this.healthChecker = healthChecker;
    }

    /**
     * Creates a new infrastructure manager with default ports (Grafana: 3000, InfluxDB: 8086).
     *
     * @param workDir working directory for extracted resources
     */
    public GrafanaInfrastructure(Path workDir) {
        this(workDir, 3000, 8086);
    }

    /**
     * Extracts bundled resources, starts the Docker stack, and waits for health.
     *
     * @throws IOException          if resource extraction or Docker process fails
     * @throws InterruptedException if the current thread is interrupted
     * @throws IllegalStateException if Docker is not available or health check times out
     */
    public void start() throws IOException, InterruptedException {
        verifyDockerAvailable();
        extractBundledResources();
        startDockerStack();
        awaitInfrastructureReady();
        LOGGER.info(() -> "Grafana ready at http://localhost:" + grafanaPort);
    }

    /**
     * Stops the Docker stack.
     *
     * @param removeVolumes if true, removes Docker volumes (data is lost)
     * @throws IOException          if the Docker process fails
     * @throws InterruptedException if the current thread is interrupted
     */
    public void stop(boolean removeVolumes) throws IOException, InterruptedException {
        if (removeVolumes) {
            dockerRunner.run(composeFilePath(), "down", "-v");
        } else {
            dockerRunner.run(composeFilePath(), "down");
        }
    }

    /**
     * Checks whether the Grafana instance is reachable and healthy.
     *
     * @return true if Grafana's health endpoint returns HTTP 200
     */
    public boolean isRunning() {
        return healthChecker.isHealthy(grafanaHealthUrl());
    }

    // --- Static factory methods ---

    /**
     * Starts the Grafana + InfluxDB infrastructure using default configuration.
     * <p>
     * This is a convenience method for bootstrapping the full stack without
     * requiring an agent context. On failure, logs a warning and returns {@code null}.
     *
     * @return the started infrastructure, or {@code null} if startup failed
     */
    public static GrafanaInfrastructure startDefault() {
        var config = GrafanaConfig.defaults();
        var infra = new GrafanaInfrastructure(config.workDir());
        try {
            infra.start();
            return infra;
        } catch (Exception e) {
            LOGGER.warning(() -> "Grafana infrastructure unavailable: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a fully initialized {@link SimuMetrics} backed by InfluxDB and CSV writers,
     * using default configuration values from {@link GrafanaConfig#defaults()}.
     * <p>
     * Returns {@link SimuMetrics#noOp()} if the infrastructure is {@code null} or not running.
     *
     * @param infrastructure the running infrastructure (may be {@code null})
     * @return a metrics instance (never {@code null})
     */
    public static SimuMetrics createMetrics(GrafanaInfrastructure infrastructure) {
        if (infrastructure == null || !infrastructure.isRunning()) {
            return SimuMetrics.noOp();
        }
        try {
            var config = GrafanaConfig.defaults();
            var influx = new InfluxDbWriter(
                config.influxUrl(), config.influxOrg(), config.influxBucket(), config.influxToken());
            var csv = new CsvWriter(Path.of("simulation-metrics.csv"));
            return new SimuMetrics(influx, csv);
        } catch (Exception e) {
            LOGGER.warning(() -> "Metrics initialization failed: " + e.getMessage());
            return SimuMetrics.noOp();
        }
    }

    /**
     * Creates a {@link GrafanaConnection} using default configuration values.
     *
     * @return a Grafana connection (never {@code null})
     */
    public static GrafanaConnection createDefaultConnection() {
        var config = GrafanaConfig.defaults();
        return new GrafanaConnection(config.grafanaUrl(), config.grafanaUser(), config.grafanaPassword());
    }

    // --- Private methods with single responsibility each ---

    /**
     * Throws if Docker CLI is not on the PATH.
     */
    private void verifyDockerAvailable() {
        if (!dockerRunner.isDockerAvailable()) {
            throw new IllegalStateException(
                "Docker is not available. Install Docker and ensure 'docker' is on the system PATH.");
        }
    }

    /**
     * Creates the work directory and copies all bundled resources.
     *
     * @throws IOException if a resource cannot be extracted
     */
    private void extractBundledResources() throws IOException {
        Files.createDirectories(workDir);
        for (String resource : BUNDLED_RESOURCES) {
            extractResource(resource);
        }
    }

    /**
     * Runs {@code docker compose up -d}.
     *
     * @throws IOException          if the Docker process fails
     * @throws InterruptedException if the current thread is interrupted
     */
    private void startDockerStack() throws IOException, InterruptedException {
        dockerRunner.run(composeFilePath(), "up", "-d");
    }

    /**
     * Polls both InfluxDB and Grafana health endpoints.
     *
     * @throws InterruptedException if the current thread is interrupted
     */
    private void awaitInfrastructureReady() throws InterruptedException {
        healthChecker.awaitHealthy(influxDbHealthUrl(), DEFAULT_TIMEOUT);
        healthChecker.awaitHealthy(grafanaHealthUrl(), DEFAULT_TIMEOUT);
    }

    /**
     * Copies a single classpath resource to the work directory.
     *
     * @param resourceName the resource name relative to {@code /grafana-infra/}
     * @throws IOException if the resource cannot be found or copied
     */
    private void extractResource(String resourceName) throws IOException {
        Path target = workDir.resolve(resourceName);
        Files.createDirectories(target.getParent());
        try (InputStream in = getClass().getResourceAsStream("/grafana-infra/" + resourceName)) {
            if (in == null) {
                throw new IOException("Bundled resource not found: " + resourceName);
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Returns the resolved path to docker-compose.yml.
     *
     * @return the compose file path within the work directory
     */
    private Path composeFilePath() {
        return workDir.resolve("docker-compose.yml");
    }

    /**
     * Returns the Grafana health endpoint URL.
     *
     * @return the Grafana health URL
     */
    private String grafanaHealthUrl() {
        return "http://localhost:" + grafanaPort + "/api/health";
    }

    /**
     * Returns the InfluxDB health endpoint URL.
     *
     * @return the InfluxDB health URL
     */
    private String influxDbHealthUrl() {
        return "http://localhost:" + influxDbPort + "/health";
    }
}
