package madkit.simulation.viewer.chartfx;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/** Verifies the non-blocking dataset insertion path. */
public class NonBlockingDataPathTest {
	private static final int POINT_COUNT = 10_000;
	private static final long MAX_ELAPSED_MILLISECONDS = 500;

	@Test
	public void addData_shouldCompleteWithinThreshold_forTenThousandPoints() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(POINT_COUNT + 1);
		manager.addDataSet("series", "Series");
		// When
		long started = System.nanoTime();
		for (int point = 0; point < POINT_COUNT; point++) manager.addData("series", point, point);
		long elapsedMilliseconds = (System.nanoTime() - started) / 1_000_000;
		// Then
		assertThat(elapsedMilliseconds).isLessThan(MAX_ELAPSED_MILLISECONDS);
	}

	@Test
	public void addData_shouldRetainAndEvictPointsAtConfiguredLimit() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(5_000);
		manager.addDataSet("series", "Series");
		// When
		for (int point = 0; point < POINT_COUNT; point++) manager.addData("series", point, point);
		// Then
		var dataSet = manager.getDataSets().get("series");
		assertThat(dataSet.getDataCount()).isEqualTo(5_000);
		assertThat(dataSet.getX(0)).isEqualTo(5_000.0);
		assertThat(dataSet.getX(4_999)).isEqualTo(9_999.0);
	}
}
