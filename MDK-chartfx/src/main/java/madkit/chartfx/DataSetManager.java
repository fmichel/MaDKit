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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.fair_acc.dataset.spi.DoubleDataSet;

/**
 * Package-private helper that manages a keyed collection of
 * {@link DoubleDataSet} instances and a sliding-window eviction strategy.
 *
 * <p>This class is extracted from {@link XYChartViewer} so that dataset
 * management and eviction logic can be unit-tested without the MaDKit kernel
 * or the JavaFX platform. It is <b>not</b> part of the public API.
 *
 * @param <K> the type of keys used to identify datasets
 *
 * @see XYChartViewer
 */
class DataSetManager<K> {

	private final Map<K, DoubleDataSet> dataSets = new HashMap<>();
	private int maxDataPoints;

	/**
	 * Creates a new manager with the given eviction threshold.
	 *
	 * @param maxDataPoints the maximum number of data points per dataset
	 *                      before eviction kicks in; must be positive
	 */
	DataSetManager(int maxDataPoints) {
		this.maxDataPoints = maxDataPoints;
	}

	/**
	 * Creates and registers a new {@link DoubleDataSet} with the given key
	 * and display name.
	 *
	 * @param key  the key used to identify the dataset
	 * @param name the display name shown in the chart legend
	 * @return the newly created {@link DoubleDataSet}
	 * @throws IllegalArgumentException if a dataset with the same key
	 *                                  already exists
	 */
	DoubleDataSet addDataSet(K key, String name) {
		if (dataSets.containsKey(key)) {
			throw new IllegalArgumentException("Dataset already exists for key: " + key);
		}
		DoubleDataSet ds = new DoubleDataSet(name);
		dataSets.put(key, ds);
		return ds;
	}

	/**
	 * Appends a data point to the dataset identified by {@code key} and
	 * evicts the oldest points if the dataset exceeds the threshold.
	 *
	 * @param key the dataset key
	 * @param x   the x-axis value
	 * @param y   the y-axis value
	 * @throws IllegalArgumentException if no dataset exists for {@code key}
	 */
	void addData(K key, double x, double y) {
		DoubleDataSet ds = dataSets.get(key);
		if (ds == null) {
			throw new IllegalArgumentException("No dataset for key: " + key);
		}
		ds.add(x, y);
		evictIfNeeded(ds);
	}

	/**
	 * Returns an unmodifiable view of the dataset map.
	 *
	 * @return a read-only {@link Map} from keys to {@link DoubleDataSet}
	 *         instances
	 */
	Map<K, DoubleDataSet> getDataSets() {
		return Collections.unmodifiableMap(dataSets);
	}

	/**
	 * Returns the current eviction threshold.
	 *
	 * @return the maximum number of data points per dataset
	 */
	int getMaxDataPoints() {
		return maxDataPoints;
	}

	/**
	 * Sets the eviction threshold.
	 *
	 * @param maxDataPoints the new threshold; must be positive
	 */
	void setMaxDataPoints(int maxDataPoints) {
		this.maxDataPoints = maxDataPoints;
	}

	/**
	 * Trims the oldest data points from the given dataset if its size
	 * exceeds the {@code maxDataPoints} threshold.
	 *
	 * @param ds the dataset to check and potentially trim
	 */
	private void evictIfNeeded(DoubleDataSet ds) {
		if (ds.getDataCount() > maxDataPoints) {
			int excess = ds.getDataCount() - maxDataPoints;
			ds.remove(0, excess);
		}
	}
}
