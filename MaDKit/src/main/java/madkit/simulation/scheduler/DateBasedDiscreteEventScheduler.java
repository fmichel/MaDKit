package madkit.simulation.scheduler;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import madkit.kernel.Activator;
import madkit.kernel.Scheduler;

/**
 * This class defines a scheduler for discrete event simulation. It is based on
 * the {@link DateBasedTimer} class which is used to manage the simulation time.
 * The scheduler uses a priority queue to manage the activation list of the
 * activators.
 */
public class DateBasedDiscreteEventScheduler extends Scheduler<DateBasedTimer> {

	private PriorityQueue<DateBasedDiscreteEventActivator> activatorActivationList = new PriorityQueue<>();

	/**
	 * Constructs a new scheduler with a {@link DateBasedTimer} as simulation time.
	 */
	public DateBasedDiscreteEventScheduler() {
		setSimulationTime(new DateBasedTimer());
	}

	@Override
	public void doSimulationStep() {
		DateBasedDiscreteEventActivator nextActivator = getActivationList().poll();
		getSimuTimer().setCurrentTime(nextActivator.getNextActivationDate());
		nextActivator.execute();
		getActivationList().add(nextActivator);
	}

	/**
	 * Returns the current simulation time.
	 * 
	 * @return the current simulation time
	 */
	public LocalDateTime getCurrentTime() {
		return getSimuTimer().getCurrentTime();
	}

	/**
	 * Logs the activation list.
	 */
	public void logActivationList() {
		getLogger().fine(() -> {
			StringBuilder s = new StringBuilder("\nActivation list ->\n");
			for (DateBasedDiscreteEventActivator dateBasedDiscreteEventActivator : getActivationList()) {
				s.append(dateBasedDiscreteEventActivator).append('\n');
			}
			return s.toString();
		});
	}

	@Override
	public void addActivator(Activator activator) {
		super.addActivator(activator);
		if (activator instanceof DateBasedDiscreteEventActivator dbdea && !getActivationList().contains(activator)) {
			getActivationList().add(dbdea);
		}
	}

	/**
	 * Should be used when an activator changes its next event date so that the new
	 * one is earlier. So, this method triggers the reordering of this activator.
	 * 
	 * @param a
	 */
	public void updateActivatorRanking(Activator a) {
		if (activatorActivationList.remove(a)) {
			activatorActivationList.add((DateBasedDiscreteEventActivator) a);
		}
	}

	@Override
	public void removeActivator(Activator activator) {
		super.removeActivator(activator);
		getActivationList().remove(activator);
	}

	/**
	 * Returns the activation list.
	 * 
	 * @return the activation list
	 */
	public PriorityQueue<DateBasedDiscreteEventActivator> getActivationList() {
		return activatorActivationList;
	}

}
