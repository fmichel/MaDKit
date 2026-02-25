package madkit.grafana.docker;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class DockerComposeRunnerTest {

    @Test
    public void givenRunner_whenIsDockerAvailableCalled_thenReturnsBooleanWithoutException() {
        // Given
        var runner = new DockerComposeRunner();

        // When
        boolean available = runner.isDockerAvailable();

        // Then
        assertThat(available).as("isDockerAvailable returns a boolean").isIn(true, false);
    }
}
