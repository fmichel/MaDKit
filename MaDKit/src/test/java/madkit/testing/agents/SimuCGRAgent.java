package madkit.testing.agents;

import madkit.kernel.MadkitTestConstants;
import madkit.simulation.SimuAgent;

/**
 * The Class SimuCGRAgent.
 */
public class SimuCGRAgent extends SimuAgent {

	@Override
	protected void onActivation() {
		playRole(MadkitTestConstants.ROLE);
	}

	private void bug() {
		throw new NullPointerException("Buggy simu agent !!!");
	}
}
