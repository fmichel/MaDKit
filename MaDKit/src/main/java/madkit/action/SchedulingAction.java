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
 * Enum representing operations which could be done by a {@link Scheduler} agent. It could
 * be used by an agent to interact with the scheduler by creating actions using
 * {@link #getActionFrom(Scheduler, Object...)}.
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
	 * Returns the action data corresponding to this enum.
	 * 
	 * @return the actionData corresponding to this enum.
	 */
	public ActionData getActionData() {
		if (actionData == null) {
			actionData = new ActionData(name(), keyEvent);
		}
		return actionData;
	}

	/**
	 * Builds an FX action that will make the kernel do the corresponding operation if
	 * possible.
	 * 
	 * @param agent      the agent that will send the message to the scheduler
	 * @param parameters the parameters to send to the scheduler
	 * @return the new corresponding action
	 */
	public Action getActionFrom(Scheduler<?> agent, Object... parameters) {
		return new ActionFromEnum(getActionData(), _ -> {
			if (agent.isAlive()) {
				agent.receiveMessage(new SchedulingMessage(SchedulingAction.this, parameters));
			}
		});
	}

	/**
	 * Creates an action group for the given {@link Scheduler} agent. The action group
	 * includes actions for running and pausing the scheduler.
	 * 
	 * @param scheduler the scheduler agent to create the action group for
	 * @return the action group corresponding to the given scheduler
	 */
	public static ActionGroup createActionGroup(Scheduler<?> scheduler) {
		Collection<Action> actions = new ArrayList<>();
		actions.add(RUN.getActionFrom(scheduler));
		actions.add(STEP.getActionFrom(scheduler));
		return new ActionGroup("Scheduling", actions);
	}

}
