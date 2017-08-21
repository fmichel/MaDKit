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

import static java.awt.event.KeyEvent.VK_N;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JOptionPane;

import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.agr.LocalCommunity.Groups;
import com.distrimind.madkit.agr.LocalCommunity.Roles;
import com.distrimind.madkit.i18n.I18nUtilities;
import com.distrimind.madkit.i18n.Words;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.network.AskForConnectionMessage;
import com.distrimind.madkit.kernel.network.ConnectionStatusMessage.Type;
import com.distrimind.madkit.kernel.network.DoubleIP;
import com.distrimind.madkit.message.GUIMessage;
import com.distrimind.madkit.message.KernelMessage;

/**
 * Enum representing operations which could be done by the default GUI manager
 * of MaDKit. It could be used by an agent to interact with the GUI manager by
 * creating {@link Action} using
 * {@link #getActionFor(AbstractAgent, Object...)}.
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.14
 * @see Action
 * @version 0.91
 * 
 */
public enum GUIManagerAction {

	/**
	 * For connecting kernels in a wide area network
	 */
	CONNECT_TO_IP(VK_N),
	/**
	 * Iconify all the agent frames
	 */
	ICONIFY_ALL(KeyEvent.VK_M),
	/**
	 * Deiconify all the agent frames
	 */
	DEICONIFY_ALL(KeyEvent.VK_I),
	/**
	 * Kills all the agents having a GUI
	 */
	KILL_AGENTS(KeyEvent.VK_G),
	// CONNECT_WEB_REPO(VK_W),

	/**
	 * Requests an agent frame creation. The corresponding action should be created
	 * by specifying the targeted agent:
	 * 
	 * <pre>
	 * <code>
	 * SETUP_AGENT_GUI.getActionFor(anAgent, targetedAgent);
	 * </code>
	 * </pre>
	 * 
	 * They could be identical.
	 * 
	 * @see AbstractAgent#setupFrame(javax.swing.JFrame)
	 */
	SETUP_AGENT_GUI(KeyEvent.VK_DOLLAR), LOG_LEVEL(KeyEvent.VK_DOLLAR), WARNING_LOG_LEVEL(KeyEvent.VK_DOLLAR),
	/**
	 * Requests an agent frame disposal: This will kill the agent. The corresponding
	 * action should be created by specify the targeted agent:
	 * 
	 * <pre>
	 * <code>
	 * SETUP_AGENT_GUI.getActionFor(anAgent, targetedAgent);
	 * </code>
	 * </pre>
	 * 
	 * They could be identical.
	 */
	DISPOSE_AGENT_GUI(KeyEvent.VK_DOLLAR);

	private ActionInfo actionInfo;
	final private int keyEvent;

	final static private ResourceBundle messages = I18nUtilities
			.getResourceBundle(GUIManagerAction.class.getSimpleName());

	private GUIManagerAction(int keyEvent) {
		this.keyEvent = keyEvent;
	}

	/**
	 * Returns an Action that will send to the GUI manager the corresponding
	 * request. The corresponding action should be created by specifying the agent
	 * for which this action is created. Here is an example :
	 * 
	 * <pre>
	 * <code>
	 * KILL_AGENTS.getActionFor(anAgent);
	 * </code>
	 * </pre>
	 * 
	 * This will create an agent that will make <code>anAgent</code> send a message
	 * to the gui manager asking the kill of all GUI agents.
	 * 
	 * @param agent
	 *            the agent for which this Action will be created
	 * @param commandOptions
	 *            optional information related to the action itself
	 * @return an Action that could be used in an GUI for instance
	 */
	public Action getActionFor(final AbstractAgent agent, final Object... commandOptions) {
		if (this == CONNECT_TO_IP) {

			return new MDKAbstractAction(getActionInfo()) {
				/**
				 * 
				 */
				private static final long serialVersionUID = -5716094161691491218L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (agent.isAlive()) {
						try {
							final String port = JOptionPane.showInputDialog(null,
									getActionInfo().getName() + " (Port) : ");
							final String ip = JOptionPane.showInputDialog(null, getActionInfo().getName() + " (IP) : ");
							if (ip != null) {
								agent.sendMessage(LocalCommunity.Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE,
										new KernelMessage(KernelAction.MANAGE_DIRECT_DONNECTION,
												new AskForConnectionMessage(Type.CONNECT,
														new DoubleIP(new InetSocketAddress(InetAddress.getByName(ip),
																Integer.parseInt(port))))));
							}
						} catch (HeadlessException e1) {
							e1.printStackTrace();
						} catch (UnknownHostException e1) {
							JOptionPane.showMessageDialog(null, e1, Words.FAILED.toString(),
									JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			};
		}
		return new MDKAbstractAction(getActionInfo()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2231685794614332333L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (agent.isAlive()) {
					final Message m = new GUIMessage(GUIManagerAction.this, commandOptions);
					final AgentAddress guiManager = agent.getAgentWithRole(Groups.GUI, Roles.GUI);
					if (guiManager != null) {
						agent.sendMessage(guiManager, m);
					} else {// this is the gui manager itself
						agent.receiveMessage(m);
					}
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
