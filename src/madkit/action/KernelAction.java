/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.action;

import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_J;
import static java.awt.event.KeyEvent.VK_N;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_Y;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

import javax.swing.Action;
import javax.swing.JComponent;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.messages.KernelMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public enum KernelAction {

	EXIT(VK_E),
	COPY(VK_Y),
	RESTART(VK_R),
	LAUNCH_NETWORK(VK_N),
	STOP_NETWORK(VK_T),
//
//	//Actions that need parameters, i.e. not global
	LOAD_LOCAL_DEMOS(VK_D),
	LAUNCH_AGENT(VK_J),
	LAUNCH_SESSION(VK_DOLLAR),
	KILL_AGENT(VK_J),
	CONNECT_WEB_REPO(VK_DOLLAR),
	LOAD_JAR_FILE(VK_J); 


	//	final private MKAction mkAction;

	private ActionInfo actionInfo;
	final private int keyEvent;

//	final static ResourceBundle messages = I18nUtilities.getResourceBundle(KernelAction.class.getSimpleName());

	private KernelAction(int keyEvent){
		this.keyEvent = keyEvent;
	}
	
	@Override
	public String toString() {
		return MKAbstractAction.enumToMethodName(this);
	}

	public Action getActionFor(final AbstractAgent agent, final Object... info){
		return new MKAbstractAction(getActionInfo()){
			/**
			 * 
			 */
			private static final long serialVersionUID = -8907472475007112860L;

			@Override
			public void actionPerformed(ActionEvent e) {//TODO I could do the check validity here for logging purpose
				agent.sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, new KernelMessage(KernelAction.this, info));//TODO work with AA but this is probably worthless	
			}
	};
	//		return getMKAction().getNewInstanceFor(agent,info);
}

	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent);
		return actionInfo;
	}

public static void addAllActionsTo(JComponent menuOrToolBar, AbstractAgent agent){
	try {//this bypasses class incompatibility
		final Method add = menuOrToolBar.getClass().getMethod("add", Action.class);
		final Method addSeparator = menuOrToolBar.getClass().getMethod("addSeparator");
		for (KernelAction mkA : EnumSet.allOf(KernelAction.class)) {
			if(mkA == LAUNCH_AGENT)
				return;
			add.invoke(menuOrToolBar, mkA.getActionFor(agent));
			switch (mkA) {
			case EXIT:
			case RESTART:
			case STOP_NETWORK:
				addSeparator.invoke(menuOrToolBar);
			default:
				break;
			}
		}
	} catch (InvocationTargetException e) {
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
	} catch (IllegalAccessException e) {
		e.printStackTrace();
	} catch (SecurityException e) {
	} catch (NoSuchMethodException e) {
	}
}

}
