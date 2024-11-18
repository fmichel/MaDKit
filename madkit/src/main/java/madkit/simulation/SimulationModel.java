package madkit.simulation;

import static madkit.simulation.DefaultOrganization.MODEL_ROLE;

import madkit.kernel.Agent;
import madkit.kernel.Watcher;
import madkit.simulation.SimulationEngine.ENGINE;

/**
 * @author Fabien Michel
 *
 */
public class SimulationModel extends Watcher {

	@Override
	protected void onActivation() {
		getSimuEngine().setModel(this);
		requestSimuRole(getEngineGroup(), MODEL_ROLE);
	}
	
	public static void main(String[] args) {
		executeThisAgent("--scheduler=Test");
	}
	


}
