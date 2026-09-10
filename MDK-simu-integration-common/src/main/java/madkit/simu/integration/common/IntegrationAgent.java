package madkit.simu.integration.common;

import madkit.simulation.SimuAgent;

/** Minimal simulated agent proving that the consumer can launch an agent. */
public class IntegrationAgent extends SimuAgent {

	private int steps;

	@Override
	protected void onActivation() {
		playRole("simuAgent");
	}

	private void doIt() {
		steps++;
	}

	/** @return the number of scheduler steps observed by this agent */
	public int getSteps() {
		return steps;
	}
}
