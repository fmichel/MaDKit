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

import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_W;

import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.util.ResourceBundle;

import javax.swing.Action;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent;
import madkit.message.KernelMessage;

/**
 * Enum representing kernel actions. This especially could
 * be used to communicate with the kernel in order to
 * trigger kernel's actions.
 * It could be used by any agent to interact with the kernel
 * by creating {@link Action} using {@link #getActionFor(AbstractAgent, Object...)}.
 * 
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.92
 * 
 */

public enum KernelAction {

	/**
	 * Close the kernel 
	 */
	EXIT(VK_Q),
	/**
	 *  Clone the kernel with its initial options
	 */
	COPY(VK_C),
	/**
	 *  Restart the kernel with its initial options
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
	 * Makes a redirection of the out and err 
	 * to a MaDKit agent.
	 */
	CONSOLE(VK_O),

	//	//Actions that need parameters, i.e. not global
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
	 * For connecting kernels in a wide area network. It requires a parameter of type {@link InetAddress}.
	 */
	CONNECT_TO_IP(VK_DOLLAR);
	


	private ActionInfo actionInfo;
	final private int keyEvent;

	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(KernelAction.class.getSimpleName());

	private KernelAction(int keyEvent){
		this.keyEvent = keyEvent;
	}

	/**
	 * Builds an action that will make the kernel do the
	 * corresponding operation if possible.
	 * 
	 * @param agent the agent that will send the message
	 * to the kernel
	 * @param parameters the info 
	 * @return the new corresponding action 
	 */
	public Action getActionFor(final AbstractAgent agent, final Object... parameters){
		return new MDKAbstractAction(getActionInfo()){
			/**
			 * 
			 */
			private static final long serialVersionUID = -8907472475007112860L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(agent.isAlive()){
				agent.sendMessage(
						LocalCommunity.NAME, 
						Groups.SYSTEM, 
						Organization.GROUP_MANAGER_ROLE, 
						new KernelMessage(KernelAction.this, parameters));//TODO work with AA but this is probably worthless	
				}
			}
		};
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
