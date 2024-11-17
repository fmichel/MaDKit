package madkit.simulation.activator;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import madkit.kernel.Agent;
import madkit.simulation.SimuAgent;

/**
 * An activator that works using a discrete-event simulation scheme.
 *
 * @author Fabien Michel
 *
 * @param <A> The most common class of the simulated agents
 */
public class DiscreteEventAgentsActivator extends DateBasedDiscreteEventActivator {

	protected PriorityQueue<SimuAgent> activationList = new PriorityQueue<>(
			(SimuAgent o1, SimuAgent o2) -> o1.getNextEventDate().compareTo(o2.getNextEventDate()));

	public DiscreteEventAgentsActivator(String community, String group, String role, String theBehaviorToActivate) {
		super(community, group, role, theBehaviorToActivate);
		setNextActivationDate(LocalDateTime.MAX);
	}

	@Override
	protected void onAdding(Agent agent) {
		LocalDateTime currentDate = getNextActivationDate();
		SimuAgent sa = (SimuAgent) agent;
		LocalDateTime date = sa.getNextEventDate();
		activationList.add(sa);
		if(date.compareTo(currentDate) < 0) {
			getScheduler().updateActivatorRanking(this);
		}
	}
	
	@Override
	public LocalDateTime getNextActivationDate() {
		return activationList.isEmpty() ? LocalDateTime.MAX : activationList.peek().getNextEventDate(); 
	}

	@Override
	protected void onRemoving(Agent agent) {
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
					onAdding(a);
				} 
			}
	}

	@Override
	public String toString() {
		return super.toString() + " ; nextDate = " + getNextActivationDate();
	}

}
