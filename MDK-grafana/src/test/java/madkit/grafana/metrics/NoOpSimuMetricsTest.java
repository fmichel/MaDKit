package madkit.grafana.metrics;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Map;

import org.testng.annotations.Test;

public class NoOpSimuMetricsTest {

    @Test
    public void givenNoOpInstance_whenRecordCalled_thenNoException() {
        // Given
        SimuMetrics noOp = SimuMetrics.noOp();

        // When / Then
        assertThatCode(() -> noOp.record("test", 1.0))
                .as("record on no-op should not throw")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenNoOpInstance_whenRecordWithTagsCalled_thenNoException() {
        // Given
        SimuMetrics noOp = SimuMetrics.noOp();

        // When / Then
        assertThatCode(() -> noOp.record("test", 1.0, Map.of("k", "v")))
                .as("record with tags on no-op should not throw")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenNoOpInstance_whenIncrementCalled_thenNoException() {
        // Given
        SimuMetrics noOp = SimuMetrics.noOp();

        // When / Then
        assertThatCode(() -> noOp.increment("counter"))
                .as("increment on no-op should not throw")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenNoOpInstance_whenCloseCalled_thenNoException() {
        // Given
        SimuMetrics noOp = SimuMetrics.noOp();

        // When / Then
        assertThatCode(noOp::close)
                .as("close on no-op should not throw")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenNoOpInstance_whenRecordWithTimestampCalled_thenNoException() {
        // Given
        SimuMetrics noOp = SimuMetrics.noOp();

        // When / Then
        assertThatCode(() -> noOp.record("test", 1.0, Map.of(), 1234567890_000_000_000L))
                .as("record with timestamp on no-op should not throw")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenNoOpFactory_whenCalledTwice_thenReturnsSameInstance() {
        // Given / When
        SimuMetrics first = SimuMetrics.noOp();
        SimuMetrics second = SimuMetrics.noOp();

        // Then — singleton
        org.assertj.core.api.Assertions.assertThat(first)
                .as("noOp() should return the same singleton instance")
                .isSameAs(second);
    }
}
