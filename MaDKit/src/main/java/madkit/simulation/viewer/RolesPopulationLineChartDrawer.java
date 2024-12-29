/**
 * 
 */
package madkit.simulation.viewer;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import madkit.gui.FXManager;
import madkit.kernel.Probe;

/**
 * A Viewer that displays the population of roles in the artificial organization
 */
public class RolesPopulationLineChartDrawer extends LineChartDrawer {

	private Map<Probe, XYChart.Series<String, Number>> series;

	@Override
	public void render() {
		for (Map.Entry<Probe, XYChart.Series<String, Number>> entry : series.entrySet()) {
			ObservableList<Data<String, Number>> serie = entry.getValue().getData();
			serie.add(new XYChart.Data<>(getSimuTimer().toString(), entry.getKey().size()));
		}
	}

	@Override
	protected void onActivation() {
		super.onActivation();
		series = new HashMap<>();
	}

	@Override
	protected String getLineChartTitle() {
		return "Roles population";
	}

	@Override
	protected String getyAxisLabel() {
		return "Population count";
	}

	@Override
	protected String getxAxisLabel() {
		return "Time";
	}

	@Override
	public void display() {
		for (Map.Entry<Probe, XYChart.Series<String, Number>> entry : series.entrySet()) {
			ObservableList<Data<String, Number>> serie = entry.getValue().getData();
			serie.add(new XYChart.Data<>(getSimuTimer().toString(), entry.getKey().size()));
		}
		super.display();
	}

	/**
	 * Adds a role to the monitoring
	 * 
	 * @param group the group to monitor
	 * @param role  the role to monitor
	 */
	protected void addRoleToMonitoring(String group, String role) {
		Probe probe = new Probe(getCommunity(), group, role);
		addProbe(probe);
		XYChart.Series<String, Number> serie = new XYChart.Series<>();
		FXManager.runLater(() -> {
			getLineChart().getData().add(serie);
			serie.setName(role);
			series.put(probe, serie);
		});
	}
}
