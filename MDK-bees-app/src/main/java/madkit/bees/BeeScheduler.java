package madkit.bees;

import madkit.kernel.Scheduler;
import madkit.simulation.scheduler.DateBasedTimer;
import madkit.simulation.scheduler.MethodActivator;

/**
 * The scheduler of the bee simulation.
 */
public class BeeScheduler extends Scheduler<DateBasedTimer> {

	@Override
	public void onActivation() {
		super.onActivation();
		MethodActivator bees = new MethodActivator(getModelGroup(), Bee.BEE_ROLE, "buzz");
		addActivator(bees);
		addViewersActivator();
	}

	/**
	 * Defines a simulation step.
	 */
	@Override
	public void doSimulationStep() {
		logCurrrentTime();
		getActivators().forEach(a -> a.execute());
		getSimuTimer().addOneTimeUnit();
	}

}
