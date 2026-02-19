package madkit.test.agents;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simulation.SimuAgent;

/**
 * The Class SimuCGRAgent.
 */
public class SimuCGRAgent extends SimuAgent {

	@Override
	protected void onActivation() {
		playRole(MadkitConcurrentTestCase.ROLE);
	}

	private void bug() {
		throw new NullPointerException("Buggy simu agent !!!");
	}
}
