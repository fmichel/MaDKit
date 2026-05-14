/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2026 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.chartfx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.annotations.Test;

import io.fair_acc.dataset.spi.DoubleDataSet;

/**
 * Tier 1 unit tests for {@link DataSetManager}.
 *
 * <p>
 * These tests validate dataset management and sliding-window eviction logic without
 * requiring the MaDKit kernel or the JavaFX platform.
 */
public class DataSetManagerTest {

	/**
	 * Verifies that adding a dataset creates it with the correct name and registers it under
	 * the given key.
	 */
	@Test
	public void addDataSet_shouldCreateDataSet() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(1000);

		// When
		DoubleDataSet ds = manager.addDataSet("s1", "Series1");

		// Then
		assertThat(ds).isNotNull();
		assertThat(ds.getName()).isEqualTo("Series1");
		assertThat(manager.getDataSets()).containsKey("s1");
		assertThat(manager.getDataSets().get("s1")).isSameAs(ds);
	}

	/**
	 * Verifies that adding data points appends them correctly to the dataset.
	 */
	@Test
	public void addData_shouldAppendPoint() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(1000);
		manager.addDataSet("s1", "Temperature");

		// When
		manager.addData("s1", 1.0, 42.0);
		manager.addData("s1", 2.0, 43.5);

		// Then
		DoubleDataSet ds = manager.getDataSets().get("s1");
		assertThat(ds.getDataCount()).isEqualTo(2);
		assertThat(ds.getX(0)).isEqualTo(1.0);
		assertThat(ds.getY(0)).isEqualTo(42.0);
		assertThat(ds.getX(1)).isEqualTo(2.0);
		assertThat(ds.getY(1)).isEqualTo(43.5);
	}

	/**
	 * Verifies that adding data with an unknown key throws an
	 * {@link IllegalArgumentException}.
	 */
	@Test
	public void addData_whenKeyMissing_shouldThrowException() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(1000);

		// When / Then
		assertThatThrownBy(() -> manager.addData("unknown", 1.0, 2.0)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("unknown");
	}

	/**
	 * Verifies that adding a dataset with a duplicate key throws an
	 * {@link IllegalArgumentException}.
	 */
	@Test
	public void addDataSet_whenDuplicateKey_shouldThrowException() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(1000);
		manager.addDataSet("s1", "First");

		// When / Then
		assertThatThrownBy(() -> manager.addDataSet("s1", "Duplicate")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("s1");
	}

	/**
	 * Verifies that the sliding-window eviction trims the oldest points when the dataset
	 * exceeds the threshold.
	 */
	@Test
	public void eviction_shouldTrimOldestPoints() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(5);
		manager.addDataSet("s1", "Series1");

		// When — add 7 points (exceeds threshold of 5)
		for (int i = 0; i < 7; i++) {
			manager.addData("s1", i, i * 10.0);
		}

		// Then — only the last 5 points remain
		DoubleDataSet ds = manager.getDataSets().get("s1");
		assertThat(ds.getDataCount()).isEqualTo(5);
		// The oldest 2 (x=0, x=1) should have been evicted
		assertThat(ds.getX(0)).isEqualTo(2.0);
		assertThat(ds.getY(0)).isEqualTo(20.0);
		assertThat(ds.getX(4)).isEqualTo(6.0);
		assertThat(ds.getY(4)).isEqualTo(60.0);
	}

	/**
	 * Verifies that no eviction occurs when the dataset is below the threshold.
	 */
	@Test
	public void eviction_shouldNotTrimWhenBelowThreshold() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(10);
		manager.addDataSet("s1", "Series1");

		// When — add 5 points (below threshold of 10)
		for (int i = 0; i < 5; i++) {
			manager.addData("s1", i, i * 10.0);
		}

		// Then — all 5 points remain
		DoubleDataSet ds = manager.getDataSets().get("s1");
		assertThat(ds.getDataCount()).isEqualTo(5);
		assertThat(ds.getX(0)).isEqualTo(0.0);
	}

	/**
	 * Verifies that no eviction occurs when the dataset is exactly at the threshold boundary.
	 */
	@Test
	public void eviction_exactlyAtThreshold_shouldNotTrim() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(5);
		manager.addDataSet("s1", "Series1");

		// When — add exactly 5 points (equal to threshold)
		for (int i = 0; i < 5; i++) {
			manager.addData("s1", i, i * 10.0);
		}

		// Then — all 5 points remain
		DoubleDataSet ds = manager.getDataSets().get("s1");
		assertThat(ds.getDataCount()).isEqualTo(5);
		assertThat(ds.getX(0)).isEqualTo(0.0);
		assertThat(ds.getX(4)).isEqualTo(4.0);
	}

	/**
	 * Verifies that eviction is applied independently per dataset — exceeding the threshold
	 * on one dataset does not affect another.
	 */
	@Test
	public void eviction_shouldEvictIndependently() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(5);
		manager.addDataSet("s1", "Series1");
		manager.addDataSet("s2", "Series2");

		// When — s1 gets 7 points (over threshold), s2 gets 3 (under)
		for (int i = 0; i < 7; i++) {
			manager.addData("s1", i, i * 10.0);
		}
		for (int i = 0; i < 3; i++) {
			manager.addData("s2", i, i * 100.0);
		}

		// Then — s1 trimmed to 5, s2 untouched at 3
		DoubleDataSet ds1 = manager.getDataSets().get("s1");
		DoubleDataSet ds2 = manager.getDataSets().get("s2");
		assertThat(ds1.getDataCount()).isEqualTo(5);
		assertThat(ds2.getDataCount()).isEqualTo(3);
		// s1 oldest points evicted
		assertThat(ds1.getX(0)).isEqualTo(2.0);
		// s2 all points intact
		assertThat(ds2.getX(0)).isEqualTo(0.0);
	}
}
