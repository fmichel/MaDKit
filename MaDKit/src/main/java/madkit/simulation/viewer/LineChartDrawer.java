/**
 * 
 */
package madkit.simulation.viewer;

import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import madkit.gui.FXManager;
import madkit.simulation.Viewer;
import madkit.simulation.DefaultViewerGUI;

/**
 * A Viewer agent specialized in line chart drawing.
 * 
 * <p>
 * The GUI associated with this agent is a {@link DefaultViewerGUI} that
 * displays a JavaFX stage with a {@link LineChart} as the central node. The
 * chart is initialized with a {@link CategoryAxis} for the x-axis and a
 * {@link NumberAxis} for the y-axis. The axes labels and the chart title are
 * set by the abstract methods {@link #getxAxisLabel()},
 * {@link #getyAxisLabel()} and {@link #getLineChartTitle()} respectively.
 * 
 */
public abstract class LineChartDrawer extends Viewer {

	private LineChart<String, Number> lineChart;

	/**
	 * This method is called when the agent is activated. By default, it requests
	 * the role {@link DefaultOrganization#VIEWER_ROLE} in the group
	 * {@link DefaultOrganization#ENGINE_GROUP.
	 * 
	 * 
	 * <p>
	 * The GUI is initialized with a {@link LineChart} as the central node.
	 */
	@Override
	protected void onActivation() {
		super.onActivation();
		setGUI(new DefaultViewerGUI(this) {
			@Override
			protected Node createCentralNode() {
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
		pause(5000);
		FXManager.runAndWait(() -> {
			getGUI().getStage().show();
			getGUI().getStage().setWidth(800);
			getGUI().getStage().setHeight(800);
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

}
