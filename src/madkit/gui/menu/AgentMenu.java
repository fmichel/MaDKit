/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.menu;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;

import madkit.action.AgentAction;
import madkit.kernel.AbstractAgent;

/**
 * An out of the box menu called <i>Agent</i> for Madkit Agent GUI.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */
public class AgentMenu extends JMenu {// TODO i18n

	/**
	 * 
	 */
	private static final long serialVersionUID = 7141072399712971987L;

	/**
	 * Creates a menu featuring: 
	 * {@link AgentAction#RELOAD}, 
	 * {@link AgentAction#LAUNCH_AGENT}, 
	 * {@link AgentAction#KILL_AGENT}
	 * 
	 * @param agent the agent for which this menu is created
	 */
	public AgentMenu(final AbstractAgent agent) {
		super("Agent");
		setMnemonic(KeyEvent.VK_A);
		try {
			if(agent.getClass().getConstructor((Class<?>[]) null) != null){
				add(AgentAction.RELOAD.getActionFor(agent));
				add(AgentAction.LAUNCH_AGENT.getActionFor(agent,agent.getClass().getName(),0,true));
			}
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		add(AgentAction.KILL_AGENT.getActionFor(agent,agent));
	}
	
}
