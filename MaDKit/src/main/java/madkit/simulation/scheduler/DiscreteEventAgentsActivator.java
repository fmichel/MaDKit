/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.simulation.scheduler;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import madkit.kernel.Agent;
import madkit.simulation.SimuAgent;

/**
 * An activator that works using a discrete-event simulation scheme.
 *
 */
public class DiscreteEventAgentsActivator extends DateBasedDiscreteEventActivator {

	/**
	 * The priority queue that holds the simulated agents based on their next event date.
	 */
	protected PriorityQueue<SimuAgent> activationList = new PriorityQueue<>(
			(SimuAgent o1, SimuAgent o2) -> o1.getNextEventDate().compareTo(o2.getNextEventDate()));

	/**
	 * Constructs a new DiscreteEventAgentsActivator with the specified group, role, and
	 * behavior to activate.
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
	 * Called when an agent is added to the activator. Updates the activation list and the
	 * next activation date.
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
	 * Called when an agent is removed from the activator. Updates the activation list and the
	 * next activation date.
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
			if (!activationList.contains(a) && getAgents().contains(a)) {
				onAdding(a);
			}
		}
	}

	/**
	 * Returns a string representation of the activator, including the next activation date.
	 *
	 * @return a string representation of the activator
	 */
	@Override
	public String toString() {
		return super.toString() + " ; nextDate = " + getNextActivationDate();
	}
}
