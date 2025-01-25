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

import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_R;

import org.controlsfx.control.action.Action;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.SystemRoles;
import madkit.gui.ActionFromEnum;
import madkit.kernel.Agent;
import madkit.messages.KernelMessage;

/**
 * Enum representing kernel actions. This especially could be used to communicate with the
 * kernel in order to trigger kernel's actions.
 * 
 * @since MaDKit 5.0.0.14
 * @version 6.0
 */

public enum KernelAction {

	/**
	 * Close this MaDKit session
	 */
	EXIT(VK_Q),
	/**
	 * Clone the MaDKit session with its initial options
	 */
	COPY(VK_C),
	/**
	 * Restart MaDKit with its initial options
	 */
	RESTART(VK_R),

	// //Actions that need parameters, i.e. not global
	/**
	 * Launch an agent
	 */
	LAUNCH_AGENT(VK_DOLLAR),
	/**
	 * Kill an agent
	 */
	KILL_AGENT(VK_DOLLAR);

	private ActionData actionInfo;

	private KernelAction(int keyEvent) {
		actionInfo = new ActionData(name(), keyEvent);
	}

	/**
	 * Returns the action associated with this enum constant for the specified agent, and with
	 * the specified parameters.
	 * 
	 * @param agent      the agent that will perform the action
	 * @param parameters the parameters to be used by the action
	 * @return an action that can be used to perform the action
	 */
	public Action newActionFor(Agent agent, Object... parameters) {
		return new ActionFromEnum(actionInfo, _ -> request(agent, parameters));
	}

	/**
	 * Sends a message to the kernel to perform the action associated with this enum constant,
	 * with the specified parameters.
	 * 
	 * @param requester  the agent that will perform the action
	 * @param parameters the parameters to be used by the action
	 */
	public void request(Agent requester, Object... parameters) {
		if (requester.isAlive()) {
			requester.send(new KernelMessage(KernelAction.this, parameters), LocalCommunity.NAME, Groups.SYSTEM,
					SystemRoles.GROUP_MANAGER);
		}
	}

}
