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
package madkit.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;

import javafx.collections.ObservableList;
import madkit.kernel.Agent;
import madkit.kernel.Scheduler;

/**
 * 
 * Utility class for creating {@link Action} for agents.
 * 
 */
public class Actions {

	private Actions() {
		throw new IllegalAccessError();
	}

	/**
	 * Creates an action group for the given scheduler agent. The action group includes
	 * actions for running and pausing the scheduler.
	 * 
	 * @param scheduler the scheduler agent for which to create the action group
	 * @return an ActionGroup containing the actions for the scheduler
	 */
	public static ActionGroup createSchedulerActionGroup(Scheduler<?> scheduler) {
		return SchedulingAction.createActionGroup(scheduler);
	}

	/**
	 * Creates an MaDKit actions group for the given agent. The action group includes actions
	 * for various MaDKit operations.
	 * 
	 * @param agent the agent for which to create the action
	 * @return an action group containing the default MaDKit actions for the agent
	 */
	public static ActionGroup createMadkitActionGroupFor(Agent agent) {
		Collection<Action> actions = new ArrayList<>();
		actions.add(GlobalAction.JCONSOLE.getFXAction());
		actions.add(ActionUtils.ACTION_SEPARATOR);
		actions.add(KernelAction.COPY.newActionFor(agent));
		actions.add(KernelAction.RESTART.newActionFor(agent));
		actions.add(KernelAction.EXIT.newActionFor(agent));
		actions.add(ActionUtils.ACTION_SEPARATOR);
		return new ActionGroup("MaDKit", actions);
	}

	/**
	 * Creates an action group for the given agent. The action group includes actions for
	 * launching and killing the agent.
	 * 
	 * @param agent the agent for which to create the action group
	 * @return an action group containing the default actions for the agent
	 */
	public static ActionGroup createAgentActionGroupFor(Agent agent) {
		Collection<Action> actions = new ArrayList<>();
		try {
			if (agent.getClass().getConstructor((Class<?>[]) null) != null) {
				actions.add(AgentAction.LAUNCH_AGENT.getActionFor(agent, agent.getClass().getName(), 0));
			}
		} catch (SecurityException | NoSuchMethodException e) {
			// do nothing: the agent has no default constructor
		}
		actions.add(AgentAction.KILL_AGENT.getActionFor(agent, agent, 2));
		return new ActionGroup("Agent", actions);
	}

	/**
	 * Creates an action group for the given agent. The action group includes actions for
	 * launching and killing the agent, and for restarting, copying or quitting the MaDKit
	 * instance.
	 * 
	 * @param agent the agent for which to create the action group
	 * @return a list of actions for the agent
	 */
	public static List<Action> createMDKActionsListFor(Agent agent) {
		return List.of(GlobalAction.JCONSOLE.getFXAction(), ActionUtils.ACTION_SEPARATOR,
				KernelAction.COPY.newActionFor(agent), KernelAction.RESTART.newActionFor(agent),
				KernelAction.EXIT.newActionFor(agent));
	}

	/**
	 * Returns the MaDKit actions for the given agent.
	 * 
	 * @param agent the agent for which to get the actions
	 * @return an ObersvableList of actions for the agent
	 */
	public static ObservableList<Action> getMadkitActions(Agent agent) {
		return createMadkitActionGroupFor(agent).getActions();
	}

}
