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

import java.util.Map;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.dataset.spi.DoubleDataSet;
import madkit.gui.FXExecutor;
import madkit.gui.UIProperty;

/**
 * A convenience {@link ChartFxViewer} subclass for XY line and scatter charts, backed by
 * chart-fx's {@link XYChart} and {@link DoubleDataSet}.
 *
 * <p>
 * This class manages a map of named {@link DoubleDataSet} instances keyed by a
 * user-defined type {@code K}. Data is appended via the non-blocking
 * {@link #addData(Object, double, double)} method, which does <b>not</b> use
 * {@link FXExecutor#runAndWait(Runnable)} — chart-fx's dataset is lock-free and the chart
 * picks up new data on its next rendering pulse automatically.
 *
 * <p>
 * A sliding-window eviction strategy removes the oldest data points when a dataset
 * exceeds {@link #getMaxDataPoints() maxDataPoints}, providing smooth visual continuity
 * instead of the clear-all approach used by
 * {@link madkit.simulation.viewer.LineChartDrawer}.
 *
 * <h2>Usage example</h2>
 * 
 * <pre>
 * {
 * 	&#64;code
 * 	public class TemperatureViewer extends XYChartViewer<String> {
 *
 * 		&#64;Override
 * 		protected void onActivation() {
 * 			super.onActivation();
 * 			addDataSet("indoor", "Indoor Temperature");
 * 			addDataSet("outdoor", "Outdoor Temperature");
 * 		}
 *
 * 		@Override
 * 		public void display() {
 * 			double tick = ((Number) getSimuTimer().getCurrentTime()).doubleValue();
 * 			addData("indoor", tick, readIndoorTemp());
 * 			addData("outdoor", tick, readOutdoorTemp());
 * 			super.display();
 * 		}
 * 	}
 * }
 * </pre>
 *
 * @param <K> the type of keys used to identify datasets (e.g., {@link String}, an enum,
 *            or a {@link madkit.kernel.Probe})
 *
 * @see ChartFxViewer
 * @see DoubleDataSet
 * @see <a href="https://github.com/fair-acc/chart-fx">chart-fx on GitHub</a>
 */
public abstract class XYChartViewer<K> extends ChartFxViewer {

	/** Default maximum number of data points per dataset before eviction. */
	private static final int DEFAULT_MAX_DATA_POINTS = 50_000;

	private final DataSetManager<K> dataSetManager = new DataSetManager<>(DEFAULT_MAX_DATA_POINTS);

	@UIProperty
	private int maxDataPoints = DEFAULT_MAX_DATA_POINTS;

	/**
	 * Creates an {@link XYChart} with two {@link DefaultNumericAxis} instances (both
	 * auto-ranging) and an {@link ErrorDataSetRenderer}.
	 *
	 * <p>
	 * The chart title and axis labels are obtained from {@link #getChartTitle()},
	 * {@link #getXAxisLabel()}, and {@link #getYAxisLabel()} respectively. The chart has
	 * animations disabled for real-time simulation use.
	 *
	 * @return a fully-configured {@link XYChart}, never {@code null}
	 */
	@Override
	protected XYChart createChart() {
		DefaultNumericAxis xAxis = new DefaultNumericAxis(getXAxisLabel());
		DefaultNumericAxis yAxis = new DefaultNumericAxis(getYAxisLabel());
		xAxis.setAutoRanging(true);
		yAxis.setAutoRanging(true);
		XYChart xyChart = new XYChart(xAxis, yAxis);
		xyChart.setTitle(getChartTitle());
		xyChart.setAnimated(false);
		xyChart.getRenderers().setAll(new ErrorDataSetRenderer());
		return xyChart;
	}

	/**
	 * Creates and registers a new {@link DoubleDataSet} identified by the given key, and adds
	 * it to the chart's renderer.
	 *
	 * <p>
	 * This method should be called during {@link #onActivation()} (after
	 * {@code super.onActivation()}) to set up the datasets before the simulation starts
	 * producing data.
	 *
	 * @param key  the key used to identify the dataset in subsequent
	 *             {@link #addData(Object, double, double)} calls
	 * @param name the display name of the dataset shown in the chart legend
	 * @return the newly created {@link DoubleDataSet}
	 * @throws IllegalArgumentException if a dataset with the same key already exists
	 * @see #addData(Object, double, double)
	 */
	protected void addDataSet(K key, String name) {
		FXExecutor.runAndWait(() -> {
			DoubleDataSet ds = dataSetManager.addDataSet(key, name);
			getXYChart().getDatasets().add(ds);
		});
	}

	/**
	 * Appends a data point to the dataset identified by {@code key}.
	 *
	 * <p>
	 * <b>Thread safety:</b> This method is safe to call from the simulation thread (or any
	 * non-FX thread). It does <b>not</b> block on the JavaFX Application Thread. The chart-fx
	 * library will pick up the new data on its next rendering pulse. No
	 * {@link FXExecutor#runAndWait(Runnable)} call is involved — this is the critical
	 * performance difference with
	 * {@link madkit.simulation.viewer.LineChartDrawer#addData(Object, String, Number)
	 * LineChartDrawer.addData()}.
	 *
	 * @param key the dataset key
	 * @param x   the x-axis value (typically simulation time)
	 * @param y   the y-axis value
	 * @throws IllegalArgumentException if no dataset exists for {@code key}
	 * @see #addDataSet(Object, String)
	 */
	public void addData(K key, double x, double y) {
		dataSetManager.addData(key, x, y);
	}

	/**
	 * Returns an unmodifiable view of the dataset map.
	 *
	 * @return a read-only {@link Map} from keys to {@link DoubleDataSet} instances
	 */
	public Map<K, DoubleDataSet> getDataSets() {
		return dataSetManager.getDataSets();
	}

	/**
	 * Returns the chart-fx {@link XYChart} managed by this viewer.
	 *
	 * <p>
	 * This is a typed convenience method equivalent to casting {@link #getChart()} to
	 * {@link XYChart}.
	 *
	 * @return the {@link XYChart}, or {@code null} if not yet activated
	 */
	public XYChart getXYChart() {
		return (XYChart) getChart();
	}

	/**
	 * Returns the label for the X axis. The default is {@code "Time"}.
	 *
	 * <p>
	 * Override this method to provide a custom X-axis label.
	 *
	 * @return the X-axis label, never {@code null}
	 */
	protected String getXAxisLabel() {
		return "Time";
	}

	/**
	 * Returns the label for the Y axis. The default is an empty string.
	 *
	 * <p>
	 * Override this method to provide a custom Y-axis label.
	 *
	 * @return the Y-axis label, never {@code null}
	 */
	protected String getYAxisLabel() {
		return "";
	}

	/**
	 * Returns the chart title. The default is the simple name of this class.
	 *
	 * <p>
	 * Override this method to provide a custom chart title.
	 *
	 * @return the chart title, never {@code null}
	 */
	protected String getChartTitle() {
		return getClass().getSimpleName();
	}

	/**
	 * Returns the maximum number of data points per dataset before the sliding-window
	 * eviction removes the oldest points.
	 *
	 * @return the current maximum data points threshold
	 * @see #setMaxDataPoints(int)
	 */
	public int getMaxDataPoints() {
		return dataSetManager.getMaxDataPoints();
	}

	/**
	 * Sets the maximum number of data points per dataset before eviction.
	 *
	 * @param maxDataPoints the new threshold; must be positive
	 * @see #getMaxDataPoints()
	 */
	public void setMaxDataPoints(int maxDataPoints) {
		this.maxDataPoints = maxDataPoints;
		dataSetManager.setMaxDataPoints(maxDataPoints);
	}
}
