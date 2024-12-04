/*******************************************************************************
 * TurtleKit 3 - Agent Based and Artificial Life Simulation Platform
 * Copyright (C) 2011-2014 Fabien Michel
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package madkit.simulation.viewer;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

/**
 * 
 * Creates a chart tracing the population for each role taken by the turtles in
 * the Simulation. The "turtle" role is ignored by default.
 * 
 * @author Fabien Michel
 *
 */
public abstract class LineCharter extends AbstractViewer {

	private int maxDataSize = 500;
	private LineChart<String, Number> lineChart;

	protected LineChart<String, Number> getLineChart() {
		return lineChart;
	}

//	@Override
//	public void setupGUI() {
//		FXManager.runAndWait2(() -> {
//			AgentFxStage stage = new AgentFxStage(this);
////	xAxis.setStyle("-fx-tick-label-fill: red;");
////	yAxis.setStyle("-fx-tick-label-fill: red;");
//
//			// populating the series with data
//			Scene scene = new Scene(lineChart, 800, 600);
//			stage.setScene(scene);
//			stage.show();
//		});
//	}

	@Override
	protected Node createCentralNode() {
		final CategoryAxis xAxis = new CategoryAxis();
		final NumberAxis yAxis = new NumberAxis();
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

	protected abstract String getLineChartTitle();

	protected abstract String getyAxisLabel();

	protected abstract String getxAxisLabel();

	static ChronoField fromChronoUnitToTemporalField(ChronoUnit cu) {
		for (ChronoField cf : ChronoField.values()) {
			if (cf.getBaseUnit().equals(cu)) {
				return cf;
			}
		}
		return null;
	}

	public void setMaxDataSize(int maxNbOfAxisPoint) {
		maxDataSize = maxNbOfAxisPoint;
	}

	/**
	 * @return the maxDataSize
	 */
	public int getMaxDataSize() {
		return maxDataSize;
	}

}
