package madkit.simu.integration.common;

import madkit.kernel.Activator;
import madkit.simulation.scheduler.MethodActivator;
import madkit.simulation.scheduler.TickBasedScheduler;

/** Deterministic headless scheduler for integration tests. */
public class IntegrationScheduler extends TickBasedScheduler {

	private MethodActivator agents;
	private Activator viewers;

	@Override
	protected void onActivation() {
		super.onActivation();
		agents = new MethodActivator(getModelGroup(), "simuAgent", "doIt");
		addActivator(agents);
		viewers = addViewersActivator();
	}

	@Override
	public void doSimulationStep() {
		agents.execute();
		viewers.execute();
		super.doSimulationStep();
	}
}
