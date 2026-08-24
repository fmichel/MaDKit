package madkit.simulation.viewer.chartfx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.fair_acc.dataset.spi.DoubleDataSet;

/** Manages keyed chart-fx datasets and their sliding-window eviction policy. */
class DataSetManager<K> {
	private final Map<K, DoubleDataSet> dataSets = new HashMap<>();
	private int maxDataPoints;

	DataSetManager(int maxDataPoints) {
		this.maxDataPoints = maxDataPoints;
	}

	DoubleDataSet addDataSet(K key, String name) {
		if (dataSets.containsKey(key)) {
			throw new IllegalArgumentException("Dataset already exists for key: " + key);
		}
		DoubleDataSet dataSet = new DoubleDataSet(name);
		dataSets.put(key, dataSet);
		return dataSet;
	}

	void addData(K key, double x, double y) {
		DoubleDataSet dataSet = dataSets.get(key);
		if (dataSet == null) {
			throw new IllegalArgumentException("No dataset for key: " + key);
		}
		dataSet.add(x, y);
		if (dataSet.getDataCount() > maxDataPoints) {
			dataSet.remove(0, dataSet.getDataCount() - maxDataPoints);
		}
	}

	Map<K, DoubleDataSet> getDataSets() {
		return Collections.unmodifiableMap(dataSets);
	}

	int getMaxDataPoints() {
		return maxDataPoints;
	}

	void setMaxDataPoints(int maxDataPoints) {
		this.maxDataPoints = maxDataPoints;
	}
}
