/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.action;

import static java.awt.event.KeyEvent.VK_N;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JOptionPane;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.i18n.I18nUtilities;
import madkit.i18n.Words;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import madkit.message.GUIMessage;
import madkit.message.KernelMessage;

/**
 * Enum representing operations which 
 * could be done by the default GUI manager of MaDKit.
 * It could be used by an agent to interact with the GUI manager
 * by creating {@link Action} using {@link #getActionFor(AbstractAgent, Object...)}.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @see Action
 * @version 0.9
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
	//	CONNECT_WEB_REPO(VK_W),

	/**
	 * Requests an agent frame creation.
	 * The corresponding action should be created
	 * by specifying the targeted agent:
	 * <pre><code>
	 * SETUP_AGENT_GUI.getActionFor(anAgent, targetedAgent);
	 * </code></pre>
	 * They could be identical.
	 * @see AbstractAgent#setupFrame(javax.swing.JFrame)
	 */
	SETUP_AGENT_GUI(KeyEvent.VK_DOLLAR), 
	LOG_LEVEL(KeyEvent.VK_DOLLAR),
	WARNING_LOG_LEVEL(KeyEvent.VK_DOLLAR),
	/**
	 * Requests an agent frame disposal: This will
	 * kill the agent.
	 * The corresponding action should be created
	 * by specify the targeted agent:
	 * <pre><code>
	 * SETUP_AGENT_GUI.getActionFor(anAgent, targetedAgent);
	 * </code></pre>
	 * They could be identical.
	 */
	DISPOSE_AGENT_GUI(KeyEvent.VK_DOLLAR);

	private ActionInfo actionInfo;
	final private int keyEvent;

	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(GUIManagerAction.class.getSimpleName());

	private GUIManagerAction(int keyEvent){
		this.keyEvent = keyEvent;
	}


	/**
	 * Returns an Action that will send to the GUI manager
	 * the corresponding request. 
	 * The corresponding action should be created
	 * by specifying the agent for which this action is created.
	 * Here is an example :
	 * <pre><code>
	 * KILL_AGENTS.getActionFor(anAgent);
	 * </code></pre>
	 * This will create an agent that will make <code>anAgent</code>
	 * send a message to the gui manager asking the kill of all GUI
	 * agents.
	 * @param agent the agent for which this Action will be created
	 * @param commandOptions optional information related to the action
	 * itself
	 * @return an Action that could be used in an GUI for instance
	 */
	public Action getActionFor(final AbstractAgent agent, final Object... commandOptions){
		if(this == CONNECT_TO_IP){
			return new MDKAbstractAction(getActionInfo()){
				/**
				 * 
				 */
				private static final long	serialVersionUID	= -5716094161691491218L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (agent.isAlive()) {
							try {
								final String ip = JOptionPane.showInputDialog(null, getActionInfo().getName()+" : ");
								if (ip != null) {
									agent.sendMessage(
											LocalCommunity.NAME,
											Groups.SYSTEM,
											Organization.GROUP_MANAGER_ROLE,
											new KernelMessage(KernelAction.CONNECT_TO_IP,
													InetAddress.getByName(ip)));
								}
							} catch (HeadlessException e1) {
								e1.printStackTrace();
							} catch (UnknownHostException e1) {
								JOptionPane.showMessageDialog(null, e1, Words.FAILED.toString(), JOptionPane.WARNING_MESSAGE);
							}
					}
				}
			};
		}
		return new MDKAbstractAction(getActionInfo()){
			/**
			 * 
			 */
			private static final long serialVersionUID = 2231685794614332333L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (agent.isAlive()) {
					final Message m = new GUIMessage(GUIManagerAction.this, commandOptions);
					final AgentAddress guiManager = agent.getAgentWithRole(LocalCommunity.NAME, Groups.GUI, Organization.GROUP_MANAGER_ROLE);
					if(guiManager != null){
						agent.sendMessage(guiManager, m);
					}
					else{//this is the gui manager itself
						agent.receiveMessage(m);
					}
				}
			}};
	}

	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent,messages);
		return actionInfo;
	}

}



