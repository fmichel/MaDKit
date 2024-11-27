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
	private int counter;
	private Instant begin;
	
	@Override
	protected void onActivation() {
		getLogger().setLevel(Level.FINER);
		super.onActivation();
		agents = new MethodActivator(getModelGroup(), "simuAgent","doIt");
		addActivator(agents);
		agents.setShufflingMode(true);
		viewers = new MethodActivator(getEngineGroup(), DefaultOrganization.VIEWER_ROLE,"observe");
		addActivator(viewers);
	}
	
	@Override
	public void doSimulationStep() {
		counter++;
		agents.execute();
		viewers.execute();
//		logSpeed();
	}

	private void logSpeed() {
		if (begin == null) {
			begin = Instant.now();
		} 
		if(counter%100 == 0) {
			getLogger().info(() -> ""+Duration.between(begin,Instant.now()).toMillis());
			begin = null;
		}
	}

}
