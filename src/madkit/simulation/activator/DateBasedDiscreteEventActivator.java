package madkit.simulation.activator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import madkit.kernel.AbstractAgent;

public class DateBasedDiscreteEventActivator<A extends AbstractAgent> extends GenericBehaviorActivator<A> implements Comparable<DateBasedDiscreteEventActivator<A>> {
    
    private LocalDateTime nextActivationDate;
    private int priority = 0;
    private Duration defaultInterval = Duration.ofDays(1);

    public DateBasedDiscreteEventActivator(String community, String group, String role, String theBehaviorToActivate) {
	super(community, group, role, theBehaviorToActivate);
    }

    @Override
    public int compareTo(DateBasedDiscreteEventActivator<A> o) {
	int result = getNextActivationDate().compareTo(o.getNextActivationDate());
	if(result == 0) {
	    return Integer.compare(getPriority(), o.getPriority());
	}
	return result;
    }

    public LocalDateTime getNextActivationDate() {
	return nextActivationDate;
    }

    public void setNextActivationDate(LocalDateTime nextActivationDate) {
	this.nextActivationDate = nextActivationDate;
    }
    
    @Override
    public void execute(List<A> agents, Object... args) {
        super.execute(agents, args);
        setNextActivationDate(getSimulationTime().getCurrentDate().plusDays(1));
    }

    public int getPriority() {
	return priority;
    }

    public void setPriority(int priority) {
	this.priority = priority;
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
