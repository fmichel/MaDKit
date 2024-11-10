package madkit.simulation.activator;

import java.time.Duration;
import java.time.LocalDateTime;

import madkit.kernel.Activator;
import madkit.simulation.scheduler.DateBasedDiscreteEventScheduler;

/**
 * A behavior activator that is designed to work with a {@link DateBasedDiscreteEventScheduler}, that is following a
 * discrete-event simulation scheme. This activator activates all the agents of the corresponding CGR for each specific
 * date for which it is activated.
 *
 * It encapsulates an activation date which is coded using a {@link LocalDateTime} object.
 *
 * @param <A>
 *           The most common class of the simulated agents
 *
 * @author Fabien Michel
 */
public class DateBasedDiscreteEventActivator extends MethodActivator {

	private LocalDateTime nextActivationDate;
	private Duration defaultInterval = Duration.ofSeconds(1);

	public DateBasedDiscreteEventActivator(String community, String group, String role, String theBehaviorToActivate) {
		super(community, group, role, theBehaviorToActivate);
	}

	public LocalDateTime getCurrentTime() {
		return (LocalDateTime) getSimuTimer().getCurrentTime();
	}

	/**
	 * Defines an ordering so that the scheduler can classify activators according to their date and priority.
	 */
	@Override
	public int compareTo(Activator o) {// FIXME
		if (o instanceof DateBasedDiscreteEventActivator dba) {
			int result = getNextActivationDate().compareTo(dba.getNextActivationDate());
			if (result != 0) {
				return result;
			}
		}
		return super.compareTo(o);
	}
	
	@Override
	public DateBasedDiscreteEventScheduler getScheduler() {
		return (DateBasedDiscreteEventScheduler) super.getScheduler();
	}

	/**
	 * Returns the next date at which this activator should be triggered.
	 *
	 * @return next date at which this activator should be triggered.
	 */
	public LocalDateTime getNextActivationDate() {
		return nextActivationDate;
	}

	/**
	 * Sets the next date at which the activator will be triggered
	 *
	 * @param nextActivationDate
	 *           a {@link LocalDateTime} which should be greater than the current simulation time
	 */
	public void setNextActivationDate(LocalDateTime nextActivationDate) {
		this.nextActivationDate = nextActivationDate;
	}

	@Override
	public void execute(Object... args) {
		super.execute(args);
		setNextActivationDate(getCurrentTime().plus(defaultInterval));
	}

	@Override
	public String toString() {
		return getNextActivationDate().toString()+" ->"+super.toString();
	}

	public Duration getDefaultInterval() {
		return defaultInterval;
	}

	public void setDefaultInterval(Duration defaultInterval) {
		this.defaultInterval = defaultInterval;
	}

}
