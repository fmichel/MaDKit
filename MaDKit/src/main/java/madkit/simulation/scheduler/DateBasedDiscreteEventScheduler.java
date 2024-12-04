package madkit.simulation.scheduler;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import madkit.kernel.AbstractScheduler;
import madkit.kernel.Activator;
import madkit.simulation.DateBasedTimer;
import madkit.simulation.activator.DateBasedDiscreteEventActivator;

public class DateBasedDiscreteEventScheduler extends AbstractScheduler<DateBasedTimer> {

	private PriorityQueue<DateBasedDiscreteEventActivator> activatorActivationList = new PriorityQueue<>();

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

	public LocalDateTime getCurrentTime() {
		return getSimuTimer().getCurrentTime();
	}

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
		if (activator instanceof DateBasedDiscreteEventActivator && !getActivationList().contains(activator)) {
			getActivationList().add((DateBasedDiscreteEventActivator) activator);
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

//	@Override
//	public void logActivationStep() {
//		super.logActivationStep();
//		getLogger().finest(() -> "Activation list -> " + getActivationList());
//	}

	public PriorityQueue<DateBasedDiscreteEventActivator> getActivationList() {
		return activatorActivationList;
	}

}
