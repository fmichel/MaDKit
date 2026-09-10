package madkit.simu.integration.common;

import madkit.simulation.SimuEnvironment;

/** Minimal headless environment used by the integration project. */
public class IntegrationEnvironment extends SimuEnvironment {

	private int simulationStarts;

	@Override
	public void onSimulationStart() {
		super.onSimulationStart();
		simulationStarts++;
	}

	/** @return the number of completed simulation-start callbacks */
	public int getSimulationStarts() {
		return simulationStarts;
	}
}
