package madkit.simu.integration.common;

import madkit.simulation.SimuEnvironment;

/** Minimal headless environment used by the integration project. */
public class IntegrationEnvironment extends SimuEnvironment {

	private int simulationStarts;

	@Override
	public void onSetupSimulation() {
		super.onSetupSimulation();
		simulationStarts++;
	}

	/** @return the number of completed simulation-start callbacks */
	public int getSimulationStarts() {
		return simulationStarts;
	}
}
