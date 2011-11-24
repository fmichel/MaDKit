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
package madkit.kernel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import static java.awt.event.KeyEvent.*;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.gui.actions.Actions;
import madkit.gui.actions.AgentAction;
import madkit.gui.actions.MadkitAction;
import madkit.gui.actions.MadkitGUIAction;
import madkit.i18n.I18nUtilities;
import madkit.messages.CommandMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public enum AAAction {

	LAUNCH_AGENT(KeyEvent.VK_DOLLAR),
	RELOAD(VK_E), 
	KILL_AGENT(KeyEvent.VK_DOLLAR);

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

	private AAAction(int keyEvent){
		this.keyEvent = keyEvent;
	}

	public Action getActionFor(final AbstractAgent agent, final Object... commandOptions){
		return new MKAbstractAction(getActionInfo()){
			@Override
			public void actionPerformed(ActionEvent e) {//TODO I could do the check validity here for logging purpose
				agent.proceedCommandMessage(new CommandMessage<AAAction>(AAAction.this, commandOptions));
			}
	};
	//		return getMKAction().getNewInstanceFor(agent,info);
}
	

//public static void addAllGlobalActionsTo(JComponent menuOrToolBar, AbstractAgent agent){
//	try {//this bypasses class incompatibility
//		final Method add = menuOrToolBar.getClass().getMethod("add", Action.class);
//		final Method addSeparator = menuOrToolBar.getClass().getMethod("addSeparator");
//		for (AAAction mkA : EnumSet.allOf(AAAction.class)) {
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
