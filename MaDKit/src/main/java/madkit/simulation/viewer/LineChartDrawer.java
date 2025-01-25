/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
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
package madkit.simulation.viewer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import madkit.gui.FXExecutor;
import madkit.gui.UIProperty;
import madkit.simulation.SimuOrganization;
import madkit.simulation.Viewer;

/**
 * A Viewer agent specialized in line chart drawing.
 * 
 * <p>
 * The GUI associated with this agent is a {@link ViewerDefaultGUI} that displays a JavaFX
 * stage with a {@link LineChart} as the central node. The chart is initialized with a
 * {@link CategoryAxis} for the x-axis and a {@link NumberAxis} for the y-axis. The axes
 * labels and the chart title are set by the abstract methods {@link #getxAxisLabel()},
 * {@link #getyAxisLabel()} and {@link #getLineChartTitle()} respectively.
 * 
 * @param <K> the type of the keys used to identify the series in the line chart
 * 
 * 
 */
public abstract class LineChartDrawer<K> extends Viewer {

	private LineChart<String, Number> lineChart;
	private Map<K, XYChart.Series<String, Number>> series;
	@UIProperty
	private int maxXDataPoints = 500;

	/**
	 * This method is called when the agent is activated. By default, it requests the role
	 * {@link SimuOrganization#VIEWER_ROLE} in the group
	 * {@link SimuOrganization#ENGINE_GROUP}.
	 * 
	 * <p>
	 * The GUI is initialized with a {@link LineChart} as the central node.
	 */
	@Override
	protected void onActivation() {
		series = new HashMap<>();
		super.onActivation();
		setGUI(new ViewerDefaultGUI(this) {
			@Override
			protected Node createCenterNode() {
				CategoryAxis xAxis = new CategoryAxis();
				NumberAxis yAxis = new NumberAxis();
				xAxis.setLabel(getxAxisLabel());
				yAxis.setLabel(getyAxisLabel());
				lineChart = new LineChart<>(xAxis, yAxis);
				lineChart.setTitle(getLineChartTitle());
				lineChart.setCreateSymbols(false);
				lineChart.setCache(true);
				lineChart.setAnimated(false);
				lineChart.setMinSize(500, 500);
				return lineChart;
			}
		});
		FXExecutor.runAndWait(() -> {
			getGUI().getStage().show();
			getGUI().getStage().setWidth(400);
			getGUI().getStage().setHeight(400);
			getGUI().getStage().setX(0);
		});
		getGUI().getSynchroPaintingAction().setSelected(false);
	}

	/**
	 * Adds a new serie to the line chart.
	 * 
	 * @param key  the key to identify the serie
	 * @param name the name of the serie
	 */
	protected void addSerie(K key, String name) {
		FXExecutor.runAndWait(() -> {
			XYChart.Series<String, Number> serie = new XYChart.Series<>();
			serie.setName(name);
			series.put(key, serie);
			lineChart.getData().add(serie);
		});
	}

	/**
	 * Adds a new data point to the serie identified by the key.
	 * 
	 * @param key the key of the serie
	 * @param x   the x value
	 * @param y   the y value
	 */
	public void addData(K key, String x, Number y) {
		FXExecutor.runAndWait(() -> {
			checkDataSize();
			series.get(key).getData().add(new XYChart.Data<>(x, y));
		});
	}

	/**
	 * Returns the title of the line chart.
	 * 
	 * @return the title of the line chart
	 */
	protected abstract String getLineChartTitle();

	/**
	 * Returns the label of the y-axis of the line chart.
	 * 
	 * @return the label of the y-axis of the line chart
	 */
	protected abstract String getyAxisLabel();

	/**
	 * Returns the label of the x-axis of the line chart.
	 * 
	 * @return the label of the x-axis of the line chart
	 */
	protected abstract String getxAxisLabel();

	/**
	 * Returns the line chart node
	 * 
	 * @return the line chart node
	 */
	protected LineChart<String, Number> getLineChart() {
		return lineChart;
	}

	/**
	 * Returns the series map
	 * 
	 * @return the series map
	 */
	public Map<K, XYChart.Series<String, Number>> getSeries() {
		return series;
	}

	private void checkDataSize() {
		Collection<Series<String, Number>> values = series.values();
		for (Series<String, Number> serie : values) {
			if (serie.getData().size() > maxXDataPoints) {
				values.forEach(s -> s.getData().clear());
				break;
			}
		}
	}

	/**
	 * Returns the maximum number of data points to display on the x-axis
	 * 
	 * @return the maxXDataPoints
	 */
	public int getMaxXDataPoints() {
		return maxXDataPoints;
	}

	/**
	 * Sets the maximum number of data points to display on the x-axis
	 * 
	 * @param maxXDataPoints the maxXDataPoints to set
	 */
	public void setMaxXDataPoints(int maxXDataPoints) {
		this.maxXDataPoints = maxXDataPoints;
	}

}
