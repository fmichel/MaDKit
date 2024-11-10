package madkit.action;

import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_U;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;

import org.controlsfx.control.action.ActionGroup;

import madkit.gui.fx.FXAction;
import madkit.kernel.AbstractScheduler;
import madkit.messages.SchedulingMessage;

/**
 * Enum representing operations which could be done by a {@link Scheduler}
 * agent. It could be used by an agent to interact with the scheduler by
 * creating {@link Action} using {@link #getActionFor(Scheduler, Object...)}.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 6
 * 
 */
public enum SchedulingAction {

	RUN(VK_P), 
	STEP(VK_U), 
	SPEED_UP(VK_RIGHT), 
	SPEED_DOWN(VK_LEFT), 
	PAUSE(VK_DOLLAR), 
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

//	/**
//	 * Builds an action that will make the corresponding scheduler do the related
//	 * operation if possible.
//	 * 
//	 * @param theScheduler the scheduler on which the action will be triggered if
//	 *                     possible
//	 * @param parameters   the info
//	 * @return the corresponding action
//	 */
//	public Action getActionFor(final AbstractScheduler<?> theScheduler, final Object... parameters) {
//		return new MDKAbstractAction(getActionData()) {
//			private static final long serialVersionUID = 5434867603425806658L;
//
//			@Override
//			public void actionPerformed(ActionEvent e) {// TODO I could do the check validity here for logging purpose
//				theScheduler.receiveMessage(new SchedulingMessage(SchedulingAction.this, parameters));// TODO work with
//																										// AA but this
//																										// is probably
//																										// worthless
//			}
//		};
//	}

	/**
	 * Builds an FX action that will make the kernel do the corresponding operation
	 * if possible.
	 * 
	 * @param agent      the agent that will send the message to the kernel
	 * @param parameters the info
	 * @return the new corresponding action
	 */
	public org.controlsfx.control.action.Action getFxActionFrom(final AbstractScheduler<?> agent, final Object... parameters) {
		return new FXAction(getActionData(), ae -> {
			if (agent.isAlive()) {
				agent.receiveMessage(new SchedulingMessage(SchedulingAction.this, parameters));// TODO work with AA but
																								// this is probably
																								// worthless
			}
		});
	}

	public static final ActionGroup createAgentFxActionGroup(final AbstractScheduler<?> agent) {
		Collection<org.controlsfx.control.action.Action> actions = new ArrayList<>();
		actions.add(RUN.getFxActionFrom(agent));
		actions.add(STEP.getFxActionFrom(agent));
//		actions.add(SPEED_UP.getFxActionFor(agent));
//		actions.add(SPEED_DOWN.getFxActionFor(agent));
		return new ActionGroup("Scheduling", actions);
	}

}
