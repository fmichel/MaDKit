package madkit.simulation.activator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;

import madkit.kernel.AbstractAgent;

/**
 * An activator that works using a discrete-event simulation scheme.
 *
 * @author Fabien Michel
 *
 * @param <A>
 */
public class DiscreteEventAgentsActivator<A extends AbstractAgent> extends GenericBehaviorActivator<A> implements Comparable<DiscreteEventAgentsActivator<A>> {

    private LocalDateTime nextActivationDate;
    private Duration defaultInterval = Duration.ofSeconds(1);
    private PriorityQueue<A> agentsActivationList = new PriorityQueue<>(new Comparator<A>() {
	public int compare(AbstractAgent o1, AbstractAgent o2) {
	    return o1.getNextEventDate().compareTo(o2.getNextEventDate());
	};
    });

    public DiscreteEventAgentsActivator(String community, String group, String role, String theBehaviorToActivate) {
	super(community, group, role, theBehaviorToActivate);
    }

    @Override
    public int compareTo(DiscreteEventAgentsActivator<A> o) {
	int result = getNextActivationDate().compareTo(o.getNextActivationDate());
	if(result == 0) {
	    return Integer.compare(getPriority(), o.getPriority());
	}
	return result;
    }

    public LocalDateTime getNextActivationDate() {
	return nextActivationDate;
    }

    @Override
    protected void adding(A agent) {
        super.adding(agent);
        agentsActivationList.add(agent);
    }

    @Override
    protected void removing(A agent) {
        super.removing(agent);
        agentsActivationList.remove(agent);
    }

    public void setNextActivationDate(LocalDateTime nextActivationDate) {
	this.nextActivationDate = nextActivationDate;
    }

    @Override
    public void execute(Object... args) {
	while(agentsActivationList.size()>0 && agentsActivationList.peek().getNextEventDate().equals(nextActivationDate)) {
	    A a = agentsActivationList.poll();
	    executeBehaviorOf(a, getBehaviorName(), args);
	    agentsActivationList.add(a);
	}
        setNextActivationDate(agentsActivationList.peek().getNextEventDate());
    }

    @Override
    public String toString() {
        return super.toString()+" ; nextDate = "+getNextActivationDate();
    }

    public Duration getDefaultInterval() {
	return defaultInterval;
    }

    public void setDefaultInterval(Duration defaultInterval) {
	this.defaultInterval = defaultInterval;
    }


}
