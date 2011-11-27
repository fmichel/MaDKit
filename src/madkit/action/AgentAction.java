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

import static java.awt.event.KeyEvent.VK_E;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;

import madkit.kernel.AbstractAgent;
import madkit.messages.CommandMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public enum AgentAction {

	LAUNCH_AGENT(KeyEvent.VK_DOLLAR),
	RELOAD(VK_E), 
	LOG_LEVEL(KeyEvent.VK_DOLLAR),
	WARNING_LOG_LEVEL(KeyEvent.VK_DOLLAR),
	KILL_AGENT(KeyEvent.VK_DOLLAR);
//	final private static Method doIt;
//	static{
//		final Class<?>[] paramTypes = {CommandMessage.class};
//		Method m = null;
//		try {
//			m = AbstractAgent.class.getDeclaredMethod("proceedCommandMessage", paramTypes);
//		} catch (SecurityException e) {
//		} catch (NoSuchMethodException e) {
//		}
//		doIt = m;
//	}

	private ActionInfo actionInfo;
	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent);
		return actionInfo;
	}

	final private int keyEvent;

	private AgentAction(int keyEvent){
		this.keyEvent = keyEvent;
	}

	public Action getActionFor(final AbstractAgent agent, final Object... commandOptions){
		return new MKAbstractAction(getActionInfo()){
			/**
			 * 
			 */
			private static final long serialVersionUID = -3078505474395164899L;

			@Override
			public void actionPerformed(ActionEvent e) {//TODO I could do the check validity here for logging purpose
				agent.proceedCommandMessage(new CommandMessage<AgentAction>(AgentAction.this, commandOptions));
			}
	};
	//		return getMKAction().getNewInstanceFor(agent,info);
}
	

//public static void addAllGlobalActionsTo(JComponent menuOrToolBar, AbstractAgent agent){
//	try {//this bypasses class incompatibility
//		final Method add = menuOrToolBar.getClass().getMethod("add", Action.class);
//		final Method addSeparator = menuOrToolBar.getClass().getMethod("addSeparator");
//		for (AgentAction mkA : EnumSet.allOf(AgentAction.class)) {
//			add.invoke(menuOrToolBar, mkA.getActionFor(agent));
//			switch (mkA) {
//			case EXIT:
//				addSeparator.invoke(menuOrToolBar);
//			default:
//				break;
//			}
//			if(mkA == LAUNCH_AGENT)
//				return;
//		}
//	} catch (InvocationTargetException e) {
//	} catch (IllegalArgumentException e) {
//		e.printStackTrace();
//	} catch (IllegalAccessException e) {
//		e.printStackTrace();
//	} catch (SecurityException e) {
//	} catch (NoSuchMethodException e) {
//	}
//}


}
