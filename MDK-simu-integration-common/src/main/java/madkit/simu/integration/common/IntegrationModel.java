package madkit.simu.integration.common;

import madkit.simulation.SimuModel;

/** Minimal model used by the external simulation integration project. */
public class IntegrationModel extends SimuModel {

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
