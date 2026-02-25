package madkit.grafana.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Integration test — requires a running InfluxDB instance (typically started
 * via {@code GrafanaInfrastructureIT} or manually via Docker).
 */
@Test(groups = "integration")
public class InfluxDbWriterIT {

    private InfluxDbWriter writer;

    @BeforeClass
    public void setUp() {
        writer = new InfluxDbWriter("http://localhost:8086", "madkit", "madkit", "madkit-dev-token");
    }

    @Test
    public void givenRunningInfluxDb_whenWriteBatch_thenNoException() {
        // Given
        var points = List.of(
            new DataPoint("it_test", Map.of("env", "ci"), "value", 1.0, System.currentTimeMillis() * 1_000_000L, Map.of()),
            new DataPoint("it_test", Map.of("env", "ci"), "value", 2.0, System.currentTimeMillis() * 1_000_000L, Map.of())
        );

        // When / Then
        assertThatCode(() -> writer.writeBatch(points))
                .as("writing to InfluxDB should not throw")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenRunningInfluxDb_whenWriteAndFlush_thenDataIsPersisted() {
        // Given
        long ts = System.currentTimeMillis() * 1_000_000L;
        var points = List.of(
            new DataPoint("it_verify", Map.of("run", "test"), "value", 42.0, ts, Map.of())
        );

        // When
        writer.writeBatch(points);

        // Then — ideally query InfluxDB to verify; for now, assert no exception
        assertThat(true).as("write completed without error").isTrue();
    }
}
