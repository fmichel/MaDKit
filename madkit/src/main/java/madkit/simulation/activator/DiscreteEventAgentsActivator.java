package madkit.simulation.activator;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import madkit.kernel.Agent;

/**
 * An activator that works using a discrete-event simulation scheme.
 *
 * @author Fabien Michel
 *
 * @param <A> The most common class of the simulated agents
 */
public class DiscreteEventAgentsActivator extends DateBasedDiscreteEventActivator {

	protected PriorityQueue<Agent> activationList = new PriorityQueue<>(
			(Agent o1, Agent o2) -> o1.getNextEventDate().compareTo(o2.getNextEventDate()));

	public DiscreteEventAgentsActivator(String community, String group, String role, String theBehaviorToActivate) {
		super(community, group, role, theBehaviorToActivate);
		setNextActivationDate(LocalDateTime.MAX);
	}

	@Override
	protected void adding(Agent agent) {
		LocalDateTime currentDate = getNextActivationDate();
		LocalDateTime date = agent.getNextEventDate();
		activationList.add(agent);
		if(date.compareTo(currentDate) < 0) {
			getScheduler().updateActivatorRanking(this);
		}
	}
	
	@Override
	public LocalDateTime getNextActivationDate() {
		return activationList.isEmpty() ? LocalDateTime.MAX : activationList.peek().getNextEventDate(); 
	}

	@Override
	protected void removing(Agent agent) {
		LocalDateTime currentDate = getNextActivationDate();
		activationList.remove(agent);
		if(activationList.isEmpty()) {
			setNextActivationDate(LocalDateTime.MAX);
		}
		if(! getNextActivationDate().equals(currentDate)) {
			getScheduler().updateActivatorRanking(this);
		}
	}

	@Override
	public void execute(Object... args) {
			if (! activationList.isEmpty()) {
				Agent a = activationList.poll();
				executeBehaviorOf(a, getMethod(), args);
				if (! activationList.contains(a) && getCurrentAgentsList().contains(a)) {
					adding(a);
				} 
			}
	}

	@Override
	public String toString() {
		return super.toString() + " ; nextDate = " + getNextActivationDate();
	}

}
