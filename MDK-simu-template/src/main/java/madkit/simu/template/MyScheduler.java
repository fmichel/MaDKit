package madkit.simu.template;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;

import madkit.kernel.Activator;
import madkit.simulation.DefaultOrganization;
import madkit.simulation.activator.MethodActivator;
import madkit.simulation.scheduler.TickBasedScheduler;

public class MyScheduler extends TickBasedScheduler {

	MethodActivator agents;
	private Activator viewers;

	@Override
	protected void onActivation() {
		getLogger().setLevel(Level.FINER);
		super.onActivation();
		agents = new MethodActivator(getModelGroup(), "simuAgent", "doIt");
		addActivator(agents);
		agents.setShufflingMode(true);
		viewers = new MethodActivator(getEngineGroup(), DefaultOrganization.VIEWER_ROLE, "observe");
		addActivator(viewers);
	}

	@Override
	public void doSimulationStep() {
		agents.execute();
		viewers.execute();
		super.doSimulationStep();
	}

}
