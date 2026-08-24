package madkit.simulation.viewer.chartfx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.annotations.Test;

/** Tests keyed dataset creation and sliding-window eviction. */
public class DataSetManagerTest {
	@Test
	public void addDataSet_shouldCreateDataSet() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(2);
		// When
		var dataSet = manager.addDataSet("series", "Series");
		// Then
		assertThat(dataSet.getName()).isEqualTo("Series");
		assertThat(manager.getDataSets()).containsEntry("series", dataSet);
	}

	@Test
	public void addData_whenKeyMissing_shouldThrowException() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(2);
		// When
		var action = (org.assertj.core.api.ThrowableAssert.ThrowingCallable) () -> manager.addData("missing", 1, 2);
		// Then
		assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("missing");
	}

	@Test
	public void addDataSet_whenDuplicateKey_shouldThrowException() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(2);
		manager.addDataSet("series", "Series");
		// When
		var action = (org.assertj.core.api.ThrowableAssert.ThrowingCallable) () -> manager.addDataSet("series", "Duplicate");
		// Then
		assertThatThrownBy(action).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("series");
	}

	@Test
	public void eviction_shouldTrimOldestPointsIndependently() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(2);
		manager.addDataSet("first", "First");
		manager.addDataSet("second", "Second");
		// When
		for (int point = 0; point < 3; point++) manager.addData("first", point, point);
		manager.addData("second", 0, 0);
		// Then
		assertThat(manager.getDataSets().get("first").getDataCount()).isEqualTo(2);
		assertThat(manager.getDataSets().get("first").getX(0)).isEqualTo(1.0);
		assertThat(manager.getDataSets().get("second").getDataCount()).isOne();
	}
}
