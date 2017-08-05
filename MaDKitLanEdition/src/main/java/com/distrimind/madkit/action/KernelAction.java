/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.action;

import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_W;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.Action;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.i18n.I18nUtilities;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.network.AskForConnectionMessage;
import com.distrimind.madkit.kernel.network.AskForTransferMessage;
import com.distrimind.madkit.message.KernelMessage;

/**
 * Enum representing kernel actions. This especially could be used to
 * communicate with the kernel in order to trigger kernel's actions. It could be
 * used by any agent to interact with the kernel by creating {@link Action}
 * using {@link #getActionFor(AbstractAgent, Object...)}.
 * 
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.14
 * @version 0.93
 * 
 */

public enum KernelAction {

	/**
	 * Close the kernel
	 */
	EXIT(VK_Q),
	/**
	 * Clone the kernel with its initial options
	 */
	COPY(VK_C),
	/**
	 * Restart the kernel with its initial options
	 */
	RESTART(VK_R),
	/**
	 * Start the network
	 */
	LAUNCH_NETWORK(VK_W),
	/**
	 * Stop the network
	 */
	STOP_NETWORK(VK_T),

	/**
	 * Makes a redirection of the out and err to a MaDKit agent.
	 */
	CONSOLE(VK_O),

	// //Actions that need parameters, i.e. not global
	/**
	 * Launch an agent
	 */
	LAUNCH_AGENT(VK_DOLLAR),
	/**
	 * Launch a MAS configuration
	 */
	LAUNCH_MAS(VK_DOLLAR),
	/**
	 * Launch an XML configuration
	 */
	LAUNCH_XML(VK_DOLLAR),
	/**
	 * Kill an agent
	 */
	KILL_AGENT(VK_DOLLAR),
	/**
	 * Connection to the MaDKit web repository
	 */
	CONNECT_WEB_REPO(VK_DOLLAR),
	/**
	 * For connecting/disconnecting two kernels directly in a wide area network. It
	 * requires a parameter of type {@link AskForConnectionMessage}.
	 */
	MANAGE_DIRECT_DONNECTION(VK_DOLLAR),

	/**
	 * For connecting/disconnecting two kernels indirectly by making data transfered
	 * by the current kernel to constitute a meshed network. It requires a parameter
	 * of type {@link AskForTransferMessage}.
	 */
	MANAGE_TRANSFER_CONNECTION(VK_DOLLAR),

	/**
	 * Cancel a programmed execution of a task which would be executed by the
	 * TaskAgent identified by the given name.
	 */
	CANCEL_TASK(java.awt.event.KeyEvent.VK_A),

	/**
	 * Add a new task to be executed at a specific time by the task agent which
	 * correspond to the given task agent name.
	 */
	SCHEDULE_TASK(java.awt.event.KeyEvent.VK_H),

	/**
	 * Add a new collection of tasks to be executed at a specific time by the task
	 * agent which correspond to the given task agent name.
	 */
	SCHEDULE_TASKS(java.awt.event.KeyEvent.VK_1),

	/**
	 * Kill a Task Manager Agent
	 */
	KILL_TASK_MANAGER_AGENT(java.awt.event.KeyEvent.VK_2),

	/**
	 * Launch a Task Manager Agent
	 */
	LAUNCH_TASK_MANAGER_AGENT(java.awt.event.KeyEvent.VK_3),

	/**
	 * Set threads priority related to a specific Task Manager Agent
	 */
	SET_TASK_MANAGER_AGENT_PRIORITY(java.awt.event.KeyEvent.VK_4);

	private ActionInfo actionInfo;
	final private int keyEvent;

	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(KernelAction.class.getSimpleName());

	private KernelAction(int keyEvent) {
		this.keyEvent = keyEvent;
	}

	/**
	 * Builds an action that will make the kernel do the corresponding operation if
	 * possible.
	 * 
	 * @param agent
	 *            the agent that will send the message to the kernel
	 * @param parameters
	 *            the info
	 * @return the new corresponding action
	 */
	public Action getActionFor(final AbstractAgent agent, final Object... parameters) {
		return new MDKAbstractAction(getActionInfo()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8907472475007112860L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (agent.isAlive()) {
					agent.sendMessage(LocalCommunity.Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE,
							new KernelMessage(KernelAction.this, parameters));// TODO work with AA but this is probably
																				// worthless
				}

			}
		};
	}

	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if (actionInfo == null)
			actionInfo = new ActionInfo(this, keyEvent, messages);
		return actionInfo;
	}

}
