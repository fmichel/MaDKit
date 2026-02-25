package madkit.grafana.docker;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import madkit.grafana.metrics.SimuMetrics;

/**
 * Integration test — requires Docker.
 * Starts and stops the full Grafana + InfluxDB stack.
 */
@Test(groups = "integration")
public class GrafanaInfrastructureIT {

    private GrafanaInfrastructure infra;
    private Path workDir;

    @BeforeClass
    public void setUp() throws Exception {
        workDir = Files.createTempDirectory("grafana-it-");
        infra = new GrafanaInfrastructure(workDir);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        if (infra != null) {
            infra.stop(true);
        }
    }

    @Test
    public void givenInfrastructure_whenStart_thenGrafanaIsRunning() throws Exception {
        // Given — infrastructure instance created in setUp

        // When
        infra.start();

        // Then
        assertThat(infra.isRunning())
                .as("Grafana should be running after start()")
                .isTrue();
    }

    @Test(dependsOnMethods = "givenInfrastructure_whenStart_thenGrafanaIsRunning")
    public void givenRunningInfra_whenStartAgain_thenIdempotent() throws Exception {
        // Given — already started

        // When — start again
        infra.start();

        // Then — still running, no error
        assertThat(infra.isRunning())
                .as("Grafana should still be running after idempotent start()")
                .isTrue();
    }

    @Test(dependsOnMethods = "givenRunningInfra_whenStartAgain_thenIdempotent")
    public void givenRunningInfra_whenStop_thenGrafanaIsNotRunning() throws Exception {
        // Given — running

        // When
        infra.stop(true);

        // Then
        assertThat(infra.isRunning())
                .as("Grafana should not be running after stop()")
                .isFalse();
    }

    @Test(dependsOnMethods = "givenInfrastructure_whenStart_thenGrafanaIsRunning")
    public void givenRunningInfrastructure_whenCreateMetrics_thenReturnsActiveMetrics() {
        // Given — infrastructure is running (started in previous test)

        // When
        SimuMetrics metrics = GrafanaInfrastructure.createMetrics(infra);

        // Then
        assertThat(metrics).as("running infra should produce active metrics").isNotSameAs(SimuMetrics.noOp());
        metrics.close();
    }
}
