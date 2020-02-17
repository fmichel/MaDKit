package madkit.simulation.activator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.simulation.scheduler.DateBasedDiscreteEventScheduler;

/**
 * A behavior activator that is designed to work with a {@link DateBasedDiscreteEventScheduler},
 *  that is following a discrete-event simulation scheme.
 *  This activator activates all the agents of the corresponding CGR for each specific date for
 *  which it is activated.
 *
 * It encapsulates an activation date which is coded using a {@link LocalDateTime} object.
 *
 * @author Fabien Michel
 *
 * @param <A>
 */
public class DateBasedDiscreteEventActivator<A extends AbstractAgent> extends GenericBehaviorActivator<A> implements Comparable<DateBasedDiscreteEventActivator<A>> {

    private LocalDateTime nextActivationDate;
    private Duration defaultInterval = Duration.ofSeconds(1);

    public DateBasedDiscreteEventActivator(String community, String group, String role, String theBehaviorToActivate) {
	super(community, group, role, theBehaviorToActivate);
    }

    /**
     * Defines an ordering so that the scheduler can classify activators according to their date and priority.
     */
    @Override
    public int compareTo(DateBasedDiscreteEventActivator<A> o) {
	int result = getNextActivationDate().compareTo(o.getNextActivationDate());
	if (result == 0) {
	    return Integer.compare(getPriority(), o.getPriority());
	}
	return result;
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
     * @param nextActivationDate a {@link LocalDateTime} which should be greater
     * than the current simulation time
     */
    public void setNextActivationDate(LocalDateTime nextActivationDate) {
	this.nextActivationDate = nextActivationDate;
    }

    @Override
    public void execute(List<A> agents, Object... args) {
	super.execute(agents, args);
	setNextActivationDate(getSimulationTime().getCurrentDate().plus(defaultInterval));
    }

    @Override
    public String toString() {
	return super.toString() + " ; nextDate = " + getNextActivationDate();
    }

    public Duration getDefaultInterval() {
	return defaultInterval;
    }

    public void setDefaultInterval(Duration defaultInterval) {
	this.defaultInterval = defaultInterval;
    }

}
