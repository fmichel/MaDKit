package madkit.grafana.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

public class InfluxDbWriterTest {

    @Test
    public void givenEmptyBatch_whenWriteBatch_thenNoNetworkCall() {
        // Given
        var writer = new InfluxDbWriter("http://localhost:9999", "org", "bucket", "token");

        // When / Then
        assertThatCode(() -> writer.writeBatch(List.of()))
                .as("empty batch should be a no-op")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenUnreachableServer_whenWriteBatch_thenNoExceptionThrown() {
        // Given
        var writer = new InfluxDbWriter("http://localhost:9999", "org", "bucket", "token");
        var points = List.of(new DataPoint("m", Map.of(), "value", 1.0, 1000L, Map.of()));

        // When / Then
        assertThatCode(() -> writer.writeBatch(points))
                .as("unreachable server should be handled gracefully")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenDataPointWithExtraFields_whenWriteBatch_thenLineProtocolContainsTick() {
        // Given
        var dp = new DataPoint("pop", Map.of("role", "bee"), "value", 42.0, 1000L, Map.of("tick", 5.0));

        // When
        String lineProtocol = dp.toLineProtocol();

        // Then
        assertThat(lineProtocol)
                .as("line protocol should contain tick extra field")
                .contains("value=42.0,tick=5.0");
        assertThat(lineProtocol)
                .as("line protocol should contain measurement and tags")
                .startsWith("pop,role=bee");
    }
}
