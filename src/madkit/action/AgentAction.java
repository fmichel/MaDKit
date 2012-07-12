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

import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_E;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.Action;

import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent;
import madkit.message.EnumMessage;

/**
 * Enum representing agent actions
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
public enum AgentAction {

	LAUNCH_AGENT(KeyEvent.VK_L),
	RELOAD(VK_E), 
	CREATE_GROUP(VK_DOLLAR),
	REQUEST_ROLE(VK_DOLLAR),
	LEAVE_ROLE(VK_DOLLAR),
	LEAVE_GROUP(VK_DOLLAR),
	SEND_MESSAGE(VK_DOLLAR),
	BROADCAST_MESSAGE(VK_DOLLAR),
	KILL_AGENT(KeyEvent.VK_K)
	
	;

	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(AgentAction.class.getSimpleName());
	private ActionInfo actionInfo;
	/**
	 * @return the actionInfo corresponding to this constant
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent,messages);
		return actionInfo;
	}

	final private int keyEvent;

	private AgentAction(int keyEvent){
		this.keyEvent = keyEvent;
	}

	/**
	 * Builds an action that will make the agent do the
	 * corresponding behavior
	 * 
	 * @param agent the agent on which this action 
	 * will operate
	 * @param parameters the info to be used
	 * @return the action corresponding to the enum
	 */
	public Action getActionFor(final AbstractAgent agent, final Object... parameters){
		return new MDKAbstractAction(getActionInfo()){
			/**
			 * 
			 */
			private static final long serialVersionUID = -3078505474395164899L;

			@Override
			public void actionPerformed(ActionEvent e) {//TODO I could do the check validity here for logging purpose
				if (agent.isAlive()) {
					agent.proceedEnumMessage(new EnumMessage<AgentAction>(AgentAction.this, parameters));
				}
			}
	};
}
	
}
