package madkit.action;

import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_E;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;

import madkit.kernel.Agent;
import madkit.gui.fx.FXAction;
import madkit.messages.EnumMessage;

/**
 * Enum representing agent actions
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 */
public enum AgentAction {

	LAUNCH_AGENT(KeyEvent.VK_U), 
	RELOAD(VK_E), 
	CREATE_GROUP(VK_DOLLAR), 
	REQUEST_ROLE(VK_DOLLAR), 
	LEAVE_ROLE(VK_DOLLAR),
	LEAVE_GROUP(VK_DOLLAR), 
	SEND_MESSAGE(VK_DOLLAR), 
	BROADCAST_MESSAGE(VK_DOLLAR), 
	KILL_AGENT(KeyEvent.VK_K);

	private ActionData actionInfo;

	private AgentAction(int keyEvent) {
		actionInfo = new ActionData(name(), keyEvent);
	}

	/**
	 * Builds an FX action that will make the kernel do the corresponding operation
	 * if possible.
	 * 
	 * @param agent      the agent that will send the message to the kernel
	 * @param parameters the info
	 * @return the new corresponding action
	 */
	public Action newActionFor(final Agent agent, final Object... parameters) {
		return new FXAction(actionInfo, ae -> {
			if (agent.isAlive()) {
				agent.proceedEnumMessage(new EnumMessage<>(AgentAction.this, parameters));
			}
		});
	}

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
