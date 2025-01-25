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

import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_E;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;

import madkit.kernel.Agent;
import madkit.reflection.ReflectionUtils;

/**
 * This enumeration contains common agent actions that they can perform.
 * <p>
 * It is designed to ease the creation of GUIs for agents by providing a way to create
 * {@link Action}s for common agent actions.
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * {@code
 * AgentAction action = AgentAction.LAUNCH_AGENT;
 * Action fxAction = action.newActionFor(agent, parameters);
 * }
 * </pre>
 * 
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 * @see madkit.kernel.Agent
 * @see madkit.gui.ActionFromEnum
 * @see madkit.messages.EnumMessage
 * 
 */
public enum AgentAction {

	/**
	 * Action to launch an agent.
	 */
	LAUNCH_AGENT(KeyEvent.VK_U),
	/**
	 * Action to reload an agent.
	 */
	RELOAD(VK_E),
	/**
	 * Action to create a group.
	 */
	CREATE_GROUP(VK_DOLLAR),
	/**
	 * Action to request a role.
	 */
	REQUEST_ROLE(VK_DOLLAR),
	/**
	 * Action to leave a role.
	 */
	LEAVE_ROLE(VK_DOLLAR),
	/**
	 * Action to leave a group.
	 */
	LEAVE_GROUP(VK_DOLLAR),
	/**
	 * Action to send a message.
	 */
	SEND_MESSAGE(VK_DOLLAR),
	/**
	 * Action to broadcast a message.
	 */
	BROADCAST_MESSAGE(VK_DOLLAR),

	/**
	 * Action to kill an agent.
	 */
	KILL_AGENT(KeyEvent.VK_K);

	/** The action data associated with this agent action. */
	private ActionData actionInfo;

	/**
	 * Constructs an AgentAction with the specified key event.
	 * 
	 * @param keyEvent the key event associated with the action
	 */
	private AgentAction(int keyEvent) {
		actionInfo = new ActionData(name(), keyEvent);
	}

	/**
	 * Builds an FX action that will make the agent perform the corresponding operation if
	 * possible.
	 * 
	 * @param agent      the agent that will send the message to the kernel
	 * @param parameters the parameters for the action
	 * @return the new corresponding action
	 */
	public Action getActionFor(Agent agent, Object... parameters) {
		AgentMethodAction action = new AgentMethodAction(agent, ReflectionUtils.enumToMethodName(this), parameters);
		action.setActionData(actionInfo);
		return action;
	}

	/**
	 * Creates an action group for the given agent. The action group includes actions for
	 * reloading, launching, and killing the agent.
	 * 
	 * @param agent the agent for which to create the action group
	 * @return an ActionGroup containing the actions for the agent
	 */
	public static ActionGroup createAgentFxActionGroup(Agent agent) {
		Collection<Action> actions = new ArrayList<>();
		try {
			if (agent.getClass().getConstructor((Class<?>[]) null) != null) {
				actions.add(RELOAD.getActionFor(agent));
				actions.add(LAUNCH_AGENT.getActionFor(agent, agent.getClass().getName(), true, 0));
			}
		} catch (SecurityException | NoSuchMethodException e) {
			agent.getLogger().finest(() -> "No default constructor -> Cannot create all FX actions");
		}
		actions.add(KILL_AGENT.getActionFor(agent, agent, 2));
		actions.add(KernelAction.EXIT.newActionFor(agent));
		return new ActionGroup("Agent", actions);
	}
}
