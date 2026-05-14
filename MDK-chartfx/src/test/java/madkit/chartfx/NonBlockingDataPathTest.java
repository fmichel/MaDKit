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

import org.testng.annotations.Test;

import io.fair_acc.dataset.spi.DoubleDataSet;

/**
 * Tier 2 performance tests verifying that the
 * {@link DataSetManager#addData(Object, double, double)} path is non-blocking.
 *
 * <p>The critical design contract of the chart-fx integration is that
 * {@link XYChartViewer#addData(Object, double, double)} delegates to
 * {@link DataSetManager#addData(Object, double, double)} which calls
 * {@link DoubleDataSet#add(double, double)} — a lock-free operation.
 * No {@link madkit.gui.FXExecutor#runAndWait(Runnable)} call is involved.
 *
 * <p>These tests verify this contract by measuring the elapsed time for a
 * large batch of {@code addData()} calls. If the implementation were to
 * round-trip through the FX thread, each call would incur ~1–16ms of
 * latency, making 10,000 calls take at least 10 seconds. The non-blocking
 * path completes the same batch in well under 100ms.
 *
 * <p>No MaDKit kernel or JavaFX platform is required for these tests
 * because {@link DataSetManager} is directly instantiable as a
 * package-private class.
 *
 * @see DataSetManager
 * @see XYChartViewer#addData(Object, double, double)
 */
public class NonBlockingDataPathTest {

	/** Number of data points used in the performance test. */
	private static final int POINT_COUNT = 10_000;

	/**
	 * Maximum allowed elapsed time (in milliseconds) for {@value #POINT_COUNT}
	 * {@code addData()} calls. If the path were blocking on the FX thread,
	 * this would be exceeded by orders of magnitude.
	 */
	private static final long MAX_ELAPSED_MS = 100;

	/**
	 * Verifies that {@value #POINT_COUNT} {@code addData()} calls complete
	 * in less than {@value #MAX_ELAPSED_MS}ms, proving that no FX thread
	 * round-trip is involved.
	 */
	@Test
	public void addData_shouldCompleteWithinThreshold_forTenThousandPoints() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(POINT_COUNT + 1);
		manager.addDataSet("perf", "Performance");

		// When
		long startNanos = System.nanoTime();
		for (int i = 0; i < POINT_COUNT; i++) {
			manager.addData("perf", i, i * 0.1);
		}
		long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

		// Then
		assertThat(elapsedMs)
				.as("Elapsed time for %d addData() calls should be < %dms (was %dms)",
						POINT_COUNT, MAX_ELAPSED_MS, elapsedMs)
				.isLessThan(MAX_ELAPSED_MS);
	}

	/**
	 * Verifies that all {@value #POINT_COUNT} data points are present in the
	 * dataset after the non-blocking batch insertion.
	 */
	@Test
	public void addData_shouldRetainAllPoints_whenBelowEvictionThreshold() {
		// Given
		DataSetManager<String> manager = new DataSetManager<>(POINT_COUNT + 1);
		manager.addDataSet("perf", "Performance");

		// When
		for (int i = 0; i < POINT_COUNT; i++) {
			manager.addData("perf", i, i * 0.1);
		}

		// Then
		DoubleDataSet ds = manager.getDataSets().get("perf");
		assertThat(ds.getDataCount()).isEqualTo(POINT_COUNT);
		assertThat(ds.getX(0)).isEqualTo(0.0);
		assertThat(ds.getX(POINT_COUNT - 1)).isEqualTo(POINT_COUNT - 1);
	}

	/**
	 * Verifies that the non-blocking data path still respects the
	 * sliding-window eviction strategy when the threshold is exceeded
	 * during a large batch.
	 */
	@Test
	public void addData_shouldEvictCorrectly_duringLargeBatch() {
		// Given — threshold is smaller than the number of points
		int threshold = 5_000;
		DataSetManager<String> manager = new DataSetManager<>(threshold);
		manager.addDataSet("perf", "Performance");

		// When
		for (int i = 0; i < POINT_COUNT; i++) {
			manager.addData("perf", i, i * 0.1);
		}

		// Then — only the last `threshold` points remain
		DoubleDataSet ds = manager.getDataSets().get("perf");
		assertThat(ds.getDataCount()).isEqualTo(threshold);
		int expectedFirstX = POINT_COUNT - threshold;
		assertThat(ds.getX(0)).isEqualTo((double) expectedFirstX);
		assertThat(ds.getX(threshold - 1)).isEqualTo((double) (POINT_COUNT - 1));
	}

	/**
	 * Verifies that the non-blocking data path with eviction also completes
	 * within the performance threshold. Eviction adds a small overhead per
	 * point (the {@code remove()} call), but the total time must still be
	 * well below what an FX-thread round-trip would cost.
	 */
	@Test
	public void addData_withEviction_shouldStillBeUnderThreshold() {
		// Given — threshold forces eviction on every add after the initial fill
		int threshold = 1_000;
		DataSetManager<String> manager = new DataSetManager<>(threshold);
		manager.addDataSet("perf", "Performance");

		// When
		long startNanos = System.nanoTime();
		for (int i = 0; i < POINT_COUNT; i++) {
			manager.addData("perf", i, i * 0.1);
		}
		long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

		// Then
		assertThat(elapsedMs)
				.as("Elapsed time with eviction for %d addData() calls should be < %dms (was %dms)",
						POINT_COUNT, MAX_ELAPSED_MS, elapsedMs)
				.isLessThan(MAX_ELAPSED_MS);
	}
}
