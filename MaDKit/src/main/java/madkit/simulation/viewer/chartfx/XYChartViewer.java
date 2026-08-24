package madkit.simulation.viewer.chartfx;

import java.util.Map;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.dataset.spi.DoubleDataSet;
import madkit.gui.FXExecutor;
import madkit.gui.UIProperty;

/** Chart-fx XY viewer with keyed, non-blocking data updates. */
public abstract class XYChartViewer<K> extends ChartFxViewer {
	private static final int DEFAULT_MAX_DATA_POINTS = 50_000;
	private final DataSetManager<K> dataSetManager = new DataSetManager<>(DEFAULT_MAX_DATA_POINTS);
	@UIProperty
	private int maxDataPoints = DEFAULT_MAX_DATA_POINTS;

	@Override
	protected XYChart createChart() {
		DefaultNumericAxis xAxis = new DefaultNumericAxis(getXAxisLabel());
		DefaultNumericAxis yAxis = new DefaultNumericAxis(getYAxisLabel());
		xAxis.setAutoRanging(true);
		yAxis.setAutoRanging(true);
		XYChart chart = new XYChart(xAxis, yAxis);
		chart.setTitle(getChartTitle());
		chart.setAnimated(false);
		chart.getRenderers().setAll(new ErrorDataSetRenderer());
		return chart;
	}

	/** Registers a dataset during viewer activation. */
	protected void addDataSet(K key, String name) {
		FXExecutor.runAndWait(() -> getXYChart().getDatasets().add(dataSetManager.addDataSet(key, name)));
	}

	/** Appends a point without synchronously waiting for the JavaFX thread. */
	public void addData(K key, double x, double y) {
		dataSetManager.addData(key, x, y);
	}

	/** Returns the datasets by their keys. */
	public Map<K, DoubleDataSet> getDataSets() {
		return dataSetManager.getDataSets();
	}

	/** Returns the managed XY chart. */
	public XYChart getXYChart() {
		return (XYChart) getChart();
	}

	/** Returns the X-axis label. */
	protected String getXAxisLabel() { return "Time"; }
	/** Returns the Y-axis label. */
	protected String getYAxisLabel() { return ""; }
	/** Returns the chart title. */
	protected String getChartTitle() { return getClass().getSimpleName(); }
	/** Returns the data-point sliding-window limit. */
	public int getMaxDataPoints() { return dataSetManager.getMaxDataPoints(); }
	/** Sets the data-point sliding-window limit. */
	public void setMaxDataPoints(int maxDataPoints) {
		this.maxDataPoints = maxDataPoints;
		dataSetManager.setMaxDataPoints(maxDataPoints);
	}
}
