package madkit.grafana.docker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.testng.annotations.Test;

import madkit.grafana.metrics.SimuMetrics;

public class GrafanaInfrastructureTest {

    @Test
    public void givenNoDocker_whenStart_thenThrowsIllegalState() {
        // Given
        var fakeRunner = new DockerComposeRunner() {
            @Override
            public boolean isDockerAvailable() {
                return false;
            }
        };
        var infra = new GrafanaInfrastructure(
                Path.of("test-workdir"), 3000, 8086, fakeRunner, new HealthChecker());

        // When / Then
        assertThatThrownBy(infra::start)
                .as("start should fail when Docker is unavailable")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Docker is not available");
    }

    @Test
    public void givenUnusedPort_whenCreated_thenIsRunningReturnsFalse() {
        // Given / When — use a non-standard port unlikely to have anything running
        var infra = new GrafanaInfrastructure(Path.of("test"), 39999, 39998);

        // Then
        assertThat(infra.isRunning())
                .as("not running when nothing is on the specified port")
                .isFalse();
    }

    @Test
    public void givenInfra_whenStartCalled_thenBothHealthEndpointsAreChecked() {
        // Given
        var healthCheckedUrls = new java.util.ArrayList<String>();
        var fakeRunner = new DockerComposeRunner() {
            @Override
            public boolean isDockerAvailable() {
                return true;
            }

            @Override
            public void run(Path composeFile, String... args) {
                /* no-op */
            }
        };
        var fakeChecker = new HealthChecker() {
            @Override
            public boolean isHealthy(String url) {
                return true;
            }

            @Override
            public void awaitHealthy(String url, java.time.Duration timeout) {
                healthCheckedUrls.add(url);
            }
        };
        var infra = new GrafanaInfrastructure(
                Path.of("test-workdir"), 3000, 8086, fakeRunner, fakeChecker);

        // When / Then
        assertThatCode(infra::start).doesNotThrowAnyException();

        assertThat(healthCheckedUrls)
                .as("both InfluxDB and Grafana health URLs should be checked")
                .containsExactly(
                        "http://localhost:8086/health",
                        "http://localhost:3000/api/health");
    }

    @Test
    public void givenNullInfrastructure_whenCreateMetrics_thenReturnsNoOp() {
        // Given
        GrafanaInfrastructure infra = null;

        // When
        SimuMetrics metrics = GrafanaInfrastructure.createMetrics(infra);

        // Then
        assertThat(metrics).as("null infra should produce no-op metrics").isSameAs(SimuMetrics.noOp());
    }

    @Test
    public void givenNotRunningInfrastructure_whenCreateMetrics_thenReturnsNoOp() {
        // Given
        var infra = new GrafanaInfrastructure(Path.of("nonexistent"), 39999, 39998);

        // When
        SimuMetrics metrics = GrafanaInfrastructure.createMetrics(infra);

        // Then
        assertThat(metrics).as("not-running infra should produce no-op metrics").isSameAs(SimuMetrics.noOp());
    }
}
