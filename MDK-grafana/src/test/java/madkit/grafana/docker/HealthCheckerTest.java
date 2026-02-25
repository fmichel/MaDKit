package madkit.grafana.docker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.testng.annotations.Test;

public class HealthCheckerTest {

    @Test
    public void givenUnreachableUrl_whenIsHealthy_thenReturnsFalse() {
        // Given
        var checker = new HealthChecker();

        // When
        boolean healthy = checker.isHealthy("http://localhost:19999/nonexistent");

        // Then
        assertThat(healthy).as("unreachable URL should not be healthy").isFalse();
    }

    @Test
    public void givenUnreachableUrl_whenAwaitHealthyWithShortTimeout_thenThrowsIllegalState() {
        // Given
        var checker = new HealthChecker();

        // When / Then
        assertThatThrownBy(() -> checker.awaitHealthy("http://localhost:19999/nonexistent", Duration.ofSeconds(2)))
                .as("awaitHealthy should throw on timeout")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("timed out");
    }
}
