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
 */
public class DiscreteEventAgentsActivator extends DateBasedDiscreteEventActivator {

	/**
	 * The priority queue that holds the simulated agents based on their next event
	 * date.
	 */
	protected PriorityQueue<SimuAgent> activationList = new PriorityQueue<>(
			(SimuAgent o1, SimuAgent o2) -> o1.getNextEventDate().compareTo(o2.getNextEventDate()));

	/**
	 * Constructs a new DiscreteEventAgentsActivator with the specified group, role,
	 * and behavior to activate.
	 *
	 * @param group                 the group of the agents
	 * @param role                  the role of the agents
	 * @param theBehaviorToActivate the behavior to activate
	 */
	public DiscreteEventAgentsActivator(String group, String role, String theBehaviorToActivate) {
		super(group, role, theBehaviorToActivate);
		setNextActivationDate(LocalDateTime.MAX);
	}

	/**
	 * Called when an agent is added to the activator. Updates the activation list
	 * and the next activation date.
	 *
	 * @param agent the agent being added
	 */
	@Override
	protected void onAdding(Agent agent) {
		LocalDateTime currentDate = getNextActivationDate();
		SimuAgent sa = (SimuAgent) agent;
		LocalDateTime date = sa.getNextEventDate();
		activationList.add(sa);
		if (date.compareTo(currentDate) < 0) {
			getScheduler().updateActivatorRanking(this);
		}
	}

	/**
	 * Returns the next activation date based on the activation list.
	 *
	 * @return the next activation date
	 */
	@Override
	public LocalDateTime getNextActivationDate() {
		return activationList.isEmpty() ? LocalDateTime.MAX : activationList.peek().getNextEventDate();
	}

	/**
	 * Called when an agent is removed from the activator. Updates the activation
	 * list and the next activation date.
	 *
	 * @param agent the agent being removed
	 */
	@Override
	protected void onRemoving(Agent agent) {
		LocalDateTime currentDate = getNextActivationDate();
		activationList.remove(agent);
		if (activationList.isEmpty()) {
			setNextActivationDate(LocalDateTime.MAX);
		}
		if (!getNextActivationDate().equals(currentDate)) {
			getScheduler().updateActivatorRanking(this);
		}
	}

	/**
	 * Executes the behavior of the next agent in the activation list.
	 *
	 * @param args the arguments to pass to the behavior
	 */
	@Override
	public void execute(Object... args) {
		if (!activationList.isEmpty()) {
			Agent a = activationList.poll();
			executeBehaviorOf(a, getMethod(), args);
			if (!activationList.contains(a) && getCurrentAgentsList().contains(a)) {
				onAdding(a);
			}
		}
	}

	/**
	 * Returns a string representation of the activator, including the next
	 * activation date.
	 *
	 * @return a string representation of the activator
	 */
	@Override
	public String toString() {
		return super.toString() + " ; nextDate = " + getNextActivationDate();
	}
}
