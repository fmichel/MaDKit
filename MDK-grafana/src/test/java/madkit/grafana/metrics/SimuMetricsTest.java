package madkit.grafana.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

public class SimuMetricsTest {

    /**
     * A test double that captures written batches for assertion.
     */
    static class CapturingWriter implements MetricsWriter {
        final List<DataPoint> captured = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void writeBatch(List<DataPoint> points) {
            captured.addAll(points);
        }

        @Override
        public void flush() { }

        @Override
        public void close() { }
    }

    @Test
    public void givenMetricsWithCapturingWriter_whenRecordAndClose_thenAllPointsAreFlushed() throws Exception {
        // Given
        var writer = new CapturingWriter();
        var metrics = new SimuMetrics(writer);

        // When
        metrics.record("test.metric", 42.0);
        metrics.record("test.metric", 43.0, Map.of("tag", "v1"));
        metrics.increment("test.counter");
        metrics.close();

        // Then
        assertThat(writer.captured)
                .as("all three data points should be flushed on close")
                .hasSize(3);
        assertThat(writer.captured.get(0).measurement()).isEqualTo("test.metric");
        assertThat(writer.captured.get(0).value()).isEqualTo(42.0);
        assertThat(writer.captured.get(2).value())
                .as("increment records value 1.0")
                .isEqualTo(1.0);
    }

    @Test
    public void givenClosedMetrics_whenRecord_thenPointIsDiscarded() throws Exception {
        // Given
        var writer = new CapturingWriter();
        var metrics = new SimuMetrics(writer);
        metrics.close();

        // When
        metrics.record("after.close", 1.0);

        // Then
        assertThat(writer.captured)
                .as("no points should be recorded after close")
                .isEmpty();
    }

    @Test
    public void givenMultipleWriters_whenRecord_thenAllWritersReceiveData() throws Exception {
        // Given
        var writer1 = new CapturingWriter();
        var writer2 = new CapturingWriter();
        var metrics = new SimuMetrics(writer1, writer2);

        // When
        metrics.record("multi", 1.0);
        metrics.close();

        // Then
        assertThat(writer1.captured).as("writer1").hasSize(1);
        assertThat(writer2.captured).as("writer2").hasSize(1);
    }

    @Test
    public void givenConcurrentRecording_whenManyThreadsRecord_thenNoDataLoss() throws Exception {
        // Given
        var writer = new CapturingWriter();
        var metrics = new SimuMetrics(writer);
        int threadCount = 10;
        int recordsPerThread = 100;
        var latch = new CountDownLatch(threadCount);

        // When
        for (int t = 0; t < threadCount; t++) {
            Thread.ofVirtual().start(() -> {
                for (int i = 0; i < recordsPerThread; i++) {
                    metrics.record("concurrent", i);
                }
                latch.countDown();
            });
        }
        latch.await(5, TimeUnit.SECONDS);
        metrics.close();

        // Then
        assertThat(writer.captured)
                .as("all points from all threads should be captured")
                .hasSize(threadCount * recordsPerThread);
    }

    @Test
    public void givenMetrics_whenRecordWithExplicitTimestamp_thenTimestampIsPreserved() throws Exception {
        // Given
        var writer = new CapturingWriter();
        var metrics = new SimuMetrics(writer);
        long customTimestamp = 1234567890_000_000_000L;

        // When
        metrics.record("test.metric", 42.0, Map.of("env", "test"), customTimestamp);
        metrics.close();

        // Then
        assertThat(writer.captured).hasSize(1);
        assertThat(writer.captured.get(0).timestampNanos()).isEqualTo(customTimestamp);
    }

    @Test
    public void givenCustomFlushInterval_whenRecordAndClose_thenDataIsFlushed() throws Exception {
        // Given
        var writer = new CapturingWriter();
        var metrics = new SimuMetrics(100L, writer); // 100ms flush

        // When
        metrics.record("fast.metric", 1.0);
        Thread.sleep(200); // Wait for at least one flush cycle
        metrics.close();

        // Then
        assertThat(writer.captured).as("data should be flushed with custom interval").hasSize(1);
    }

    @Test
    public void givenMetrics_whenRecordWithTick_thenTickIsStoredAsExtraField() throws Exception {
        // Given
        var writer = new CapturingWriter();
        var metrics = new SimuMetrics(writer);

        // When
        metrics.record("test.metric", 42.0, 5.0);
        metrics.close();

        // Then
        assertThat(writer.captured).hasSize(1);
        assertThat(writer.captured.get(0).extraFields())
                .as("tick should be stored as extra field")
                .containsEntry("tick", 5.0);
        assertThat(writer.captured.get(0).value()).isEqualTo(42.0);
    }

    @Test
    public void givenMetrics_whenRecordWithTickAndTags_thenBothPresent() throws Exception {
        // Given
        var writer = new CapturingWriter();
        var metrics = new SimuMetrics(writer);

        // When
        metrics.record("test.metric", 42.0, Map.of("role", "bee"), 17.0);
        metrics.close();

        // Then
        assertThat(writer.captured).hasSize(1);
        DataPoint dp = writer.captured.get(0);
        assertThat(dp.tags()).as("tags present").containsEntry("role", "bee");
        assertThat(dp.extraFields()).as("tick present").containsEntry("tick", 17.0);
        assertThat(dp.value()).isEqualTo(42.0);
    }
}
