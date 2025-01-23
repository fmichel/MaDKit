package madkit.action;

import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_U;

import java.util.ArrayList;
import java.util.Collection;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;

import madkit.gui.ActionFromEnum;
import madkit.kernel.Scheduler;
import madkit.messages.SchedulingMessage;

/**
 * Enum representing operations which could be done by a {@link Scheduler}
 * agent. It could be used by an agent to interact with the scheduler by
 * creating actions using {@link #getActionFrom(Scheduler, Object...)}.
 * 
 * @version 6
 * 
 */
public enum SchedulingAction {

	/**
	 * Makes the scheduler run the simulation
	 */
	RUN(VK_P),
	/**
	 * Makes the scheduler do one step of the simulation
	 */
	STEP(VK_U),
	/**
	 * Makes the scheduler pause the simulation
	 */
	PAUSE(VK_DOLLAR),
	/**
	 * Makes the scheduler stop the simulation
	 */
	SHUTDOWN(VK_DOLLAR);

	private ActionData actionData;
	private final int keyEvent;

	private SchedulingAction(int keyEvent) {
		this.keyEvent = keyEvent;
	}

	/**
	 * @return the actionData
	 */
	public ActionData getActionData() {
		if (actionData == null)
			actionData = new ActionData(name(), keyEvent);
		return actionData;
	}

	/**
	 * Builds an FX action that will make the kernel do the corresponding operation
	 * if possible.
	 * 
	 * @param agent      the agent that will send the message to the kernel
	 * @param parameters the info
	 * @return the new corresponding action
	 */
	public Action getActionFrom(Scheduler<?> agent, Object... parameters) {
		return new ActionFromEnum(getActionData(), _ -> {
			if (agent.isAlive()) {
				agent.receiveMessage(new SchedulingMessage(SchedulingAction.this, parameters));
			}
		});
	}

	public static ActionGroup createAgentFxActionGroup(Scheduler<?> agent) {
		Collection<org.controlsfx.control.action.Action> actions = new ArrayList<>();
		actions.add(RUN.getActionFrom(agent));
		actions.add(STEP.getActionFrom(agent));
//		actions.add(SPEED_UP.getFxActionFor(agent));
//		actions.add(SPEED_DOWN.getFxActionFor(agent));
		return new ActionGroup("Scheduling", actions);
	}

}
