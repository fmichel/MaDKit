/**
 * 
 */
package madkit.simulation.viewer;

import madkit.kernel.Probe;

/**
 * A Viewer that displays the population of roles in the artificial organization
 */
public class RolesPopulationLineChartDrawer extends LineChartDrawer<Probe> {


	@Override
	protected void onActivation() {
		super.onActivation();
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
		getProbes().forEach(probe -> {
			addData(probe, getSimuTimer().toString(), probe.size());
		});
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
		addSerie(probe, probe.getRole());
	}

	@Override
	public void render() {
	}
}
