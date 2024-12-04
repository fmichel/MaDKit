
package madkit.action;

import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_E;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;

import madkit.gui.fx.FXAction;
import madkit.kernel.Agent;
import madkit.messages.EnumMessage;

/**
 * Enum representing agent actions. Each action is associated with a keyboard
 * event.
 * 
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
 * @see madkit.gui.fx.FXAction
 * @see madkit.messages.EnumMessage
 * 
 * @author Fabien Michel
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
	 * Builds an FX action that will make the agent perform the corresponding
	 * operation if possible.
	 * 
	 * @param agent      the agent that will send the message to the kernel
	 * @param parameters the parameters for the action
	 * @return the new corresponding action
	 */
	public Action newActionFor(final Agent agent, final Object... parameters) {
		return new FXAction(actionInfo, ae -> {
			if (agent.isAlive()) {
				agent.proceedEnumMessage(new EnumMessage<>(AgentAction.this, parameters));
			}
		});
	}

	/**
	 * Creates an action group for the given agent. The action group includes
	 * actions for reloading, launching, and killing the agent.
	 * 
	 * @param agent the agent for which to create the action group
	 * @return an ActionGroup containing the actions for the agent
	 */
	public static final ActionGroup createAgentFxActionGroup(Agent agent) {
		Collection<Action> actions = new ArrayList<>();
		try {
			if (agent.getClass().getConstructor((Class<?>[]) null) != null) {
				actions.add(RELOAD.newActionFor(agent));
				actions.add(LAUNCH_AGENT.newActionFor(agent, agent.getClass().getName(), true, 0));
			}
		} catch (SecurityException | NoSuchMethodException e) {
			agent.getLogger().finest(() -> "No default constructor -> Cannot create all FX actions");
		}
		actions.add(KILL_AGENT.newActionFor(agent, agent, 2));
		actions.add(KernelAction.EXIT.newActionFor(agent));
		return new ActionGroup("Agent", actions);
	}
}
