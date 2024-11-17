package madkit.simulation;

import static madkit.simulation.DefaultOrganization.MODEL_ROLE;

import madkit.kernel.Agent;
import madkit.simulation.SimulationEngine.ENGINE;

/**
 * @author Fabien Michel
 *
 */
public class SimulationModel extends SimuAgent {

	@Override
	protected void onActivation() {
		requestRole(getCommunity(), getEngineGroup(), MODEL_ROLE);
		getSimuEngine().setModel(this);
	}
	
	public static void main(String[] args) {
		executeThisAgent("--scheduler=Test");
	}
	


}
