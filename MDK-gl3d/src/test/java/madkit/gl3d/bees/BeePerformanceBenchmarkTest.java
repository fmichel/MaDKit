package madkit.gl3d.bees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.testng.annotations.Test;

public class BeePerformanceBenchmarkTest {
    @Test
    public void benchmarkMeasuresTicksAndSnapshots() {
        // Given
        int beeCount = 1_000;

        // When
        BeePerformanceBenchmark.BenchmarkResult result = BeePerformanceBenchmark.measure(
                beeCount, 10, 1, 2);

        // Then
        assertThat(result.beeCount()).isEqualTo(beeCount);
        assertThat(result.measuredTicks()).isEqualTo(2);
        assertThat(result.tickNanos()).isPositive();
        assertThat(result.snapshotNanos()).isPositive();
        assertThat(result.snapshotEntities()).isEqualTo(2L * (beeCount + 10));
        assertThat(result.toCsv()).startsWith("1000,10,2,");
    }

    @Test
    public void benchmarkRejectsInvalidMeasurementCount() {
        // Given
        int measuredTicks = 0;

        // When / Then
        assertThatIllegalArgumentException().isThrownBy(() -> BeePerformanceBenchmark.measure(
                10, 1, 0, measuredTicks));
    }
}
